package org.kaaproject.data_migration;

import org.apache.avro.Schema;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.kaaproject.data_migration.model.ConfigurationSchema;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.CtlMetaInfo;
import org.kaaproject.data_migration.utils.datadefinition.DataDefinition;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static org.kaaproject.data_migration.utils.Constants.HOST;
import static org.kaaproject.data_migration.utils.Constants.PORT;
import static org.kaaproject.data_migration.utils.datadefinition.Constraint.constraint;
import static org.kaaproject.data_migration.utils.datadefinition.ReferenceOptions.CASCADE;

public class CTLConfigurationMigration {
    private Connection connection;
    private final int NUM_OF_BASE_SCHEMA_FIELDS = 8;
    private AdminClient client;
    private QueryRunner runner;
    private DataDefinition dd;

    public CTLConfigurationMigration(Connection connection) {
        this.connection = connection;
        this.client = new AdminClient(HOST, PORT);
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
    }

    public CTLConfigurationMigration(Connection connection, String host, int port) {
        this.connection = connection;
        this.client = new AdminClient(host, port);
        runner = new QueryRunner();
        dd = new DataDefinition(connection);
    }


    private void beforeTransform() throws SQLException {
        dd.dropUnnamedFK("configuration_schems", "schems");
        dd.dropUnnamedFK("configuration", "configuration_schems");
        dd.alterTable("configuration")
                .add(constraint("FK_configuration_schems_id")
                        .foreignKey("configuration_schems_id")
                        .references("configuration_schems", "id")
                        .onDelete(CASCADE)
                        .onUpdate(CASCADE)
                )
                .execute();
    }


    public void transform() throws SQLException {
        try {
            beforeTransform();
            List<ConfigurationSchema> schemas = runner.query(connection,
                    "select conf.id as id, created_time as createdTime, created_username as createdUsername, description, name, schems, version, application_id as appId " +
                            "from configuration_schems conf join schems s on conf.id = s.id", new BeanListHandler<ConfigurationSchema>(ConfigurationSchema.class));

            String toDelete = schemas.stream().map(s -> s.getId().toString()).collect(joining(", "));
            runner.update(connection, "delete from schems where id in (" + toDelete + ")");

            Long shift = runner.query(connection, "select max(id) as max_id from base_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
            runner.update(connection, "update configuration_schems set id = id + " + shift + " order by id desc");
            schemas.forEach(s -> s.setId(s.getId() + shift));

            Map<Ctl, ConfigurationSchema> confSchemasToCTL = new HashMap<>();
            Long currentCTLMetaId = runner.query(connection, "select max(id) as max_id from ctl_metainfo", rs -> rs.next() ? rs.getLong("max_id") : null);
            Long currentCtlId = runner.query(connection, "select max(id) as max_id from ctl", rs -> rs.next() ? rs.getLong("max_id") : null);

            // CTL creation
            //TODO add check for already existed CTL schema to avoid constrain violation
            for (ConfigurationSchema schema : schemas) {
                currentCTLMetaId++;
                currentCtlId++;
                Schema schemaBody = new Schema.Parser().parse(schema.getSchems());
                String fqn = schemaBody.getFullName();
                RawSchema rawSchema = new RawSchema(schemaBody.toString());
                DefaultRecordGenerationAlgorithm<RawData> algotithm = new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
                String defaultRecord = algotithm.getRootData().getRawData();
                Long tenantId = runner.query(connection, "select tenant_id from application where id = " + schema.getAppId(), rs -> rs.next() ? rs.getLong("tenant_id") : null);
                runner.insert(connection, "insert into ctl_metainfo values(?, ?, ?, ?)", rs -> null, currentCTLMetaId, fqn, schema.getAppId(), tenantId);
                runner.insert(connection, "insert into ctl values(?, ?, ?, ?, ?, ?, ?)", rs -> null, currentCtlId, schema.getSchems(), schema.getCreatedTime(),
                        schema.getCreatedUsername(), defaultRecord, schema.getVersion(), currentCTLMetaId);


                Ctl ctl = new Ctl(currentCtlId, new CtlMetaInfo(fqn, schema.getAppId(), tenantId));
                confSchemasToCTL.put(ctl, schema);
            }

            List<Object[]> params = new ArrayList<>();
            for (Ctl ctl : confSchemasToCTL.keySet()) {
                ConfigurationSchema schema = confSchemasToCTL.get(ctl);
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
        } catch (SQLException | ConfigurationGenerationException | IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            DbUtils.closeQuietly(connection);
        }

    }


}
