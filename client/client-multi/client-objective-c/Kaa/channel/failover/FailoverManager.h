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

#ifndef Kaa_FailoverManager_h
#define Kaa_FailoverManager_h

#import <Foundation/Foundation.h>
#import "TransportConnectionInfo.h"
#import "FailoverDecision.h"
#import "FailoverStrategy.h"

/**
 * Manager responsible for managing current server's failover/connection events
 */
@protocol FailoverManager

/**
 * Needs to be invoked when a server fail occurs.
 */
- (void)onServerFailedWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo failoverStatus:(FailoverStatus)status;

/**
 * Needs to be invoked as soon as current server is changed.
 */
- (void)onServerChangedWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo;

/**
 * Needs to be invoked as soon as connection to the current server is established.
 */
- (void)onServerConnectedWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo;

/**
 * Needs to be invoked to determine a decision that resolves the failover.
 *
 * @param failoverStatus Current status of the failover.
 *
 * @return Decision which is meant to resolve the failover.
 */
- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status;

/**
 *
 * @param failoverStrategy strategy that will be used to resolve failovers.
 *
 * @see FailoverStrategy
 */
- (void)setFailoverStrategy:(id<FailoverStrategy>)failoverStrategy;

@end

#endif
