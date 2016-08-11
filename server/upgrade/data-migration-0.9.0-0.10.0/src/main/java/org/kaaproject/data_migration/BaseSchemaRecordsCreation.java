package org.kaaproject.data_migration;


import org.apache.commons.dbutils.QueryRunner;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.MigrationEntity;
import org.kaaproject.data_migration.model.Schema;
import org.kaaproject.data_migration.utils.datadefinition.DataDefinition;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BaseSchemaRecordsCreation {
    protected Connection connection;
    protected QueryRunner runner;
    protected DataDefinition dd;

    public BaseSchemaRecordsCreation(Connection connection) {
        this.connection = connection;
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
    }

    public void create(Map<Ctl, List<Schema>> ctlToSchemas) throws SQLException {
        List<MigrationEntity> baseSchemas =  new ArrayList<>();
        for (Ctl ctl : ctlToSchemas.keySet()) {
            for (Schema schema : ctlToSchemas.get(ctl)) {
                baseSchemas.add(new MigrationEntity(ctl, schema));
            }
        }

        // sort ids descending in order to avoid constraint violation during shifting ids
        baseSchemas.sort((m1, m2) -> m2.getSchema().getId().compareTo(m1.getSchema().getId()));


        Long shift = runner.query(connection, "select max(id) as max_id from base_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
        runner.update(connection, "update configuration_schems set id = id + " + shift + " order by id desc");
        schemas.forEach(s -> s.setId(s.getId() + shift));


        List<Object[]> params = new ArrayList<>();
        for (Ctl ctl : confSchemasToCTL.keySet()) {
            Schema schema = confSchemasToCTL.get(ctl);
            params.add(new Object[]{
                    schema.getId(),
                    schema.getCreatedTime(),
                    schema.getCreatedUsername(),
                    schema.getDescription(),
                    schema.getName(),
                    schema.getVersion(),
                    ctl.getMetaInfo().getAppId(),
                    ctl.getId()
            });
        }

        runner.batch(connection, "insert into base_schems values(?, ?, ?, ?, ?, ?, ?, ?)", params.toArray(new Object[schemas.size()][]));
    }

}
