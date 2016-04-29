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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.zk.WorkerNodeTracker;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Neighbors Class. Collect all Operations Servers neighbors through listening
 * ZooKeeper OperationsNode changes. Use thriftHost:thriftPort as server Key and
 * hold Map of NeighborConnection.
 * 
 * @author Andrey Panasenko
 * @author Andrew Shvayka
 *
 */
public class Neighbors<T extends NeighborTemplate<V>, V> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Neighbors.class);

    private final KaaThriftService serviceType;

    private final ConcurrentMap<String, NeighborConnection<T, V>> neigbors;

    private final int maxNumberNeighborConnections;

    private final T template;

    private volatile String zkId;

    /**
     * Default constructor. If zkNode is not set, postpone timer task to wait
     * until node is set.
     * 
     * @param eventService
     */
    public Neighbors(KaaThriftService serviceType, T template, int maxNumberNeighborConnections) {
        this.serviceType = serviceType;
        this.template = template;
        this.maxNumberNeighborConnections = maxNumberNeighborConnections;
        this.neigbors = new ConcurrentHashMap<String, NeighborConnection<T, V>>();
    }

    public void sendMessage(ConnectionInfo info, V msg) {
        sendMessages(info, Collections.singleton(msg));
    }

    public void sendMessages(ConnectionInfo info, Collection<V> msg) {
        NeighborConnection<T, V> neighbor = neigbors.get(getServerID(info));
        if (neighbor != null) {
            try {
                neighbor.sendMessages(msg);
            } catch (InterruptedException e) {
                LOG.error("Failed to send message to {}", neighbor.getId());
                throw new RuntimeException(e);
            }
        } else {
            LOG.warn("Can't find server for id {}", getServerID(info));
        }
    }

    public void brodcastMessage(V msg) {
        brodcastMessages(Collections.singleton(msg));
    }

    public void brodcastMessages(Collection<V> msgs) {
        if(LOG.isTraceEnabled()){
            LOG.trace("Broadcasting {} msgs to {} neighbors", msgs.size(), neigbors.values().size());
        }
        for (NeighborConnection<T, V> neighbor : neigbors.values()) {
            LOG.trace("Broadcasting to {} neighbor", neighbor);
            try {
                neighbor.sendMessages(msgs);
            } catch (InterruptedException e) {
                LOG.warn("Failed to send message to {}", neighbor.getId());
            }
        }
    }

    /**
     * Shutdown all neighbors connections and cancel timer task if exist.
     */
    public void shutdown() {
        for (NeighborConnection<T, V> neigbor : neigbors.values()) {
            LOG.info("Shuting down neighbor connection {}", neigbor.getId());
            neigbor.shutdown();
        }
        neigbors.clear();
    }

    /**
     * Return current list of Neighbors.
     * 
     * @return List<NeighborConnection> neighbors.
     */
    public List<NeighborConnection<T, V>> getNeighbors() {
        return new LinkedList<NeighborConnection<T, V>>(neigbors.values());
    }

    /**
     * Return specific Neighbor connection by Id
     * 
     * @param serverId
     *            String in format thriftHost:thriftPort
     * @return NeighborConnection or null if such server not exist
     */
    public NeighborConnection<T, V> getNeghborConnection(String serverId) {
        return neigbors.get(serverId);
    }

    /**
     * Build server ID from ConnectionInfo object.
     * 
     * @param info
     *            ConnectionInfo
     * @return server ID in format thriftHost:thriftPort
     */
    public static String getServerID(ConnectionInfo info) {
        return getServerID(KaaThriftService.OPERATIONS_SERVICE, info);
    }

    /**
     * Build server ID from ConnectionInfo object.
     *
     * @param service
     *            KaaThriftService
     * @param info
     *            ConnectionInfo
     * @return server ID in format thriftHost:thriftPort
     */
    public static String getServerID(KaaThriftService service, ConnectionInfo info) {
        StringBuffer sb = new StringBuffer();
        sb.append(info.getThriftHost());
        sb.append(":");
        sb.append(info.getThriftPort());
        sb.append(":");
        sb.append(service.name());
        return sb.toString();
    }

    public void setZkNode(KaaThriftService service, ConnectionInfo connectionInfo, WorkerNodeTracker zkNode) {
        setZkNode(getServerID(service, connectionInfo), zkNode);
    }

    /**
     * Zk Node setter. Notify ZK initialization.
     * 
     * @param zkNode
     *            the zkNode to set
     */
    private void setZkNode(String id, WorkerNodeTracker zkNode) {
        this.zkId = id;
        zkNode.addListener(new OperationsNodeListener() {

            @Override
            public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
                addOpsServer(nodeInfo);
            }

            @Override
            public void onNodeAdded(OperationsNodeInfo nodeInfo) {
                addOpsServer(nodeInfo);
            }

            @Override
            public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
                String opId = getServerID(nodeInfo.getConnectionInfo());
                if (!zkId.equals(opId)) {
                    NeighborConnection<T, V> connection = neigbors.remove(opId);
                    if (connection != null) {
                        connection.shutdown();
                    }
                    LOG.info("Operations server {} removed to {} Neighbors list ({}). Now {} neighbors", opId, neigbors.size());
                }
            }
        });

        List<OperationsNodeInfo> nodes = zkNode.getCurrentOperationServerNodes();
        for (OperationsNodeInfo opServer : nodes) {
            addOpsServer(opServer);
        }
        LOG.debug("Neighbor zk init complete: {} neighbors registered.", neigbors.size());
    }

    private void addOpsServer(OperationsNodeInfo opServer) {
        LOG.trace("[{}] Building id for {}", zkId, opServer.getConnectionInfo());
        String opId = getServerID(serviceType, opServer.getConnectionInfo());
        if (!zkId.equals(opId)) {
            LOG.trace("Adding {} to {}", opId, neigbors);
            neigbors.putIfAbsent(opId, new NeighborConnection<T, V>(opServer.getConnectionInfo(), maxNumberNeighborConnections, template));
            neigbors.get(opId).start();
            LOG.info("Operations server {} added/updated to {} Neighbors list. Now {} neighbors", opId, zkId, neigbors.size());
        }
    }
}
