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

@interface DefaultNotificationTransportTest : XCTestCase

@end

@implementation DefaultNotificationTransportTest

- (void)testSyncNegative {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    [transport setClientState:clientState];
    @try {
        [transport sync];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSyncNegativeSucceed. Caught ChannelRuntimeException");
    }
}

- (void)testSync {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    [transport setClientState:clientState];
    [transport setChannelManager:channelManager];
    [transport sync];
    
    [verifyCount(channelManager, times(1)) syncForTransportType:TRANSPORT_TYPE_NOTIFICATION];
}

- (void)testCreateEmptyRequest {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    
    [transport setClientState:clientState];
    
    NotificationSyncRequest *request = [transport createEmptyNotificationRequest];
    
    XCTAssertNil(request.acceptedUnicastNotifications.data);
    XCTAssertNil(request.subscriptionCommands.data);
    XCTAssertEqual([TopicListHashCalculator calculateTopicListHash:nil], request.topicListHash);
}

- (void)testEmptyTopicListHash {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<NotificationProcessor> processor = mockProtocol(@protocol(NotificationProcessor));
    
    int32_t listHash = [TopicListHashCalculator calculateTopicListHash:nil];
    [given([clientState topicListHash]) willReturnInt:listHash];
    
    NotificationSyncResponse *response = [[NotificationSyncResponse alloc] init];
    response.responseStatus = SYNC_RESPONSE_STATUS_DELTA;
    response.availableTopics = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0 data:[NSArray array]];
    
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    
    id<NotificationTransport> notificationTransport = [[DefaultNotificationTransport alloc] init];
    [notificationTransport setChannelManager:channelManager];
    [notificationTransport setNotificationProcessor:processor];
    [notificationTransport setClientState:clientState];
    
    [notificationTransport onNotificationResponse:response];
    
    NotificationSyncRequest *request = [notificationTransport createNotificationRequest];
    XCTAssertEqual(listHash, request.topicListHash);
}

- (void)testTopicListHash {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<NotificationProcessor> processor = mockProtocol(@protocol(NotificationProcessor));
    
    NSArray *topics = @[[[Topic alloc] initWithId:2 name:nil subscriptionType:SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION],
    [[Topic alloc] initWithId:1 name:nil subscriptionType:SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION]];
    
    int32_t listHash = [TopicListHashCalculator calculateTopicListHash:topics];
    
    [given([clientState topicListHash]) willReturnInt:listHash];
    
    NotificationSyncResponse *response = [[NotificationSyncResponse alloc] init];
    response.responseStatus = SYNC_RESPONSE_STATUS_DELTA;
    response.availableTopics = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0 data:topics];
    
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport setNotificationProcessor:processor];
    [transport setClientState:clientState];
    
    [transport onNotificationResponse:response];
    
    NotificationSyncRequest *request = [transport createNotificationRequest];
    XCTAssertEqual(listHash, request.topicListHash);
}


- (void)testAcceptUnicastNotification {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<NotificationProcessor> processor = mockProtocol(@protocol(NotificationProcessor));
    
    NotificationSyncResponse *response1 = [self getNewNotificationResponseWithResponseStatus:SYNC_RESPONSE_STATUS_DELTA];
    
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport setNotificationProcessor:processor];
    [transport setClientState:clientState];
    
    Notification *nf1 = [self getNotificationWithTopicId:1 uid:@"uid1" sequenceNumber:5];
    Notification *nf2 = [self getNotificationWithTopicId:2 uid:@"uid2" sequenceNumber:3];
    Notification *nf3 = [self getNotificationWithTopicId:3 uid:@"uid2" sequenceNumber:5];
    
    [response1 setNotifications:
     [KAAUnion unionWithBranch:KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0 data:@[nf1, nf2, nf3]]];
    [transport onNotificationResponse:response1];
    
    NotificationSyncRequest *request1 = [transport createNotificationRequest];
    XCTAssertTrue([request1.acceptedUnicastNotifications.data count] == 2);
    
    NotificationSyncResponse *response2 = [self getNewNotificationResponseWithResponseStatus:SYNC_RESPONSE_STATUS_NO_DELTA];
    
    [transport onNotificationResponse:response2];
    
    NotificationSyncRequest *request2 = [transport createNotificationRequest];
    XCTAssertNil(request2.acceptedUnicastNotifications.data);
}

- (void)testOnNotificationResponse {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<NotificationProcessor> processor = mockProtocol(@protocol(NotificationProcessor));
    
    NotificationSyncResponse *response = [self getNewNotificationResponseWithResponseStatus:SYNC_RESPONSE_STATUS_DELTA];
    
    int64_t topicId1 = 1;
    int64_t topicId2 = 2;
    
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport onNotificationResponse:response];
    [transport onNotificationResponse:response];
    [transport setNotificationProcessor:processor];
    [transport onNotificationResponse:response];
    [transport setClientState:clientState];
    [transport onNotificationResponse:response];
    
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = topicId1;
    topic1.name = nil;
    topic1.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = topicId2;
    topic2.name = nil;
    topic2.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    NSArray *topics = @[topic1, topic2];
    [response setAvailableTopics:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0 data:topics]];
    
    Notification *nf1 = [self getNotificationWithTopicId:topicId2 uid:@"uid" sequenceNumber:5];
    Notification *nf2 = [self getNotificationWithTopicId:topicId1 sequenceNumber:3];
    Notification *nf3 = [self getNotificationWithTopicId:topicId1 sequenceNumber:6];
    
    NSArray *notificationsArray = @[nf1, nf2, nf3];
    [response setNotifications:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0 data:notificationsArray]];
    
    [transport onNotificationResponse:response];
    
    [verifyCount(processor, times(1)) notificationsReceived:anything()];
    [verifyCount(processor, times(1)) topicsListUpdated:topics];
    [verifyCount(clientState, times(1)) updateSubscriptionInfoForTopicId:topicId1 sequence:3];
    [verifyCount(clientState, times(1)) updateSubscriptionInfoForTopicId:topicId1 sequence:6];
    
    XCTAssertEqualObjects(@"uid", [transport createNotificationRequest].acceptedUnicastNotifications.data[0]);
}

- (void)testFilterStaleNotification {
    id<KaaClientState> state = mockProtocol(@protocol(KaaClientState));
    id<NotificationProcessor> processor = mockProtocol(@protocol(NotificationProcessor));
    
    NotificationSyncResponse *response = [self getNewNotificationResponseWithResponseStatus:SYNC_RESPONSE_STATUS_DELTA];
    
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport setNotificationProcessor:processor];
    [transport setClientState:state];
    
    Notification *nf1 = [self getNotificationWithTopicId:1l sequenceNumber:3];
    Notification *nf2 = [self getNotificationWithTopicId:1l sequenceNumber:3];
    
    NSArray *array = @[nf1, nf2];
    [response setNotifications:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0 data:array]];
    [transport onNotificationResponse:response];
    
    NSArray *expectedNotifications = [NSArray array];
    [verifyCount(processor, times(1)) notificationsReceived:expectedNotifications];
}

- (void)testTopicState {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    
    NSDictionary *nfSubscriptions = [NSDictionary dictionaryWithObjects:@[@"topic1", @"topic2"]
                                                                forKeys:@[@(10), @(3)]];
    
    [given([clientState getNotificationSubscriptions]) willReturn:nfSubscriptions];
    
    id<NotificationTransport> transport = [[DefaultNotificationTransport alloc] init];
    
    XCTAssertNil([transport createEmptyNotificationRequest]);
    
    [transport setClientState:clientState];
    
    NotificationSyncRequest *request = [transport createNotificationRequest];
    
    XCTAssertTrue([request.topicStates.data count] == 2);
}

#pragma mark - Supporting methods

- (NotificationSyncResponse *)getNewNotificationResponseWithResponseStatus:(SyncResponseStatus)responseStatus {
    NotificationSyncResponse *response = [[NotificationSyncResponse alloc] init];
    response.responseStatus = responseStatus;
    return response;
}

- (Notification *)getNotificationWithTopicId:(int64_t)topicId uid:(NSString *)uid sequenceNumber:(int32_t)seqNumber {
    Notification *notification = [[Notification alloc] init];
    notification.topicId = topicId;
    notification.uid = [KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0 data:uid];
    notification.type = NOTIFICATION_TYPE_CUSTOM;
    notification.seqNumber = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(seqNumber)];
    int32_t int123 = 123;
    NSData *data = [NSData dataWithBytes:&int123 length:sizeof(int123)];
    notification.body = data;
    return notification;
}

- (Notification *)getNotificationWithTopicId:(int64_t)topicId sequenceNumber:(int32_t)seqNumber {
    Notification *notification = [[Notification alloc] init];
    notification.topicId = topicId;
    notification.type = NOTIFICATION_TYPE_CUSTOM;
    notification.seqNumber = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(seqNumber)];
    int32_t int123 = 123;
    NSData *data = [NSData dataWithBytes:&int123 length:sizeof(int123)];
    notification.body = data;
    return notification;
}

@end
