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

#import "SimpleExecutorContext.h"
#import "KaaLogging.h"

#define SINGLE_THREAD 1

#define TAG @"SimpleExecutorContext >>>"

@interface SimpleExecutorContext ()

@property (nonatomic) int32_t lifeCycleThreadCount;
@property (nonatomic) int32_t apiThreadCount;
@property (nonatomic) int32_t callbackThreadCount;
@property (nonatomic) int32_t scheduledThreadCount;

@property (nonatomic, strong) NSOperationQueue *lifeCycleExecutor;
@property (nonatomic, strong) NSOperationQueue *apiExecutor;
@property (nonatomic, strong) NSOperationQueue *callBackExecutor;
@property (nonatomic, strong) NSOperationQueue *scheduledExecutor;

@end

@implementation SimpleExecutorContext

- (instancetype)init {
    return [self initWithLifeCycleThreadCount:SINGLE_THREAD
                            apiThreadCount:SINGLE_THREAD
                       callbackThreadCount:SINGLE_THREAD
                      scheduledThreadCount:SINGLE_THREAD];
}

- (instancetype)initWithLifeCycleThreadCount:(int32_t)lifeCycleThreadCount
                           apiThreadCount:(int32_t)apiThreadCount
                      callbackThreadCount:(int32_t)callbackThreadCount
                     scheduledThreadCount:(int32_t)scheduledThreadCount {
    self = [super init];
    if (self) {
        self.lifeCycleThreadCount   = lifeCycleThreadCount;
        self.apiThreadCount         = apiThreadCount;
        self.callbackThreadCount    = callbackThreadCount;
        self.scheduledThreadCount   = scheduledThreadCount;
    }
    return self;
}

- (void)initiate {
    DDLogDebug(@"%@ Creating executor services", TAG);
    self.lifeCycleExecutor = [[NSOperationQueue alloc] init];
    self.apiExecutor = [[NSOperationQueue alloc] init];
    self.callBackExecutor = [[NSOperationQueue alloc] init];
    self.scheduledExecutor = [[NSOperationQueue alloc] init];
    
    [self.lifeCycleExecutor setMaxConcurrentOperationCount:self.lifeCycleThreadCount];
    [self.apiExecutor setMaxConcurrentOperationCount:self.apiThreadCount];
    [self.callBackExecutor setMaxConcurrentOperationCount:self.callbackThreadCount];
    [self.scheduledExecutor setMaxConcurrentOperationCount:self.scheduledThreadCount];
}

- (void)stop {
    [self.lifeCycleExecutor cancelAllOperations];
    [self.apiExecutor cancelAllOperations];
    [self.callBackExecutor cancelAllOperations];
    [self.scheduledExecutor cancelAllOperations];
}

- (NSOperationQueue *)getLifeCycleExecutor {
    return self.lifeCycleExecutor;
}

- (NSOperationQueue *)getApiExecutor {
    return self.apiExecutor;
}

- (NSOperationQueue *)getCallbackExecutor {
    return self.callBackExecutor;
}

- (dispatch_queue_t)getSheduledExecutor {
    return [self.scheduledExecutor underlyingQueue];
}

@end
