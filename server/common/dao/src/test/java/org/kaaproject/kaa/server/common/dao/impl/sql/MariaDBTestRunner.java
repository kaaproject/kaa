package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.kaaproject.kaa.server.common.dao.DBTestRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by chvova on 21.03.16.
 */
public class MariaDBTestRunner extends DBTestRunner {
    @Override
    protected PreparedStatement prepareStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT tablename FROM pg_tables where schemaname = 'public'");
    }

    @Override
    protected String getTrancateSql() {
        return new StringBuilder("TRUNCATE TABLE ").append(FORMATER).append(" CASCADE").toString();
    }
}
