package org.kaaproject.data_migration.utils;


import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Utils {

    private static final String QUERY_FIND_FK_NAME = "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_SCHEMA = 'kaa' AND TABLE_NAME = '%s' and referenced_table_name='%s'";

    public static void runFile(QueryRunner runner, Connection connection, String fileName) throws IOException, SQLException {
        String query = IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(fileName));
        runner.update(connection, query);
    }

    public static void dropFK(Connection connection, String tableName, String referencedTableName) throws SQLException {
        QueryRunner runner = new QueryRunner();

        String query = String.format(QUERY_FIND_FK_NAME, tableName, referencedTableName);
        String fkName = runner.query(connection, query, rs -> rs.next() ? rs.getString(1) : null);
        runner.update(connection, "ALTER TABLE " + tableName + " DROP FOREIGN KEY " + fkName);
    }

}
