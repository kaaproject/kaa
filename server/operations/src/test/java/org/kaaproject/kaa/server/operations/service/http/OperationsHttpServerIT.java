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

package org.kaaproject.kaa.server.operations.service.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.server.common.http.server.NettyHttpServer;
import org.kaaproject.kaa.server.common.http.server.SessionTrackable;
import org.kaaproject.kaa.server.common.http.server.Track;
import org.kaaproject.kaa.server.operations.service.bootstrap.DefaultOperationsBootstrapService;
import org.kaaproject.kaa.server.operations.service.config.HttpChannelConfig;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.config.ServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.security.FileKeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class OperationsHttpServerIT {

    /** Port which used to bind to for Netty HTTP */
    public static final int bindPort = 9194;

    /** Max HTTP request size which used in Netty framework */
    public static final int MAX_HTTP_REQUEST_SIZE = 65536;

    /** Max header fields in test messages */
    public static final int MAX_HEADER_FIELD_SIZE = 2048;

    /** Max response body size */
    public static final int MAX_RESPONSE_BODY = 40960;

        /** Number of tests */
    public static final int NUMBER_OF_TESTS = 100;

    /** Max test wait timeout, in ms */
    public static final int MAX_TEST_TIMEOUT = NUMBER_OF_TESTS*1000;


    /** HTTP request header field, used to set Test ID */
    public static final String REQUEST_ID = "RequestID";

    /** HTTP header field to transmit random String in headers */
    public static final String REQUEST_RANDOM = "RequestRandom";

    /** HTTP POST filed to transmit request body */
    public static final String REQUEST_DATA = "RequestData";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(OperationsHttpServerIT.class);

    private static ExecutorService executor = null;

    private NettyHttpServer netty;

    private static OperationsServerConfig config;

    private static DefaultOperationsBootstrapService bootstrapServiceMock;

    private static FileKeyStoreService keystoreServiceMock;

    private static TestCacheService testCacheService = new TestCacheService();

    private static TestOperationsService testEndpointService = new TestOperationsService();

    private static TestAkkaService testAkkaService = new TestAkkaService(testEndpointService);

    private static Object sync = new Object();

    private static int testProcessed = 0;
    private static int testFailed = 0;
    private static int requestTime = 0;
    private static int sessionTime = 0;

    private static PrivateKey serverPrivateKey;

    private static PublicKey serverPublicKey;

    private static Random rnd = new Random();

    private static ConcurrentHashMap<Integer, SyncTest<SyncRequest, SyncResponse>> syncTests = new ConcurrentHashMap<>(NUMBER_OF_TESTS);

    public class RequestTrack implements Track {

        private long requestStart;
        private int id;
        private long calculatedProcessTime;

        @Override
        public int newRequest(String requestName) {
            requestStart = System.currentTimeMillis();
            id = rnd.nextInt();
            return id;
        }

        @Override
        public void setProcessTime(int requestId, long time) {

        }

        @Override
        public void closeRequest(int requestId) {
            calculatedProcessTime = System.currentTimeMillis() - requestStart;
            requestTime += calculatedProcessTime;
        }

    }

    public class Statistics implements SessionTrackable {

        private final ConcurrentHashMap<UUID, Long> sessions;

        public Statistics() {
            sessions = new ConcurrentHashMap<>(NUMBER_OF_TESTS);
        }

        @Override
        public Track newSession(UUID uuid) {
            sessions.put(uuid, new Long(System.currentTimeMillis()));
            return new RequestTrack();
        }

        @Override
        public void closeSession(UUID uuid) {
            sessionTime += System.currentTimeMillis() - sessions.remove(uuid).longValue();
        }

    }

    public class SyncTest<T,R> {
        public int id;
        public T requestSent;
        public T requestReceived;
        public R responseSent;
        public R responseReceived;

        public SyncTest(int id, T requestSent) {
            this.requestSent = requestSent;
            this.id = id;
        }

        public boolean test() {
            if(!requestSent.equals(requestReceived)) {
                logger.trace("SyncTest: HTTP sync request sent not equal to HTTP sync request recived");
                return false;
            }
            if (!responseSent.equals(responseReceived)) {
                logger.trace("SyncTest: HTTP sync response sent not equal to HTTP sync response recived");
                return false;
            }
            return true;
        }
    }

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        executor = Executors.newCachedThreadPool();
        config = new OperationsServerConfig();
        HttpChannelConfig httpChannelConfig = new HttpChannelConfig();

        httpChannelConfig.setChannelEnabled(true);
        httpChannelConfig.setBindInterface("localhost");
        httpChannelConfig.setPort(bindPort);
        httpChannelConfig.setClientMaxBodySize(MAX_HTTP_REQUEST_SIZE);
        httpChannelConfig.setExecutorThreadSize(3);
        httpChannelConfig.setServerInitializerClass("org.kaaproject.kaa.server.operations.service.http.TestEndPointServerInitializer");

        List<String> commands = new Vector<>();
        commands.add("org.kaaproject.kaa.server.operations.service.http.commands.SyncCommand");
        commands.add("org.kaaproject.kaa.server.operations.service.http.commands.LongSyncCommand");

        httpChannelConfig.setCommandList(commands);


        List<ServiceChannelConfig> channelList = new ArrayList<>();
        channelList.add(httpChannelConfig);
        config.setChannelList(channelList);

        config.setAkkaService(testAkkaService);
        KeyPairGenerator serverKeyGen;
        try {
            serverKeyGen = KeyPairGenerator.getInstance("RSA");
            serverKeyGen.initialize(2048);
            KeyPair servertKeyPair = serverKeyGen.genKeyPair();
            serverPrivateKey = servertKeyPair.getPrivate();
            serverPublicKey = servertKeyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            fail(e.toString());
        }

        bootstrapServiceMock = mock(DefaultOperationsBootstrapService.class);
        config.setOperationsBootstrapService(bootstrapServiceMock);

        keystoreServiceMock = mock(FileKeyStoreService.class);
        when(bootstrapServiceMock.getKeyStoreService()).thenReturn(keystoreServiceMock);
        when(keystoreServiceMock.getPublicKey()).thenReturn(serverPublicKey);
        when(keystoreServiceMock.getPrivateKey()).thenReturn(serverPrivateKey);
        when(bootstrapServiceMock.getCacheService()).thenReturn(testCacheService);
        when(bootstrapServiceMock.getOperationsService()).thenReturn(testEndpointService);

        testAkkaService.start();
    }

    /**
     * After.
     *
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void after() throws Exception {
        executor.shutdown();
        testAkkaService.shutdown();
    }

    /**
     * Before test.
     *
     * @throws Exception
     */
    @Before
    public void beforeTest() throws Exception {
        testProcessed = 0;
        testFailed = 0;
        requestTime = 0;
        sessionTime = 0;

        HttpChannelConfig httpChannelConfig = (HttpChannelConfig) config.getChannelList().get(0);

        httpChannelConfig.setSessionTrack(new Statistics());
        httpChannelConfig.setOperationServerConfig(config);
        netty = new NettyHttpServer(httpChannelConfig);
        netty.init();
        netty.start();
        assertNotNull(netty);
        Thread.sleep(500);
    }

    /**
     * After test.
     *
     * @throws Exception the exception
     */
    @After
    public void afterTest() throws Exception {
        if (testProcessed > 0) {
            logger.info("Average session time "+sessionTime/testProcessed);
            logger.info("Average request time "+requestTime/testProcessed);
        }
        netty.shutdown();
        try {
            netty.join();
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    @Test
    public void TestSync() {
        logger.info("Test Sync request");

        for(int i=0; i< NUMBER_OF_TESTS; i++) {
            sendSyncTest();
        }

        try {
            synchronized (sync) {
                sync.wait(MAX_TEST_TIMEOUT);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(Integer id : syncTests.keySet()) {
            if (!syncTests.get(id).test()) {
                testFailed++;
                logger.trace("SYnc Test "+id+" failed.");
            }
        }

        assertEquals(0, testFailed);
    }

    public static void SyncTestSetRequestReceived(int id, SyncRequest requestReceived) {
        if(syncTests.containsKey(Integer.valueOf(id))) {
            syncTests.get(Integer.valueOf(id)).requestReceived = requestReceived;
        }
    }

    public static void SyncTestSetResponseSent(int id, SyncResponse responseSent) {
        if(syncTests.containsKey(Integer.valueOf(id))) {
            syncTests.get(Integer.valueOf(id)).responseSent = responseSent;
        }
    }

    private void sendSyncTest() {
        try {
            final HttpTestSyncClient cl1 = new HttpTestSyncClient(serverPublicKey, new HttpActivity<SyncResponse>() {

                @Override
                public void httpRequestComplete(Exception ioe,
                        int id,
                        SyncResponse response) {

                    assertNull(ioe);

                    if (syncTests.containsKey(Integer.valueOf(id))) {
                        SyncTest<SyncRequest, SyncResponse> test = syncTests.get(Integer.valueOf(id));
                        test.responseReceived = response;
                        testProcessed++;
                    } else {
                        logger.trace("Test failed: not found in HashMap id: "+id);
                    }


                    if (testProcessed >= NUMBER_OF_TESTS) {
                        synchronized (sync) {
                            sync.notify();
                        }
                    }
                }
            });
            testCacheService.addPublicKey(cl1.getClientPublicKeyHash(), cl1.getClientPublicKey());
            syncTests.putIfAbsent(Integer.valueOf(cl1.getId()), new SyncTest<SyncRequest, SyncResponse>(cl1.getId(), cl1.getRequest()));
            executor.execute(cl1);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
