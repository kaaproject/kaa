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

#import "MemBucket.h"
#import "KaaLogging.h"

#define TAG @"MemBucket >>>"

@interface MemBucket ()

@property (nonatomic) int64_t maxSize;
@property (nonatomic) int32_t maxRecordCount;
@property (nonatomic) int64_t size;

@end

@implementation MemBucket

- (instancetype)initWithId:(int32_t)bucketId maxSize:(int64_t)maxSize maxRecordCount:(int32_t)maxRecordCount {
    self = [super init];
    if (self) {
        _bucketId = bucketId;
        _maxSize = maxSize;
        _maxRecordCount = maxRecordCount;
        _records = [NSMutableArray array];
        _state = MEM_BUCKET_STATE_FREE;
    }
    return self;
}

- (int64_t)getSize {
    return _size;
}

- (int32_t)getCount {
    return [self.records count];
}

- (BOOL)addRecord:(LogRecord *)record {
    if (self.size + [record getSize] > self.maxSize) {
        DDLogVerbose(@"%@ No space left in bucket. Current size: %li, record size: %li, max size: %li", TAG,
                     (long)self.size, (long)[record getSize], (long)self.maxSize);
        return NO;
    } else if ([self getCount] + 1 > self.maxRecordCount) {
        DDLogVerbose(@"%@ No space left in bucket. Current count: %li, max count: %li", TAG,
                     (long)[self getCount], (long)self.maxRecordCount);
        return NO;
    }
    [self.records addObject:record];
    self.size += [record getSize];
    return YES;
}

- (NSArray *)shrinkToSize:(int64_t)newSize newCount:(int32_t)newCount {
    DDLogVerbose(@"%@ Shrinking %@ bucket to the new size: %li and count %li", TAG,
                 self, (long)newSize, (long)newCount);
    if (newSize < 0 || newCount < 0) {
        [NSException raise:NSInvalidArgumentException format:@"New size and count values must be non-negative"];
    }
    if (newSize >= self.size && newCount >= [self getCount]) {
        return [NSArray array];
    }
    NSMutableArray *overSize = [NSMutableArray array];
    NSInteger lastIndex = [self.records count] - 1;
    while ((self.size > newSize || [self getCount] > newCount) && lastIndex > 0) {
        LogRecord *currentRecord = [self.records objectAtIndex:lastIndex];
        lastIndex--;
        [overSize addObject:currentRecord];
        self.size -= [currentRecord getSize];
        [self.records removeObjectAtIndex:lastIndex];
    }
    DDLogVerbose(@"%@ Shrink over-sized elements: %li. New bucket size: %li and count %li", TAG,
                 (long)[overSize count], (long)self.size, (long)[self getCount]);
    return overSize;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"MemBucket id:%li maxSize:%li maxRecordCount:%li records count:%li size:%li state:%i", (long)self.bucketId, (long)self.maxSize, (long)self.maxRecordCount,
            (long)[self.records count], (long)self.size, self.state];
}

@end
