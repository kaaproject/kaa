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

#ifndef FailoverStrategy_h
#define FailoverStrategy_h

#import <Foundation/Foundation.h>
#import "TransportConnectionInfo.h"
#import "FailoverDecision.h"
#import "TimeCommons.h"

/**
 * Failover strategy is responsible for producing failover decisions based on failover statuses.
 */
@protocol FailoverStrategy

@property (nonatomic, readonly) int64_t bootstrapServersRetryPeriod;
@property (nonatomic, readonly) int64_t operationsServersRetryPeriod;
@property (nonatomic, readonly) TimeUnit timeUnit;

/**
 * Needs to be invoked once client recovered after failover.
 *
 * @param connectionInfo server information
 *
 * @see TransportConnectionInfo
 */
- (void)onRecoverWithConnectionInfo:(id<TransportConnectionInfo>) connectionInfo;

/**
 * Needs to be invoked to determine a decision that resolves the failover.
 *
 * @param failoverStatus current status of the failover.
 *
 * @return decision which is meant to resolve the failover.
 *
 * @see FailoverDecision
 * @see FailoverStatus
 */
- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status;

@end

#endif /* FailoverStrategy_h */
