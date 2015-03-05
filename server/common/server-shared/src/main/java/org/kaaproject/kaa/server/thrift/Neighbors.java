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

package org.kaaproject.kaa.server.thrift;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    public Neighbors(T template, int maxNumberNeighborConnections) {
        this.template = template;
        this.maxNumberNeighborConnections = maxNumberNeighborConnections;
        this.neigbors = new ConcurrentHashMap<String, NeighborConnection<T, V>>();
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
    public static String getOperationsServerID(ConnectionInfo info) {
        StringBuffer sb = new StringBuffer();
        sb.append(info.getThriftHost());
        sb.append(":");
        sb.append(info.getThriftPort());
        return sb.toString();
    }

    /**
     * Zk Node setter. Notify ZK initialization.
     * 
     * @param zkNode
     *            the zkNode to set
     */
    public void setZkNode(String id, WorkerNodeTracker zkNode) {
        this.zkId = id;
        zkNode.addListener(new OperationsNodeListener() {

            @Override
            public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
                String opId = getOperationsServerID(nodeInfo.getConnectionInfo());
                if (!zkId.equals(opId)) {
                    neigbors.putIfAbsent(opId,
                            new NeighborConnection<T, V>(nodeInfo.getConnectionInfo(), maxNumberNeighborConnections, template)).start();
                    LOG.info("Operations server {} added to {} Neighbors list ({}). Now {} neighbors", opId, neigbors.size());
                }
            }

            @Override
            public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
                String opId = getOperationsServerID(nodeInfo.getConnectionInfo());
                if (!zkId.equals(opId)) {
                    NeighborConnection<T, V> connection = neigbors.remove(opId);
                    if (connection != null) {
                        connection.shutdown();
                    }
                    LOG.info("Operations server {} removed to {} Neighbors list ({}). Now {} neighbors", opId, neigbors.size());
                }
            }

            @Override
            public void onNodeAdded(OperationsNodeInfo nodeInfo) {
                String opId = getOperationsServerID(nodeInfo.getConnectionInfo());
                if (!zkId.equals(opId)) {
                    neigbors.putIfAbsent(opId,
                            new NeighborConnection<T, V>(nodeInfo.getConnectionInfo(), maxNumberNeighborConnections, template)).start();
                    LOG.info("Operations server {} added to {} Neighbors list ({}). Now {} neighbors", opId, neigbors.size());
                }
            }
        });

        List<OperationsNodeInfo> nodes = zkNode.getCurrentOperationServerNodes();
        for (OperationsNodeInfo opServer : nodes) {
            String opId = getOperationsServerID(opServer.getConnectionInfo());
            if (!zkId.equals(opId)) {
                neigbors.putIfAbsent(opId,
                        new NeighborConnection<T, V>(opServer.getConnectionInfo(), maxNumberNeighborConnections, template)).start();
                LOG.info("Operations server {} added to Neighbors list.", opId, zkId);
            }
        }
        LOG.debug("Neighbor zk init complete: {} neighbors registered.", neigbors.size());
    }
}
