package org.kaaproject.data_migration;


import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.CtlMetaInfo;
import org.kaaproject.data_migration.model.Schema;
import org.kaaproject.data_migration.utils.datadefinition.DataDefinition;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTLAggregation {
    private Connection connection;
    private QueryRunner runner;
    private DataDefinition dd;

    public CTLAggregation(Connection connection) {
        this.connection = connection;
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
    }


    public Map<Ctl, List<Schema>> aggregate(List<Schema> schemas) throws SQLException, ConfigurationGenerationException, IOException {
        Map<Ctl, List<Schema>> confSchemasToCTL = new HashMap<>();
        Long currentCTLMetaId = runner.query(connection, "select max(id) as max_id from ctl_metainfo", rs -> rs.next() ? rs.getLong("max_id") : null);
        Long currentCtlId = runner.query(connection, "select max(id) as max_id from ctl", rs -> rs.next() ? rs.getLong("max_id") : null);

        // CTL creation
        //TODO add check for already existed CTL schema to avoid constrain violation
        for (Schema schema : schemas) {
            currentCTLMetaId++;
            currentCtlId++;
            org.apache.avro.Schema schemaBody = new org.apache.avro.Schema.Parser().parse(schema.getSchems());
            String fqn = schemaBody.getFullName();
            RawSchema rawSchema = new RawSchema(schemaBody.toString());
            DefaultRecordGenerationAlgorithm<RawData> algotithm = new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
            String defaultRecord = algotithm.getRootData().getRawData();
            Long tenantId = runner.query(connection, "select tenant_id from application where id = " + schema.getAppId(), rs -> rs.next() ? rs.getLong("tenant_id") : null);
            Ctl ctl = new Ctl(currentCtlId, new CtlMetaInfo(fqn, schema.getAppId(), tenantId));


            runner.insert(connection, "insert into ctl_metainfo values(?, ?, ?, ?)", new ScalarHandler<Long>(), currentCTLMetaId, fqn, schema.getAppId(), tenantId);
            runner.insert(connection, "insert into ctl values(?, ?, ?, ?, ?, ?, ?)", new ScalarHandler<Long>(), currentCtlId, schema.getSchems(), schema.getCreatedTime(),
                    schema.getCreatedUsername(), defaultRecord, schema.getVersion(), currentCTLMetaId);


            confSchemasToCTL.put(ctl, schema);
        }

    }
}
