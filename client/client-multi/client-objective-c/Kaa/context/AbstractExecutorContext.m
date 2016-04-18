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


@import QuartzCore;
#import "AbstractExecutorContext.h"
#import "KaaLogging.h"

#define TAG @"AbstractExecutorContext >>>"

#define DEFAULT_TIMEOUT     5
#define DEFAULT_TIMEUNIT    TIME_UNIT_SECONDS

@implementation AbstractExecutorContext


- (instancetype)init {
    return [self initWithTimeOut:DEFAULT_TIMEOUT timeUnit:DEFAULT_TIMEUNIT];
}

- (instancetype)initWithTimeOut:(int64_t)timeOut timeUnit:(TimeUnit)timeUnit {
    self = [super init];
    if (self) {
        self.timeOut = timeOut;
        self.timeUnit = timeUnit;
    }
    return self;
}

- (void)shutDownExecutor:(NSOperationQueue *)queue {
    
    if (!queue) {
        DDLogWarn(@"%@ Can't shutdown empty executor", TAG);
        return;
    }
    
    DDLogDebug(@"%@ Shutdown executor service", TAG);
    [queue cancelAllOperations];
    DDLogDebug(@"%@ Waiting for executor service to shutdown for %lli %u", TAG, self.timeOut, self.timeUnit);
    @try {
        double fixedTime = CACurrentMediaTime();
        while ([queue operationCount] && (CACurrentMediaTime() < (fixedTime + self.timeOut))) {
            sleep(100);
        }
    }
    @catch (NSException *exception) {
        DDLogWarn(@"%@ Interrupted while waiting for executor to shutdown. Reason: %@", TAG, exception.reason);
    }
}

- (NSOperationQueue *)getLifeCycleExecutor {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (NSOperationQueue *)getApiExecutor {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (NSOperationQueue *)getCallbackExecutor {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (dispatch_queue_t)getSheduledExecutor {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (void)stop {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
}

- (void)initiate {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
}


@end
