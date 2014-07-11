/*
 * Copyright 2014 CyberVision, Inc.
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

import java.io.IOException;
import java.util.List;

import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;

/**
 * Interface for event management.
 *
 * @author Taras Lemkin
 *
 */
public interface EventManager extends EventListenersResolver {

    /**
     * Add event family object which can handle concrete event.
     *
     * @param eventFamily EventFamily instance
     * @see EventFamily
     */
    void registerEventFamily(EventFamily eventFamily);

    /**
     * Creates an Event and passes it to OPS
     *
     * @param eventFqn  Fully qualified name of the Event
     * @param data      Event data
     * @param target    Event target, null for event broadcasting.
     */
    void produceEvent(String eventFqn, byte[] data, String target) throws IOException;

    /**
     * Retrieves an event.
     *
     * @param eventFqn  Fully qualified name of the Event
     * @param data      Event data
     * @param source    Event source
     */
    void onGenericEvent(String eventFqn, byte[] data, String source);

    /**
     * Called when SyncResponse contains resolved list of endpoints which
     * support FQNs given in a request before.
     *
     * @param response  List of responses.
     * @see EventListenersResponse
     */
    void eventListenersResponseReceived(List<EventListenersResponse> response);

    /**
     * Adds new events and event listener requests to the given Sync request.
     *
     * @param request the Event sync request.
     * @see EventSyncRequest
     */
    void fillEventSyncRequest(EventSyncRequest request);

    /**
     * Clears the current manager's state.
     */
    void clearState();
}
