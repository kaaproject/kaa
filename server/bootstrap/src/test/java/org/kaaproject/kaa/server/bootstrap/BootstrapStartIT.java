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
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.kaaproject.kaa.server.bootstrap.service.initialization.DefaultBootstrapInitializationService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftChannelType;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftCommunicationParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftIpParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService.Client;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftSupportedChannel;
import org.kaaproject.kaa.server.common.thrift.util.ThriftActivity;
import org.kaaproject.kaa.server.common.thrift.util.ThriftClient;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
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
public class BootstrapStartIT {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(BootstrapStartIT.class);

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

    /** Random generator*/
    private static Random rnd = new Random();

    /** ZooKeeper port constant */
    public static final int ZK_PORT = 21810;

    /** Bootstarp starter */
    private BootstrapStarted bootstrap;

    /** BootstrapNode change listener */
    private static TestBootstrapNodeListener listener;

    /** Thrift command complete */
    private boolean thriftComplete = false;

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
            logger.info("Bootstrap node added "+nodeInfo.getConnectionInfo().toString());
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

        }

        /* (non-Javadoc)
         * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
         */
        @Override
        public void onNodeRemoved(BootstrapNodeInfo nodeInfo) {
            logger.info("Bootstrap node removed "+nodeInfo.getConnectionInfo().toString());
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
    public void BootstrapUpdateTest() {
        assertNotNull("Bootstrap not started",bootstrap);
        List<BootstrapNodeInfo> nodes = listener.getNodes();

        logger.info("BootstrapNode found....");

        assertEquals(1, nodes.size());

        BootstrapNodeInfo info = nodes.get(0);

        assertEquals(nettyHost, info.getBootstrapHostName().toString());

        assertEquals((long)nettyPort, (long)info.getBootstrapPort());

        assertEquals(thriftHost, info.getConnectionInfo().getThriftHost().toString());

        assertEquals((long)thriftPort, (long)info.getConnectionInfo().getThriftPort());

        ByteBuffer keyData = ByteBuffer.wrap(bootstrap.getBootstrapInitializationService().getKeyStoreService().getPublicKey().getEncoded());

        assertEquals(keyData, info.getConnectionInfo().getPublicKey());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }

        logger.info("Going to test Thrift OperationsServerList update.....");

        final List<ThriftOperationsServer> update = generateDnsUpdate();

        logger.info("Generated "+update.size()+" endpoint servers");

        updateBootstrap(nodes.get(0), update);

        DefaultBootstrapInitializationService defBootstrapInitService = (DefaultBootstrapInitializationService) bootstrap.getBootstrapInitializationService();
        List<OperationsServer> list = defBootstrapInitService.getOperationsServerListService().getOpsServerList().getOperationsServerArray();

        assertEquals(update.size(), list.size());

        for(OperationsServer server : list) {
            String host = server.getName();
            logger.info("Server Name {} 1",host);
            ThriftOperationsServer sendServ = getThriftOperationsServerByName(host, update);
            if (sendServ != null) {
                
                assertEquals(sendServ.priority, server.getPriority().intValue());

                assertEquals(sendServ.publicKey, server.getPublicKey());

            } else {
                fail("Test failed, received Operations Server list don't have "+host+" but must");
            }
        }

        logger.info("Going to test HTTP Operations Server List update.....");

        PublicKey serverPublicKey = bootstrap.getBootstrapInitializationService().getKeyStoreService().getPublicKey();

        final Object httpSync = new Object();

        TestClient client;
        try {
            client = new TestClient(nettyHost, nettyPort, serverPublicKey, new HttpActivity() {

                @Override
                public void httpRequestComplete(Exception e, int id,
                        OperationsServerList response) {

                    if (e != null) {
                        testFailed = true;
                        fail(e.toString());
                    }

                    synchronized (httpSync) {
                        List<OperationsServer> respList = response.getOperationsServerArray();

                        assertEquals(update.size(), respList.size());

                        for(OperationsServer server : respList) {
                            String host = server.getName();
                            logger.info("Server Name {} 2",host);
                            ThriftOperationsServer sendServ = getThriftOperationsServerByName(host,update);
                            if (sendServ != null) {
                                
                                assertEquals(sendServ.priority, server.getPriority().intValue());

                                assertEquals(sendServ.publicKey, server.getPublicKey());

                            } else {
                                fail("Test failed, received Operations Server list don't have "+host+" but must");
                            }
                        }
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
                logger.info("Going to test HTTP Operations Server List update..... complete.");
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        if (testFailed) {
            fail("Test failed");
        }

    }


    /**
     * Update bootstrap.
     *
     * @param nodeInfo the node info
     */
    private void updateBootstrap(BootstrapNodeInfo nodeInfo, final List<ThriftOperationsServer> update) {
        final Object sync = new Object();
        thriftComplete = false;
        logger.debug("Update bootstrap server: Thrift: "+nodeInfo.getConnectionInfo().getThriftHost().toString()+":"+nodeInfo.getConnectionInfo().getThriftPort());
        try {
            ThriftClient<BootstrapThriftService.Client> client = new ThriftClient<BootstrapThriftService.Client>(
                            nodeInfo.getConnectionInfo().getThriftHost().toString(),
                            nodeInfo.getConnectionInfo().getThriftPort(),
                            BootstrapThriftService.Client.class);
            client.setThriftActivity(new ThriftActivity<BootstrapThriftService.Client>() {

                @Override
                public void isSuccess(boolean activitySuccess) {
                    logger.info("Bootstrap  Operations servers list updated successfull: "+activitySuccess);
                    synchronized (sync) {
                        thriftComplete = true;
                        sync.notify();
                    }
                    if (!activitySuccess) {
                        fail("Error send Thrift Operations Server List update");
                    }
                }

                @Override
                public void doInTemplate(Client t) {
                    try {
                        t.onOperationsServerListUpdate(update);
                        logger.info("Bootstrap  Operations servers list updated.");
                    } catch (TException e) {
                        logger.error("Bootstrap  Operations servers list updated failed: "+e.toString());
                        fail(e.toString());
                    }
                }
            });

            ThriftExecutor.execute(client);
        } catch (NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            logger.error("Bootstrap Operations servers list execute updated failed: "+e.toString());
            fail(e.toString());
        }
        synchronized (sync) {
            if (!thriftComplete) {
                try {
                    sync.wait(10000);
                } catch (InterruptedException e) {
                    fail(e.toString());
                }
            }
        }
    }

    /**
     * Generate ThriftOperationsServer list.
     * @return List<ThriftOperationsServer>
     */
    private List<ThriftOperationsServer> generateDnsUpdate() {
        int updateSize = rnd.nextInt(10);
        List<ThriftOperationsServer> operationsServersList = new ArrayList<>();
        for(int i=0;i<updateSize;i++) {
            List<ThriftSupportedChannel> scList = new ArrayList<>();
            String host = rnd.nextInt(255)+"."+rnd.nextInt(255)+"."+rnd.nextInt(255)+"."+rnd.nextInt(255);
            int port = 1000+rnd.nextInt(32768);
            ThriftIpParameters ipParams = new ThriftIpParameters(host,port);
            ThriftCommunicationParameters comParam = new ThriftCommunicationParameters();
            comParam.setHttpParams(ipParams);
            ThriftSupportedChannel sc = new ThriftSupportedChannel(ThriftChannelType.HTTP, comParam);
            scList.add(sc);
            ThriftOperationsServer s = new ThriftOperationsServer(host, rnd.nextInt(100), getRandomByteBuffer(256), scList );
            operationsServersList.add(s);
        }
        return operationsServersList;
    }
    
    private ThriftOperationsServer getThriftOperationsServerByName(String name, List<ThriftOperationsServer> list) {
        ThriftOperationsServer server = null;
        for(ThriftOperationsServer s : list) {
            logger.info("getThriftOperationsServerByName name: {}; s.getName {}",name,s.getName()); 
            if (s.getName() != null && s.getName().equals(name)) {
                server = s;
                break;
            }
        }
        return server;
    }
    
    private ByteBuffer getRandomByteBuffer(int size) {
        byte[] buffer = new byte[size];
        rnd.nextBytes(buffer);
        return ByteBuffer.wrap(buffer);
    }
}
