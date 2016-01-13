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

- (NSString *)description {
    return [NSString stringWithFormat:@"MemBucket id:%li maxSize:%li maxRecordCount:%li records count:%li size:%li state:%i", (long)self.bucketId, (long)self.maxSize, (long)self.maxRecordCount,
            (long)[self.records count], (long)self.size, self.state];
}

@end
