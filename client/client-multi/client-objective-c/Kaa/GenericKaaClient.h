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

#ifndef Kaa_GenericKaaClient_h
#define Kaa_GenericKaaClient_h

#import "ConfigurationStorage.h"
#import "NotificationTopicListDelegate.h"
#import "LogStorage.h"
#import "LogUploadStrategy.h"
#import "EventDelegates.h"
#import "KaaChannelManager.h"
#import "EndpointAccessToken.h"
#import "EndpointRegistrationManager.h"
#import "EventFamilyFactory.h"
#import "ProfileCommon.h"
#import "ConfigurationCommon.h"
#import "NotificationCommon.h"
#import "LogDeliveryDelegate.h"
#import "FailoverStrategy.h"
#import "FailureDelegate.h"

/**
 * Root interface for the Kaa client.<br>
 * This interface contains methods that are predefined and does not contain any auto-generated code.
 */
@protocol GenericKaaClient <NSObject>

/**
 * Starts Kaa's workflow.
 */
- (void)start;

/**
 * Stops Kaa's workflow.
 */
- (void)stop;

/**
 * Pauses Kaa's workflow.
 */
- (void)pause;

/**
 * Resumes Kaa's workflow.
 */
- (void)resume;

/**
 * Sets profile container implemented by the user.
 */
- (void)setProfileContainer:(id<ProfileContainer>)container;

/**
 * Sync of updated profile with server
 */
- (void)updateProfile;

/**
 * Sets the configuration storage that will be used to persist configuration.
 */
- (void)setConfigurationStorage:(id<ConfigurationStorage>)storage;

/**
 * Register configuration update delegate
 */
-(void)addConfigurationDelegate:(id<ConfigurationDelegate>)delegate;

/**
 * Removes configuration update delegate
 */
- (void)removeConfigurationDelegate:(id<ConfigurationDelegate>)delegate;

/**
 * Add delegate for notification topics' list updates.
 */
- (void)addTopicListDelegate:(id<NotificationTopicListDelegate>)delegate;

/**
 * Remove delegate of notification topics' list updates.
 */
- (void)removeTopicListDelegate:(id<NotificationTopicListDelegate>)delegate;

/**
 * Retrieve an array of available notification topics.
 * @return Array of available topics <Topic>
 */
- (NSArray *)getTopics;

/**
 * Add delegate to receive all notifications (both for mandatory and optional topics).
 */
- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate;

/**
 * Add delegate to receive notifications relating to the specified topic.<br>
 * Delegate(s) for optional topics may be added/removed irrespective to
 * whether subscription existed or not.
 *
 * @exception UnavailableTopicException Unknown topic id is provided.
 */
- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId;

/**
 * Remove delegate receiving all notifications (both for mandatory and optional topics).
 */
- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate;

/**
 * Remove delegate receiving notifications for the specified topic.<br>
 * Delegate(s) for optional topics may be added/removed irrespective to
 * whether subscription existed or not.
 *
 * @param topicId ID of topic (both mandatory and optional).
 *
 * @exception UnavailableTopicException Unknown topic id is provided.
 */
- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId;

/**
 * Subscribe to notifications relating to the specified optional topic.
 * @param topicId ID of a optional topic.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)subscribeToTopicWithId:(int64_t)topicId;

/**
 * Subscribe to notifications relating to the specified optional topic.
 *
 * @param topicId ID of an optional topic.
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)subscribeToTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync;

/**
 * Subscribe to notifications relating to the specified array of optional topics.
 *
 * @param topicIds Array of optional topic IDs. <int64_t>
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds;

/**
 * Subscribe to notifications relating to the specified array of optional topics.
 *
 * @param topicIds Array of optional topic IDs. <int64_t>
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 *
 */
- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync;

/**
 * Unsubscribe from notifications relating to the specified optional topic.
 * All previously added delegates will be removed automatically.
 *
 * @param topicId ID of optional topic.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)unsubscribeFromTopicWithId:(int64_t)topicId;

/**
 * Unsubscribe from notifications relating to the specified optional topic.
 * All previously added delegates will be removed automatically.
 *
 * @param topicId ID of optional topic.
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)unsubscribeFromTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync;

/**
 * Unsubscribe from notifications relating to the specified array of optional topics.
 * All previously added delegates will be removed automatically.
 *
 * @param topicIds Array of optional topic IDs. <int64_t>
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds;

/**
 * Unsubscribe from notifications relating to the specified array of optional topics.
 * All previously added delegates will be removed automatically.
 *
 * @param topicIds Array of optional topic IDs. <int64_t>
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync;

/**
 * Force sync of pending subscription changes with server.
 */
- (void)syncTopicsList;

/**
 * Set user implementation of a log storage.
 */
- (void)setLogStorage:(id<LogStorage>)storage;

/**
 * Set user implementation of a log upload strategy.
 */
- (void)setLogUploadStrategy:(id<LogUploadStrategy>)strategy;

/**
 * Set a delegate which receives a delivery status of each log bucket.
 */
- (void)setLogDeliveryDelegate:(id<LogDeliveryDelegate>)delegate;

/**
 * Retrieves Kaa event family factory.
 */
- (EventFamilyFactory *)getEventFamilyFactory;

/**
 * Submits an event delegates resolution request.
 *
 * @param eventFQNs Array of event class FQNs which have to be supported by endpoint. <NSString>
 */
- (void)findListenersForEventFQNs:(NSArray *)eventFQNs delegate:(id<FindEventListenersDelegate>) delegate;

/**
 * Retrieves Kaa channel manager.
 */
- (id<KaaChannelManager>)getChannelManager;

/**
 * Retrieves the client's public key.<br>
 * Required in user implementation of an operation data channel. Public key
 * hash (SHA-1) is used by servers as identification number to uniquely
 * identify each connected endpoint.
 */
- (SecKeyRef)getClientPublicKey;

/**
 * Retrieves endpoint public key hash.<br>
 * Required in EndpointRegistrationManager implementation to react
 * on detach response from Operations server.
 *
 * @return NSString containing current endpoint's public key hash.
 */
- (NSString *)getEndpointKeyHash;

/**
 * Retrieves the client's private key.<br>
 * Required in user implementation of an operation data channel. Private key
 * is used by encryption schema between endpoint and servers.
 *
 * @return Client's private key
 */
- (SecKeyRef)getClientPrivateKey;

/**
 * Set new access token for a current endpoint.
 */
- (void)setEndpointAccessToken:(NSString *)token;

/**
 * Generate new access token for a current endpoint.
 */
- (NSString *)refreshEndpointAccessToken;

/**
 * Retrieve an access token for a current endpoint.
 */
- (NSString *)getEndpointAccessToken;

/**
 * Updates with new endpoint attach request<br>
 * OnAttachEndpointOperationCallback is populated with EndpointKeyHash of an attached endpoint.
 *
 * @param endpointAccessToken Access token of the attaching endpoint
 * @param delegate Delegate to notify about result of the endpoint attaching
 */
- (void)attachEndpointWithAccessToken:(EndpointAccessToken *)endpointAccessToken delegate:(id<OnAttachEndpointOperationDelegate>)delegate;

/**
 * Updates with new endpoint detach request
 *
 * @param endpointKeyHash Key hash of the detaching endpoint
 * @param delegate Delegate to notify about result of the enpoint attaching
 */
- (void)detachEndpointWithKeyHash:(EndpointKeyHash *)endpointKeyHash delegate:(id<OnDetachEndpointOperationDelegate>)delegate;

/**
 * Creates user attach request using default verifier. Default verifier is selected during SDK generation.
 * If there was no default verifier selected this method will throw runtime exception.
 */
- (void)attachUserWithId:(NSString *)userExternalId accessToken:(NSString *)userAccessToken delegate:(id<UserAttachDelegate>)delegate;

/**
 * Creates user attach request using specified verifier.
 */
- (void)attachUserWithVerifierToken:(NSString*)userVerifierToken
                             userId:(NSString*)userExternalId
                        accessToken:(NSString*)userAccessToken
                           delegate:(id<UserAttachDelegate>)delegate;

/**
 * Checks if current endpoint is attached to user.
 */
- (BOOL)isAttachedToUser;

/**
 * Sets callback for notifications when current endpoint is attached to user.
 */
- (void)setAttachDelegate:(id<AttachEndpointToUserDelegate>)delegate;

/**
 * Sets callback for notifications when current endpoint is detached from user.
 */
- (void)setDetachDelegate:(id<DetachEndpointFromUserDelegate>)delegate;

/**
 * @param failoverStrategy strategy that will be used to resolve failovers.
 *
 * @see FailoverStrategy
 */
- (void)setFailoverStrategy:(id<FailoverStrategy>)failoverStrategy;

/**
 * Set listener which is notified on critical failures that are treated as something
 * that client should not handle on its own.
 *
 * @param delegate delegate to handle critical failures
 */
- (void)setFailureDelegate:(id<FailureDelegate>)delegate;

@end
#endif
