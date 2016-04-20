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

#import "FailoverDecision.h"

@implementation FailoverDecision

- (instancetype)initWithFailoverAction:(FailoverAction)failoverAction {
    self = [super init];
    if (self) {
        _failoverAction = failoverAction;
    }
    return self;
}

- (instancetype)initWithFailoverAction:(FailoverAction)failoverAction
                           retryPeriod:(int64_t)retryPeriod
                              timeUnit:(TimeUnit)timeUnit {
    self = [super init];
    if (self) {
        _failoverAction = failoverAction;
        _retryPeriod = [TimeUtils convertValue:retryPeriod fromTimeUnit:timeUnit toTimeUnit:TIME_UNIT_MILLISECONDS];
    }
    return self;
}

@end
