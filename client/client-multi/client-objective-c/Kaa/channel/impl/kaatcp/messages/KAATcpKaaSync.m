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

#import "KAATcpKaaSync.h"
#import "KAATcpSyncResponse.h"
#import "KAATcpSyncRequest.h"
#import "KaaExceptions.h"

#define KAASYNC_MESSAGE_TYPE_SHIFT 4

static const char FIXED_HEADER_CONST[] = {0x00,0x06,'K','a','a','t','c','p',KAASYNC_VERSION};

@implementation KAATcpKaaSync

- (instancetype)init {
    self = [super init];
    if (self) {
        [self setKaaSyncMessageType:KAA_SYNC_MESSAGE_TYPE_UNUSED];
        [self setMessageType:TCP_MESSAGE_TYPE_KAASYNC];
    }
    return self;
}

- (instancetype)initRequest:(BOOL)isRequest zipped:(BOOL)isZipped encypted:(BOOL)isEncrypted {
    self = [self init];
    if (self) {
        self.request = isRequest;
        self.zipped = isZipped;
        self.encrypted = isEncrypted;
        self.remainingLength = KAASYNC_VARIABLE_HEADER_LENGTH_V1;
    }
    return self;
}

- (instancetype)initWithOldKaaSync:(KAATcpKaaSync *)old {
    self = [super initWithOld:old];
    if (self) {
        [self setMessageType:TCP_MESSAGE_TYPE_KAASYNC];
        self.messageId = old.messageId;
        self.request = old.request;
        self.zipped = old.zipped;
        self.encrypted = old.encrypted;
        self.kaaSyncMessageType = old.kaaSyncMessageType;
    }
    return self;
}

- (void)packVariableHeader {
    [self.buffer appendBytes:FIXED_HEADER_CONST length:sizeof(FIXED_HEADER_CONST)];
    self.bufferPosition += sizeof(FIXED_HEADER_CONST);

    uint16_t messageId = htons(self.messageId);
    [self.buffer appendBytes:&messageId length:sizeof(messageId)];
    self.bufferPosition += sizeof(messageId);
    
    char flags = 0x00;
    if (self.request) {
        flags = flags | KAASYNC_REQUEST_FLAG;
    }
    if (self.zipped) {
        flags = flags | KAASYNC_ZIPPED_FLAG;
    }
    if (self.encrypted) {
        flags = flags | KAASYNC_ENCRYPTED_FLAG;
    }
    flags = flags | (self.kaaSyncMessageType << KAASYNC_MESSAGE_TYPE_SHIFT);
    [self.buffer appendBytes:&flags length:sizeof(flags)];
    self.bufferPosition++;
}

- (void)decodeVariableHeaderFromInput:(NSInputStream *)input {
    uint8_t header[sizeof(FIXED_HEADER_CONST)];
    [input read:header maxLength:sizeof(header)];
    self.bufferPosition += sizeof(FIXED_HEADER_CONST);
    for (int i = 0; i < sizeof(FIXED_HEADER_CONST); i++) {
        if (header[i] != FIXED_HEADER_CONST[i]) {
            [NSException raise:KaaTcpProtocolException format:@"Kaatcp protocol version missmatch"];
        }
    }
    
    uint8_t messageId[2];
    [input read:messageId maxLength:sizeof(messageId)];
    self.bufferPosition += sizeof(messageId);
    self.messageId = ntohs(*(uint16_t *)messageId);
    
    uint8_t flagByte[1];
    [input read:flagByte maxLength:sizeof(flagByte)];
    self.bufferPosition++;
    char flag = *(char *)flagByte;
    
    self.request = ((flag & 0xFF) & KAASYNC_REQUEST_FLAG) != 0;
    self.zipped = ((flag & 0xFF) & KAASYNC_ZIPPED_FLAG) != 0;
    self.encrypted = ((flag & 0xFF) & KAASYNC_ENCRYPTED_FLAG) != 0;

    self.kaaSyncMessageType = (flag >> KAASYNC_MESSAGE_TYPE_SHIFT) & 0x0F;
}

- (void)pack {
    [self packVariableHeader];
}

- (void)decode {
    NSInputStream *input = [self remainingStream];
    [input open];
    [self decodeVariableHeaderFromInput:input];
    [input close];
}

- (KAAMqttFrame *)upgradeFrame {
    switch (self.kaaSyncMessageType) {
        case KAA_SYNC_MESSAGE_TYPE_SYNC:
            if (self.request) {
                return [[KAATcpSyncRequest alloc] initWithOldKaaSync:self];
            } else {
                return [[KAATcpSyncResponse alloc] initWithOldKaaSync:self];
            }
            break;
        case KAA_SYNC_MESSAGE_TYPE_UNUSED:
            [NSException raise:KaaTcpProtocolException format:@"KaaSync Message type is incorrect"];
            break;
    }
    [NSException raise:KaaTcpProtocolException format:@"KaaSync Message type is incorrect"];
    return nil;
}

- (BOOL)needToCloseConnection {
    return NO;
}

@end
