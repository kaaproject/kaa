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

#ifndef Kaa_NotificationManager_h
#define Kaa_NotificationManager_h

#import <Foundation/Foundation.h>
#import "NotificationTopicListDelegate.h"
#import "NotificationCommon.h"

/**
 * Interface for the notification delivery system.
 *
 * Responsible for processing received topic/notification updates, subscribing
 * for optional topic updates and unsubscribing from them.
 *
 * @see AbstractNotificationListener
 * @see NotificationTopicListDelegate
 */
@protocol NotificationManager

/**
 * Add delegate for notification topics' list updates.
 *
 * @param delegate The delegate to receive updates.
 * @see NotificationTopicListDelegate
 */
- (void)addTopicListDelegate:(id<NotificationTopicListDelegate>)delegate;

/**
 * Remove delegate of notification topics' list updates.
 *
 * @param delegate The delegate which no longer needs updates.
 * @see NotificationTopicListDelegate
 */
- (void)removeTopicListDelegate:(id<NotificationTopicListDelegate>)delegate;

/**
 * Retrieve a list of available notification topics.
 *
 * @return Array of available topics.
 * @see Topic
 */
- (NSArray *)getTopics;

/**
 * Add delegate to receive all notifications (both for mandatory and
 * optional topics).
 *
 * @param delegate Delegate to receive notifications
 */
- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate;

/**
 * Add listener to receive notifications relating to the specified topic.
 *
 * Listener(s) for optional topics may be added/removed irrespective to
 * whether subscription existed or not.
 *
 * @param delegate Delegate to receive notifications.
 * @param topicId Id of topic (both mandatory and optional).
 *
 * @exception UnavailableTopicException Unknown topic id is provided.
 */
- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId;

/**
 * Remove listener receiving all notifications (both for mandatory and optional topics).
 *
 * @param delegate Delegate to receive notifications
 */
- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate;

/**
 * Remove delegate receiving notifications for the specified topic.
 *
 * Delegate(s) for optional topics may be added/removed irrespective to
 * whether subscription existed or not.
 *
 * @param delegate Delegate to receive notifications.
 * @param topicId Id of topic (both mandatory and optional).
 *
 * @exception UnavailableTopicException Unknown topic id is provided.
 */
- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId;

/**
 * Subscribe to notifications relating to the specified optional topic.
 *
 * @param topicId Id of a optional topic.
 * @param forceSync Define whether current subscription update should be accepted immediately
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)subscribeToTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync;

/**
 * Subscribe to notifications relating to the specified list of optional topics.
 *
 * @param topicIds Array of optional topic ids.
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync;

/**
 * Unsubscribe from notifications relating to the specified optional topic.
 *
 * All previously added listeners will be removed automatically.
 *
 * @param topicId Id of a optional topic.
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)unsubscribeFromTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync;

/**
 * Unsubscribe from notifications relating to the specified array of optional topics.
 *
 * All previously added listeners will be removed automatically.
 *
 * @param topicIds Array of optional topic ids. < NSString >
 * @param forceSync Define whether current subscription update should be accepted immediately.
 *
 * @exception UnavailableTopicException Unknown topic id is provided or topic isn't optional.
 */
- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync;

/**
 * Accept optional subscription changes.
 */
- (void)sync;

@end

#endif
