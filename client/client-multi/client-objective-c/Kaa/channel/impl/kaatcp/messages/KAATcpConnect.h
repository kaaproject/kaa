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


#define CONNECT_VARIABLE_HEADER_LENGTH_V1   18
#define CONNECT_AES_SESSION_KEY_LENGTH      256
#define CONNECT_SIGNATURE_LENGTH            256
#define CONNECT_VERSION                     0x01
#define CONNECT_FIXED_HEADER_FLAG           0x02
#define CONNECT_SESSION_KEY_FLAGS           0x11
#define CONNECT_SIGNATURE_FLAGS             0x01

/**
 * Connect message class.<br>
 * When a TCP/IP socket connection is established from a client to a server,
 * a protocol level session must be created using a CONNECT flow.<br>
 * Variable header<br>
 * Protocol Name<br>
 *    byte 1  Length MSB (0)<br>
 *    byte 2  Length LSB (6)<br>
 *    byte 3  K<br>
 *    byte 4  a<br>
 *    byte 5  a<br>
 *    byte 6  t<br>
 *    byte 7  c<br>
 *    byte 8  p<br>
 * Protocol version<br>
 *    byte 9  Version (1)<br>
 * Connect Flags<br>
 *    byte 10<br>
 *            User name flag (0)<br>
 *            Password flag (0)<br>
 *            Will RETAIN (0)<br>
 *            Will QoS (00)<br>
 *            Will flag (0)<br>
 *            Clean Session (1)<br>
 *                0x02 - value<br>
 * Keep Alive timer<br>
 *    byte 11 Keep alive MSB (0)<br>
 *    byte 12 Keep alive LSB (200)<br>
 *            Keep Alive timer - default value 200 seconds.<br>
 *
 * Payload:<br>
 *  Session Key:   AES Session encoding key (16 byte) - encrypted with the Operations server RSA Public Key<br>
 *  EndpointPublicKeyHash: SHA Hash of Endpoint Public Key (32 byte)<br>
 *  Signature: RSA signature (32 byte) signed with the Endpoint Private Key of Session key (16 byte) + EndpointPublicKeyHash (32 byte)<br>
 **/
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
