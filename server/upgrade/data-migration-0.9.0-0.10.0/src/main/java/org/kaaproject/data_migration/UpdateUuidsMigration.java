/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.data_migration;

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
