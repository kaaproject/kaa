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

#import "DefaultNotificationTransport.h"
#import "KaaLogging.h"

#define TAG @"DefaultNotificationTransport >>>"

@interface DefaultNotificationTransport ()

@property (nonatomic, strong) id<NotificationProcessor> notificationProcessor;
@property (nonatomic, copy) NSComparisonResult (^notificationComparator)(Notification *first, Notification *second);
@property (nonatomic, strong) NSMutableSet *acceptedUnicastNotificationIds;  //<NSString>
@property (nonatomic, strong) NSMutableArray *sentNotificationCommands;      //<SubscriptionCommand>

- (NSArray *)getTopicStates;
- (NSArray *)getUnicastNotifications:(NSArray *)notifications;
- (NSArray *)getMulticastNotifications:(NSArray *)notifications;

@end

@implementation DefaultNotificationTransport

- (instancetype)init {
    self = [super init];
    if (self) {
        self.acceptedUnicastNotificationIds = [NSMutableSet set];
        self.sentNotificationCommands = [NSMutableArray array];
        
        self.notificationComparator = ^NSComparisonResult (Notification *first, Notification *second) {
            return [first.seqNumber.data intValue] - [second.seqNumber.data intValue];
        };
    }
    return self;
}

- (NotificationSyncRequest *)createEmptyNotificationRequest {
    if (!self.clientState) {
        return nil;
    }
    
    NotificationSyncRequest *request = [[NotificationSyncRequest alloc] init];
    request.appStateSeqNumber = [self.clientState notificationSequenceNumber];
    NSArray *states = [self getTopicStates];
    if (states) {
        request.topicStates = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0 data:states];
    }
    return request;
}

- (NotificationSyncRequest *)createNotificationRequest {
    if (!self.clientState) {
        return nil;
    }
    
    NotificationSyncRequest *request = [[NotificationSyncRequest alloc] init];
    request.appStateSeqNumber = [self.clientState notificationSequenceNumber];
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
    }
    
    if (response.availableTopics && response.availableTopics.branch == KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0) {
        NSArray *topics = response.availableTopics.data;
        for (Topic *topic in topics) {
            [self.clientState addTopic:topic];
        }
        [self.notificationProcessor topicsListUpdated:topics];
    }
    
    if (response.notifications && response.notifications.branch == KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0) {
        NSArray *notifications = response.notifications.data;
        NSMutableArray *newNotifications = [NSMutableArray array];
        
        NSArray *unicastNotifications = [self getUnicastNotifications:notifications];
        NSArray *multicastNotifications = [self getMulticastNotifications:notifications];
        
        for (Notification *notification in unicastNotifications) {
            DDLogInfo(@"%@ Received unicast: %@", TAG, notification);
            if (!notification.uid || notification.uid.branch == KAA_UNION_STRING_OR_NULL_BRANCH_1) {
                DDLogWarn(@"%@ No UID for notification with topic id: %@", TAG, notification.topicId);
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
                DDLogWarn(@"%@ No seq.num for notification with topicId: %@", TAG, notification.topicId);
                continue;
            }
            NSNumber *seqNumber = notification.seqNumber.data;
            if ([self.clientState updateTopicSubscriptionInfo:notification.topicId sequence:[seqNumber intValue]]) {
                [newNotifications addObject:notification];
            } else {
                DDLogInfo(@"%@ Notification with seq number [%i] was already received", TAG, [seqNumber intValue]);
            }
        }
        
        [self.notificationProcessor notificationReceived:newNotifications];
    }
    
    @synchronized(self.sentNotificationCommands) {
        [self.sentNotificationCommands removeAllObjects];
    }
    [self.clientState setNotificationSequenceNumber:response.appStateSeqNumber];
    
    [self syncAck:response.responseStatus];
    
    DDLogInfo(@"%@ Processed notification response", TAG);
}

- (void)onSubscriptionChanged:(NSArray *)commands {
    @synchronized(self.sentNotificationCommands) {
        [self.sentNotificationCommands addObjectsFromArray:commands];
    }
}

//- (void)setNotificationProcessor:(id<NotificationProcessor>)processor {
//    self.notificationProcessor = processor;
//}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_NOTIFICATION;
}

- (NSArray *)getUnicastNotifications:(NSArray *)notifications {
    NSMutableArray *result = [NSMutableArray array];
    for (Notification *notification in notifications) {
        if (notification.uid && notification.uid.branch == KAA_UNION_STRING_OR_NULL_BRANCH_0) {
            [result addObject:notification];
        }
    }
    return result;
}

- (NSArray *)getMulticastNotifications:(NSArray *)notifications {
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
        for (NSString *key in nfSubscriptions.allKeys) {
            TopicState *state = [[TopicState alloc] init];
            state.topicId = key;
            state.seqNumber = [nfSubscriptions[key] intValue];
            [states addObject:state];
            DDLogInfo(@"%@ %@ : %i", TAG, state.topicId, state.seqNumber);
        }
    }
    return states;
}

@end
