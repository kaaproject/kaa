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

import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.transact.TransactionId;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Default {@link EventManager} implementation.
 *
 * @author Taras Lemkin
 */
public class DefaultEventManager implements EventManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultEventManager.class);
  private final Set<EventFamily> registeredEventFamilies = new HashSet<EventFamily>();
  private final List<Event> currentEvents = new LinkedList<Event>();
  private final Object eventsGuard = new Object();
  private final Object trxGuard = new Object();
  private final Map<Integer, EventListenersRequestBinding> eventListenersRequests =
          new HashMap<Integer, EventListenersRequestBinding>();
  private final EventTransport transport;
  private final KaaClientState state;
  private final ExecutorContext executorContext;
  private final Map<TransactionId, List<Event>> transactions = new HashMap<>();
  private Boolean isEngaged = false;

  /**
   * All-args constructor.
   */
  public DefaultEventManager(KaaClientState state, ExecutorContext executorContext,
                             EventTransport transport) {
    this.state = state;
    this.transport = transport;
    this.executorContext = executorContext;
  }

  @Override
  public void fillEventListenersSyncRequest(EventSyncRequest request) {
    if (!eventListenersRequests.isEmpty()) {
      LOG.debug("There are {} unresolved eventListenersResolution request{}",
              eventListenersRequests.size(),
          (eventListenersRequests.size() == 1 ? "" : "s")); // NOSONAR
      List<EventListenersRequest> requests = new ArrayList<EventListenersRequest>();
      for (Map.Entry<Integer, EventListenersRequestBinding> entry : eventListenersRequests
              .entrySet()) {
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
    synchronized (eventsGuard) {
      currentEvents.clear();
    }
  }

  @Override
  public void produceEvent(String eventFqn, byte[] data, String target) {
    produceEvent(eventFqn, data, target, null);
  }

  @Override
  public void produceEvent(String eventFqn, byte[] data, String target, TransactionId trxId) {
    if (trxId == null) {
      LOG.info("Producing event [eventClassFQN: {}, target: {}]", eventFqn,
              (target != null ? target : "broadcast")); // NOSONAR
      synchronized (eventsGuard) {
        currentEvents.add(new Event(state.getAndIncrementEventSeqNum(), eventFqn,
                ByteBuffer.wrap(data), null, target));
      }
      if (!isEngaged) {
        transport.sync();
      }
    } else {
      LOG.info("Adding event [eventClassFQN: {}, target: {}] to transaction {}",
              eventFqn, target != null ? target : "broadcast", trxId); // NOSONAR
      synchronized (trxGuard) {
        List<Event> events = transactions.get(trxId);
        if (events != null) {
          events.add(new Event(-1, eventFqn, ByteBuffer.wrap(data), null, target));
        } else {
          LOG.warn("Transaction with id {} is missing. Ignoring event");
        }
      }
    }
  }

  @Override
  public void registerEventFamily(EventFamily eventFamily) {
    registeredEventFamilies.add(eventFamily);
  }

  @Override
  public void onGenericEvent(final String eventFqn, final byte[] data, final String source) {
    LOG.info("Received event [eventClassFQN: {}]", eventFqn);
    for (final EventFamily family : registeredEventFamilies) {
      LOG.info("Lookup event fqn {} in family {}", eventFqn, family);
      if (family.getSupportedEventFqns().contains(eventFqn)) {
        LOG.info("Event fqn {} found in family {}", eventFqn, family);
        executorContext.getCallbackExecutor().submit(new Runnable() {
          @Override
          public void run() {
            family.onGenericEvent(eventFqn, data, source);
          }
        });
      }
    }
  }

  @Override
  public int findEventListeners(List<String> eventFqns, FindEventListenersCallback listener) {
    int requestId = new Random().nextInt();
    EventListenersRequest request = new EventListenersRequest(requestId, eventFqns);
    EventListenersRequestBinding bind = new EventListenersRequestBinding(listener, request);
    eventListenersRequests.put(requestId, bind);
    LOG.debug("Adding event listener resolution request. Request ID: {}", requestId);
    if (!isEngaged) {
      transport.sync();
    }
    return requestId;
  }

  @Override
  public void eventListenersResponseReceived(List<EventListenersResponse> response) {
    for (final EventListenersResponse singleResponse : response) {
      LOG.debug("Received event listener resolution response: {}", response);
      final EventListenersRequestBinding bind = eventListenersRequests.remove(
              singleResponse.getRequestId());
      executorContext.getCallbackExecutor().submit(new Runnable() {
        @Override
        public void run() {
          if (bind != null) {
            if (singleResponse.getResult() == SyncResponseResultType.SUCCESS) {
              bind.getListener().onEventListenersReceived(singleResponse.getListeners());
            } else {
              bind.getListener().onRequestFailed();
            }
          }
        }
      });
    }
  }

  @Override
  public List<Event> pollPendingEvents() {
    return getPendingEvents(true);
  }

  @Override
  public List<Event> peekPendingEvents() {
    return getPendingEvents(false);
  }

  private List<Event> getPendingEvents(boolean clear) {
    List<Event> pendingEvents = new ArrayList<Event>();
    synchronized (eventsGuard) {
      pendingEvents.addAll(currentEvents);
      if (clear) {
        currentEvents.clear();
      }
    }
    return pendingEvents;
  }

  @Override
  public TransactionId beginTransaction() {
    TransactionId trxId = new TransactionId();
    synchronized (trxGuard) {
      if (!transactions.containsKey(trxId)) {
        LOG.debug("Creating events transaction with id {}", trxId);
        transactions.put(trxId, new LinkedList<Event>());
      }
    }
    return trxId;
  }

  @Override
  public void commit(TransactionId trxId) {
    LOG.debug("Commiting events transaction with id {}", trxId);
    synchronized (trxGuard) {
      List<Event> eventsToCommit = transactions.remove(trxId);
      synchronized (eventsGuard) {
        for (Event e : eventsToCommit) {
          e.setSeqNum(state.getAndIncrementEventSeqNum());
          currentEvents.add(e);
        }
      }
      if (!isEngaged) {
        transport.sync();
      }

    }
  }

  @Override
  public void rollback(TransactionId trxId) {
    LOG.debug("Rolling back events transaction with id {}", trxId);
    synchronized (trxGuard) {
      List<Event> eventsToRemove = transactions.remove(trxId);
      if (eventsToRemove != null) {
        for (Event e : eventsToRemove) {
          LOG.trace("Removing event {}", e);
        }
      } else {
        LOG.debug("Transaction with id {} was not created", trxId);
      }
    }
  }

  @Override
  public synchronized void engageDataChannel() {
    isEngaged = true;
  }

  @Override
  public synchronized boolean releaseDataChannel() {
    isEngaged = false;
    boolean needSync = !currentEvents.isEmpty();
    if (!needSync) {
      for (EventListenersRequestBinding b : eventListenersRequests.values()) {
        needSync |= !b.isSent();
      }
    }
    return needSync;
  }

  private class EventListenersRequestBinding {
    private final FindEventListenersCallback listener;
    private final EventListenersRequest request;
    private Boolean sent;

    public EventListenersRequestBinding(
            FindEventListenersCallback listener, EventListenersRequest request) {
      this.listener = listener;
      this.request = request;
      this.sent = false;
    }

    public FindEventListenersCallback getListener() {
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

}
