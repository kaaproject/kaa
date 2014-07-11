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

package org.kaaproject.kaa.client.channel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultEventTransport;
import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.mockito.Mockito;

public class DefaultEventTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        EventTransport transport = new DefaultEventTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.EVENT)).thenReturn(channel);

        EventTransport transport = new DefaultEventTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channel, Mockito.times(1)).sync(TransportType.EVENT);
    }

    @Test
    public void testCreateRequest() {
        EventManager manager = Mockito.mock(EventManager.class);

        EventTransport transport = new DefaultEventTransport();
        transport.createEventRequest();
        transport.setEventManager(manager);
        transport.createEventRequest();
        Mockito.verify(manager, Mockito.times(1)).fillEventSyncRequest(Mockito.any(EventSyncRequest.class));
    }

    @Test
    public void onEventResponse() throws Exception {
        EventManager manager = Mockito.mock(EventManager.class);
        EventTransport transport = new DefaultEventTransport();
        EventSyncResponse response = new EventSyncResponse();

        transport.onEventResponse(response);
        transport.setEventManager(manager);
        transport.onEventResponse(response);

        List<Event> events = new ArrayList<>(1);
        response.setEvents(events);
        transport.onEventResponse(response);
        events.add(new Event(5, "eventClassFQN", ByteBuffer.wrap(new byte [] { 1, 2, 3 }), "source", "target"));
        transport.onEventResponse(response);

        List<EventListenersResponse> listeners = new ArrayList<>(1);
        response.setEventListenersResponses(listeners);
        transport.onEventResponse(response);
        listeners.add(new EventListenersResponse());
        transport.onEventResponse(response);

        Mockito.verify(manager, Mockito.times(3)).onGenericEvent(Mockito.eq("eventClassFQN"), Mockito.eq(new byte [] { 1, 2, 3 }), Mockito.eq("source"));
        Mockito.verify(manager, Mockito.times(1)).eventListenersResponseReceived(listeners);
    }
}
