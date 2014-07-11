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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventTransport extends AbstractKaaTransport implements
        EventTransport {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventTransport.class);

    private EventManager manager;

    @Override
    public void sync() {
        syncByType(TransportType.EVENT);
    }

    @Override
    public EventSyncRequest createEventRequest() {
        if (manager != null) {
            EventSyncRequest request = new EventSyncRequest();
            manager.fillEventSyncRequest(request);
            return request;
        }
        return null;
    }

    @Override
    public void onEventResponse(EventSyncResponse response) {
        if (manager != null) {
            manager.clearState();
            if (response.getEvents() != null && !response.getEvents().isEmpty()) {
                for (Event event : response.getEvents()) {
                    manager.onGenericEvent(event.getEventClassFQN(), event.getEventData().array(), event.getSource());
                }
            }
            if (response.getEventListenersResponses() != null && !response.getEventListenersResponses().isEmpty()) {
                manager.eventListenersResponseReceived(response.getEventListenersResponses());
            }
        }
        LOG.info("Processed event response");
    }

    @Override
    public void setEventManager(EventManager manager) {
        this.manager = manager;
    }

}
