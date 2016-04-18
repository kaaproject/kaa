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
@property (nonatomic, strong) NSMutableDictionary *buckets;
@property (atomic) int32_t bucketIdSeq;
@property (atomic) volatile int64_t consumedVolume;
@property (atomic) volatile int64_t recordCount;

@property (nonatomic, strong) MemBucket *currentBucket;

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

- (instancetype)initWithMaxStorageSize:(int64_t)maxStorageSize
                            bucketSize:(int64_t)bucketSize
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
    DDLogDebug(@"%@ Consumed volume: %lli", TAG, _consumedVolume);
    return _consumedVolume;
}

- (int64_t)getRecordCount {
    DDLogDebug(@"%@ Record count: %lli", TAG, _recordCount);
    return _recordCount;
}

- (BucketInfo *)addLogRecord:(LogRecord *)record {
    DDLogVerbose(@"%@ Adding new log record with size %lli", TAG, [record getSize]);
    if ([record getSize] > self.maxBucketSize) {
        [NSException raise:NSInvalidArgumentException format:@"Record size(%lli) is bigger than max bucket size(%lli)!",
         [record getSize], self.maxBucketSize];
    }
    @synchronized(self.buckets) {
        if (self.consumedVolume + [record getSize] > self.maxStorageSize) {
            [NSException raise:@"IllegalStateException" format:@"Storage is full!"];
        }
        if (!self.currentBucket || self.currentBucket.state != MEM_BUCKET_STATE_FREE) {
            self.currentBucket = [[MemBucket alloc] initWithId:self.bucketIdSeq++ maxSize:self.maxBucketSize maxRecordCount:self.maxBucketRecordCount];
            self.buckets[@(self.currentBucket.bucketId)] = self.currentBucket;
        }
        if (![self.currentBucket addRecord:record]) {
            DDLogVerbose(@"%@ Current bucket is full. Creating new one.", TAG);
            self.currentBucket.state = MEM_BUCKET_STATE_FULL;
            self.currentBucket = [[MemBucket alloc] initWithId:self.bucketIdSeq++ maxSize:self.maxBucketSize maxRecordCount:self.maxBucketRecordCount];
            self.buckets[@(self.currentBucket.bucketId)] = self.currentBucket;
            [self.currentBucket addRecord:record];
        }
        self.recordCount++;
        self.consumedVolume += [record getSize];
    }
    
    DDLogVerbose(@"%@ Added a new log record to bucket with id [%i]", TAG, [self.currentBucket bucketId]);
    
    return [[BucketInfo alloc] initWithBucketId:self.currentBucket.bucketId logCount:[self.currentBucket getCount]];
}

- (LogBucket *)getNextBucket {
    DDLogVerbose(@"%@ Getting new record bucket with bucket size: %lli and count: %i", TAG, self.maxBucketSize, self.maxBucketRecordCount);
    LogBucket *result = nil;
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
                DDLogVerbose(@"%@ Only a bucket with state FREE found: [%i]. Changing its state to PENDING",
                             TAG, bucketCandidate.bucketId);
                bucketCandidate.state = MEM_BUCKET_STATE_PENDING;
            }
            result = [[LogBucket alloc] initWithBucketId:bucketCandidate.bucketId records:bucketCandidate.records];
            DDLogDebug(@"%@ Return record bucket with records count: [%i]", TAG, [bucketCandidate getCount]);
        }
    }
    return result;
}

- (void)removeBucketWithId:(int32_t)bucketId {
    DDLogVerbose(@"%@ Removing bucket with id [%i]", TAG, bucketId);
    @synchronized(self.buckets) {
        [self.buckets removeObjectForKey:@(bucketId)];
    }
}

- (void)rollbackBucketWithId:(int32_t)bucketId {
    DDLogVerbose(@"%@ Upload of bucket [%i] failed", TAG, bucketId);
    @synchronized(self.buckets) {
        MemBucket * bucket = self.buckets[@(bucketId)];
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
