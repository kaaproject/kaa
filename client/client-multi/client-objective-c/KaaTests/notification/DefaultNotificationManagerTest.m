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

#define HC_SHORTHAND
#import <OCHamcrest/OCHamcrest.h>

#define MOCKITO_SHORTHAND
#import <OCMockito/OCMockito.h>

#import <XCTest/XCTest.h>
#import <Kaa/Kaa.h>
#import "TestsHelper.h"

#define UNKNOWN_TOPIC_ID 100500

@interface DefaultNotificationManagerTest : XCTestCase <NotificationTopicListDelegate>

@property (nonatomic, strong) id<ExecutorContext> executorContext;
@property (nonatomic, strong) NSOperationQueue *executor;
@property (nonatomic, strong) AvroBytesConverter *converter;
@property (nonatomic, strong) NSMutableArray *topicsArray;

@end

@implementation DefaultNotificationManagerTest

- (void)onListUpdated:(NSArray *)list {
    KAATestEqual(self.topicsArray, list);
    [self.topicsArray removeAllObjects];
}

- (void)setUp {
    [super setUp];
    self.executorContext = mockProtocol(@protocol(ExecutorContext));
    self.executor = [[NSOperationQueue alloc] init];
    self.converter = [[AvroBytesConverter alloc] init];
    
    [self.topicsArray removeAllObjects];
    
    [given([self.executorContext getApiExecutor]) willReturn:self.executor];
    [given([self.executorContext getCallbackExecutor]) willReturn:self.executor];
    
    NSError *error = nil;
    NSString *storage = [NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES) firstObject];
    NSString *stateFileLocation = [[[NSURL fileURLWithPath:storage] URLByAppendingPathComponent:STATE_FILE_DEFAULT] path];
    [[NSFileManager defaultManager] removeItemAtPath:stateFileLocation error:&error];
}

- (void)tearDown {
    [super tearDown];
    [self.executor cancelAllOperations];
}

- (void)testEmptyTopicList {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    for (Topic *t in [notificationManager getTopics]) {
        NSLog(@"%@", t);
    }
    
    XCTAssertTrue([[notificationManager getTopics] count] == 0);
}

- (void)testTopicsAfterUpdate {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name2";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    
    NSArray *topicsArray = @[topic1, topic2];
    [notificationManager topicsListUpdated:topicsArray];
    
    XCTAssertTrue([[notificationManager getTopics] count] == [topicsArray count]);
}

- (void)testTopicPersistence {
    KaaClientProperties *prop = [TestsHelper getProperties];
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:prop];
    
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name2";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    NSArray *topicsArray = @[topic1, topic2];
    
    [notificationManager topicsListUpdated:topicsArray];
    [state persist];
    
    KaaClientPropertiesState *newState = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    DefaultNotificationManager *newNotificationManager = [[DefaultNotificationManager alloc] initWithState:newState executorContext:self.executorContext notificationTransport:transport];
    
    XCTAssertTrue([[newNotificationManager getTopics] count] == [topicsArray count]);
}

- (void)testTwiceTopicUpdate {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic3 = [[Topic alloc] init];
    topic3.id = 3;
    topic3.name = @"topic_name1";
    topic3.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    NSMutableArray *topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:topicsArray];
    
    [topicsArray removeObject:topic2];
    [topicsArray addObject:topic3];
    
    [notificationManager topicsListUpdated:topicsArray];
    
    NSArray *newTopics = [notificationManager getTopics];
    
    XCTAssertTrue([newTopics count] == [topicsArray count]);
    XCTAssertTrue([newTopics containsObject:topic1]);
    XCTAssertTrue([newTopics containsObject:topic3]);
}

- (void)testAddTopicUpdateListener {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic3 = [[Topic alloc] init];
    topic3.id = 3;
    topic3.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, topic3, nil];
    
    [notificationManager addTopicListDelegate:self];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    [NSThread sleepForTimeInterval:0.5f];
    
    XCTAssertEqual(0, [self.topicsArray count]);
}

- (void)testRemoveTopicUpdateDelegate {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    id<NotificationTopicListDelegate> delegate1 = mockProtocol(@protocol(NotificationTopicListDelegate));
    id<NotificationTopicListDelegate> delegate2 = mockProtocol(@protocol(NotificationTopicListDelegate));
    
    [notificationManager addTopicListDelegate:delegate1];
    [notificationManager addTopicListDelegate:delegate2];
    
    NSArray *topicUpdate = [NSArray array];
    
    [notificationManager topicsListUpdated:topicUpdate];
    [notificationManager removeTopicListDelegate:delegate2];
    [notificationManager topicsListUpdated:topicUpdate];
    
    [NSThread sleepForTimeInterval:2];
    [verifyCount(delegate1, times(2)) onListUpdated:topicUpdate];
    [verifyCount(delegate2, times(1)) onListUpdated:topicUpdate];
}

- (void)testGlobalNotificationDelegates {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    KAADummyNotification *notification = [[KAADummyNotification alloc] init];
    NSData *notificationBody = [self.converter toBytes:notification];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    
    Notification *notification1 = [[Notification alloc] init];
    notification1.topicId = 1;
    notification1.type = NOTIFICATION_TYPE_CUSTOM;
    notification1.seqNumber = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(1)];
    notification1.body = notificationBody;
    
    Notification *notification2 = [[Notification alloc] init];
    notification2.topicId = 2;
    notification2.type = NOTIFICATION_TYPE_CUSTOM;
    notification2.seqNumber = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(1)];
    notification2.body = notificationBody;
    
    NSArray *notificationUpdate = @[notification1, notification2];
    
    id<NotificationDelegate> mandatoryDelegate= mockProtocol(@protocol(NotificationDelegate));
    id<NotificationDelegate> globalDelegate = mockProtocol(@protocol(NotificationDelegate));
    
    [notificationManager addNotificationDelegate:mandatoryDelegate];
    [notificationManager notificationsReceived:notificationUpdate];
    
    [NSThread sleepForTimeInterval:1.f];
    
    [notificationManager removeNotificationDelegate:mandatoryDelegate];
    [notificationManager addNotificationDelegate:globalDelegate];
    
    [notificationManager notificationsReceived:notificationUpdate];
    [notificationManager notificationsReceived:notificationUpdate];
    
    [NSThread sleepForTimeInterval:2.f];
    
    [verifyCount(mandatoryDelegate, times(1)) onNotification:anything() withTopicId:1];
    [verifyCount(mandatoryDelegate, times(1)) onNotification:anything() withTopicId:2];
    [verifyCount(globalDelegate, times(2)) onNotification:anything() withTopicId:1];
    [verifyCount(globalDelegate, times(2)) onNotification:anything() withTopicId:2];
}

- (void)testNotificationDelegateOnTopic {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    KAADummyNotification *notification = [[KAADummyNotification alloc] init];
    NSData *notificationBody = [self.converter toBytes:notification];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    
    Notification *notification1 = [[Notification alloc] init];
    notification1.topicId = 1;
    notification1.type = NOTIFICATION_TYPE_CUSTOM;
    notification1.seqNumber = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(1)];
    notification1.body = notificationBody;
    
    Notification *notification2 = [[Notification alloc] init];
    notification2.topicId = 2;
    notification2.type = NOTIFICATION_TYPE_CUSTOM;
    notification2.seqNumber = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(1)];
    notification2.body = notificationBody;
    
    NSArray *notificationUpdate = @[notification1, notification2];
    
    id<NotificationDelegate> globalDelegate= mockProtocol(@protocol(NotificationDelegate));
    id<NotificationDelegate> topicDelegate = mockProtocol(@protocol(NotificationDelegate));
    
    [notificationManager addNotificationDelegate:globalDelegate];
    [notificationManager addNotificationDelegate:topicDelegate forTopicId:2];
    
    [notificationManager notificationsReceived:notificationUpdate];
    [notificationManager removeNotificationDelegate:topicDelegate forTopicId:2];
    [notificationManager notificationsReceived:notificationUpdate];
    
    [NSThread sleepForTimeInterval:1.f];
    
    [verifyCount(globalDelegate, times(2)) onNotification:anything() withTopicId:1];
    [verifyCount(globalDelegate, times(1)) onNotification:anything() withTopicId:2];
    
    [NSThread sleepForTimeInterval:1.f];
    
    [verifyCount(topicDelegate, times(1)) onNotification:anything() withTopicId:2];
}

- (void)testAddDelegateForUnknownTopic {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    
    id<NotificationDelegate> delegate= mockProtocol(@protocol(NotificationDelegate));
    @try {
        [notificationManager addNotificationDelegate:delegate forTopicId:UNKNOWN_TOPIC_ID];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testAddDelegateForUnknownTopic succeeded");
    }
}

- (void)testRemoveDelegateForUnknownTopic {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    
    id<NotificationDelegate> delegate= mockProtocol(@protocol(NotificationDelegate));
    @try {
        [notificationManager removeNotificationDelegate:delegate forTopicId:UNKNOWN_TOPIC_ID];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testRemoveDelegateForUnknownTopic succeeded");
    }
}

- (void)testSubsribeForUnknownTopic1 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    
    @try {
        [notificationManager subscribeToTopicWithId:UNKNOWN_TOPIC_ID forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSubsribeForUnknownTopic1 succeeded");
    }
}

- (void)testSubsribeForUnknownTopic2 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager subscribeToTopicsWithIDs:@[@(1), @(2), @(UNKNOWN_TOPIC_ID)] forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSubsribeForUnknownTopic2 succeeded");
    }
}

- (void)testUnsubsribeForUnknownTopic1 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager unsubscribeFromTopicWithId:UNKNOWN_TOPIC_ID forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSubsribeForUnknownTopic1 succeeded");
    }
}

- (void)testUnsubsribeForUnknownTopic2 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager unsubscribeFromTopicsWithIDs:@[@(1),
                                                            @(2),
                                                            @(UNKNOWN_TOPIC_ID)]
                                                forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testUnsubsribeForUnknownTopic2 succeeded");
    }
}

- (void)testSubscribeOnMandatoryTopic1 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager subscribeToTopicWithId:2 forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSubscribeOnMandatoryTopic1 succeeded");
    }
}

- (void)testSubscribeOnMandatoryTopic2 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager subscribeToTopicsWithIDs:@[@(1), @(2)] forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSubscribeOnMandatoryTopic2 succeeded");
    }
}

- (void)testUnsubscribeFromMandatoryTopic1 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager unsubscribeFromTopicWithId:2 forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testUnsubscribeFromMandatoryTopic1 succeeded");
    }
}

- (void)testUnsubscribeFromMandatoryTopic2 {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, nil];
    [notificationManager topicsListUpdated:self.topicsArray];
    @try {
        [notificationManager unsubscribeFromTopicsWithIDs:@[@(1), @(2)] forceSync:YES];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testUnsubscribeFromMandatoryTopic2 succeeded");
    }
}

- (void)testSuccessSubscriptionToTopic {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<NotificationTransport> transport = mockProtocol(@protocol(NotificationTransport));
    
    DefaultNotificationManager *notificationManager = [[DefaultNotificationManager alloc] initWithState:state executorContext:self.executorContext notificationTransport:transport];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = 1;
    topic1.name = @"topic_name1";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = 2;
    topic2.name = @"topic_name1";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    Topic *topic3 = [[Topic alloc] init];
    topic3.id = 3;
    topic3.name = @"topic_name1";
    topic3.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    self.topicsArray = [NSMutableArray arrayWithObjects:topic1, topic2, topic3, nil];
    
    [notificationManager topicsListUpdated:self.topicsArray];
    [notificationManager subscribeToTopicWithId:1 forceSync:YES];
    
    [verifyCount(transport, times(1)) sync];
    
    [notificationManager subscribeToTopicsWithIDs:@[@(1), @(1)] forceSync:NO];
    [notificationManager unsubscribeFromTopicWithId:1 forceSync:NO];
    
    [verifyCount(transport, times(1)) sync];
    
    [notificationManager sync];
    
    [verifyCount(transport, times(2)) sync];
    
    [notificationManager unsubscribeFromTopicsWithIDs:@[@(1), @(1)] forceSync:YES];
    
    [verifyCount(transport, times(3)) sync];
}

@end
