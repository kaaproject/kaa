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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class AvroAsyncRpcClient implements AsyncRpcClient {

  public static final String ASYNC_MAX_THREADS = "async-mx-threads";
  private static final Logger LOG = LoggerFactory.getLogger(AvroAsyncRpcClient.class);
  ArrayBlockingQueue<RpcClient> clientQueue;
  ListeningExecutorService executorService;

  /**
   * Create new instance of <code>AvroAsyncRpcClient</code>.
   *
   * @param starterProp the properties of starter
   * @param numberOfClientThreads is number of client's threads
   */
  public AvroAsyncRpcClient(Properties starterProp, int numberOfClientThreads) {
    clientQueue = new ArrayBlockingQueue<RpcClient>(numberOfClientThreads);

    for (int i = 0; i < numberOfClientThreads; i++) {
      RpcClient client = RpcClientFactory.getInstance(starterProp);
      clientQueue.add(client);
    }

    LOG.info("Number of Threads:" + numberOfClientThreads);
    executorService = MoreExecutors
        .listeningDecorator(Executors.newFixedThreadPool(numberOfClientThreads));
  }

  /**
   * Create new instance of <code>AvroAsyncRpcClient</code>.
   *
   * @param hostname        the RPC hostname, use it to create RPC client
   * @param port            the RPC port, use it to create RPC client
   * @param numberOfThreads is number of client's threads
   */
  public AvroAsyncRpcClient(String hostname, Integer port, int numberOfThreads) {
    int numberOfClientThreads = numberOfThreads;

    clientQueue = new ArrayBlockingQueue<RpcClient>(numberOfClientThreads);

    for (int i = 0; i < numberOfClientThreads; i++) {
      RpcClient client = RpcClientFactory.getDefaultInstance(hostname, port);
      clientQueue.add(client);
    }

    LOG.info("Number of Threads:" + numberOfClientThreads);
    executorService = MoreExecutors
        .listeningDecorator(Executors.newFixedThreadPool(numberOfClientThreads));
  }

  /**
   * Async append event to RPC client, return listenable future.
   *
   * @param event to adding by RPC
   * @return listenable future
   */
  public ListenableFuture<AppendAsyncResultPojo> appendAsync(final Event event)
      throws EventDeliveryException {
    ListenableFuture<AppendAsyncResultPojo> future = executorService.submit(
        new Callable<AppendAsyncResultPojo>() {
          public AppendAsyncResultPojo call() throws Exception {
            RpcClient client = clientQueue.poll();
            client.append(event);
            clientQueue.add(client);
            return new AppendAsyncResultPojo(true, event);
          }
        });
    return future;
  }

  /**
   * Async append some events to RPC client, return listenable future.
   *
   * @param events to adding by RPC
   * @return listenable future
   */
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
    } catch (Exception ex) {
      throw new EventDeliveryException(ex);
    }
  }

  @Override
  public void appendBatch(List<Event> events) throws EventDeliveryException {
    try {
      this.appendBatchAsync(events).get();
    } catch (Exception ex) {
      throw new EventDeliveryException(ex);
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
