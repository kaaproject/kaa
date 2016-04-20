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

import java.util.List;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendBatchAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AsyncRpcClient;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNodes;
import org.kaaproject.kaa.server.appenders.flume.config.gen.PrioritizedFlumeNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

public abstract class FlumeClientManager<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeClientManager.class);

    protected static final String CONNECT_TIMEOUT = "connect-timeout";
    protected static final String REQUEST_TIMEOUT = "request-timeout";
    protected static final String HOST_SELECTOR = "host-selector";
    protected static final String CLIENT_TYPE = "client.type";
    protected static final String HOSTS = "hosts";
    protected static final int MAX_RETRY_COUNT = 3;

    protected AsyncRpcClient currentClient = null;

    protected abstract AsyncRpcClient initManager(T parameters);

    protected abstract AsyncRpcClient initManager(T parameters, int maxClientThreads);

    public abstract void sendEventToFlume(Event event) throws EventDeliveryException;

    public abstract void sendEventsToFlume(List<Event> events) throws EventDeliveryException;

    public abstract ListenableFuture<AppendAsyncResultPojo> sendEventToFlumeAsync(Event event)
            throws EventDeliveryException;

    public abstract ListenableFuture<AppendBatchAsyncResultPojo> sendEventsToFlumeAsync(List<Event> events)
            throws EventDeliveryException;

    public void init(T parameters) {
        if (parameters != null) {
            currentClient = initManager(parameters);
            LOG.debug("Initialized rpc client {}", currentClient.getClass());
        } else {
            LOG.warn("Flume parameters is empty.");
        }
        if (currentClient == null) {
            throw new RuntimeException("Can't initialize flume Rpc client.");
        }
    }

    public void init(T parameters, int clientThreadPoolSize) {
        if (parameters != null) {
            currentClient = initManager(parameters, clientThreadPoolSize);
            LOG.debug("Initialized rpc client {}", currentClient.getClass());
        } else {
            LOG.warn("Flume parameters is empty.");
        }
        if (currentClient == null) {
            throw new RuntimeException("Can't initialize flume Rpc client.");
        }
    }

    public void cleanUp() {
        LOG.debug("Close flume rpc client.");
        if (currentClient != null) {
            currentClient.close();
            currentClient = null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> FlumeClientManager<T> getInstance(FlumeConfig configuration) {
        FlumeClientManager<?> clientManager = null;
        Object hostsBalancing = configuration.getHostsBalancing();
        if (hostsBalancing instanceof PrioritizedFlumeNodes) {
            LOG.debug("Init priority client manager");
            PrioritizedFlumeNodes flumeNodesConfig = (PrioritizedFlumeNodes) hostsBalancing;
            PriorityFlumeClientManager priorityClientManager = new PriorityFlumeClientManager();
            priorityClientManager.init(flumeNodesConfig, configuration.getClientsThreadPoolSize());
            clientManager = priorityClientManager;
        } else if (hostsBalancing instanceof FlumeNodes) {
            LOG.debug("Init round robin client manager");
            FlumeNodes flumeNodesConfig = (FlumeNodes) hostsBalancing;
            BalancingFlumeClientManager balancingClientManager = new BalancingFlumeClientManager();
            balancingClientManager.init(flumeNodesConfig, configuration.getClientsThreadPoolSize());
            clientManager = balancingClientManager;
        } else {
            LOG.error("Balancing type: {} does not supported.", hostsBalancing.getClass());
        }
        return (FlumeClientManager<T>) clientManager;
    }
}
