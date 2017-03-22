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

package org.kaaproject.kaa.server.thrift;

import com.google.common.base.Function;

import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import com.twitter.common.thrift.Thrift;
import com.twitter.common.thrift.ThriftFactory;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Iface;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Neighbor Connection Class. Hold thrift connection pool to specific operations
 * server. Provides sendEventMessage() for send messages to neighbor Operations
 * Server
 *
 * @author Andrey Panasenko
 * @author Andrew Shvayka
 */
public final class NeighborConnection<T extends NeighborTemplate<V>, V> {

  private static final Logger LOG = LoggerFactory.getLogger(NeighborConnection.class);

  /**
   * SOCKET_TIMEOUT on opened connection in seconds.
   */
  private static final long DEFAULT_SOCKET_TIMEOUT_CONNECTION_TO_NEIGHBOR = 20;

  /**
   * Default maximum number of event messages queue.
   */
  private static final int DEFAULT_EVENT_MESSAGE_QUEUE_LENGTH = 1024 * 1024;

  /**
   * ID of connection in thriftHost:thriftPort format.
   */
  private final String id;

  /**
   * ConnectionInfo of neighbor Operations server.
   */
  private final ConnectionInfo connectionInfo;

  private final int maxNumberConnection;

  private final T template;

  /**
   * Real SOCKET_TIMEOUT on opened connection, if not set used default.
   */
  private final long socketTimeout;

  /**
   * Real maximum number of event messages queue.
   */
  private final int messageQueueLength = DEFAULT_EVENT_MESSAGE_QUEUE_LENGTH;


  private ThriftFactory<OperationsThriftService.Iface> clientFactory;
  private Thrift<OperationsThriftService.Iface> thrift;


  private LinkedBlockingQueue<V> messageQueue;

  /**
   * Fixed Thread pool to run event workers.
   */
  private ExecutorService executor;

  /**
   * Future list of event workers.
   */
  private List<Future<?>> workers;

  private boolean started;

  /**
   * Create new instance of <code>NeighborConnection</code>.
   *
   * @param connectionInfo is connection info
   * @param maxNumberConnection os max number connection
   * @param socketTimeout is socket timeout
   * @param template is template
   */
  public NeighborConnection(ConnectionInfo connectionInfo, int maxNumberConnection,
                            long socketTimeout, T template) {
    this.connectionInfo = connectionInfo;
    this.maxNumberConnection = maxNumberConnection;
    this.socketTimeout = socketTimeout;
    this.template = template;
    this.id = Neighbors.getServerId(connectionInfo);
  }

  public NeighborConnection(ConnectionInfo connectionInfo,
                            int maxNumberNeighborConnections, T template) {
    this(connectionInfo, maxNumberNeighborConnections,
        DEFAULT_SOCKET_TIMEOUT_CONNECTION_TO_NEIGHBOR, template);
  }

  /**
   * Cancel event workers.
   */
  private void cancelWorkers() {
    for (Future<?> f : workers) {
      f.cancel(true);
    }
    workers.clear();
  }

  /**
   * Return Thrift service client interface.
   *
   * @return OperationsThriftService.Iface
   */
  public OperationsThriftService.Iface getClient() {
    return thrift.builder()
        .disableStats()
        .withRequestTimeout(Amount.of(socketTimeout, Time.SECONDS))
        .create();
  }

  /**
   * Start neighbor connection if it not started yet.
   */
  public synchronized void start() {
    if (!started) {
      executor = Executors.newFixedThreadPool(maxNumberConnection);
      messageQueue = new LinkedBlockingQueue<>(messageQueueLength);
      workers = new LinkedList<>();
      clientFactory = ThriftFactory.create(OperationsThriftService.Iface.class);
      InetSocketAddress address = new InetSocketAddress(
          connectionInfo.getThriftHost().toString(), connectionInfo.getThriftPort()
      );
      Set<InetSocketAddress> backends = new HashSet<>();
      backends.add(address);
      thrift = clientFactory.withMaxConnectionsPerEndpoint(maxNumberConnection)
          .withSocketTimeout(Amount.of(socketTimeout, Time.SECONDS))
          .withClientFactory(new Function<TTransport, OperationsThriftService.Iface>() {
                @Override
                public Iface apply(TTransport transport) {
                  TProtocol protocol = new TBinaryProtocol(transport);
                  TMultiplexedProtocol mprotocol = new TMultiplexedProtocol(
                      protocol, KaaThriftService.OPERATIONS_SERVICE.getServiceName()
                  );
                  return new OperationsThriftService.Client(mprotocol);
                }
              }).build(backends);
      for (int i = 0; i < maxNumberConnection; i++) {
        EventWorker worker = new EventWorker(template);
        workers.add(executor.submit(worker));
      }
      started = true;
    } else {
      LOG.debug("Neighbor Connection {} is already started", getId());
    }
  }

  /**
   * Stops neighbor Operations server connections.
   */
  public synchronized void shutdown() {
    if (started) {
      cancelWorkers();
      executor.shutdown();
      try {
        executor.awaitTermination(1, TimeUnit.SECONDS);
      } catch (InterruptedException ex) {
        LOG.error("Neighbor Connection {} error terminates ExecutorService", getId(), ex);
      }
      thrift.close();
      started = false;
    } else {
      LOG.debug("Neighbor Connection {} is already stopped or was not started yet", getId());
    }
  }

  /**
   * Send list of event message to the neighbor operations server.
   *
   * @param messages a list of messages that will be sent to  to the neighbor server
   * @throws InterruptedException in case of queuing error occurred.
   */
  public void sendMessages(Collection<V> messages) throws InterruptedException {
    for (V e : messages) {
      if (!messageQueue.offer(e, 1, TimeUnit.MINUTES)) {
        LOG.error("NeighborConnection [{}] event messages queue is full "
            + "more than 1 minute. Operation impossible.", getId());
        throw new InterruptedException("Event messages queue is full more than 10 minutes");
      }
    }
  }

  /**
   * Neighbor Operations Server ID getter.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Return Neighbor Operations Server ConnectionInfo.
   *
   * @return the connectionInfo
   */
  public ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  /**
   * SOCKET_TIMEOUT on opened connection getter.
   *
   * @return the socketTimeout
   */
  public long getSocketTimeout() {
    return socketTimeout;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    NeighborConnection other = (NeighborConnection) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }


  @Override
  public String toString() {
    return "NeighborConnection [Id=" + id + "]";
  }

  /**
   * EventWorker Class. Provides sending EventMessages asynchronously.
   * EventWorker blocks if messageQueue is empty in poll() operation.
   */
  public class EventWorker implements Runnable {

    private final T template;
    private final UUID uniqueId = UUID.randomUUID();
    private OperationsThriftService.Iface client = getClient();
    private boolean operate = true;

    public EventWorker(T template) {
      super();
      this.template = template;
    }


    @Override
    public void run() {
      LinkedList<V> messages = new LinkedList<>(); // NOSONAR
      while (operate) {
        try {
          V event = messageQueue.poll(1, TimeUnit.HOURS);
          if (event != null) {
            messages.push(event);
            messageQueue.drainTo(messages);
            template.process(client, messages);
            LOG.debug("EventWorker [{}:<{}>] {} messages sent", id, uniqueId, messages.size());
            messages.clear();
          }
        } catch (TException te) {
          LOG.error("EventWorker [{}:{}] error sending event messages pack. ", id, uniqueId, te);
          template.onServerError(id, te);
        } catch (InterruptedException ex) {
          LOG.info("EventWorker [{}<{}>] terminated: ", id, uniqueId, ex);
          operate = false;
        }
      }
    }
  }
}
