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

package org.kaaproject.kaa.server.bootstrap.service.initialization;

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
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.bootstrap.service.http.ProtocolService;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * DefaultBootstrapInitializationService Class.
 * Starts and stops all services in the Bootstrap service:
 * 1. CLI Thrift service;
 * 2. ZooKeeper service;
 * 3. Netty HTTP service.
 *
 * @author Andrey Panasenko
 */
@Service
public class DefaultBootstrapInitializationService implements
        BootstrapInitializationService {

    /** Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapInitializationService.class);

    @Value("#{properties[thrift_host]}")
    private String thriftHost;

    @Value("#{properties[thrift_port]}")
    private int thriftPort;

    @Value("#{properties[netty_host]}")
    private String nettyHost;

    @Value("#{properties[netty_port]}")
    private int nettyPort;

    @Value("#{properties[zk_enabled]}")
    private boolean zkEnabled;

    @Value("#{properties[zk_host_port_list]}")
    private String zkHostPortList;

    @Value("#{properties[zk_max_retry_time]}")
    private int zkMaxRetryTime;

    @Value("#{properties[zk_sleep_time]}")
    private int zkSleepTime;

    @Value("#{properties[zk_ignore_errors]}")
    private boolean zkIgnoreErrors;

    /** The bootstrap thrift service. */
    @Autowired
    private BootstrapThriftServiceImpl bootstrapThriftService;

    /** Bootstrap Config. */
    @Autowired
    private BootstrapConfig bootstrapConfig;

    /** The bootstrap node. */
    private BootstrapNode bootstrapNode;

    /** The server. */
    private TServer server;

    /** The http service. */
    private ProtocolService httpService;

    private OperationsServerListService operationsServerListService;

    /** The key store service. */
    @Autowired
    private KeyStoreService keyStoreService;

    /**
     * Return Bootstrap Config.
     *
     * @return BootstrapConfig
     */
    private BootstrapConfig getConfig() {
        return bootstrapConfig;
    }

    @Override
    public KeyStoreService getKeyStoreService() {
        return keyStoreService;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService#start()
     */
    @Override
    public void start() {
        LOG.trace("Starting Bootstrap service..."+propertiesToString());
        try {
            if (zkEnabled) {
                startZK();
            }
            LOG.trace("Bootstrap Service ZK started");
            if (operationsServerListService == null) {
                operationsServerListService = new OperationsServerListService(getConfig());
                if (operationsServerListService == null) {
                    throw new Exception("Error initializing OperationsServerListService()");
                }
                getConfig().setOperationsServerListService(operationsServerListService);
                if (zkEnabled) {
                    getConfig().setBootstrapNode(bootstrapNode);
                }
                operationsServerListService.init();
            }

            LOG.trace("Config: "+getConfig().toString());

            httpService = new ProtocolService(getConfig());
            httpService.start();

            bootstrapThriftService.setConfig(getConfig());

            startThrift();
        } catch (Exception e) {
            LOG.error("Error starting Bootstrap Service", e);
        }

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService#stop()
     */
    @Override
    public void stop() {
        LOG.trace("Stopping Bootstrap Service..."+propertiesToString());

        if (httpService != null) {
            httpService.stop();
            httpService = null;
        }

        if (operationsServerListService != null) {
            operationsServerListService.deinit();
            operationsServerListService = null;
        }

        if (zkEnabled) {
            stopZK();
        }

        // Thrift stop
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Start thrift.
     */
    private void startThrift() {
        LOG.info("Initializing Thrift service for Bootstrap server at {}:{}...", thriftHost, thriftPort);

        try {
            BootstrapThriftService.Processor<BootstrapThriftService.Iface> processor = new BootstrapThriftService.Processor<BootstrapThriftService.Iface>(bootstrapThriftService);

            TServerTransport serverTransport = new TServerSocket(new InetSocketAddress(thriftHost, thriftPort));
            server = new TThreadPoolServer(new Args(serverTransport).processor(processor));

            LOG.info("Bootstrap Server Started");

            server.serve();

            LOG.info("Bootstrap Server Stopped");

        } catch (TTransportException e) {
            LOG.error("TTransportException", e);
        } finally {
            server = null;
        }
    }


    /**
     * Start zk.
     * @throws Exception
     */
    private void startZK() throws Exception {
        if (zkEnabled) {
            LOG.info("Bootstrap Server starting ZooKepper connection to {}", zkHostPortList);
            BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
            ByteBuffer keyData = ByteBuffer.wrap(keyStoreService.getPublicKey().getEncoded());
            LOG.trace("Bootstrap server: registering in ZK: thriftHost {}; thriftPort {}; nettyHost {}; nettyPort {}"
                    , thriftHost, thriftPort, nettyHost, nettyPort);
            ConnectionInfo connectionInfo = new ConnectionInfo(thriftHost, thriftPort, nettyHost, nettyPort, keyData);
            nodeInfo.setConnectionInfo(connectionInfo);
            bootstrapNode = new BootstrapNode(nodeInfo, zkHostPortList, new RetryUntilElapsed(zkMaxRetryTime, zkSleepTime));
            if (bootstrapNode != null) {
                bootstrapNode.start();
            }
        }
    }

    /**
     * Stop zk.
     */
    private void stopZK() {
        try {
            if (bootstrapNode != null) {
                bootstrapNode.close();
            }
        } catch (IOException e) {
            LOG.warn("Exception when closing ZK node", e);
        } finally {
            bootstrapNode = null;
        }
    }

    /**
     * @return the bootstrapThriftService
     */
    public BootstrapThriftServiceImpl getBootstrapThriftService() {
        return bootstrapThriftService;
    }

    /**
     * @return the bootstrapNode
     */
    public BootstrapNode getBootstrapNode() {
        return bootstrapNode;
    }

    /**
     * @return the httpService
     */
    public ProtocolService getHttpService() {
        return httpService;
    }

    /**
     * @return the operationsServerListService
     */
    public OperationsServerListService getOperationsServerListService() {
        return operationsServerListService;
    }

    /**
     * Return properties for printing in logs.
     * @return String representation of properties
     */
    private String propertiesToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nProperties: \n");
        sb.append("thriftHost: "+thriftHost+"\n");
        sb.append("thriftPort: "+thriftPort+"\n");
        sb.append("zkEnabled: "+zkEnabled+"\n");
        sb.append("zkHostPortList: "+zkHostPortList+"\n");
        sb.append("zkMaxRetryTime: "+zkMaxRetryTime+"\n");
        sb.append("zkSleepTime: "+zkSleepTime+"\n");
        sb.append("zkIgnoreErrors: "+zkIgnoreErrors+"\n");
        return sb.toString();
    }
}
