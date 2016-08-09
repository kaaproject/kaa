package org.kaaproject.data_migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.kaaproject.data_migration.model.EventClass;
import org.kaaproject.data_migration.model.EventSchemaVersion;
import org.kaaproject.data_migration.utils.datadefinition.DataDefinition;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTLEventsMigration {
    private Connection connection;
    private AdminClient client = new AdminClient("localhost", 8080);

    private static final String EVENT_SCHEMA_VERSION_TABLE_NAME = "event_schems_versions";
    private static final String EVENT_CLASS_FAMILY_TABLE_NAME = "events_class_family";
    private static final String EVENT_CLASS_FAMILY_VERSION_TABLE_NAME = "events_class_family_versions";
    private static final String EVENT_CLASS_TABLE_NAME = "events_class";
    private static final String BASE_SCHEMA_TABLE_NAME = "base_schems";
    private static final String CTL_TABLE_NAME = "ctl";
    private DataDefinition dd;

    public CTLEventsMigration(Connection connection) {
        this.connection = connection;
        this.dd = new DataDefinition(connection);
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
         * 8. AdminClient: save ctl schems based on bodies (p.5 schems) , application_id = null, tenant_id as select :events_class by (p.5 id)
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
        List<EventSchemaVersion> oldESVs = run.query(connection, "SELECT id, schems FROM " + EVENT_SCHEMA_VERSION_TABLE_NAME, esvHandler);
        //2
        run.update(connection, "ALTER TABLE " + EVENT_SCHEMA_VERSION_TABLE_NAME + " DROP COLUMN schems");
        //3
        run.update(connection, "ALTER TABLE " + EVENT_SCHEMA_VERSION_TABLE_NAME + " RENAME " + EVENT_CLASS_FAMILY_VERSION_TABLE_NAME);
        //4
        dd.dropUnnamedFK(EVENT_CLASS_TABLE_NAME, EVENT_CLASS_FAMILY_TABLE_NAME);
        run.update(connection, "ALTER TABLE " + EVENT_CLASS_TABLE_NAME + " CHANGE events_class_family_id events_class_family_versions_id bigint(20)");
        //5
        ResultSetHandler<List<EventClass>> ecHandler = new BeanListHandler<EventClass>(EventClass.class);
        List<EventClass> oldECs = run.query(connection, "SELECT id, schems, version FROM " + EVENT_CLASS_TABLE_NAME, ecHandler);
        //6
        run.update(connection, "ALTER TABLE " + EVENT_CLASS_TABLE_NAME + " DROP COLUMN schems");
        run.update(connection, "ALTER TABLE " + EVENT_CLASS_TABLE_NAME + " DROP COLUMN version");
        //7
        for (EventClass ec : oldECs) {
            updateFamilyVersionId(ec, oldESVs, run);
        }
        //8
        Map<EventClass, CTLSchemaDto> eventClassCTLMap = new HashMap<>();
        for (EventClass ec : oldECs) {
            String tenantId = run.query(connection, "select tenant_id from " + EVENT_CLASS_TABLE_NAME + " where id=?",
                    rs -> rs.next() ? rs.getString("tenant_id") : null, ec.getId());

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

            run.update(connection, "update " + EVENT_CLASS_TABLE_NAME + " set id = (id + ?) where id = ?", maxId, ec.getId());
            ec.setId(ec.getId()+maxId);

            String ctlBody = run.query(connection, "select body from " + CTL_TABLE_NAME + " where id = ?",
                    rs -> rs.next() ? rs.getString("tenant_id") : null, ctl.getId());

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

    private void updateFamilyVersionId(EventClass ec, List<EventSchemaVersion> versions, QueryRunner run) throws SQLException, IOException {
        for (EventSchemaVersion esv : versions) {
            int updateCount = 0;
            if (ecBelongToThisFamilyVersion(ec, esv)) {
                updateCount = run.update(this.connection,
                        "UPDATE " + EVENT_CLASS_TABLE_NAME + " SET events_class_family_versions_id=? WHERE id=?",
                        esv.getId(), ec.getId());
                if (updateCount > 0) ; //success

                break;
            }
        }
    }

    private boolean ecBelongToThisFamilyVersion(EventClass ec, EventSchemaVersion esv) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(ec.getSchems());
        String namespace = jsonNode.get("namespace").asText();
        String name = jsonNode.get("name").asText();

        return esv.getSchems().contains(name) && esv.getSchems().contains(namespace);
    }

    private String parseName(String body) throws IOException {
        JsonNode jsonNode = new ObjectMapper().readTree(body);
        return jsonNode.get("name").asText();
    }
}
