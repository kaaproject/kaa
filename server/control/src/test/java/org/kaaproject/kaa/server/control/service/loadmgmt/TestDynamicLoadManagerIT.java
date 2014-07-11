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
package org.kaaproject.kaa.server.control.service.loadmgmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.SystemColor;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DynamicLoadManager Integration test, emulates new Bootstrap node adding.
 * @author Andrey Panasenko
 *
 */
public class TestDynamicLoadManagerIT {
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(TestDynamicLoadManagerIT.class);


    /** Thrift host for Bootstrap test service */
    private static final String thriftHost = "localhost";

    /** Thrift port for Bootstrap test service */
    private static final int thriftPort = 9819;


    /** Thread executor */
    private static ExecutorService executor = null;

    /** Bootstrap thrift test service runner*/
    private ThriftRunner bootstrapThrift;

    private static LoadDistributionService ldServiceMock;
    private static ControlZkService zkServiceMock;
    private static ControlNode pNodeMock;

    /**
     * ThriftRunner Class. Used to run thrift servers.
     */
    public class ThriftRunner implements Runnable {

        private final String thriftHost;
        private final int thriftPort;

        private final BootstrapThriftServiceImpl bootstrapThriftService;

        private boolean stopComplete = false;
        private boolean startComplete = false;
        private final Object stopSync;
        private final Object startSync;

        /** The server. */
        private TServer server;

        public ThriftRunner(String thriftHost, int thriftPort) {
            this.thriftHost = thriftHost;
            this.thriftPort = thriftPort;
            this.stopSync = new Object();
            this.startSync = new Object();
            bootstrapThriftService = new BootstrapThriftServiceImpl();
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            LOG.info("Initializing Thrift Service for Bootstrap Server....");
            LOG.info("thrift host: {}",thriftHost);
            LOG.info("thrift port: {}",thriftPort);
            try {
                BootstrapThriftService.Processor<BootstrapThriftService.Iface> processor
                        = new BootstrapThriftService.Processor<BootstrapThriftService.Iface>(bootstrapThriftService);
                TServerTransport serverTransport = new TServerSocket(new InetSocketAddress(thriftHost, thriftPort));
                server = new TThreadPoolServer(new Args(serverTransport).processor(processor));

                LOG.info("Bootstrap test Server {}:{} Started.",thriftHost,thriftPort);
                synchronized (startSync) {
                    startComplete = true;
                    startSync.notify();
                }
                server.serve();

                LOG.info("Bootstrap test Server {}:{} Stopped.",thriftHost,thriftPort);
            } catch (TTransportException e) {
                LOG.error("TTransportException", e);
            } finally {
                synchronized (stopSync) {
                    stopComplete = true;
                    bootstrapThriftService.reset();
                    stopSync.notify();
                }
            }
        }

        public void waitStart() {
            LOG.info("Bootstrap test Server {}:{} waitStart()",thriftHost,thriftPort);
            synchronized (startSync) {
                if (!startComplete) {
                    try {
                        startSync.wait(60000);
                    } catch (InterruptedException e) {
                        LOG.error("Interupted ThiftRunner startWait()", e);
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("Interupted ThiftRunner startWait() in sleep", e);
            }
        }
        
        public void shutdown() {
            LOG.info("Bootstrap test Server {}:{} shutdown()",thriftHost,thriftPort);
            server.stop();
            synchronized (stopSync) {
                if (!stopComplete) {
                    try {
                        stopSync.wait(60000);
                    } catch (InterruptedException e) {
                        LOG.error("Interupted ThiftRunner shutdown", e);
                    }
                }
            }
        }

        public BootstrapThriftServiceImpl getBootstrapThriftServiceImpl() {
            return bootstrapThriftService;
        }
    }

    /**
     * Initialize mock objects and necessary test services
     * @throws Exception
     */
    @BeforeClass
    public static void init() throws Exception {
        executor = Executors.newCachedThreadPool();
        ldServiceMock = mock(LoadDistributionService.class);
        zkServiceMock = mock(ControlZkService.class);
        pNodeMock = mock(ControlNode.class);
        when(ldServiceMock.getOpsServerHistoryTTL()).thenReturn(300);
        when(ldServiceMock.getDynamicMgmtClass()).thenReturn("org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.DefaultRebalancer");
        when(ldServiceMock.getZkService()).thenReturn(zkServiceMock);
        when(zkServiceMock.getControlZKNode()).thenReturn(pNodeMock);
    }

    /**
     * Stops services.
     * @throws Exception
     */
    @AfterClass
    public static void after() throws Exception {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    /**
     * Start Bootstrap thrift service
     * @throws Exception
     */
    @Before
    public void beforeTest() throws Exception {
        bootstrapThrift = new ThriftRunner(thriftHost, thriftPort);
        executor.execute(bootstrapThrift);
        bootstrapThrift.waitStart();
    }

    /**
     * Stop bootstrap hrift service
     * @throws Exception
     */
    @After
    public void afterTest() throws Exception {
        bootstrapThrift.shutdown();
    }

    /**
     * Test Bootstrap node add.
     */
    @Test
    public void bootstrapNodeAddTest() {
        LOG.info("bootstrapNodeAddTest started");
        
        DynamicLoadManager dm = getDynamicLoadManager();

        ConnectionInfo bsConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsNode = new BootstrapNodeInfo(bsConnectionInfo , "localhost", 9897, 0);
        dm.onNodeAdded(bsNode);

        checkBSNode();
    }

    /**
     * Test Bootstrap Node update
     */
    @Test
    public void bootstrapNodeUpdateTest() {
        LOG.info("BootstrapNodeUpdateTest started");
        DynamicLoadManager dm = getDynamicLoadManager();


        ConnectionInfo bsErrConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort+1,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsErrNode = new BootstrapNodeInfo(bsErrConnectionInfo ,"localhost", 9898, 0);

        dm.onNodeAdded(bsErrNode);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }

        ConnectionInfo bsConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsNode = new BootstrapNodeInfo(bsConnectionInfo ,"localhost", 9898, 0);

        dm.onNodeUpdated(bsNode);

        dm.recalculate();

        checkBSNode();

    }

    /**
     * Test Bootstrap Node remove
     */
    @Test
    public void bootstrapNodeDeleteTest() {
        LOG.info("BootstrapNodeUpdateTest started");
        DynamicLoadManager dm = getDynamicLoadManager();


        ConnectionInfo bsErrConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort+1,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsErrNode = new BootstrapNodeInfo(bsErrConnectionInfo ,"localhost", 9897, 0);

        dm.onNodeAdded(bsErrNode);

        ConnectionInfo bsConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsNode = new BootstrapNodeInfo(bsConnectionInfo , "localhost", 9898, 0);

        dm.onNodeAdded(bsNode);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }

        dm.onNodeRemoved(bsErrNode);

        dm.recalculate();

        checkBSNode();
    }

    /**
     * Test Operations Node Update
     * Update with two phases,
     *  one with same ConnectionInfo DNS Name,
     *  second with changed ConnectionInfo DNS Name
     */
    @Test
    public void operationsNodeUpdateTest() {
        LOG.info("BootstrapNodeUpdateTest started");
        
        DynamicLoadManager dm = getDynamicLoadManager();

        OperationsNodeInfo nodeInfo = generateOperationsNodeInfo("localhost",1200,9898,ByteBuffer.wrap("Just array modified".getBytes()));

        dm.onNodeUpdated(nodeInfo);
        LOG.info("BootstrapNodeTest Operations Node {} updated.", nodeInfo.toString());


        OperationsNodeInfo nodeInfo2 = generateOperationsNodeInfo("localhost",1201,9899,ByteBuffer.wrap("Just array modified".getBytes()));

        dm.onNodeUpdated(nodeInfo2);
        LOG.info("BootstrapNodeTest Operations Node {} updated.", nodeInfo.toString());

        ConnectionInfo bsConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsNode = new BootstrapNodeInfo(bsConnectionInfo ,"localhost", 9898, 0);

        dm.onNodeAdded(bsNode);

        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl());
        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap() );

        assertEquals(2,bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().size());

        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1200"));
        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1201"));

        assertEquals((long)10,(long)bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1200").getPriority());
        assertEquals((long)10,(long)bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1201").getPriority());

        byte[] pk = bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1200").getPublicKey().array();
        assertEquals("Just array modified".getBytes().length,pk.length);
        for (int i = 0; i < pk.length; i++) {
            assertEquals("Just array modified".getBytes()[i],pk[i]);
        }
    }

    /**
     * Test Operations Node Remove
     */
    @Test
    public void operationsNodeRemoveTest() {
        LOG.info("BootstrapNodeRemoveTest started");
        bootstrapThrift.getBootstrapThriftServiceImpl().reset();
        
        DynamicLoadManager dm = getDynamicLoadManager();

        ConnectionInfo bsConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsNode = new BootstrapNodeInfo(bsConnectionInfo ,"localhost", 9898, 0);

        dm.onNodeAdded(bsNode);

        checkBSNode();

        
        OperationsNodeInfo nodeInfo = generateOperationsNodeInfo("localhost",1201,9899,ByteBuffer.wrap("Just".getBytes()));

        bootstrapThrift.getBootstrapThriftServiceImpl().reset();

        dm.onNodeAdded(nodeInfo);

        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap());

        assertEquals(2,bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().size());

        bootstrapThrift.getBootstrapThriftServiceImpl().reset();

        dm.onNodeRemoved(nodeInfo);

        checkBSNode();
    }

    @Ignore("TODO: FIX before merge to master")
    @Test
    public void sendRedirectionRuleTest() {
        LOG.info("BootstrapNodeUpdateTest started");
        DynamicLoadManager dm = getDynamicLoadManager();
        long timeStarted = System.currentTimeMillis();

        OperationsNodeInfo nodeInfo1 = getUpdatedOperationsNode(9898,10,timeStarted);
        OperationsNodeInfo nodeInfo2 = getUpdatedOperationsNode(9899,0,timeStarted);
        LOG.info("BootstrapNodeTest Operations Node {} updated. 1", nodeInfo1.toString());
        dm.onNodeUpdated(nodeInfo1);
        dm.onNodeUpdated(nodeInfo2);

        nodeInfo1 = getUpdatedOperationsNode(9898,30,timeStarted+300000);
        nodeInfo2 = getUpdatedOperationsNode(9899,0,timeStarted+300000);
        LOG.info("BootstrapNodeTest Operations Node {} updated. 2", nodeInfo1.toString());
        dm.onNodeUpdated(nodeInfo1);
        dm.onNodeUpdated(nodeInfo2);

        nodeInfo1 = getUpdatedOperationsNode(9898,55,timeStarted+600000);
        nodeInfo2 = getUpdatedOperationsNode(9899,0,timeStarted+600000);
        LOG.info("BootstrapNodeTest Operations Node {} updated. 3", nodeInfo1.toString());
        dm.onNodeUpdated(nodeInfo1);
        dm.onNodeUpdated(nodeInfo2);

        ConnectionInfo bsConnectionInfo = new ConnectionInfo(
                thriftHost,
                thriftPort,
                ByteBuffer.wrap("Just array".getBytes()));
        BootstrapNodeInfo bsNode = new BootstrapNodeInfo(bsConnectionInfo ,"localhost", 9898, 0);

        dm.onNodeAdded(bsNode);

        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl());
        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap());

        assertEquals(2,bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().size());


        bootstrapThrift.getBootstrapThriftServiceImpl().reset();

        dm.recalculate();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }


    private OperationsNodeInfo getUpdatedOperationsNode(int httpPort, int processRequestCount, long timeStart) {
        Long timeStarted = new Long(timeStart);
        Integer deltaCalculationCount = new Integer(0);
        Integer registeredUsersCount = new Integer(0);
        Integer processedRequestCount = new Integer(processRequestCount);

        OperationsNodeInfo nodeInfo = generateOperationsNodeInfo("localhost",1200,httpPort,ByteBuffer.wrap("Just array".getBytes()));

        return nodeInfo;
    }

    private DynamicLoadManager getDynamicLoadManager() {
        DynamicLoadManager dm = new DynamicLoadManager(ldServiceMock);
        assertNotNull(dm);
        Long timeStarted = new Long(0);
        Integer deltaCalculationCount = new Integer(0);
        Integer registeredUsersCount = new Integer(0);
        Integer processedRequestCount = new Integer(0);

        OperationsNodeInfo nodeInfo = generateOperationsNodeInfo("localhost",1200,9898,ByteBuffer.wrap("Just array".getBytes()));
        dm.onNodeAdded(nodeInfo);
        LOG.info("BootstrapNodeTest Operations Node {} added.", nodeInfo.toString());
        return dm;
    }

    private void checkBSNode() {
        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl());
        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap());

        assertEquals(1,bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().size());

        assertNotNull(bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1200"));


        assertEquals((long)10,(long)bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1200").getPriority());
        byte[] pk = bootstrapThrift.getBootstrapThriftServiceImpl().getOperatonsServerMap().get("localhost:1200").getPublicKey().array();
        assertEquals("Just array".getBytes().length,pk.length);
        for (int i = 0; i < pk.length; i++) {
            assertEquals("Just array".getBytes()[i],pk[i]);
        }
    }

    private OperationsNodeInfo generateOperationsNodeInfo(String thriftHost, int thriftPort, int httpPort, ByteBuffer publicKey) {
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        nodeInfo.setTimeStarted(System.currentTimeMillis());
        List<SupportedChannel> supportedChannels = new ArrayList<>();
        ZkHttpComunicationParameters httpCommunicationParameters = new ZkHttpComunicationParameters(new IpComunicationParameters("localhost", httpPort));
        BaseStatistics statistics = new BaseStatistics(0, 0, 0, System.currentTimeMillis());
        ZkHttpStatistics httpChannelStatistics = new ZkHttpStatistics(statistics );
        SupportedChannel channelHttp = new SupportedChannel(new ZkSupportedChannel(ZkChannelType.HTTP, true, httpCommunicationParameters, httpChannelStatistics));
        supportedChannels.add(channelHttp);
        nodeInfo.setSupportedChannelsArray(supportedChannels );
        nodeInfo.setConnectionInfo(new ConnectionInfo(thriftHost, thriftPort, publicKey));
        return nodeInfo;
    }
}

