package org.kaaproject.data_migration;


import org.apache.avro.Schema;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.kaaproject.data_migration.model.Configuration;
import org.kaaproject.data_migration.model.ConfigurationSchema;
import org.kaaproject.data_migration.model.Ctl;
import org.kaaproject.data_migration.model.CtlMetaInfo;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class CTLConfigurationMigration {
//    private static final Logger LOG = LoggerFactory.getLogger(CTLConfigurationMigration.class.getSimpleName());

    private static final String UUID_FIELD = "__uuid";
    private static final String UUID_VALUE = "org.kaaproject.configuration.uuidT";

    private Connection connection;
    private final int NUM_OF_BASE_SCHEMA_FIELDS = 8;
    private AdminClient client = new AdminClient("localhost", 8080);

    public CTLConfigurationMigration(Connection connection) {
        this.connection = connection;
    }

    public void transform() throws SQLException, IOException {
        updateUuids();

        QueryRunner runner = new QueryRunner();
        try {
            List<ConfigurationSchema> schemas  = runner.query(connection, "select conf.id as id, created_time as createdTime, created_username as createdUsername, description, name, schems, version, application_id as appId " +
                    "from configuration_schems conf join schems s on conf.id = s.id", new BeanListHandler<ConfigurationSchema>(ConfigurationSchema.class));
            String toDelete = schemas.stream().map(s -> s.getId().toString()).collect(joining(", "));

            runner.update(connection, "delete from schems where id in (" + toDelete + ")");

            Long shift = runner.query(connection, "select max(id) as max_id from base_schems", rs -> rs.next() ? rs.getLong("max_id") : null);
            runner.update(connection, "update configuration_schems set id = id + " + shift + " order by id desc");
            schemas.forEach(s -> s.setId(s.getId() + shift));
            Map<Ctl, List<ConfigurationSchema>> confSchemasToCTL = new HashMap<>();

            // CTL creation
            for (ConfigurationSchema schema : schemas) {
                Long tenantId = runner.query(connection, "select tenant_id from application where id = " + schema.getAppId(), rs -> rs.next() ? rs.getLong("tenant_id") : null);
                CTLSchemaDto ctlSchemaDto = client.saveCTLSchemaWithAppToken(schema.getSchems(), tenantId.toString(), schema.getAppId().toString());
                // aggregate configuration schemas with same fqn
                Ctl ctl = new Ctl(Long.parseLong(ctlSchemaDto.getId()), new CtlMetaInfo(ctlSchemaDto.getMetaInfo().getFqn(), schema.getAppId(), tenantId));
                if (confSchemasToCTL.containsKey(ctl)) {
                    List<ConfigurationSchema> list = confSchemasToCTL.get(ctl);
                    list.add(schema);
                    confSchemasToCTL.put(ctl, list);
                } else {
                    confSchemasToCTL.put(ctl, asList(schema));
                }
            }

            List<Object[]> params = new ArrayList<>();
            for (Ctl ctl : confSchemasToCTL.keySet()) {
                for (ConfigurationSchema schema : confSchemasToCTL.get(ctl)) {
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
            }

            runner.batch(connection, "insert into base_schems values(?, ?, ?, ?, ?, ?, ?, ?)", params.toArray(new Object[schemas.size()][]));
        } catch (SQLException e) {
            DbUtils.rollback(connection);
        } finally {
            DbUtils.closeQuietly(connection);
        }

    }

    private void updateUuids() throws SQLException, IOException {
        QueryRunner run = new QueryRunner();

        ResultSetHandler<List<Configuration>> rsHandler = new BeanListHandler<Configuration>(Configuration.class);
        try {
            List<Configuration> configs = run.query(this.connection, "SELECT * FROM configuration", rsHandler);
            for (Configuration config : configs) {
                JsonNode json = new ObjectMapper().readTree(config.getConfiguration_body());
                JsonNode jsonEncoded = encodeUuids(json);
                byte[] encodedConfigurationBody = jsonEncoded.toString().getBytes();

                int updates = run.update(this.connection, "UPDATE configuration SET configuration_body=? WHERE id=?", encodedConfigurationBody,config.getId());
                if (updates != 1) {
//                    LOG.error("Failed to update configuration: {}", config);
                } else {
//                    LOG.info("Updated configuration: {}", config);
                }
            }
        } catch (SQLException e) {
//            LOG.error("Failed to load configurations. {}", e);
        } finally {
            DbUtils.close(this.connection);
        }
    }

    private JsonNode encodeUuids(JsonNode json) throws IOException {
        if (json.has(UUID_FIELD)) {
            JsonNode j = json.get(UUID_FIELD);
            if (j.has(UUID_VALUE)) {
                String value = j.get(UUID_VALUE).asText();
                String encodedValue = Base64.getEncoder().encodeToString(value.getBytes("ISO-8859-1"));
                ((ObjectNode)j).put(UUID_VALUE, encodedValue);
            }
        }

        for (JsonNode node : json) {
            if (node.isContainerNode()) encodeUuids(node);
        }

        return json;
    }
}
