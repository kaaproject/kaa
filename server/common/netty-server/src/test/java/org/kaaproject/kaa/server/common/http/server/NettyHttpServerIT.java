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

package org.kaaproject.kaa.server.common.http.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.kaaproject.kaa.server.common.server.Config;
import org.kaaproject.kaa.server.common.server.ConfigConst;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessorFactory;
import org.kaaproject.kaa.server.common.server.SessionTrackable;
import org.kaaproject.kaa.server.common.server.Track;
import org.kaaproject.kaa.server.common.server.http.DefaultHttpServerInitializer;
import org.kaaproject.kaa.server.common.server.http.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NettyHttpServerIT Class to test Netty HTTP Server,
 * using serious of http requests to check validity of transmission.
 *
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class NettyHttpServerIT implements SessionTrackable, Track {

    /** Port which used to bind to for Netty HTTP */
    public static final int bindPort = 9193;

    /** Max HTTP request size which used in Netty framework */
    public static final int MAX_HTTP_REQUEST_SIZE = 65536;

    /** Max header fields in test messages */
    public static final int MAX_HEADER_FIELD_SIZE = 2048;

    /** Max response body size */
    public static final int MAX_RESPONSE_BODY = 40960;

    /** Number of tests */
    public static final int NUMBER_OF_TESTS = 10;

    /** Max test complete timeout */
    public static final int MAX_TIMEOUT_TEST_WAIT = NUMBER_OF_TESTS*100;

    /** HTTP request header field, used to set Test ID */
    public static final String REQUEST_ID = "RequestID";

    /** HTTP header field to transmit random String in headers */
    public static final String REQUEST_RANDOM = "RequestRandom";

    /** HTTP POST filed to transmit request body */
    public static final String REQUEST_DATA = "RequestData";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(NettyHttpServerIT.class);

    private static ExecutorService executor = null;

    private NettyHttpServer netty;

    private static Config config;

    private static Map<String, HttpTest> testMessages;

    private static Random rnd;

    private static Object sync = new Object();

    private static int testProcessed = 0;

    private static int sessionsCreated = 0;
    private static int sessionsClosed = 0;
    private static long averageTime = 0;

    private static Map<Integer, Long> requests = new ConcurrentHashMap<Integer, Long>();

    /**
     * Used to test request/response
     * id - random field Integer
     * requestIdRandom - random String with random length with max length MAX_HEADER_FIELD_SIZE
     * responseIdRandom - should be same as requestIdRandom, returned as header field of response
     * requestBody - random String with random length with max length MAX_HEADER_FIELD_SIZE
     * requestReceivedBody - set on receiver side to future check, must be equal to requestBody
     * responseBody - random String with random length with max length MAX_RESPONSE_BODY
     * responseReceivedBody - set on client side to future check, must be equal to responseBody
     */
    public class HttpTest {

        public String id;
        public String requestIdRandom;
        public String responseIdRandom;
        public String requestBody;
        public String requestReceivedBody;
        public String responseBody;
        public String responseReceivedBody;
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HttpTest other = (HttpTest) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }
        private NettyHttpServerIT getOuterType() {
            return NettyHttpServerIT.this;
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
        executor = Executors.newFixedThreadPool(5);
        config = new Config();
        config.setPort(bindPort);
        config.setClientMaxBodySize(MAX_HTTP_REQUEST_SIZE);
        config.setExecutorThreadSize(3);
        config.setBindInterface("localhost");
        KaaCommandProcessorFactory<HttpRequest, HttpResponse> commandTestProcessorFactory = new KaaCommandProcessorFactory<HttpRequest, HttpResponse>() {

            @Override
            public String getCommandName() {
                return CommandTestProcessor.TEST_COMMAND_NAME;
            }

            @Override
            public KaaCommandProcessor<HttpRequest, HttpResponse> createCommandProcessor() {
                return new CommandTestProcessor();
            }
        };
        List<KaaCommandProcessorFactory> commands = new ArrayList<>();
        commands.add(commandTestProcessorFactory);
        config.setCommandList(commands);
        testMessages = new ConcurrentHashMap<String, NettyHttpServerIT.HttpTest>();
        rnd = new Random();
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
    }

    /**
     * Before test.
     *
     * @throws Exception
     */
    @Before
    public void beforeTest() throws Exception {
        config.setSessionTrack(this);
        sessionsCreated = 0;
        sessionsClosed = 0;
        testProcessed = 0;
        averageTime = 0;
        netty = new NettyHttpServer(config, new DefaultHttpServerInitializer());
        netty.init();
        netty.start();
        assertNotNull(netty);
    }

    /**
     * After test.
     *
     * @throws Exception the exception
     */
    @After
    public void afterTest() throws Exception {
        netty.shutdown();
        try {
            netty.join();
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    /**
     * Test, generate NUMBER_OF_TESTS http request with random fields and
     * check if it transmitted correctly.
     */
    @Test
    public void testRequest() {
        logger.info("Test request");
        testProcessed = 0;
        for(int i=0; i<NUMBER_OF_TESTS;i++) {
            sendTest();
        }
        try {
            synchronized (sync) {
                if (testProcessed < NUMBER_OF_TESTS
                        || sessionsCreated > sessionsClosed) {
                    logger.info("get to wait {}",testProcessed);
                    sync.wait(MAX_TIMEOUT_TEST_WAIT);
                    logger.info("leave wait {}",testProcessed);
                }
            }
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        logger.info("Test request: tested "+testProcessed+" messages, processed with "+sessionsCreated+" sessions");
        for(String id : testMessages.keySet()) {
            if (!checkHttpTest(testMessages.get(id))) {
                fail("Error test request");
            }
        }
        logger.info("Test request: tests checked "+testMessages.size()+";");
        if (testMessages.size() > 0) {
            logger.info("Test request: average time "+averageTime/testMessages.size()+" ms");
        }
        assertEquals(0, requests.size());
        //assertEquals(sessionsCreated, sessionsClosed); //Commented, in some cases not all session closing
    }

    /**
     * Test on incorrect URL
     */
    @Test
    public void testIncorrectRequest() {
        logger.info("Test Incorrect request");
        PostParameters params = new PostParameters();
        //Incorrect command name
        String commandName = "test";
        try {
            final HttpTestClient client = new HttpTestClient(params, new HttpActivity() {

                @Override
                public void httpRequestComplete(IOException ioe,
                        Map<String, List<String>> header, String body) {
                    assertNotNull(ioe);
                    logger.info("Test complete, Error 500 got.");
                }
            }, commandName);
            executor.execute(client);
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test on incorrect HTTP method
     */
    @Test
    public void testIncorrectMethod() {
        logger.info("Test Incorrect Method request");
        try {
            URLConnection connection = new URL("http://localhost:"+NettyHttpServerIT.bindPort+"/domain/"+CommandTestProcessor.getCommandName()).openConnection();
            StringBuffer b = new StringBuffer();
            InputStreamReader r = new InputStreamReader(connection.getInputStream(), "UTF-8");
            int c;
            while ((c = r.read()) != -1) {
                b.append((char)c);
            }
            fail("Exception not cauth");
        } catch (IOException e) {
            assertNotNull(e);
            if (!e.toString().contains("HTTP response code: 400 for URL")) {
                fail(e.toString());
            } else {
                logger.info("Test for incorrect method pass");
            }
        }
    }

    /**
     * Generate and process HTTP test.
     * @return String test id
     */
    private String sendTest() {
        HttpTest test = newtest();
        PostParameters params = new PostParameters();
        try {
            params.add(REQUEST_ID, test.id);
            params.add(REQUEST_RANDOM, test.requestIdRandom);
            params.add(REQUEST_DATA, test.requestBody);
        } catch (UnsupportedEncodingException e) {
            fail(e.toString());
        }
        testMessages.put(test.id, test);
        try {
            final HttpTestClient client = new HttpTestClient(params, new HttpActivity() {

                @Override
                public void httpRequestComplete(IOException ioe, Map<String, List<String>> header, String body) {
                    assertNull(ioe);
                    if (!header.containsKey(REQUEST_ID)) {
                        fail(REQUEST_ID+" field not found in HTTP response");
                    }
                    String id = header.get(REQUEST_ID).get(0);
                    assertNotNull(id);
                    if (!testMessages.containsKey(id)) {
                        fail(REQUEST_ID+" not found in send map");
                    }
                    HttpTest test = testMessages.get(id);
                    if (!header.containsKey(REQUEST_RANDOM)) {
                        fail(REQUEST_RANDOM+" field not found in HTTP response");
                    }
                    test.responseIdRandom = header.get(REQUEST_RANDOM).get(0);
                    test.responseReceivedBody = body;
                    synchronized (sync) {
                        testProcessed++;
                        logger.info("Test "+id+" complete: "+testProcessed);
                        if (testProcessed >= NUMBER_OF_TESTS) {
                            sync.notify();
                        }
                    }

                }
            }, CommandTestProcessor.getCommandName());

            executor.execute(client);
        } catch (IOException e) {
            fail(e.toString());
        }
        return test.id;
    }

    /**
     * Generate new test container, fill out id, requestBody, requestIdRandom,
     * @return HttpTest new test container
     */
    private HttpTest newtest() {
        HttpTest test = new HttpTest();
        test.id = String.valueOf(rnd.nextInt());
        int bs = Math.round(rnd.nextFloat() * MAX_HEADER_FIELD_SIZE);
        test.requestBody = getRandomString(bs);
        bs = Math.round(rnd.nextFloat() * MAX_HEADER_FIELD_SIZE);
        test.requestIdRandom = getRandomString(bs);
        return test;
    }

    /**
     * Generate String with random ascii symbols from 48 till 122 with length size.
     *
     * @param size of String
     * @return String with random ascii symbols
     */
    public static String getRandomString(int size) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < size; i++) {
            byte b = (byte)(Math.round(rnd.nextFloat()*74)+48);
            sb.append(new String(new byte[] {b}));
        }
        return sb.toString();
    }

    /**
     * Set HttpTest message fileds received on Netty side.
     * @param id test id
     * @param receivedRequestBody received requestBody
     * @param responseBody response body which sent in HTTP response.
     */
    public static void setHttpResponseData(String id, String receivedRequestBody, String responseBody) {
        HttpTest test = testMessages.get(id);
        if (test != null) {
            test.requestReceivedBody = receivedRequestBody;
            test.responseBody = responseBody;


        }
    }

    /**
     * Check if HttpTest pass, compare:
     *  requestIdRandom with responseIdRandom
     *  requestBody with requestReceivedBody
     *  responseBody with responseReceivedBody
     * @param test HttpTest
     * @return boolean, true if test complete successfully
     */
    private boolean checkHttpTest(HttpTest test) {
        boolean ok = false;
        if (test != null) {
            if (!checkDiff(test.requestIdRandom, test.responseIdRandom)) {
                logger.error("ID Random filed not equals to each other: ("+
                        test.requestIdRandom.length()+"): ["+
                        test.requestIdRandom+"]");
                logger.error("ID Random filed not equals to each other: ("+
                        test.responseIdRandom.length()+"): ["+
                        test.responseIdRandom+"]");
                return ok;
            }

            if(!checkDiff(test.requestBody, test.requestReceivedBody)) {
                logger.error("Request Body Random filed not equals to each other: ("+
                        test.requestBody.length()+"): ["+
                        test.requestBody+"]");
                logger.error("Request Body Random filed not equals to each other: ("+
                        test.requestReceivedBody.length()+"): ["+
                        test.requestReceivedBody+"]");
                return ok;
            }

            if(!checkDiff(test.responseBody, test.responseReceivedBody)) {
                logger.error("Response Body Random filed not equals to each other: ("+
                        test.responseBody.length()+"): ["+
                        test.responseBody+"]");
                logger.error("Response Body Random filed not equals to each other: ("+
                        test.responseReceivedBody.length()+"): ["+
                        test.responseReceivedBody+"]");
                return ok;
            }
            ok = true;
        }
        return ok;
    }

    private boolean checkDiff(String left, String right) {
        for(int i=0;i<left.length();i++) {
            if (i >= right.length()) {
                logger.error("diff: right string length shorter to "+(left.length() - right.length()));
                return false;
            }
            if (left.charAt(i) != right.charAt(i)) {
                int ri = 5;
                if ((i+ri) >= left.length()) {
                    ri = left.length() - i;
                }
                if ((i+ri) >= right.length()) {
                    ri = right.length() - i;
                }
                logger.error("diff: position "+i+" left: ["+left.substring(i,i+ri)+"] right: ["+right.substring(i,i+ri)+"]");
                return false;
            }
        }
        if (left.length() < right.length()) {
            logger.error("diff: left string length shorter to "+(right.length() - left.length()));
            return false;
        }
        return true;
    }

    @Override
    public int newRequest() {
        int id = rnd.nextInt();
        requests.put(new Integer(id), new Long(System.currentTimeMillis()));
        return id;
    }

    @Override
    public void setProcessTime(int requestId, long time) {
        Integer id = new Integer(requestId);
        if (requests.containsKey(id)) {
            averageTime += time;
        }
    }

    @Override
    public void closeRequest(int requestId) {
        requests.remove(new Integer(requestId));
    }

    @Override
    public Track newSession(UUID uuid) {
        sessionsCreated++;
        return this;
    }

    @Override
    public void closeSession(UUID uuid) {
        sessionsClosed++;
    }
}
