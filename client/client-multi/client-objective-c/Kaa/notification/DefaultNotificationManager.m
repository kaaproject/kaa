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

#import "DefaultNotificationManager.h"
#import "NotificationCommon.h"
#import "EndpointGen.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"DefaultNotificationManager >>>"

@interface DefaultNotificationManager ()

@property (nonatomic, strong) id<KaaClientState> state;
@property (nonatomic, strong) id<ExecutorContext> context;
@property (nonatomic, strong) volatile id<NotificationTransport> transport;

@property (nonatomic, strong) NSMutableDictionary *topics;               //<int64_t,Topic> as key-value
@property (nonatomic, strong) NSMutableSet *mandatoryListeners;          //<NotificationDelegate>
@property (nonatomic, strong) NSMutableDictionary *optionalListeners;    //<int64_t,NSArray<NotificationDelegate>> as key-value
@property (nonatomic, strong) NSMutableSet *topicsListeners;             //<NotificationTopicListDelegate>
@property (nonatomic, strong) NSMutableArray *subscriptionInfo;          //<SubscriptionCommand>
@property (nonatomic, strong) NotificationDeserializer *deserializer;

- (Topic *)findTopicById:(int64_t)topicId;
- (void)updateSubscriptionInfoForTopicId:(int64_t)topicId commandType:(SubscriptionCommandType)commandType;
- (void)updateSubscriptions:(NSArray *)subscriptionUpdate;
- (void)notifyDelegates:(NSArray *)delegates forTopic:(Topic *)topic notification:(Notification *)notification;
- (void)performSync;

@end

@implementation DefaultNotificationManager

- (instancetype)initWithState:(id<KaaClientState>)state
              executorContext:(id<ExecutorContext>)context
        notificationTransport:(id<NotificationTransport>)transport {
    self = [super init];
    if (self) {
        self.state = state;
        self.context = context;
        self.transport = transport;
        
        self.topics = [NSMutableDictionary dictionary];
        self.mandatoryListeners = [NSMutableSet set];
        self.optionalListeners = [NSMutableDictionary dictionary];
        self.topicsListeners = [NSMutableSet set];
        self.subscriptionInfo = [NSMutableArray array];
        self.deserializer = [[NotificationDeserializer alloc] initWithExecutorContext:context];
        
        NSArray *topicList = [state getTopics];
        if (topicList) {
            for (Topic *topic in topicList) {
                self.topics[@(topic.id)] = topic;
            }
        }
    }
    return self;
}

- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate {
    if (!delegate) {
        DDLogWarn(@"%@ Failed to add notification delegate: nil", TAG);
        [NSException raise:NSInvalidArgumentException format:@"Nil notification delegate"];
    }
    
    @synchronized (self.mandatoryListeners) {
        [self.mandatoryListeners addObject:delegate];
    }
}

- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate {
    if (!delegate) {
        DDLogWarn(@"%@ Failed to remove notification delegate: nil", TAG);
        [NSException raise:NSInvalidArgumentException format:@"Nil notification delegate"];
    }
    @synchronized (self.mandatoryListeners) {
        [self.mandatoryListeners removeObject:delegate];
    }
}

- (void)addTopicListDelegate:(id<NotificationTopicListDelegate>)delegate {
    if (!delegate) {
        DDLogWarn(@"%@ Failed to add topic list delegate: nil", TAG);
        [NSException raise:NSInvalidArgumentException format:@"Nil topic list delegate"];
    }
    @synchronized (self.topicsListeners) {
        [self.topicsListeners addObject:delegate];
    }
}

- (void)removeTopicListDelegate:(id<NotificationTopicListDelegate>)delegate {
    if (!delegate) {
        DDLogWarn(@"%@ Failed to remove topic list delegate: nil", TAG);
        [NSException raise:NSInvalidArgumentException format:@"Nil topic list delegate"];
    }
    @synchronized (self.topicsListeners) {
        [self.topicsListeners removeObject:delegate];
    }
}

- (NSArray *)getTopics {
    @synchronized (self.topics) {
        return self.topics.allValues;
    }
}

- (void)subscribeToTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync {
    Topic *topic = [self findTopicById:topicId];
    if (topic.subscriptionType != SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION) {
        DDLogWarn(@"%@ Failed to subscribe: topic [%lld] isn't optional", TAG, topicId);
        [NSException raise:KaaUnavailableTopic format:@"Topic [%lld] isn't optional", topicId];
    }
    
    [self updateSubscriptionInfoForTopicId:topicId commandType:SUBSCRIPTION_COMMAND_TYPE_ADD];
    
    if (forceSync) {
        [self performSync];
    }
}

- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync {
    NSMutableArray *subscriptionUpdate = [NSMutableArray array];
    for (NSNumber *topicId in topicIds) {
        Topic *topic = [self findTopicById:[topicId longValue]];
        if (topic.subscriptionType != SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION) {
            DDLogWarn(@"%@ Failed to subscribe: topic [%ld] isn't optional", TAG, [topicId longValue]);
            [NSException raise:KaaUnavailableTopic format:@"Topic [%ld] isn't optional", [topicId longValue]];
        }
        SubscriptionCommand *subscriptionCommand = [[SubscriptionCommand alloc] init];
        subscriptionCommand.topicId = [topicId longValue];
        subscriptionCommand.command = SUBSCRIPTION_COMMAND_TYPE_ADD;
        [subscriptionUpdate addObject:subscriptionCommand];
    }
    
    [self updateSubscriptions:subscriptionUpdate];
    
    if (forceSync) {
        [self performSync];
    }
}

- (void)unsubscribeFromTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync {
    Topic *topic = [self findTopicById:topicId];
    if (topic.subscriptionType != SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION) {
        DDLogWarn(@"%@ Failed to unsubscribe: topic [%lld] isn't optional", TAG, topicId);
        [NSException raise:KaaUnavailableTopic format:@"Topic [%@] isn't optional", @(topicId)];
    }
    
    [self updateSubscriptionInfoForTopicId:topicId commandType:SUBSCRIPTION_COMMAND_TYPE_REMOVE];
    
    if (forceSync) {
        [self performSync];
    }
}

- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync {
    NSMutableArray *subscriptionUpdate = [NSMutableArray array];
    for (NSNumber *topicId in topicIds) {
        Topic *topic = [self findTopicById:[topicId longValue]];
        if (topic.subscriptionType != SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION) {
            DDLogWarn(@"%@ Failed to unsubscribe: topic [%ld] isn't optional", TAG, [topicId longValue]);
            [NSException raise:KaaUnavailableTopic format:@"Topic [%@] isn't optional", topicId];
        }
        SubscriptionCommand *subscriptionCommand = [[SubscriptionCommand alloc] init];
        subscriptionCommand.topicId = [topicId longValue];
        subscriptionCommand.command = SUBSCRIPTION_COMMAND_TYPE_REMOVE;
        [subscriptionUpdate addObject:subscriptionCommand];
    }
    
    [self updateSubscriptions:subscriptionUpdate];
    
    if (forceSync) {
        [self performSync];
    }
}

- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId {
    if (!delegate) {
        DDLogWarn(@"%@ Failed to add delegate: id: %lld, delegate: %@", TAG, topicId, delegate);
        [NSException raise:NSInvalidArgumentException format:@"Bad delegate data"];
    }
    
    [self findTopicById:topicId];
    
    @synchronized (self.optionalListeners) {
        NSMutableArray *delegates = self.optionalListeners[@(topicId)];
        if (!delegates) {
            delegates = [NSMutableArray array];
            self.optionalListeners[@(topicId)] = delegates;
        }
        
        [delegates addObject:delegate];
    }
}

- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId {
    if (!delegate) {
        DDLogWarn(@"%@ Failed to remove delegate: id: %lld, delegate: %@", TAG, topicId, delegate);
        [NSException raise:NSInvalidArgumentException format:@"Bad delegate data"];
    }
    
    [self findTopicById:topicId];
    
    @synchronized (self.optionalListeners) {
        NSMutableArray *delegates = self.optionalListeners[@(topicId)];
        if (delegates) {
            [delegates removeObject:delegate];
        }
    }
}

- (void)sync {
    [self performSync];
}

- (void)topicsListUpdated:(NSArray *)topics {
    NSMutableDictionary *newTopics = [NSMutableDictionary dictionary];
    
    @synchronized (self.topics) {
        for (Topic *topic in topics) {
            newTopics[@(topic.id)] = topic;
            if (self.topics[@(topic.id)]) {
                [self.topics removeObjectForKey:@(topic.id)];
            } else {
                [self.state addTopic:topic];
            }
        }
        @synchronized (self.optionalListeners) {
            for (Topic *topic in self.topics.allValues) {
                [self.optionalListeners removeObjectForKey:@(topic.id)];
                [self.state removeTopicId:topic.id];
            }
        }
        self.topics = newTopics;
    }
    
    @synchronized (self.topicsListeners) {
        for (id<NotificationTopicListDelegate> delegate in self.topicsListeners) {
            [[self.context getCallbackExecutor] addOperationWithBlock:^{
                [delegate onListUpdated:topics];
            }];
        }
    }
}

- (void)notificationsReceived:(NSArray *)notifications {
    for (Notification *notification in notifications) {
        @try {
            Topic *topic = [self findTopicById:notification.topicId];
            BOOL hasOwner = NO;
            
            @synchronized (self.optionalListeners) {
                NSArray *delegates = self.optionalListeners[@(topic.id)];
                if (delegates && [delegates count] > 0) {
                    hasOwner = YES;
                    [self notifyDelegates:delegates forTopic:topic notification:notification];
                }
            }
            
            if (!hasOwner) {
                @synchronized (self.mandatoryListeners) {
                    [self notifyDelegates:[self.mandatoryListeners allObjects] forTopic:topic notification:notification];
                }
            }
        }
        @catch (NSException *exception) {
            DDLogWarn(@"%@ Caught exception: %@ reason: %@", TAG, exception.name, exception.reason);
            DDLogWarn(@"%@ Received notification for an unknown topic [id:%lld]", TAG, notification.topicId);
        }
    }
}

- (void)notifyDelegates:(NSArray *)delegates forTopic:(Topic *)topic notification:(Notification *)notification {
    if (notification.body) {
        __weak typeof(self)weakSelf = self;
        NSArray *blockDelegates = [delegates copy];
        [[self.context getCallbackExecutor] addOperationWithBlock:^{
            @try {
                [weakSelf.deserializer notifyDelegates:blockDelegates withTopic:topic data:notification.body];
            }
            @catch (NSException *exception) {
                DDLogError(@"%@ Failed to process notification for topic %lld. Error: %@ reason: %@",
                           TAG, topic.id, exception.name, exception.reason);
            }
        }];
    }
}

- (void)updateSubscriptionInfoForTopicId:(int64_t)topicId commandType:(SubscriptionCommandType)commandType {
    @synchronized (self.subscriptionInfo) {
        SubscriptionCommand *subscriptionCommand = [[SubscriptionCommand alloc] init];
        subscriptionCommand.topicId = topicId;
        subscriptionCommand.command = commandType;
        [self.subscriptionInfo addObject:subscriptionCommand];
    }
}

- (void)updateSubscriptions:(NSArray *)subscriptionUpdate {
    @synchronized (self.subscriptionInfo) {
        [self.subscriptionInfo addObjectsFromArray:subscriptionUpdate];
    }
}

- (Topic *)findTopicById:(int64_t)topicId {
    @synchronized (self.topics) {
        Topic *topic = self.topics[@(topicId)];
        if (!topic) {
            DDLogWarn(@"%@ Failed to find topic: [id:%lld] is unknown", TAG, topicId);
            [NSException raise:KaaUnavailableTopic format:@"Topic id [%lld] is unknown", topicId];
        }
        return topic;
    }
}

- (void)performSync {
    @synchronized (self.subscriptionInfo) {
        [self.transport onSubscriptionChangedWithCommands:self.subscriptionInfo];
        [self.subscriptionInfo removeAllObjects];
        [self.transport sync];
    }
}

@end
