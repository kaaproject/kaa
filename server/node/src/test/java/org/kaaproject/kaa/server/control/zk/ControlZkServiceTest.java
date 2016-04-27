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

package org.kaaproject.kaa.server.control.zk;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The Class ControlZkServiceTest.
 */
public class ControlZkServiceTest {
    
    /**
     * Test control zk service start.
     *
     * @throws Exception the exception
     */
    @Test
    public void testControlZkServiceStart() throws Exception {
        ControlZkService zkService = new ControlZkService();
        KaaNodeServerConfig kaaNodeServerConfig = createKaaNodeServerConfig();
        ReflectionTestUtils.setField(zkService, "kaaNodeServerConfig", kaaNodeServerConfig);
        kaaNodeServerConfig.setZkEnabled(false);
        zkService.start();
        kaaNodeServerConfig.setZkEnabled(true);
        kaaNodeServerConfig.setZkIgnoreErrors(true);
        zkService.start();
    }
    
    /**
     * Test control zk service start with error.
     *
     * @throws Exception the exception
     */
    @Test(expected = RuntimeException.class)
    public void testControlZkServiceStartWithError() throws Exception {
        ControlZkService zkService = new ControlZkService();
        KaaNodeServerConfig kaaNodeServerConfig = createKaaNodeServerConfig();
        ReflectionTestUtils.setField(zkService, "kaaNodeServerConfig", kaaNodeServerConfig);
        kaaNodeServerConfig.setZkIgnoreErrors(false);
        zkService.start();
    }
    
    /**
     * Test control zk service stop.
     *
     * @throws Exception the exception
     */
    @Test
    public void testControlZkServiceStop() throws Exception {
        
        ControlZkService zkService = new ControlZkService();
        KaaNodeServerConfig kaaNodeServerConfig = createKaaNodeServerConfig();
        ReflectionTestUtils.setField(zkService, "kaaNodeServerConfig", kaaNodeServerConfig);
        kaaNodeServerConfig.setZkEnabled(false);
        zkService.stop();
        
        ControlNode controlZKNode = Mockito.mock(ControlNode.class);
        Mockito.doNothing().when(controlZKNode).close();

        kaaNodeServerConfig.setZkEnabled(true);
        ReflectionTestUtils.setField(zkService, "controlZKNode", controlZKNode);
        zkService.stop();
        
        Mockito.doThrow(IOException.class).when(controlZKNode).close();
        zkService.stop();
    }
    
    /**
     * Test control zk service getBootstrapNodes.
     *
     * @throws Exception the exception
     */
    @Test
    public void testControlZkBootstrapNodes() throws Exception {
        ControlZkService zkService = new ControlZkService();
        KaaNodeServerConfig kaaNodeServerConfig = createKaaNodeServerConfig();
        ReflectionTestUtils.setField(zkService, "kaaNodeServerConfig", kaaNodeServerConfig);
        kaaNodeServerConfig.setZkEnabled(false);
        Assert.assertNull(zkService.getCurrentBootstrapNodes());
        
        kaaNodeServerConfig.setZkEnabled(true);
        
        ControlNode controlZKNode = Mockito.mock(ControlNode.class);
        ReflectionTestUtils.setField(zkService, "controlZKNode", controlZKNode);
        
        Assert.assertNotNull(zkService.getCurrentBootstrapNodes());
    }
    
    /**
     * Test control zk service sendEndpointNotification.
     *
     * @throws Exception the exception
     */
    @Test
    public void testControlZkSendEndpointNotification() throws Exception {
        ControlZkService zkService = new ControlZkService();
        KaaNodeServerConfig kaaNodeServerConfig = createKaaNodeServerConfig();
        ReflectionTestUtils.setField(zkService, "kaaNodeServerConfig", kaaNodeServerConfig);
        kaaNodeServerConfig.setZkEnabled(false);
        zkService.sendEndpointNotification(null);
        
        kaaNodeServerConfig.setZkEnabled(true);
        ControlNode controlZKNode = Mockito.mock(ControlNode.class);
        ReflectionTestUtils.setField(zkService, "controlZKNode", controlZKNode);
        
        List<OperationsNodeInfo> endpointNodes = Arrays.asList(
                new OperationsNodeInfo(new ConnectionInfo("host1", 123, null), new LoadInfo(1, 1.0), System.currentTimeMillis(), new ArrayList<TransportMetaData>()));
        
        
        Mockito.when(controlZKNode.getCurrentOperationServerNodes()).thenReturn(endpointNodes);
        
        Notification notification = new Notification();
        zkService.sendEndpointNotification(notification);
        
        Thread.sleep(6000);
        
        ControlZkService zkServiceStubbed = Mockito.spy(zkService);

        OperationsThriftService.Client client = Mockito.mock(OperationsThriftService.Client.class);
        Mockito.doNothing().when(client).onNotification(notification);
        
        zkServiceStubbed.sendEndpointNotification(notification);
        
        Thread.sleep(2000);
    }
    
    /**
     * create Kaa Node Config.
     *
     */
    private KaaNodeServerConfig createKaaNodeServerConfig() {
        KaaNodeServerConfig kaaNodeServerConfig = new KaaNodeServerConfig();
        kaaNodeServerConfig.setZkEnabled(true);
        kaaNodeServerConfig.setThriftHost("localhost");
        kaaNodeServerConfig.setThriftPort(9090);
        kaaNodeServerConfig.setZkHostPortList("localhost:2185");
        kaaNodeServerConfig.setZkMaxRetryTime(3000);
        kaaNodeServerConfig.setZkSleepTime(1000);
        return kaaNodeServerConfig;
    }

}
