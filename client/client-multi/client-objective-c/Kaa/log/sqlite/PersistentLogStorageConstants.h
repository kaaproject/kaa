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

#ifndef PersistentLogStorageConstants_h
#define PersistentLogStorageConstants_h

#define DEFAULT_DB_NAME             @"kaa_logs.db"

#define LOG_TABLE_NAME              @"kaa_logs"
#define STORAGE_INFO_TABLE_NAME     @"kaa_storage_info"

#define RECORD_ID_COLUMN            @"record_id"
#define BUCKET_ID_COLUMN            @"bucket_id"
#define BUCKET_STATE_COLUMN         @"bucket_state"
#define LOG_DATA_COLUMN             @"log_data"
#define STORAGE_INFO_KEY_COLUMN     @"storage_info_key"
#define STORAGE_INFO_VALUE_COLUMN   @"storage_info_value"
#define BUCKET_ID_INDEX_NAME        @"IX_BUCKET_ID"

#define BUCKET_PENDING_STATE        @"pending"
#define STORAGE_BUCKET_SIZE         @"bucket_size"
#define STORAGE_RECORD_COUNT        @"record_count"

#define KAA_CREATE_LOG_TABLE_IF_NOT_EXISTS \
[NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (\
%@ INTEGER PRIMARY KEY AUTOINCREMENT, \
%@ INTEGER NOT NULL, \
%@ TEXT, \
%@ BLOB)", LOG_TABLE_NAME, RECORD_ID_COLUMN, BUCKET_ID_COLUMN, BUCKET_STATE_COLUMN, LOG_DATA_COLUMN]

#define KAA_CREATE_INFO_TABLE_IF_NOT_EXISTS \
[NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (\
%@ TEXT UNIQUE, \
%@ INTEGER)", STORAGE_INFO_TABLE_NAME, STORAGE_INFO_KEY_COLUMN, STORAGE_INFO_VALUE_COLUMN]

#define KAA_CREATE_BUCKET_ID_INDEX_IF_NOT_EXISTS \
[NSString stringWithFormat:@"CREATE INDEX IF NOT EXISTS %@ ON %@ (%@)", BUCKET_ID_INDEX_NAME, LOG_TABLE_NAME, BUCKET_ID_COLUMN]

#define KAA_GET_LOG_COUNT_IN_DB \
[NSString stringWithFormat:@"SELECT COUNT(*), SUM(LENGTH(%@)) FROM %@", LOG_DATA_COLUMN, LOG_TABLE_NAME]

#define KAA_INSERT_NEW_RECORD \
[NSString stringWithFormat:@"INSERT INTO %@ (%@, %@) VALUES (?, ?)", LOG_TABLE_NAME, BUCKET_ID_COLUMN, LOG_DATA_COLUMN]

#define KAA_DELETE_BY_BUCKET_ID [NSString stringWithFormat:@"DELETE FROM %@ WHERE %@ = ?", LOG_TABLE_NAME, BUCKET_ID_COLUMN]

#define KAA_RESET_BY_BUCKET_ID \
[NSString stringWithFormat:@"UPDATE %@ SET %@ = NULL WHERE %@ = ?", LOG_TABLE_NAME, BUCKET_STATE_COLUMN, BUCKET_ID_COLUMN]

#define KAA_DELETE_ALL_DATA [NSString stringWithFormat:@"DELETE FROM %@", LOG_TABLE_NAME]

#define KAA_SELECT_MIN_BUCKET_ID \
[NSString stringWithFormat:@"SELECT MIN (%@) FROM %@ WHERE %@ IS NULL", BUCKET_ID_COLUMN, LOG_TABLE_NAME, BUCKET_STATE_COLUMN]

#define KAA_SELECT_MAX_BUCKET_ID \
[NSString stringWithFormat:@"SELECT MAX (%@) FROM %@", BUCKET_ID_COLUMN, LOG_TABLE_NAME]

#define KAA_SELECT_LOG_RECORDS_BY_BUCKET_ID \
[NSString stringWithFormat:@"SELECT %@, %@ FROM %@ WHERE %@ = ?", LOG_DATA_COLUMN, BUCKET_STATE_COLUMN, LOG_TABLE_NAME, BUCKET_ID_COLUMN]

#define KAA_UPDATE_BUCKET_ID \
[NSString stringWithFormat:@"UPDATE %@ SET %@ = ? WHERE %@ = ?", LOG_TABLE_NAME, BUCKET_STATE_COLUMN, BUCKET_ID_COLUMN]

#define KAA_RESET_BUCKET_STATE_ON_START \
[NSString stringWithFormat:@"UPDATE %@ SET %@ = NULL WHERE %@ IS NOT NULL", LOG_TABLE_NAME, BUCKET_STATE_COLUMN, BUCKET_STATE_COLUMN]

#define KAA_UPDATE_STORAGE_INFO \
[NSString stringWithFormat:@"INSERT OR REPLACE INTO %@ (%@, %@) VALUES (?, ?)", \
STORAGE_INFO_TABLE_NAME, STORAGE_INFO_KEY_COLUMN, STORAGE_INFO_VALUE_COLUMN]

#define KAA_SELECT_STORAGE_INFO \
[NSString stringWithFormat:@"SELECT %@ FROM %@ WHERE %@ = ?", \
STORAGE_INFO_VALUE_COLUMN, STORAGE_INFO_TABLE_NAME, STORAGE_INFO_KEY_COLUMN]

#endif /* PersistentLogStorageConstants_h */
