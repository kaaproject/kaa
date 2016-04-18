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

#ifndef Kaa_ProfileManager_h
#define Kaa_ProfileManager_h

#import <Foundation/Foundation.h>
#import "ProfileCommon.h"

/**
 * Interface for the profile manager.
 *
 * Responsible for the management of the user-defined profile container
 *
 * Profile manager is used to track any profile updates.
 * If no container is set, Kaa won't be able to process these updates.
 *
 * @see AbstractProfileContainer
 * @see SerializedProfileContainer
 */
@protocol ProfileManager

/**
 * Sets profile container implemented by the user.
 *
 * @param container User-defined container
 * @see AbstractProfileContainer
 */
- (void)setProfileContainer:(id<ProfileContainer>)container;

/**
 * Retrieves serialized profile
 *
 * @return Serialized profile data
 */
- (NSData *)getSerializedProfile;

/**
 * Force sync of updated profile with server
 */
- (void)updateProfile;

/**
 * Retrieves ready-to-use state.
 *
 * A user should provide a profile container in case of a non-default profile schema.
 *
 * @return YES if ready.
 */
- (BOOL)isInitialized;

@end

#endif
