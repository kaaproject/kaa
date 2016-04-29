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

#ifndef Kaa_MetaDataTransport_h
#define Kaa_MetaDataTransport_h

#import <Foundation/Foundation.h>
#import "EndpointGen.h"
#import "KaaClientProperties.h"
#import "KaaClientState.h"
#import "EndpointObjectHash.h"

/**
 * Transport for general client's state.
 */
@protocol MetaDataTransport

/**
 * Creates new Meta data request.
 */
- (SyncRequestMetaData *)createMetaDataRequest;

/**
 * Sets the given client's properties.
 */
- (void)setClientProperties:(KaaClientProperties *)properties;

/**
 * Sets the given client's state .
 */
- (void)setClientState:(id<KaaClientState>)state;

/**
 * Sets the given public key hash.
 */
- (void)setEndpointPublicKeyHash:(EndpointObjectHash *)hash;

/**
 * Sets the response timeout.
 */
- (void)setTimeout:(int64_t)timeout;

@end

#endif
