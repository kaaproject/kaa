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

package org.kaaproject.kaa.client.event;

import org.kaaproject.kaa.client.transact.Transactable;
import org.kaaproject.kaa.client.transact.TransactionId;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;

import java.util.List;

/**
 * Interface for event management.
 *
 * @author Taras Lemkin
 */
public interface EventManager extends EventListenersResolver, Transactable {

  /**
   * Add event family object which can handle specified event.
   *
   * @param eventFamily EventFamily instance
   * @see EventFamily
   */
  void registerEventFamily(EventFamily eventFamily);

  /**
   * Creates an Event and passes it to OPS
   *
   * @param eventFqn Fully qualified name of the Event
   * @param data     Event data
   * @param target   Event target, null for event broadcasting.
   */
  void produceEvent(String eventFqn, byte[] data, String target);

  /**
   * Creates an Event and passes it to OPS
   *
   * @param eventFqn Fully qualified name of the Event
   * @param data     Event data
   * @param target   Event target, null for event broadcasting.
   * @param trxId    Transaction Id of event
   */
  void produceEvent(String eventFqn, byte[] data, String target, TransactionId trxId);

  /**
   * Retrieves an event.
   *
   * @param eventFqn Fully qualified name of the Event
   * @param data     Event data
   * @param source   Event source
   */
  void onGenericEvent(String eventFqn, byte[] data, String source);

  /**
   * Called when SyncResponse contains resolved list of endpoints which
   * support FQNs given in a request before.
   *
   * @param response List of responses.
   * @see EventListenersResponse
   */
  void eventListenersResponseReceived(List<EventListenersResponse> response);

  /**
   * Adds new event listener requests to the given Sync request.
   *
   * @param request the Event sync request.
   * @see EventSyncRequest
   */
  void fillEventListenersSyncRequest(EventSyncRequest request);

  /**
   * Retrieves and clears list of pending events and removes them from EventManager.
   *
   * @return List of Event objects
   * @see Event
   */
  List<Event> pollPendingEvents();

  /**
   * Peek but not clear list of pending events and removes them from EventManager.
   *
   * @return List of Event objects
   * @see Event
   */
  List<Event> peekPendingEvents();


  /**
   * Clears the current manager's state.
   */
  void clearState();

  /**
   * Restrict manager to use data channel until {@link #releaseDataChannel()} called.
   */
  void engageDataChannel();

  /**
   * Allow manager to use data channel.
   *
   * @return <b>true</b> if there is data to be sent via data channel<br> <b>false</b> otherwise
   */
  boolean releaseDataChannel();
}
