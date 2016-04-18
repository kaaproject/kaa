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

#import "SingleThreadExecutorContext.h"
#import "KaaLogging.h"

#define TAG @"SingleThreadExecutorContext >>>"

@interface SingleThreadExecutorContext ()

@property (nonatomic, strong) NSOperationQueue *singleThreadExecutor;

@end

@implementation SingleThreadExecutorContext

- (void)initiate {
    DDLogDebug(@"%@ Creating executor service", TAG);
    self.singleThreadExecutor = [[NSOperationQueue alloc] init];
    DDLogDebug(@"%@ Created executor service", TAG);
}

- (void)stop {
    [self.singleThreadExecutor cancelAllOperations];
}

- (NSOperationQueue *)getLifeCycleExecutor {
    return self.singleThreadExecutor;
}

- (NSOperationQueue *)getApiExecutor {
    return self.singleThreadExecutor;
}

- (NSOperationQueue *)getCallbackExecutor {
    return self.singleThreadExecutor;
}

- (dispatch_queue_t)getSheduledExecutor {
    return [self.singleThreadExecutor underlyingQueue];
}

@end
