/*
 * Copyright 2014-2015 CyberVision, Inc.
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
 * Connect message Class.
 * When a TCP/IP socket connection is established from a client to a server,
 * a protocol level session must be created using a CONNECT flow.
 * Variable header
 * Protocol Name
 *    byte 1  Length MSB (0)
 *    byte 2  Length LSB (6)
 *    byte 3  K
 *    byte 4  a
 *    byte 5  a
 *    byte 6  t
 *    byte 7  c
 *    byte 8  p
 * Protocol version
 *    byte 9  Version (1)
 * Connect Flags
 *    byte 10
 *            User name flag (0)
 *            Password flag (0)
 *            Will RETAIN (0)
 *            Will QoS (00)
 *            Will flag (0)
 *            Clean Session (1)
 *                0x02 - value
 * Keep Alive timer
 *    byte 11 Keep alive MSB (0)
 *    byte 12 Keep alive LSB (200)
 *            Keep Alive timer - default value 200 seconds.
 *
 * Payload:
 *  Session Key:   AES Session encoding key (16 byte) - encrypted with the Operations server RSA Public Key
 *  EndpointPublicKeyHash: SHA Hash of Endpoint Public Key (32 byte)
 *  Signature: RSA signature (32 byte) signed with the Endpoint Private Key of Session key (16 byte) + EndpointPublicKeyHash (32 byte)
 **/

#define CONNECT_VARIABLE_HEADER_LENGTH_V1   18
#define CONNECT_AES_SESSION_KEY_LENGTH      256
#define CONNECT_SIGNATURE_LENGTH            256
#define CONNECT_VERSION                     0x01
#define CONNECT_FIXED_HEADER_FLAG           0x02
#define CONNECT_SESSION_KEY_FLAGS           0x11
#define CONNECT_SIGNATURE_FLAGS             0x01

@interface KAATcpConnect : KAAMqttFrame

@property (nonatomic) uint16_t keepAlive;                //kaatcp keep alive interval, default 200 seconds

@property (nonatomic) uint32_t nextProtocolId;

@property (nonatomic, strong) NSData *aesSessionKey;
@property (nonatomic, strong) NSData *signature;     //Signature of aesSessionKey and endpointPublicKeyHash
@property (nonatomic, strong) NSData *syncRequest;   //SyncRequest in Connect message

@property (nonatomic, readonly) BOOL hasSignature;
@property (nonatomic, readonly) BOOL isEncrypted;

- (instancetype)initWithAlivePeriod:(uint16_t)keepAlive
                     nextProtocolId:(uint32_t)protocolId
                      aesSessionKey:(NSData *)key
                        syncRequest:(NSData *)request
                          signature:(NSData *)signature;

@end
