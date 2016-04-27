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

#import "KAAFramer.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"Framer >>>"

@interface KAAFramer ()

@property (nonatomic, strong) NSMutableArray *delegates;
@property (nonatomic, strong) KAAMqttFrame *currentFrame;

- (void)notifyDelegatesWithFrame:(KAAMqttFrame *)frame;

/**
 * Creates specific Kaatcp message by MessageType
 * @param type - KaaMessageType of mqttFrame
 * @return mqttFrame
 * @throws KaaTcpProtocolException if specified type is unsupported
 */
- (KAAMqttFrame *)getFrameByType:(char)type;

@end

@implementation KAAFramer

- (instancetype)init {
    self = [super init];
    if (self) {
        self.delegates = [NSMutableArray array];
    }
    return self;
}

- (void)registerFrameDelegate:(id<MqttFrameDelegate>)delegate {
    [self.delegates addObject:delegate];
}

- (int32_t)pushBytes:(NSMutableData *)data {
    if (!data) {
        [NSException raise:KaaTcpProtocolException format:@"%@ Received nil data", TAG];
        return -1;
    }
    
    int32_t used = 0;
    char *mutableData = [data mutableBytes];
    while (data.length > used) {
        if (!self.currentFrame) {
            if ((data.length - used) >= 1) { // 1 bytes minimum header length
                int32_t intType = mutableData[used] & 0xFF;
                self.currentFrame = [self getFrameByType:(char) (intType >> 4)];
                ++used;
            } else {
                break;
            }
        }
        used += [self.currentFrame pushBytes:data toPosition:used];
        if (self.currentFrame.frameDecodeComplete) {
            [self notifyDelegatesWithFrame:[self.currentFrame upgradeFrame]];
            self.currentFrame = nil;
        }
    }
    return used;
}

- (void)notifyDelegatesWithFrame:(KAAMqttFrame *)frame {
    for (id<MqttFrameDelegate> delegate in self.delegates) {
        [delegate onMqttFrame:frame];
    }
}

- (KAAMqttFrame *)getFrameByType:(char)type {
    KAAMqttFrame *frame = nil;
    switch (type) {
        case TCP_MESSAGE_TYPE_CONNACK:
            frame = [[KAATcpConnAck alloc] init];
            break;
        case TCP_MESSAGE_TYPE_CONNECT:
            frame = [[KAATcpConnect alloc] init];
            break;
        case TCP_MESSAGE_TYPE_DISCONNECT:
            frame = [[KAATcpDisconnect alloc] init];
            break;
        case TCP_MESSAGE_TYPE_KAASYNC:
            frame = [[KAATcpKaaSync alloc] init];
            break;
        case TCP_MESSAGE_TYPE_PINGREQ:
            frame = [[KAATcpPingRequest alloc] init];
            break;
        case TCP_MESSAGE_TYPE_PINGRESP:
            frame = [[KAATcpPingResponse alloc] init];
            break;
        default:
            [NSException raise:KaaTcpProtocolException format:@"Got incorrect messageType format: %i", type];
            break;
    }
    return frame;
}

- (void)flush {
    self.currentFrame = nil;
    DDLogVerbose(@"%@ Invoked flush", TAG);
}

@end
