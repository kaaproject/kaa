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

package org.kaaproject.kaa.server.node.service.initialization;

import static org.mockito.Mockito.mock;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.node.KaaNodeThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class KaaNodeInitializationServiceTest {

    private InitializationService controlInitializationService;
    private InitializationService bootstrapInitializationService;
    private InitializationService operationsInitializationService;

    /**
     * Test kaa node initialization service start.
     *
     * @throws Exception the exception
     */
    @Test
    public void testKaaNodeInitializationServiceStart() throws Exception {
        KaaNodeInitializationService kaaNodeInitializationService = kaaNodeInitializationServiceSpy();
        
        TThreadPoolServer server = Mockito.mock(TThreadPoolServer.class);
        Mockito.doNothing().when(server).serve();
        
        Mockito.doReturn(server).when(kaaNodeInitializationService).createServer(Mockito.any(TServerTransport.class), 
                Mockito.any(TMultiplexedProcessor.class));
        
        kaaNodeInitializationService.start();
        
        Mockito.verify(controlInitializationService).start();
        Mockito.verify(bootstrapInitializationService).start();
        Mockito.verify(operationsInitializationService).start();
    }
    
    /**
     * Test kaa node initialization service start with transport exception.
     *
     * @throws Exception the exception
     */
    @Test
    public void testKaaNodeInitializationServiceStartTransportException() throws Exception {
        KaaNodeInitializationService kaaNodeInitializationService = kaaNodeInitializationServiceSpy();
        
        TThreadPoolServer server = Mockito.mock(TThreadPoolServer.class);
        Mockito.doThrow(TTransportException.class).when(server).serve();
        
        Mockito.doReturn(server).when(kaaNodeInitializationService).createServer(Mockito.any(TServerTransport.class), 
                Mockito.any(TMultiplexedProcessor.class));
        
        kaaNodeInitializationService.start();
        
        Mockito.verify(controlInitializationService).start();
        Mockito.verify(bootstrapInitializationService).start();
        Mockito.verify(operationsInitializationService).start();
    }
    
    /**
     * Test kaa node initialization service stop.
     *
     * @throws Exception the exception
     */
    @Test
    public void testKaaNodeInitializationServiceStop() throws Exception {
        KaaNodeInitializationService kaaNodeInitializationService = kaaNodeInitializationServiceSpy();

        TThreadPoolServer server = Mockito.mock(TThreadPoolServer.class);
        Mockito.doNothing().when(server).serve();
        
        Mockito.doReturn(server).when(kaaNodeInitializationService).createServer(Mockito.any(TServerTransport.class), 
                Mockito.any(TMultiplexedProcessor.class));
        
        kaaNodeInitializationService.start();
        kaaNodeInitializationService.stop();
        
        Mockito.verify(controlInitializationService).start();
        Mockito.verify(bootstrapInitializationService).start();
        Mockito.verify(operationsInitializationService).start();

        Mockito.verify(controlInitializationService).stop();
        Mockito.verify(bootstrapInitializationService).stop();
        Mockito.verify(operationsInitializationService).stop();

    }
    
    
    /**
     * created stubbed kaa node initialization service.
     *
     * @return the KaaNodeInitializationService
     * @throws Exception the exception
     */
    private KaaNodeInitializationService kaaNodeInitializationServiceSpy() throws Exception {
        
        KaaNodeInitializationService kaaNodeInitializationService = Mockito.spy(new KaaNodeInitializationService());
        
        KaaNodeServerConfig kaaNodeServerConfig = new KaaNodeServerConfig();
        kaaNodeServerConfig.setThriftHost("localhost");
        kaaNodeServerConfig.setThriftPort(10090);        
        kaaNodeServerConfig.setControlServerEnabled(true);
        kaaNodeServerConfig.setBootstrapServerEnabled(true);
        kaaNodeServerConfig.setOperationsServerEnabled(true);
        
        ReflectionTestUtils.setField(kaaNodeInitializationService, "kaaNodeServerConfig", kaaNodeServerConfig);
        
        KaaNodeThriftService.Iface kaaNodeThriftService = Mockito.mock(KaaNodeThriftService.Iface.class);
        ReflectionTestUtils.setField(kaaNodeInitializationService, "kaaNodeThriftService", kaaNodeThriftService);
        
        BootstrapThriftService.Iface bootstrapThriftService = Mockito.mock(BootstrapThriftService.Iface.class);
        ReflectionTestUtils.setField(kaaNodeInitializationService, "bootstrapThriftService", bootstrapThriftService);
        
        OperationsThriftService.Iface operationsThriftService = Mockito.mock(OperationsThriftService.Iface.class);
        ReflectionTestUtils.setField(kaaNodeInitializationService, "operationsThriftService", operationsThriftService);
        
        controlInitializationService = mock(InitializationService.class);
        bootstrapInitializationService = mock(InitializationService.class);
        operationsInitializationService = mock(InitializationService.class);
        
        ReflectionTestUtils.setField(kaaNodeInitializationService, "controlInitializationService", controlInitializationService);

        ReflectionTestUtils.setField(kaaNodeInitializationService, "bootstrapInitializationService", bootstrapInitializationService);
        
        ReflectionTestUtils.setField(kaaNodeInitializationService, "operationsInitializationService", operationsInitializationService);
        
        TServerSocket serverSocket = Mockito.mock(TServerSocket.class);
        
        Mockito.doReturn(serverSocket).when(kaaNodeInitializationService).createServerSocket();
        
        return kaaNodeInitializationService;
    }
}
