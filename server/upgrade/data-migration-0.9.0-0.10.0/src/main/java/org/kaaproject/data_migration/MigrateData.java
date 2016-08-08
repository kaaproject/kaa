package org.kaaproject.data_migration;

import java.io.IOException;
import java.sql.SQLException;

import static org.kaaproject.data_migration.utils.DataSources.MARIADB;

public class MigrateData {
    public static void main(String[] args) throws SQLException, IOException {
        CTLConfigurationMigration migration = new CTLConfigurationMigration(MARIADB.getDs().getConnection());
        migration.transform();

        CTLEventsMigration eventsMigration = new CTLEventsMigration(MARIADB.getDs().getConnection());
        eventsMigration.transform();
    }
}
