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
 * KAASYNC subcomand id table
 * Mnemonic  Enumeration Description
 * UNUSED    0           reserved value
 * SYNC      1           Sync request/response
 * BOOTSTRAP 2           Bootstrap resolve/response
 */
typedef enum {
    KAA_SYNC_MESSAGE_TYPE_UNUSED = 0,
    KAA_SYNC_MESSAGE_TYPE_SYNC = 1
} KaaSyncMessageType;

/**
 * KaaSync message Class.
 * The KAASYNC message is used as intermediate class for decoding messages
 * SyncRequest,SyncResponse,BootstraResolve,BootstrapResponse.
 *
 *
 *
 * Variable header
 * Protocol Name
 *     byte 1  Length MSB (0)
 *     byte 2  Length LSB (6)
 *     byte 3  K
 *     byte 4  a
 *     byte 5  a
 *     byte 6  t
 *     byte 7  c
 *     byte 8  p
 * Protocol version
 *     byte 9  Version (1)
 * Message ID (2 bytes)
 *     byte 10 ID MSB
 *     byte 11 ID LSB
 * Flags
 *     byte 12
 *         Request/Response (bit 0)
 *         1 - request, 0 - response
 *
 *         Zipped (bit 1)
 *         1 - zepped, 0 - unzipped
 *
 *         Encrypted(bit 2)
 *         1 - encrypted, 0 - unencrypted
 *
 *         Unused(bit 3)
 *
 *         bit4-bit7 - KAASYNC subcomand messageIdid
 *
 *
 * KAASYNC subcomand id table
 * Mnemonic   Enumeration   Description
 * UNUSED     0             reserved value
 * SYNC       1             Sync request/response
 * BOOTSTRAP  2             Bootstrap resolve/response
 */

#define KAASYNC_VARIABLE_HEADER_LENGTH_V1 12

#define KAASYNC_REQUEST_FLAG    0x01
#define KAASYNC_ZIPPED_FLAG     0x02
#define KAASYNC_ENCRYPTED_FLAG  0x04
#define KAASYNC_VERSION         0x01

@interface KAATcpKaaSync : KAAMqttFrame

@property (nonatomic) uint16_t messageId;                            //message id if used, default 0
@property (nonatomic) BOOL request;                             //Request/Response (bit 0) 1 - request, 0 - response
@property (nonatomic) BOOL zipped;                              //Zipped (bit 1) 1 - zipped, 0 - unzipped
@property (nonatomic) BOOL encrypted;                           //Encrypted(bit 2) 1 - encrypted, 0 - not encrypted
@property (nonatomic) KaaSyncMessageType kaaSyncMessageType;    //KaaSync subcommand message type

/**
 * Default constructor.
 * @param isRequest boolean 'true' is request, else response
 * @param isZipped boolean if message is Zipped
 * @param isEcrypted boolean if message is Encrypted
 */
- (instancetype)initRequest:(BOOL)isRequest zipped:(BOOL)isZipped encypted:(BOOL)isEncrypted;

- (instancetype)initWithOldKaaSync:(KAATcpKaaSync *)old;

- (void)packVariableHeader;

- (void)decodeVariableHeaderFromInput:(NSInputStream *)input;

@end
