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
#import "KaaTransport.h"
#import "KaaChannelManager.h"
#import "KaaClientState.h"
#import "EndpointGen.h"

@interface AbstractKaaTransport : NSObject <KaaTransport>

@property (nonatomic, strong) id<KaaChannelManager> channelManager;
@property (nonatomic, strong) id<KaaClientState> clientState;

- (void)syncByType:(TransportType)type;
- (void)syncAckByType:(TransportType)type;
- (void)syncByType:(TransportType)type ack:(BOOL)ack;
- (void)syncAll:(TransportType)type;
- (void)syncByType:(TransportType)type ack:(BOOL)ack all:(BOOL)all;
- (void)syncAck;
- (void)syncAck:(SyncResponseStatus)status;
- (TransportType)getTransportType;

@end
