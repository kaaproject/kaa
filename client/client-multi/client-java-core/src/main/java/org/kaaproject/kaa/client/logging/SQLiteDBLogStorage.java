/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.client.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDBLogStorage implements LogStorage, LogStorageStatus {

    private static final Logger LOG = LoggerFactory.getLogger(SQLiteDBLogStorage.class);

    private static final String SQLITE_URL = "jdbc:sqlite:test.db";
    private static final String TABLE_NAME = "kaa_logs";
    private static final String RECORD_ID_COLUMN = "record_id";
    private static final String BUCKET_ID_COLUMN = "bucket_id";
    private static final String LOG_DATA_COLUMN = "log_data";
    private static final String BUCKET_ID_INDEX_NAME = "KAA_BUCKET_ID_INDEX";

    private static final String KAA_CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("  +
                                                        RECORD_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                        BUCKET_ID_COLUMN + " INTEGER, " +
                                                        LOG_DATA_COLUMN + " BLOB);";

    private static final String KAA_CREATE_BUCKET_ID_INDEX  = "CREATE INDEX IF NOT EXISTS " + BUCKET_ID_INDEX_NAME + " " +
                                                              " ON " + TABLE_NAME +  " (" + BUCKET_ID_COLUMN + ");";

    private static final String KAA_HOW_MANY_LOGS_IN_DB = "SELECT COUNT(*), SUM(LENGTH(" +
                                                          LOG_DATA_COLUMN + ")) FROM " + TABLE_NAME + ";";

    private static final String KAA_INSERT_NEW_RECORD = "INSERT INTO " + TABLE_NAME + " (" + LOG_DATA_COLUMN + ") " +
                                                        "VALUES (?);";

    private PreparedStatement insertStatement;

    private long recordCount;
    private long consumedSize;

    private final Connection connection;

    public SQLiteDBLogStorage() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_URL);
            initTable();
            retrieveConsumedSizeAndVolume();
        } catch (ClassNotFoundException e) {
            LOG.error("Can't find SQLite classes in classpath", e);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            LOG.error("Error while initializing SQLite DB and its tables", e);
            throw new RuntimeException(e);
        }

        LOG.debug("SQLite connection was successfully established");
    }

    @Override
    public void addLogRecord(LogRecord record) {
        synchronized (connection) {
            LOG.trace("Adding new log record...");
            if (insertStatement == null) {
                try {
                    insertStatement = connection.prepareStatement(KAA_INSERT_NEW_RECORD);
                } catch (SQLException e) {
                    LOG.error("Can't create an insert statement: {}", KAA_INSERT_NEW_RECORD, e);
                    return;
                }
            }

            try {
                insertStatement.setBytes(1, record.getData());
                int affectedRows = insertStatement.executeUpdate();
                if (affectedRows == 1) {
                    consumedSize += record.getSize();
                    recordCount++;
                    LOG.trace("Added new log record, records: {}", recordCount);
                }
            } catch (SQLException e) {
                LOG.error("Can't add a new record", e);
                return;
            }
        }
    }

    @Override
    public LogStorageStatus getStatus() {
        return this;
    }

    @Override
    public LogBlock getRecordBlock(long blockSize) {
        return null;
    }

    @Override
    public void removeRecordBlock(int id) {

    }

    @Override
    public void notifyUploadFailed(int id) {

    }

    @Override
    public long getConsumedVolume() {
        return consumedSize;
    }

    @Override
    public long getRecordCount() {
        return recordCount;
    }

    private void initTable() throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(KAA_CREATE_LOG_TABLE);
            statement.executeUpdate(KAA_CREATE_BUCKET_ID_INDEX);
        } finally {
            tryCloseStatement(statement);
        }
    }

    private void retrieveConsumedSizeAndVolume() throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(KAA_HOW_MANY_LOGS_IN_DB);
            rs.next();
            recordCount = rs.getLong(1);
            consumedSize = rs.getLong(1);
        } finally {
            tryCloseStatement(statement);
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Can't close SQLite db connection", e);
        }
    }

    private void tryCloseStatement(Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }
}
