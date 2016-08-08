package org.kaaproject.data_migration.utils;


import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Utils {

    public static void runFile(QueryRunner runner, Connection connection, String fileName) throws IOException, SQLException {
        String query = IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(fileName));
        runner.update(connection, query);
    }

}
