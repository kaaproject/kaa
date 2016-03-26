/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import <XCTest/XCTest.h>
#import "MemLogStorage.h"
#import "SQLiteLogStorage.h"
#import "LogTestHelper.h"

@interface CommonLogStorageTests : XCTestCase

@end

@implementation CommonLogStorageTests

- (NSArray *)storagesToTestWithBucketSize:(int64_t)bucketSize recordCount:(int32_t)recordCount {
    NSMutableArray *storagesToTest = [NSMutableArray array];
    [storagesToTest addObject:[[MemLogStorage alloc]initWithBucketSize:bucketSize bucketRecordCount:recordCount]];
    [storagesToTest addObject:[[SQLiteLogStorage alloc] initWithDatabaseName:DEFAULT_TEST_DB_NAME
                                                                  bucketSize:bucketSize
                                                           bucketRecordCount:recordCount]];
    return storagesToTest;
}

- (void)setUp {
    [super setUp];
    
    NSString *appSupportDir = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES)[0];
    NSString *dbPath = [appSupportDir stringByAppendingPathComponent:DEFAULT_TEST_DB_NAME];
    [[NSFileManager defaultManager] removeItemAtPath:dbPath error:nil];
}

- (void)testRemoval {
    int64_t maxBucketSize = 10;
    int32_t maxRecordCount = 4;
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t insertionCount = 12;
        
        int32_t iter = insertionCount;
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        LogBucket *logBucket = [storage getNextBucket];
        XCTAssertTrue([[logBucket logRecords] count] <= maxRecordCount, @"Failue for storage: %@", [(id)storage class]);
        XCTAssertTrue([self getLogBucketSize:logBucket] <= maxBucketSize, @"Failue for storage: %@", [(id)storage class]);
        XCTAssertEqual(insertionCount - [[logBucket logRecords] count], [[storage getStatus] getRecordCount],
                       @"Failue for storage: %@", [(id)storage class]);
        [storage close];
    }
}

- (void)testEmptyLogRecord {
    int64_t bucketSize = 3;
    int32_t recordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:bucketSize recordCount:recordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogBucket *bucket = [storage getNextBucket];
        XCTAssertNil(bucket, @"Failue for storage: %@", [(id)storage class]);
        [storage close];
    }
}

- (void)testRecordCountAndConsumedBytes {
    int64_t maxBucketSize = 3;
    int32_t maxRecordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t insertionCount = 3;
        int32_t iter = insertionCount;
        
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        XCTAssertTrue([[storage getStatus] getRecordCount] == insertionCount,
                      @"Failue for storage: %@", [(id)storage class]);
        XCTAssertTrue([[storage getStatus] getConsumedVolume] == (insertionCount * [record getSize]),
                      @"Failue for storage: %@", [(id)storage class]);
        [storage close];
    }
}

- (void)testUniqueIdGenerator {
    int64_t maxBucketSize = 3;
    int32_t maxRecordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t insertionCount = 3;
        int32_t iter = insertionCount;
        
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        LogBucket *group1 = [storage getNextBucket];
        LogBucket *group2 = [storage getNextBucket];
        XCTAssertNotEqual([group1 bucketId], [group2 bucketId], @"Failue for storage: %@", [(id)storage class]);
        [storage close];
    }
}

- (void)testLogRecordAdding {
    [self helpAddingRecords:1 bucketSize:3 recordCount:1 expectedCount:1];
    [self helpAddingRecords:4 bucketSize:3 recordCount:2 expectedCount:1];
    [self helpAddingRecords:3 bucketSize:9 recordCount:4 expectedCount:3];
    [self helpAddingRecords:5 bucketSize:5 recordCount:2 expectedCount:1];
}

- (void)testGetSameLogBucket {
    int64_t maxBucketSize = 3;
    int32_t maxRecordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t iter = 3;
        
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        LogBucket *group1 = [storage getNextBucket];
        [storage rollbackBucketWithId:[group1 bucketId]];
        LogBucket *group2 = [storage getNextBucket];
        
        XCTAssertTrue([[group1 logRecords] count] == [[group2 logRecords] count], @"Failue for storage: %@", [(id)storage class]);
        
        NSArray *group1Array = [NSArray arrayWithArray:[group1 logRecords]];
        NSArray *group2Array = [NSArray arrayWithArray:[group2 logRecords]];
        
        for (int i = 0; i < [group1Array count]; i++) {
            LogRecord *expected = group1Array[i];
            LogRecord *actual = group2Array[i];
            
            XCTAssertTrue([expected getSize] == [actual getSize], @"Failue for storage: %@", [(id)storage class]);
            XCTAssertEqualObjects([expected data], [actual data], @"Failue for storage: %@", [(id)storage class]);
        }
        [storage close];
    }
}

- (void)testRecordRemoval {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:bucketSize recordCount:recordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t insertionCount = 7;
        int32_t iter = insertionCount;
        
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        LogBucket *removingBucket = [storage getNextBucket];
        
        insertionCount -= [[removingBucket logRecords] count];
        [storage removeBucketWithId:[removingBucket bucketId]];
        removingBucket = [storage getNextBucket];
        
        insertionCount -= [[removingBucket logRecords] count];
        [storage removeBucketWithId:[removingBucket bucketId]];
        
        LogBucket *leftBucket = [storage getNextBucket];
        XCTAssertTrue([[leftBucket logRecords] count] == insertionCount, @"Failue for storage: %@", [(id)storage class]);
        [storage close];
    }
}

- (void)testComplexRemoval {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:bucketSize recordCount:recordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t insertionCount = 8;
        int32_t iter = insertionCount;
        
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        LogBucket *removingBucket1 = [storage getNextBucket];
        insertionCount -= [[removingBucket1 logRecords] count];
        
        LogBucket *removingBucket2 = [storage getNextBucket];
        insertionCount -= [[removingBucket2 logRecords] count];
        
        LogBucket *removingBucket3 = [storage getNextBucket];
        insertionCount -= [[removingBucket3 logRecords] count];
        
        [storage removeBucketWithId:[removingBucket2 bucketId]];
        [storage rollbackBucketWithId:[removingBucket1 bucketId]];
        insertionCount += [[removingBucket1 logRecords] count];
        
        LogBucket *leftBucket1 = [storage getNextBucket];
        LogBucket *leftBucket2 = [storage getNextBucket];
        NSInteger leftSize = [[leftBucket1 logRecords] count];
        
        if (leftBucket2)
            leftSize += [[leftBucket2 logRecords] count];
        
        XCTAssertEqual(leftSize, insertionCount, @"Failue for storage: %@", [(id)storage class]);
        
        [storage close];
    }
}

- (void)testLogStorageCountAndVolume {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:bucketSize recordCount:recordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        
        int32_t insertionCount = 9;
        int32_t receivedCount = 0;
        int32_t iter = insertionCount;
        
        while (iter-- > 0) {
            [storage addLogRecord:record];
        }
        
        LogBucket *logBucket = [storage getNextBucket];
        receivedCount = [self addRecordCountIfNotEmpty:receivedCount toLogBucket:logBucket];
        XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount],
                       @"Failue for storage: %@", [(id)storage class]);
        XCTAssertEqual((insertionCount - receivedCount) * RECORD_PAYLOAD_SIZE, [[storage getStatus] getConsumedVolume],
                       @"Failue for storage: %@", [(id)storage class]);
        
        logBucket = [storage getNextBucket];
        receivedCount = [self addRecordCountIfNotEmpty:receivedCount toLogBucket:logBucket];
        XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount],
                       @"Failue for storage: %@", [(id)storage class]);
        XCTAssertEqual((insertionCount - receivedCount) * RECORD_PAYLOAD_SIZE, [[storage getStatus] getConsumedVolume],
                       @"Failue for storage: %@", [(id)storage class]);
        
        logBucket = [storage getNextBucket];
        receivedCount = [self addRecordCountIfNotEmpty:receivedCount toLogBucket:logBucket];
        XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount],
                       @"Failue for storage: %@", [(id)storage class]);
        XCTAssertEqual((insertionCount - receivedCount) * RECORD_PAYLOAD_SIZE, [[storage getStatus] getConsumedVolume],
                       @"Failue for storage: %@", [(id)storage class]);
        
        [storage rollbackBucketWithId:[logBucket bucketId]];
        receivedCount -= [[logBucket logRecords] count];
        XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount],
                       @"Failue for storage: %@", [(id)storage class]);
        XCTAssertEqual((insertionCount - receivedCount) * RECORD_PAYLOAD_SIZE, [[storage getStatus] getConsumedVolume],
                       @"Failue for storage: %@", [(id)storage class]);
    }
}

- (void)helpAddingRecords:(int32_t)addedCount
               bucketSize:(int32_t)bucketSize
              recordCount:(int32_t)recordCount
            expectedCount:(int32_t)expectedCount {
    
    NSArray *storagesToTest = [self storagesToTestWithBucketSize:bucketSize recordCount:recordCount];
    
    for (id<LogStorage> storage in storagesToTest) {
        LogRecord *record = [LogTestHelper defaultLogRecord];
        NSMutableArray *expectedArray = [NSMutableArray array];
        
        while (addedCount-- > 0) {
            [storage addLogRecord:record];
        }
        
        while (expectedCount-- > 0) {
            [expectedArray addObject:record];
        }
        
        LogBucket *group = [storage getNextBucket];
        NSArray *actualArray = [group logRecords];
        
        XCTAssertTrue([expectedArray count] == [actualArray count], @"Failue for storage: %@", [(id)storage class]);
        
        for (int i = 0; i < [expectedArray count]; i++) {
            LogRecord *expected = expectedArray[i];
            LogRecord *actual = actualArray[i];
            
            XCTAssertTrue([expected getSize] == [actual getSize], @"Failue for storage: %@", [(id)storage class]);
            XCTAssertEqualObjects([expected data], [actual data], @"Failue for storage: %@", [(id)storage class]);
        }
        [storage close];
    }
}

- (int32_t)addRecordCountIfNotEmpty:(int32_t)count toLogBucket:(LogBucket *)logBucket {
    if (logBucket && [[logBucket logRecords] count] > 0) {
        count += [[logBucket logRecords] count];
    }
    return count;
}

- (int64_t)getLogBucketSize:(LogBucket *)logBucket {
    int64_t size = 0;
    for (LogRecord *record in [logBucket logRecords]) {
        size += [record getSize];
    }
    return size;
}

@end
