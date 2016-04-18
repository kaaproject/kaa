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

#import "SQLiteLogStorage.h"
#import "PersistentLogStorageConstants.h"
#import <sqlite3.h>
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define LENGTH_UNLIMITED -1

#define TAG @"SQLiteLogStorage >>>"

@interface SQLiteLogStorage ()

@property (nonatomic) sqlite3 *database;
@property (nonatomic) int64_t maxBucketSize;
@property (nonatomic) int32_t maxBucketRecordCount;

@property (nonatomic) int64_t totalRecordCount;
@property (nonatomic) int64_t unmarkedRecordCount;
@property (nonatomic) int64_t unmarkedConsumedSize;
@property (nonatomic) int32_t currentBucketId;

@property (nonatomic) int64_t currentBucketSize;
@property (nonatomic) int32_t currentRecordCount;

/**
 * Format: <int32_t, int64_t> as key-value wrapped with NSNumber;
 * key:     bucket id;
 * value:   bucket size.
 */
@property (nonatomic, strong) NSMutableDictionary *consumedMemoryMap;

- (void)openDBAtPath:(NSString *)path;
- (void)executeQuery:(NSString *)query;
- (sqlite3_stmt *)statementForQuery:(NSString *)query;

- (void)truncateIfBucketSizeIncompatible;
- (void)retrieveConsumedSizeAndVolume;
- (void)retrieveBucketId;
- (void)resetBucketsState;
- (void)updateLogStorageParam:(NSString *)param withValue:(int64_t)value;
- (void)moveToNextBucket;
- (void)updateStateForBucketWithId:(int32_t)bucketId;

@end

@implementation SQLiteLogStorage

- (instancetype)initWithBucketSize:(int64_t)bucketSize bucketRecordCount:(int32_t)bucketRecordCount {
    return [self initWithDatabaseName:DEFAULT_DB_NAME
                           bucketSize:bucketSize
                    bucketRecordCount:bucketRecordCount];
}

- (instancetype)initWithDatabaseName:(NSString *)dbName
                          bucketSize:(int64_t)bucketSize
                   bucketRecordCount:(int32_t)bucketRecordCount {
    self = [super init];
    if (self) {
        
        self.consumedMemoryMap = [NSMutableDictionary dictionary];
        self.maxBucketSize = bucketSize;
        self.maxBucketRecordCount = bucketRecordCount;
        self.currentBucketId = 1;
        
        NSString *baseDir = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES)[0];
        NSString *dbPath = [baseDir stringByAppendingPathComponent:dbName];
        
        NSFileManager *fileManager = [NSFileManager defaultManager];
        if ([fileManager fileExistsAtPath:dbPath]) {
            DDLogInfo(@"%@ Opening database [%@]", TAG, dbName);
            [self openDBAtPath:dbPath];
        } else {
            NSError *error;
            BOOL dirCreated = [fileManager createDirectoryAtPath:baseDir withIntermediateDirectories:YES attributes:nil error:&error];
            if (dirCreated) {
                DDLogInfo(@"%@ Database [%@] first start!", TAG, dbName);
                [self openDBAtPath:dbPath];
            } else {
                DDLogError(@"%@ Directory [%@] doesn't exist and couldn't be created: %@", TAG, baseDir, error);
                [NSException raise:KaaRuntimeException format:@"Can't create directory for database"];
            }
        }
    }
    return self;
}

-(BucketInfo *)addLogRecord:(LogRecord *)record {
    @synchronized(self) {
        DDLogVerbose(@"%@ Adding new log record", TAG);
        
        sqlite3_stmt *statement = [self statementForQuery:KAA_INSERT_NEW_RECORD];
        
        int64_t remainingSize = self.maxBucketSize - self.currentBucketSize;
        int64_t remainingRecordCount = self.maxBucketRecordCount - self.currentRecordCount;
        
        if (remainingSize < [record getSize] || remainingRecordCount == 0) {
            [self moveToNextBucket];
        }
        
        sqlite3_bind_int64(statement, 1, self.currentBucketId);
        sqlite3_bind_blob(statement, 2, [record.data bytes], (int32_t)record.data.length, SQLITE_TRANSIENT);
        
        if (sqlite3_step(statement) == SQLITE_DONE) {
            
            int64_t insertedRowId = sqlite3_last_insert_rowid(self.database);
            if (insertedRowId >= 0) {
                self.currentBucketSize += [record getSize];
                self.currentRecordCount++;
                
                self.unmarkedConsumedSize += [record getSize];
                self.totalRecordCount++;
                self.unmarkedRecordCount++;
                
                DDLogInfo(@"%@ Added a new log record, total record count: %lld unmarked record count: %lld",
                          TAG, self.totalRecordCount, self.unmarkedRecordCount);
            } else {
                DDLogWarn(@"%@ No log record was added: %lld", TAG, insertedRowId);
            }
            
        } else {
            DDLogError(@"%@ Can't add new log record", TAG);
        }
        
        sqlite3_finalize(statement);
        
        return [[BucketInfo alloc] initWithBucketId:self.currentBucketId logCount:self.currentRecordCount];
    }
}

- (LogBucket *)getNextBucket {
    @synchronized(self) {
        DDLogDebug(@"%@ Creating new log bucket", TAG);
        
        LogBucket *logBucket = nil;
        NSMutableArray *logRecords = [NSMutableArray array];
        int32_t bucketId = 0;
        
        sqlite3_stmt *statement = [self statementForQuery:KAA_SELECT_MIN_BUCKET_ID];
        
        if (sqlite3_step(statement) == SQLITE_ROW) {
            bucketId = sqlite3_column_int(statement, 0);
        }
        
        sqlite3_finalize(statement);
        
        int64_t leftBucketSize = self.maxBucketSize;
        if (bucketId > 0) {
            statement = [self statementForQuery:KAA_SELECT_LOG_RECORDS_BY_BUCKET_ID];
            
            sqlite3_bind_int(statement, 1, bucketId);
            
            while (sqlite3_step(statement) == SQLITE_ROW) {
                const void *blob = sqlite3_column_blob(statement, 0);
                int32_t blobLength = sqlite3_column_bytes(statement, 0);
                
                [logRecords addObject:[[LogRecord alloc] initWithData:[NSData dataWithBytes:blob length:blobLength]]];
                
                leftBucketSize -= blobLength;
            }
            sqlite3_finalize(statement);
            
            if ([logRecords count] > 0) {
                
                [self updateStateForBucketWithId:bucketId];
                logBucket = [[LogBucket alloc] initWithBucketId:bucketId records:logRecords];
                
                int64_t logBucketSize = self.maxBucketSize - leftBucketSize;
                self.unmarkedConsumedSize -= logBucketSize;
                self.unmarkedRecordCount -= [logRecords count];
                self.consumedMemoryMap[@(logBucket.bucketId)] = @(logBucketSize);
                
                if (self.currentBucketId == bucketId) {
                    [self moveToNextBucket];
                }
                
                DDLogDebug(@"%@ Created log block with id [%i], size [%lld], record count [%lld]",
                           TAG, bucketId, logBucketSize, (int64_t)logRecords.count);
                
            } else {
                DDLogInfo(@"%@ No unmarked log records found", TAG);
            }
        } else {
            DDLogWarn(@"%@ Min bucket id < 0 [%i]", TAG, bucketId);
        }
        
        return logBucket;
    }
}

- (void)removeBucketWithId:(int32_t)bucketId {
    @synchronized(self) {
        sqlite3_stmt *statement = [self statementForQuery:KAA_DELETE_BY_BUCKET_ID];
        
        sqlite3_bind_int64(statement, 1, bucketId);
        
        if (sqlite3_step(statement) != SQLITE_DONE) {
            DDLogError(@"%@ Unable to remove bucket with id [%i]", TAG, bucketId);
        }
        
        int removedRecordsCount = sqlite3_changes(self.database);
        
        if (removedRecordsCount > 0) {
            self.totalRecordCount -= removedRecordsCount;
            DDLogDebug(@"%@ Removed %i records from storage. Total log record count: %lld",
                       TAG, removedRecordsCount, self.totalRecordCount);
        } else {
            DDLogDebug(@"%@ No records were removed from storage", TAG);
        }
        
        sqlite3_finalize(statement);
    }
}

- (void)rollbackBucketWithId:(int32_t)bucketId {
    @synchronized(self) {
        DDLogDebug(@"%@ Rollback bucket with id [%i]", TAG, bucketId);
        
        sqlite3_stmt *statement = [self statementForQuery:KAA_RESET_BY_BUCKET_ID];
        
        sqlite3_bind_int64(statement, 1, bucketId);
        
        if (sqlite3_step(statement) != SQLITE_DONE) {
            DDLogError(@"%@ Unable to rollback bucket with id [%i]", TAG, bucketId);
        }
        
        int resetRecordsCount = sqlite3_changes(self.database);
        
        if (resetRecordsCount > 0) {
            DDLogDebug(@"%@ Reset %i records for bucket with id [%i]", TAG, resetRecordsCount, bucketId);
            
            int64_t previouslyConsumedSize = [self.consumedMemoryMap[@(bucketId)] longLongValue];
            [self.consumedMemoryMap removeObjectForKey:@(bucketId)];
            
            self.unmarkedConsumedSize += previouslyConsumedSize;
            self.unmarkedRecordCount += resetRecordsCount;
            
        } else {
            DDLogDebug(@"%@ No records were reset for bucket with id [%i]", TAG, bucketId);
        }
        
        sqlite3_finalize(statement);
    }
}

- (id<LogStorageStatus>)getStatus {
    return self;
}

- (int64_t)getConsumedVolume {
    return _unmarkedConsumedSize;
}

- (int64_t)getRecordCount {
    return _unmarkedRecordCount;
}

- (void)close {
    if (self.database == NULL) {
        return;
    }
    
    int result = sqlite3_close(self.database);
    if (result != SQLITE_OK) {
        DDLogWarn(@"%@ Failed to close database", TAG);
    }
}

- (void)openDBAtPath:(NSString *)path {
    BOOL dbOpenResult = sqlite3_open([path UTF8String], &_database) == SQLITE_OK;
    
    if (!dbOpenResult) {
        DDLogError(@"%@ Failed to open database at path: %@", TAG, path);
        return;
    }
    
    [self executeQuery:KAA_CREATE_LOG_TABLE_IF_NOT_EXISTS];
    [self executeQuery:KAA_CREATE_INFO_TABLE_IF_NOT_EXISTS];
    [self executeQuery:KAA_CREATE_BUCKET_ID_INDEX_IF_NOT_EXISTS];
    
    [self truncateIfBucketSizeIncompatible];
    [self retrieveConsumedSizeAndVolume];
    
    if (self.totalRecordCount > 0) {
        [self retrieveBucketId];
        [self resetBucketsState];
    }
}

- (void)executeQuery:(NSString *)query {
    int resultCode = sqlite3_exec(self.database, [query UTF8String], NULL, NULL, nil);
    if (resultCode != SQLITE_OK) {
        DDLogError(@"%@ Unable to execute query: %@ error code [%i]", TAG, query, resultCode);
        [NSException raise:KaaRuntimeException format:@"Can't execute query"];
    }
}

- (sqlite3_stmt *)statementForQuery:(NSString *)query {
    sqlite3_stmt *statement;
    int resultCode = sqlite3_prepare(self.database, [query UTF8String], LENGTH_UNLIMITED, &statement, NULL);
    if (resultCode != SQLITE_OK) {
        DDLogError(@"%@ Can't prepare statement: %@; error code: %i", TAG, query, resultCode);
        [NSException raise:KaaRuntimeException format:@"Can't prepare statement"];
    }
    return statement;
}

- (void)truncateIfBucketSizeIncompatible {
    int lastSavedBucketSize = 0;
    int lastSavedRecordCount = 0;
    
    sqlite3_stmt *statement = [self statementForQuery:KAA_SELECT_STORAGE_INFO];
   
    DDLogVerbose(@"%@ Retrieving bucket size from storage info", TAG);
    sqlite3_bind_text(statement, 1, [STORAGE_BUCKET_SIZE UTF8String], LENGTH_UNLIMITED, SQLITE_TRANSIENT);
    if (sqlite3_step(statement) == SQLITE_ROW) {
        lastSavedBucketSize = sqlite3_column_int(statement, 0);
    }
    sqlite3_reset(statement);
    
    
    DDLogVerbose(@"%@ Retrieving record count from storage info", TAG);
    sqlite3_bind_text(statement, 1, [STORAGE_RECORD_COUNT UTF8String], LENGTH_UNLIMITED, SQLITE_TRANSIENT);
    if (sqlite3_step(statement) == SQLITE_ROW) {
        lastSavedRecordCount = sqlite3_column_int(statement, 0);
    }
    sqlite3_finalize(statement);
    
    if (lastSavedBucketSize != self.maxBucketSize || lastSavedRecordCount != self.maxBucketRecordCount) {
        [self executeQuery:KAA_DELETE_ALL_DATA];
    }
    
    [self updateLogStorageParam:STORAGE_BUCKET_SIZE withValue:self.maxBucketSize];
    [self updateLogStorageParam:STORAGE_RECORD_COUNT withValue:self.maxBucketRecordCount];
}

- (void)updateLogStorageParam:(NSString *)param withValue:(int64_t)value {
    sqlite3_stmt *statement = [self statementForQuery:KAA_UPDATE_STORAGE_INFO];
    
    sqlite3_bind_text(statement, 1, [param UTF8String], LENGTH_UNLIMITED, SQLITE_TRANSIENT);
    sqlite3_bind_int64(statement, 2, value);
    if (sqlite3_step(statement) != SQLITE_DONE) {
        DDLogWarn(@"%@ Can't update storage info param: %@", TAG, param);
    }
    
    sqlite3_finalize(statement);
}

- (void)retrieveConsumedSizeAndVolume {
    @synchronized(self) {
        sqlite3_stmt *statement = [self statementForQuery:KAA_GET_LOG_COUNT_IN_DB];
        
        if (sqlite3_step(statement) == SQLITE_ROW) {
            self.unmarkedRecordCount = sqlite3_column_int64(statement, 0);
            self.totalRecordCount = self.unmarkedRecordCount;
            self.unmarkedConsumedSize = sqlite3_column_int64(statement, 1);
            DDLogInfo(@"%@ Retrieved record count: %lld consumed size: %lld", TAG, self.unmarkedRecordCount, self.unmarkedConsumedSize);
        } else {
            DDLogWarn(@"%@ Can't retrieve consumed volume and size", TAG);
        }
        
        sqlite3_finalize(statement);
    }
}

- (void)retrieveBucketId {
    sqlite3_stmt *statement = [self statementForQuery:KAA_SELECT_MAX_BUCKET_ID];

    if (sqlite3_step(statement) == SQLITE_ROW) {
        int maxBucketId = sqlite3_column_int(statement, 0);
        if (maxBucketId == 0) {
            DDLogDebug(@"%@ Max bucket id is 0 - seems no logs in the storage", TAG);
        } else {
            self.currentBucketId = ++maxBucketId;
        }
    } else {
        DDLogWarn(@"%@ Can't retrieve max bucket id", TAG);
    }
    
    sqlite3_finalize(statement);
}

- (void)resetBucketsState {
    @synchronized(self) {
        DDLogDebug(@"%@ Resetting bucket ids on application start", TAG);
        [self executeQuery:KAA_RESET_BUCKET_STATE_ON_START];
        DDLogVerbose(@"%@ Affected rows: %i", TAG, sqlite3_changes(self.database));
    }
}

- (void)moveToNextBucket {
    self.currentBucketSize = 0;
    self.currentRecordCount = 0;
    self.currentBucketId++;
}

- (void)updateStateForBucketWithId:(int32_t)bucketId {
    @synchronized(self) {
        DDLogVerbose(@"%@ Updating state for bucket with id [%i]", TAG, bucketId);
        
        sqlite3_stmt *statement = [self statementForQuery:KAA_UPDATE_BUCKET_ID];
        
        sqlite3_bind_text(statement, 1, [BUCKET_STATE_COLUMN UTF8String], LENGTH_UNLIMITED, SQLITE_TRANSIENT);
        sqlite3_bind_int64(statement, 2, bucketId);
        
        if (sqlite3_step(statement) == SQLITE_DONE) {
            int affectedRows = sqlite3_changes(self.database);
            if (affectedRows > 0) {
                DDLogInfo(@"%@ Successfully updated state for bucket with id [%i] for %i records", TAG, bucketId, affectedRows);
            } else {
                DDLogWarn(@"%@ No log records were updated", TAG);
            }
        } else {
            DDLogWarn(@"%@ Can't update state for bucket id [%i]", TAG, bucketId);
        }
        
        sqlite3_finalize(statement);
    }
}

@end
