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

#import "KAATcpConnect.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"Connect >>>"

@interface KAATcpConnect ()

- (void)packVariableHeader;

- (void)decodeSyncRequestFromInput:(NSInputStream *)input;
- (void)decodeSignatureFromInput:(NSInputStream *)input;
- (void)decodeSessionKeyFromInput:(NSInputStream *)input;
- (void)decodeVariableHeaderFromInput:(NSInputStream *)input;
- (void)decodeKeepAliveFromInput:(NSInputStream *)input;

@end

static const char FIXED_HEADER_CONST[] = {0x00,0x06,'K','a','a','t','c','p',CONNECT_VERSION,CONNECT_FIXED_HEADER_FLAG};

@implementation KAATcpConnect

- (instancetype)init {
    self = [super init];
    if (self) {
        self.keepAlive = 200;
        [self setMessageType:TCP_MESSAGE_TYPE_CONNECT];
    }
    return self;
}

- (instancetype)initWithAlivePeriod:(uint16_t)keepAlive
                     nextProtocolId:(uint32_t)protocolId
                      aesSessionKey:(NSData *)key
                        syncRequest:(NSData *)request
                          signature:(NSData *)signature {
    self = [self init];
    if (self) {
        [self setKeepAlive:keepAlive];
        [self setNextProtocolId:protocolId];
        [self setAesSessionKey:key];
        [self setSyncRequest:request];
        [self setSignature:signature];
        self.remainingLength = CONNECT_VARIABLE_HEADER_LENGTH_V1;
        if (key) {
            self.remainingLength += CONNECT_AES_SESSION_KEY_LENGTH;
        }
        if (signature) {
            self.remainingLength += CONNECT_SIGNATURE_LENGTH;
        }
        if (request) {
            self.remainingLength += (int32_t)request.length;
        }
        DDLogDebug(@"%@ Created Connect message: session key size: %li, signature size: %li, sync request size: %li",
                   TAG, (long)key.length, (long)signature.length, (long)request.length);
    }
    return self;
}

- (void)pack {
    [self packVariableHeader];
    if (self.aesSessionKey) {
        [self.buffer appendData:self.aesSessionKey];
        self.bufferPosition += (int32_t)self.aesSessionKey.length;
    }
    if (self.signature) {
        [self.buffer appendData:self.signature];
        self.bufferPosition += (int32_t)self.signature.length;
    }
    if (self.syncRequest) {
        [self.buffer appendData:self.syncRequest];
        self.bufferPosition += (int32_t)self.syncRequest.length;
    }
}

- (void)setAesSessionKey:(NSData *)aesSessionKey {
    _aesSessionKey = aesSessionKey;
    _isEncrypted = nil != _aesSessionKey;
}

- (void)setSignature:(NSData *)signature {
    _signature = signature;
    _hasSignature = nil != _signature;
}

- (void)decode {
    NSInputStream *input = [self remainingStream];
    [input open];
    
    [self decodeVariableHeaderFromInput:input];
    
    uint8_t protocolId[4];
    [input read:protocolId maxLength:sizeof(protocolId)];
    _nextProtocolId = ntohl(*(uint32_t *)protocolId);
    self.bufferPosition += sizeof(protocolId);
    
    uint8_t aesKey[1];
    [input read:aesKey maxLength:sizeof(aesKey)];
    _isEncrypted = (*(char *)aesKey) != 0;
    self.bufferPosition += sizeof(aesKey);
    
    uint8_t sign[1];
    [input read:sign maxLength:sizeof(sign)];
    _hasSignature = (*(char *)sign) != 0;
    self.bufferPosition += sizeof(sign);
    
    [self decodeKeepAliveFromInput:input];
    
    if (_isEncrypted) {
        [self decodeSessionKeyFromInput:input];
    }
    if (_hasSignature) {
        [self decodeSignatureFromInput:input];
    }
    [self decodeSyncRequestFromInput:input];
    
    [input close];
}

- (BOOL)needToCloseConnection {
    return NO;
}

- (void)decodeSyncRequestFromInput:(NSInputStream *)input {
    int32_t syncRequestSize = (int32_t)(self.buffer.length - self.bufferPosition);
    if (syncRequestSize > 0) {
        uint8_t data[syncRequestSize];
        [input read:data maxLength:sizeof(data)];
        self.bufferPosition += syncRequestSize;
        self.syncRequest = [NSData dataWithBytes:data length:sizeof(data)];
    }
}

- (void)decodeSignatureFromInput:(NSInputStream *)input {
    uint8_t signature[CONNECT_SIGNATURE_LENGTH];
    [input read:signature maxLength:sizeof(signature)];
    self.bufferPosition += CONNECT_SIGNATURE_LENGTH;
    self.signature = [NSData dataWithBytes:signature length:CONNECT_SIGNATURE_LENGTH];
}

- (void)decodeSessionKeyFromInput:(NSInputStream *)input {
    uint8_t key[CONNECT_AES_SESSION_KEY_LENGTH];
    [input read:key maxLength:sizeof(key)];
    self.bufferPosition += CONNECT_AES_SESSION_KEY_LENGTH;
    self.aesSessionKey = [NSData dataWithBytes:key length:CONNECT_AES_SESSION_KEY_LENGTH];
}

- (void)decodeVariableHeaderFromInput:(NSInputStream *)input {
    int32_t headerSize = sizeof(FIXED_HEADER_CONST);
    uint8_t header[headerSize];
    [input read:header maxLength:headerSize];
    self.bufferPosition += headerSize;
    for (int i = 0; i < headerSize; i++) {
        if (header[i] != FIXED_HEADER_CONST[i]) {
            [NSException raise:KaaTcpProtocolException format:@"Kaatcp protocol version missmatch"];
        }
    }
}

- (void)decodeKeepAliveFromInput:(NSInputStream *)input {
    uint8_t keepAliveBytes[2];
    [input read:keepAliveBytes maxLength:sizeof(keepAliveBytes)];
    self.bufferPosition += sizeof(keepAliveBytes);
    self.keepAlive = ntohs(*(uint16_t *)keepAliveBytes);
}

- (void)packVariableHeader {
    [self.buffer appendBytes:FIXED_HEADER_CONST length:sizeof(FIXED_HEADER_CONST)];
    self.bufferPosition += sizeof(FIXED_HEADER_CONST);
    
    uint32_t protocolId = htonl(self.nextProtocolId);
    [self.buffer appendBytes:&protocolId length:sizeof(protocolId)];
    self.bufferPosition += sizeof(protocolId);
    
    char keyFlag = self.aesSessionKey ? CONNECT_SESSION_KEY_FLAGS : 0;
    [self.buffer appendBytes:&keyFlag length:sizeof(keyFlag)];
    self.bufferPosition += sizeof(keyFlag);
    
    char signFlag = self.signature ? CONNECT_SIGNATURE_FLAGS : 0;
    [self.buffer appendBytes:&signFlag length:sizeof(signFlag)];
    self.bufferPosition += sizeof(signFlag);
    
    uint16_t keepAlive = htons(self.keepAlive);
    [self.buffer appendBytes:&keepAlive length:sizeof(keepAlive)];
    self.bufferPosition += sizeof(keepAlive);
}

@end
