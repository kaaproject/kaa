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

#import "DefaultEventManager.h"
#import "EventListenersRequestBinding.h"
#import "EndpointGen.h"
#include <stdlib.h>
#import "KaaLogging.h"

#define TAG @"DefaultEventManager >>>"

@interface DefaultEventManager ()

@property (nonatomic, strong) id<KaaClientState> state;;
@property (nonatomic, strong) id<ExecutorContext> executorContext;
@property (nonatomic, strong) id<EventTransport> transport;

@property (nonatomic, strong) NSMutableSet *registeredEventFamilies;         //<BaseEventFamily>
@property (nonatomic, strong) NSMutableArray *currentEvents;                 //<Event>
@property (nonatomic, strong) NSMutableDictionary *eventListenersRequests;   //<NSNumber, EventListenersRequestBinding>
@property (nonatomic, strong) NSMutableDictionary *transactions;             //<TransactionId, NSArray<Event>>

@property (nonatomic) BOOL isEngaged;
@property (nonatomic) int requestId;

@property (nonatomic, strong) NSObject *eventGuard;
@property (nonatomic, strong) NSObject *transactionsGuard;

- (NSMutableArray *)getAndClearPendingEvents:(BOOL)clear;

@end

@implementation DefaultEventManager

- (instancetype)initWithState:(id<KaaClientState>)state
              executorContext:(id<ExecutorContext>)executorContext
               eventTransport:(id<EventTransport>)transport {
    self = [super init];
    if (self) {
        self.state = state;
        self.executorContext = executorContext;
        self.transport = transport;
        
        self.registeredEventFamilies = [NSMutableSet set];
        self.currentEvents = [NSMutableArray array];
        self.eventListenersRequests = [NSMutableDictionary dictionary];
        self.transactions = [NSMutableDictionary dictionary];
        
        self.requestId = 0;
        self.isEngaged = NO;
        self.eventGuard = [[NSObject alloc] init];
        self.transactionsGuard = [[NSObject alloc] init];
    }
    return self;
}

- (void)fillEventListenersSyncRequest:(EventSyncRequest *)request {
    if ([self.eventListenersRequests count] > 0) {
        DDLogDebug(@"%@ Unresolved eventListenersResolution request count: %li",
                   TAG, (long)[self.eventListenersRequests count]);
        NSMutableArray *requests = [NSMutableArray array];
        for (EventListenersRequestBinding *bind in self.eventListenersRequests.allValues) {
            if (!bind.isSent) {
                [requests addObject:bind.request];
                bind.isSent = YES;
            }
        }
        request.eventListenersRequests = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0 data:requests];
    }
}

- (void)clearState {
    @synchronized(self.eventGuard) {
        [self.currentEvents removeAllObjects];
    }
}

- (void)produceEventWithFQN:(NSString *)eventFQN data:(NSData *)data target:(NSString *)target {
    [self produceEventWithFQN:eventFQN data:data target:target transactionId:nil];
}

- (void)produceEventWithFQN:(NSString *)eventFQN data:(NSData *)data target:(NSString *)target transactionId:(TransactionId *)transactionId {
    if (transactionId) {
        DDLogInfo(@"%@ Adding event [eventClassFQN: %@, target: %@] to transaction %@", TAG, eventFQN, (target ?: @"broadcast"), transactionId);
        @synchronized(self.transactionsGuard) {
            NSMutableArray *events = self.transactions[transactionId];
            if (events) {
                Event *event = [[Event alloc] init];
                event.seqNum = -1;
                event.eventClassFQN = eventFQN;
                event.eventData = [NSData dataWithData:data];
                if (target) {
                    event.target = [KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0 data:target];
                }
                [events addObject:event];
            } else {
                DDLogWarn(@"%@ Transaction with id %@ is missing. Ignoring event.", TAG, transactionId);
            }
        }
    } else {
        DDLogInfo(@"%@ Producing event [eventClassFQN: %@, target: %@]", TAG, eventFQN, (target ?: @"broadcast"));
        @synchronized(self.eventGuard) {
            Event *event = [[Event alloc] init];
            event.seqNum = [self.state getAndIncrementEventSequenceNumber];
            event.eventClassFQN = eventFQN;
            event.eventData = [NSData dataWithData:data];
            if (target) {
                event.target = [KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0 data:target];
            }
            [self.currentEvents addObject:event];
        }
        
        if (!self.isEngaged) {
            [self.transport sync];
        }
    }
}

- (void)registerEventFamily:(id<BaseEventFamily>)eventFamily {
    [self.registeredEventFamilies addObject:eventFamily];
}

- (void)onGenericEvent:(NSString *)eventFQN withData:(NSData *)data fromSource:(NSString *)source {
    DDLogInfo(@"%@ Received event [eventClassFQN: %@]", TAG, eventFQN);
    for (id<BaseEventFamily> family in self.registeredEventFamilies) {
        DDLogInfo(@"%@ Lookup event fqn %@ in family %@", TAG, eventFQN, family);
        if ([[family getSupportedEventFQNs] containsObject:eventFQN]) {
            DDLogInfo(@"%@ Event fqn [%@] found in family [%@]", TAG, eventFQN, family);
            [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                [family onGenericEvent:eventFQN withData:data fromSource:source];
            }];
        }
    }
}

- (int32_t)requestListenersForEventFQNs:(NSArray *)eventFQNs delegate:(id<FindEventListenersDelegate>)delegate {
    int32_t requestId = self.requestId++;
    EventListenersRequest *request = [[EventListenersRequest alloc] init];
    request.requestId = requestId;
    request.eventClassFQNs = eventFQNs;
    EventListenersRequestBinding *bind = [[EventListenersRequestBinding alloc] initWithRequest:request delegate:delegate];
    self.eventListenersRequests[@(requestId)] = bind;
    DDLogDebug(@"%@ Adding event listener resolution request. Request ID: %i", TAG, requestId);
    if (!self.isEngaged) {
        [self.transport sync];
    }
    return requestId;
}

- (void)eventListenersResponseReceived:(NSArray *)response {
    for (EventListenersResponse *singleResponse in response) {
        DDLogDebug(@"%@ Received event listener resolution response: %@", TAG, singleResponse);
        __block EventListenersRequestBinding *bind = self.eventListenersRequests[@(singleResponse.requestId)];
        if (bind) {
            [self.eventListenersRequests removeObjectForKey:@(singleResponse.requestId)];
            [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                if (singleResponse.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
                    [bind.delegate onEventListenersReceived:((NSArray *)singleResponse.listeners.data)];
                } else {
                    [bind.delegate onRequestFailed];
                }
            }];
        }
    }
}

- (NSArray *)pollPendingEvents {
    return [self getAndClearPendingEvents:YES];
}

- (NSArray *)peekPendingEvents {
    return [self getAndClearPendingEvents:NO];
}

- (NSMutableArray *)getAndClearPendingEvents:(BOOL)clear {
    @synchronized(self.eventGuard) {
        NSMutableArray *pendingEvents = [NSMutableArray arrayWithArray:self.currentEvents];
        if (clear) {
            [self.currentEvents removeAllObjects];
        }
    return pendingEvents;
    }
}

- (TransactionId *)beginTransaction {
    TransactionId *trxId = [[TransactionId alloc] init];
    @synchronized(self.transactionsGuard) {
        if (!self.transactions[trxId]) {
            DDLogDebug(@"%@ Creating events transaction with id [%@]", TAG, trxId);
            self.transactions[trxId] = [NSMutableArray array];
        }
    }
    return trxId;
}

- (void)commitTransactionWithId:(TransactionId *)trxId {
    DDLogDebug(@"%@ Committing events transaction with id [%@]", TAG, trxId);
    @synchronized(self.transactionsGuard) {
        NSArray *eventsToCommit = self.transactions[trxId];
        if (eventsToCommit) {
            [self.transactions removeObjectForKey:trxId];
            
            @synchronized(self.eventGuard) {
                for (Event *event in eventsToCommit) {
                    event.seqNum = [self.state getAndIncrementEventSequenceNumber];
                    [self.currentEvents addObject:event];
                }
            }
        }
        if (!self.isEngaged) {
            [self.transport sync];
        }
    }
}

- (void)rollbackTransactionWithId:(TransactionId *)trxId {
    DDLogDebug(@"%@ Rolling back events transaction with id %@", TAG, trxId);
    @synchronized(self.transactionsGuard) {
        NSMutableArray *eventsToRemove = self.transactions[trxId];
        if (eventsToRemove) {
            [self.transactions removeObjectForKey:trxId];
            for (Event *event in eventsToRemove) {
                DDLogVerbose(@"%@ Removing event %@", TAG, event);
            }
        } else {
            DDLogDebug(@"%@ Transaction with id [%@] was not created", TAG, trxId);
        }
    }
}

- (void)engageDataChannel {
    @synchronized (self) {
        self.isEngaged = YES;
    }
}

- (BOOL)releaseDataChannel {
    @synchronized (self) {
        self.isEngaged = NO;
        BOOL needSync = [self.currentEvents count] > 0;
        if (!needSync) {
            for (EventListenersRequestBinding *bind in self.eventListenersRequests.allValues) {
                needSync |= !bind.isSent;
            }
        }
        return needSync;
    }
}

@end
