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

#import "BucketRunner.h"
#import "BlockingQueue.h"
#import <QuartzCore/QuartzCore.h>
#import "KaaExceptions.h"
#import "KaaLogging.h"

#define TAG @"BucketRunner >>>"

typedef enum {
    RUNNER_STATE_WAITING,
    RUNNER_STATE_DONE
} LogFutureTaskState;


@interface ExecutionResult : NSObject

@property (nonatomic,readonly) id data;
@property (nonatomic,strong,readonly) NSException *exception;

- (instancetype)initWithData:(id)data exception:(NSException *)exception;

@end

@implementation ExecutionResult

- (instancetype)initWithData:(id)data exception:(NSException *)exception {
    self = [super init];
    if (self) {
        _data = data;
        _exception = exception;
    }
    return self;
}

@end


@interface BucketRunner ()

@property (nonatomic,strong) BlockingQueue *queue;
@property (nonatomic) volatile LogFutureTaskState state;

- (id)processResult:(ExecutionResult *)result;

@end

@implementation BucketRunner

- (instancetype)init {
    self = [super init];
    if (self) {
        self.queue = [[BlockingQueue alloc] init];
        self.state = RUNNER_STATE_WAITING;
    }
    return self;
}

- (BOOL)isRunnerDone {
    return self.state == RUNNER_STATE_DONE;
}

- (void)setValue:(id)value {
    @try {
        [self.queue offer:[[ExecutionResult alloc] initWithData:value exception:nil]];
    }
    @catch (NSException *ex) {
        DDLogError(@"%@ Failed to push value: %@, reason: %@", TAG, ex.name, ex.reason);
    }
    @finally {
        self.state = RUNNER_STATE_DONE;
    }
}

- (void)setFailure:(NSException *)failure {
    @try {
        [self.queue offer:[[ExecutionResult alloc] initWithData:nil exception:failure]];
    }
    @catch (NSException *ex) {
        DDLogError(@"%@ Failed to push value: %@, reason: %@", TAG, ex.name, ex.reason);
    }
    @finally {
        self.state = RUNNER_STATE_DONE;
    }

}

- (id)get {
    ExecutionResult *result = [self.queue take];
    return [self processResult:result];
}

- (id)getWithTimeout:(int64_t)timeout andTimeUnit:(TimeUnit)timeUnit {
    double timeoutMillis = [TimeUtils convert:timeout from:timeUnit to:TIME_UNIT_MILLISECONDS];
    double endCheck = CACurrentMediaTime() * 1000 + timeoutMillis;
    
    while (CACurrentMediaTime() * 1000 < endCheck) {
        if ([self.queue size] > 0) {
            return [self processResult:[self.queue take]];
        }
    }
    [NSException raise:KaaInterruptedException format:@"Timeout waiting to poll value"];
    return nil;
}

- (id)processResult:(ExecutionResult *)result {
    if ([result exception]) {
        [result.exception raise];
    }
    return result.data;
}

@end
