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

/**
 * Enum which describes status of the current failover state.
 * Managed by a failover strategy.
 */
typedef NS_ENUM(int, FailoverStatus) {
    FailoverStatusEndpointVerificationFailed,
    FailoverStatusEndpointCredentialsRevoked,
    FailoverStatusBootstrapServersNotAvailable,
    FailoverStatusCurrentBootstrapServerNotAvailable,
    FailoverStatusOperationsServersNotAvailable,
    FailoverStatusNoOperationsServersReceived,
    FailoverStatusNoConnectivity
};

/**
 * Enum which represents an action corresponding to a failover scenario.
 */
typedef NS_ENUM(int, FailoverAction) {
    FailoverActionNoop,               // doing nothing
    FailoverActionRetry,
    FailoverActionUseNextBootstrap,
    FailoverActionUseNextOperations,
    FailoverActionFailure
};

/**
 * Class that describes a decision which is made by a failover manager, 
 * which corresponds to a failover strategy.
 */
@interface FailoverDecision : NSObject

@property(nonatomic, readonly) FailoverAction failoverAction;

/**
 * Retry period in milliseconds.
 */
@property(nonatomic, readonly) int64_t retryPeriod;

- (instancetype)initWithFailoverAction:(FailoverAction)failoverAction;
- (instancetype)initWithFailoverAction:(FailoverAction)failoverAction
                           retryPeriod:(int64_t)retryPeriod
                              timeUnit:(TimeUnit)timeUnit;

@end