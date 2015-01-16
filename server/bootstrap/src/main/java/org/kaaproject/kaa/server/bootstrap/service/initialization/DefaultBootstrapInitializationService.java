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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.config.BootstrapServerConfig;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.transport.TransportService;
import org.kaaproject.kaa.server.transport.TransportUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * DefaultBootstrapInitializationService Class. Starts and stops all services in
 * the Bootstrap service: 1. CLI Thrift service; 2. ZooKeeper service; 3. Netty
 * HTTP service.
 *
 * @author Andrey Panasenko
 */
@Service
public class DefaultBootstrapInitializationService implements BootstrapInitializationService {

    /** Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapInitializationService.class);

    @Value("#{properties[thrift_host]}")
    private String thriftHost;

    @Value("#{properties[thrift_port]}")
    private int thriftPort;

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

    @Autowired
    private TransportService transportService;

    @Autowired
    private BootstrapServerConfig serverConfig;

    /** The bootstrap node. */
    private BootstrapNode bootstrapNode;

    /** The server. */
    private TServer server;

    @Autowired
    private OperationsServerListService operationsServerListService;

    /** The key store service. */
    @Autowired
    private KeyStoreService keyStoreService;

    @Override
    public KeyStoreService getKeyStoreService() {
        return keyStoreService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.
     * BootstrapInitializationService#start()
     */
    @Override
    public void start() {
        LOG.trace("Starting Bootstrap service..." + propertiesToString());
        try {
            LOG.trace("Bootstrap Service ZK started");
            if (operationsServerListService == null) {
                throw new Exception("Error initializing OperationsServerListService()"); // NOSONAR
            }

            transportService.lookupAndInit();

            final CountDownLatch thriftStartupLatch = new CountDownLatch(1);
            final CountDownLatch thriftShutdownLatch = new CountDownLatch(1);

            startThrift(thriftStartupLatch, thriftShutdownLatch);

            try {
                thriftStartupLatch.await();
            } catch (InterruptedException e) {
                LOG.error("Interrupted while waiting for thrift to start...", e);
            }

            if (zkEnabled) {
                startZK();
            }

            operationsServerListService.init(bootstrapNode);

            transportService.addListener(new TransportUpdateListener() {

                @Override
                public void onTransportsStarted(List<TransportMetaData> mdList) {
                    BootstrapNodeInfo info = bootstrapNode.getNodeInfo();
                    info.setTransports(mdList);
                    try {
                        bootstrapNode.updateNodeData(info);
                    } catch (IOException e) {
                        LOG.error("Failed to update bootstrap node info", e);
                    }
                }

            });

            transportService.start();

            try {
                thriftShutdownLatch.await();
            } catch (InterruptedException e) {
                LOG.error("Interrupted while waiting for thrift to stop...", e);
            }
        } catch (Exception e) {
            LOG.error("Error starting Bootstrap Service", e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.
     * BootstrapInitializationService#stop()
     */
    @Override
    public void stop() {
        LOG.trace("Stopping Bootstrap Service..." + propertiesToString());

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
     *
     * @param thriftStartupLatch
     *            CountDownLatch
     * @param thriftShutdownLatch
     *            CountDownLatch
     */
    private void startThrift(final CountDownLatch thriftStartupLatch, final CountDownLatch thriftShutdownLatch) {
        Runnable thriftRunnable = new Runnable() {

            @Override
            public void run() {

                LOG.info("Initializing Thrift service for Bootstrap server at {}:{}...", thriftHost, thriftPort);

                try {
                    BootstrapThriftService.Processor<BootstrapThriftService.Iface> processor = new BootstrapThriftService.Processor<BootstrapThriftService.Iface>(
                            bootstrapThriftService);

                    TServerTransport serverTransport = new TServerSocket(new InetSocketAddress(thriftHost, thriftPort));

                    TThreadPoolServer.Args args = new Args(serverTransport).processor(processor);
                    args.stopTimeoutVal = 3;
                    args.stopTimeoutUnit = TimeUnit.SECONDS;
                    server = new TThreadPoolServer(args);

                    LOG.info("Bootstrap Server Started");

                    thriftStartupLatch.countDown();

                    server.serve();

                    LOG.info("Bootstrap Server Stopped");

                    thriftShutdownLatch.countDown();
                } catch (TTransportException e) {
                    LOG.error("TTransportException", e);
                } finally {
                    server = null;

                    if (thriftStartupLatch.getCount() > 0) {
                        thriftStartupLatch.countDown();
                    }
                    if (thriftShutdownLatch.getCount() > 0) {
                        LOG.info("Thrift Bootstrap Server Stopped.");
                        thriftShutdownLatch.countDown();
                    }
                }
            }
        };

        new Thread(thriftRunnable).start();
    }

    /**
     * Start zk.
     *
     * @throws Exception
     *             in case of error
     */
    private void startZK() throws Exception { // NOSONAR
        if (zkEnabled) {
            LOG.info("Bootstrap Server starting ZooKepper connection to {}", zkHostPortList);
            BootstrapNodeInfo nodeInfo = new BootstrapNodeInfo();
            ByteBuffer keyData = ByteBuffer.wrap(keyStoreService.getPublicKey().getEncoded());
            LOG.trace("Bootstrap server: registering in ZK: thriftHost {}; thriftPort {}; nettyHost {}; nettyPort {}", thriftHost,
                    thriftPort);
            nodeInfo.setConnectionInfo(new ConnectionInfo(thriftHost, thriftPort, keyData));
            nodeInfo.setTransports(new ArrayList<TransportMetaData>());
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
            if (transportService != null) {
                transportService.stop();
            }
        } catch (IOException e) {
            LOG.warn("Exception when closing ZK node", e);
        } finally {
            bootstrapNode = null;
        }
    }

    /**
     * Return properties for printing in logs.
     *
     * @return String representation of properties
     */
    private String propertiesToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nProperties: \n");
        sb.append("thriftHost: " + thriftHost + "\n");
        sb.append("thriftPort: " + thriftPort + "\n");
        sb.append("zkEnabled: " + zkEnabled + "\n");
        sb.append("zkHostPortList: " + zkHostPortList + "\n");
        sb.append("zkMaxRetryTime: " + zkMaxRetryTime + "\n");
        sb.append("zkSleepTime: " + zkSleepTime + "\n");
        sb.append("zkIgnoreErrors: " + zkIgnoreErrors + "\n");
        return sb.toString();
    }
}
