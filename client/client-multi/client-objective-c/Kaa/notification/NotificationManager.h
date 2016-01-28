/*
 * Copyright 2014-2015 CyberVision, Inc.
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
 * @param delegate the delegate to receive updates.
 * @see NotificationTopicListDelegate
 */
- (void)addTopicListDelegate:(id<NotificationTopicListDelegate>)delegate;

/**
 * Remove delegate of notification topics' list updates.
 *
 * @param delegate delegate the delegate which is no longer needs updates.
 * @see NotificationTopicListDelegate
 */
- (void)removeTopicListDelegate:(id<NotificationTopicListDelegate>)delegate;

/**
 * Retrieve a list of available notification topics.
 *
 * @return list of available topics <Topic>
 * @see Topic
 */
- (NSArray *)getTopics;

/**
 * Add delegate to receive all notifications (both for mandatory and
 * optional topics).
 *
 * @param delegate delegate to receive notifications
 */
- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate;

/**
 * Add listener to receive notifications relating to the specified topic.
 *
 * Listener(s) for optional topics may be added/removed irrespective to
 * whether subscription was already or not.
 *
 * @param delegate delegate to receive notifications.
 * @param topicId Id of topic (both mandatory and optional).
 *
 * @throws UnavailableTopicException if unknown topic id is provided.
 */
- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(NSString *)topicId;

/**
 * Remove listener receiving all notifications (both for mandatory and optional topics).
 *
 * @param delegate delegate to receive notifications
 */
- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate;

/**
 * Remove delegate receiving notifications for the specified topic.
 *
 * Delegate(s) for optional topics may be added/removed irrespective to
 * whether subscription was already or not.
 *
 * @param delegate delegate to receive notifications.
 * @param topicId Id of topic (both mandatory and optional).
 *
 * @throws UnavailableTopicException if unknown topic id is provided.
 */
- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(NSString *)topicId;

/**
 * Subscribe to notifications relating to the specified optional topic.
 *
 * @param topicId Id of a optional topic.
 * @param forceSync Define whether current subscription update should be accepted immediately (#sync)
 *
 * @throws UnavailableTopicException if unknown topic id is provided or topic isn't optional.
 *
 * @see #sync
 */
- (void)subscribeToTopicWithId:(NSString *)topicId forceSync:(BOOL)forceSync;

/**
 * Subscribe to notifications relating to the specified list of optional topics.
 *
 * @param topicIds list of optional topic ids. <NSString>
 * @param forceSync define whether current subscription update should be accepted immediately (#sync)
 *
 * @throws UnavailableTopicException if unknown topic id is provided or topic isn't optional.
 *
 * @see #sync
 */
- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync;

/**
 * Unsubscribe from notifications relating to the specified optional topic.
 *
 * All previously added listeners will be removed automatically.
 *
 * @param topicId Id of a optional topic.
 * @param forceSync define whether current subscription update should be accepted immediately (#sync).
 *
 * @throws UnavailableTopicException if unknown topic id is provided or topic isn't optional.
 *
 * @see #sync
 */
- (void)unsubscribeFromTopicWithId:(NSString *)topicId forceSync:(BOOL)forceSync;

/**
 * Unsubscribe from notifications relating to the specified list of optional topics.
 *
 * All previously added listeners will be removed automatically.
 *
 * @param topicIds list of optional topic ids. <NSString>
 * @param forceSync define whether current subscription update should be accepted immediately (#sync).
 *
 * @throws UnavailableTopicException if unknown topic id is provided or topic isn't optional.
 *
 * @see #sync
 */
- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync;

/**
 * Accept optional subscription changes.
 */
- (void)sync;

@end

#endif
