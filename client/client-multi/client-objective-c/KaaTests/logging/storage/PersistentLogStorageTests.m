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

#import <XCTest/XCTest.h>
#import "SQLiteLogStorage.h"
#import "LogTestHelper.h"

@interface PersistentLogStorageTests : XCTestCase

@end

@implementation PersistentLogStorageTests

- (void)setUp {
    [super setUp];
    NSString *appSupportDir = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES)[0];
    NSString *dbPath = [appSupportDir stringByAppendingPathComponent:DEFAULT_TEST_DB_NAME];
    [[NSFileManager defaultManager] removeItemAtPath:dbPath error:nil];
}

- (void)testPersistDBState {
    
    int64_t bucketSize = 3;
    int32_t recordCount = 2;
    
    id<LogStorage> storage = [self logStorageWithBucketSize:bucketSize recordCount:recordCount];

    LogRecord *logRecord = [LogTestHelper defaultLogRecord];
    
    int32_t insertionCount = 7;

    int32_t iter = insertionCount;
    while (iter-- > 0) {
        [storage addLogRecord:logRecord];
    }
    
    LogBucket *beforePersist = [storage getNextBucket];
    [storage close];
    
    storage = [self logStorageWithBucketSize:bucketSize recordCount:recordCount];
    
    XCTAssertEqual(insertionCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual(insertionCount * RECORD_PAYLOAD_SIZE, [[storage getStatus] getConsumedVolume]);
    
    LogBucket *afterPersist = [storage getNextBucket];
    XCTAssertEqual([beforePersist.logRecords count], [afterPersist.logRecords count]);
    
    [storage close];
}

- (void)testGetBigLogBucket {
    int64_t bucketSize = 8192;
    int32_t recordCount = 1000;
    
    id<LogStorage> storage = [self logStorageWithBucketSize:bucketSize recordCount:recordCount];
    
    LogRecord *logRecord = [LogTestHelper defaultLogRecord];
    
    int32_t insertionCount = 7;
    
    /*
     * Size of each record is 3B
     */
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:logRecord];
    }
    
    LogBucket *bucket = [storage getNextBucket];
    
    XCTAssertEqual(insertionCount, [bucket.logRecords count]);
    
    [storage close];
}

- (id<LogStorage>)logStorageWithBucketSize:(int64_t)bucketSize recordCount:(int32_t)recordCount {
    return [[SQLiteLogStorage alloc] initWithDatabaseName:DEFAULT_TEST_DB_NAME bucketSize:bucketSize bucketRecordCount:recordCount];
}

@end
