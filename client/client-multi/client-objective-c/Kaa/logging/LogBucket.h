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

#import <Foundation/Foundation.h>

/**
 * Wrapper class for a log bucket which is going to be sent.
 *
 * Each log bucket should have its unique id to be mapped in the log storage and
 * delivery stuff.
 */
@interface LogBucket : NSObject

//Unique id for sending log bucket
@property(nonatomic,readonly) int32_t bucketId;
//List of sending log records <LogRecord>
@property(nonatomic,strong,readonly) NSArray* logRecords;

- (instancetype)initWithBucketId:(int32_t)bucketId andRecords:(NSArray *)logRecords;

@end
