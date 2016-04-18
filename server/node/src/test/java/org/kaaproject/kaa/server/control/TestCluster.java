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

package org.kaaproject.kaa.server.control;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.node.service.initialization.KaaNodeInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TestCluster.
 */
public class TestCluster {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(TestCluster.class);

    /** The Constant BOOTSTRAP_NODE_HOST. */
    private static final String BOOTSTRAP_NODE_HOST = "127.0.0.1";
    
    /** The Constant OPERATIONS_NODE_HOST. */
    private static final String OPERATIONS_NODE_HOST = "127.0.0.1";
    
    /** The kaa node instance. */
    private static KaaNodeInitializationService kaaNodeInstance;
    
    /** The zk cluster. */
    private static TestingCluster zkCluster;
    
    /** The endpoint node. */
    private static OperationsNode endpointNode;
    
    /** The bootstrap node. */
    private static BootstrapNode bootstrapNode;
    
    /** The kaa node server thread. */
    private static Thread kaaNodeServerThread;
    
    /** The kaa node server started. */
    private static boolean kaaNodeServerStarted = false;
    
    /**
     * Check started.
     *
     * @param kaaNodeInitializationService the kaa node initialization service
     * @throws Exception the exception
     */
    public static void checkStarted(KaaNodeInitializationService kaaNodeInitializationService) throws Exception {
        if (!kaaNodeServerStarted) {
            zkCluster = new TestingCluster(new InstanceSpec(null, 2185, -1, -1, true, -1, -1, -1));
            zkCluster.start();

            BootstrapNodeInfo bootstrapNodeInfo = buildBootstrapNodeInfo();

            bootstrapNode = new BootstrapNode(bootstrapNodeInfo, 
                    zkCluster.getConnectString(), buildDefaultRetryPolicy());
            bootstrapNode.start();

            OperationsNodeInfo endpointNodeInfo = buildEndpointNodeInfo();

            endpointNode = new OperationsNode(endpointNodeInfo,
                    zkCluster.getConnectString(), buildDefaultRetryPolicy());
            endpointNode.start();
            
            kaaNodeInstance = kaaNodeInitializationService;
            
            kaaNodeServerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    LOG.info("Kaa Node Started.");
                    kaaNodeInstance.start();
                    LOG.info("Kaa Node Stoped.");
                }
            });

            kaaNodeServerThread.start();

            Thread.sleep(3000);

            kaaNodeServerStarted = true;
        }
        
        
    }
    
    /**
     * Stop.
     *
     * @throws Exception the exception
     */
    public static void stop() throws Exception {
        if (kaaNodeServerStarted) {
            Thread.sleep(1000);
            kaaNodeInstance.stop();
            kaaNodeServerThread.join();
            endpointNode.close();
            bootstrapNode.close();
            zkCluster.close();
            kaaNodeServerStarted = false;
        }
    }
    
    /**
     * Builds the default retry policy.
     *
     * @return the retry policy
     */
    private static RetryPolicy buildDefaultRetryPolicy() {
        return new ExponentialBackoffRetry(100, 1);
    }

    /**
     * Builds the bootstrap node info.
     *
     * @return the bootstrap node info
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    private static BootstrapNodeInfo buildBootstrapNodeInfo() throws NoSuchAlgorithmException {
        BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
        KeyPair keys = KeyUtil.generateKeyPair();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                keys.getPublic().getEncoded());
        ByteBuffer testKeyData = ByteBuffer.wrap(x509EncodedKeySpec.getEncoded());
        nodeInfo.setConnectionInfo(new ConnectionInfo(BOOTSTRAP_NODE_HOST, 10090, testKeyData));
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        nodeInfo.setTransports(new ArrayList<TransportMetaData>());
        
        return nodeInfo;
    }

    /**
     * Builds the endpoint node info.
     *
     * @return the operations node info
     */
    private static OperationsNodeInfo buildEndpointNodeInfo() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10,11,12,45,34,23,67,89,66,12});
        nodeInfo.setConnectionInfo(new ConnectionInfo(OPERATIONS_NODE_HOST, 10090,testKeyData));
        nodeInfo.setLoadInfo(new LoadInfo(1, 1.0));
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        nodeInfo.setTransports(new ArrayList<TransportMetaData>());
        return nodeInfo;
    }
    
}
