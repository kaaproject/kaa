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

public interface PersistentLogStorageConstants {
    int DB_VERSION = 1;
    String DEFAULT_DB_NAME = "kaa_logs";
    String TABLE_NAME = "kaa_logs";
    String RECORD_ID_COLUMN = "record_id";
    String BUCKET_ID_COLUMN = "bucket_id";
    String LOG_DATA_COLUMN = "log_data";
    String BUCKET_ID_INDEX_NAME = "IX_BUCKET_ID";

    String SUBSTITUTE_SYMBOL = "?";

    String KAA_CREATE_LOG_TABLE =
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("  +
                    RECORD_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BUCKET_ID_COLUMN + " INTEGER, " +
                    LOG_DATA_COLUMN + " BLOB);";

    String KAA_CREATE_BUCKET_ID_INDEX =
                    "CREATE INDEX IF NOT EXISTS " + BUCKET_ID_INDEX_NAME + " " +
                    " ON " + TABLE_NAME +  " (" + BUCKET_ID_COLUMN + ");";

    String KAA_HOW_MANY_LOGS_IN_DB =
                    "SELECT COUNT(*), SUM(LENGTH(" +
                    LOG_DATA_COLUMN + ")) FROM " + TABLE_NAME + ";";

    String KAA_RESET_BUCKET_ID_ON_START =
                    "UPDATE " + TABLE_NAME + " " +
                    "SET " + BUCKET_ID_COLUMN + " = NULL " +
                    "WHERE " + BUCKET_ID_COLUMN + " IS NOT NULL;";

    String KAA_INSERT_NEW_RECORD =
                    "INSERT INTO " + TABLE_NAME + " (" + LOG_DATA_COLUMN + ") " +
                    "VALUES (?);";

    String KAA_SELECT_UNMARKED_RECORDS =
                    "SELECT " + RECORD_ID_COLUMN + ", " + LOG_DATA_COLUMN + " " +
                    "FROM " + TABLE_NAME + " " +
                    "WHERE " + BUCKET_ID_COLUMN + " IS NULL " +
                    "ORDER BY " + RECORD_ID_COLUMN + " ASC " +
                    "LIMIT ?;";

    String KAA_DELETE_BY_RECORD_ID =
                    "DELETE FROM " + TABLE_NAME + " " +
                    "WHERE " + RECORD_ID_COLUMN + " = ?;";

    String KAA_UPDATE_BUCKET_ID =
                    "UPDATE " + TABLE_NAME + " "  +
                    "SET " + BUCKET_ID_COLUMN + " = ? " +
                    "WHERE " + RECORD_ID_COLUMN + " IN (?);";

    String KAA_DELETE_BY_BUCKET_ID =
                    "DELETE FROM " + TABLE_NAME + " " +
                    "WHERE " + BUCKET_ID_COLUMN + " = ?;";

    String KAA_RESET_BY_BUCKET_ID =
                    "UPDATE " + TABLE_NAME + " " +
                    "SET " + BUCKET_ID_COLUMN + " = NULL " +
                    "WHERE " + BUCKET_ID_COLUMN + " = ?;";
}
