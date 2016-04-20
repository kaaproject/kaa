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

#ifndef Kaa_ProfileTransport_h
#define Kaa_ProfileTransport_h

#import <Foundation/Foundation.h>
#import "KaaTransport.h"
#import "EndpointGen.h"
#import "ProfileManager.h"
#import "KaaClientProperties.h"

/**
 * KaaTransport for the Profile service.
 * Updates the Profile manager state.
 */
@protocol ProfileTransport <KaaTransport>

/**
 * Creates a new Profile update request.
 *
 * @return New Profile update request.
 * @see ProfileSyncRequest
 */
- (ProfileSyncRequest *)createProfileRequest;

/**
 * Updates the state of the Profile manager from the given response.
 *
 * @param response The response from the server.
 * @see ProfileSyncResponse
 */
- (void)onProfileResponse:(ProfileSyncResponse *)response;

/**
 * Sets the given Profile manager.
 *
 * @param manager The Profile manager to be set.
 * @see ProfileManager
 */
- (void)setProfileManager:(id<ProfileManager>)manager;

/**
 * Sets the given client's properties.
 *
 * @param properties The client's properties to be set.
 * @see KaaClientProperties
 */
- (void)setClientProperties:(KaaClientProperties *)clientProperties;

@end

#endif
