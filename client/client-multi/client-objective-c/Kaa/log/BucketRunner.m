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

#import "BucketRunner.h"
#import "BlockingQueue.h"
#import <QuartzCore/QuartzCore.h>
#import "KaaExceptions.h"
#import "KaaLogging.h"

#define TAG @"BucketRunner >>>"

typedef enum {
    BUCKET_RUNNER_STATE_WAITING,
    BUCKET_RUNNER_STATE_DONE
} BucketRunnberTaskState;


@interface BucketRunner ()

@property (nonatomic, strong) BlockingQueue *queue;
@property (nonatomic) volatile BucketRunnberTaskState state;
@property (nonatomic) double executionStartTimestamp;

@end

@implementation BucketRunner

static int32_t gBucketIdCounter = 0;

- (instancetype)init {
    self = [super init];
    if (self) {
        _queue = [[BlockingQueue alloc] init];
        _state = BUCKET_RUNNER_STATE_WAITING;
        _runnerId = gBucketIdCounter++;
        _executionStartTimestamp = CACurrentMediaTime() * 1000;
    }
    return self;
}

- (BOOL)isRunnerDone {
    return self.state == BUCKET_RUNNER_STATE_DONE;
}

- (void)setValue:(BucketInfo *)value {
    @try {
        value.scheduledBucketTimestamp = self.executionStartTimestamp;
        value.bucketDeliveryDuration = CACurrentMediaTime() * 1000 - self.executionStartTimestamp;
        [self.queue offer:value];
    }
    @catch (NSException *ex) {
        DDLogError(@"%@ Failed to push value: %@, reason: %@", TAG, ex.name, ex.reason);
    }
    @finally {
        self.state = BUCKET_RUNNER_STATE_DONE;
    }
}

- (BucketInfo *)getValue {
    if ([NSThread isMainThread]) {
        [NSException raise:KaaRuntimeException format:@"Method should not be called in main thread!"];
    }
    return [self.queue take];
}

- (BucketInfo *)getValueWithTimeout:(int64_t)timeout timeUnit:(TimeUnit)timeUnit {
    if ([NSThread isMainThread]) {
        [NSException raise:KaaRuntimeException format:@"Method should not be called in main thread!"];
    }
    double timeoutMillis = [TimeUtils convertValue:timeout fromTimeUnit:timeUnit toTimeUnit:TIME_UNIT_MILLISECONDS];
    double endCheck = CACurrentMediaTime() * 1000 + timeoutMillis;
    
    while (CACurrentMediaTime() * 1000 < endCheck) {
        if ([self.queue size] > 0) {
            return [self.queue take];
        }
    }
    [NSException raise:KaaInterruptedException format:@"Timeout waiting to poll value"];
    return nil;
}

- (BOOL)isEqual:(id)object {
    if (!object) {
        return NO;
    }
    
    if ([object isKindOfClass:[BucketRunner class]]) {
        BucketRunner *other = (BucketRunner *)object;
        if (self.runnerId == other.runnerId) {
            return YES;
        }
    }
    
    return NO;
}

- (NSUInteger)hash {
    const int prime = 31;
    NSUInteger result = 1;
    result = prime * result + self.runnerId;
    return result;
}

@end
