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

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static java.lang.Long.parseLong;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.Bytes;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.kaaproject.kaa.server.datamigration.model.Schema;
import org.kaaproject.kaa.server.datamigration.utils.Options;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


public class CtlNotificationMigration extends AbstractCtlMigration {
  private MongoClient client;
  private Cluster cluster;
  private String dbName;
  private String nosql;

  /**
   * Create entity that responsible for data migration from old tables notification to
   * new ctl based ones.
   *
   * @param connection the connection to relational database
   * @param options    the options for configuring NoSQL databases
   */
  public CtlNotificationMigration(Connection connection, Options options) {
    super(connection);
    client = new MongoClient(options.getHost());
    cluster = Cluster.builder()
        .addContactPoint(options.getHost())
        .build();
    dbName = options.getDbName();
    this.nosql = options.getDbName();
  }


  @Override
  protected List<Schema> transform() throws SQLException {
    List<Schema> res = super.transform();

    if (nosql.equals(Options.DEFAULT_NO_SQL)) {
      MongoDatabase database = client.getDatabase(dbName);
      MongoCollection<Document> notification = database.getCollection("notification");
      MongoCollection<Document> enpNotification = database.getCollection("endpoint_notification");

      FindIterable<Document> cursor = notification.find();
      for (Document document : cursor) {
        Object id = document.get("_id");
        Long schemaId = parseLong((String) document.get("notification_schema_id"));
        notification.updateMany(
            Filters.eq("_id", id),
            Filters.eq("$set", Filters.eq("notification_schema_id", schemaId + idShift))
        );
      }

      cursor = enpNotification.find();
      for (Document document : cursor) {
        Object id = document.get("_id");
        Long schemaId = parseLong((String) document.get("notification.notification_schema_id"));
        notification.updateMany(
            Filters.eq("_id", id),
            Filters.eq("$set", Filters.eq(
                "notification.notification_schema_id",
                schemaId + idShift)
            )
        );
      }
    } else {
      Session session = cluster.connect(dbName);
      BatchStatement batchStatement = new BatchStatement();

      //notification
      ResultSet results = session.execute(select().from("notification"));
      for (Row row : results) {
        String id = row.getString("nf_id");
        Long schemaId = parseLong(row.getString("schema_id"));
        String[] ids = id.split("::");

        batchStatement.add(
            update("notification")
                .with(set("schema_id", String.valueOf(schemaId + idShift)))
                .where(eq("topic_id", ids[0]))
                .and(eq("nf_type", ids[1]))
                .and(eq("nf_version", Integer.valueOf(ids[2])))
                .and(eq("seq_num", Integer.valueOf(ids[3])))
        );
      }

      //endpoint notification
      results = session.execute(select().from("ep_nfs"));
      for (Row row : results) {
        String id = row.getString("nf_id");
        Long schemaId = parseLong(row.getString("schema_id"));
        String[] ids = id.split("::");
        ByteBuffer epKeyHash = Bytes.fromHexString(ids[0]);
        Date lastModTime = new Date(Long.valueOf(ids[1]));

        batchStatement.add(
            update("ep_nfs")
                .with(set("schema_id", String.valueOf(schemaId + idShift)))
                .where(eq("ep_key_hash", epKeyHash))
                .and(eq("last_mod_time", lastModTime))
        );
      }

      session.execute(batchStatement);
      session.close();
      cluster.close();
    }
    return res;
  }

  @Override
  protected String getPrefixTableName() {
    return "notification";
  }
}
