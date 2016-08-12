package org.kaaproject.data_migration;


import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.data_migration.model.Configuration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.kaaproject.data_migration.utils.Utils.encodeUuids;

public class UpdateUuidsMigration {
    private Connection connection;

    public UpdateUuidsMigration(Connection connection) {
        this.connection = connection;
    }

    public void transform() throws IOException, SQLException {
            QueryRunner run = new QueryRunner();
            ResultSetHandler<List<Configuration>> rsHandler = new BeanListHandler<Configuration>(Configuration.class);
            List<Configuration> configs = run.query(connection, "SELECT * FROM configuration", rsHandler);
            for (Configuration config : configs) {
                JsonNode json = new ObjectMapper().readTree(config.getConfiguration_body());
                JsonNode jsonEncoded = encodeUuids(json);
                byte[] encodedConfigurationBody = jsonEncoded.toString().getBytes();

                int updates = run.update(connection, "UPDATE configuration SET configuration_body=? WHERE id=?", encodedConfigurationBody, config.getId());
                if (updates != 1) {
                    System.err.println("Error: failed to update configuration: " + config);
                }
            }
    }

}
