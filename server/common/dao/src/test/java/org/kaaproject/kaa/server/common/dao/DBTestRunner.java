/*
 * Copyright 2014 CyberVision, Inc.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

public abstract class DBTestRunner {

    protected static final String FORMATER = "{0}";

    public void truncateTables(DataSource dataSource) throws SQLException {
        Set<String> tableNames = getTableNames(dataSource);
        truncateTables(tableNames, dataSource);
    }
    
//    public void truncateSequences(DataSource dataSource) throws SQLException {
//        Set<String> sequenceNames = getSequenceNames(dataSource);
//        truncateSequences(sequenceNames, dataSource);
//    }

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
    
//    private Set<String> getSequenceNames(DataSource dataSource) throws SQLException {
//        Set<String> sequenceNames = new HashSet<>();
//        try (Connection connection = dataSource.getConnection();
//                PreparedStatement preparedStatement = prepareGetSequencesStatement(connection);
//                ResultSet resultSet = preparedStatement.executeQuery()) {
//            while (resultSet.next()) {
//                sequenceNames.add(resultSet.getString(1));
//            }
//        }
//        return sequenceNames;
//    }

    private void truncateTables(Set<String> tableNames, DataSource dataSource) throws SQLException {
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
    
//    private void truncateSequences(Set<String> sequenceNames, DataSource dataSource) throws SQLException {
//        if (sequenceNames == null || sequenceNames.isEmpty()) {
//            return;
//        }
//        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
//            for (String sequenceName : sequenceNames) {
//                statement.addBatch(MessageFormat.format(getTrancateSequenceSql(), sequenceName));
//            }
//            statement.executeBatch();
//        }
//    }

    protected abstract PreparedStatement prepareStatement(Connection connection) throws SQLException;
    
//    protected abstract PreparedStatement prepareGetSequencesStatement(Connection connection) throws SQLException;

    protected abstract String getTrancateSql();
    
 //   protected abstract String getTrancateSequenceSql();

}
