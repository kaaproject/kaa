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

#ifndef KAATcpDelegates_h
#define KAATcpDelegates_h

#import "KAATcpConnAck.h"
#import "KAATcpConnect.h"
#import "KAATcpDisconnect.h"
#import "KAATcpKaaSync.h"
#import "KAATcpSyncRequest.h"
#import "KAATcpSyncResponse.h"
#import "KAATcpPingRequest.h"
#import "KAATcpPingResponse.h"

@protocol MqttFrameDelegate

- (void)onMqttFrame:(KAAMqttFrame *)frame;

@end

@protocol ConnAckDelegate

- (void)onConnAckMessage:(KAATcpConnAck *)message;

@end

@protocol ConnectDelegate

- (void)onConnectMessage:(KAATcpConnect *)message;

@end

@protocol DisconnectDelegate

- (void)onDisconnectMessage:(KAATcpDisconnect *)message;

@end

@protocol KaaSyncDelegate

- (void)onKaaSyncMessage:(KAATcpKaaSync *)message;

@end

@protocol SyncRequestDelegate

- (void)onSyncRequestMessage:(KAATcpSyncRequest *)message;

@end

@protocol SyncResponseDelegate

- (void)onSyncResponseMessage:(KAATcpSyncResponse *)message;

@end

@protocol PingRequestDelegate

- (void)onPingRequestMessage:(KAATcpPingRequest *)message;

@end

@protocol PingResponseDelegate

- (void)onPingResponseMessage:(KAATcpPingResponse *)message;

@end

#endif /* KAATcpDelegates_h */
