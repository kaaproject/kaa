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

#import "DefaultEventTransport.h"
#import "KaaLogging.h"

#define TAG @"DefaultEventTransport >>>"

@interface DefaultEventTransport ()

@property (nonatomic,strong) NSMutableDictionary *pendingEvents;    //<NSNumber(int), NSMutableSet<Event>> as key-value
@property (copy) NSComparisonResult (^eventSNComparator)(Event *first, Event *second);
@property (nonatomic,strong) id<KaaClientState> kaaClientState;
@property (nonatomic,strong) id<EventManager> kaaEventManager;

@property (nonatomic) BOOL isEventSNSynchronized;
@property (atomic) int startEventSN;

@end

@implementation DefaultEventTransport

- (instancetype)initWithState:(id<KaaClientState>)state {
    self = [super init];
    if (self) {
        self.kaaClientState = state;
        self.startEventSN = [self.kaaClientState eventSequenceNumber];
        self.pendingEvents = [NSMutableDictionary dictionary];
        
        self.eventSNComparator = ^NSComparisonResult (Event *first, Event *second) {
            return first.seqNum - second.seqNum;
        };
    }
    return self;
}

- (EventSyncRequest *)createEventRequest:(int32_t)requestId {
    if (!self.kaaEventManager) {
        DDLogError(@"%@ Can't create EventSyncRequest because event manager is nil", TAG);
        return nil;
    }
    
    EventSyncRequest *request = [[EventSyncRequest alloc] init];
    [self.kaaEventManager fillEventListenersSyncRequest:request];
    
    if (self.isEventSNSynchronized) {
        NSMutableSet *eventsSet = [NSMutableSet set];
        
        if ([self.pendingEvents count] > 0) {
            for (NSNumber *key in self.pendingEvents.allKeys) {
                NSMutableSet *value = [self.pendingEvents objectForKey:key];
                DDLogDebug(@"%@ Have not received response for %li events sent with request id %i",
                           TAG, (long)[value count], [key intValue]);
                [eventsSet unionSet:value];
            }
            
        }
        
        [eventsSet addObjectsFromArray:[self.kaaEventManager pollPendingEvents]];
        
        if ([eventsSet count] > 0) {
            NSArray *sortedEvents = [[eventsSet allObjects] sortedArrayUsingComparator:self.eventSNComparator];
            DDLogDebug(@"%@ Going to send events bundle with size: %li", TAG, (long)[sortedEvents count]);
            request.events = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0 andData:sortedEvents];
            [self.pendingEvents setObject:eventsSet forKey:[NSNumber numberWithInt:requestId]];
        }
        
        request.eventSequenceNumberRequest = [KAAUnion unionWithBranch:KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_1];
    } else {
        request.eventSequenceNumberRequest = [KAAUnion unionWithBranch:KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_0
                                                               andData:[[EventSequenceNumberRequest alloc] init]];
        DDLogVerbose(@"%@ Sending event sequence number request: restored SN = %i", TAG, self.startEventSN);
    }
    return request;
}

- (void)onEventResponse:(EventSyncResponse *)response {
    if (!self.kaaEventManager) {
        DDLogError(@"%@ Can't process EventSyncResponse because Event Manager is nil", TAG);
        return;
    }
    
    if (!self.isEventSNSynchronized && response.eventSequenceNumberResponse
        && response.eventSequenceNumberResponse.branch == KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_0) {
        EventSequenceNumberResponse *seqNumResponse = response.eventSequenceNumberResponse.data;
        int expectedSN = seqNumResponse.seqNum > 0 ? seqNumResponse.seqNum + 1 : seqNumResponse.seqNum;
        
        if (self.startEventSN != expectedSN) {
            self.startEventSN = expectedSN;
            [self.kaaClientState setEventSequenceNumber:self.startEventSN];
            
            NSMutableSet *eventsSet = [NSMutableSet set];
            for (NSMutableSet *events in self.pendingEvents.allValues) {
                [eventsSet unionSet:events];
            }
            
            [eventsSet addObjectsFromArray:[self.kaaEventManager peekPendingEvents]];
            
            NSArray *sortedEvents = [[eventsSet allObjects] sortedArrayUsingComparator:self.eventSNComparator];
            
            [self.kaaClientState setEventSequenceNumber:(self.startEventSN + [sortedEvents count])];
            if ([sortedEvents count] > 0 && ((Event *)[sortedEvents objectAtIndex:0]).seqNum != self.startEventSN) {
                DDLogInfo(@"%@ Put in order event sequence numbers (expected: %i, actual: %i)",
                          TAG, self.startEventSN, ((Event *)[sortedEvents objectAtIndex:0]).seqNum);
                for (Event *event in sortedEvents) {
                    event.seqNum = self.startEventSN++;
                }
            } else {
                self.startEventSN += [sortedEvents count];
            }
            
            DDLogInfo(@"%@ Event sequence number isn't synchronized. Set to %i", TAG, self.startEventSN);
        } else {
            DDLogInfo(@"%@ Event sequence number is up to date: %i", TAG, self.startEventSN);
        }
        
        self.isEventSNSynchronized = YES;
    }
    
    if (response.events && response.events.branch == KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0
        && [response.events.data count] > 0) {
        NSArray *events = response.events.data;
        NSArray *sortedEvents = [events sortedArrayUsingComparator:self.eventSNComparator];
        for (Event *event in sortedEvents) {
            if (!event.source && event.source.branch != KAA_UNION_STRING_OR_NULL_BRANCH_0) {
                DDLogWarn(@"%@ Can't process event with source nil. Sequence number: %i", TAG, event.seqNum);
                continue;
            }
            NSString *eventSource = event.source.data;
            [self.kaaEventManager onGenericEvent:event.eventClassFQN data:event.eventData source:eventSource];
        }
    }
    
    if (response.eventListenersResponses
        && response.eventListenersResponses.branch == KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_0
        && [response.eventListenersResponses.data count] > 0) {
        [self.kaaEventManager eventListenersResponseReceived:response.eventListenersResponses.data];
    }
    
    DDLogVerbose(@"%@ Processed event response", TAG);
}

- (void)setEventManager:(id<EventManager>)eventManager {
    self.kaaEventManager = eventManager;
}

//TODO revisit for performance improvement (inner cycles)
- (void)onSyncResposeIdReceived:(int32_t)requestId {
    DDLogDebug(@"%@ Events sent with request id %li were accepted", TAG, (long)requestId);
    NSNumber *key = [NSNumber numberWithInt:(int)requestId];
    NSMutableSet *acceptedEvents = [self.pendingEvents objectForKey:key];
    if (acceptedEvents) {
        [self.pendingEvents removeObjectForKey:key];
        NSMutableArray *discardedItems = [NSMutableArray array];
        for (NSNumber *key in self.pendingEvents.allKeys) {
            NSMutableSet *value = [self.pendingEvents objectForKey:key];
            for (Event *acceptedEvent in acceptedEvents) {
                [value removeObject:acceptedEvent];
            }
            if ([value count] == 0) {
                DDLogDebug(@"%@ Remove entry for request with id: %li", TAG, (long)requestId);
                [discardedItems addObject:key];
            }
        }
        for (NSNumber *key in discardedItems) {
            [self.pendingEvents removeObjectForKey:key];
        }
    }
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_EVENT;
}

- (void)blockEventManager {
    if (self.kaaEventManager) {
        [self.kaaEventManager engageDataChannel];
    }
}

- (void)releaseEventManager {
    if (self.kaaEventManager) {
        if ([self.kaaEventManager releaseDataChannel]) {
            [self sync];
        }
    }
}

@end
