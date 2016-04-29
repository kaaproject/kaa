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

#import "DefaultNotificationTransport.h"
#import "KaaLogging.h"
#import "TopicListHashCalculator.h"

#define TAG @"DefaultNotificationTransport >>>"

@interface DefaultNotificationTransport ()

@property (nonatomic, strong) id<NotificationProcessor> notificationProcessor;
@property (nonatomic, copy) NSComparisonResult (^notificationComparator)(Notification *first, Notification *second);
@property (nonatomic, strong) NSMutableSet *acceptedUnicastNotificationIds;  //<NSString>
@property (nonatomic, strong) NSMutableArray *sentNotificationCommands;      //<SubscriptionCommand>

- (NSArray *)getTopicStates;
- (NSArray *)getUnicastNotificationsFromNotifications:(NSArray *)notifications;
- (NSArray *)getMulticastNotificationsFromNotifications:(NSArray *)notifications;

@end

@implementation DefaultNotificationTransport

- (instancetype)init {
    self = [super init];
    if (self) {
        self.acceptedUnicastNotificationIds = [NSMutableSet set];
        self.sentNotificationCommands = [NSMutableArray array];
        
        self.notificationComparator = ^NSComparisonResult (Notification *first, Notification *second) {
            if ([first.seqNumber.data intValue] > [second.seqNumber.data intValue]) {
                return NSOrderedDescending;
            }
            if ([first.seqNumber.data intValue] < [second.seqNumber.data intValue]) {
                return NSOrderedAscending;
            }
            return NSOrderedSame;
        };
    }
    return self;
}

- (NotificationSyncRequest *)createEmptyNotificationRequest {
    if (!self.clientState) {
        return nil;
    }
    
    NotificationSyncRequest *request = [[NotificationSyncRequest alloc] init];
    NSArray *states = [self getTopicStates];
    if (states) {
        request.topicStates = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0 data:states];
    }
    request.topicListHash = [self.clientState topicListHash];
    return request;
}

- (NotificationSyncRequest *)createNotificationRequest {
    if (!self.clientState) {
        return nil;
    }
    
    NotificationSyncRequest *request = [[NotificationSyncRequest alloc] init];
    if ([self.acceptedUnicastNotificationIds count] > 0) {
        DDLogInfo(@"%@ Accepted unicast Notifications: %li", TAG,
                  (long)[self.acceptedUnicastNotificationIds count]);
        request.acceptedUnicastNotifications = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0
                                                                 data:[self.acceptedUnicastNotificationIds allObjects]];
    }
    NSArray *states = [self getTopicStates];
    if (states) {
         request.topicStates = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0 data:states];   
    }
    request.topicListHash = [self.clientState topicListHash];
    request.subscriptionCommands = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0
                                                     data:self.sentNotificationCommands];
    return request;
}

- (void)onNotificationResponse:(NotificationSyncResponse *)response {
    if (!self.notificationProcessor || !self.clientState) {
        DDLogWarn(@"%@ Unable to process NotificationSyncResponse: invalid params", TAG);
        return;
    }
    
    if (response.responseStatus == SYNC_RESPONSE_STATUS_NO_DELTA) {
        [self.acceptedUnicastNotificationIds removeAllObjects];
    } else if (response.availableTopics
               && response.availableTopics.branch == KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0) {
        NSArray *topics = response.availableTopics.data;
        
        [self.clientState setTopicListHash:[TopicListHashCalculator calculateTopicListHash:topics]];
        [self.notificationProcessor topicsListUpdated:topics];
    }
    
    for (SubscriptionCommand *subscriptionCommand in _sentNotificationCommands) {
        if (subscriptionCommand.command == SUBSCRIPTION_COMMAND_TYPE_ADD) {
            [self.clientState addSubscriptionForTopicWithId:subscriptionCommand.topicId];
        } else if (subscriptionCommand.command == SUBSCRIPTION_COMMAND_TYPE_REMOVE) {
            [self.clientState removeSubscriptionForTopicWithId:subscriptionCommand.topicId];
        }
    }
    
    if (response.notifications && response.notifications.branch == KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0) {
        NSArray *notifications = response.notifications.data;
        NSMutableArray *newNotifications = [NSMutableArray array];
        
        NSArray *unicastNotifications = [self getUnicastNotificationsFromNotifications:notifications];
        NSArray *multicastNotifications = [self getMulticastNotificationsFromNotifications:notifications];
        
        for (Notification *notification in unicastNotifications) {
            DDLogInfo(@"%@ Received unicast: %@", TAG, notification);
            if (!notification.uid || notification.uid.branch == KAA_UNION_STRING_OR_NULL_BRANCH_1) {
                DDLogWarn(@"%@ No UID for notification with topic id: %lld", TAG, notification.topicId);
                continue;
            }
            if ([self.acceptedUnicastNotificationIds containsObject:notification.uid.data]) {
                DDLogInfo(@"%@ Notification with uid [%@] was already received", TAG, notification.uid.data);
            } else {
                [self.acceptedUnicastNotificationIds addObject:notification.uid.data];
                [newNotifications addObject:notification];
            }
        }
        
        for (Notification *notification in multicastNotifications) {
            DDLogInfo(@"%@ Received multicast: %@", TAG, notification);
            if (!notification.seqNumber || notification.seqNumber.branch == KAA_UNION_INT_OR_NULL_BRANCH_1) {
                DDLogWarn(@"%@ No seq.num for notification with topicId: %lld", TAG, notification.topicId);
                continue;
            }
            NSNumber *seqNumber = notification.seqNumber.data;
            if ([self.clientState updateSubscriptionInfoForTopicId:notification.topicId sequence:[seqNumber intValue]]) {
                [newNotifications addObject:notification];
            } else {
                DDLogInfo(@"%@ Notification with seq number [%i] was already received", TAG, [seqNumber intValue]);
            }
        }
        
        [self.notificationProcessor notificationsReceived:newNotifications];
    }
    
    @synchronized(self.sentNotificationCommands) {
        [self.sentNotificationCommands removeAllObjects];
    }
    
    [self syncAck:response.responseStatus];
    
    DDLogInfo(@"%@ Processed notification response", TAG);
}

- (void)onSubscriptionChangedWithCommands:(NSArray *)commands {
    @synchronized(self.sentNotificationCommands) {
        [self.sentNotificationCommands addObjectsFromArray:commands];
    }
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_NOTIFICATION;
}

- (NSArray *)getUnicastNotificationsFromNotifications:(NSArray *)notifications {
    NSMutableArray *result = [NSMutableArray array];
    for (Notification *notification in notifications) {
        if (notification.uid && notification.uid.branch == KAA_UNION_STRING_OR_NULL_BRANCH_0) {
            [result addObject:notification];
        }
    }
    return result;
}

- (NSArray *)getMulticastNotificationsFromNotifications:(NSArray *)notifications {
    NSMutableArray *result = [NSMutableArray array];
    for (Notification *notification in notifications) {
        if (!notification.uid || notification.uid.branch == KAA_UNION_STRING_OR_NULL_BRANCH_1) {
            [result addObject:notification];
        }
    }
    return [result sortedArrayUsingComparator:self.notificationComparator];
}

- (NSMutableArray *)getTopicStates {
    NSMutableArray *states = nil;
    NSDictionary *nfSubscriptions = [self.clientState getNotificationSubscriptions];
    if ([nfSubscriptions count] > 0) {
        states = [NSMutableArray array];
        DDLogInfo(@"%@ Topic States:", TAG);
        for (NSNumber *key in nfSubscriptions.allKeys) {
            TopicState *state = [[TopicState alloc] init];
            state.topicId = [key longValue];
            state.seqNumber = [nfSubscriptions[key] intValue];
            [states addObject:state];
            DDLogInfo(@"%@ %lld : %i", TAG, state.topicId, state.seqNumber);
        }
    }
    return states;
}

@end
