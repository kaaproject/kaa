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

package org.kaaproject.kaa.server.control;

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
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.control.ControlServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControlServerLauncherIT.
 */
public class ControlServerLauncherIT {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerLauncherIT.class);
    
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
     * Test start control server application.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartControlServerApplication() throws Exception {
        TestingCluster zkCluster = null;
        TTransport transport = null;
        Thread controlServerLauncherThread = null;
        ControlThriftService.Client client = null;
        try {
            zkCluster = new TestingCluster(new InstanceSpec(null, 2185, -1, -1, true, -1, -1, -1));
            zkCluster.start();
            
            controlServerLauncherThread = new Thread(new Runnable() {
                @SuppressWarnings("static-access")
                @Override
                public void run() {
                    logger.info("Starting Control Server ...");
                    new ControlServerApplication().main(new String[]{"common-test-context.xml"});
                    logger.info("Control Server Stopped");
                }
            });
            
            controlServerLauncherThread.start();
            
            Thread.sleep(5000);
            
            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new ControlThriftService.Client(protocol);
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
            if (controlServerLauncherThread != null) {
                controlServerLauncherThread.join(10000);
                if (controlServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (10 sec) occured while waiting control server shutdown thread!");
                }
            }
            if (zkCluster != null) {
                zkCluster.close();
            }
        }
    }
    
    /**
     * Test start control server application without Zk started.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStartControlServerApplicationWithoutZkStarted() throws Exception {
        TTransport transport = null;
        Thread controlServerLauncherThread = null;
        ControlThriftService.Client client = null;
        try {
            controlServerLauncherThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    logger.info("Starting Control Server ...");
                    ControlServerApplication.main(new String[]{"common-test-context.xml"});
                    logger.info("Control Server Stopped");
                }
            });
            
            controlServerLauncherThread.start();
            
            Thread.sleep(20000);
            
            transport = new TSocket(HOST, PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new ControlThriftService.Client(protocol);
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
            if (controlServerLauncherThread != null) {
                controlServerLauncherThread.join(10000);
                if (controlServerLauncherThread.isAlive()) {
                    throw new TimeoutException("Timeout (10 sec) occured while waiting control server shutdown thread!");
                }
            }
        }
    }
    
}
