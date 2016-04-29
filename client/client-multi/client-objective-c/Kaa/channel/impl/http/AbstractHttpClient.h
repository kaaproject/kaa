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
#import "MessageEncoderDecoder.h"

@interface AbstractHttpClient : NSObject

@property (nonatomic, strong) NSString *url;

- (instancetype)initWithURLString:(NSString *)url
                    privateKeyRef:(SecKeyRef)privateK
                     publicKeyRef:(SecKeyRef)publicK
                        remoteKey:(NSData *)remoteK;

- (instancetype)initWithURLString:(NSString *)url
                    privateKeyRef:(SecKeyRef)privateK
                     publicKeyRef:(SecKeyRef)publicK
                     remoteKeyRef:(SecKeyRef)remoteK;

- (void)disableVerification;
- (NSData *)verifyResponse:(NSData *)body signature:(NSData *)signature;
- (MessageEncoderDecoder *)getEncoderDecoder;

//NOTE: methods below are abstract

- (void)executeHttpRequest:(NSString *)uri
                    entity:(NSDictionary *)entity
            verifyResponse:(BOOL)verifyResponse
                   success:(void (^)(NSData *response))success
                   failure:(void (^)(NSInteger responseCode))failure;
- (void)close;
- (void)abort;
- (BOOL)canAbort;

@end
