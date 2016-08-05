package org.kaaproject.data_migration;


import org.apache.commons.dbutils.QueryRunner;
import org.kaaproject.data_migration.utils.DataSources;

import java.sql.SQLException;

import static org.kaaproject.data_migration.utils.DataSources.MARIADB;

public class MigrateData {
    public static void main(String[] args) throws SQLException {
        CTLConfigurationMigration migration = new CTLConfigurationMigration(MARIADB.getDs().getConnection());
        migration.transform();
    }
}
