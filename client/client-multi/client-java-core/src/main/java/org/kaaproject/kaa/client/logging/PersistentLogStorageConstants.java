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

public interface PersistentLogStorageConstants {
  int DB_VERSION = 1;
  String DEFAULT_DB_NAME = "kaa_logs";
  String LOG_TABLE_NAME = "kaa_logs";
  String STORAGE_INFO_TABLE_NAME = "kaa_storage_info";
  String RECORD_ID_COLUMN = "record_id";
  String BUCKET_ID_COLUMN = "bucket_id";
  String BUCKET_STATE_COLUMN = "bucket_state";
  String LOG_DATA_COLUMN = "log_data";
  String STORAGE_INFO_KEY_COLUMN = "storage_info_key";
  String STORAGE_INFO_VALUE_COLUMN = "storage_info_value";
  String BUCKET_ID_INDEX_NAME = "IX_BUCKET_ID";

  String BUCKET_PENDING_STATE = "pending";
  String STORAGE_BUCKET_SIZE = "bucket_size";
  String STORAGE_RECORD_COUNT = "record_count";

  String KAA_CREATE_LOG_TABLE =
      "CREATE TABLE IF NOT EXISTS " + LOG_TABLE_NAME + " ("
              + RECORD_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
              + BUCKET_ID_COLUMN + " INTEGER NOT NULL, "
              + BUCKET_STATE_COLUMN + " TEXT, "
              + LOG_DATA_COLUMN + " BLOB);";

  String KAA_CREATE_INFO_TABLE =
      "CREATE TABLE IF NOT EXISTS " + STORAGE_INFO_TABLE_NAME + " ("
              + STORAGE_INFO_KEY_COLUMN + " TEXT UNIQUE, "
              + STORAGE_INFO_VALUE_COLUMN + " INTEGER);";

  String KAA_CREATE_BUCKET_ID_INDEX =
      "CREATE INDEX IF NOT EXISTS " + BUCKET_ID_INDEX_NAME + " "
              + "ON " + LOG_TABLE_NAME + " (" + BUCKET_ID_COLUMN + ");";

  String KAA_HOW_MANY_LOGS_IN_DB =
      "SELECT COUNT(*), SUM(LENGTH("
              + LOG_DATA_COLUMN + ")) FROM " + LOG_TABLE_NAME + ";";

  String KAA_INSERT_NEW_RECORD =
      "INSERT INTO " + LOG_TABLE_NAME
              + "(" + BUCKET_ID_COLUMN + "," + LOG_DATA_COLUMN + ") "
              + "VALUES (?, ?);";

  String KAA_DELETE_BY_BUCKET_ID =
      "DELETE FROM " + LOG_TABLE_NAME + " "
              + "WHERE " + BUCKET_ID_COLUMN + " = ?;";

  String KAA_RESET_BY_BUCKET_ID =
      "UPDATE " + LOG_TABLE_NAME + " "
              + "SET " + BUCKET_STATE_COLUMN + " = NULL "
              + "WHERE " + BUCKET_ID_COLUMN + " = ?;";

  String KAA_DELETE_ALL_DATA =
      "DELETE FROM " + LOG_TABLE_NAME + ";";

  String KAA_SELECT_MIN_BUCKET_ID =
      "SELECT MIN (" + BUCKET_ID_COLUMN + ") "
              + "FROM " + LOG_TABLE_NAME + " "
              + "WHERE " + BUCKET_STATE_COLUMN + " IS NULL;";

  String KAA_SELECT_MAX_BUCKET_ID =
      "SELECT MAX (" + BUCKET_ID_COLUMN + ") "
              + "FROM " + LOG_TABLE_NAME + ";";

  String KAA_SELECT_LOG_RECORDS_BY_BUCKET_ID =
      "SELECT " + LOG_DATA_COLUMN + "," + BUCKET_STATE_COLUMN + " "
              + "FROM " + LOG_TABLE_NAME + " "
              + "WHERE " + BUCKET_ID_COLUMN + "=?;";

  String KAA_UPDATE_BUCKET_ID =
      "UPDATE " + LOG_TABLE_NAME + " "
              + "SET " + BUCKET_STATE_COLUMN + " = ? "
              + "WHERE " + BUCKET_ID_COLUMN + " = ?;";

  String KAA_RESET_BUCKET_STATE_ON_START =
      "UPDATE " + LOG_TABLE_NAME + " "
              + "SET " + BUCKET_STATE_COLUMN + " = NULL "
              + "WHERE " + BUCKET_STATE_COLUMN + " IS NOT NULL;";

  String KAA_UPDATE_STORAGE_INFO =
      "INSERT OR REPLACE INTO  " + STORAGE_INFO_TABLE_NAME + " "
              + "(" + STORAGE_INFO_KEY_COLUMN + "," + STORAGE_INFO_VALUE_COLUMN + ") "
              + "VALUES (?, ?);";

  String KAA_SELECT_STORAGE_INFO =
      "SELECT " + STORAGE_INFO_VALUE_COLUMN + " "
              + "FROM " + STORAGE_INFO_TABLE_NAME + " "
              + "WHERE " + STORAGE_INFO_KEY_COLUMN + " = ?;";

}
