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

package org.kaaproject.kaa.server.operations.service.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.thrift.TMultiplexedProcessor;
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
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.thrift.OperationsThriftServiceImpl;
import org.kaaproject.kaa.server.sync.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EventService test Class. Test Thrift EventMessage sending and receiving
 * procedures. Run ZK test cluster. Run 2 different thrift servers. Check that
 * both is registered in each other, also check one fake thrift server to check
 * errors on delivery messages. Send 3 different types of messages:
 * UserRouteInfo RouteInfo RemoteEndpointEvent
 * 
 * @author Andrey Panasenko
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/operations/operations-server-eventService-test-IT.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class EventServiceThriftTestIT {
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EventServiceThriftTestIT.class);

    /** ZooKeeper port constant */
    public static final int ZK_PORT = 21810;

    /** Thread executor */
    private static ExecutorService executor = null;

    /** Event service for fake thrift server */
    @Autowired
    private EventService eventService;

    /**
     * default number of started working thrift service, first server is used to
     * send event messages to second server
     */
    private final int numberOfServers = 2;

    /** Default thrift host, used on both servers */
    private final String thriftHostBase = "localhost";

    /**
     * Base thrift port, real is calculated with adding index of server, so
     * first have 9810, second 9811
     */
    private final int thriftPortBase = 9810;

    /** List wich hold all running thrift servers */
    private static List<ThriftRunner> thriftServers;

    /** Checking listener to check that all servers started */
    private static OperationsNodesListener listener;

    /**
     * Initialization class, run ZK Cluster, register check listener and
     * initialize other structures.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void init() throws Exception {
        TestCluster.checkStarted();
        listener = new OperationsNodesListener();
        TestCluster.addOperationsListener(listener);
        executor = Executors.newCachedThreadPool();
        thriftServers = new LinkedList<>();
    }

    /**
     * Stops services.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void after() throws Exception {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        thriftServers.clear();
        thriftServers = null;
        TestCluster.removeOperationsListener(listener);
        TestCluster.stop();
    }

    @Before
    public void beforeTest() throws Exception {
        for (int i = 0; i < numberOfServers; i++) {
            ThriftRunner runner = new ThriftRunner(thriftHostBase, thriftPortBase + i);
            executor.execute(runner);
            thriftServers.add(runner);
//            waitNeigborsRegister(runner.getEventService(), i + 1);
        }
        
        eventService.setZkNode(TestCluster.getOperationsNode());
    }

    @After
    public void afterTest() throws Exception {
        for (ThriftRunner runner : thriftServers) {
            runner.shutdown();
        }
        eventService.shutdown();
    }

    /**
     * OperationsNode Listener Class. Used to check that all nodes started and
     * registred.
     */
    public static class OperationsNodesListener implements OperationsNodeListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener
         * #
         * onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo
         * )
         */
        @Override
        public void onNodeAdded(OperationsNodeInfo nodeInfo) {
            LOG.info("Operation Node {}:{} added", nodeInfo.getConnectionInfo().getThriftHost().toString(), nodeInfo.getConnectionInfo()
                    .getThriftPort().toString());
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener
         * #
         * onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo
         * )
         */
        @Override
        public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
            LOG.info("Operation Node {}:{} updated", nodeInfo.getConnectionInfo().getThriftHost().toString(), nodeInfo.getConnectionInfo()
                    .getThriftPort().toString());
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener
         * #
         * onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo
         * )
         */
        @Override
        public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
            LOG.info("Operation Node {}:{} removed", nodeInfo.getConnectionInfo().getThriftHost().toString(), nodeInfo.getConnectionInfo()
                    .getThriftPort().toString());
        }

    }

    /**
     * ThriftRunner Class. Used to run thrift servers.
     */
    public class ThriftRunner implements Runnable {

        private final String thriftHost;
        private final int thriftPort;

        private final OperationsThriftServiceImpl operationsThriftService;
        private OperationsNode operationsNode;

        /** The server. */
        private TServer server;

        private final EventService eventService;

        /**
         * @return the eventService
         */
        public EventService getEventService() {
            return eventService;
        }

        public ThriftRunner(String thriftHost, int thriftPort) {
            this.thriftHost = thriftHost;
            this.thriftPort = thriftPort;
            DefaultEventService eventServiceInst = new DefaultEventService();
            eventServiceInst.initBean();
            OperationsServerConfig config = new OperationsServerConfig();
            ReflectionTestUtils.setField(eventServiceInst, "operationsServerConfig", config);

            eventService = eventServiceInst;
            operationsThriftService = new OperationsThriftServiceImpl();
            // operationsThriftService.setEventService(eventService);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            LOG.info("Initializing Thrift Service for Operations Server....");
            LOG.info("thrift host: {}", thriftHost);
            LOG.info("thrift port: {}", thriftPort);

            registerZK();

            try {
                
                TMultiplexedProcessor processor = new TMultiplexedProcessor();
                
                OperationsThriftService.Processor<OperationsThriftService.Iface> operationsProcessor = new OperationsThriftService.Processor<OperationsThriftService.Iface>(
                        operationsThriftService);
                
                processor.registerProcessor(KaaThriftService.OPERATIONS_SERVICE.getServiceName(), operationsProcessor);
                
                TServerTransport serverTransport = new TServerSocket(new InetSocketAddress(thriftHost, thriftPort));
                server = new TThreadPoolServer(new Args(serverTransport).processor(processor));

                LOG.info("Operations Server {}:{} Started.", thriftHost, thriftPort);

                server.serve();

                LOG.info("Operations Server {}:{} Stopped.", thriftHost, thriftPort);

            } catch (TTransportException e) {
                LOG.error("TTransportException", e);
            }
        }

        public void registerZK() {
            LOG.info("Registring Operations Server in ZK {}:{}", thriftHost, thriftPort);
            OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
            ByteBuffer keyData = ByteBuffer.wrap(new byte[] { 45, 45, 45, 45, 45 });
            ConnectionInfo connectionInfo = new ConnectionInfo(thriftHost, thriftPort, keyData);
            nodeInfo.setConnectionInfo(connectionInfo);
            nodeInfo.setLoadInfo(new LoadInfo(1, 1.0));
            nodeInfo.setTransports(new ArrayList<TransportMetaData>());
            String zkHostPortList = "localhost:" + ZK_PORT;
            operationsNode = new OperationsNode(nodeInfo, zkHostPortList, new RetryUntilElapsed(3000, 1000));
            try {
                operationsNode.start();
                eventService.setZkNode(operationsNode);
                LOG.info("Operations Server {}:{} Zk node set in Config", thriftHost, thriftPort);
            } catch (Exception e) {
                LOG.error("Exception: ", e);
            }
        }

        public void shutdown() {
            LOG.info("Operations Server {}:{} shutdown()", thriftHost, thriftPort);
            eventService.shutdown();
            server.stop();
        }

        /**
         * @return the thriftHost
         */
        public String getThriftHost() {
            return thriftHost;
        }

        /**
         * @return the thriftPort
         */
        public int getThriftPort() {
            return thriftPort;
        }
    }

    /**
     * Test Class. Used to gather test pass.
     */
    public class ESTest {
        private int callCounter = 4;

        private final Object sync = new Object();

        private boolean onServerError = false;

        private boolean onUserRouteInfo = false;

        private boolean onRouteInfo = false;

        private boolean onEvent = false;;

        /**
         * @param onServerError
         *            the onServerError to set
         */
        public void setOnServerError(boolean onServerError) {

            synchronized (sync) {
                if (this.onServerError != onServerError) {
                    this.onServerError = onServerError;
                    callCounter--;
                    sync.notify();
                }
            }
        }

        /**
         * @param onUserRouteInfo
         *            the onUserRouteInfo to set
         */
        public void setOnUserRouteInfo(boolean onUserRouteInfo) {
            this.onUserRouteInfo = onUserRouteInfo;
            synchronized (sync) {
                callCounter--;
                sync.notify();
            }
        }

        public void setOnRouteInfo(boolean onRouteInfo) {
            this.onRouteInfo = onRouteInfo;
            synchronized (sync) {
                callCounter--;
                sync.notify();
            }
        }

        /**
         * @param onEvent
         */
        public void setOnEvent(boolean onEvent) {
            this.onEvent = onEvent;
            synchronized (sync) {
                callCounter--;
                sync.notify();
            }
        }

        public void waitFinish() {
            while (callCounter > 0) {
                synchronized (sync) {
                    try {
                        sync.wait(50000);
                    } catch (InterruptedException e) {
                        fail(e.toString());
                    }
                }
            }
        }

        public boolean isESTestComplete() {
            return this.onServerError & this.onUserRouteInfo & this.onRouteInfo & this.onEvent;
        }

    }

    //TODO: make new test
    @Test
    @Ignore("Need to refactor this")
    public void nodesInitializationTest() {
        LOG.info("Starting initialization tests...");

        final ESTest test = new ESTest();

        EventService eventService1 = thriftServers.get(0).getEventService();
        EventService eventService2 = thriftServers.get(1).getEventService();

        assertNotNull(eventService1);
        assertNotNull(eventService2);

        // Register listener to receive errors of transition event messages
        eventService1.addListener(new EventServiceListener() {

            @Override
            public void onUserRouteInfo(UserRouteInfo routeInfo) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onServerError(String serverId) {
                LOG.info("Server {} error", serverId);
                assertEquals(TestCluster.OPERATIONS_NODE_HOST + ":1000", serverId);
                test.setOnServerError(true);
            }

            @Override
            public void onRouteInfo(RouteInfo routeInfo) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEvent(RemoteEndpointEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEndpointRouteUpdate(GlobalRouteInfo update) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEndpointStateUpdate(EndpointUserConfigurationUpdate update) {
                // TODO Auto-generated method stub

            }

        });

        LOG.info("Servers started sucessfully...");

        String serverId1 = thriftServers.get(1).getThriftHost() + ":" + thriftServers.get(0).getThriftPort();
        String serverId2 = thriftServers.get(1).getThriftHost() + ":" + thriftServers.get(1).getThriftPort();

        // Generate UserRouteInfo
        final UserRouteInfo sendUserRrouteInfo = new UserRouteInfo("tenant1", "user1", "localhost:9810", RouteOperation.DELETE);

        // Generate RouteInfo
        EndpointObjectHash endpointKey = EndpointObjectHash.fromBytes(new byte[] { 30, 30, 30, 30, 30 });
        RouteTableAddress address = new RouteTableAddress(endpointKey, "appToken1", serverId1);
        List<EventClassFamilyVersion> ecfVersions = new ArrayList<>();
        ecfVersions.add(new EventClassFamilyVersion("ecfid1", 1));
        final RouteInfo sendRouteInfo = new RouteInfo("tenant1", "user1", address, ecfVersions);

        // generate event
        Event avroEvent = new Event(0, "eventClassFQN1", ByteBuffer.wrap("adcascd".getBytes()), "localhost:9810", "localhost:9811");
        EndpointEvent endpointEvent = new EndpointEvent(endpointKey, avroEvent, UUID.randomUUID(), System.currentTimeMillis(), 10);
        RouteTableAddress recipient = new RouteTableAddress(endpointKey, "appToken", serverId2);
        final RemoteEndpointEvent sendEvent = new RemoteEndpointEvent("tenant1", "user1", endpointEvent, recipient);

        // Register listener to event messages
        eventService2.addListener(new EventServiceListener() {

            @Override
            public void onUserRouteInfo(UserRouteInfo routeInfo) {
                LOG.info("Got onUserRouteInfo serverId={}; tenantId={}; userId={}", routeInfo.getServerId(), routeInfo.getTenantId(),
                        routeInfo.getUserId());
                assertEquals(sendUserRrouteInfo, routeInfo);
                test.setOnUserRouteInfo(true);
            }

            @Override
            public void onRouteInfo(RouteInfo routeInfo) {
                LOG.info("Got onRouteInfo {}", routeInfo.toString());
                // This sleep is used to check synchronous connections
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                LOG.info("After timeout");
                assertEquals(sendRouteInfo, routeInfo);
                test.setOnRouteInfo(true);
            }

            @Override
            public void onEvent(RemoteEndpointEvent event) {
                LOG.info("Got onEvent {}", event.toString());
                assertEquals(sendEvent, event);
                test.setOnEvent(true);
            }

            @Override
            public void onServerError(String serverId) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEndpointRouteUpdate(GlobalRouteInfo update) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEndpointStateUpdate(EndpointUserConfigurationUpdate update) {
                // TODO Auto-generated method stub

            }

        });

        LOG.info("Sending UserRoute Info: {}", sendUserRrouteInfo.toString());
        eventService1.sendUserRouteInfo(sendUserRrouteInfo);

        LOG.info("Sending Route Info: {}", sendRouteInfo.toString());
        eventService1.sendRouteInfo(sendRouteInfo, serverId2);
        LOG.info(">>>>>>>>>>>>>>>After Sending Route Info");

        LOG.info("Sending Event : {}", sendEvent.toString());
        eventService1.sendEvent(sendEvent);

        test.waitFinish();

        if (!test.isESTestComplete()) {
            fail("Event Service Test failed");
        }
        LOG.info("Event Service test complete");

    }
}
