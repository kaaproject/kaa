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

#ifndef Kaa_ExecutorContext_h
#define Kaa_ExecutorContext_h

#import <Foundation/Foundation.h>

/**
 * Responsible for creation of thread executor instances for SDK internal usage.
 * Implementation should not manage created executor life-cycle. Executors will be stopped during
 * "stop" procedure, thus executor instances should not be cached in context 
 * or context should check shutdown status before return of cached value.
 */
@protocol ExecutorContext

/**
 * Initialize executors.
 */
- (void)initiate;

/**
 * Stops executors.
 */
- (void)stop;

/**
 * Operation queue that executes lifecycle events/commands of Kaa client.
 */
- (NSOperationQueue *)getLifeCycleExecutor;

/**
 * Operation queue that executes user API calls to SDK client. For example, serializing of log
 * records before submit to transport.
 */
- (NSOperationQueue *)getApiExecutor;

/**
 * Operation queue that executes callback methods provided by SDK client user.
 */
- (NSOperationQueue *)getCallbackExecutor;

/**
 * Dispatch queue that executes scheduled tasks(periodically if needed) as log upload.
 */
- (dispatch_queue_t)getSheduledExecutor;

@end

#endif
