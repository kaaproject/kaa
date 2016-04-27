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

#import "DefaultLogUploadStrategy.h"
#import "TimeCommons.h"

/**
 * Issue log upload when reaches records count threshold in storage 
 * or records are stored longer than specified time limit.
 */
@interface RecordCountWithTimeLimitLogUploadStrategy : DefaultLogUploadStrategy

@property (nonatomic) int64_t lastUploadTime;

- (instancetype)initWithCountThreshold:(int32_t)countThreshold timeLimit:(int64_t)timeLimit timeUnit:(TimeUnit)timeUnit;

@end
