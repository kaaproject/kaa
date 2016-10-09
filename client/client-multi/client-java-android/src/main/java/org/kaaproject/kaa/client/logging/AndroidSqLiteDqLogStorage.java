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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AndroidSqLiteDqLogStorage implements LogStorage, LogStorageStatus {

  private static final String TAG = "AndroidSqLiteDqLogStorage";

  private static final String CHANGES_QUERY_RESULT = "affected_row_count";
  private static final String GET_CHANGES_QUERY = "SELECT changes() AS " + CHANGES_QUERY_RESULT;

  private final SQLiteOpenHelper dbHelper;
  private final SQLiteDatabase database;

  private long totalRecordCount;
  private long unmarkedRecordCount;
  private long unmarkedConsumedSize;
  private int currentBucketId = 1;

  private long currentBucketSize;
  private int currentRecordCount;

  private long maxBucketSize;
  private int maxRecordCount;

  private Map<Integer, Long> consumedMemoryStorage = new HashMap<>();

  private SQLiteStatement insertStatement;
  private SQLiteStatement deleteByBucketIdStatement;
  private SQLiteStatement updateBucketStateStatement;
  private SQLiteStatement resetBucketIdStatement;

  /**
   * Instantiates the AndroidSqLiteDqLogStorage.
   */
  public AndroidSqLiteDqLogStorage(Context context, long maxBucketSize, int maxRecordCount) {
    this(context, PersistentLogStorageConstants.DEFAULT_DB_NAME, maxBucketSize, maxRecordCount);
  }

  /**
   * Instantiates the AndroidSqLiteDqLogStorage.
   */
  public AndroidSqLiteDqLogStorage(Context context, String dbName, long bucketSize,
                                   int recordCount) {
    Log.i(TAG, "Connecting to db with name: " + dbName);
    dbHelper = new DataCollectionDbHelper(context, dbName);
    database = dbHelper.getWritableDatabase();
    this.maxRecordCount = recordCount;
    this.maxBucketSize = bucketSize;
    truncateIfBucketSizeIncompatible();
    retrieveConsumedSizeAndVolume();
    if (totalRecordCount > 0) {
      retrieveBucketId();
      resetBucketsState();
    }
  }

  @Override
  public BucketInfo addLogRecord(LogRecord record) {
    synchronized (database) {
      Log.d(TAG, "Adding a new log record...");
      if (insertStatement == null) {
        try {
          insertStatement = database.compileStatement(
                  PersistentLogStorageConstants.KAA_INSERT_NEW_RECORD);
        } catch (SQLiteException ex) {
          Log.e(TAG, "Can't create row insert statement", ex);
          throw new RuntimeException(ex);
        }
      }
      long leftConsumedSize = maxBucketSize - currentBucketSize;
      long leftRecordCount = maxRecordCount - currentRecordCount;

      if (leftConsumedSize < record.getSize() || leftRecordCount == 0) {
        moveToNextBucket();
      }

      try {
        insertStatement.bindLong(1, currentBucketId);
        insertStatement.bindBlob(2, record.getData());
        long insertedId = insertStatement.executeInsert();
        if (insertedId >= 0) {
          currentBucketSize += record.getSize();
          currentRecordCount++;

          unmarkedConsumedSize += record.getSize();
          totalRecordCount++;
          unmarkedRecordCount++;
          Log.i(TAG, "Added a new log record, total record count: " + totalRecordCount
                     + ", data: " + Arrays.toString(record.getData())
                     + "unmarked record count: " + unmarkedRecordCount);
        } else {
          Log.w(TAG, "No log record was added");
        }
      } catch (SQLiteException ex) {
        Log.e(TAG, "Can't add a new record", ex);
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
    synchronized (database) {
      Log.d(TAG, "Creating a new record block");
      LogBucket logBlock = null;
      Cursor cursor = null;
      List<LogRecord> logRecords = new LinkedList<>();
      int bucketId = 0;
      try {
        cursor = database.rawQuery(PersistentLogStorageConstants.KAA_SELECT_MIN_BUCKET_ID, null);
        if (cursor.moveToFirst()) {
          bucketId = cursor.getInt(0);
        }
      } catch (SQLiteException ex) {
        Log.e(TAG, "Can't retrieve min bucket ID", ex);
      } finally {
        try {
          tryCloseCursor(cursor);
        } catch (SQLiteException ex) {
          Log.e(TAG, "Unable to close cursor", ex);
        }
      }

      try {
        long leftBucketSize = maxBucketSize;
        if (bucketId > 0) {
          cursor = database.rawQuery(
                  PersistentLogStorageConstants.KAA_SELECT_LOG_RECORDS_BY_BUCKET_ID,
              new String[]{String.valueOf(bucketId)});
          while (cursor.moveToNext()) {
            byte[] recordData = cursor.getBlob(0);

            logRecords.add(new LogRecord(recordData));
            leftBucketSize -= recordData.length;
          }

          if (!logRecords.isEmpty()) {
            updateBucketState(bucketId);
            logBlock = new LogBucket(bucketId, logRecords);

            long logBlockSize = maxBucketSize - leftBucketSize;
            unmarkedConsumedSize -= logBlockSize;
            unmarkedRecordCount -= logRecords.size();
            consumedMemoryStorage.put(logBlock.getBucketId(), logBlockSize);

            if (currentBucketId == bucketId) {
              moveToNextBucket();
            }
            Log.i(TAG, "Created log block: id [" + logBlock.getBucketId() + "], size: "
                       + logBlockSize + ". Log block record count: "
                       + logBlock.getRecords().size() + ", total record count: " + totalRecordCount
                       + ", unmarked record count: " + unmarkedRecordCount);
          } else {
            Log.i(TAG, "No unmarked log records found");
          }
        }
      } catch (SQLiteException ex) {
        Log.e(TAG, "Can't retrieve unmarked records from storage", ex);
      } finally {
        try {
          tryCloseCursor(cursor);
        } catch (SQLiteException ex) {
          Log.e(TAG, "Unable to close cursor", ex);
        }
      }
      return logBlock;
    }
  }

  private void updateBucketState(int bucketId) {
    synchronized (database) {
      Log.v(TAG, "Updating bucket id [" + bucketId + "]");

      try {
        if (updateBucketStateStatement == null) {
          updateBucketStateStatement = database.compileStatement(
                  PersistentLogStorageConstants.KAA_UPDATE_BUCKET_ID);
        }
        updateBucketStateStatement.bindString(
                1, PersistentLogStorageConstants.BUCKET_STATE_COLUMN);
        updateBucketStateStatement.bindLong(2, bucketId);
        updateBucketStateStatement.execute();

        long affectedRows = getAffectedRowCount();
        if (affectedRows > 0) {
          Log.i(TAG, "Successfully updated state for bucket ID [" + bucketId
                     + "] for log records: " + affectedRows);
        } else {
          Log.w(TAG, "No log records were updated");
        }

      } catch (SQLiteException ex) {
        Log.e(TAG, "Failed to update state for bucket [" + bucketId + "]", ex);
      }
    }
  }

  @Override
  public void removeBucket(int recordBlockId) {
    synchronized (database) {
      Log.d(TAG, "Removing record block with id [" + recordBlockId + "] from storage");
      if (deleteByBucketIdStatement == null) {
        try {
          deleteByBucketIdStatement = database.compileStatement(
                  PersistentLogStorageConstants.KAA_DELETE_BY_BUCKET_ID);
        } catch (SQLiteException ex) {
          Log.e(TAG, "Can't create record block deletion statement", ex);
          throw new RuntimeException(ex);
        }
      }

      try {
        deleteByBucketIdStatement.bindLong(1, recordBlockId);
        deleteByBucketIdStatement.execute();

        long removedRecordsCount = getAffectedRowCount();

        if (removedRecordsCount > 0) {
          totalRecordCount -= removedRecordsCount;
          Log.i(TAG, "Removed " + removedRecordsCount
              + " records from storage. Total log record count: " + totalRecordCount);
        } else {
          Log.i(TAG, "No records were removed from storage");
        }
      } catch (SQLiteException ex) {
        Log.e(TAG, "Failed to remove record block with id [" + recordBlockId + "]", ex);
      }
    }
  }

  @Override
  public void rollbackBucket(int bucketId) {
    synchronized (database) {
      Log.d(TAG, "Notifying upload fail for bucket id: " + bucketId);
      if (resetBucketIdStatement == null) {
        try {
          resetBucketIdStatement = database.compileStatement(
                  PersistentLogStorageConstants.KAA_RESET_BY_BUCKET_ID);
        } catch (SQLiteException ex) {
          Log.e(TAG, "Can't create bucket id reset statement", ex);
          throw new RuntimeException(ex);
        }
      }

      try {
        resetBucketIdStatement.bindLong(1, bucketId);
        resetBucketIdStatement.execute();

        long affectedRows = getAffectedRowCount();
        if (affectedRows > 0) {
          Log.i(TAG, "Total " + affectedRows + " log records reset for bucket id: ["
                     + bucketId + "]");
          long previouslyConsumedSize = consumedMemoryStorage.remove(bucketId);
          unmarkedConsumedSize += previouslyConsumedSize;
          unmarkedRecordCount += affectedRows;
        } else {
          Log.i(TAG, "No log records for bucket with id: [" + bucketId + "]");
        }

      } catch (SQLiteException ex) {
        Log.e(TAG, "Failed to reset bucket with id [" + bucketId + "]", ex);
      }
    }
  }

  @Override
  public void close() {
    tryCloseStatement(insertStatement);
    tryCloseStatement(deleteByBucketIdStatement);
    tryCloseStatement(resetBucketIdStatement);
    tryCloseStatement(updateBucketStateStatement);

    if (database != null) {
      database.close();
    }

    if (dbHelper != null) {
      dbHelper.close();
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

  private void retrieveBucketId() {
    Cursor cursor = null;
    try {
      cursor = database.rawQuery(PersistentLogStorageConstants.KAA_SELECT_MAX_BUCKET_ID, null);
      if (cursor.moveToFirst()) {
        int currentBucketId = cursor.getInt(0);
        if (currentBucketId == 0) {
          Log.d(TAG, "Can't retrieve max bucket ID. Seems there is no logs");
          return;
        }

        this.currentBucketId = ++currentBucketId;
      }
    } catch (SQLiteException ex) {
      Log.e(TAG, "Can't create select max bucket ID statement", ex);
      throw new RuntimeException("Can't create select max bucket ID statement");
    } finally {
      try {
        tryCloseCursor(cursor);
      } catch (SQLiteException ex) {
        Log.e(TAG, "Unable to close cursor", ex);
      }
    }
  }

  private void retrieveConsumedSizeAndVolume() {
    synchronized (database) {
      Cursor cursor = null;
      try {
        cursor = database.rawQuery(PersistentLogStorageConstants.KAA_HOW_MANY_LOGS_IN_DB, null);
        if (cursor.moveToFirst()) {
          unmarkedRecordCount = totalRecordCount = cursor.getLong(0);
          unmarkedConsumedSize = cursor.getLong(1);
          Log.i(TAG, "Retrieved record count: " + totalRecordCount + ","
                     + " consumed size: " + unmarkedConsumedSize);
        } else {
          Log.e(TAG, "Unable to retrieve consumed size and volume");
          throw new RuntimeException("Unable to retrieve consumed size and volume");
        }
      } finally {
        tryCloseCursor(cursor);
      }
    }
  }

  private void truncateIfBucketSizeIncompatible() {
    Cursor cursor = null;
    int lastSavedBucketSize = 0;
    int lastSavedRecordCount = 0;
    try {
      cursor = database.rawQuery(PersistentLogStorageConstants.KAA_SELECT_STORAGE_INFO,
          new String[]{PersistentLogStorageConstants.STORAGE_BUCKET_SIZE});
      if (cursor.moveToFirst()) {
        lastSavedBucketSize = cursor.getInt(0);
      }
    } catch (SQLiteException ex) {
      Log.e(TAG, "Cannot retrieve storage param: bucketSize", ex);
      throw new RuntimeException("Cannot retrieve storage param: bucketSize");
    } finally {
      tryCloseCursor(cursor);
    }

    try {
      cursor = database.rawQuery(PersistentLogStorageConstants.KAA_SELECT_STORAGE_INFO,
          new String[]{PersistentLogStorageConstants.STORAGE_RECORD_COUNT});
      if (cursor.moveToFirst()) {
        lastSavedRecordCount = cursor.getInt(0);
      }
    } catch (SQLiteException ex) {
      Log.e(TAG, "Cannot retrieve storage param: recordCount", ex);
      throw new RuntimeException("Cannot retrieve storage param: recordCount");
    } finally {
      tryCloseCursor(cursor);
    }

    try {
      if (lastSavedBucketSize != maxBucketSize || lastSavedRecordCount != maxRecordCount) {
        database.execSQL(PersistentLogStorageConstants.KAA_DELETE_ALL_DATA);
      }
    } catch (SQLiteException ex) {
      Log.e(TAG, "Can't prepare delete statement", ex);
      throw new RuntimeException("Can't prepare delete statement");
    } finally {
      tryCloseCursor(cursor);
    }

    updateStorageParams();
  }

  private void updateStorageParams() {
    SQLiteStatement updateInfoStatement = null;
    try {
      updateInfoStatement = database.compileStatement(
              PersistentLogStorageConstants.KAA_UPDATE_STORAGE_INFO);
      updateInfoStatement.bindString(1, PersistentLogStorageConstants.STORAGE_BUCKET_SIZE);
      updateInfoStatement.bindLong(2, maxBucketSize);
      updateInfoStatement.execute();

      updateInfoStatement.bindString(1, PersistentLogStorageConstants.STORAGE_RECORD_COUNT);
      updateInfoStatement.bindLong(2, maxRecordCount);
      updateInfoStatement.execute();
    } catch (SQLiteException ex) {
      Log.e(TAG, "Can't prepare update storage info statement", ex);
      throw new RuntimeException("Can't prepare update storage info statement");
    } finally {
      tryCloseStatement(updateInfoStatement);
    }
  }


  private void resetBucketsState() {
    synchronized (database) {
      Log.d(TAG, "Resetting bucket ids on application start");
      database.execSQL(PersistentLogStorageConstants.KAA_RESET_BUCKET_STATE_ON_START);
      long updatedRows = getAffectedRowCount();
      Log.v(TAG, "Number of rows affected: " + updatedRows);
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
