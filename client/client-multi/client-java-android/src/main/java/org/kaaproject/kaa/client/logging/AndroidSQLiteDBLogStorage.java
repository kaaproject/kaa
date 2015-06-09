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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import org.sqlite.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AndroidSQLiteDBLogStorage implements LogStorage, LogStorageStatus {

    private static final String TAG = "AndroidSQLiteDBLogStorage";

    private static final String CHANGES_QUERY_RESULT = "affected_row_count";
    private static final String GET_CHANGES_QUERY = "SELECT changes() AS " + CHANGES_QUERY_RESULT;

    private final SQLiteOpenHelper dbHelper;
    private final SQLiteDatabase database;

    private long recordCount;
    private long consumedSize;
    private int currentBucketId;

    private Map<Integer, Long> consumedMemoryStorage = new HashMap<>();

    private SQLiteStatement insertStatement;
    private SQLiteStatement deleteByRecordIdStatement;
    private SQLiteStatement deleteByBucketIdStatement;
    private SQLiteStatement resetBucketIdStatement;

    public AndroidSQLiteDBLogStorage(Context context) {
        this(context, PersistentLogStorageStorageInfo.DEFAULT_DB_NAME);
    }

    public AndroidSQLiteDBLogStorage(Context context, String dbName) {
        Log.i(TAG, "Connecting to db with name: " + dbName);
        dbHelper = new DataCollectionDBHelper(context, dbName);
        database = dbHelper.getWritableDatabase();
        retrieveConsumedSizeAndVolume();
        if (recordCount > 0) {
            resetBucketIDs();
        }
    }

    @Override
    public void addLogRecord(LogRecord record) {
        synchronized (database) {
            Log.v(TAG, "Adding a new log record...");
            if (insertStatement == null) {
                try {
                    insertStatement = database.compileStatement(PersistentLogStorageStorageInfo.KAA_INSERT_NEW_RECORD);
                } catch (SQLiteException e) {
                    Log.e(TAG, "Can't create row insert statement");
                    throw new RuntimeException(e);
                }
            }

            try {
                insertStatement.bindBlob(0, record.getData());
                long affectedRows = insertStatement.executeInsert();
                if (affectedRows == 1) {
                    consumedSize += record.getSize();
                    recordCount++;
                    Log.v(TAG, "Added a new log record, records count: " + recordCount + ", data: " + Arrays.toString(record.getData()));
                } else {
                    Log.w(TAG, "No log record was added");
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Can't add a new record");
            }
        }
    }

    @Override
    public LogStorageStatus getStatus() {
        return this;
    }

    @Override
    public LogBlock getRecordBlock(long blockSize) {
        synchronized (database) {
            Log.v(TAG, "Creating a new record block, needed size: " + blockSize);
            LogBlock logBlock = null;
            Cursor cursor = null;
            List<String> unmarkedRecordIds = new LinkedList<>();
            List<LogRecord> logRecords = new LinkedList<>();
            long leftBlockSize = blockSize;
            try {
                cursor = database.rawQuery(PersistentLogStorageStorageInfo.KAA_SELECT_UNMARKED_RECORDS, null);
                while (cursor.moveToNext()) {
                    int recordId = cursor.getInt(0);
                    byte[] recordData = cursor.getBlob(1);

                    if (recordData != null && recordData.length > 0) {
                        if (leftBlockSize < recordData.length) {
                            break;
                        }
                        logRecords.add(new LogRecord(recordData));
                        unmarkedRecordIds.add(String.valueOf(recordId));
                        leftBlockSize -= recordData.length;
                    } else {
                        Log.w(TAG, "Found unmarked record with no data. Deleting it...");
                        removeRecordById(recordId);
                    }
                }

                if (!logRecords.isEmpty()) {
                    updateBucketIdForRecords(currentBucketId, unmarkedRecordIds);
                    logBlock = new LogBlock(currentBucketId++, logRecords);

                    long logBlockSize = blockSize - leftBlockSize;
                    consumedSize -= logBlockSize;
                    consumedMemoryStorage.put(logBlock.getBlockId(), logBlockSize);

                    Log.i(TAG, "Created log block: id [" + logBlock.getBlockId() + "], size: " + logBlockSize + ". Log block record count:" +
                            logBlock.getRecords().size() + ", Total record count: " + recordCount);
                } else {
                    Log.i(TAG, "No unmarked log records found");
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Can't retrieve unmarked records from storage", e);
            } finally {
                try {
                    tryCloseCursor(cursor);
                } catch (SQLiteException e) {
                    Log.e(TAG, "Unable to close cursor", e);
                }
            }
            return logBlock;
        }
    }

    private void removeRecordById(int recordId) {
        synchronized (database) {
            Log.v(TAG, "Removing log record with id [" + recordId + "]");
            if (deleteByRecordIdStatement == null) {
                try {
                    deleteByRecordIdStatement = database.compileStatement(PersistentLogStorageStorageInfo.KAA_DELETE_BY_RECORD_ID);
                } catch (SQLiteException e) {
                    Log.e(TAG, "Can't create log remove statement", e);
                    throw new RuntimeException(e);
                }
            }

            try {
                deleteByRecordIdStatement.bindLong(0, recordId);
                deleteByRecordIdStatement.execute();

                long affectedRows = getAffectedRowCount();

                if (affectedRows > 0) {
                    recordCount--;
                    Log.i(TAG, "Removed log record with id [" + recordId + "]");
                } else {
                    Log.w(TAG, "No log record was removed");
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to remove a log record by recordId [" + recordId + "]", e);
            }
        }
    }

    private void updateBucketIdForRecords(int bucketId, List<String> recordIds) {
        synchronized (database) {
            Log.v(TAG, "Updating bucket id [" + bucketId + "] for records with ids: " + recordIds);

            SQLiteStatement setBucketIdStatement = null;
            try {
                setBucketIdStatement =
                        database.compileStatement(PersistentLogStorageStorageInfo.KAA_UPDATE_BUCKET_ID);

                setBucketIdStatement.bindLong(0, bucketId);
                setBucketIdStatement.bindString(1, StringUtils.join(recordIds, ","));
                setBucketIdStatement.execute();
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to update bucket id [" + bucketId + "] for records with ids: " + recordIds, e);
            } finally {
                tryCloseStatement(resetBucketIdStatement);
            }
        }
    }

    @Override
    public void removeRecordBlock(int recordBlockId) {
        synchronized (database) {
            Log.v(TAG, "Removing record block with id [" + recordBlockId + "] from storage");
            if (deleteByBucketIdStatement == null) {
                try {
                    deleteByBucketIdStatement = database.compileStatement(PersistentLogStorageStorageInfo.KAA_DELETE_BY_BUCKET_ID);
                } catch (SQLiteException e) {
                    Log.e(TAG, "Can't create record block deletion statement", e);
                    throw new RuntimeException(e);
                }
            }

            try {
                deleteByBucketIdStatement.bindLong(0, recordBlockId);
                deleteByBucketIdStatement.execute();

                database.rawQuery("GET_CHANGES_QUERY", null);
                long removedRecordsCount = getAffectedRowCount();

                if (removedRecordsCount > 0) {
                    recordCount -= removedRecordsCount;
                    Log.i(TAG, "Removed " + removedRecordsCount + "records from storage. Total log record count: " + recordCount);
                } else {
                    Log.i(TAG, "No records were removed from storage");
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to remove record block with id [" + recordBlockId + "]", e);
            }
        }
    }

    @Override
    public void notifyUploadFailed(int bucketId) {
        synchronized (database) {
            Log.v(TAG, "Notifying upload fail for bucket id: " + bucketId);
            if (resetBucketIdStatement == null) {
                try {
                    resetBucketIdStatement = database.compileStatement(PersistentLogStorageStorageInfo.KAA_RESET_BY_BUCKET_ID);
                } catch (SQLiteException e) {
                    Log.e(TAG, "Can't create bucket id reset statement", e);
                    throw new RuntimeException(e);
                }
            }

            try {
                resetBucketIdStatement.bindLong(0, bucketId);
                resetBucketIdStatement.execute();

                long previouslyConsumedSize = consumedMemoryStorage.get(bucketId);
                consumedMemoryStorage.remove(bucketId);
                consumedSize += previouslyConsumedSize;
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to reset bucket with id [" + bucketId + "]", e);
            }
        }
    }

    @Override
    public void close() {
        tryCloseStatement(insertStatement);
        tryCloseStatement(deleteByBucketIdStatement);
        tryCloseStatement(deleteByRecordIdStatement);
        tryCloseStatement(resetBucketIdStatement);

        if (database != null) {
            database.close();
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public long getConsumedVolume() {
        return consumedSize;
    }

    @Override
    public long getRecordCount() {
        return recordCount;
    }

    private void retrieveConsumedSizeAndVolume() {
        synchronized (database) {
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(PersistentLogStorageStorageInfo.KAA_HOW_MANY_LOGS_IN_DB, null);
                cursor.moveToNext();
                recordCount = cursor.getLong(0);
                consumedSize = cursor.getLong(1);
                Log.v(TAG, "Retrieved record count: " + recordCount + ", consumed size: " + consumedSize);
            } finally {
                tryCloseCursor(cursor);
            }
        }
    }

    private void resetBucketIDs() {
        synchronized (database) {
            Log.d(TAG, "Resetting bucket ids on application start");
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(PersistentLogStorageStorageInfo.KAA_RESET_BUCKET_ID_ON_START, null);
                long updatedRows = cursor.getLong(0);
                Log.v(TAG, "Number of rows affected: " + updatedRows);
            } finally {
                tryCloseCursor(cursor);
            }
        }
    }

    private long getAffectedRowCount() {
        synchronized (database) {
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(GET_CHANGES_QUERY, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndex(CHANGES_QUERY_RESULT));
                } else {
                    return 0;
                }
            } finally {
                tryCloseCursor(cursor);
            }
        }
    }

    private void tryCloseCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    private void tryCloseStatement(SQLiteStatement statement) {
        if (statement != null) {
            statement.close();
        }
    }
}
