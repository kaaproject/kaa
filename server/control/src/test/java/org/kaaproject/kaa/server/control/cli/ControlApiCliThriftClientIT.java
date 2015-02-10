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

package org.kaaproject.kaa.server.control.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import jline.console.ConsoleReader;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDataLoader;
import org.kaaproject.kaa.server.control.TestCluster;
import org.kaaproject.kaa.server.control.cli.ControlApiCliThriftClient;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ControlApiCliThriftClientIT {

    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 10090;
    
    /** The control service. */
    @Autowired
    private ControlService controlService;
    
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
        TestCluster.stop();
        MongoDBTestRunner.tearDown();
    }
    
    /**
     * Before test.
     *
     * @throws Exception the exception
     */
    @Before
    public void beforeTest() throws Exception {
        MongoDataLoader.loadData();
        TestCluster.checkStarted(controlService);
    }
    
    /**
     * Test run control api cli client
     * @throws Exception 
     */
    @Test
    public void testRunControlApiCliClient() throws Exception {
        File propertiesFile = File.createTempFile("testApiCliClnt", null);
        Properties props = new Properties();
        props.put("thrift_host", HOST);
        props.put("thrift_port", PORT+"");
        FileOutputStream fos = new FileOutputStream(propertiesFile);
        props.store(fos, "");
        fos.close();
        ConsoleReader reader = Mockito.mock(ConsoleReader.class);
        Mockito.doReturn("disconnect; quit").when(reader).readLine(Mockito.any(String.class));
        ControlApiCliThriftClient cli = Mockito.spy(new ControlApiCliThriftClient());
        Mockito.doReturn(reader).when(cli).createReader();
        int result = cli.process(new String[]{"\\/\\/\\","----"});
        Assert.assertEquals(1, result);
        result = cli.process(new String[]{"-c", "fake"});
        Assert.assertEquals(2, result);
        result = cli.process(new String[]{"-c", propertiesFile.getAbsolutePath()});
        Assert.assertEquals(-1, result);
        TestCluster.stop();
        result = cli.process(new String[]{"-c", propertiesFile.getAbsolutePath()});
        Assert.assertEquals(1, result);
        propertiesFile.delete();
    }
    
    
}
