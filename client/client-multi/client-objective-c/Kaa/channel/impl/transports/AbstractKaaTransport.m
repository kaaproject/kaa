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

#import "AbstractKaaTransport.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"AbstractKaaTransport >>>"

@implementation AbstractKaaTransport

- (void)setChannelManager:(id<KaaChannelManager>)channelManager {
    _channelManager = channelManager;
}

- (void)setClientState:(id<KaaClientState>)state {
    _clientState = state;
}

- (void)syncByType:(TransportType)type {
    [self syncByType:type ack:NO];
}

- (void)syncAckByType:(TransportType)type {
    [self syncByType:type ack:YES];
}

- (void)syncByType:(TransportType)type ack:(BOOL)ack {
    [self syncByType:type ack:ack all:NO];
}

- (void)syncAll:(TransportType)type {
    [self syncByType:type ack:NO all:YES];
}

- (void)syncByType:(TransportType)type ack:(BOOL)ack all:(BOOL)all {
    if (!self.channelManager) {
        DDLogError(@"%@ Channel manager is not set during sync for type %i", TAG, type);
        [NSException raise:KaaChannelRuntimeException format:@"Failed to find channel for transport %i", type];
    }
    
    if (ack) {
        [self.channelManager syncAckForTransportType:type];
    } else if (all) {
        [self.channelManager syncAll:type];
    } else {
        [self.channelManager syncForTransportType:type];
    }
}

- (void)sync {
    [self syncByType:[self getTransportType]];
}

- (void)syncAck {
    [self syncAckByType:[self getTransportType]];
}

- (void)syncAck:(SyncResponseStatus)status {
    if (status != SYNC_RESPONSE_STATUS_NO_DELTA) {
        DDLogInfo(@"%@ Sending ack due to response status: %i", TAG, status);
        [self syncAck];
    }
}

- (TransportType)getTransportType {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class!"];
    return -1;
}

@end
