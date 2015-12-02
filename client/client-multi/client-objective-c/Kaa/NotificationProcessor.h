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

#ifndef Kaa_NotificationProcessor_h
#define Kaa_NotificationProcessor_h

#import <Foundation/Foundation.h>
#import "EndpointGen.h"

/**
 * Used to process notifications.
 */
@protocol NotificationProcessor

/**
 * Called on topics' list update.
 *
 * @param list the new topics' list. <Topic>
 * @see Topic
 */
- (void)topicsListUpdated:(NSArray *)topics;

/**
 * Called when new notifications arrived.
 *
 * @param notifications the list of new notifications.
 * @see Notification
 */
- (void)notificationReceived:(NSArray *)notifications;

@end

#endif
