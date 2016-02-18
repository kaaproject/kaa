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
#import "LogStorage.h"
#import "MemLogStorage.h"

@interface MemLogStorageTest : XCTestCase

@end

@implementation MemLogStorageTest

- (void)testRemoval {
    int64_t maxBucketSize = 10;
    int32_t maxRecordCount = 4;
    id <LogStorage> storage = [self getStorageWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    LogRecord *record = [self getLogRecord];
    
    int32_t insertionCount = 12;
    
    /*
     * Size of each record is 3B
     */
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBucket *logBucket = [storage getNextBucket];
    XCTAssertTrue([[logBucket logRecords] count] <= maxRecordCount);
    XCTAssertTrue([self getLogBucketSize:logBucket] <= maxBucketSize);
    XCTAssertEqual(insertionCount - [[logBucket logRecords] count], [[storage getStatus] getRecordCount]);
    [storage close];
}

- (void)testEmptyLogRecord {
    int64_t bucketSize = 3;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize recordCount:recordCount];
    LogBucket *group = [storage getNextBucket];
    XCTAssertNil(group);
    [storage close];
}

- (void)testRecordCountAndConsumedBytes {
    int64_t maxBucketSize = 3;
    int32_t maxRecordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    LogRecord *record = [self getLogRecord];
    
    //size of each record is 3B
    int32_t insertionCount = 3;
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    XCTAssertTrue([[storage getStatus] getRecordCount] == insertionCount);
    XCTAssertTrue([[storage getStatus] getConsumedVolume] == (insertionCount * [record getSize]));
    [storage close];
}

- (void)testUniqueIdGenerator {
    int64_t maxBucketSize = 3;
    int32_t maxRecordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    LogRecord *record = [self getLogRecord];
    
    //size of each record is 3B
    int32_t insertionCount = 3;
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBucket *group1 = [storage getNextBucket];
    LogBucket *group2 = [storage getNextBucket];
    XCTAssertNotEqual([group1 bucketId], [group2 bucketId]);
    [storage close];
}

- (void)testLogRecordAdding {
    //size of each record is 3B
    [self testAddHelperWithRecordCount:1 bucketSize:3 batchSize:1 expectedCount:1];
    [self testAddHelperWithRecordCount:4 bucketSize:3 batchSize:2 expectedCount:1];
    [self testAddHelperWithRecordCount:3 bucketSize:9 batchSize:4 expectedCount:3];
    [self testAddHelperWithRecordCount:5 bucketSize:5 batchSize:2 expectedCount:1];
}

- (void)testGetSameLogBucket {
    int64_t maxBucketSize = 3;
    int32_t maxRecordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:maxBucketSize recordCount:maxRecordCount];
    LogRecord *record = [self getLogRecord];
    
    int32_t iter = 3;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBucket *group1 = [storage getNextBucket];
    [storage rollbackBucketWithId:[group1 bucketId]];
    LogBucket *group2 = [storage getNextBucket];
    
    XCTAssertTrue([[group1 logRecords] count] == [[group2 logRecords] count]);
    
    NSArray *group1Array = [NSArray arrayWithArray:[group1 logRecords]];
    NSArray *group2Array = [NSArray arrayWithArray:[group2 logRecords]];
    
    for (int i = 0; i < [group1Array count]; i++) {
        LogRecord *expected = group1Array[i];
        LogRecord *actual = group2Array[i];
        
        XCTAssertTrue([expected getSize] == [actual getSize]);
        XCTAssertEqualObjects([expected data], [actual data]);
    }
    [storage close];
}

- (void)testRecordRemoval {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize recordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
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
    XCTAssertTrue([[leftBucket logRecords] count] == insertionCount);
    [storage close];
}

- (void)testComplexRemoval {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize recordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
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
    
    XCTAssertEqual(leftSize, insertionCount);
    
    [storage close];
}

- (void)testLogStorageCountAndVolume {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize recordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
    int32_t insertionCount = 9;
    int32_t receivedCount = 0;
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBucket *logBucket = [storage getNextBucket];
    receivedCount = [self addRecordCountIfNotEmpty:receivedCount toLogBucket:logBucket];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
    logBucket = [storage getNextBucket];
    receivedCount = [self addRecordCountIfNotEmpty:receivedCount toLogBucket:logBucket];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
    logBucket = [storage getNextBucket];
    receivedCount = [self addRecordCountIfNotEmpty:receivedCount toLogBucket:logBucket];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
    [storage rollbackBucketWithId:[logBucket bucketId]];
    receivedCount -= [[logBucket logRecords] count];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
}

- (void)testAddHelperWithRecordCount:(int32_t)addedN bucketSize:(int32_t)bucketSize batchSize:(int32_t)batchSize expectedCount:(int32_t)expectedN {
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize recordCount:batchSize];
    LogRecord *record = [self getLogRecord];
    NSMutableArray *expectedArray = [NSMutableArray array];
    
    while (addedN-- > 0) {
        [storage addLogRecord:record];
    }
    
    while (expectedN-- > 0) {
        [expectedArray addObject:record];
    }
    
    LogBucket *group = [storage getNextBucket];
    NSArray *actualArray = [group logRecords];
    
    XCTAssertTrue([expectedArray count] == [actualArray count]);
    
    for (int i = 0; i < [expectedArray count]; i++) {
        LogRecord *expected = expectedArray[i];
        LogRecord *actual = actualArray[i];
        
        XCTAssertTrue([expected getSize] == [actual getSize]);
        XCTAssertEqualObjects([expected data], [actual data]);
    }
    [storage close];
}

- (int32_t)addRecordCountIfNotEmpty:(int32_t)count toLogBucket:(LogBucket *)logBucket {
    if (logBucket && [[logBucket logRecords] count] > 0) {
        count += [[logBucket logRecords] count];
    }
    return count;
}

- (MemLogStorage *)getStorageWithBucketSize:(int64_t)bucketSize recordCount:(int32_t)recordCount {
    return [[MemLogStorage alloc]initWithBucketSize:bucketSize bucketRecordCount:recordCount];
}

- (int64_t)getLogBucketSize:(LogBucket *)logBucket {
    int64_t size = 0;
    for (LogRecord *record in [logBucket logRecords]) {
        size += [record getSize];
    }
    return size;
}

- (LogRecord *)getLogRecord {
    char _1byte = 0;
    int DATA_SIZE = 3;
    NSMutableData *data = [[NSMutableData alloc] initWithCapacity:DATA_SIZE];
    for (int i = 0; i < DATA_SIZE; i++) {
        [data appendBytes:&_1byte length:sizeof(char)];
    }
    
    LogRecord *record = [[LogRecord alloc]initWithData:data];
    return record;
}

@end
