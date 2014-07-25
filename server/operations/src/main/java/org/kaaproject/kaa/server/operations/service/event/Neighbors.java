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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Neighbors Class.
 * Collect all Operations Servers neighbors through listening ZooKeeper OperationsNode changes.
 * Use thriftHost:thriftPort as server Key and hold Hashtable of NeighborConnection. 
 * @author Andrey Panasenko
 *
 */
public class Neighbors implements OperationsNodeListener {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(Neighbors.class);
    
    /** Event service */
    private DefaultEventService eventService;
    
    /** Self ID in thriftHost:thriftPort */
    private String selfId;
    
    /** Unique integer, used in logging mostly in tests */
    private int uniqId;
    
    private Random rnd = new Random();

    /** Hashtable of NeighborConnection, Key - thriftHost:thriftPort */
    private Hashtable<String, NeighborConnection> neigbors; //NOSONAR
    
    /** Synchronize object, used during neighbors list updates */
    private Object neighborsUpdateSync = new Object();
    
    /** timer and task, used to postpone correct ZK node listener registration */
    private Timer timerUpdate;
    private TimerTask taskUpdate;
    
    /** ZooKeeper Operations Node */
    private OperationsNode zkNode;
    /** Synchronize object, initZK() waite until zkNode is set using this object */
    private Object zkNodeSetSync = new Object();
    
    
    /**
     * Default constructor.
     * If zkNode is not set, postpone timer task to wait until node is set.
     * @param eventService
     */
    public Neighbors(DefaultEventService eventService) {
        uniqId = rnd.nextInt(1000);
        LOG.info("Neighbors instance {} created", uniqId);
        this.eventService = eventService;
        neigbors  = new Hashtable<>();
        synchronized (zkNodeSetSync) {
            if (zkNode != null) {
                LOG.debug("Neighbors instance {} ZK node set(without timer), init() startig....", uniqId);
                initZK();
            } else {
                timerUpdate = new Timer();
                taskUpdate = new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (zkNodeSetSync) {
                            if(zkNode == null) {
                                try {
                                    zkNodeSetSync.wait();
                                    LOG.debug("Neighbors instance {} ZK node set, init() startig....", uniqId);
                                    initZK();
                                } catch (InterruptedException e) {
                                    LOG.error("Wait to set ZK node, was interrupted. Error initializing EventService",e);
                                }
                            } else {
                                LOG.debug("Neighbors instance {} ZK node set(without wait), init() startig....", uniqId);
                                initZK();
                            }
                        }
                    }
                };
                //100 msec delay in reading nodes list
                timerUpdate.schedule(taskUpdate, 0);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeAdded(OperationsNodeInfo nodeInfo) {
        String opId = getOperationsServerID(nodeInfo.getConnectionInfo());
        if (!selfId.equals(opId)) {
            synchronized (neighborsUpdateSync) {
                if (!neigbors.contains(opId)) {
                    neigbors.put(opId, new NeighborConnection(eventService, nodeInfo.getConnectionInfo()));
                    LOG.info("New Operations server {} added to {} Neighbors list ({}). Now {} neighbors",opId, selfId, uniqId, neigbors.size());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
        String opId = getOperationsServerID(nodeInfo.getConnectionInfo());
        if (!selfId.equals(opId)) {
            synchronized (neighborsUpdateSync) {
                if (!neigbors.contains(opId)) {
                    neigbors.put(opId, new NeighborConnection(eventService, nodeInfo.getConnectionInfo()));
                    LOG.info("Operations server {} updated in Neighbors list. Now {} neighbors",opId, neigbors.size());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
        String opId = getOperationsServerID(nodeInfo.getConnectionInfo());
        if (!selfId.equals(opId)) {
            NeighborConnection server = null;
            synchronized (neighborsUpdateSync) {
                server = neigbors.remove(opId);
            }
            if (server != null) {
                server.shutdown();
            }
            LOG.info("Operations server {} removed from {} Neighbors list({}). Now {} neighbors",opId, selfId, uniqId, neigbors.size());
        }
    }

    /**
     * Initialize ZooKeeper registration.
     */
    private void initZK() {
        selfId = getOperationsServerID(zkNode.getSelfNodeInfo().getConnectionInfo());
        zkNode.addListener(this);
        updateOperationsServersList();
        LOG.debug("OperationsServer {} Nighbors({}) ZooKeeper initialization comlete. Now {} neighbors.",selfId, uniqId, neigbors.size());
        return;
    }
    
    /**
     * Load initial Operations Servers list from ZK node.
     */
    private void updateOperationsServersList() {
        LOG.debug("OperationsServer {} Nighbors({}) Update server list. Now {} neighbors.",selfId, uniqId, neigbors.size());        
        OperationsNode node = eventService.getConfig().getZkNode();
        List<OperationsNodeInfo> nodes = node.getCurrentOperationServerNodes();
        for(OperationsNodeInfo opServer : nodes) {
            String opId = getOperationsServerID(opServer.getConnectionInfo());
            if (!selfId.equals(opId)) {
                synchronized (neighborsUpdateSync) {
                    if (!neigbors.containsKey(opId)) {
                        neigbors.put(opId, new NeighborConnection(eventService, opServer.getConnectionInfo()));
                        LOG.info("New Operations server {} added to {} Neighbors list({}).",opId, selfId, uniqId);
                    }
                }
            }
        }
    }
    
    /**
     * Shutdown all neighbors connections and cancel timer task if exist.
     */
    public void shutdown() {
        LOG.info("Operations server {}  Neighbors list({}) shutdown all Neighbor connections....", selfId, uniqId);
        synchronized (neighborsUpdateSync) {
            for(NeighborConnection neigbor : neigbors.values()) {
                LOG.info("Operations server {}  Neighbors list({}) shutdown connection to {}", selfId, uniqId, neigbor.getId());
                neigbor.shutdown();
            }
            neigbors.clear();
        }
        if (timerUpdate != null) {
            timerUpdate.cancel();
            timerUpdate = null;
        }
        
    }
    
    /**
     * Return current list of Neighbors.
     * @return List<NeighborConnection> neighbors.
     */
    public List<NeighborConnection> getNeighbors() {
        synchronized (neighborsUpdateSync) {
            return new LinkedList<NeighborConnection>(neigbors.values()); //NOSONAR
        }
    }
    
    /**
     * Return specific Neighbor connection by Id
     * @param serverId String in format thriftHost:thriftPort
     * @return NeighborConnection or null if such server not exist
     */
    public NeighborConnection getNeghborConnection(String serverId) {
        synchronized (neighborsUpdateSync) {
            return neigbors.get(serverId);
        }
    }
    
    /**
     * Build server ID from ConnectionInfo object.
     * @param info ConnectionInfo
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
     * Self ID getter.
     * @return String
     */
    public String getSelfId() {
        return selfId;
    }

    /**
     * ZK node getter.
     * @return the zkNode
     */
    public OperationsNode getZkNode() {
        return zkNode;
    }

    /**
     * Zk Node setter. Notify ZK initialization.
     * @param zkNode the zkNode to set
     */
    public void setZkNode(OperationsNode zkNode) {
        synchronized (zkNodeSetSync) {
            this.zkNode = zkNode;
            zkNodeSetSync.notify();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Neighbors [uniqId=" + uniqId + "]";
    }
    
    
}
