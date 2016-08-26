package org.kaaproject.data_migration;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.kaaproject.data_migration.model.Schema;
import org.kaaproject.data_migration.utils.Options;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


public class CTLNotificationMigration extends AbstractCTLMigration {

    private MongoClient client;
    private String dbName;
    private String nosql;

    public CTLNotificationMigration(Connection connection, String host, String db, String nosql) {
        super(connection);
        client = new MongoClient(host);
        dbName = db;
        this.nosql = nosql;
    }


    @Override
    protected List<Schema> transform() throws SQLException {
        List<Schema> res = super.transform();

        if(nosql.equals(Options.DEFAULT_NO_SQL)) {
            MongoDatabase database = client.getDatabase(dbName);
            MongoCollection<Document> notification = database.getCollection("notification");
            MongoCollection<Document> enpNotification = database.getCollection("endpoint_notification");

            FindIterable<Document> cursor = notification.find();
            for (Document document : cursor) {
                Object id = document.get("_id");
                Long schemaId = Long.parseLong((String) document.get("notification_schema_id"));
                notification.updateMany(eq("_id", id), eq("$set", eq("notification_schema_id", schemaId + idShift)));
            }

            cursor = enpNotification.find();
            for (Document document : cursor) {
                Object id = document.get("_id");
                Long schemaId = Long.parseLong((String) document.get("notification.notification_schema_id"));
                notification.updateMany(eq("_id", id), eq("$set", eq("notification.notification_schema_id", schemaId + idShift)));
            }
        } else {

            //TODO update for cassandra
        }
        return res;
    }

    @Override
    protected String getPrefixTableName() {
        return "notification";
    }
}
