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

#ifndef Kaa_KaaChannelManager_h
#define Kaa_KaaChannelManager_h

#import "TransportCommon.h"
#import "KaaDataChannel.h"
#import "FailoverManager.h"

/**
 * Channel manager establishes/removes channels' links between client and server.
 *
 * Use this manager to add or remove specific network channel implementation for 
 * client-server communication.
 */
@protocol KaaChannelManager

/**
 * Updates the manager by setting the channel to the specified transport type.
 */
- (void)setChannel:(id<KaaDataChannel>)channel withType:(TransportType)type;

/**
 * Updates the manager by adding the channel.
 */
- (void)addChannel:(id<KaaDataChannel>)channel;

/**
 * Updates the manager by removing the channel from the manager.
 */
- (void)removeChannel:(id<KaaDataChannel>)channel;

/**
 * Removes channel by the unique channel id.
 */
- (void)removeChannelById:(NSString *)channelId;

/**
 * Retrieves the list of current channels.
 */
- (NSArray *)getChannels;

/**
 * Retrieves channel by the unique channel id.
 */
- (id<KaaDataChannel>)getChannelById:(NSString *)channelId;

/**
 * Reports to Channel Manager in case link with server was not established.
 * 
 * @param server the parameters of server that was not connected.
 * @param status failover status that reports connection issue.
 *
 * @see TransportConnectionInfo
 * @see FailoverStatus
 */
- (void)onServerFailedWithConnectionInfo:(id<TransportConnectionInfo>)server failoverStatus:(FailoverStatus)status;

/**
 * Clears the list of channels.
 */
- (void)clearChannelList;

/**
 * Invoke sync on active channel by specified transport type.
 */
- (void)syncForTransportType:(TransportType)type;

/**
 * Invoke sync acknowledgement on active channel by specified transport type;
 */
- (void)syncAckForTransportType:(TransportType)type;

/**
 * Invoke sync acknowledgement on active channel;
 *
 * @param type transport type that is used to identify active channel.
 */
- (void)syncAll:(TransportType)type;

/**
 * Returns information about server that is used for data transfer for specified TransportType.
 */
- (id<TransportConnectionInfo>)getActiveServerForType:(TransportType)type;

/**
 * Sets a new failover manager
 */
- (void)setFailoverManager:(id<FailoverManager>)failoverManager;

@end

#endif
