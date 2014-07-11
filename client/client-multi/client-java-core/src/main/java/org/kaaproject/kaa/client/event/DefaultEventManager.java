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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link EventManager} implementation.
 *
 * @author Taras Lemkin
 *
 */
public class DefaultEventManager implements EventManager {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultEventManager.class);
    private final Set<EventFamily>  registeredEventFamilies = new HashSet<EventFamily>();
    private final List<Event>       currentEvents = new LinkedList<Event>();
    private final Map<String, EventListenersRequestBinding> eventListenersRequests = new HashMap<String, EventListenersRequestBinding>();
    private final EventTransport transport;
    private final AtomicInteger eventSequence = new AtomicInteger();

    private class EventListenersRequestBinding {
        private final FetchEventListeners     listener;
        private final EventListenersRequest   request;
        private Boolean                 sent;

        public EventListenersRequestBinding(FetchEventListeners listener,
                EventListenersRequest request) {
            this.listener = listener;
            this.request = request;
            this.sent = false;
        }

        public FetchEventListeners getListener() {
            return listener;
        }

        public EventListenersRequest getRequest() {
            return request;
        }

        public Boolean isSent() {
            return sent;
        }

        public void setSent(Boolean sent) {
            this.sent = sent;
        }
    }

    public DefaultEventManager(EventTransport transport) {
        this.transport = transport;
    }

    @Override
    public void fillEventSyncRequest(EventSyncRequest request) {
        if (!currentEvents.isEmpty()) {
            LOG.debug("Going to send {} event{}", currentEvents.size()
                    , (currentEvents.size() == 1 ? "" : "s")); //NOSONAR
            List<Event> events = new LinkedList<Event>(currentEvents);
            request.setEvents(events);
        }
        if (!eventListenersRequests.isEmpty()) {
            LOG.debug("There are {} unresolved eventListenersResolution request{}"
                    , eventListenersRequests.size()
                    , (eventListenersRequests.size() == 1 ? "" : "s")); //NOSONAR
            List<EventListenersRequest> requests = new ArrayList<EventListenersRequest>();
            for (Map.Entry<String, EventListenersRequestBinding> entry : eventListenersRequests.entrySet()) {
                if (!entry.getValue().isSent()) {
                    requests.add(entry.getValue().getRequest());
                    entry.getValue().setSent(Boolean.TRUE);
                }
            }
            request.setEventListenersRequests(requests);
        }
    }

    @Override
    public void clearState() {
        currentEvents.clear();
    }

    @Override
    public void produceEvent(String eventFqn, byte[] data, String target) throws IOException {
        LOG.info("Producing event [eventClassFQN: {}, target: {}]"
                , eventFqn, (target != null ? target : "broadcast")); //NOSONAR
        currentEvents.add(new Event(eventSequence.getAndIncrement(), eventFqn, ByteBuffer.wrap(data), null, target));
        transport.sync();
    }

    @Override
    public void registerEventFamily(EventFamily eventFamily) {
        registeredEventFamilies.add(eventFamily);
    }

    @Override
    public void onGenericEvent(String eventFqn, byte[] data, String source) {
        LOG.info("Received event [eventClassFQN: {}]", eventFqn);
        for (EventFamily family : registeredEventFamilies) {
            LOG.info("Lookup event fqn {} in family {}", eventFqn, family);
            if (family.getSupportedEventFQNs().contains(eventFqn)) {
                LOG.info("Event fqn {} found in family {}", eventFqn, family);
                family.onGenericEvent(eventFqn, data, source);
            }
        }
    }

    @Override
    public String findEventListeners(List<String> eventClassFQNs,
            FetchEventListeners listener) {
        String requestId = UUID.randomUUID().toString();
        EventListenersRequest request = new EventListenersRequest(requestId, eventClassFQNs);
        EventListenersRequestBinding bind = new EventListenersRequestBinding(listener, request);
        eventListenersRequests.put(requestId, bind);
        LOG.debug("Adding event listener resolution request. Request ID: {}"
                , requestId);
        transport.sync();
        return requestId;
    }

    @Override
    public void eventListenersResponseReceived(
            List<EventListenersResponse> response) {
        for (EventListenersResponse singleResponse : response) {
            LOG.debug("Received event listener resolution response: {}", response);
            EventListenersRequestBinding bind = eventListenersRequests.remove(singleResponse.getRequestId());
            if (bind != null) {
                if (singleResponse.getResult() == SyncResponseResultType.SUCCESS) {
                    bind.getListener().onEventListenersReceived(singleResponse.getListeners());
                } else {
                    bind.getListener().onRequestFailed();
                }
            }
        }
    }

}
