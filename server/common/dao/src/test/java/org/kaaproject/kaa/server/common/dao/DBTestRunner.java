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

package org.kaaproject.kaa.server.common.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

public abstract class DBTestRunner {

    protected static final String FORMATER = "{0}";

    public void truncateTables(DataSource dataSource) throws SQLException {
        Set<String> tableNames = getTableNames(dataSource);
        truncateTables(tableNames, dataSource);
    }

    private Set<String> getTableNames(DataSource dataSource) throws SQLException {
        Set<String> tableNames = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = prepareStatement(connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(1));
            }
        }
        return tableNames;
    }

    protected void truncateTables(Set<String> tableNames, DataSource dataSource) throws SQLException {
        if (tableNames == null || tableNames.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String tableName : tableNames) {
                statement.addBatch(MessageFormat.format(getTrancateSql(), tableName));
            }
            statement.executeBatch();
        }
    }

    protected abstract PreparedStatement prepareStatement(Connection connection) throws SQLException;

    protected abstract String getTrancateSql();

}
