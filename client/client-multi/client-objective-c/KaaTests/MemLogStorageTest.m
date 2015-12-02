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

#import <XCTest/XCTest.h>
#import "LogStorage.h"
#import "MemLogStorage.h"

@interface MemLogStorageTest : XCTestCase

@end

@implementation MemLogStorageTest

- (void) testRemovalWithBucketShrinking {
    id <LogStorage> storage = [self getStorageWithBucketSize:10 andRecordCount:4];
    LogRecord *record = [self getLogRecord];
    
    int32_t insertionCount = 12;
    
    /*
     * Size of each record is 3B
     */
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    int64_t maxSize = 6;
    int32_t maxCount = 3;
    LogBlock *logBlock = [storage getRecordBlock:maxSize batchCount:maxCount];
    XCTAssertTrue([[logBlock logRecords] count] <= maxCount);
    XCTAssertTrue([self getLogBlockSize:logBlock] <= maxSize);
    XCTAssertEqual(insertionCount - [[logBlock logRecords] count], [[storage getStatus] getRecordCount]);
}

- (void)testEmptyLogRecord {
    int64_t bucketSize = 3;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
    LogBlock *group = [storage getRecordBlock:5 batchCount:1];
    XCTAssertNil(group);
    [storage close];
}

- (void)testRecordCountAndConsumedBytes {
    int64_t bucketSize = 3;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
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
    int64_t bucketSize = 3;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
    //size of each record is 3B
    int32_t insertionCount = 3;
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBlock *group1 = [storage getRecordBlock:6 batchCount:2];
    LogBlock *group2 = [storage getRecordBlock:6 batchCount:2];
    XCTAssertNotEqual([group1 blockId], [group2 blockId]);
    [storage close];
}

- (void) testLogRecordAdding {
    //size of each record is 3B
    [self testAddHelper:1 :3 :1 :1];
    [self testAddHelper:4 :3 :2 :1];
    [self testAddHelper:3 :9 :4 :3];
    [self testAddHelper:5 :5 :2 :1];
}

- (void) testGetSameLogBlock {
    int64_t bucketSize = 3;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
    int32_t iter = 3;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBlock *group1 = [storage getRecordBlock:7 batchCount:2];
    [storage notifyUploadFailed:[group1 blockId]];
    LogBlock *group2 = [storage getRecordBlock:7 batchCount:2];
    
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

- (void) testRecordRemoval {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
    NSInteger insertionCount = 7;
    NSInteger iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBlock *removingBlock = [storage getRecordBlock:7 batchCount:2];
    
    insertionCount -= [[removingBlock logRecords] count];
    [storage removeRecordBlock:[removingBlock blockId]];
    removingBlock = [storage getRecordBlock:9 batchCount:3];
    
    insertionCount -= [[removingBlock logRecords] count];
    [storage removeRecordBlock:[removingBlock blockId]];
    
    LogBlock *leftBlock = [storage getRecordBlock:50 batchCount:50];
    XCTAssertTrue([[leftBlock logRecords] count] == insertionCount);
    [storage close];
}

- (void) testComplexRemoval {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
    int32_t insertionCount = 8;
    NSInteger iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBlock *removingBlock1 = [storage getRecordBlock:7 batchCount:2];
    insertionCount -= [[removingBlock1 logRecords] count];
    
    LogBlock *removingBlock2 = [storage getRecordBlock:9 batchCount:3];
    insertionCount -= [[removingBlock2 logRecords] count];
    
    LogBlock *removingBlock3 = [storage getRecordBlock:6 batchCount:2];
    insertionCount -= [[removingBlock3 logRecords] count];
    
    [storage removeRecordBlock:[removingBlock2 blockId]];
    [storage notifyUploadFailed:[removingBlock1 blockId]];
    insertionCount += [[removingBlock1 logRecords] count];
    
    LogBlock *leftBlock1 = [storage getRecordBlock:50 batchCount:50];
    LogBlock *leftBlock2 = [storage getRecordBlock:50 batchCount:50];
    NSInteger leftSize = [[leftBlock1 logRecords] count];
    
    if (leftBlock2)
        leftSize += [[leftBlock2 logRecords] count];
    
    XCTAssertEqual(leftSize, insertionCount);
    
    [storage close];
}

- (void) testLogStorageCountAndVolume {
    int64_t bucketSize = 9;
    int32_t recordCount = 3;
    
    id <LogStorage> storage = [self getStorageWithBucketSize:bucketSize andRecordCount:recordCount];
    LogRecord *record = [self getLogRecord];
    
    int32_t insertionCount = 9;
    int32_t receivedCount = 0;
    int32_t iter = insertionCount;
    
    while (iter-- > 0) {
        [storage addLogRecord:record];
    }
    
    LogBlock *logBlock = [storage getRecordBlock:6 batchCount:2];
    receivedCount = [self addIfNotEmpty:receivedCount :logBlock];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
    logBlock = [storage getRecordBlock:7 batchCount:3];
    receivedCount = [self addIfNotEmpty:receivedCount :logBlock];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
    logBlock = [storage getRecordBlock:9 batchCount:2];
    receivedCount = [self addIfNotEmpty:receivedCount :logBlock];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
    [storage notifyUploadFailed:[logBlock blockId]];
    receivedCount -= [[logBlock logRecords] count];
    XCTAssertEqual(insertionCount - receivedCount, [[storage getStatus] getRecordCount]);
    XCTAssertEqual((insertionCount - receivedCount) * 3, [[storage getStatus] getConsumedVolume]);
    
}

- (void)testAddHelper:(int32_t)addedN :(int32_t)blockSize :(int32_t)batchSize :(int32_t)expectedN {
    id <LogStorage> storage = [self getStorageWithBucketSize:blockSize andRecordCount:batchSize];
    LogRecord *record = [self getLogRecord];
    NSMutableArray *expectedArray = [NSMutableArray array];
    
    while (addedN-- > 0) {
        [storage addLogRecord:record];
    }
    
    while (expectedN-- > 0) {
        [expectedArray addObject:record];
    }
    
    LogBlock *group = [storage getRecordBlock:blockSize batchCount:batchSize];
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

- (int32_t)addIfNotEmpty:(int32_t)count :(LogBlock *)logBlock {
    if (logBlock && [[logBlock logRecords] count] > 0) {
        count += [[logBlock logRecords] count];
    }
    return count;
}

- (MemLogStorage *)getStorageWithBucketSize:(int64_t)bucketSize andRecordCount:(int32_t)recordCount {
    return [[MemLogStorage alloc]initWithBucketSize:bucketSize bucketRecordCount:recordCount];
}

- (int64_t) getLogBlockSize:(LogBlock *)logBlock {
    int64_t size = 0;
    for (LogRecord *record in [logBlock logRecords]) {
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
