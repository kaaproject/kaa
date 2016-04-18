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
#import "KAATcpDelegates.h"
#import "KAAFramer.h"

/**
  MessageFactory Class. Used to transform byte stream to specific protocol messages.
 
  Typical use:
 
    MessageFactory *factory = [[MessageFactory alloc] init];
    [factory registerMessageDelegate:delegate];
    [[factory framer] pushBytes:bytes];
 
  Where delegate is instance of class which implements one of protocol message
  delegates and bytes - NSData object received from TCP/IP.
 */
@interface KAAMessageFactory : NSObject <MqttFrameDelegate>

@property (nonatomic, strong) KAAFramer *framer;

- (instancetype)initWithFramer:(KAAFramer *)framer;

- (void)registerConnAckDelegate:(id<ConnAckDelegate>)delegate;
- (void)registerConnectDelegate:(id<ConnectDelegate>)delegate;
- (void)registerDisconnectDelegate:(id<DisconnectDelegate>)delegate;
- (void)registerPingRequestDelegate:(id<PingRequestDelegate>)delegate;
- (void)registerPingResponseDelegate:(id<PingResponseDelegate>)delegate;
- (void)registerSyncRequestDelegate:(id<SyncRequestDelegate>)delegate;
- (void)registerSyncResponseDelegate:(id<SyncResponseDelegate>)delegate;

@end
