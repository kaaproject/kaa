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
import static org.junit.Assert.assertFalse;
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
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.ControlNodeInfo;

public class BootstrapNodeIT {

    private static final String BOOTSTRAP_NODE_HOST = "192.168.0.202";
    private static final String CONTROL_NODE_HOST = "192.168.0.1";

    @Test
    public void boostrapListenerTest() throws Exception {
        Timing timing = new Timing();
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        try {
            ControlNodeInfo controlNodeInfo = buildControlNodeInfo();
            BootstrapNodeInfo bootstrapNodeInfo = buildBootstrapNodeInfo();

            ControlNode controlNode = new ControlNode(controlNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            BootstrapNodeListener mockListener = mock(BootstrapNodeListener.class);
            controlNode.addListener(mockListener);
            controlNode.start();

            BootstrapNode bootstrapNode = new BootstrapNode(bootstrapNodeInfo, cluster.getConnectString(), buildDefaultRetryPolicy());
            bootstrapNode.start();
            timing.sleepABit();
            
            verify(mockListener).onNodeAdded(bootstrapNodeInfo);

            int random = new Random().nextInt();
            bootstrapNodeInfo.setProcessedRequestCount(random);
            bootstrapNode.updateNodeData(bootstrapNodeInfo);
            timing.sleepABit();
            
            verify(mockListener).onNodeUpdated(bootstrapNodeInfo);
            assertEquals(new Integer(random), controlNode.getCurrentBootstrapNodes().get(0).getProcessedRequestCount());
            
            bootstrapNode.close();
            timing.sleepABit();
            
            verify(mockListener).onNodeRemoved(bootstrapNodeInfo);
            bootstrapNode.close();
            
            assertTrue(controlNode.removeListener(mockListener));
            assertFalse(controlNode.removeListener(mockListener));
            controlNode.close();
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

    private ControlNodeInfo buildControlNodeInfo() {
        ControlNodeInfo controlNodeInfo = new ControlNodeInfo();
        controlNodeInfo.setConnectionInfo(new ConnectionInfo(CONTROL_NODE_HOST, 1000, CONTROL_NODE_HOST, 1001, null));
        controlNodeInfo.setBootstrapServerCount(3);
        controlNodeInfo.setOperationsServerCount(4);
        return controlNodeInfo;
    }
}
