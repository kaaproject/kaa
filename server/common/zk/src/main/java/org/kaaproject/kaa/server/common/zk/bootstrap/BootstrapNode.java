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

package org.kaaproject.kaa.server.common.zk.bootstrap;

import java.io.IOException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.kaaproject.kaa.server.common.zk.WorkerNodeTracker;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapSupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class BootstrapNode.
 */
public class BootstrapNode extends WorkerNodeTracker {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(BootstrapNode.class);

    /** The node info. */
    private BootstrapNodeInfo nodeInfo;

    /**
     * Instantiates a new bootstrap node.
     *
     * @param nodeInfo the node info
     * @param zkHostPortList the zk host port list
     * @param retryPolicy the retry policy
     */
    public BootstrapNode(BootstrapNodeInfo nodeInfo, String zkHostPortList,
            RetryPolicy retryPolicy) {
        super(zkHostPortList, retryPolicy);
        this.nodeInfo = nodeInfo;
    }

    @Override
    public boolean createZkNode() throws IOException {
        return doZKClientAction(new ZKClientAction() {
            @Override
            public void doWithZkClient(CuratorFramework client) throws Exception {
                nodePath = client
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(
                                BOOTSTRAP_SERVER_NODE_PATH
                                        + BOOTSTRAP_SERVER_NODE_PATH,
                                bootstrapNodeAvroConverter.get().toByteArray(nodeInfo));
                LOG.info("Created node with path: " + nodePath);
            }
        });
    }

    /**
     * Updates current ZK node data.
     *
     * @param currentNodeInfo the current node info
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void updateNodeData(BootstrapNodeInfo currentNodeInfo)
            throws IOException {
        this.nodeInfo = currentNodeInfo;
        doZKClientAction(new ZKClientAction() {
            @Override
            public void doWithZkClient(CuratorFramework client) throws Exception {
                client.setData().forPath(nodePath,
                        bootstrapNodeAvroConverter.get().toByteArray(nodeInfo));
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
        
        LOG.trace("Bootstrap Node update statistics for {} channel", channelType.toString());
        for(BootstrapSupportedChannel channel : nodeInfo.getSupportedChannelsArray()) {
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
                case KAATCP:
                    ZkKaaTcpStatistics zkTcpStats = (ZkKaaTcpStatistics)channel.getZkChannel().getChannelStatistics();
                    stats = zkTcpStats.getZkStatistics();
                    break;
                }
                if (stats != null) {
                    stats.setDeltaCalculationCount(deltaCalculationCount);
                    stats.setProcessedRequestCount(processedRequestCount);
                    stats.setRegisteredUsersCount(registeredUsersCount);
                    
                    try {
                        client.setData().forPath(nodePath,
                                bootstrapNodeAvroConverter.get().toByteArray(nodeInfo));
                        LOG.trace("Bootstrap Node update statistics for {} channel, data set: {},{},{}", 
                                channelType.toString(),
                                deltaCalculationCount,
                                processedRequestCount,
                                registeredUsersCount);
                    } catch (Exception e) {
                        LOG.error("Unknown Error", e);
                        close();
                    }
                }
                break;
            }
        }
    }
}
