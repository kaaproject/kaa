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

package org.kaaproject.kaa.server.node;

import java.util.concurrent.TimeoutException;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.node.KaaNodeThriftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class KaaNodeServerLauncherIT.
 */
public class KaaNodeServerLauncherIT {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(KaaNodeServerLauncherIT.class);
    
    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 10090;
    
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
     * Test start kaa node server application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartKaaNodeServerApplication() throws Exception {
        TestingCluster zkCluster = null;
        TTransport transport = null;
        Thread kaaNodeServerLauncherThread = null;
        KaaNodeThriftService.Client client = null;
        try {
            zkCluster = new TestingCluster(new InstanceSpec(null, 2185, -1, -1, true, -1, -1, -1));
            zkCluster.start();
            
            kaaNodeServerLauncherThread = new Thread(new Runnable() {
                @SuppressWarnings("static-access")
                @Override
                public void run() {
                    LOG.info("Starting Kaa Node Server ...");
                    new KaaNodeApplication(new String[]{}, new String[]{}).main(new String[]{"common-test-context.xml", "kaa-node-test.properties"});
                    LOG.info("Kaa Node Server Stopped");
                }
            });
            
            kaaNodeServerLauncherThread.start();
            
            Thread.sleep(15000);

            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, KaaThriftService.KAA_NODE_SERVICE.getServiceName());
            client = new KaaNodeThriftService.Client(mp);
            transport.open();
            client.shutdown();
            
        }
        finally {
            if (transport != null && transport.isOpen()) {
                try {
                    transport.close();
                }
                catch (Exception e){}
            }
            if (kaaNodeServerLauncherThread != null) {
                kaaNodeServerLauncherThread.join(30000);
                if (kaaNodeServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (30 sec) occured while waiting kaa node server shutdown thread!");
                }
            }
            if (zkCluster != null) {
                zkCluster.close();
            }
        }
    }

    /**
     * Test start kaa node server application without Zk started.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartKaaNodeServerApplicationWithoutZkStarted() throws Exception {
        TTransport transport = null;
        Thread kaaNodeServerLauncherThread = null;
        KaaNodeThriftService.Client client = null;
        try {
            kaaNodeServerLauncherThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    LOG.info("Starting Kaa Node Server ...");
                    KaaNodeApplication.main(new String[]{"common-zk-test-context.xml", "kaa-node-zk-test.properties"});
                    LOG.info("Kaa Node Server Stopped");
                }
            });
            
            kaaNodeServerLauncherThread.start();
            
            Thread.sleep(30000);
            
            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, KaaThriftService.KAA_NODE_SERVICE.getServiceName());
            client = new KaaNodeThriftService.Client(mp);
            transport.open();
            client.shutdown();
        }
        finally {
            if (transport != null && transport.isOpen()) {
                try {
                    transport.close();
                }
                catch (Exception e){}
            }
            if (kaaNodeServerLauncherThread != null) {
                kaaNodeServerLauncherThread.join(30000);
                if (kaaNodeServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (30 sec) occured while waiting kaa node server shutdown thread!");
                }
            }
        }
    }

}
