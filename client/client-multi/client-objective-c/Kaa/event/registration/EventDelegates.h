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

#ifndef Kaa_CommonEventDelegates_h
#define Kaa_CommonEventDelegates_h

#import <Foundation/Foundation.h>
#import "EndpointGen.h"
#import "EndpointKeyHash.h"

/**
 * Callback interface for attached endpoint list change notifications
 */
@protocol ChangedAttachedEndpointListDelegate

/**
 * Callback on attached endpoints list changed
 *
 * @param attachedEndpoints <EndpointAccessToken, EndpointKeyHash> as key-value
 *
 */
- (void)onAttachedEndpointListChanged:(NSDictionary *)attachedEndpoints;

@end


/**
 * Callback interface for attached endpoint notifications.
 *
 * Use this interface to receive result of next operations:
 *
 * Attach endpoint to user by <EndpointAccessToken>
 *
 * Once result from Operations server is received, listener is notified with
 * string representation of operation name, result of the operation <SyncResponseResultType>
 * and additional data if available.
 */
@protocol OnAttachEndpointOperationDelegate

/**
 * Callback on endpoint attach response<br>
 * For AttachEndpoint operation is populated with EndpointKeyHash of attached endpoint.
 *
 * NOTE: endpointKeyHash is not null for endpoint attach operation
 * and contains EndpointKeyHash object with key hash of attached endpoint.
 *
 * @param endpointKeyHash Additional data of operation result, may be nil.
 */
- (void)onAttachResult:(SyncResponseResultType)result withEndpointKeyHash:(EndpointKeyHash *)endpointKeyHash;

@end


/**
 * Callback interface for detached endpoint notifications.
 *
 * Use this interface to receive result of next operations:<br>
 * Detach endpoint from user by EndpointKeyHash
 *
 * Once result from Operations server is received, listener is notified with
 * string representation of operation name, result of the operation SyncResponseResultType
 * and additional data if available.
 */
@protocol OnDetachEndpointOperationDelegate

/**
 * Callback on endpoint detach response
 */
- (void)onDetachResult:(SyncResponseResultType)result;

@end


/**
 * Retrieves result of user authentication
 *
 * Use this listener to retrieve result of attaching current endpoint to user.
 */
@protocol UserAttachDelegate

/**
 * Called when auth result is retrieved from operations server.
 */
- (void)onAttachResult:(UserAttachResponse *)response;

@end


/**
 * Callback interface for attached to user notifications.
 *
 * Provide listener implementation to <EndpointRegistrationManager> to
 * retrieve notification when current endpoint is attached to user by another endpoint.
 */
@protocol AttachEndpointToUserDelegate

/**
 * Callback on current endpoint is attached to user.
 */
- (void)onAttachedToUser:(NSString *)userExternalId token:(NSString *)endpointAccessToken;

@end


/**
 * Callback interface for detached from user notifications.
 *
 * Provide listener implementation to <EndpointRegistrationManager> to
 * retrieve notification when current endpoint is detached from user by another endpoint.
 */
@protocol DetachEndpointFromUserDelegate

/**
 * Callback on current endpoint is detached from user.
 */
- (void)onDetachedEndpointWithAccessToken:(NSString *)endpointAccessToken;

@end


/**
 * Listener interface for retrieving endpoints array
 * which supports requested event class FQNs
 */
@protocol FindEventListenersDelegate

/**
 * Called when resolve was successful
 *
 * @param eventListeners Array of endpoints <NSString>
 */
- (void)onEventListenersReceived:(NSArray *)eventListeners;

// TODO: add some kind of error reason

/**
 * Called when some error occured during resolving endpoints via event class FQNs.
 */
- (void)onRequestFailed;

@end


#endif
