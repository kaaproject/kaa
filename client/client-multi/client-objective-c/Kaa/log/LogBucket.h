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

/**
 * The helper class which is used to transfer logs from LogStorage to LogCollector.
 *
 * NOTE: the bucket id should be unique across all available log buckets.
 */
@interface LogBucket : NSObject

/**
 * The unique id of a log bucket
 */
@property(nonatomic, readonly) int32_t bucketId;

/**
 * Log records as <LogRecord>
 */
@property(nonatomic, strong, readonly) NSArray* logRecords;

- (instancetype)initWithBucketId:(int32_t)bucketId records:(NSArray *)logRecords;

@end
