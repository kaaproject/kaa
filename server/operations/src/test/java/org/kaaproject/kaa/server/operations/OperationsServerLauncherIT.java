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

package org.kaaproject.kaa.server.operations;

import java.util.concurrent.TimeoutException;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.operations.OperationsServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationsServerLauncherIT {
    private static final int STARTUP_TIMEOUT = 30000;

    private static final int SHUTDOWN_TIMEOUT = 30000;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(OperationsServerLauncherIT.class);

    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 10091;

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    /**
     * After.
     *
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        MongoDBTestRunner.tearDown();
    }

    /**
     * Test start operations server application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartOperationsServerWithoutZKApplication() throws Exception {
        TTransport transport = null;
        Thread operationsServerLauncherThread = null;
        OperationsThriftService.Client client = null;
        try {

            operationsServerLauncherThread = new Thread(new Runnable() {
                @SuppressWarnings("static-access")
                @Override
                public void run() {
                    logger.info("Starting Operations Server ...");
                    OperationsServerApplication.main(new String[]{"common-test-context.xml"});
                    logger.info("Operations Server Stopped");
                }
            });

            operationsServerLauncherThread.start();

            Thread.sleep(STARTUP_TIMEOUT);

            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new OperationsThriftService.Client(protocol);
            transport.open();
            client.shutdown();
        }
        finally {
            if (transport != null && transport.isOpen()) {
                try {
                    transport.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (operationsServerLauncherThread != null) {
                operationsServerLauncherThread.join(SHUTDOWN_TIMEOUT);
                if (operationsServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (10 sec) occured while waiting operations server shutdown thread!");
                }
            }
        }
    }

    /**
     * Test start operations server application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartOperationsServerWithZKApplication() throws Exception {
        TestingCluster zkCluster = null;
        TTransport transport = null;
        Thread operationsServerLauncherThread = null;
        OperationsThriftService.Client client = null;
        try {
            zkCluster = new TestingCluster(new InstanceSpec(null, 2185, -1, -1, true, -1, -1, -1));
            zkCluster.start();

            operationsServerLauncherThread = new Thread(new Runnable() {
                @SuppressWarnings("static-access")
                @Override
                public void run() {
                    logger.info("Starting Operations Server ...");
                    OperationsServerApplication.main(new String[]{"common-zk-test-context.xml"});
                    logger.info("Operations Server Stopped");
                }
            });

            operationsServerLauncherThread.start();

            Thread.sleep(STARTUP_TIMEOUT);

            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new OperationsThriftService.Client(protocol);
            transport.open();
            client.shutdown();
        }
        finally {
            if (transport != null && transport.isOpen()) {
                try {
                    transport.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (operationsServerLauncherThread != null) {
                operationsServerLauncherThread.join(SHUTDOWN_TIMEOUT);
                if (operationsServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (10 sec) occured while waiting endpoint server shutdown thread!");
                }
            }
            logger.info("ZKCluster Stop started");
            if (zkCluster != null) {
                logger.info("Closing zk cluster");
                zkCluster.close();
            }
            logger.info("ZKCluster Stop ended");
        }
    }

}
