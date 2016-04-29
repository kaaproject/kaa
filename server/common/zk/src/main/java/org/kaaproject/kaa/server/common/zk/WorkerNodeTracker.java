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

package org.kaaproject.kaa.server.common.zk;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class WorkerNodeTracker.
 */
public abstract class WorkerNodeTracker extends ControlNodeTracker {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(WorkerNodeTracker.class);

    /** The endpoint cache. */
    private PathChildrenCache endpointCache;

    /** The bootstrap cache. */
    private PathChildrenCache bootstrapCache;

    /** The endpoint listeners. */
    private List<OperationsNodeListener> endpointListeners;

    /** The bootstrap listeners. */
    private List<BootstrapNodeListener> bootstrapListeners;

    /** Correspondence between each functioning operation node's thriftHost+thriftPort
     * string and its start time */
    private Map<String, Long> operationNodesStartTimes;

    /** Correspondence between each functioning bootstrap node's thriftHost+thriftPort
     * string and its start time */
    private Map<String, Long> bootstrapNodesStartTimes;

    /**
     * Instantiates a new worker node tracker.
     *
     * @param zkHostPortList
     *            the zk host port list
     * @param retryPolicy
     *            the retry policy
     */
    public WorkerNodeTracker(String zkHostPortList, RetryPolicy retryPolicy) {
        super(zkHostPortList, retryPolicy);
        init();
    }

    /**
     * Instantiates a new worker node tracker.
     *
     * @param zkHostPortList
     *            the zk host port list
     * @param sessionTimeoutMs
     *            the session timeout
     * @param connectionTimeoutMs
     *            the connection timeout
     * @param retryPolicy
     *            the retry policy
     */
    public WorkerNodeTracker(String zkHostPortList, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy) {
        super(zkHostPortList, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
        init();
    }

    private void init() {
        endpointCache = new PathChildrenCache(client, OPERATIONS_SERVER_NODE_PATH, true);
        bootstrapCache = new PathChildrenCache(client, BOOTSTRAP_SERVER_NODE_PATH, true);
        endpointListeners = new CopyOnWriteArrayList<OperationsNodeListener>();
        bootstrapListeners = new CopyOnWriteArrayList<BootstrapNodeListener>();
        operationNodesStartTimes = new HashMap<String, Long>();
        bootstrapNodesStartTimes = new HashMap<String, Long>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.common.zk.ControlNodeTracker#start()
     */
    @Override
    public void start() throws Exception {
        super.start();
        endpointCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                case CHILD_ADDED:
                    endpointAdded(event.getData());
                    break;
                case CHILD_UPDATED:
                    endpointUpdated(event.getData());
                    break;
                case CHILD_REMOVED:
                    endpointRemoved(event.getData());
                    break;
                default:
                    break;
                }
            }
        });
        endpointCache.start(StartMode.NORMAL);

        bootstrapCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                LOG.info("Bootstrap node event: " + event.getType());
                switch (event.getType()) {
                case CHILD_ADDED:
                    bootstrapAdded(event.getData());
                    break;
                case CHILD_UPDATED:
                    bootstrapUpdated(event.getData());
                    break;
                case CHILD_REMOVED:
                    bootstrapRemoved(event.getData());
                    break;
                default:
                    break;
                }
            }
        });
        bootstrapCache.start(StartMode.NORMAL);
    }

    /**
     * Gets the current endpoint nodes.
     *
     * @return the current endpoint nodes
     */
    public List<OperationsNodeInfo> getCurrentOperationServerNodes() {
        List<ChildData> nodesData = endpointCache != null ? endpointCache.getCurrentData() : new ArrayList<ChildData>();
        Map<ConnectionInfoKey, OperationsNodeInfo> uniqueMap = new HashMap<>();

        for (ChildData data : nodesData) {
            OperationsNodeInfo newNodeInfo = extractOperationServerInfo(data);
            ConnectionInfoKey key = new ConnectionInfoKey(newNodeInfo.getConnectionInfo());
            OperationsNodeInfo oldNodeInfo = uniqueMap.get(key);
            if (oldNodeInfo != null) {
                if (newNodeInfo.getTimeStarted() >= oldNodeInfo.getTimeStarted()) {
                    uniqueMap.put(key, newNodeInfo);
                }
            } else {
                uniqueMap.put(key, newNodeInfo);
            }
        }

        return new ArrayList<>(uniqueMap.values());
    }

    /**
     * Gets the current bootstrap nodes.
     *
     * @return the current bootstrap nodes
     */
    public List<BootstrapNodeInfo> getCurrentBootstrapNodes() {
        List<ChildData> nodesData = bootstrapCache != null ? bootstrapCache.getCurrentData() : new ArrayList<ChildData>();
        List<BootstrapNodeInfo> result = new ArrayList<>(nodesData.size());
        for (ChildData data : nodesData) {
            result.add(extractBootstrapServerInfo(data));
        }
        return result;
    }

    /**
     * Operations Node added.
     *
     * @param data
     *            the data
     */
    protected void endpointAdded(ChildData data) {
        OperationsNodeInfo nodeInfo = extractOperationServerInfo(data);
        String endpointAddress = constructEndpointAddress(nodeInfo);
        operationNodesStartTimes.put(endpointAddress, nodeInfo.getTimeStarted());
        for (OperationsNodeListener listener : endpointListeners) {
            listener.onNodeAdded(nodeInfo);
        }
    }

    /**
     * Operations Node updated.
     *
     * @param data
     *            the data
     */
    protected void endpointUpdated(ChildData data) {
        OperationsNodeInfo nodeInfo = extractOperationServerInfo(data);
        String endpointAddress = constructEndpointAddress(nodeInfo);
        operationNodesStartTimes.put(endpointAddress, nodeInfo.getTimeStarted());
        for (OperationsNodeListener listener : endpointListeners) {
            listener.onNodeUpdated(nodeInfo);
        }
    }

    /**
     * Operations Node removed.
     *
     * @param data
     *            the data
     */
    protected void endpointRemoved(ChildData data) {
        OperationsNodeInfo nodeInfo = extractOperationServerInfo(data);
        String endpointAddress = constructEndpointAddress(nodeInfo);
        Long removeTime = nodeInfo.getTimeStarted();
        Long updateTime = operationNodesStartTimes.get(endpointAddress);
        if (updateTime == null || removeTime >= updateTime) {
            operationNodesStartTimes.remove(endpointAddress);
            for (OperationsNodeListener listener : endpointListeners) {
                listener.onNodeRemoved(nodeInfo);
            }
        } else {
            LOG.debug("Ignoring [{}] endpoint removal, as it was before add/update", endpointAddress);
        }
    }

    /**
     * Bootstrap added.
     *
     * @param data
     *            the data
     */
    protected void bootstrapAdded(ChildData data) {
        BootstrapNodeInfo nodeInfo = extractBootstrapServerInfo(data);
        String bootstrapAddress = constructBootstrapAddress(nodeInfo);
        bootstrapNodesStartTimes.put(bootstrapAddress, nodeInfo.getTimeStarted());
        for (BootstrapNodeListener listener : bootstrapListeners) {
            listener.onNodeAdded(nodeInfo);
        }
    }

    /**
     * Bootstrap updated.
     *
     * @param data
     *            the data
     */
    protected void bootstrapUpdated(ChildData data) {
        BootstrapNodeInfo nodeInfo = extractBootstrapServerInfo(data);
        String bootstrapAddress = constructBootstrapAddress(nodeInfo);
        bootstrapNodesStartTimes.put(bootstrapAddress, nodeInfo.getTimeStarted());
        for (BootstrapNodeListener listener : bootstrapListeners) {
            listener.onNodeUpdated(nodeInfo);
        }
    }

    /**
     * Bootstrap removed.
     *
     * @param data
     *            the data
     */
    protected void bootstrapRemoved(ChildData data) {
        BootstrapNodeInfo nodeInfo = extractBootstrapServerInfo(data);
        String bootstrapAddress = constructBootstrapAddress(nodeInfo);
        Long removeTime = nodeInfo.getTimeStarted();
        Long updateTime = bootstrapNodesStartTimes.get(bootstrapAddress);
        if (updateTime == null || removeTime >= updateTime) {
            for (BootstrapNodeListener listener : bootstrapListeners) {
                listener.onNodeRemoved(nodeInfo);
            }
        } else {
            LOG.debug("Ignoring [{}] bootstrap removal, as it was before add/update", bootstrapAddress);
        }
    }

    /**
     * Adds the listener.
     *
     * @param listener
     *            the listener
     */
    public void addListener(OperationsNodeListener listener) {
        LOG.debug("Listener registered: " + listener);
        endpointListeners.add(listener);
    }

    /**
     * Removes the listener.
     *
     * @param listener
     *            the listener
     * @return true, if successful
     */
    public boolean removeListener(OperationsNodeListener listener) {
        if (endpointListeners.remove(listener)) {
            LOG.debug("Listener removed: " + listener);
            return true;
        } else {
            LOG.debug("Listener not found: " + listener);
            return false;
        }
    }

    /**
     * Adds the listener.
     *
     * @param listener
     *            the listener
     */
    public void addListener(BootstrapNodeListener listener) {
        LOG.debug("Listener registered: " + listener);
        bootstrapListeners.add(listener);
    }

    /**
     * Removes the listener.
     *
     * @param listener
     *            the listener
     * @return true, if successful
     */
    public boolean removeListener(BootstrapNodeListener listener) {
        if (bootstrapListeners.remove(listener)) {
            LOG.debug("Listener removed: " + listener);
            return true;
        } else {
            LOG.debug("Listener not found: " + listener);
            return false;
        }
    }

    /**
     * Extract endpoint server info.
     *
     * @param currentData
     *            the current data
     * @return the endpoint node info
     */
    private OperationsNodeInfo extractOperationServerInfo(ChildData currentData) {
        OperationsNodeInfo endpointServerInfo = null;
        try {
            endpointServerInfo = operationsNodeAvroConverter.get().fromByteArray(currentData.getData(), null);
        } catch (IOException e) {
            LOG.error("error reading control server info", e);
        }
        return endpointServerInfo;
    }

    /**
     * Extract bootstrap server info.
     *
     * @param currentData
     *            the current data
     * @return the bootstrap node info
     */
    private BootstrapNodeInfo extractBootstrapServerInfo(ChildData currentData) {
        BootstrapNodeInfo bootstrapServerInfo = null;
        try {
            bootstrapServerInfo = bootstrapNodeAvroConverter.get().fromByteArray(currentData.getData(), null);
        } catch (IOException e) {
            LOG.error("error reading control server info", e);
        }
        return bootstrapServerInfo;
    }

    private String constructEndpointAddress(OperationsNodeInfo nodeInfo) {
        return nodeInfo.getConnectionInfo().getThriftHost() + ":" +
                String.valueOf(nodeInfo.getConnectionInfo().getThriftPort());
    }

    private String constructBootstrapAddress(BootstrapNodeInfo nodeInfo) {
        return nodeInfo.getConnectionInfo().getThriftHost() + ":" +
                String.valueOf(nodeInfo.getConnectionInfo().getThriftPort());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        endpointCache.close();
        bootstrapCache.close();
        super.close();
    }
}
