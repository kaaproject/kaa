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

#ifndef Kaa_BootstrapManager_h
#define Kaa_BootstrapManager_h

#import <Foundation/Foundation.h>
#import "TransportProtocolId.h"
#import "BootstrapTransport.h"
#import "FailoverManager.h"
#import "KaaInternalChannelManager.h"

@protocol BootstrapTransport;

/**
 * Bootstrap manager manages the list of available operation servers.
 */
@protocol BootstrapManager

/**
 * Receives the latest list of servers from the bootstrap server.
 */
- (void)receiveOperationsServerList;

/**
 * Force switch to the next operations server that support given <TransportProtocolId>
 *
 * @param transportId id of the transport protocol.
 * @param status failovers status that causes using of the next operation server.
 *
 * @see FailoverStatus
 * @see TransportProtocolId
 */
- (void)useNextOperationsServerWithTransportId:(TransportProtocolId *)transportId failoverStatus:(FailoverStatus)status;

/**
 * Update the Channel Manager with endpoint's properties retrieved by its DNS.
 *
 * @param accessPointId endpoint's DNS.
 */
- (void)useNextOperationsServerByAccessPointId:(int32_t)accessPointId;

/**
 * Sets bootstrap transport object.
 *
 * @param transport object to be set.
 * @see BootstrapTransport
 */
- (void)setTransport:(id<BootstrapTransport>)transport;

/**
 * Sets Channel manager.
 *
 * @param manager the channel manager to be set.
 * @see KaaInternalChannelManager
 */
- (void)setChannelManager:(id<KaaInternalChannelManager>)manager;

/**
 * Sets Failover manager.
 *
 * @param manager the failover manager to be set
 * @see FailoverManager
 */
- (void)setFailoverManager:(id<FailoverManager>)manager;

/**
 * Updates the operation server list.
 *
 * @param list the operation server list. <ProtocolMetaData>
 * @see ProtocolMetaData
 */
- (void)onProtocolListUpdated:(NSArray *)list;

@end

#endif
