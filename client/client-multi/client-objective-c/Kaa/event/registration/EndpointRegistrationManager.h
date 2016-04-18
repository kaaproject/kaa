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

#ifndef Kaa_EndpointRegistrationManager_h
#define Kaa_EndpointRegistrationManager_h

#import <Foundation/Foundation.h>
#import "EndpointGen.h"
#import "EndpointKeyHash.h"
#import "EventDelegates.h"
#import "EndpointAccessToken.h"

/**
  Module that manages endpoint-initiated attaching and detaching endpoints to/from user.
 
  To assign endpoints to user current endpoint has to be already attached,
  otherwise attach/detach operations will fail.
 
  Current endpoint can be attached to user in two ways:<br>
  1. By calling -attachUser.<br>
  2. Attached from another endpoint.
 
  EndpointKeyHash for endpoint can be received with AttachEndpoint operation
  provided from Operations server.
 
  If current endpoint is assumed to be attached or detached by another endpoint,
  specific AttachEndpointToUserDelegate and DetachEndpointFromUserDelegate
  may be specified to receive notification about such event.
 
  Manager uses specific UserTransport to communicate with Operations
  server in scope of basic functionality and ProfileTransport when its
  access token is changed.
 */
@protocol EndpointRegistrationManager

/**
 * Updates with new endpoint attach request.
 *
 * OnAttachEndpointOperationDelegate is populated with EndpointKeyHash of an attached endpoint.
 *
 * @param accessToken Access token of the attaching endpoint
 * @param delegate Delegate to notify about result of the endpoint attaching
 */
- (void)attachEndpointWithAccessToken:(EndpointAccessToken *)accessToken delegate:(id<OnAttachEndpointOperationDelegate>)delegate;

/**
 * Updates with new endpoint detach request
 *
 * @param endpointKeyHash Key hash of the detaching endpoint
 * @param delegate Delegate to notify about result of the enpoint detaching
 */
- (void)detachEndpointWithKeyHash:(EndpointKeyHash *)keyHash delegate:(id<OnDetachEndpointOperationDelegate>)delegate;

/**
 * Creates user attach request using default verifier. Default verifier is selected during SDK generation.
 * If there was no default verifier selected this method will throw runtime exception.
 */
- (void)attachUserWithId:(NSString *)userExternalId userAccessToken:(NSString *)token delegate:(id<UserAttachDelegate>)delegate;

/**
 * Creates user attach request using specified verifier.
 */
- (void)attachUserWithVerifierToken:(NSString *)userVerifierToken
                     userExternalId:(NSString *)externalId
                    userAccessToken:(NSString *)token
                           delegate:(id<UserAttachDelegate>)delegate;

/**
 * Checks if current endpoint is attached to user.
 *
 * @return YES if current endpoint is attached to any user, NO otherwise.
 */
- (BOOL)isAttachedToUser;

/**
 * Sets delegate for notifications when current endpoint is attached to user
 */
- (void)setAttachDelegate:(id<AttachEndpointToUserDelegate>)delegate;

/**
 * Sets delegate for notifications when current endpoint is detached from user
 */
- (void)setDetachDelegate:(id<DetachEndpointFromUserDelegate>)delegate;

@end


#endif
