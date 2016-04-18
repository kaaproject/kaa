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

#ifndef Kaa_KaaClientPlatformContext_h
#define Kaa_KaaClientPlatformContext_h

#import <Foundation/Foundation.h>
#import "KaaClientProperties.h"
#import "AbstractHttpClient.h"
#import "KAABase64.h"
#import "ConnectivityChecker.h"
#import "ExecutorContext.h"

/**
 * Represents platform specific context for Kaa client initialization
 */
@protocol KaaClientPlatformContext

/**
 * Returns platform SDK properties
 */
- (KaaClientProperties *)getProperties;

/**
 * Returns platform dependent implementation of http client
 */
- (AbstractHttpClient *)createHttpClientWithURLString:(NSString *)url
                                        privateKeyRef:(SecKeyRef)privateK
                                         publicKeyRef:(SecKeyRef)publicK
                                            remoteKey:(NSData *)remoteK;

/**
 * Returns platform dependent implementation of Base64 algorithm
 */
- (id<KAABase64>)getBase64;

/**
 * Creates checker for internet connection
 */
- (ConnectivityChecker *)createConnectivityChecker;

/**
 * Returns SDK thread execution context
 */
- (id<ExecutorContext>)getExecutorContext;

@end

#endif
