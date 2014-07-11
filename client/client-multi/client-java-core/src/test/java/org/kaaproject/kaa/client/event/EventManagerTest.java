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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.mockito.Mockito;

public class EventManagerTest {

    private class ConcreteEventFamily implements EventFamily {

        Set<String> supportedEventFQNs = new HashSet<String>();
        private Integer eventsCount;
        public ConcreteEventFamily(String supportedFQN) {
            eventsCount = 0;
            supportedEventFQNs.add(supportedFQN);
        }

        @Override
        public Set<String> getSupportedEventFQNs() {
            return supportedEventFQNs;
        }

        @Override
        public void onGenericEvent(String eventFQN, byte[] data, String source) {
            eventsCount++;
        }

        public Integer getEventsCount() {
            return eventsCount;
        }

    }

    @Test
    public void testNoHandler() {
        EventTransport transport = Mockito.mock(EventTransport.class);
        EventFamily   eventFamily   = Mockito.mock(EventFamily.class);

        EventManager  eventManager = new DefaultEventManager(transport);
        eventManager.registerEventFamily(eventFamily);
        try {
            eventManager.produceEvent("kaa.test.event.PlayEvent", new byte[0], null);
        } catch (IOException e) {
            assertTrue("Unexpected exception", false);
        }
        Mockito.verify(transport, times(1)).sync();
        verify(eventFamily, times(0)).getSupportedEventFQNs();
        verify(eventFamily, times(0)).onGenericEvent(anyString(), any(byte[].class), anyString());
    }

    @Test
    public void testOneEventForTwoDifferentFamilies() {
        EventTransport transport = Mockito.mock(EventTransport.class);
        ConcreteEventFamily eventFamily = new ConcreteEventFamily("kaa.test.event.PlayEvent");
        ConcreteEventFamily eventFamily2 = new ConcreteEventFamily("kaa.test.event.StopEvent");

        EventManager eventManager = new DefaultEventManager(transport);

        eventManager.registerEventFamily(eventFamily);
        eventManager.registerEventFamily(eventFamily2);

        assertEquals("Events count doesn't match", new Integer(0), eventFamily.getEventsCount());
        assertEquals("Events count doesn't match", new Integer(0), eventFamily2.getEventsCount());

        eventManager.onGenericEvent("kaa.test.event.PlayEvent", new byte[0], null);

        assertEquals("Events count doesn't match", new Integer(1), eventFamily.getEventsCount());
        assertEquals("Events count doesn't match", new Integer(0), eventFamily2.getEventsCount());

        eventManager.onGenericEvent("kaa.test.event.StopEvent", new byte[0], null);

        assertEquals("Events count doesn't match", new Integer(1), eventFamily.getEventsCount());
        assertEquals("Events count doesn't match", new Integer(1), eventFamily2.getEventsCount());

        eventManager.onGenericEvent("kaa.test.event.NoSuchEvent", new byte[0], null);

        assertEquals("Events count doesn't match", new Integer(1), eventFamily.getEventsCount());
        assertEquals("Events count doesn't match", new Integer(1), eventFamily2.getEventsCount());
    }

    @Test
    public void checkFillRequest() {
        EventTransport transport = Mockito.mock(EventTransport.class);
        EventManager eventManager = new DefaultEventManager(transport);

        EventSyncRequest request = new EventSyncRequest();
        try {
            eventManager.produceEvent("kaa.test.event.SomeEvent", new byte[0], "theTarget");
        } catch (IOException e) {
            assertTrue("Unexpected exception", false);
        }

        eventManager.fillEventSyncRequest(request);

        assertNotNull(request.getEvents());
        assertEquals(1, request.getEvents().size());
        assertEquals("kaa.test.event.SomeEvent", request.getEvents().get(0).getEventClassFQN());
        assertEquals("theTarget", request.getEvents().get(0).getTarget());
        assertArrayEquals(new byte[0], request.getEvents().get(0).getEventData().array());

        request = new EventSyncRequest();
        List<String> eventFQNs =  new ArrayList<String>();
        eventFQNs.add("eventFQN1");
        eventManager.findEventListeners(eventFQNs, new FetchEventListeners() {

            @Override
            public void onRequestFailed() {
            }

            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
            }
        });
        eventManager.findEventListeners(eventFQNs, new FetchEventListeners() {

            @Override
            public void onRequestFailed() {
            }

            @Override
            public void onEventListenersReceived(List<String> eventListeners) {
            }
        });

        eventManager.fillEventSyncRequest(request);
        assertNotNull(request.getEventListenersRequests());
        assertEquals(2, request.getEventListenersRequests().size());
        assertArrayEquals(eventFQNs.toArray(), request.getEventListenersRequests().get(0).getEventClassFQNs().toArray());
    }

    @Test
    public void testEventListenersRequestResponse() {
        EventTransport transport = Mockito.mock(EventTransport.class);
        EventManager eventManager = new DefaultEventManager(transport);

        List<String> eventFQNs =  new ArrayList<String>();
        eventFQNs.add("eventFQN1");

        FetchEventListeners fetchListener = mock(FetchEventListeners.class);
        String requestIdOk = eventManager.findEventListeners(eventFQNs, fetchListener);
        String requestIdBad = eventManager.findEventListeners(eventFQNs, fetchListener);

        verify(transport, atLeast(1)).sync();

        List<EventListenersResponse> response = new ArrayList<EventListenersResponse>();
        response.add(new EventListenersResponse(requestIdOk, new ArrayList<String>(), SyncResponseResultType.SUCCESS));
        response.add(new EventListenersResponse(requestIdBad, new ArrayList<String>(), SyncResponseResultType.FAILURE));

        eventManager.eventListenersResponseReceived(response);

        verify(fetchListener, times(1)).onEventListenersReceived(anyListOf(String.class));
        verify(fetchListener, times(1)).onRequestFailed();
    }
}
