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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;

public class BootstrapNodeIT {

    private static final String UTF_8 = "UTF-8";
    static final int TCP_ID = 73;
    static final int HTTP_ID = 42;
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

            List<TransportMetaData> transports = bootstrapNodeInfo.getTransports();
            transports.remove(getHttpTransportMD());
            bootstrapNode.updateNodeData(bootstrapNodeInfo);
            timing.sleepABit();

            verify(mockListener).onNodeUpdated(bootstrapNodeInfo);

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

    @Test
    public void outdatedRemovalTest() throws Exception {
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

            BootstrapNodeInfo bootstrapNodeInfoWithGreaterTimeStarted = buildBootstrapNodeInfo();
            BootstrapNode bootstrapNodeWithGreaterTimeStarted = new BootstrapNode(bootstrapNodeInfoWithGreaterTimeStarted, cluster.getConnectString(), buildDefaultRetryPolicy());

            bootstrapNodeWithGreaterTimeStarted.start();
            timing.sleepABit();
            
            bootstrapNode.close();
            timing.sleepABit();
            verify(mockListener, never()).onNodeRemoved(bootstrapNodeInfo);
            
            bootstrapNodeWithGreaterTimeStarted.close();
            timing.sleepABit();
            verify(mockListener).onNodeRemoved(bootstrapNodeInfoWithGreaterTimeStarted);

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
        nodeInfo.setConnectionInfo(new ConnectionInfo(BOOTSTRAP_NODE_HOST, 1000, testKeyData));
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        nodeInfo.setTransports(getHttpAndTcpTransportMD());

        return nodeInfo;
    }

    static List<TransportMetaData> getHttpAndTcpTransportMD() {
        List<TransportMetaData> supportedTransports = new ArrayList<>();

        try {
            supportedTransports.add(getHttpTransportMD());
            supportedTransports.add(getTcpTransportMD());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return supportedTransports;
    }

    static TransportMetaData getTcpTransportMD() throws UnsupportedEncodingException {
        TransportMetaData tcp = new TransportMetaData();
        tcp.setId(TCP_ID);
        tcp.setMaxSupportedVersion(2);
        tcp.setMinSupportedVersion(2);
        tcp.setConnectionInfo(Collections.singletonList(new VersionConnectionInfoPair(2, ByteBuffer.wrap("tcp".getBytes(UTF_8)))));
        return tcp;
    }

    static TransportMetaData getHttpTransportMD() throws UnsupportedEncodingException {
        TransportMetaData http = new TransportMetaData();
        http.setId(HTTP_ID);
        http.setMaxSupportedVersion(1);
        http.setMinSupportedVersion(1);
        http.setConnectionInfo(Collections.singletonList(new VersionConnectionInfoPair(1, ByteBuffer.wrap("http".getBytes(UTF_8)))));
        return http;
    }

    private ControlNodeInfo buildControlNodeInfo() {
        ControlNodeInfo controlNodeInfo = new ControlNodeInfo();
        controlNodeInfo.setConnectionInfo(new ConnectionInfo(CONTROL_NODE_HOST, 1000, null));
        controlNodeInfo.setBootstrapServerCount(3);
        controlNodeInfo.setOperationsServerCount(4);
        return controlNodeInfo;
    }
}
