/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import <Foundation/Foundation.h>

/**
 * Enum which describes status of the current failover state.
 * Managed by a failover strategy.
 */
typedef enum {
    FAILOVER_STATUS_BOOTSTRAP_SERVERS_NA,
    FAILOVER_STATUS_CURRENT_BOOTSTRAP_SERVER_NA,
    FAILOVER_STATUS_OPERATION_SERVERS_NA,
    FAILOVER_STATUS_NO_OPERATION_SERVERS_RECEIVED,
    FAILOVER_STATUS_NO_CONNECTIVITY
} FailoverStatus;

/**
 * Enum which represents an action corresponding to a failover scenario.
 */
typedef enum  {
    FAILOVER_ACTION_NOOP,               // doing nothing
    FAILOVER_ACTION_RETRY,
    FAILOVER_ACTION_USE_NEXT_BOOTSTRAP,
    FAILOVER_ACTION_USE_NEXT_OPERATIONS,
    FAILOVER_ACTION_STOP_APP
} FailoverAction;

/**
 * Class that describes a decision which is made by a failover manager, 
 * which corresponds to a failover strategy.
 */
@interface FailoverDecision : NSObject

@property(nonatomic, readonly) FailoverAction failoverAction;
@property(nonatomic, readonly) int64_t retryPeriod;

- (instancetype)initWithFailoverAction:(FailoverAction)failoverAction;
- (instancetype)initWithFailoverAction:(FailoverAction)failoverAction retryPeriodInMilliseconds:(int64_t)retryPeriod;

@end