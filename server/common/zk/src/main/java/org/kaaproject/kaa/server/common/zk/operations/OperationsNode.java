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

package org.kaaproject.kaa.server.common.zk.operations;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.kaaproject.kaa.server.common.zk.WorkerNodeTracker;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The Class OperationsNode.
 */
public class OperationsNode extends WorkerNodeTracker {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(BootstrapNode.class);

  /**
   * The node info.
   */
  private OperationsNodeInfo nodeInfo;

  /**
   * Instantiates a new endpoint node.
   *
   * @param nodeInfo the node info
   */
  public OperationsNode(OperationsNodeInfo nodeInfo) {
    super();
    this.nodeInfo = nodeInfo;
    this.nodeInfo.setTimeStarted(System.currentTimeMillis());
  }

  /**
   * Instantiates a new endpoint node.
   *
   * @param nodeInfo the node info
   * @param zkClient Zookeeper client
   */
  public OperationsNode(OperationsNodeInfo nodeInfo, CuratorFramework zkClient) {
    super(zkClient);
    this.nodeInfo = nodeInfo;
    this.nodeInfo.setTimeStarted(System.currentTimeMillis());
  }

  /**
   * Updates current ZK node data.
   *
   * @param currentNodeInfo the current node info
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void updateNodeData(OperationsNodeInfo currentNodeInfo) throws IOException {
    this.nodeInfo = currentNodeInfo;
    doZkClientAction(new ZkClientAction() {
      @Override
      public void doWithZkClient(CuratorFramework client) throws Exception {
        client.setData().forPath(nodePath, operationsNodeAvroConverter.get().toByteArray(nodeInfo));
      }
    });
  }

  @Override
  public boolean createZkNode() throws IOException {
    return doZkClientAction(new ZkClientAction() {
      @Override
      public void doWithZkClient(CuratorFramework client) throws Exception {
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        nodePath = client
            .create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
            .forPath(OPERATIONS_SERVER_NODE_PATH + OPERATIONS_SERVER_NODE_PATH,
                operationsNodeAvroConverter.get().toByteArray(nodeInfo));
        LOG.info("Created node with path: " + nodePath);
      }
    });
  }

  /**
   * Self NodeInfo getter.
   *
   * @return OperationsNodeInfo
   */
  public OperationsNodeInfo getNodeInfo() {
    return nodeInfo;
  }

  @Override
  public String toString() {
    return "OperationsNode {" + "host =" + nodeInfo.getConnectionInfo().getThriftHost() + "port =" + nodeInfo.getConnectionInfo().getThriftPort()
        + "timeStarted =" + nodeInfo.getTimeStarted() + '}';
  }
}
