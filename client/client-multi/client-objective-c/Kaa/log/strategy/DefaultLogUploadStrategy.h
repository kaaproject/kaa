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
#import "LogUploadStrategy.h"

/**
 * Default implementation of log upload strategy.
 */
@interface DefaultLogUploadStrategy : NSObject <LogUploadStrategy>

@property (nonatomic) int32_t timeout;
@property (nonatomic) int32_t uploadCheckPeriod;
@property (nonatomic) int32_t retryPeriod;
@property (nonatomic) int32_t volumeThreshold;
@property (nonatomic) int32_t countThreshold;
@property (nonatomic) int64_t batchSize;
@property (nonatomic) int32_t batchCount;
@property (nonatomic) BOOL    isUploadLocked;
@property (nonatomic) int64_t timeLimit;
@property (nonatomic) int64_t maxParallelUploads;

- (instancetype)initWithDefaults;

@end
