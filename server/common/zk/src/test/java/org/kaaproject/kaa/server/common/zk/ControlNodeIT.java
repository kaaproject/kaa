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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.Timing;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.control.ControlNodeListener;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.ControlNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;

public class ControlNodeIT {

    private static final String ENDPOINT_NODE_HOST = "192.168.0.101";
    private static final String SECONDARY_NODE_HOST = "192.168.0.2";
    private static final String CONTROL_NODE_HOST = "192.168.0.1";

    @Test
    public void masterFailoverTest() throws Exception {
        Timing timing = new Timing();
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        try {
            ControlNodeInfo controlNodeInfo = buildControlNodeInfo();

            ControlNodeInfo secondaryNodeInfo = buildSecondaryNodeInfo();

            OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

            OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            endpointNode.start();
            assertNull(endpointNode.getControlServerInfo());

            ControlNode controlNode = new ControlNode(controlNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            assertFalse(controlNode.isMaster());
            controlNode.start();
            ControlNode secondaryNode = new ControlNode(secondaryNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            assertFalse(secondaryNode.isMaster());
            secondaryNode.start();
            timing.sleepABit();
            assertTrue(controlNode.isMaster());
            assertFalse(secondaryNode.isMaster());
            assertNotNull(endpointNode.getControlServerInfo());
            assertEquals(CONTROL_NODE_HOST, endpointNode.getControlServerInfo().getConnectionInfo().getThriftHost().toString());

            controlNode.close();
            timing.sleepABit();

            assertNotNull(endpointNode.getControlServerInfo());
            assertEquals(SECONDARY_NODE_HOST, endpointNode.getControlServerInfo().getConnectionInfo().getThriftHost().toString());
            secondaryNode.close();
            endpointNode.close();
        } finally {
            cluster.close();
        }
    }

    @Test
    public void masterListenerTest() throws Exception {
        Timing timing = new Timing();
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        try {
            ControlNodeInfo controlNodeInfo = buildControlNodeInfo();
            ControlNodeInfo secondaryNodeInfo = buildSecondaryNodeInfo();
            OperationsNodeInfo endpointNodeInfo = buildOperationsNodeInfo();

            OperationsNode endpointNode = new OperationsNode(endpointNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            ControlNodeListener mockListener = mock(ControlNodeListener.class);
            endpointNode.addListener(mockListener);
            endpointNode.start();

            ControlNode controlNode = new ControlNode(controlNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            controlNode.start();
            ControlNode secondaryNode = new ControlNode(secondaryNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            secondaryNode.start();
            timing.sleepABit();
            verify(mockListener).onControlNodeChange(controlNodeInfo);

            int random = new Random().nextInt();
            controlNodeInfo.setBootstrapServerCount(random);
            controlNode.updateNodeData(controlNodeInfo);

            int random2 = new Random().nextInt();
            secondaryNodeInfo.setBootstrapServerCount(random2);
            secondaryNode.updateNodeData(secondaryNodeInfo);

            timing.sleepABit();
            verify(mockListener).onControlNodeChange(controlNodeInfo);
            assertEquals(new Integer(random), endpointNode.getControlServerInfo().getBootstrapServerCount());
            assertEquals(new Integer(random2), secondaryNode.getCurrentNodeInfo().getBootstrapServerCount());

            controlNode.close();
            timing.sleepABit();
            verify(mockListener).onControlNodeDown();
            verify(mockListener).onControlNodeChange(secondaryNodeInfo);

            assertTrue(endpointNode.removeListener(mockListener));
            assertFalse(endpointNode.removeListener(mockListener));

            secondaryNode.close();
            endpointNode.close();
        } finally {
            cluster.close();
        }
    }

    private RetryPolicy buildDefaultRetryPolicy() {
        return new ExponentialBackoffRetry(100, 1);
    }

    private OperationsNodeInfo buildOperationsNodeInfo() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[] { 10, 11, 12, 45, 34, 23, 67, 89, 66, 12 });
        nodeInfo.setConnectionInfo(new ConnectionInfo(ENDPOINT_NODE_HOST, 1000, testKeyData));
        nodeInfo.setLoadInfo(new LoadInfo(1, 1.0));
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        nodeInfo.setTransports(BootstrapNodeIT.getHttpAndTcpTransportMD() );
        return nodeInfo;
    }

    private ControlNodeInfo buildSecondaryNodeInfo() {
        ControlNodeInfo secondaryNodeInfo = new ControlNodeInfo();
        secondaryNodeInfo.setConnectionInfo(new ConnectionInfo(SECONDARY_NODE_HOST, 1000, null));
        secondaryNodeInfo.setBootstrapServerCount(1);
        secondaryNodeInfo.setOperationsServerCount(2);
        return secondaryNodeInfo;
    }

    private ControlNodeInfo buildControlNodeInfo() {
        ControlNodeInfo controlNodeInfo = new ControlNodeInfo();
        controlNodeInfo.setConnectionInfo(new ConnectionInfo(CONTROL_NODE_HOST, 1000, null));
        controlNodeInfo.setBootstrapServerCount(3);
        controlNodeInfo.setOperationsServerCount(4);
        return controlNodeInfo;
    }
}
