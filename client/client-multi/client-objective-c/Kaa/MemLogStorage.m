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

#import "MemLogStorage.h"
#import "MemBucket.h"
#import "KaaLogging.h"

#define TAG @"MemLogStorage >>>"

#define DEFAULT_MAX_STORAGE_SIZE        (16 * 1024 * 1024)
#define DEFAULT_MAX_BUCKET_SIZE         (16 * 1024)
#define DEFAULT_MAX_BUCKET_RECORD_COUNT (256)

@interface MemLogStorage ()

@property (nonatomic) int64_t maxStorageSize;
@property (nonatomic) int64_t maxBucketSize;
@property (nonatomic) int32_t maxBucketRecordCount;
@property (nonatomic,strong) NSMutableDictionary *buckets;
@property (atomic) int32_t bucketIdSeq;
@property (atomic) volatile int64_t consumedVolume;
@property (atomic) volatile int64_t recordCount;

@property (nonatomic,strong) MemBucket *currentBucket;

@end

@implementation MemLogStorage

- (instancetype)initWithDefaults {
    self = [super init];
    if (self) {
        self.maxStorageSize = DEFAULT_MAX_STORAGE_SIZE;
        self.maxBucketSize = DEFAULT_MAX_BUCKET_SIZE;
        self.maxBucketRecordCount = DEFAULT_MAX_BUCKET_RECORD_COUNT;
        self.bucketIdSeq = 0;
        self.buckets = [NSMutableDictionary dictionary];
    }
    return self;
}

- (instancetype)initWithBucketSize:(int64_t)maxBucketSize bucketRecordCount:(int32_t)maxBucketRecordCount {
    self = [super init];
    if (self) {
        self.maxStorageSize = DEFAULT_MAX_STORAGE_SIZE;
        self.maxBucketSize = maxBucketSize;
        self.maxBucketRecordCount = maxBucketRecordCount;
        self.bucketIdSeq = 0;
        self.buckets = [NSMutableDictionary dictionary];
    }
    return self;
}

- (instancetype)initWithMaxStorageSize:(int64_t)maxStorageSize bucketSize:(int64_t)bucketSize
                     bucketRecordCount:(int32_t)bucketRecordCount {
    self = [super init];
    if (self) {
        self.maxStorageSize = maxStorageSize;
        self.maxBucketSize = bucketSize;
        self.maxBucketRecordCount = bucketRecordCount;
        self.bucketIdSeq = 0;
        self.buckets = [NSMutableDictionary dictionary];
    }
    return self;
}

- (int64_t)getConsumedVolume {
    DDLogDebug(@"%@ Consumed volume: %li", TAG, (long)_consumedVolume);
    return _consumedVolume;
}

- (int64_t)getRecordCount {
    DDLogDebug(@"%@ Record count: %li", TAG, (long)_recordCount);
    return _recordCount;
}

- (void)addLogRecord:(LogRecord *)record {
    DDLogVerbose(@"%@ Adding new log record with size %li", TAG, (long)[record getSize]);
    if ([record getSize] > self.maxBucketSize) {
        [NSException raise:NSInvalidArgumentException format:@"Record size(%li) is bigger than max bucket size(%li)!",
         (long)[record getSize], (long)self.maxBucketSize];
    }
    @synchronized(self.buckets) {
        if (self.consumedVolume + [record getSize] > self.maxStorageSize) {
            [NSException raise:@"IllegalStateException" format:@"Storage is full!"];
        }
        if (!self.currentBucket || self.currentBucket.state != MEM_BUCKET_STATE_FREE) {
            self.currentBucket = [[MemBucket alloc] initWithId:self.bucketIdSeq++ maxSize:self.maxBucketSize maxRecordCount:self.maxBucketRecordCount];
            [self.buckets setObject:self.currentBucket forKey:[NSNumber numberWithLong:self.currentBucket.bucketId]];
        }
        if (![self.currentBucket addRecord:record]) {
            DDLogVerbose(@"%@ Current bucket is full. Creating new one.", TAG);
            self.currentBucket.state = MEM_BUCKET_STATE_FULL;
            self.currentBucket = [[MemBucket alloc] initWithId:self.bucketIdSeq++ maxSize:self.maxBucketSize maxRecordCount:self.maxBucketRecordCount];
            [self.buckets setObject:self.currentBucket forKey:[NSNumber numberWithLong:self.currentBucket.bucketId]];
            [self.currentBucket addRecord:record];
        }
        self.recordCount++;
        self.consumedVolume += [record getSize];
    }
    
    DDLogVerbose(@"%@ Added a new log record to bucket with id [%li]", TAG, (long)[self.currentBucket bucketId]);
}

- (LogBlock *)getRecordBlock:(int64_t)blockSize batchCount:(int32_t)batchCount {
    DDLogVerbose(@"%@ Getting new record block with block size: %li and count: %li", TAG, (long)blockSize, (long)batchCount);
    if (blockSize > self.maxBucketSize || batchCount > self.maxBucketRecordCount) {
        //TODO add support of block resize
        DDLogWarn(@"%@ Resize of record block is not supported yet", TAG);
    }
    LogBlock *result = nil;
    MemBucket *bucketCandidate = nil;
    @synchronized(self.buckets) {
        for (MemBucket *bucket in self.buckets.allValues) {
            if (bucket.state == MEM_BUCKET_STATE_FREE) {
                bucketCandidate = bucket;
            }
            if (bucket.state == MEM_BUCKET_STATE_FULL) {
                bucket.state = MEM_BUCKET_STATE_PENDING;
                bucketCandidate = bucket;
                break;
            }
        }
        if (bucketCandidate) {
            self.consumedVolume -= [bucketCandidate getSize];
            self.recordCount -= [bucketCandidate getCount];
            if (bucketCandidate.state == MEM_BUCKET_STATE_FREE) {
                DDLogVerbose(@"%@ Only a bucket with state FREE found: [%li]. Changing its state to PENDING",
                             TAG, (long)bucketCandidate.bucketId);
                bucketCandidate.state = MEM_BUCKET_STATE_PENDING;
            }
            if ([bucketCandidate getSize] <= blockSize && [bucketCandidate getCount] <= batchCount) {
                result = [[LogBlock alloc] initWithBlockId:bucketCandidate.bucketId andRecords:bucketCandidate.records];
                DDLogDebug(@"%@ Return record block with records count: [%li]", TAG, (long)[bucketCandidate getCount]);
            } else {
                DDLogDebug(@"%@ Shrinking bucket %@ to new size: [%li] and count: [%li]", TAG, bucketCandidate, (long)blockSize, (long)batchCount);
                NSArray *overSized = [bucketCandidate shrinkToSize:blockSize newCount:batchCount];
                result = [[LogBlock alloc] initWithBlockId:bucketCandidate.bucketId andRecords:bucketCandidate.records];
                for (LogRecord *logRecord in overSized) {
                    [self addLogRecord:logRecord];
                }
            }
        }
    }
    return result;
}

- (void)removeRecordBlock:(int32_t)blockId {
    DDLogVerbose(@"%@ Removing record block with id [%li]", TAG, (long)blockId);
    @synchronized(self.buckets) {
        [self.buckets removeObjectForKey:[NSNumber numberWithLong:blockId]];
    }
}

- (void)notifyUploadFailed:(int32_t)blockId {
    DDLogVerbose(@"%@ Upload of record block [%li] failed", TAG, (long)blockId);
    @synchronized(self.buckets) {
        MemBucket * bucket = [self.buckets objectForKey:[NSNumber numberWithLong:blockId]];
        bucket.state = MEM_BUCKET_STATE_FULL;
        self.consumedVolume += [bucket getSize];
        self.recordCount += [bucket getCount];
    }
}

- (void)close {
    DDLogDebug(@"%@ Closing log storage", TAG);
    //TODO: forgot to clean up anything?
}

- (id<LogStorageStatus>)getStatus {
    return self;
}

@end
