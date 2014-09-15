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

package org.kaaproject.kaa.server.control;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapSupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TestCluster.
 */
public class TestCluster {
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(TestCluster.class);

    /** The Constant BOOTSTRAP_NODE_HOST. */
    private static final String BOOTSTRAP_NODE_HOST = "192.168.0.202";
    
    /** The Constant ENDPOINT_NODE_HOST. */
    private static final String ENDPOINT_NODE_HOST = "192.168.0.101";
    
    /** The control service static instance. */
    private static ControlService controlServiceInstance;
    
    /** The zookeeper cluster static instance. */
    private static TestingCluster zkCluster;
    
    /** The endpoint node static instance. */
    private static OperationsNode endpointNode;
    
    /** The bootstrap node static instance. */
    private static BootstrapNode bootstrapNode;
    
    /** The control server thread. */
    private static Thread controlServerThread;
    
    /** The control server started. */
    private static boolean controlServerStarted = false;
    
    /**
     * Start.
     *
     * @param controlService the control service
     * @throws Exception the exception
     */
    public static void checkStarted(ControlService controlService) throws Exception {
        if (!controlServerStarted) {
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
            
            controlServiceInstance = controlService;
            
            controlServerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    logger.info("Control Server Started.");
                    controlServiceInstance.start();
                    logger.info("Control Server Stoped.");
                }
            });

            controlServerThread.start();

            Thread.sleep(3000);

            controlServerStarted = true;
        }
        
        
    }
    
    /**
     * Stop.
     *
     * @throws Exception the exception
     */
    public static void stop() throws Exception {
        if (controlServerStarted) {
            Thread.sleep(1000);
            controlServiceInstance.stop();
            controlServerThread.join();
            endpointNode.close();
            bootstrapNode.close();
            zkCluster.close();
            controlServerStarted = false;
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
     * @throws NoSuchAlgorithmException 
     */
    private static BootstrapNodeInfo buildBootstrapNodeInfo() throws NoSuchAlgorithmException {
        BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
        KeyPair keys = KeyUtil.generateKeyPair();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                keys.getPublic().getEncoded());
        ByteBuffer testKeyData = ByteBuffer.wrap(x509EncodedKeySpec.getEncoded());
        nodeInfo.setConnectionInfo(new ConnectionInfo(BOOTSTRAP_NODE_HOST, 1000, testKeyData));
        
        
        List<BootstrapSupportedChannel> chList = new LinkedList<>();
        ZkHttpComunicationParameters CommunicationParameters = new ZkHttpComunicationParameters(new IpComunicationParameters(BOOTSTRAP_NODE_HOST, 1001));
        ZkHttpStatistics ChannelStatistics = new ZkHttpStatistics(new BaseStatistics(
                Integer.valueOf(1), 
                Integer.valueOf(1), 
                Integer.valueOf(1), 
                Long.valueOf(1)));
        chList.add(new BootstrapSupportedChannel(new ZkSupportedChannel(
                ZkChannelType.HTTP, 
                true, 
                CommunicationParameters, 
                ChannelStatistics)));
        nodeInfo.setSupportedChannelsArray(chList);
        
        return nodeInfo;
    }

    /**
     * Builds the endpoint node info.
     *
     * @return the endpoint node info
     */
    private static OperationsNodeInfo buildEndpointNodeInfo() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer testKeyData = ByteBuffer.wrap(new byte[]{10,11,12,45,34,23,67,89,66,12});
        nodeInfo.setConnectionInfo(new ConnectionInfo(ENDPOINT_NODE_HOST, 1000,testKeyData));
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        List<SupportedChannel> supportedChannels = new ArrayList<>();
        ZkHttpComunicationParameters httpCommunicationParameters = new ZkHttpComunicationParameters(new IpComunicationParameters(ENDPOINT_NODE_HOST, 1000));
        BaseStatistics statistics = new BaseStatistics(2, 3, 1, System.currentTimeMillis());
        ZkHttpStatistics httpChannelStatistics = new ZkHttpStatistics(statistics );
        SupportedChannel channelHttp = new SupportedChannel(new ZkSupportedChannel(ZkChannelType.HTTP, true, httpCommunicationParameters, httpChannelStatistics));
        supportedChannels.add(channelHttp);
        nodeInfo.setSupportedChannelsArray(supportedChannels );
        return nodeInfo;
    }
    
}
