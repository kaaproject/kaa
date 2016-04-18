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
#import "TimeCommons.h"
#import "BucketInfo.h"

/**
 * Designed to track bucket upload.
 */
@interface BucketRunner : NSOperation

@property (nonatomic, readonly) int32_t runnerId;

- (BOOL)isRunnerDone;

- (void)setValue:(BucketInfo *)value;

/**
 * Waits if necessary for the computation to complete, and then retrieves its result.
 *
 * <b>NOTE:</b> Method should not be called on main thread since main thread should be
 * accessible from internal components or external libraries.
 *
 * <b>Important:</b> Raises KaaRuntimeException if method is called in main thread.
 *
 * @return The computed result
 */
- (BucketInfo *)getValue;

/**
 * Waits if necessary for at most the given time for the computation
 * to complete, and then retrieves its result, if available.
 *
 * <b>NOTE:</b> Method should not be called on main thread since main thread should be
 * accessible from internal components or external libraries.
 *
 * @param timeout The maximum time to wait
 * @param unit The time unit of the timeout argument
 *
 * <b>Important:</b> Raises KaaRuntimeException if method is called on main thread.
 *
 * @return The computed result
 */
- (BucketInfo *)getValueWithTimeout:(int64_t)timeout timeUnit:(TimeUnit)timeUnit;

@end
