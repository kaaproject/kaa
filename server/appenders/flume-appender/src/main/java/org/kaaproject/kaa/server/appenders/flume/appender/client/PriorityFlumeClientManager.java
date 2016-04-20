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

package org.kaaproject.kaa.server.appenders.flume.appender.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendBatchAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AsyncRpcClient;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AvroAsyncRpcClient;
import org.kaaproject.kaa.server.appenders.flume.config.gen.PrioritizedFlumeNode;
import org.kaaproject.kaa.server.appenders.flume.config.gen.PrioritizedFlumeNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

public class PriorityFlumeClientManager extends FlumeClientManager<PrioritizedFlumeNodes> {

    private static final Logger LOG = LoggerFactory.getLogger(PriorityFlumeClientManager.class);

    private static final int DEFAULT_POSITION = 0;
    private List<PrioritizedFlumeNode> flumeNodes = null;
    private Integer currentPosition = DEFAULT_POSITION;
    private int maxClientThreads = 1;

    @Override
    public AsyncRpcClient initManager(PrioritizedFlumeNodes parameters) {
        parameters.getFlumeNodes();
        flumeNodes = parameters.getFlumeNodes();
        if (!flumeNodes.isEmpty()) {
            Collections.sort(flumeNodes, new Comparator<PrioritizedFlumeNode>() {
                @Override
                public int compare(PrioritizedFlumeNode o1, PrioritizedFlumeNode o2) {
                    int result;
                    if (o2 != null) {
                        result = o1.getPriority() - o2.getPriority();
                    } else {
                        result = -1;
                    }
                    return result;
                }
            });
        } else {
            LOG.warn("Can't initialize flume Rpc client. No required hosts paraneters.");
        }
        return getNextClient(true);
    }

    @Override
    public AsyncRpcClient initManager(PrioritizedFlumeNodes parameters, int maxClientThreads) {
        parameters.getFlumeNodes();
        flumeNodes = parameters.getFlumeNodes();
        this.maxClientThreads = maxClientThreads;
        if (!flumeNodes.isEmpty()) {
            Collections.sort(flumeNodes, new Comparator<PrioritizedFlumeNode>() {
                @Override
                public int compare(PrioritizedFlumeNode o1, PrioritizedFlumeNode o2) {
                    int result;
                    if (o2 != null) {
                        result = o1.getPriority() - o2.getPriority();
                    } else {
                        result = -1;
                    }
                    return result;
                }
            });
        } else {
            LOG.warn("Can't initialize flume Rpc client. No required hosts paraneters.");
        }
        return getNextClient(true);
    }

    private AsyncRpcClient getNextClient(boolean isInit) {
        return getNextClient(isInit, 0);
    }

    private AsyncRpcClient getNextClient(boolean isInit, int retryCount) {
        LOG.debug("Get next flume rpc client");
        AsyncRpcClient client = null;
        PrioritizedFlumeNode node = null;
        if (isInit) {
            node = flumeNodes.get(DEFAULT_POSITION);
        } else {
            if (++currentPosition >= flumeNodes.size()) {
                node = flumeNodes.get(DEFAULT_POSITION);
                currentPosition = DEFAULT_POSITION;
            } else {
                node = flumeNodes.get(currentPosition);
            }
        }
        try {
            LOG.warn("Initialize new flume client.");
            client = new AvroAsyncRpcClient(node.getHost(), node.getPort(), maxClientThreads);
        } catch (FlumeException e) {
            LOG.warn("Can't initialize flume client.", e);
            if (retryCount <= MAX_RETRY_COUNT) {
                client = getNextClient(false, ++retryCount);
            } else {
                LOG.warn("Wasn't initialized any clients. Got exception {}", e);
                throw e;
            }
        }
        return client;
    }

    @Override
    public void sendEventToFlume(Event event) throws EventDeliveryException {
        sendEventToFlume(event, 1);
    }

    @Override
    public void sendEventsToFlume(List<Event> events) throws EventDeliveryException {
        sendEventsToFlume(events, 1);
    }

    private void sendEventToFlume(Event event, int retryCount) throws EventDeliveryException {
        try {
            LOG.debug("Sending flume event to flume agent {}", event);
            currentClient.append(event);
        } catch (EventDeliveryException e) {
            LOG.warn("Can't send flume event. Got exception {}", e);
            currentClient.close();
            currentClient = getNextClient(false);
            if (retryCount <= MAX_RETRY_COUNT) {
                LOG.debug("Retry send flume event. Count {}", retryCount);
                sendEventToFlume(event, ++retryCount);
            } else {
                LOG.warn("Flume event wasn't sent. Got exception {}", e);
                throw e;
            }
        }
    }

    private void sendEventsToFlume(List<Event> events, int retryCount) throws EventDeliveryException {
        try {
            LOG.debug("Sending flume events to flume agent {}", events);
            currentClient.appendBatch(events);
        } catch (EventDeliveryException e) {
            LOG.warn("Can't send flume events. Got exception {}", e);
            currentClient.close();
            currentClient = getNextClient(false);
            if (retryCount <= MAX_RETRY_COUNT) {
                LOG.debug("Retry send flume events. Count {}", retryCount);
                sendEventsToFlume(events, ++retryCount);
            } else {
                LOG.warn("Flume events wasn't sent. Got exception {}", e);
                throw e;
            }
        }
    }

    @Override
    public ListenableFuture<AppendAsyncResultPojo> sendEventToFlumeAsync(Event event) throws EventDeliveryException {
        return sendEventToFlumeAsync(event, 1);
    }

    @Override
    public ListenableFuture<AppendBatchAsyncResultPojo> sendEventsToFlumeAsync(List<Event> events)
            throws EventDeliveryException {
        return sendEventsToFlumeAsync(events, 1);
    }

    public ListenableFuture<AppendAsyncResultPojo> sendEventToFlumeAsync(Event event, int retryCount)
            throws EventDeliveryException {
        try {
            LOG.debug("Sending flume event to flume agent {}", event);
            return currentClient.appendAsync(event);
        } catch (EventDeliveryException e) {
            LOG.warn("Can't send flume event. Got exception {}", e);
            currentClient.close();
            currentClient = getNextClient(false);
            if (retryCount <= MAX_RETRY_COUNT) {
                LOG.debug("Retry send flume event. Count {}", retryCount);
                return sendEventToFlumeAsync(event, ++retryCount);
            } else {
                LOG.warn("Flume event wasn't sent. Got exception {}", e);
                throw e;
            }
        }
    }

    public ListenableFuture<AppendBatchAsyncResultPojo> sendEventsToFlumeAsync(List<Event> events, int retryCount)
            throws EventDeliveryException {
        try {
            LOG.debug("Sending flume events to flume agent {}", events);
            return currentClient.appendBatchAsync(events);
        } catch (EventDeliveryException e) {
            LOG.warn("Can't send flume events. Got exception {}", e);
            currentClient.close();
            currentClient = getNextClient(false);
            if (retryCount <= MAX_RETRY_COUNT) {
                LOG.debug("Retry send flume events. Count {}", retryCount);
                return sendEventsToFlumeAsync(events, ++retryCount);
            } else {
                LOG.warn("Flume events wasn't sent. Got exception {}", e);
                throw e;
            }
        }
    }
}
