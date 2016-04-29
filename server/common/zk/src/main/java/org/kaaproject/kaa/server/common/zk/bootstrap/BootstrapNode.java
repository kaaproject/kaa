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

package org.kaaproject.kaa.server.common.zk.bootstrap;

import java.io.IOException;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.kaaproject.kaa.server.common.zk.WorkerNodeTracker;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class BootstrapNode.
 */
public class BootstrapNode extends WorkerNodeTracker {

    /** The Constant LOG. */
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

    public BootstrapNodeInfo getNodeInfo() {
        return nodeInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BootstrapNode [nodeInfo=");
        builder.append(nodeInfo);
        builder.append("]");
        return builder.toString();
    }
}
