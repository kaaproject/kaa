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

import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.control.service.ControlServiceImpl;
import org.kaaproject.kaa.server.control.service.loadmgmt.LoadDistributionService;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ControlServiceTest.
 */
public class ControlServiceTest {

    /**
     * Test control service start.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testControlServiceStart() throws Exception {
        ControlServiceImpl controlService = controlServiceSpy();
        
        TThreadPoolServer server = Mockito.mock(TThreadPoolServer.class);
        Mockito.doNothing().when(server).serve();
        
        Mockito.doReturn(server).when(controlService).createServer(Mockito.any(TServerTransport.class), 
                Mockito.any(ControlThriftService.Processor.class));
        
        controlService.start();
        Assert.assertTrue(controlService.started());
        controlService.start();
        Assert.assertTrue(controlService.started());
    }
    
    /**
     * Test control service start with transport exception.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testControlServiceStartTransportException() throws Exception {
        ControlServiceImpl controlService = controlServiceSpy();
        
        TThreadPoolServer server = Mockito.mock(TThreadPoolServer.class);
        Mockito.doThrow(TTransportException.class).when(server).serve();
        
        Mockito.doReturn(server).when(controlService).createServer(Mockito.any(TServerTransport.class), 
                Mockito.any(ControlThriftService.Processor.class));
        
        controlService.start();
    }
    
    /**
     * Test control service stop.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testControlServiceStop() throws Exception {
        ControlServiceImpl controlService = controlServiceSpy();

        TThreadPoolServer server = Mockito.mock(TThreadPoolServer.class);
        Mockito.doNothing().when(server).serve();
        
        Mockito.doReturn(server).when(controlService).createServer(Mockito.any(TServerTransport.class), 
                Mockito.any(ControlThriftService.Processor.class));
        
        controlService.start();
        Assert.assertTrue(controlService.started());
        controlService.stop();
        Assert.assertFalse(controlService.started());
        controlService.stop();
        Assert.assertFalse(controlService.started());
    }
    
    /**
     * created stubbed control service.
     *
     * @return the control service impl
     * @throws Exception the exception
     */
    private ControlServiceImpl controlServiceSpy() throws Exception {
        
        ControlServiceImpl controlService = Mockito.spy(new ControlServiceImpl());
        
        ReflectionTestUtils.setField(controlService, "host", "localhost");
        ReflectionTestUtils.setField(controlService, "port", 9090);

        ControlZkService zkService = Mockito.mock(ControlZkService.class);
        Mockito.doNothing().when(zkService).start();
        Mockito.doNothing().when(zkService).stop();
        
        LoadDistributionService loadMgmtService = Mockito.mock(LoadDistributionService.class);
        Mockito.doNothing().when(loadMgmtService).start();
        Mockito.doNothing().when(loadMgmtService).shutdown();
        
        ReflectionTestUtils.setField(controlService, "controlZkService", zkService);
        
        ReflectionTestUtils.setField(controlService, "loadMgmtService", loadMgmtService);
        
        ControlThriftService.Iface controlThriftService = Mockito.mock(ControlThriftService.Iface.class);

        ReflectionTestUtils.setField(controlService, "controlService", controlThriftService);
        
        TServerSocket serverSocket = Mockito.mock(TServerSocket.class);
        
        Mockito.doReturn(serverSocket).when(controlService).createServerSocket();
        
        return controlService;
    }
}
