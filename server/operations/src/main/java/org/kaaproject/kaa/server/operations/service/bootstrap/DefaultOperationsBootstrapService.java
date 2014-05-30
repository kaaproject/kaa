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

package org.kaaproject.kaa.server.operations.service.bootstrap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.http.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.http.ProtocolService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;
import org.kaaproject.kaa.server.operations.service.statistics.StatisticsService;
import org.kaaproject.kaa.server.operations.service.thrift.OperationsThriftServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The Class for Operations bootstrap process.
 * Main methods are {@link #start() start} and {@link #stop() stop}
 * Launches Transport and ZK services
 *
 * @author ashvayka
 */
@Service
public class DefaultOperationsBootstrapService implements OperationsBootstrapService {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultOperationsBootstrapService.class);

    /** The thrift host. */
    @Value("#{properties[thrift_host]}")
    private String thriftHost;

    /** The thrift port. */
    @Value("#{properties[thrift_port]}")
    private int thriftPort;

    /** The thrift port. */
    @Value("#{properties[netty_host]}")
    private String nettyHost;
    
    /** The thrift port. */
    @Value("#{properties[netty_port]}")
    private int nettyPort;
    
    /** The zk enabled. */
    @Value("#{properties[zk_enabled]}")
    private boolean zkEnabled;

    /** The zk host port list. */
    @Value("#{properties[zk_host_port_list]}")
    private String zkHostPortList;

    /** The zk max retry time. */
    @Value("#{properties[zk_max_retry_time]}")
    private int zkMaxRetryTime;

    /** The zk sleep time. */
    @Value("#{properties[zk_sleep_time]}")
    private int zkSleepTime;

    /** The zk ignore errors. */
    @Value("#{properties[zk_ignore_errors]}")
    private boolean zkIgnoreErrors;
    
    /** Statistics collect window in seconds */
    @Value("#{properties[statistics_calculation_window]}")
    private long statisticsCalculationWindow;
    
    /** Number of statistics update during collect window */
    @Value("#{properties[statistics_update_times]}")
    private int statisticsUpdateTimes;
    
    /** Real bind IP address of Netty HTTP Server */
    private String realBindInterface;

    /** The server. */
    private TServer server;

    /** The operations node. */
    private OperationsNode operationsNode;

    /** The http service. */
    private ProtocolService httpService;

    /** The operations thrift service. */
    @Autowired
    private OperationsThriftServiceImpl operationsThriftService;

    /** The key store service. */
    @Autowired
    private KeyStoreService keyStoreService;

    /** The operations service. */
    @Autowired
    private OperationsService operationsService;
    
    /** The Akka service. */
    @Autowired
    private AkkaService akkaService;    
    
    /** The cache service. */
    @Autowired
    private CacheService cacheService;
    
    /** The operations server config. */
    @Autowired
    OperationsServerConfig operationsServerConfig;
    
    /** Statisics Service */
    private StatisticsService statistics;

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#getEndpointService()
     */
    @Override
    public OperationsService getOperationsService() {
        return operationsService;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#getKeyStoreService()
     */
    @Override
    public KeyStoreService getKeyStoreService() {
        return keyStoreService;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService#getCacheService()
     */
    @Override
    public CacheService getCacheService() {
        return cacheService;
    }


    /**
     * OperationsServerConfig getter
     * @return OperationsServerConfig
     */
    private OperationsServerConfig getConfig() {
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
        getConfig().setPort(nettyPort);
        getConfig().setBindInterface(nettyHost);
        getConfig().setStatisticsCalculationWindow(statisticsCalculationWindow);
        getConfig().setStatisticsUpdateTimes(statisticsUpdateTimes);
        httpService = new ProtocolService(getConfig());
        if (httpService.getNetty() != null) {
            
            statistics = StatisticsService.getService();
            
            getConfig().setSessionTrack(statistics);
            
            httpService.start();
            realBindInterface = httpService.getNetty().getBindAddress();
            LOG.info("Operations HTTP server started on" + realBindInterface + " interface, and port "+nettyPort); 
    
            if (zkEnabled) {
                startZK();
            }
            
            // Blocking method call
            startThrift();
        } else {
            LOG.error("Operations start failed, Netty didn't started...");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.
     * OperationsBootstrapService#stop()
     */
    @Override
    public void stop() {
        if (httpService != null) {
            httpService.stop();
            httpService = null;
        }
        if (statistics != null) {
            statistics.shutdown();
            statistics = null;
        }
        if( akkaService != null){
            akkaService.getActorSystem().shutdown();
        }
        if (zkEnabled) {
            stopZK();
        }
        // Thrift stop
        server.stop();
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
     * Start thrift service.
     */
    private void startThrift() {

        LOG.info("Initializing Thrift Service for Operations Server....");
        LOG.info("thrift host: " + thriftHost);
        LOG.info("thrift port: " + thriftPort);

        try {
            OperationsThriftService.Processor<OperationsThriftService.Iface> processor = new OperationsThriftService.Processor<OperationsThriftService.Iface>(
                    operationsThriftService);
            TServerTransport serverTransport = new TServerSocket(new InetSocketAddress(thriftHost, thriftPort));
            server = new TThreadPoolServer(new Args(serverTransport).processor(processor));

            LOG.info("Operations Server Started.");

            server.serve();

            LOG.info("Operations Server Stopped.");

        } catch (TTransportException e) {
            LOG.error("TTransportException", e);
        } finally {

        }
    }

    /**
     * Start zk node.
     */
    private void startZK() {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        ByteBuffer keyData = ByteBuffer.wrap(keyStoreService.getPublicKey().getEncoded());
        ConnectionInfo connectionInfo = new ConnectionInfo(thriftHost, thriftPort, realBindInterface, nettyPort, keyData);
        nodeInfo.setConnectionInfo(connectionInfo);
        operationsNode = new OperationsNode(nodeInfo, zkHostPortList, new RetryUntilElapsed(zkMaxRetryTime, zkSleepTime));
        try {
            operationsNode.start();
            getConfig().setZkNode(operationsNode);
        } catch (Exception e) {
            if (zkIgnoreErrors) {
                LOG.info("Failed to register operations in ZooKeeper", e);
            } else {
                LOG.error("Failed to register operations in ZooKeeper", e);
                throw new RuntimeException(e);
            }
        }
    }

}
