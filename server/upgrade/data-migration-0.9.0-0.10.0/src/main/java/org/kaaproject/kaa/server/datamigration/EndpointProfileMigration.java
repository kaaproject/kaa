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


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class EndpointProfileMigration {
    private String host;
    private String dbName;
    private String nosql;


    public EndpointProfileMigration(String host, String db, String nosql) {
        dbName = db;
        this.host = host;
        this.nosql = nosql;
    }

    /**
     * Add field use_raw_configuration_schema to endpointProfile that used to support devices using SDK version 0.9.0
     * */
    public void transform() {
        //mongo
        MongoClient client = new MongoClient(host);
        MongoDatabase database = client.getDatabase(dbName);
        MongoCollection<Document> endpointProfile = database.getCollection("endpoint_profile");
        endpointProfile.updateMany(new Document(), eq("$set", eq("use_raw_schema", false)));

        //cassandra
        Cluster cluster = Cluster.builder().addContactPoint(host).build();
        Session session = cluster.connect(dbName);
        session.execute("ALTER TABLE ep_profile ADD use_raw_schema boolean");
        session.close();
        cluster.close();

    }
}
