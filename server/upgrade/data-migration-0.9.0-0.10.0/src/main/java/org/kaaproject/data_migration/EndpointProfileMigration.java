package org.kaaproject.data_migration;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.kaaproject.data_migration.utils.Options;

import java.sql.Connection;

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
        Cluster cluster = Cluster.builder().addContactPoint(host).build();
        MongoClient client = new MongoClient(host);

        //mongo
        MongoDatabase database = client.getDatabase(dbName);
        MongoCollection<Document> endpointProfile = database.getCollection("endpoint_profile");
        endpointProfile.updateMany(new Document(), eq("$set", eq("use_raw_schema", false)));

        //cassandra
//        Session session = cluster.connect(dbName);
//        session.execute("");
//        session.close();
//        cluster.close();

    }
}
