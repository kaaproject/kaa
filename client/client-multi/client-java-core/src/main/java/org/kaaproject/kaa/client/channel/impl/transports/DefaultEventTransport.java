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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventSequenceNumberRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultEventTransport extends AbstractKaaTransport implements EventTransport {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultEventTransport.class);

  private final Map<Integer, Set<Event>> pendingEvents = new HashMap<>();
  private final EventComparator eventSeqNumberComparator = new EventComparator();

  private final KaaClientState clientState;
  private final AtomicInteger startEventSn;
  private EventManager eventManager;
  private boolean isEventSnSynchronized = false;

  public DefaultEventTransport(KaaClientState state) {
    this.clientState = state;
    this.startEventSn = new AtomicInteger(clientState.getEventSeqNum());
  }

  @Override
  public EventSyncRequest createEventRequest(Integer requestId) {
    if (eventManager != null) {
      EventSyncRequest request = new EventSyncRequest();

      eventManager.fillEventListenersSyncRequest(request);

      if (isEventSnSynchronized) {
        Set<Event> eventsSet = new HashSet<Event>();

        if (!pendingEvents.isEmpty()) {
          for (Map.Entry<Integer, Set<Event>> pendingEntry : pendingEvents.entrySet()) {
            LOG.debug("Have not received response for {} events sent with request id {}",
                    pendingEntry.getValue().size(),
                pendingEntry.getKey());
            eventsSet.addAll(pendingEntry.getValue());
          }
        }

        eventsSet.addAll(eventManager.pollPendingEvents());

        List<Event> events = new ArrayList<Event>(eventsSet);
        if (!events.isEmpty()) {
          Collections.sort(events, eventSeqNumberComparator);
          LOG.debug("Going to send {} event{}", events.size(),
                  (events.size() == 1 ? "" : "s")); // NOSONAR
          request.setEvents(events);
          pendingEvents.put(requestId, eventsSet);
        }

        request.setEventSequenceNumberRequest(null);
      } else {
        request.setEventSequenceNumberRequest(new EventSequenceNumberRequest());
        LOG.trace("Sending event sequence number request: " + "restored_sn = {}", startEventSn);
      }

      return request;
    }
    return null;
  }

  @Override
  public void onEventResponse(EventSyncResponse response) {
    if (eventManager != null) {
      if (!isEventSnSynchronized && response.getEventSequenceNumberResponse() != null) {
        int lastSn = response.getEventSequenceNumberResponse().getSeqNum();
        int expectedSn = lastSn > 0 ? lastSn + 1 : lastSn;

        if (startEventSn.get() != expectedSn) {
          startEventSn.set(expectedSn);
          clientState.setEventSeqNum(startEventSn.get());

          Set<Event> eventsSet = new HashSet<Event>();
          for (Set<Event> events : pendingEvents.values()) {
            eventsSet.addAll(events);
          }

          eventsSet.addAll(eventManager.peekPendingEvents());

          List<Event> events = new ArrayList<Event>(eventsSet);
          Collections.sort(events, eventSeqNumberComparator);

          clientState.setEventSeqNum(startEventSn.get() + events.size());
          if (!events.isEmpty() && events.get(0).getSeqNum() != startEventSn.get()) {
            LOG.info("Put in order event sequence numbers (expected: {}, actual: {})",
                    startEventSn, events.get(0).getSeqNum());

            for (Event e : events) {
              e.setSeqNum(startEventSn.getAndIncrement());
            }
          } else {
            startEventSn.getAndAdd(events.size());
          }

          LOG.info("Event sequence number is unsynchronized. Set to {}", startEventSn);
        } else {
          LOG.info("Event sequence number is up to date: {}", startEventSn);
        }

        isEventSnSynchronized = true;
      }

      if (response.getEvents() != null && !response.getEvents().isEmpty()) {
        List<Event> events = new ArrayList<>(response.getEvents());
        Collections.sort(events, eventSeqNumberComparator);
        for (Event event : events) {
          eventManager.onGenericEvent(event.getEventClassFQN(), event.getEventData().array(),
                  event.getSource());
        }
      }
      if (response.getEventListenersResponses() != null
              && !response.getEventListenersResponses().isEmpty()) {
        eventManager.eventListenersResponseReceived(response.getEventListenersResponses());
      }
    }
    LOG.trace("Processed event response");
  }

  @Override
  public void setEventManager(EventManager manager) {
    this.eventManager = manager;
  }

  @Override
  public void onSyncResposeIdReceived(Integer requestId) {
    LOG.debug("Events sent with request id {} were accepted.", requestId);
    Set<Event> acceptedEvents = pendingEvents.remove(requestId);
    if (acceptedEvents != null) {
      Iterator<Entry<Integer, Set<Event>>> entrySetIterator = pendingEvents.entrySet().iterator();
      while (entrySetIterator.hasNext()) {
        Entry<Integer, Set<Event>> entry = entrySetIterator.next();
        entry.getValue().removeAll(acceptedEvents);
        if (entry.getValue().isEmpty()) {
          LOG.debug("Remove entry for request {}.", requestId);
          entrySetIterator.remove();
        }
      }
    }
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.EVENT;
  }

  @Override
  public void blockEventManager() {
    if (eventManager != null) {
      eventManager.engageDataChannel();
    }
  }

  @Override
  public void releaseEventManager() {
    if (eventManager != null) {
      if (eventManager.releaseDataChannel()) {
        sync();
      }
    }
  }

  class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event e1, Event e2) {
      return e1.getSeqNum() - e2.getSeqNum();
    }
  }
}
