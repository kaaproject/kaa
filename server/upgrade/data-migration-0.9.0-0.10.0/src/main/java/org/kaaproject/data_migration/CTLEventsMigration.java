package org.kaaproject.data_migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.kaaproject.data_migration.model.ConfigurationSchema;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.EventClass;
import org.kaaproject.data_migration.model.EventSchemaVersion;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user482400 on 05.08.16.
 */
public class CTLEventsMigration {
    private Connection connection;
    private AdminClient client = new AdminClient("localhost", 8080);

    private static final String EVENT_SCHEMA_VERSION_TABLE_NAME = "event_schems_versions";
    private static final String EVENT_CLASS_FAMILY_VERSION_TABLE_NAME = "event_class_family_versions";
    private static final String EVENT_CLASS_TABLE_NAME = "events_class";
    private static final String BASE_SCHEMA_TABLE_NAME = "base_schems";
    private static final String CTL_TABLE_NAME = "ctl";


    public CTLEventsMigration(Connection connection) {
        this.connection = connection;
    }

    public void transform() throws SQLException, IOException {
        /** Steps to migrate
         *
         * 1. remember (id,schems) :event_schems_versions
         * 2. drop column (schems) :event_schems_versions
         * 3. rename table event_schems_versions to events_class_family_versions
         * 4. rename column (events_class_family_id) to (events_class_family_versions_id) :events_class
         * 5. remember (id,schems,version) : events_class
         * 6. drop column (schems,version) : events_class
         * 7. (p.6 schems) search them as substring in (p.1 schems) and update (p.1 id) with (p.4 events_class_family_versions_id)
         * 8. AdminClient: save ctl schems based on bodies (p.5 schems) , application_id = null, tenant_id as select :event_class by (p.5 id)
         * 9. remember (p.5 EC, p.8 CTLDto)
         * 10. find max(id) :base_schems
         * 11. save base_schems:
         *                          id = (p.9 id + p.10) and update (id):events_class with this
         *                          created_time, created_username, version, ctl_id from (p.9 CTLDto)
         *                          name = parse "name" in (body) :ctl
         *                          description = null, application_id = null
         * 12. profit
         */

        QueryRunner run = new QueryRunner();

        //1
        ResultSetHandler<List<EventSchemaVersion>> esvHandler = new BeanListHandler<EventSchemaVersion>(EventSchemaVersion.class);
        List<EventSchemaVersion> oldESVs = run.query(connection, "SELECT id, schems FROM ?", esvHandler, EVENT_SCHEMA_VERSION_TABLE_NAME);
        //2
        run.update(connection, "ALTER TABLE ? DROP COLUMN schems", EVENT_SCHEMA_VERSION_TABLE_NAME);
        //3
        run.update(connection, "ALTER TABLE ? RENAME ?", EVENT_SCHEMA_VERSION_TABLE_NAME, EVENT_CLASS_FAMILY_VERSION_TABLE_NAME);
        //4
        run.update(connection, "ALTER TABLE ? CHANGE events_class_family_id events_class_family_versions_id bigint(20)", EVENT_CLASS_TABLE_NAME);
        //5
        ResultSetHandler<List<EventClass>> ecHandler = new BeanListHandler<EventClass>(EventClass.class);
        List<EventClass> oldECs = run.query(connection, "SELECT id, schems, version FROM ?", ecHandler, EVENT_CLASS_TABLE_NAME);
        //6
        run.update(connection, "ALTER TABLE ? DROP COLUMN schems", EVENT_CLASS_TABLE_NAME);
        run.update(connection, "ALTER TABLE ? DROP COLUMN version", EVENT_CLASS_TABLE_NAME);
        //7
        for (EventClass ec : oldECs) {
            updateFamilyVersionId(ec, oldESVs, run);
        }
        //8
        Map<EventClass, CTLSchemaDto> eventClassCTLMap = new HashMap<>();
        for (EventClass ec : oldECs) {
            String tenantId = run.query(connection, "select tenant_id from ?", rs -> rs.next() ? rs.getString("tenant_id") : null, BASE_SCHEMA_TABLE_NAME);
            CTLSchemaDto ctlDto = client.saveCTLSchemaWithAppToken(ec.getSchems(), tenantId, null);
            //9
            eventClassCTLMap.put(ec, ctlDto);
        }
        //10
        Long maxId = run.query(connection, "select max(id) as max_id from base_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
        //11
        List<Object[]> params = new ArrayList<>();
        for (Map.Entry<EventClass, CTLSchemaDto> entry : eventClassCTLMap.entrySet()) {
            EventClass ec = entry.getKey();
            CTLSchemaDto ctl = entry.getValue();

            run.update(connection, "update ? set id = (id + ?) where id = ?", EVENT_CLASS_TABLE_NAME, maxId, ec.getId());
            ec.setId(ec.getId()+maxId);

            String ctlBody = run.query(connection, "select body from ? where id = ?", rs -> rs.next() ? rs.getString("tenant_id") : null, CTL_TABLE_NAME, ctl.getId());

            params.add(new Object[]{
                    ec.getId(),
                    ctl.getCreatedTime(),
                    ctl.getCreatedUsername(),
                    null,
                    parseName(ctlBody),
                    ctl.getVersion(),
                    null,
                    ctl.getId()
            });
        }
        run.batch(connection, "insert into base_schems values(?, ?, ?, ?, ?, ?, ?, ?)", params.toArray(new Object[params.size()][]));
    }

    private void updateFamilyVersionId(EventClass ec, List<EventSchemaVersion> versions, QueryRunner run) throws SQLException {
        for (EventSchemaVersion esv : versions) {
            int updateCount = 0;
            if (ecBelongToThisFamilyVersion(ec, esv)) {
                updateCount = run.update(this.connection,
                        "UPDATE ? SET events_class_family_versions_id=? WHERE id=?",
                        EVENT_CLASS_TABLE_NAME, esv.getId(), ec.getId());
                if (updateCount > 0) ; //success

                break;
            }
        }
    }

    private boolean ecBelongToThisFamilyVersion(EventClass ec, EventSchemaVersion esv) {
        return new String(esv.getSchems()).contains(ec.getSchems());
    }

    private String parseName(String body) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(body);
        return jsonNode.get("name").asText();
    }
}
