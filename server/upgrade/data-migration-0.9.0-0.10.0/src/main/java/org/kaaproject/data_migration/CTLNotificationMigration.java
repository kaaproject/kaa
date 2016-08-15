package org.kaaproject.data_migration;

import java.sql.Connection;

/**
 * Created by user223225 on 15.08.16.
 */
public class CTLNotificationMigration extends AbstractCTLMigration {


    public CTLNotificationMigration(Connection connection) {
        super(connection);
    }

    @Override
    protected String getPrefixTableName() {
        return "notification";
    }
}
