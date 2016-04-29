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

#import <Foundation/Foundation.h>
#import "LogRecord.h"

typedef enum {
    MEM_BUCKET_STATE_FREE,
    MEM_BUCKET_STATE_FULL,
    MEM_BUCKET_STATE_PENDING
} MemBucketState;

@interface MemBucket : NSObject

@property (nonatomic, readonly) int32_t bucketId;
@property (nonatomic, strong, readonly) NSMutableArray *records;
@property (nonatomic) MemBucketState state;

- (instancetype)initWithId:(int32_t)bucketId maxSize:(int64_t)maxSize maxRecordCount:(int32_t)maxRecordCount;

- (int64_t)getSize;

- (int32_t)getCount;

- (BOOL)addRecord:(LogRecord *)record;

@end
