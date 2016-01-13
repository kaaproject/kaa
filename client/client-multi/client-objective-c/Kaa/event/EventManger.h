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

#ifndef Kaa_EventManger_h
#define Kaa_EventManger_h

#import <Foundation/Foundation.h>
#import "EventDelegates.h"
#import "Transactable.h"
#import "BaseEventFamily.h"
#import "TransactionId.h"
#import "EndpointGen.h"

/**
 * Public access interface for events listener resolution request.
 *
 * Use this module to find endpoints which are able to receive events by list of
 * events' fully-qualified names.
 * NOTE: Operations server will respond with list of endpoints which
 * can receive ALL listed event types (FQNs).
 */
@protocol EventListenersResolver

/**
 * Submits an event listeners resolution request
 *
 * @param eventFQNs - list of event class FQNs which have to be supported by endpoint.
 * @param delegate - result delegate
 *
 * @return Request ID of submitted request
 */
- (NSInteger)findEventListeners:(NSArray *)eventFQNs delegate:(id<FindEventListenersDelegate>)delegate;

@end


@protocol EventManager <EventListenersResolver,Transactable>

/**
 * Add event family object which can handle specified event.
 */
- (void)registerEventFamily:(id<BaseEventFamily>)eventFamily;

/**
 * Creates an Event and passes it to OPS
 *
 * @param eventFqn - fully qualified name of the Event
 * @param data     - event data
 * @param target   - event target, nil for event broadcasting.
 */
- (void)produceEvent:(NSString *)eventFQN data:(NSData *)data target:(NSString *)target;

/**
 * Creates an Event and passes it to OPS
 *
 * @param eventFqn - fully qualified name of the Event
 * @param data     - event data
 * @param target   - event target, nil for event broadcasting.
 * @param trxId    - transaction Id of event
 */
- (void)produceEvent:(NSString *)eventFQN data:(NSData *)data target:(NSString *)target transactionId:(TransactionId *)transactionId;

/**
 * Retrieves an event.
 *
 * @param eventFqn - fully qualified name of the Event
 * @param data     - event data
 * @param source   - event source
 */
- (void)onGenericEvent:(NSString *)eventFQN data:(NSData *)data source:(NSString *)source;

/**
 * Called when SyncResponse contains resolved list of endpoints which
 * support FQNs given in a request before.
 *
 * @param response - list of responses <EventListenersResponse>.
 * @see EventListenersResponse
 */
- (void)eventListenersResponseReceived:(NSArray *)response;

/**
 * Adds new event listener requests to the given Sync request.
 *
 * @param request - event sync request.
 * @see EventSyncRequest
 */
- (void)fillEventListenersSyncRequest:(EventSyncRequest *)request;

/**
 * Retrieves and clears list of pending events and removes them from EventManager.
 *
 * @return - list of <Event> objects
 * @see Event
 */
- (NSArray *)pollPendingEvents;

/**
 * Peek but not clear list of pending events and removes them from EventManager.
 *
 * @return - list of <Event> objects
 * @see Event
 */
- (NSArray *)peekPendingEvents;

/**
 * Clears the current manager's state.
 */
- (void)clearState;

/**
 * Restrict manager to use data channel until #releaseDataChannel called
 */
- (void)engageDataChannel;

/**
 * Allow manager to use data channel.
 *
 * @return <b>true</b> if there is data to be sent via data channel<br>
 * <b>false</b> otherwise
 */
- (BOOL)releaseDataChannel;

@end

#endif
