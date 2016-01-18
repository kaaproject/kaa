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

#import <Foundation/Foundation.h>
#import "KeyPair.h"

/**
 * The Class MessageEncoderDecoder is responsible for encoding/decoding logic of
 * endpoint - operations server communication.
 */
@interface MessageEncoderDecoder : NSObject

- (instancetype)initWithKeyPair:(KeyPair *)keys;

- (instancetype)initWithKeyPair:(KeyPair *)keys remotePublicKey:(NSData *)remoteKey;

- (instancetype)initWithKeyPair:(KeyPair *)keys remotePublicKeyRef:(SecKeyRef)remoteKeyRef;

- (NSData *)getSessionKey;

- (NSData *)getEncodedSessionKey;

/**
 * Encode data using sessionKey.
 *
 * @param message the data
 */
- (NSData *)encodeData:(NSData *)message;

/**
 * Decode data using session key.
 *
 * @param message the data
 */
- (NSData *)decodeData:(NSData *)message;

/**
 * Decode data using session key which is decoded using private key.
 *
 * @param message the date to decode
 * @param encodedKey the encoded key
 */
- (NSData *)decodeData:(NSData *)message withEncodedKey:(NSData *)encodedKey;

- (SecKeyRef)getPrivateKey;

- (SecKeyRef)getPublicKey;

- (SecKeyRef)getRemotePublicKey;

- (NSData *)getRemotePublicKeyAsBytes;

/**
 * Sign message using private key.
 */
- (NSData *)sign:(NSData *)message;

/**
 * Verify message using signature and remote public key.
 */
- (BOOL)verify:(NSData *)message withSignature:(NSData *)signature;

- (void)setRemotePublicKey:(NSData *)remotePublicKey;
- (void)setRemotePublicKeyRef:(SecKeyRef)remotePublicKeyRef;

@end
