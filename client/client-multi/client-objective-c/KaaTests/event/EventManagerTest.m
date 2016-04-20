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
#import "BaseEventFamily.h"
#import "KaaClientPropertiesState.h"
#import "EventTransport.h"
#import "ExecutorContext.h"
#import "DefaultEventManager.h"
#import "TestsHelper.h"

#pragma mark - TestFindEventListenersDelegate

@interface TestFindEventListenersDelegate : NSObject <FindEventListenersDelegate>

@end

@implementation TestFindEventListenersDelegate

- (void)onRequestFailed {
}

- (void)onEventListenersReceived:(NSArray *)eventListeners {
#pragma unused(eventListeners)
}


@end

#pragma mark - ConcreteEventFamily

@interface ConcreteEventFamily : NSObject <BaseEventFamily>

@property (nonatomic, strong) NSSet *supportedEventFQNs;
@property (nonatomic) NSInteger eventsCount;

@end

@implementation ConcreteEventFamily

- (instancetype)initWithSupportedFQN:(NSString *)supportedFQN {
    self = [super init];
    if (self) {
        self.eventsCount = 0;
        self.supportedEventFQNs = [NSSet setWithObject:supportedFQN];
    }
    return self;
}

- (NSSet *)getSupportedEventFQNs {
    return self.supportedEventFQNs;
}

- (void)onGenericEvent:(NSString *)eventFQN withData:(NSData *)data fromSource:(NSString *)source {
#pragma unused(eventFQN, data, source)
    self.eventsCount++;
}


@end

#pragma mark - EventManagerTest

@interface EventManagerTest : XCTestCase

@end

@implementation EventManagerTest

- (void)testNoHandler {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<EventTransport> transport = mockProtocol(@protocol(EventTransport));
    id<BaseEventFamily> eventFamily = mockProtocol(@protocol(BaseEventFamily));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    
    id<EventManager> eventManager = [[DefaultEventManager alloc] initWithState:state executorContext:executorContext eventTransport:transport];
    [eventManager registerEventFamily:eventFamily];
    
    [eventManager produceEventWithFQN:@"kaa.test.event.PlayEvent" data:[NSData data] target:nil];
    
    [verifyCount(transport, times(1)) sync];
    [verifyCount(eventFamily, times(0)) getSupportedEventFQNs];
}

- (void)testEngageRelease {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<EventTransport> transport = mockProtocol(@protocol(EventTransport));
    id<BaseEventFamily> eventFamily = mockProtocol(@protocol(BaseEventFamily));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    
    id<EventManager> eventManager = [[DefaultEventManager alloc] initWithState:state executorContext:executorContext eventTransport:transport];
    [eventManager registerEventFamily:eventFamily];
    
    [eventManager produceEventWithFQN:@"kaa.test.event.PlayEvent" data:[NSData data] target:nil];
    [verifyCount(transport, times(1)) sync];
    
    [eventManager engageDataChannel];
    [eventManager produceEventWithFQN:@"kaa.test.event.PlayEvent" data:[NSData data] target:nil];
    [verifyCount(transport, times(1)) sync];
}

- (void)testTransaction {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    id<EventTransport> transport = mockProtocol(@protocol(EventTransport));
    id<BaseEventFamily> eventFamily = mockProtocol(@protocol(BaseEventFamily));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    
    id<EventManager> eventManager = [[DefaultEventManager alloc] initWithState:state executorContext:executorContext eventTransport:transport];
    [eventManager registerEventFamily:eventFamily];
    
    TransactionId *trxId = [eventManager beginTransaction];
    XCTAssertNotNil(trxId);
    
    [eventManager produceEventWithFQN:@"kaa.test.event.PlayEvent" data:[NSData data] target:nil transactionId:trxId];
    [eventManager produceEventWithFQN:@"kaa.test.event.PlayEvent" data:[NSData data] target:nil transactionId:trxId];
    [verifyCount(transport, times(0)) sync];
    
    [eventManager rollbackTransactionWithId:trxId];
    [verifyCount(transport, times(0)) sync];
    
    trxId = [eventManager beginTransaction];
    [eventManager produceEventWithFQN:@"kaa.test.event.PlayEvent" data:[NSData data] target:nil transactionId:trxId];
    [verifyCount(transport, times(0)) sync];
    
    [eventManager commitTransactionWithId:trxId];
    [verifyCount(transport, times(1)) sync];
}

- (void)testOneEventForTwoDifferentFamilies {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    
    id<EventTransport> transport = mockProtocol(@protocol(EventTransport));
    ConcreteEventFamily *eventFamily1 = [[ConcreteEventFamily alloc] initWithSupportedFQN:@"kaa.test.event.PlayEvent"];
    ConcreteEventFamily *eventFamily2 = [[ConcreteEventFamily alloc] initWithSupportedFQN:@"kaa.test.event.StopEvent"];
    
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    
    [given([executorContext getCallbackExecutor]) willReturn:executor];
    
    id<EventManager> eventManager = [[DefaultEventManager alloc] initWithState:state executorContext:executorContext eventTransport:transport];
    [eventManager registerEventFamily:eventFamily1];
    [eventManager registerEventFamily:eventFamily2];
    
    XCTAssertEqual(0, [eventFamily1 eventsCount]);
    XCTAssertEqual(0, [eventFamily2 eventsCount]);
    
    [eventManager onGenericEvent:@"kaa.test.event.PlayEvent" withData:[NSData data] fromSource:nil];
    
    [NSThread sleepForTimeInterval:0.5f];
    
    XCTAssertEqual(1, eventFamily1.eventsCount);
    XCTAssertEqual(0, eventFamily2.eventsCount);
    
    [eventManager onGenericEvent:@"kaa.test.event.StopEvent" withData:[NSData data] fromSource:nil];
    
    [NSThread sleepForTimeInterval:0.5f];
    
    XCTAssertEqual(1, eventFamily1.eventsCount);
    XCTAssertEqual(1, eventFamily2.eventsCount);
    
    [eventManager onGenericEvent:@"kaa.test.event.NoSuchEvent" withData:[NSData data] fromSource:nil];
    
    [NSThread sleepForTimeInterval:0.5f];
    
    XCTAssertEqual(1, eventFamily1.eventsCount);
    XCTAssertEqual(1, eventFamily2.eventsCount);
}

- (void)testFillRequest {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    
    id<EventTransport> transport = mockProtocol(@protocol(EventTransport));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    id<EventManager> eventManager = [[DefaultEventManager alloc] initWithState:state executorContext:executorContext eventTransport:transport];
    
    EventSyncRequest *request = [[EventSyncRequest alloc] init];

    [eventManager produceEventWithFQN:@"kaa.test.event.SomeEvent" data:[NSData data] target:@"theTarget"];
    [eventManager fillEventListenersSyncRequest:request];
    request.events = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0 data:[eventManager pollPendingEvents]];
    
    XCTAssertNotNil(request.events);
    XCTAssertEqual(1, [request.events.data count]);
    XCTAssertEqualObjects(@"kaa.test.event.SomeEvent", [request.events.data[0] eventClassFQN]);
    XCTAssertEqualObjects([NSData data], [request.events.data[0] eventData]);
    
    request = [[EventSyncRequest alloc] init];
    NSArray *eventFQNs = [NSArray arrayWithObject:@"eventFQN1"];
    [eventManager requestListenersForEventFQNs:eventFQNs delegate:[[TestFindEventListenersDelegate alloc] init]];
    [eventManager requestListenersForEventFQNs:eventFQNs delegate:[[TestFindEventListenersDelegate alloc] init]];
    
    [eventManager fillEventListenersSyncRequest:request];
    
    XCTAssertNotNil([request eventListenersRequests]);
    XCTAssertEqual(2, [request.eventListenersRequests.data count]);
    XCTAssertEqualObjects(eventFQNs[0], [[request.eventListenersRequests.data[0] eventClassFQNs] firstObject]);
}

- (void)testEventListenersSyncRequestResponse {
    KaaClientPropertiesState *state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    
    id<EventTransport> transport = mockProtocol(@protocol(EventTransport));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    [given([executorContext getCallbackExecutor]) willReturn:[[NSOperationQueue alloc] init]];
    id<EventManager> eventManager = [[DefaultEventManager alloc] initWithState:state executorContext:executorContext eventTransport:transport];
    
    NSArray *eventFQNs = [NSArray arrayWithObject:@"eventFQN1"];
    
    id<FindEventListenersDelegate> fetchListener = mockProtocol(@protocol(FindEventListenersDelegate));
    int32_t requestIdOk = [eventManager requestListenersForEventFQNs:eventFQNs delegate:fetchListener];
    int32_t requestIdBad = [eventManager requestListenersForEventFQNs:eventFQNs delegate:fetchListener];
    
    [verifyCount(transport, atLeastOnce()) sync];
    
    NSMutableArray *response = [NSMutableArray array];
    
    EventListenersResponse *response1 = [self getNewEventListResponseWithRequestId:(int)requestIdOk
                                                                        resultType:SYNC_RESPONSE_RESULT_TYPE_SUCCESS];
    EventListenersResponse *response2 = [self getNewEventListResponseWithRequestId:(int)requestIdBad
                                                                        resultType:SYNC_RESPONSE_RESULT_TYPE_FAILURE];
    [response addObject:response1];
    [response addObject:response2];
    
    [eventManager eventListenersResponseReceived:response];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(fetchListener, times(1)) onRequestFailed];
    [verifyCount(fetchListener, times(1)) onEventListenersReceived:anything()];
}

#pragma mark - Supporting methods

- (EventListenersResponse *)getNewEventListResponseWithRequestId:(int)requestId resultType:(SyncResponseResultType)resultType {
    EventListenersResponse *response = [[EventListenersResponse alloc] init];
    response.requestId = requestId;
    response.result = resultType;
    response.listeners = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0 data:[NSArray array]];
    return response;
}

@end
