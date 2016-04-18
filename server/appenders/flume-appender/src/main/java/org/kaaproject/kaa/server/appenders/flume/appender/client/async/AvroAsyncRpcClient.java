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

package org.kaaproject.kaa.server.appenders.flume.appender.client.async;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class AvroAsyncRpcClient implements AsyncRpcClient {

    ArrayBlockingQueue<RpcClient> clientQueue;
    private static final Logger LOG = LoggerFactory.getLogger(AvroAsyncRpcClient.class);
    public static final String ASYNC_MAX_THREADS = "async-mx-threads";

    ListeningExecutorService executorService;

    public AvroAsyncRpcClient(Properties starterProp, int numberOfClientThreads) {
        clientQueue = new ArrayBlockingQueue<RpcClient>(numberOfClientThreads);

        for (int i = 0; i < numberOfClientThreads; i++) {
            RpcClient client = RpcClientFactory.getInstance(starterProp);
            clientQueue.add(client);
        }

        LOG.info("Number of Threads:" + numberOfClientThreads);
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(numberOfClientThreads));
    }

    public AvroAsyncRpcClient(String hostname, Integer port, int numberOfThreads) {
        int numberOfClientThreads = numberOfThreads;

        clientQueue = new ArrayBlockingQueue<RpcClient>(numberOfClientThreads);

        for (int i = 0; i < numberOfClientThreads; i++) {
            RpcClient client = RpcClientFactory.getDefaultInstance(hostname, port);
            clientQueue.add(client);
        }

        LOG.info("Number of Threads:" + numberOfClientThreads);
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(numberOfClientThreads));
    }

    public ListenableFuture<AppendAsyncResultPojo> appendAsync(final Event event) throws EventDeliveryException {
        ListenableFuture<AppendAsyncResultPojo> future = executorService.submit(new Callable<AppendAsyncResultPojo>() {
            public AppendAsyncResultPojo call() throws Exception {
                RpcClient client = clientQueue.poll();
                client.append(event);
                clientQueue.add(client);
                return new AppendAsyncResultPojo(true, event);
            }
        });
        return future;
    }

    public ListenableFuture<AppendBatchAsyncResultPojo> appendBatchAsync(final List<Event> events)
            throws EventDeliveryException {
        ListenableFuture<AppendBatchAsyncResultPojo> future = executorService
                .submit(new Callable<AppendBatchAsyncResultPojo>() {
                    public AppendBatchAsyncResultPojo call() throws Exception {
                        RpcClient client = clientQueue.poll();
                        client.appendBatch(events);
                        clientQueue.add(client);
                        return new AppendBatchAsyncResultPojo(true, events);
                    }
                });

        return future;
    }

    @Override
    public int getBatchSize() {
        return 0;
    }

    @Override
    public void append(Event event) throws EventDeliveryException {
        try {
            this.appendAsync(event).get();
        } catch (Exception e) {
            throw new EventDeliveryException(e);
        }
    }

    @Override
    public void appendBatch(List<Event> events) throws EventDeliveryException {
        try {
            this.appendBatchAsync(events).get();
        } catch (Exception e) {
            throw new EventDeliveryException(e);
        }
    }

    @Override
    public boolean isActive() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void close() throws FlumeException {
        // Do nothing
    }
}
