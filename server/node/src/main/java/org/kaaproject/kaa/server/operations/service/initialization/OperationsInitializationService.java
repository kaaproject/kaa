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

package org.kaaproject.kaa.server.operations.service.initialization;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.retry.RetryUntilElapsed;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.hash.ConsistentHashResolver;
import org.kaaproject.kaa.server.node.service.initialization.AbstractInitializationService;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cluster.ClusterService;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.loadbalance.LoadBalancingService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;
import org.kaaproject.kaa.server.operations.service.transport.OperationsTransportService;
import org.kaaproject.kaa.server.transport.TransportUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class for Operations bootstrap process. Main methods are {@link #start()
 * start} and {@link #stop() stop} Launches Transport and ZK services
 *
 * @author ashvayka
 */
@Service
public class OperationsInitializationService extends AbstractInitializationService {

    private static final int DEFAULT_LOAD_INDEX = 1;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(OperationsInitializationService.class);

    /** The operations node. */
    private OperationsNode operationsNode;

    /** The key store service. */
    @Autowired
    private KeyStoreService operationsKeyStoreService;

    /** The operations service. */
    @Autowired
    private OperationsService operationsService;

    /** The Akka service. */
    @Autowired
    private AkkaService akkaService;

    @Autowired
    private OperationsTransportService operationsTransportService;

    /** The cache service. */
    @Autowired
    private CacheService cacheService;
    
    /** The operations server config. */
    @Autowired
    private OperationsServerConfig operationsServerConfig;

    /** The event service */
    @Autowired
    private EventService eventService;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private LoadBalancingService loadBalancingService;

    /**
     * OperationsServerConfig getter
     *
     * @return OperationsServerConfig
     */
    private OperationsServerConfig getOperationsConfig() {
        return operationsServerConfig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.
     * OperationsBootstrapService#start()
     */
    @Override
    public void start() {
        operationsService.setPublicKey(operationsKeyStoreService.getPublicKey());

        operationsTransportService.lookupAndInit();

        if (getNodeConfig().isZkEnabled()) {
            startZK();
            operationsTransportService.addListener(new TransportUpdateListener() {

                @Override
                public void onTransportsStarted(List<TransportMetaData> mdList) {
                    if (operationsNode != null) {
                        OperationsNodeInfo info = operationsNode.getNodeInfo();
                        info.setTransports(mdList);
                        try {
                            operationsNode.updateNodeData(info);
                        } catch (IOException e) {
                            LOG.error("Failed to update bootstrap node info", e);
                        }
                    }
                }

            });
            loadBalancingService.start(operationsNode);
        }

        operationsTransportService.start();
        
        LOG.info("Operations Service Started.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.
     * OperationsBootstrapService#stop()
     */
    @Override
    public void stop() {
        if (eventService != null) {
            eventService.shutdown();
        }
        if (clusterService != null) {
            clusterService.shutdown();
        }
        if(loadBalancingService != null && getNodeConfig().isZkEnabled()) {
            loadBalancingService.stop();
        }
        if (operationsTransportService != null) {
            operationsTransportService.stop();
        }
        if (akkaService != null) {
            akkaService.getActorSystem().shutdown();
        }
        
        if (getNodeConfig().isZkEnabled()) {
            stopZK();
        }
        
        LOG.info("Operations Service Stopped.");
    }

    /**
     * Stop zk node.
     */
    private void stopZK() {
        try {
            operationsNode.close();
        } catch (IOException e) {
            LOG.warn("Error closing ZK node", e);
        }
    }

    /**
     * Start zk node.
     */
    private void startZK() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer keyData = ByteBuffer.wrap(operationsKeyStoreService.getPublicKey().getEncoded());
        nodeInfo.setConnectionInfo(new ConnectionInfo(getNodeConfig().getThriftHost(), getNodeConfig().getThriftPort(), keyData));
        nodeInfo.setLoadInfo(new LoadInfo(DEFAULT_LOAD_INDEX, 1.0));
        nodeInfo.setTransports(new ArrayList<TransportMetaData>());
        operationsNode = new OperationsNode(nodeInfo, getNodeConfig().getZkHostPortList(), new RetryUntilElapsed(getNodeConfig()
                .getZkMaxRetryTime(), getNodeConfig().getZkSleepTime()));
        try {
            operationsNode.start();
            eventService.setZkNode(operationsNode);
            eventService.setResolver(new ConsistentHashResolver(operationsNode.getCurrentOperationServerNodes(), getOperationsConfig()
                    .getUserHashPartitions()));
            clusterService.setZkNode(operationsNode);
            clusterService.setResolver(new ConsistentHashResolver(operationsNode.getCurrentOperationServerNodes(), getOperationsConfig()
                    .getUserHashPartitions()));
        } catch (Exception e) {
            if (getNodeConfig().isZkIgnoreErrors()) {
                LOG.info("Failed to register operations in ZooKeeper", e);
            } else {
                LOG.error("Failed to register operations in ZooKeeper", e);
                throw new RuntimeException(e); // NOSONAR
            }
        }
    }
}
