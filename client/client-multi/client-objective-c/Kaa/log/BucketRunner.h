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
#import "TimeCommons.h"
#import "BucketInfo.h"

@interface BucketRunner : NSOperation

@property (nonatomic, readonly) int64_t runnerId;

- (BOOL)isRunnerDone;

- (void)setValue:(BucketInfo *)value;

/**
 * Waits if necessary for the computation to complete, and then retrieves its result.
 *
 * @return the computed result
 */
- (BucketInfo *)getValue;

/**
 * Waits if necessary for at most the given time for the computation
 * to complete, and then retrieves its result, if available.
 *
 * @param timeout the maximum time to wait
 * @param unit the time unit of the timeout argument
 *
 * @return the computed result
 */
- (BucketInfo *)getValueWithTimeout:(int64_t)timeout andTimeUnit:(TimeUnit)timeUnit;


@end
