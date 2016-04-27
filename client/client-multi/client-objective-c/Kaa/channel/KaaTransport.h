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

#ifndef Kaa_KaaTransport_h
#define Kaa_KaaTransport_h

#import "KaaChannelManager.h"
#import "KaaClientState.h"

/**
 * Transport interface processing request
 * and response for the specific service.
 */
@protocol KaaTransport

/**
 * Sets the specific KaaChannelManager for the current transport.
 */
- (void)setChannelManager:(id<KaaChannelManager>)channelManager;

/**
 * Sets the client's state object.
 */
- (void)setClientState:(id<KaaClientState>)state;

/**
 * Sends update request to the server.
 */
- (void)sync;

@end

#endif
