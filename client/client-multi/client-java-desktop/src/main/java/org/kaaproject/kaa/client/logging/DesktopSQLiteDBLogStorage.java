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

package org.kaaproject.kaa.client.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DesktopSQLiteDBLogStorage implements LogStorage, LogStorageStatus {

    private static final Logger LOG = LoggerFactory.getLogger(DesktopSQLiteDBLogStorage.class);

    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    private PreparedStatement insertStatement;
    private PreparedStatement deleteByBucketIdStatement;
    private PreparedStatement resetBucketIdStatement;
    private PreparedStatement selectUnmarkedStatement;

    private long totalRecordCount;
    private long unmarkedRecordCount;
    private long unmarkedConsumedSize;
    private int currentBucketId = 1;

    private long currentBucketSize;
    private int currentRecordCount;

    private long maxBucketSize;
    private int maxRecordCount;

    private Map<Integer, Long> consumedMemoryStorage = new HashMap<>();

    private final Connection connection;

    public DesktopSQLiteDBLogStorage(long maxBucketSize, int maxRecordCount) {
        this(PersistentLogStorageConstants.DEFAULT_DB_NAME, maxBucketSize, maxRecordCount);
    }

    public DesktopSQLiteDBLogStorage(String dbName, long maxBucketSize, int maxRecordCount) {
        try {
            this.maxBucketSize = maxBucketSize;
            this.maxRecordCount = maxRecordCount;
            Class.forName("org.sqlite.JDBC");
            String dbURL = SQLITE_URL_PREFIX + dbName;
            LOG.info("Connecting to db by url: {}", dbURL);
            connection = DriverManager.getConnection(dbURL);
            LOG.debug("SQLite connection was successfully established");
            initTable();
            truncateIfBucketSizeIncompatible();
            retrieveConsumedSizeAndVolume();
            if (totalRecordCount > 0) {
                retrieveBucketId();
                resetBucketIDs();
            }
        } catch (ClassNotFoundException e) {
            LOG.error("Can't find SQLite classes in classpath", e);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            LOG.error("Error while initializing SQLite DB and its tables", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public BucketInfo addLogRecord(LogRecord record) {
        synchronized (connection) {
            LOG.trace("Adding a new log record...");
            if (insertStatement == null) {
                try {
                    insertStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_INSERT_NEW_RECORD);
                } catch (SQLException e) {
                    LOG.error("Can't create row insert statement", e);
                    throw new RuntimeException(e);
                }
            }

            long leftConsumedSize = maxBucketSize - currentBucketSize;
            long leftRecordCount = maxRecordCount - currentRecordCount;

            if (leftConsumedSize < record.getSize() || leftRecordCount == 0) {
                moveToNextBucket();
            }

            try {
                insertStatement.setInt(1, currentBucketId);
                insertStatement.setBytes(2, record.getData());
                int affectedRows = insertStatement.executeUpdate();
                if (affectedRows == 1) {
                    currentBucketSize += record.getSize();
                    currentRecordCount++;

                    unmarkedConsumedSize += record.getSize();
                    unmarkedRecordCount++;
                    totalRecordCount++;
                    LOG.trace("Added a new log record, total record count: {}, data: {}, unmarked record count: {}",
                            totalRecordCount, record.getData(), unmarkedRecordCount);
                } else {
                    LOG.warn("No log record was added");
                }
            } catch (SQLException e) {
                LOG.error("Can't add a new record", e);
            }
        }
        return new BucketInfo(currentBucketId, currentRecordCount);
    }

    @Override
    public LogStorageStatus getStatus() {
        return this;
    }

    @Override
    public LogBucket getNextBucket() {
        synchronized (connection) {
            LOG.trace("Creating a new record block, needed size: {}, batch count: {}", maxBucketSize, maxRecordCount);

            ResultSet resultSet = null;
            LogBucket logBlock = null;
            PreparedStatement selectBucketWithMinIdStatement = null;
            List<LogRecord> logRecords = new LinkedList<>();
            int bucketId = 0;

            try {
                selectBucketWithMinIdStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_SELECT_MIN_BUCKET_ID);
                resultSet = selectBucketWithMinIdStatement.executeQuery();
                if (resultSet.next()) {
                    bucketId = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                LOG.error("Can't retrieve min bucket ID", e);
            } finally {
                try {
                    tryCloseStatement(selectBucketWithMinIdStatement);
                    tryCloseResultSet(resultSet);
                } catch (SQLException e) {
                    LOG.error("Can't close result set", e);
                }
            }

            try {
                long leftBlockSize = maxBucketSize;
                if (bucketId > 0) {
                    selectUnmarkedStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_SELECT_LOG_RECORDS_BY_BUCKET_ID);
                    selectUnmarkedStatement.setInt(1, bucketId);
                    resultSet = selectUnmarkedStatement.executeQuery();
                    while (resultSet.next()) {
                        byte[] recordData = resultSet.getBytes(1);
                        if (recordData != null && recordData.length > 0) {
                            if (leftBlockSize < recordData.length) {
                                break;
                            }
                            logRecords.add(new LogRecord(recordData));
                            leftBlockSize -= recordData.length;
                        } else {
                            LOG.warn("Found unmarked record with no data. Deleting it...");
                        }
                    }

                    if (!logRecords.isEmpty()) {
                        updateBucketState(bucketId);
                        logBlock = new LogBucket(bucketId, logRecords);

                        long logBlockSize = maxBucketSize - leftBlockSize;
                        unmarkedConsumedSize -= logBlockSize;
                        unmarkedRecordCount -= logRecords.size();
                        consumedMemoryStorage.put(logBlock.getBucketId(), logBlockSize);

                        if (currentBucketId == bucketId) {
                            moveToNextBucket();
                        }

                        LOG.info("Created log block: id [{}], size {}. Log block record count: {}, total record count: {}," +
                                        " unmarked record count: {}",
                                logBlock.getBucketId(), logBlockSize, logBlock.getRecords().size(), totalRecordCount, unmarkedRecordCount);
                    } else {
                        LOG.info("No unmarked log records found");
                    }
                }

            } catch (SQLException e) {
                LOG.error("Can't retrieve unmarked records from storage", e);
            } finally {
                try {
                    tryCloseResultSet(resultSet);
                } catch (SQLException e) {
                    LOG.error("Can't close result set", e);
                }
            }
            return logBlock;
        }
    }

    private void updateBucketState(int bucketId) throws SQLException {
        synchronized (connection) {
            LOG.trace("Updating bucket id [{}]", bucketId);

            PreparedStatement updateBucketStateStatement = null;
            try {
                try {
                    updateBucketStateStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_UPDATE_BUCKET_ID);
                } catch (SQLException e) {
                    LOG.error("Can't create bucket id update statement", e);
                    throw new RuntimeException(e);
                }

                try {
                    updateBucketStateStatement.setString(1, PersistentLogStorageConstants.BUCKET_PENDING_STATE);
                    updateBucketStateStatement.setInt(2, bucketId);
                    int affectedRows = updateBucketStateStatement.executeUpdate();
                    if (affectedRows > 0) {
                        LOG.info("Successfully updated id [{}] for log records: {}", bucketId, affectedRows);
                    } else {
                        LOG.warn("No log records were updated");
                    }
                } catch (SQLException e) {
                    LOG.error("Failed to update bucket id [{}]", bucketId, e);
                }
            } finally {
                tryCloseStatement(updateBucketStateStatement);
            }
        }
    }

    @Override
    public void removeBucket(int recordBlockId) {
        synchronized (connection) {
            LOG.trace("Removing record block with id [{}] from storage", recordBlockId);
            if (deleteByBucketIdStatement == null) {
                try {
                    deleteByBucketIdStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_DELETE_BY_BUCKET_ID);
                } catch (SQLException e) {
                    LOG.error("Can't create record block deletion statement", e);
                    throw new RuntimeException(e);
                }
            }

            try {
                deleteByBucketIdStatement.setInt(1, recordBlockId);
                int removedRecordsCount = deleteByBucketIdStatement.executeUpdate();
                if (removedRecordsCount > 0) {
                    totalRecordCount -= removedRecordsCount;
                    LOG.info("Removed {} records from storage. Total log record count: {}", removedRecordsCount, totalRecordCount);
                } else {
                    LOG.warn("No records were removed from storage");
                }
            } catch (SQLException e) {
                LOG.error("Failed to remove record block with id [{}]", recordBlockId, e);
            }
        }
    }

    @Override
    public void rollbackBucket(int bucketId) {
        synchronized (connection) {
            LOG.trace("Notifying upload fail for bucket id: {}", bucketId);
            if (resetBucketIdStatement == null) {
                try {
                    resetBucketIdStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_RESET_BY_BUCKET_ID);
                } catch (SQLException e) {
                    LOG.error("Can't create bucket id reset statement", e);
                    throw new RuntimeException(e);
                }
            }

            try {
                resetBucketIdStatement.setInt(1, bucketId);
                int affectedRows = resetBucketIdStatement.executeUpdate();
                if (affectedRows > 0) {
                    LOG.info("Total {} log records reset for bucket id: [{}]", affectedRows, bucketId);
                    long previouslyConsumedSize = consumedMemoryStorage.remove(bucketId);
                    unmarkedConsumedSize += previouslyConsumedSize;
                    unmarkedRecordCount += affectedRows;
                } else {
                    LOG.info("No log records for bucket with id: [{}]", bucketId);
                }

            } catch (SQLException e) {
                LOG.error("Failed to reset bucket with id [{}]", bucketId, e);
            }
        }
    }

    @Override
    public long getConsumedVolume() {
        return unmarkedConsumedSize;
    }

    @Override
    public long getRecordCount() {
        return unmarkedRecordCount;
    }

    private void moveToNextBucket() {
        this.currentBucketSize = 0;
        this.currentRecordCount = 0;
        this.currentBucketId++;
    }

    private void initTable() throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(PersistentLogStorageConstants.KAA_CREATE_LOG_TABLE);
            statement.executeUpdate(PersistentLogStorageConstants.KAA_CREATE_INFO_TABLE);
            statement.executeUpdate(PersistentLogStorageConstants.KAA_CREATE_BUCKET_ID_INDEX);
        } finally {
            tryCloseStatement(statement);
        }
    }

    private void retrieveBucketId() throws SQLException {
        PreparedStatement selectBucketWithMaxIdStatement = null;
        ResultSet resultSet = null;
        try {
            selectBucketWithMaxIdStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_SELECT_MAX_BUCKET_ID);
            resultSet = selectBucketWithMaxIdStatement.executeQuery();
            if (resultSet.next()) {
                int currentBucketId = resultSet.getInt(1);
                if (currentBucketId == 0) {
                    LOG.trace("Can't retrieve max bucket ID. Seems there is no logs");
                    return;
                }

                this.currentBucketId = ++currentBucketId;
            }
        } catch (SQLException e) {
            LOG.error("Can't create select max bucket ID statement", e);
            throw new RuntimeException(e);
        } finally {
            tryCloseResultSet(resultSet);
            tryCloseStatement(selectBucketWithMaxIdStatement);
        }
    }

    private void retrieveConsumedSizeAndVolume() throws SQLException {
        synchronized (connection) {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(PersistentLogStorageConstants.KAA_HOW_MANY_LOGS_IN_DB);
                if (resultSet.next()) {
                    unmarkedRecordCount = totalRecordCount = resultSet.getLong(1);
                    unmarkedConsumedSize = resultSet.getLong(2);
                    LOG.trace("Retrieved record count: {}, consumed size: {}", totalRecordCount, unmarkedConsumedSize);
                } else {
                    LOG.error("Unable to retrieve consumed size and volume");
                    throw new RuntimeException("Unable to retrieve consumed size and volume");
                }
            } finally {
                tryCloseResultSet(resultSet);
                tryCloseStatement(statement);
            }
        }
    }

    private void truncateIfBucketSizeIncompatible() throws SQLException {
        PreparedStatement selectStatement = null;
        PreparedStatement deleteAllStatement = null;
        ResultSet resultSet = null;
        int lastBucketSize = 0;
        int lastRecordCount = 0;
        try {
            selectStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_SELECT_STORAGE_INFO);
            selectStatement.setString(1, PersistentLogStorageConstants.STORAGE_BUCKET_SIZE);
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                lastBucketSize = resultSet.getInt(1);
            }

            selectStatement.setString(1, PersistentLogStorageConstants.STORAGE_RECORD_COUNT);
            resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                lastRecordCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            LOG.error("Unable to prepare retrieve storage params: bucketSize and recordCount", e);
            throw new RuntimeException(e);
        } finally {
            tryCloseStatement(selectStatement);
            tryCloseStatement(deleteAllStatement);
            tryCloseResultSet(resultSet);
        }

        try {
            if (lastBucketSize != maxBucketSize || lastRecordCount != maxRecordCount) {
                deleteAllStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_DELETE_ALL_DATA);
                int affectedRows = deleteAllStatement.executeUpdate();
                if (affectedRows > 0) {
                    LOG.info("Successfully deleted: {} raws", affectedRows);
                } else {
                    LOG.warn("No log records were deleted");
                }
            }
        } catch (SQLException e) {
            LOG.error("Unable to prepare delete statement", e);
            throw new RuntimeException(e);
        } finally {
            tryCloseStatement(selectStatement);
            tryCloseResultSet(resultSet);
        }

        updateStorageParams();
    }

    private void updateStorageParams() throws SQLException {
        PreparedStatement updateInfoStatement = null;
        try {
            updateInfoStatement = connection.prepareStatement(PersistentLogStorageConstants.KAA_UPDATE_STORAGE_INFO);
            updateInfoStatement.setString(1, PersistentLogStorageConstants.STORAGE_BUCKET_SIZE);
            updateInfoStatement.setLong(2, maxBucketSize);
            int affectedRows = updateInfoStatement.executeUpdate();
            if (affectedRows > 0) {
                LOG.info("Storage bucketSize param was successfully updated: bucketSize {}", maxBucketSize);
            }

            updateInfoStatement.setString(1, PersistentLogStorageConstants.STORAGE_RECORD_COUNT);
            updateInfoStatement.setLong(2, maxRecordCount);
            affectedRows = updateInfoStatement.executeUpdate();
            if (affectedRows > 0) {
                LOG.info("Storage recordCount was successfully updated: recordCount{}", maxRecordCount);
            }

        } catch (SQLException e) {
            LOG.error("Unable to update storage params", e);
            throw new RuntimeException(e);
        } finally {
            tryCloseStatement(updateInfoStatement);
        }
    }

    public void close() {
        try {
            tryCloseStatement(insertStatement);
            tryCloseStatement(deleteByBucketIdStatement);
            tryCloseStatement(resetBucketIdStatement);
            tryCloseStatement(selectUnmarkedStatement);

            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Can't close SQLite db connection", e);
        }
    }

    private void tryCloseResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    private void tryCloseStatement(Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }

    private void resetBucketIDs() throws SQLException {
        synchronized (connection) {
            LOG.debug("Resetting bucket ids on application start");
            Statement statement = null;
            try {
                statement = connection.createStatement();
                int updatedRows = statement.executeUpdate(PersistentLogStorageConstants.KAA_RESET_BUCKET_STATE_ON_START);
                LOG.trace("Number of rows affected: {}", updatedRows);
            } catch (SQLException e) {
                LOG.error("Can't reset bucket IDs", e);
                throw new RuntimeException(e);
            } finally {
                tryCloseStatement(statement);
            }
        }
    }
}
