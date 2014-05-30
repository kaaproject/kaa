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

package org.kaaproject.kaa.server.common.zk;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.Timing;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;

public class OperationsNodeIT {

    private static final String BOOTSTRAP_NODE_HOST = "192.168.0.202";
    private static final String ENDPOINT_NODE_HOST = "192.168.0.101";

    @Test
    public void endpointListenerTest() throws Exception {
        Timing timing = new Timing();
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        try {
            OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

            BootstrapNodeInfo bootstrapNodeInfo = buildBootstrapNodeInfo();

            BootstrapNode bootstrapNode = new BootstrapNode(bootstrapNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            OperationsNodeListener mockListener = mock(OperationsNodeListener.class);
            bootstrapNode.addListener(mockListener);
            bootstrapNode.start();

            OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            endpointNode.start();

            timing.sleepABit();
            verify(mockListener).onNodeAdded(endpointNodeInfo);

            int random = new Random().nextInt();
            endpointNodeInfo.setProcessedRequestCount(random);
            endpointNode.updateNodeData(endpointNodeInfo);

            timing.sleepABit();
            verify(mockListener).onNodeUpdated(endpointNodeInfo);
            assertEquals(new Integer(random), bootstrapNode.getCurrentOperationServerNodes().get(0).getProcessedRequestCount());

            endpointNode.close();
            timing.sleepABit();
            verify(mockListener).onNodeRemoved(endpointNodeInfo);
            Assert.assertTrue(bootstrapNode.removeListener(mockListener));
            Assert.assertFalse(bootstrapNode.removeListener(mockListener));
            bootstrapNode.close();
        } finally {
            cluster.close();
        }
    }
    
    @Test
    public void endpointExceptionTest() throws Exception {
        Timing timing = new Timing();
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        try {
            OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

            OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            endpointNode.start();
            endpointNode.doZKClientAction(new ControlNodeTracker.ZKClientAction() {
                
                @Override
                public void doWithZkClient(CuratorFramework client) throws Exception {
                    throw new Exception("for test");
                }
            });
            
            Assert.assertFalse(endpointNode.isConnected());
        } finally {
            cluster.close();
        }
    }
    
    @Test(expected=IOException.class)
    public void endpointIOExceptionTest() throws Exception {
        Timing timing = new Timing();
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        try {
            OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

            OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            endpointNode.start();
            endpointNode.doZKClientAction(new ControlNodeTracker.ZKClientAction() {
                
                @Override
                public void doWithZkClient(CuratorFramework client) throws Exception {
                    throw new Exception("for test");
                }
            }, true);
            
            Assert.assertFalse(endpointNode.isConnected());
        } finally {
            cluster.close();
        }
    }     

    private RetryPolicy buildDefaultRetryPolicy() {
        return new ExponentialBackoffRetry(100, 1);
    }

    private BootstrapNodeInfo buildBootstrapNodeInfo() {
        BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[] { 10, 11, 12, 45, 34, 23, 67, 89, 66, 12 });
        nodeInfo.setConnectionInfo(new ConnectionInfo(BOOTSTRAP_NODE_HOST, 1000, BOOTSTRAP_NODE_HOST, 1001, testKeyData));
        nodeInfo.setProcessedRequestCount(1);
        return nodeInfo;
    }

    private OperationsNodeInfo buildOperationsNodeInfo() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[] { 10, 11, 12, 45, 34, 23, 67, 89, 66, 12 });
        nodeInfo.setConnectionInfo(new ConnectionInfo(ENDPOINT_NODE_HOST, 1000, ENDPOINT_NODE_HOST, 1001, testKeyData));
        nodeInfo.setDeltaCalculationCount(1);
        nodeInfo.setProcessedRequestCount(2);
        nodeInfo.setRegisteredUsersCount(3);
        return nodeInfo;
    }
}
