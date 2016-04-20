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

#import <Foundation/Foundation.h>
#import "KeyPair.h"

/**
 * Class is used to persist and fetch Public and Private Keys.
 */
@interface KeyUtils : NSObject

/**
 * Used to generate key pair with custom tag or default one.
 */
+ (KeyPair *)generateKeyPair;
+ (KeyPair *)generateKeyPairWithPrivateTag:(NSData *)privateTag publicTag:(NSData *)publicTag;

/**
 * Gets reference to default public key from keychain.
 */
+ (SecKeyRef)getPublicKeyRef;

/**
 * Gets reference to default private key from keychain.
 */
+ (SecKeyRef)getPrivateKeyRef;

/**
 * Used to get key ref by selected tag from keychain.
 */
+ (SecKeyRef)getKeyRefByTag:(NSData *)tag;

/**
 * Gets default raw public key from keychain.
 */
+ (NSData *)getPublicKey;
+ (NSData *)getPublicKeyByTag:(NSData *)tag;

/**
 * Used to store remote key to keychain.
 */
+ (SecKeyRef)storePublicKey:(NSData *)publicKey withTag:(NSData *)tag;

/**
 * Used to remove stored key from keychain.
 */
+ (void)removeKeyByTag:(NSData *)tag;

/**
 * Used to remove default key pair from keychain.
 * NOTE: for removing key pair with custom tags use (removeKeyByTag:) for each key.
 */
+ (void)deleteExistingKeyPair;

@end
