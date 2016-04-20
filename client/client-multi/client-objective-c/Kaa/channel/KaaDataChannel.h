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

#ifndef Kaa_KaaDataChannel_h
#define Kaa_KaaDataChannel_h

#import "TransportCommon.h"
#import "TransportProtocolId.h"
#import "KaaDataDemultiplexer.h"
#import "KaaDataMultiplexer.h"
#import "TransportConnectionInfo.h"
#import "ConnectivityChecker.h"

/**
 * Channel is responsible for sending/receiving data to/from the endpoint
 * server.
 */
@protocol KaaDataChannel

/**
 * Updates the channel's state of the specific service.
 */
- (void)syncForTransportType:(TransportType)type;

- (void)syncTransportTypes:(NSSet *)types;

/**
 * Updates the channel's state of all supported services.
 */
- (void)syncAll;

/**
 * Notifies channel about successful acknowledgment of the sync only in case if smth change.
 */
- (void)syncAckForTransportType:(TransportType)type;

- (void)syncAckForTransportTypes:(NSSet *)types;

/**
 * Retrieves the channel's id. It should be unique in existing channels scope.
 */
- (NSString *)getId;

- (TransportProtocolId *)getTransportProtocolId;

- (ServerType)getServerType;

/**
 * Sets the response demultiplexer for this channel.
 */
- (void)setDemultiplexer:(id<KaaDataDemultiplexer>)demultiplexer;

/**
 * Sets the request multiplexer for this channel.
 */
- (void)setMultiplexer:(id<KaaDataMultiplexer>)multiplexer;

- (void)setServer:(id<TransportConnectionInfo>)server;

- (id<TransportConnectionInfo>)getServer;

- (void)setConnectivityChecker:(ConnectivityChecker *)checker;

/**
 * Retrieves dictionary of transport types and their directions supported by this channel.
 * @return Dictionary with <TransportType, ChannelDirection> as key-value wrapped in NSNumber
 */
- (NSDictionary *)getSupportedTransportTypes;

/**
 * Shuts down the channel instance. All connections and threads should be terminated.
 * The instance can no longer be used.
 */
- (void)shutdown;

/**
 * Pauses the channel's workflow. The channel should stop all network activity.
 */
- (void)pause;

/**
 * Resumes the channel's workflow. The channel should restore previous connection.
 */
- (void)resume;

@end

#endif
