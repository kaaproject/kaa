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

#ifndef Kaa_UserTransport_h
#define Kaa_UserTransport_h

#import <Foundation/Foundation.h>
#import "KaaTransport.h"
#import "EndpointGen.h"
#import "EndpointRegistrationProcessor.h"

/**
 * KaaTransport for the Endpoint service.
 * Updates the Endpoint manager state.
 */
@protocol UserTransport <KaaTransport>

/**
 * Creates new User update request.
 *
 * @return New User update request.
 * @see UserSyncRequest
 */
- (UserSyncRequest *)createUserRequest;

/**
* Updates the state of the Endpoint manager according to the given response.
*
* @param response The response from the server.
* @see UserSyncResponse
*/
- (void)onUserResponse:(UserSyncResponse *)response;

/**
 * Sets the given Endpoint processor.
 *
 * @param processor The Endpoint processor to be set.
 * @see EndpointRegistrationProcessor
 */
- (void)setEndpointRegistrationProcessor:(id<EndpointRegistrationProcessor>)processor;

@end

#endif
