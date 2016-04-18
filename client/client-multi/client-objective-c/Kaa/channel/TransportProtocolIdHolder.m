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

#import "TransportProtocolIdHolder.h"

#define HTTP_TRANSPORT_PROTOCOL_ID          (0xfb9a3cf0)
#define HTTP_TRANSPORT_PROTOCOL_VERSION     (1)

#define TCP_TRANSPORT_PROTOCOL_ID           (0x56c8ff92)
#define TCP_TRANSPORT_PROTOCOL_VERSION      (1)

@implementation TransportProtocolIdHolder

+ (TransportProtocolId *)HTTPTransportID {
    return [[TransportProtocolId alloc] initWithId:HTTP_TRANSPORT_PROTOCOL_ID version:HTTP_TRANSPORT_PROTOCOL_VERSION];
}

+ (TransportProtocolId *)TCPTransportID {
    return [[TransportProtocolId alloc] initWithId:TCP_TRANSPORT_PROTOCOL_ID version:TCP_TRANSPORT_PROTOCOL_VERSION];
}

@end
