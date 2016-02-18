/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import "KAAMqttFrame.h"

#define CONNACK_REMAINING_LEGTH_V1 (2)

/**
 * CONNACK return code enum
 *  ACCEPTED                    0x01    Connection Accepted
 *  REFUSE_BAD_PROTOCOL         0x02    Connection Refused: unacceptable protocol version
 *  REFUSE_ID_REJECT            0x03    Connection Refused: identifier rejected
 *  REFUSE_SERVER_UNAVAILABLE   0x04    Connection Refused: server unavailable
 *  REFUSE_BAD_CREDENTIALS      0x05    Connection Refused: invalid authentication parameters
 *  REFUSE_NO_AUTH              0x06    Connection Refused: not authorized
 */

typedef enum {
    RETURN_CODE_ACCEPTED = 0x01,
    RETURN_CODE_REFUSE_BAD_PROTOCOL = 0x02,
    RETURN_CODE_REFUSE_ID_REJECT = 0x03,
    RETURN_CODE_REFUSE_SERVER_UNAVAILABLE = 0x04,
    RETURN_CODE_REFUSE_BAD_CREDENTIALS = 0x05,
    RETURN_CODE_REFUSE_NO_AUTH = 0x06,
    RETURN_CODE_UNDEFINED = 0x07
} ReturnCode;


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
