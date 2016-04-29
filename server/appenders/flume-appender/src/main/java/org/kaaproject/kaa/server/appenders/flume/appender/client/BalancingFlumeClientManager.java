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
import java.util.Properties;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendBatchAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AsyncRpcClient;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AvroAsyncRpcClient;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNode;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

public class BalancingFlumeClientManager extends FlumeClientManager<FlumeNodes> {

    private static final Logger LOG = LoggerFactory.getLogger(BalancingFlumeClientManager.class);
    private static final String ROUND_ROBIN = "round_robin";
    private static final String H = "h";

    private int maxClientThreads = 1;

    @Override
    public AsyncRpcClient initManager(FlumeNodes parameters) {
        LOG.debug("Init manager...");
        Properties properties = generateProperties(parameters);
        return new AvroAsyncRpcClient(properties, maxClientThreads);
    }

    @Override
    public AsyncRpcClient initManager(FlumeNodes parameters, int maxClientThreads) {
        LOG.debug("Init manager...");
        this.maxClientThreads = maxClientThreads;
        Properties properties = generateProperties(parameters);
        return new AvroAsyncRpcClient(properties, maxClientThreads);
    }

    @Override
    public void sendEventToFlume(Event event) throws EventDeliveryException {
        currentClient.append(event);
    }

    @Override
    public void sendEventsToFlume(List<Event> events) throws EventDeliveryException {
        currentClient.appendBatch(events);
    }

    private Properties generateProperties(FlumeNodes parameters) {
        Properties props = new Properties();
        props.put(CLIENT_TYPE, "default_loadbalance");

        List<FlumeNode> list = parameters.getFlumeNodes();
        StringBuilder hostsAlias = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            String host = H + (i + 1);
            hostsAlias.append(host).append(" ");
            FlumeNode node = list.get(i);
            props.put(HOSTS + "." + host, node.getHost() + ":" + node.getPort());
        }
        props.put(HOSTS, hostsAlias.toString().trim());
        props.put(HOST_SELECTOR, ROUND_ROBIN);
        props.put(CONNECT_TIMEOUT, 2000);
        props.put(REQUEST_TIMEOUT, 2000);

        LOG.debug("Generated properties: {}", props);

        return props;
    }

    @Override
    public ListenableFuture<AppendAsyncResultPojo> sendEventToFlumeAsync(Event event) throws EventDeliveryException {
        return currentClient.appendAsync(event);
    }

    @Override
    public ListenableFuture<AppendBatchAsyncResultPojo> sendEventsToFlumeAsync(List<Event> events)
            throws EventDeliveryException {
        return currentClient.appendBatchAsync(events);
    }
}
