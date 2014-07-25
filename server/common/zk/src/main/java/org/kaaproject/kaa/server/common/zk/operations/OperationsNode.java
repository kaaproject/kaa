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

package org.kaaproject.kaa.server.common.zk.operations;

import java.io.IOException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.kaaproject.kaa.server.common.zk.WorkerNodeTracker;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class OperationsNode.
 */
public class OperationsNode extends WorkerNodeTracker {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(BootstrapNode.class);

    /** The node info. */
    private OperationsNodeInfo nodeInfo;

    /** The node path. */
    private String nodePath;

    /**
     * Instantiates a new endpoint node.
     *
     * @param nodeInfo the node info
     * @param zkHostPortList the zk host port list
     * @param retryPolicy the retry policy
     */
    public OperationsNode(OperationsNodeInfo nodeInfo, String zkHostPortList, RetryPolicy retryPolicy) {
        super(zkHostPortList, retryPolicy);
        this.nodeInfo = nodeInfo;
        this.nodeInfo.setTimeStarted(System.currentTimeMillis());
    }

    /**
     * Updates current ZK node data.
     *
     * @param currentNodeInfo the current node info
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void updateNodeData(OperationsNodeInfo currentNodeInfo)
            throws IOException {
        this.nodeInfo = currentNodeInfo;
        doZKClientAction(new ZKClientAction() {
            @Override
            public void doWithZkClient(CuratorFramework client) throws Exception {
                client.setData().forPath(nodePath,
                        operationsNodeAvroConverter.get().toByteArray(nodeInfo));
            }
        });
    }

    /**
     * Updates statistics of current NodeData
     * @param deltaCalculationCount
     * @param processedRequestCount
     * @param RegisteredUsersCount
     * @throws IOException
     */
    public void updateNodeStatsValues(ZkChannelType channelType,
                                      int deltaCalculationCount,
                                      int processedRequestCount,
                                      int registeredUsersCount)
            throws IOException {
        for(SupportedChannel channel : nodeInfo.getSupportedChannelsArray()) {
            if(channel.getZkChannel().getChannelType().equals(channelType)) {
                BaseStatistics stats = null;
                switch (channelType) { //NOSONAR
                case HTTP:
                    ZkHttpStatistics zkHttpStats = (ZkHttpStatistics)channel.getZkChannel().getChannelStatistics();
                    stats = zkHttpStats.getZkStatistics();
                    break;
                case HTTP_LP:
                    ZkHttpLpStatistics zkHttpLpStats = (ZkHttpLpStatistics)channel.getZkChannel().getChannelStatistics();
                    stats = zkHttpLpStats.getZkStatistics();
                    break;
                }
                if (stats != null) {
                    stats.setDeltaCalculationCount(deltaCalculationCount);
                    stats.setProcessedRequestCount(processedRequestCount);
                    stats.setRegisteredUsersCount(registeredUsersCount);
                }
                break;
            }
        }
        try {
            client.setData().forPath(nodePath,
                    operationsNodeAvroConverter.get().toByteArray(nodeInfo));
        } catch (Exception e) {
            LOG.error("Unknown Error", e);
            close();
        }
    }

    @Override
    public boolean createZkNode() throws IOException{
        return doZKClientAction(new ZKClientAction() {
            @Override
            public void doWithZkClient(CuratorFramework client) throws Exception {
                nodeInfo.setTimeStarted(System.currentTimeMillis());
                nodePath = client
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(
                                OPERATIONS_SERVER_NODE_PATH
                                        + OPERATIONS_SERVER_NODE_PATH,
                                operationsNodeAvroConverter.get().toByteArray(nodeInfo));
                LOG.info("Created node with path: " + nodePath);
            }
        });
    }

    /**
     * Self NodeInfo getter.
     * @return OperationsNodeInfo
     */
    public OperationsNodeInfo getSelfNodeInfo() {
        return nodeInfo;
    }
}
