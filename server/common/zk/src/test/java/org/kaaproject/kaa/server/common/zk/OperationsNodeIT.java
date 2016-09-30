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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.Timing;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class OperationsNodeIT {

  private static final int NEW_HTTP_ID = BootstrapNodeIT.HTTP_ID + 1;
  private static final String BOOTSTRAP_NODE_HOST = "192.168.0.202";
  private static final String ENDPOINT_NODE_HOST = "192.168.0.101";
  private CuratorFramework zkClient;
  private TestingCluster cluster;

  @Before
  public void beforeTest() {
    try {
      cluster = new TestingCluster(3);
      cluster.start();
      zkClient = CuratorFrameworkFactory.newClient(cluster.getConnectString(), buildDefaultRetryPolicy());
      zkClient.start();
    } catch (Exception e) {
      System.err.println("Unable to initialize cluster before test! " + e);
    }
  }

  @After
  public void afterTest() {
    try {
      zkClient.close();
      cluster.close();
    } catch (Exception e) {
      System.err.println("Unable to shutdown cluster after test! " + e);
    }
  }

  @Test
  public void endpointListenerTest() throws Exception {
    Timing timing = new Timing();

    OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

    BootstrapNodeInfo bootstrapNodeInfo = buildBootstrapNodeInfo();

    BootstrapNode bootstrapNode = new BootstrapNode(bootstrapNodeInfo, zkClient);
    OperationsNodeListener mockListener = mock(OperationsNodeListener.class);
    bootstrapNode.addListener(mockListener);
    bootstrapNode.start();

    OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, zkClient);

    endpointNode.start();

    timing.sleepABit();
    verify(mockListener).onNodeAdded(endpointNodeInfo);

    assertNotNull(bootstrapNode.getCurrentOperationServerNodes());
    assertEquals(1, bootstrapNode.getCurrentOperationServerNodes().size());

    OperationsNodeInfo testNodeInfo = bootstrapNode.getCurrentOperationServerNodes().get(0);
    assertNotNull(testNodeInfo.getTransports());
    assertEquals(2, testNodeInfo.getTransports().size());
    assertNotNull(testNodeInfo.getTransports().get(0));
    assertEquals(BootstrapNodeIT.HTTP_ID, testNodeInfo.getTransports().get(0).getId().intValue());
    assertEquals(BootstrapNodeIT.TCP_ID, testNodeInfo.getTransports().get(1).getId().intValue());
    assertNotNull(testNodeInfo.getTransports().get(0).getConnectionInfo());

    endpointNodeInfo.getTransports().get(0).setId(NEW_HTTP_ID);

    endpointNode.updateNodeData(endpointNodeInfo);
    timing.sleepABit();
    verify(mockListener).onNodeUpdated(endpointNodeInfo);

    assertNotNull(bootstrapNode.getCurrentOperationServerNodes());
    assertEquals(1, bootstrapNode.getCurrentOperationServerNodes().size());
    assertNotNull(bootstrapNode.getCurrentOperationServerNodes().get(0));
    testNodeInfo = bootstrapNode.getCurrentOperationServerNodes().get(0);
    assertNotNull(testNodeInfo.getTransports());
    assertEquals(NEW_HTTP_ID, testNodeInfo.getTransports().get(0).getId().intValue());

    endpointNode.close();
    timing.sleepABit();
    verify(mockListener).onNodeRemoved(endpointNodeInfo);
    Assert.assertTrue(bootstrapNode.removeListener(mockListener));
    Assert.assertFalse(bootstrapNode.removeListener(mockListener));
    bootstrapNode.close();
  }

  @Test
  public void endpointExceptionTest() throws Exception {
    OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

    OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, zkClient);
    endpointNode.start();
    endpointNode.doZkClientAction(new ControlNodeTracker.ZkClientAction() {

      @Override
      public void doWithZkClient(CuratorFramework client) throws Exception {
        throw new Exception("for test");
      }
    });

    //check if operations node changed and deleted child node
    //after unexpected exception, as child node was ephemeral and connection lost
    String OPERATIONS_SERVER_NODE_PATH = "/operationsServerNodes";
    Stat stat = zkClient.checkExists().forPath(OPERATIONS_SERVER_NODE_PATH);
    int cversion = stat.getCversion();
    int numChildren = stat.getNumChildren();
    Assert.assertEquals(cversion, 2);
    Assert.assertEquals(numChildren, 0);
    endpointNode.close();
  }

  @Test(expected = IOException.class)
  public void endpointIOExceptionTest() throws Exception {
    OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();


    OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, zkClient);
    endpointNode.start();
    endpointNode.doZkClientAction(new ControlNodeTracker.ZkClientAction() {

      @Override
      public void doWithZkClient(CuratorFramework client) throws Exception {
        throw new Exception("for test");
      }
    }, true);

    Assert.assertFalse(endpointNode.isConnected());
    endpointNode.close();
  }

  @Test
  public void outdatedRemovalTest() throws Exception {
    Timing timing = new Timing();

    OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();
    BootstrapNodeInfo bootstrapNodeInfo = buildBootstrapNodeInfo();

    BootstrapNode bootstrapNode = new BootstrapNode(bootstrapNodeInfo, zkClient);
    OperationsNodeListener mockListener = mock(OperationsNodeListener.class);
    bootstrapNode.addListener(mockListener);
    bootstrapNode.start();

    OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, zkClient);

    endpointNode.start();
    timing.sleepABit();
    verify(mockListener).onNodeAdded(endpointNodeInfo);

    OperationsNodeInfo endpointNodeInfoWithGreaterTimeStarted = buildOperationsNodeInfo();
    OperationsNode endpointNodeWithGreaterTimeStarted = new OperationsNode(endpointNodeInfoWithGreaterTimeStarted, zkClient);

    endpointNodeWithGreaterTimeStarted.start();
    timing.sleepABit();

    endpointNode.close();
    timing.sleepABit();
    verify(mockListener, never()).onNodeRemoved(endpointNodeInfo);

    endpointNodeWithGreaterTimeStarted.close();
    timing.sleepABit();
    verify(mockListener).onNodeRemoved(endpointNodeInfoWithGreaterTimeStarted);

    bootstrapNode.close();
  }

  private RetryPolicy buildDefaultRetryPolicy() {
    return new ExponentialBackoffRetry(100, 1);
  }

  private BootstrapNodeInfo buildBootstrapNodeInfo() {
    BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
    ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10, 11, 12, 45, 34, 23, 67, 89, 66, 12});
    nodeInfo.setConnectionInfo(new ConnectionInfo(BOOTSTRAP_NODE_HOST, 1000, testKeyData));
    nodeInfo.setTimeStarted(System.currentTimeMillis());
    nodeInfo.setTransports(BootstrapNodeIT.getHttpAndTcpTransportMD());
    return nodeInfo;
  }

  private OperationsNodeInfo buildOperationsNodeInfo() {
    OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
    ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10, 11, 12, 45, 34, 23, 67, 89, 66, 12});
    nodeInfo.setConnectionInfo(new ConnectionInfo(ENDPOINT_NODE_HOST, 1000, testKeyData));
    nodeInfo.setLoadInfo(new LoadInfo(1, 1.0));
    nodeInfo.setTimeStarted(System.currentTimeMillis());
    nodeInfo.setTransports(BootstrapNodeIT.getHttpAndTcpTransportMD());
    return nodeInfo;
  }
}
