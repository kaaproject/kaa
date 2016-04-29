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

package org.kaaproject.kaa.server.transports.http.transport.netty;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.transport.GenericTransportContext;
import org.kaaproject.kaa.server.transport.TransportContext;
import org.kaaproject.kaa.server.transport.TransportProperties;
import org.kaaproject.kaa.server.transport.http.config.gen.AvroHttpConfig;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.kaaproject.kaa.server.transports.http.transport.HttpTransport;
import org.kaaproject.kaa.server.transports.http.transport.commands.SyncCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyHttpServerIT Class to test Netty HTTP Server, using serious of http
 * requests to check validity of transmission.
 *
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class NettyHttpServerIT {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServerIT.class);

    /** Port which used to bind to for Netty HTTP */
    public static final String TEST_HOST = "localhost";

    /** Port which used to bind to for Netty HTTP */
    public static final int TEST_PORT = 9193;

    /** Max HTTP request size which used in Netty framework */
    public static final int MAX_HTTP_REQUEST_SIZE = 65536;

    private static ExecutorService executor = null;

    private static HttpTransport netty;

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        executor = Executors.newFixedThreadPool(5);
        netty = new HttpTransport();
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
        GenericTransportContext context = new GenericTransportContext(new TransportContext(new TransportProperties(new Properties()), null,
                new MessageHandler() {

                    @Override
                    public void process(SessionInitMessage message) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void process(SessionAware message) {
                        // TODO Auto-generated method stub

                    }
                }), getTestConfig());
        netty.init(context);
        netty.start();
    }

    /**
     * After test.
     *
     * @throws Exception
     *             the exception
     */
    @After
    public void afterTest() throws Exception {
        netty.stop();
    }

    /**
     * Test on incorrect URL
     */
    @Test
    public void testIncorrectRequest() {
        LOG.info("Test Incorrect request");
        PostParameters params = new PostParameters();
        // Incorrect command name
        String commandName = "test";
        try {
            final HttpTestClient client = new HttpTestClient(params, new HttpActivity() {

                @Override
                public void httpRequestComplete(IOException ioe, Map<String, List<String>> header, String body) {
                    assertNotNull(ioe);
                    LOG.info("Test complete, Error 500 got.");
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
        LOG.info("Test Incorrect Method request");
        try {
            URLConnection connection = new URL("http://" + TEST_HOST + ":" + TEST_PORT + "/domain/" + SyncCommand.getCommandName())
                    .openConnection();
            StringBuffer b = new StringBuffer();
            InputStreamReader r = new InputStreamReader(connection.getInputStream(), "UTF-8");
            int c;
            while ((c = r.read()) != -1) {
                b.append((char) c);
            }
            fail("Exception not cauth");
        } catch (IOException e) {
            assertNotNull(e);
            if (!e.toString().contains("HTTP response code: 400 for URL")) {
                fail(e.toString());
            } else {
                LOG.info("Test for incorrect method pass");
            }
        }
    }

    private byte[] getTestConfig() throws IOException {
        AvroHttpConfig config = new AvroHttpConfig();
        config.setBindInterface(TEST_HOST);
        config.setBindPort(TEST_PORT);
        config.setPublicInterface(TEST_HOST);
        config.setPublicPort(TEST_PORT);
        config.setMaxBodySize(MAX_HTTP_REQUEST_SIZE);
        AvroByteArrayConverter<AvroHttpConfig> converter = new AvroByteArrayConverter<AvroHttpConfig>(AvroHttpConfig.class);
        return converter.toByteArray(config);
    }
}
