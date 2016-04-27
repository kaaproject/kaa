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

#import "KAAMqttFrame.h"

/**
 *  DisconnectReasonNone                    0x00    No error
 *  DisconnectReasonBadRequest              0x01    Client sent a corrupted data
 *  DisconnectReasonInternalError           0x02    Internal error has been occurred
 *  DisconnectReasonCredentialsRevoked      0x03    Credentials revoked
 */
typedef NS_ENUM(int, DisconnectReason) {
    DisconnectReasonNone = 0x00,
    DisconnectReasonBadRequest = 0x01,
    DisconnectReasonInternalError = 0x02,
    DisconnectReasonCredentialsRevoked = 0x03
};

#define DISCONNECT_REMAINING_LEGTH_V1 2

/**
 * Disconnect message class.<br>
 * The DISCONNECT message is sent from the client to the server to indicate that it is about to close
 * its TCP/IP connection. This provides a clean disconnection, rather than just dropping the line.<br>
 * If the client had connected with the clean session flag set,
 * then all previously maintained information about the client will be discarded.<br>
 * A server should not rely on the client to close the TCP/IP connection after receiving a DISCONNECT.
 */
@interface KAATcpDisconnect : KAAMqttFrame

@property (nonatomic) DisconnectReason reason;

- (instancetype)initWithDisconnectReason:(DisconnectReason)reason;

@end
