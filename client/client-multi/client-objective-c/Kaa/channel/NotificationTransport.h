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

#ifndef Kaa_NotificationTransport_h
#define Kaa_NotificationTransport_h

#import <Foundation/Foundation.h>
#import "EndpointGen.h"
#import "NotificationProcessor.h"
#import "KaaTransport.h"

/**
 * KaaTransport for the Notification service.
 * Updates the Notification manager state.
 */
@protocol NotificationTransport <KaaTransport>

/**
 * Creates a new Notification request.
 *
 * @return New Notification request.
 * @see NotificationSyncRequest
 */
- (NotificationSyncRequest *)createNotificationRequest;

/**
 * Creates a new empty Notification request.
 *
 * @return New empty Notification request.
 * @see NotificationSyncRequest
 */
- (NotificationSyncRequest *)createEmptyNotificationRequest;

/**
 * Updates the state of the Notification manager according to the given response.
 *
 * @param response The response from the server.
 * @see NotificationSyncResponse
 */
- (void)onNotificationResponse:(NotificationSyncResponse *)response;

/**
 * Sets the given Notification processor.
 *
 * @param processor The Notification processor to be set.
 * @see NotificationProcessor
 */
- (void)setNotificationProcessor:(id<NotificationProcessor>)processor;

/**
 * Notify about new subscription info.
 *
 * Will be called when one either subscribes or unsubscribes
 * on\from some optional topic(s).
 *
 * @param commands Info about subscription actions (subscribe/unsubscribe). < SubscriptionCommand >
 */
- (void)onSubscriptionChangedWithCommands:(NSArray *)commands;

@end

#endif
