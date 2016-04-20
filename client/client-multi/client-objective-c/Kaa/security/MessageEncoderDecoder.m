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

#import "MessageEncoderDecoder.h"
#import "NSData+CommonCrypto.h"
#import "NSData+Conversion.h"
#import "KeyUtils.h"
#import "KaaLogging.h"

#define TAG @"MessageEncoderDecoder"

#define SESSION_KEY_SIZE    kCCKeySizeAES128
#define WRAP_PADDING_TYPE   kSecPaddingPKCS1

static const uint8_t remotePublicKeyIdentifier[] = "org.kaaproject.kaa.remotepublickey";

@interface MessageEncoderDecoder ()

@property (nonatomic, strong) NSData *sessionKey;
@property (nonatomic, strong) KeyPair *keys;
@property (nonatomic) SecKeyRef remoteKey;
@property (nonatomic, strong) NSData *rawRemoteKey;

- (void)decodeSessionKey:(NSData *)encodedSessionKey;

@end

@implementation MessageEncoderDecoder

- (instancetype)initWithKeyPair:(KeyPair *)keys {
    return [self initWithKeyPair:keys remotePublicKey:nil];
}

- (instancetype)initWithKeyPair:(KeyPair *)keys remotePublicKey:(NSData *)remoteKey {
    self = [super init];
    if (self) {
        self.keys = keys;
        self.remoteKey = NULL;
        self.rawRemoteKey = remoteKey;
        if (remoteKey) {
            [self setRemotePublicKey:remoteKey];
        }
        DDLogVerbose(@"%@ Initialized with key pair: [%@] \nRemotePublicKey: %@", TAG, self.keys, [remoteKey hexadecimalString]);
    }
    return self;
}

- (instancetype)initWithKeyPair:(KeyPair *)keys remotePublicKeyRef:(SecKeyRef)remoteKeyRef {
    self = [super init];
    if (self) {
        self.keys = keys;
        self.remoteKey = remoteKeyRef;
        self.rawRemoteKey = nil;
        DDLogVerbose(@"%@ Initialized with key pair: [%@] and remote key ref", TAG, self.keys);
    }
    return self;
}

- (NSData *)getSessionKey {
    if (!self.sessionKey) {
        OSStatus sanityCheck = noErr;
        uint8_t * symmetricKey = malloc(SESSION_KEY_SIZE * sizeof(uint8_t));
        
        if (symmetricKey == NULL) {
            DDLogError(@"%@ Problem allocating buffer space for symmetric key generation.", TAG);
            return nil;
        }
        
        memset((void *)symmetricKey, 0x0, SESSION_KEY_SIZE);
        
        sanityCheck = SecRandomCopyBytes(kSecRandomDefault, SESSION_KEY_SIZE, symmetricKey);
        if (sanityCheck != noErr) {
            free(symmetricKey);
            DDLogError(@"%@ Problem generating the symmetric key. OSStatus: %i", TAG, (int)sanityCheck);
            return nil;
        }
        
        self.sessionKey = [[NSData alloc] initWithBytes:(const void *)symmetricKey length:SESSION_KEY_SIZE];
        DDLogDebug(@"%@ Generated new session key: %@", TAG, self.sessionKey);
        
        if (symmetricKey) {
            free(symmetricKey);
        }
    }
    return self.sessionKey;
}

- (NSData *)getEncodedSessionKey {
    NSData *sessionKey = [self getSessionKey];
    if (!sessionKey) {
        DDLogError(@"%@ Session key parameter is nil", TAG);
        return nil;
    }
    OSStatus sanityCheck = noErr;
    size_t cipherBufferSize = SecKeyGetBlockSize(self.remoteKey);
    size_t keyBufferSize = [sessionKey length];
    
    if (keyBufferSize > cipherBufferSize || keyBufferSize > (cipherBufferSize - 11)) {
        DDLogWarn(@"%@ Nonce integer is too large and falls outside multiplicative group", TAG);
    }
    
    uint8_t *cipherBuffer = malloc(cipherBufferSize * sizeof(uint8_t));
    memset((void *)cipherBuffer, 0x0, cipherBufferSize);
    
    sanityCheck = SecKeyEncrypt(self.remoteKey,
                                WRAP_PADDING_TYPE,
                                (const uint8_t *)[sessionKey bytes],
                                keyBufferSize,
                                cipherBuffer,
                                &cipherBufferSize);
    
    if (sanityCheck != noErr) {
        DDLogError(@"%@ Error encrypting, OSStatus: %i", TAG, (int)sanityCheck);
    }
    
    NSData *cipher = [NSData dataWithBytes:(const void *)cipherBuffer length:(NSUInteger)cipherBufferSize];
    
    if (cipherBuffer) {
        free(cipherBuffer);
    }
    
    return cipher;
}

- (NSData *)encodeData:(NSData *)message {
    CCCryptorStatus status = kCCSuccess;
    NSData *result = [message dataEncryptedUsingAlgorithm:kCCAlgorithmAES key:[self getSessionKey]
                                                  options:kCCOptionPKCS7Padding | kCCOptionECBMode error:&status];
    if (status != kCCSuccess) {
        DDLogError(@"%@ Unable to encode data using current session key. Message: %@", TAG, [message hexadecimalString]);
        return nil;
    }
    return result;
}

- (NSData *)decodeData:(NSData *)message {
    CCCryptorStatus status = kCCSuccess;
    NSData *result = [message decryptedDataUsingAlgorithm:kCCAlgorithmAES key:[self getSessionKey]
                                                  options:kCCOptionPKCS7Padding | kCCOptionECBMode error:&status];
    if (status != kCCSuccess) {
        DDLogError(@"%@ Unable to decode data using current session key. Message: %@", TAG, [message hexadecimalString]);
        return nil;
    }
    return result;
}

- (NSData *)decodeData:(NSData *)message withEncodedKey:(NSData *)encodedKey {
    [self decodeSessionKey:encodedKey];
    return [self decodeData:message];
}

- (SecKeyRef)getPrivateKey {
    return [self.keys getPrivateKeyRef];
}

- (SecKeyRef)getPublicKey {
    return [self.keys getPublicKeyRef];
}

- (SecKeyRef)getRemotePublicKey {
    return self.remoteKey;
}

- (NSData *)getRemotePublicKeyAsBytes {
    return self.rawRemoteKey;
}

- (void)decodeSessionKey:(NSData *)encodedSessionKey {
    OSStatus sanityCheck = noErr;
    size_t cipherBufferSize = SecKeyGetBlockSize([self getPrivateKey]);
    size_t keyBufferSize = [encodedSessionKey length];
    
    if (keyBufferSize > cipherBufferSize) {
        DDLogWarn(@"%@ Encrypted nonce is too large and falls outside multiplicative group.", TAG);
    }
    
    uint8_t *keyBuffer = malloc(keyBufferSize * sizeof(uint8_t));
    memset((void *)keyBuffer, 0x0, keyBufferSize);
    
    sanityCheck = SecKeyDecrypt([self getPrivateKey],
                                WRAP_PADDING_TYPE,
                                (const uint8_t *) [encodedSessionKey bytes],
                                cipherBufferSize,
                                keyBuffer,
                                &keyBufferSize);
    
    if (sanityCheck != noErr) {
        DDLogError(@"%@ Error decrypting. OSStatus: %i", TAG, (int)sanityCheck);
    }
    
    self.sessionKey = [NSData dataWithBytes:(const void *)keyBuffer length:(NSUInteger)keyBufferSize];
    
    if (keyBuffer) {
        free(keyBuffer);
    }
}

- (NSData *)signatureForMessage:(NSData *)message {
    size_t signedHashBytesSize = SecKeyGetBlockSize([self getPrivateKey]);
    uint8_t *signedHashBytes = malloc(signedHashBytesSize);
    memset(signedHashBytes, 0x0, signedHashBytesSize);
    
    size_t hashBytesSize = CC_SHA1_DIGEST_LENGTH;
    uint8_t *hashBytes = malloc(hashBytesSize);
    if (!CC_SHA1([message bytes], (CC_LONG)[message length], hashBytes)) {
        free(signedHashBytes);
        free(hashBytes);
        return nil;
    }
    
    OSStatus status = SecKeyRawSign([self getPrivateKey],
                                    kSecPaddingPKCS1SHA1,
                                    hashBytes,
                                    hashBytesSize,
                                    signedHashBytes,
                                    &signedHashBytesSize);
    DDLogVerbose(@"%@ Signing data status: %i", TAG, (int)status);
    NSData *signedHash = [NSData dataWithBytes:signedHashBytes length:(NSUInteger)signedHashBytesSize];
    
    if (hashBytes)
        free(hashBytes);
    if (signedHashBytes)
        free(signedHashBytes);
    
    return signedHash;
}

- (BOOL)verifyMessage:(NSData *)message withSignature:(NSData *)signature {
    size_t signedHashBytesSize = SecKeyGetBlockSize([self getRemotePublicKey]);
    const void* signedHashBytes = [signature bytes];
    
    size_t hashBytesSize = CC_SHA1_DIGEST_LENGTH;
    uint8_t *hashBytes = malloc(hashBytesSize);
    if (!CC_SHA1([message bytes], (CC_LONG)[message length], hashBytes)) {
        free(hashBytes);
        return NO;
    }
    
    OSStatus status = SecKeyRawVerify([self getRemotePublicKey],
                                      kSecPaddingPKCS1SHA1,
                                      hashBytes,
                                      hashBytesSize,
                                      signedHashBytes,
                                      signedHashBytesSize);
    if (hashBytes) {
        free(hashBytes);
    }
    BOOL verified = status == errSecSuccess;
    return verified;
}

- (void)setRemotePublicKey:(NSData *)remotePublicKey {
    NSData *tag = [[NSData alloc] initWithBytes:remotePublicKeyIdentifier length:sizeof(remotePublicKeyIdentifier)];
    [KeyUtils removeKeyByTag:tag];
    self.remoteKey = [KeyUtils storePublicKey:remotePublicKey withTag:tag];
}

- (void)setRemotePublicKeyRef:(SecKeyRef)remotePublicKeyRef {
    self.remoteKey = remotePublicKeyRef;
}
@end
