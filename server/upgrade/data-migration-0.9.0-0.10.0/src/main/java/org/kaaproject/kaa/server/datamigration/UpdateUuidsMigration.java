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

package org.kaaproject.kaa.server.datamigration;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.bson.Document;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.datamigration.model.Configuration;
import org.kaaproject.kaa.server.datamigration.utils.Options;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static org.kaaproject.kaa.server.datamigration.utils.Utils.encodeUuids;

public class UpdateUuidsMigration {
    private Connection connection;
    private MongoClient client;
    private Cluster cluster;
    private String dbName;
    private String nosql;

    public UpdateUuidsMigration(Connection connection, String host, String db, String nosql) {
        this.connection = connection;
        client = new MongoClient(host);
        cluster = Cluster.builder()
                .addContactPoint(host)
                .build();
        dbName = db;
        this.nosql = nosql;
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

        if (nosql.equals(Options.DEFAULT_NO_SQL)) {
            MongoDatabase database = client.getDatabase(dbName);
            MongoCollection<Document> userConfiguration = database.getCollection("user_configuration");
            FindIterable<Document> documents = userConfiguration.find();
            for (Document d : documents) {
                String body = (String) d.get("body");
                JsonNode json = new ObjectMapper().readTree(body);
                JsonNode jsonEncoded = encodeUuids(json);
                userConfiguration.updateOne(Filters.eq("_id", d.get("_id")), Filters.eq("$set", Filters.eq("body", jsonEncoded)));
            }

        } else {
            Session session = cluster.connect(dbName);
            BatchStatement batchStatement = new BatchStatement();

            String tableName = "user_conf";
            ResultSet results = session.execute(select().from(tableName));
            for (Row row : results) {
                String userId = row.getString("user_id");
                String appToken = row.getString("app_token");
                int schemaVersion = row.getInt("schema_version");

                String body = row.getString("body");
                String bodyEncoded = encodeUuids(new ObjectMapper().readTree(body)).toString();

                batchStatement.add(
                        update(tableName)
                                .with(set("body", bodyEncoded))
                                .where(eq("user_id", userId))
                                .and(eq("app_token", appToken))
                                .and(eq("schema_version", schemaVersion))
                );
            }

            session.execute(batchStatement);
            session.close();
            cluster.close();
        }

    }

}
