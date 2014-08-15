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

package org.kaaproject.kaa.server.operations.service.event;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.operations.EventMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Time;
import com.twitter.common.thrift.Thrift;
import com.twitter.common.thrift.ThriftFactory;

/**
 * Neighbor Connection Class.
 * Hold thrift connection pool to specific operations server.
 * Provides sendEventMessage() for send messages to neighbor Operations Server
 * @author Andrey Panasenko
 *
 */
public final class NeighborConnection {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(NeighborConnection.class);

    /** Default maximum number of opened connections to neighbor */
    private static final int DEFAULT_MAX_NUMBER_OF_CONNECTION_TO_NEIGHBOR = 2;

    /** SOCKET_TIMEOUT on opened connection in seconds */
    //In seconds
    private static final long DEFAULT_SOCKET_TIMEOUT_CONNECTION_TO_NEIGHBOR = 20;

    /** Default maximum number of event messages queue */
    private static final int DEFAULT_EVENT_MESSAGE_QUEUE_LENGTH = 1024*1024;

    /** ID of connection in thriftHost:thriftPort formar */
    private String id;

    /** ConnectionInfo of neighbor Operations server */
    private ConnectionInfo connectionInfo;

    /** Real maximum number of opened connections to neighbor, if not set use default */
    private int maxNumberConnection = DEFAULT_MAX_NUMBER_OF_CONNECTION_TO_NEIGHBOR;

    /** Real SOCKET_TIMEOUT on opened connection, if not set used default */
    private long socketTimeout = DEFAULT_SOCKET_TIMEOUT_CONNECTION_TO_NEIGHBOR;

    /** Real maximum number of event messages queue */
    private final int messageQueueLingth = DEFAULT_EVENT_MESSAGE_QUEUE_LENGTH;

    /** Thrift classes */
    private ThriftFactory<OperationsThriftService.Iface> clientFactory;
    private Thrift<OperationsThriftService.Iface> thrift;


    /** Blocking queue of event messages */
    private LinkedBlockingQueue<EventMessage> messageQueue;

    private Random rnd;

    /** Fixed Thread pool to run event workers */
    private ExecutorService executor;

    /** Future list of event workers */
    private List<Future<?>> workers;

    /** event service */
    private final DefaultEventService eventService;
    /**
     * EventWorker Class.
     * Provides sending EventMessages asynchronously.
     * EventWorker blocks if messageQueue is empty in poll() operation.
     */
    public class EventWorker implements Runnable {

        private final int uniqueId = rnd.nextInt(maxNumberConnection*1000);
        private boolean operate = true;
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            LinkedList<EventMessage> messages = new LinkedList<>(); //NOSONAR
            OperationsThriftService.Iface client = null;
            while(operate) {
                try {
                    EventMessage event = messageQueue.poll(1, TimeUnit.HOURS);
                    if (event != null) {
                        messages.push(event);
                        messageQueue.drainTo(messages);
                        client = getClient();
                        client.sendEventMessage(messages);
                        LOG.debug("EventWorker [{}:<{}>] {} messages sent", getId(), uniqueId, messages.size());
                        messages.clear();
                    }
                } catch(TException te) {
                    LOG.error("EventWorker [{}:{}] error sending event messages pack. ", getId(), uniqueId, te);
                    eventService.notifyListenersOnServerProblem(getId());
                } catch (InterruptedException  e) {
                    LOG.info("EventWorker [{}<{}>] terminated: ", getId(), uniqueId,e);
                    operate = false;
                }
            }
        }
    }

    /**
     * Constructor.
     * @param connectionInfo neighbor operations server ConnectionInfo
     * @param maxNumberConnection maximum number of opened connections to neighbor
     * @param socketTimeout SOCKET_TIMEOUT on opened connection
     */
    public NeighborConnection(DefaultEventService eventService, ConnectionInfo connectionInfo, int maxNumberConnection, long socketTimeout) {
        this.eventService = eventService;
        setMaxNumberConnection(maxNumberConnection);
        setSocketTimeout(socketTimeout);
        setConnectionInfo(connectionInfo);
        init();
    }

    /**
     * Constructor
     * @param connectionInfo neighbor operations server ConnectionInfo
     */
    public NeighborConnection(DefaultEventService eventService, ConnectionInfo connectionInfo) {
        this.eventService = eventService;
        setConnectionInfo(connectionInfo);
        init();
    }

    /**
     * Initialize Thrift connections.
     */
    private void init() {
        executor = Executors.newFixedThreadPool(getMaxNumberConnection());
        messageQueue = new LinkedBlockingQueue<>(messageQueueLingth);
        workers = new LinkedList<>();
        rnd = new Random();
        clientFactory = ThriftFactory.create(OperationsThriftService.Iface.class);
        setId(Neighbors.getOperationsServerID(connectionInfo));
        InetSocketAddress address = new InetSocketAddress(connectionInfo.getThriftHost().toString(), connectionInfo.getThriftPort());
        Set<InetSocketAddress> backends = new HashSet<InetSocketAddress>();
        backends.add(address);
        thrift = clientFactory.withMaxConnectionsPerEndpoint(maxNumberConnection).withSocketTimeout(Amount.of(socketTimeout, Time.SECONDS)).build(backends);
        initWorkers();
    }

    /**
     * Initialize event workers.
     */
    private void initWorkers() {
        for(int i = 0 ; i < getMaxNumberConnection(); i++) {
            EventWorker worker = new EventWorker();
            workers.add(executor.submit(worker));
        }
    }

    /**
     * Cancel event workers.
     */
    private void cancelWorkers() {
        for( Future<?> f : workers) {
            f.cancel(true);
        }
        workers.clear();
    }

    /**
     * Return Thrift service client interface.
     * @return OperationsThriftService.Iface
     */
    public OperationsThriftService.Iface getClient() {
        return thrift.builder().disableStats().withRequestTimeout(Amount.of(socketTimeout, Time.SECONDS)).create();
    }

    /**
     * Stops neighbor Operations server connections.
     */
    public void shutdown() {
        cancelWorkers();
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Neighbor Connection {} error terminates ExecutorService",getId(), e);
        }
        thrift.close();
    }

    /**
     * Send List<EventMessage> to neighbor Operartions Server.
     * @param messages List<EventMessage>
     * @throws InterruptedException in case of queuing error occurred.
     */
    public void sendEventMessage(List<EventMessage> messages) throws InterruptedException {
        for(EventMessage e : messages) {
            if(!messageQueue.offer(e, 1, TimeUnit.MINUTES)) {
                LOG.error("NeighborConnection [{}] event messages queue is full more than 1 minute. Operation impossible.", getId());
                throw new InterruptedException("Event messages queue is full more than 10 minutes");
            }
        }
    }

    /**
     * Neighbor Operations Server ID getter.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * Return Neighbor Operations Server ConnectionInfo
     * @return the connectionInfo
     */
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    /**
     * @param connectionInfo the connectionInfo to set
     */
    private void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    /**
     * Maximum number of opened connections to neighbor getter.
     * @return the maxNumberConnection
     */
    public int getMaxNumberConnection() {
        return maxNumberConnection;
    }

    /**
     * SOCKET_TIMEOUT on opened connection getter.
     * @return the socketTimeout
     */
    public long getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Maximum number of opened connections to neighbor setter.
     * @param maxNumberConnection the maxNumberConnection to set
     */
    public void setMaxNumberConnection(int maxNumberConnection) {
        this.maxNumberConnection = maxNumberConnection;
    }

    /**
     * SOCKET_TIMEOUT on opened connection setter.
     * @param socketTimeout the socketTimeout to set
     */
    public void setSocketTimeout(long socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NeighborConnection [Id=" + id + "]";
    }


}
