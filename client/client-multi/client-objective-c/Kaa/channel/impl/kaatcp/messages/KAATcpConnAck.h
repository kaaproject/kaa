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

#define CONNACK_REMAINING_LEGTH_V1 (2)

/**
 *  ReturnCodeAccepted                  0x01    Connection Accepted
 *  ReturnCodeRefuseBadProtocol         0x02    Connection Refused: unacceptable protocol version
 *  ReturnCodeRefuseIdReject            0x03    Connection Refused: identifier rejected
 *  ReturnCodeRefuseServerUnavailable   0x04    Connection Refused: server unavailable
 *  ReturnCodeRefuseBadCredentials      0x05    Connection Refused: invalid authentication parameters
 *  ReturnCodeRefuseNoAuth              0x06    Connection Refused: not authorized
 *  ReturnCodeRefuseVerificationFailed  0x10    Connection Refused: endpoint verification failed
 */

typedef NS_ENUM(int, ReturnCode) {
    ReturnCodeAccepted = 0x01,
    ReturnCodeRefuseBadProtocol = 0x02,
    ReturnCodeRefuseIdReject = 0x03,
    ReturnCodeRefuseServerUnavailable = 0x04,
    ReturnCodeRefuseBadCredentials = 0x05,
    ReturnCodeRefuseNoAuth = 0x06,
    ReturnCodeRefuseVerificationFailed = 0x10,
    ReturnCodeUndefined = 0x07
};


/**
 * ConnAck message Class.
 * The CONNACK message is a message sent by the server in response to a CONNECT request from a client.<br>
 * Variable header<br>
 * byte 1  reserved (0)<br>
 * byte 2 Return Code 
 * @see enum ReturnCode
 **/
@interface KAATcpConnAck : KAAMqttFrame

@property (nonatomic) ReturnCode returnCode;

- (instancetype)initWithReturnCode:(ReturnCode)code;

@end
