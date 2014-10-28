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

package org.kaaproject.kaa.server.bootstrap;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapSupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/bootstrapTestContextBootstrapIT.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class BootstrapUpdateIT {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(BootstrapUpdateIT.class);

    /** The thrift host. */
    @Value("#{properties[thrift_host]}")
    private String thriftHost;

    /** The thrift port. */
    @Value("#{properties[thrift_port]}")
    private int thriftPort;

    /** The thrift host. */
    @Value("#{properties[netty_host]}")
    private String nettyHost;

    /** The thrift port. */
    @Value("#{properties[netty_port]}")
    private int nettyPort;

    
    /** The thrift host. */
    @Value("#{properties[channel_kaa_tcp_host]}")
    private String kaaTcpHost;

    /** The thrift port. */
    @Value("#{properties[channel_kaa_tcp_port]}")
    private int kaaTcpPort;
    
    /** ZooKeeper port constant */
    public static final int ZK_PORT = 21810;

    /** Bootstarp starter */
    private BootstrapStarted bootstrap;

    /** BootstrapNode change listener */
    private static TestBootstrapNodeListener listener;

    private static ExecutorService executor = null;

    private static boolean testFailed = false;
    

    /** Class for Bootstrap service starting */
    public class BootstrapStarted extends Thread {

        /** Bootstrap initialization service */
        private BootstrapInitializationService bootstrapInitializationService;

        /** Synchronizing object */
        private final Object startSync = new Object();

        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            logger.info("Application starting.. " + Charset.defaultCharset().name());


            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bootstrapTestContextBootstrapIT.xml");
            bootstrapInitializationService = (BootstrapInitializationService) ctx
                    .getBean("bootstrapInitializationService");

            synchronized (startSync) {
                startSync.notify();
            }
            bootstrapInitializationService.start();
            ctx.close();
            logger.info("Bootstrap Server Application stopped.");
        }

        /**
         * BootstrapStarted shutdown()
         */
        public void shutdown() {
            bootstrapInitializationService.stop();
        }

        /**
         * BootstrapInitializationService getter, blocking operation till service started
         * but blocked not more than 2sec.
         * @return BootstrapInitializationService
         */
        public BootstrapInitializationService getBootstrapInitializationService() {
            synchronized (startSync) {
                if (bootstrapInitializationService == null) {
                    try {
                        startSync.wait(2000);
                    } catch (InterruptedException e) {
                        logger.error(e.toString(), e);
                    }
                }
                return bootstrapInitializationService;
            }
        }

    }

    /**
     * BootstrapNode ZK listener
     *
     */
    public class TestBootstrapNodeListener implements BootstrapNodeListener {

        /** synchronizing object */
        private final Object sync = new Object();

        /** Node list */
        List<BootstrapNodeInfo> nodeInfos = new LinkedList<>();
        /* (non-Javadoc)
         * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
         */
        @Override
        public void onNodeAdded(BootstrapNodeInfo nodeInfo) {
            logger.info("Bootstrap node added "+nodeInfo.getConnectionInfo().getThriftHost());
            synchronized (sync) {
                nodeInfos.add(nodeInfo);
                sync.notify();
            }
        }

        /* (non-Javadoc)
         * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
         */
        @Override
        public void onNodeUpdated(BootstrapNodeInfo nodeInfo) {
            logger.info("Bootstrap node updated "+nodeInfo.getConnectionInfo().getThriftHost());
            synchronized (sync) {
                nodeInfos.clear();
                nodeInfos.add(nodeInfo);
                sync.notify();
            }
        }

        /* (non-Javadoc)
         * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
         */
        @Override
        public void onNodeRemoved(BootstrapNodeInfo nodeInfo) {
            logger.info("Bootstrap node removed "+nodeInfo.getConnectionInfo().getThriftHost());
            synchronized (sync) {
                nodeInfos.remove(nodeInfo);
                sync.notify();
            }
        }

        /**
         * BootstrapNodeInfo list getter, blocks until first element added.
         * @return List<BootstrapNodeInfo>
         */
        public List<BootstrapNodeInfo> getNodes() {
            synchronized (sync) {
                if (nodeInfos.size() <= 0) {
                    try {
                        sync.wait(10000);
                    } catch (InterruptedException e) {
                        logger.error(e.toString(),e);
                    }
                }
                return nodeInfos;
            }
        }

        /**
         * BootstrapNodeInfo list getter, blocks until list become zero size.
         * @return List<BootstrapNodeInfo>
         */
        public List<BootstrapNodeInfo> getNodesAfterRemove() {
            synchronized (sync) {
                if (nodeInfos.size() > 0) {
                    try {
                        sync.wait(10000);
                    } catch (InterruptedException e) {
                        logger.error(e.toString(),e);
                    }
                }
                return nodeInfos;
            }
        }
    }

    @BeforeClass
    public static void init() throws Exception {
        TestCluster.checkStarted();
    }

    @AfterClass
    public static void after() throws Exception {
        TestCluster.stop();
    }

    @Before
    public void beforeTest() throws Exception {
        listener = new TestBootstrapNodeListener();
        executor = Executors.newCachedThreadPool();
        TestCluster.addBootstrapListener(listener);
        bootstrap = new BootstrapStarted();
        assertNotNull("Bootstrap failed to create", bootstrap);
        bootstrap.start();
        assertNotNull("Bootstrap failed to Initialize", bootstrap.getBootstrapInitializationService());

        List<BootstrapNodeInfo> nodes = listener.getNodes();

        logger.info("BootstrapNode found....");

        assertEquals(1, nodes.size());

        assertNotNull("Bootstrap failed to Initialize, supported  channels not initialized",nodes.get(0).getSupportedChannelsArray());
        
        assertEquals(2, nodes.get(0).getSupportedChannelsArray().size());
        
        assertNotNull(nodes.get(0).getSupportedChannelsArray().get(0).getZkChannel());
        
        assertNotNull(nodes.get(0).getSupportedChannelsArray().get(0).getZkChannel().getCommunicationParameters());
        
        assertNotNull(nodes.get(0).getSupportedChannelsArray().get(0).getZkChannel().getChannelType());
        
        assertNotNull(nodes.get(0).getSupportedChannelsArray().get(1).getZkChannel());
       
        assertNotNull(nodes.get(0).getSupportedChannelsArray().get(1).getZkChannel().getCommunicationParameters());
        
        assertNotNull(nodes.get(0).getSupportedChannelsArray().get(1).getZkChannel().getChannelType());
        
        if (nodes.get(0).getSupportedChannelsArray().get(0).getZkChannel().getChannelType() == ZkChannelType.HTTP) {
            ZkHttpComunicationParameters httpParams = (ZkHttpComunicationParameters) nodes.get(0).getSupportedChannelsArray().get(0).getZkChannel().getCommunicationParameters();
            assertNotNull(httpParams.getZkComunicationParameters());
            assertNotNull(httpParams.getZkComunicationParameters().getHostName());
            assertEquals(nettyHost,httpParams.getZkComunicationParameters().getHostName().toString());
            assertEquals(Integer.valueOf(nettyPort),httpParams.getZkComunicationParameters().getPort());
        } else if (nodes.get(0).getSupportedChannelsArray().get(1).getZkChannel().getChannelType() == ZkChannelType.KAATCP) {
            ZkKaaTcpComunicationParameters kaaTcpParams = (ZkKaaTcpComunicationParameters) nodes.get(0).getSupportedChannelsArray().get(1).getZkChannel().getCommunicationParameters();
            assertNotNull(kaaTcpParams.getZkComunicationParameters());
            assertNotNull(kaaTcpParams.getZkComunicationParameters().getHostName());
            assertEquals(kaaTcpHost,kaaTcpParams.getZkComunicationParameters().getHostName().toString());
            assertEquals(Integer.valueOf(kaaTcpPort),kaaTcpParams.getZkComunicationParameters().getPort());
        }
    }

    @After
    public void afterTest() throws Exception {
        bootstrap.shutdown();
        bootstrap = null;
        List<BootstrapNodeInfo> nodes = listener.getNodesAfterRemove();
        assertEquals(0, nodes.size());
        TestCluster.removeBootstrapListener(listener);
        listener = null;
    }


    /**
     * Test Bootstrap update endpoint lists nodes.
     */
    @Test
    public void bootstrapHttpStatisticsUpdateTest() {
        assertNotNull("Bootstrap not started",bootstrap);
        List<BootstrapNodeInfo> nodes = listener.getNodes();

        logger.info("BootstrapNode found....");

        assertEquals(1, nodes.size());

        BootstrapNodeInfo info = nodes.get(0);
        
        Map<ZkChannelType, BaseStatistics> startStatistics = getStatisticsFromNode(info);
        
        logger.info("Found {} channels statistics",startStatistics.size());
        for(int i=0;i<10;i++) {
            makeHttpRequest();
        }
        
        boolean httpOk = false;
        
        for(int j=0;j<10;j++) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
            
            nodes = listener.getNodes();

            logger.info("BootstrapNode found.... {} nodes", nodes.size());
            
            Map<ZkChannelType, BaseStatistics> current = getStatisticsFromNode(nodes.get(0));
            
            assertNotNull(current.get(ZkChannelType.HTTP));
            if (!isStatisticsEquals(startStatistics.get(ZkChannelType.HTTP), current.get(ZkChannelType.HTTP))) {
                httpOk = true;
                logger.info("Updated stats HTTP: {},{},{}",
                        current.get(ZkChannelType.HTTP).getProcessedRequestCount(),
                        current.get(ZkChannelType.HTTP).getDeltaCalculationCount(),
                        current.get(ZkChannelType.HTTP).getRegisteredUsersCount());
            }
            
            if (httpOk) {
               break;
            }
            
        }
        
        
        if (!httpOk) {
            fail("ZK channel statistics HTTP update failed");
        }
        
    }
    
    /**
     * Test Bootstrap update endpoint lists nodes.
     */
    @Test
    public void bootstrapKaaTcpStatisticsUpdateTest() {
        assertNotNull("Bootstrap not started",bootstrap);
        List<BootstrapNodeInfo> nodes = listener.getNodes();

        logger.info("BootstrapNode found....");

        assertEquals(1, nodes.size());

        BootstrapNodeInfo info = nodes.get(0);
        
        Map<ZkChannelType, BaseStatistics> startStatistics = getStatisticsFromNode(info);
        
        logger.info("Found {} channels statistics",startStatistics.size());
        for(int i=0;i<10;i++) {
            makeKaaTcpRequest();
        }
        
        boolean kaatcpOk = false;
        
        for(int j=0;j<10;j++) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
            
            nodes = listener.getNodes();

            logger.info("BootstrapNode found.... {} nodes", nodes.size());
            
            Map<ZkChannelType, BaseStatistics> current = getStatisticsFromNode(nodes.get(0));
            
            assertNotNull(current.get(ZkChannelType.KAATCP));

            if (!isStatisticsEquals(startStatistics.get(ZkChannelType.KAATCP), current.get(ZkChannelType.KAATCP))) {
                kaatcpOk = true;
                logger.info("Updated stats KAATCP: {},{},{}",
                        current.get(ZkChannelType.KAATCP).getProcessedRequestCount(),
                        current.get(ZkChannelType.KAATCP).getDeltaCalculationCount(),
                        current.get(ZkChannelType.KAATCP).getRegisteredUsersCount());
            }
            
            if (kaatcpOk) {
               break;
            }
            
        }
        
        
        if (!kaatcpOk) {
            fail("ZK channel statistics KAATCP update failed");
        }
        
    }
    
    private boolean isStatisticsEquals(BaseStatistics start, BaseStatistics current) {
        boolean isEqual = true;
        logger.trace("start: {},{},{} - current: {},{},{}",
                start.getDeltaCalculationCount(),
                start.getProcessedRequestCount(),
                start.getRegisteredUsersCount(),
                current.getDeltaCalculationCount(),
                current.getProcessedRequestCount(),
                current.getRegisteredUsersCount());
        if (!start.getDeltaCalculationCount().equals(current.getDeltaCalculationCount())) {
            isEqual = false;
        } else if (!start.getProcessedRequestCount().equals(current.getProcessedRequestCount())) {
            isEqual = false;
        } else if (!start.getRegisteredUsersCount().equals(current.getRegisteredUsersCount())) {
            isEqual = false;
        }
        return isEqual;
    }

    private Map<ZkChannelType, BaseStatistics> getStatisticsFromNode(BootstrapNodeInfo info) {
        Map<ZkChannelType, BaseStatistics> statistics = new HashMap<ZkChannelType, BaseStatistics>();
        
        for(BootstrapSupportedChannel channel : info.getSupportedChannelsArray()) {
            if (channel.getZkChannel().getChannelType() == ZkChannelType.HTTP) {
                ZkHttpStatistics httpStat = (ZkHttpStatistics)channel.getZkChannel().getChannelStatistics();
                statistics.put(ZkChannelType.HTTP, httpStat.getZkStatistics());
            } else if (channel.getZkChannel().getChannelType() == ZkChannelType.KAATCP) {
                ZkKaaTcpStatistics httpStat = (ZkKaaTcpStatistics)channel.getZkChannel().getChannelStatistics();
                statistics.put(ZkChannelType.KAATCP, httpStat.getZkStatistics());
            }
        }
        
        return statistics;
    }
    
    
    /**
     * 
     */
    private void makeKaaTcpRequest() {
        final Object httpSync = new Object();
        testFailed = true;
        KaaTcpTestClient tcpClient;
        try {
            tcpClient = new KaaTcpTestClient(kaaTcpHost, kaaTcpPort, "asdcasdcasdc", new HttpActivity() {
                
                @Override
                public void httpRequestComplete(Exception e, int id, OperationsServerList response) {
                    if (e != null) {
                        fail(e.toString());
                    }

                    synchronized (httpSync) {
                        testFailed = false;
                        httpSync.notify();
                    }
                    
                }
            });
            executor.execute(tcpClient);
        } catch (UnknownHostException e) {
            logger.error(e.toString(), e);
            testFailed = true;
        } catch (IOException e) {
            logger.error(e.toString(), e);
            testFailed = true;
        }

        synchronized (httpSync) {
            try {
                httpSync.wait(10000);
                logger.info("Going to test Operations Server List using KaaTcp update..... complete.");
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }
        if (testFailed) {
            fail("Test KaaTcp failed");
        }
        
    }

    /**
     * 
     */
    private void makeHttpRequest() {
        PublicKey serverPublicKey = bootstrap.getBootstrapInitializationService().getKeyStoreService().getPublicKey();
        final Object httpSync = new Object();
        testFailed = true;
        TestClient client;
        try {
            client = new TestClient(nettyHost, nettyPort, serverPublicKey, new HttpActivity() {

                @Override
                public void httpRequestComplete(Exception e, int id,
                        OperationsServerList response) {

                    if (e != null) {
                        fail(e.toString());
                    }

                    synchronized (httpSync) {
                        testFailed = false;
                        httpSync.notify();
                    }
                }
            });
            executor.execute(client);
        } catch (IOException e) {
            fail(e.toString());
        }


        synchronized (httpSync) {
            try {
                httpSync.wait(10000);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }
        
        if (testFailed) {
            fail("Test KaaTcp failed");
        }
    }



}
