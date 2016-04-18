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

#ifndef Kaa_BootstrapTransport_h
#define Kaa_BootstrapTransport_h

#import <Foundation/Foundation.h>
#import "KaaTransport.h"
#import "EndpointGen.h"
#import "BootstrapManager.h"

@protocol BootstrapManager;
/**
 * KaaTransport for the Bootstrap service.
 * Updates the Bootstrap manager state.
 */
@protocol BootstrapTransport <KaaTransport>

/**
 * Creates new Resolve request.
 *
 * @return Resovle request.
 */
- (SyncRequest *)createResolveRequest;

/**
 * Updates the state of the Bootstrap manager according the given response.
 *
 * @param servers response from Bootstrap server.
 */
- (void)onResolveResponse:(SyncResponse *)servers;

/**
 * Sets the given Bootstrap manager.
 *
 * @param manager the Bootstrap manager to be set.
 */
- (void)setBootstrapManager:(id<BootstrapManager>)manager;

@end

#endif
