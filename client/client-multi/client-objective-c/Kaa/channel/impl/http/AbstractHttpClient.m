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

#import "AbstractHttpClient.h"
#import "KaaExceptions.h"
@interface AbstractHttpClient ()

@property (nonatomic, strong) MessageEncoderDecoder *encoderDecoder;
@property (nonatomic) BOOL verificationEnabled;

@end

@implementation AbstractHttpClient

- (instancetype)initWithURLString:(NSString *)url
                    privateKeyRef:(SecKeyRef)privateK
                     publicKeyRef:(SecKeyRef)publicK
                        remoteKey:(NSData *)remoteK {
    self = [super init];
    if (self) {
        self.url = url;
        KeyPair *keys = [[KeyPair alloc] initWithPrivateKeyRef:privateK publicKeyRef:publicK];
        self.encoderDecoder = [[MessageEncoderDecoder alloc] initWithKeyPair:keys remotePublicKey:remoteK];
        self.verificationEnabled = YES;
    }
    return self;
}

- (instancetype)initWithURLString:(NSString *)url
                    privateKeyRef:(SecKeyRef)privateK
                     publicKeyRef:(SecKeyRef)publicK
                     remoteKeyRef:(SecKeyRef)remoteK {
    self = [super init];
    if (self) {
        self.url = url;
        KeyPair *keys = [[KeyPair alloc] initWithPrivateKeyRef:privateK publicKeyRef:publicK];
        self.encoderDecoder = [[MessageEncoderDecoder alloc] initWithKeyPair:keys remotePublicKeyRef:remoteK];
        self.verificationEnabled = YES;
    }
    return self;
}

- (void)disableVerification {
    self.verificationEnabled = NO;
}

- (NSData *)verifyResponse:(NSData *)body signature:(NSData *)signature {
    if (!self.verificationEnabled || [self.encoderDecoder verifyMessage:body withSignature:signature]) {
        return body;
    } else {
        [NSException raise:KaaSecurityException format:@"Message can't be verified"];
        return nil;
    }
}

- (MessageEncoderDecoder *)getEncoderDecoder {
    return self.encoderDecoder;
}

- (void)executeHttpRequest:(NSString *)uri
                    entity:(NSDictionary *)entity
            verifyResponse:(BOOL)verifyResponse
                   success:(void (^)(NSData *response))success
                   failure:(void (^)(NSInteger responseCode))failure {
#pragma unused (uri, entity, verifyResponse, success, failure)
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented"];
}

- (void)close {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented"];
}

- (void)abort {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented"];
}

- (BOOL)canAbort {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented"];
    return NO;
}

@end
