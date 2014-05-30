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

package org.kaaproject.kaa.server.bootstrap.service.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.http.server.NettyHttpServer;


public class ProtocolServiceTest {

    private static NettyHttpServer serverMock;
    private static BootstrapConfig config;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        config = new BootstrapConfig();
        serverMock = mock(NettyHttpServer.class);
        when(serverMock.getConf()).thenReturn(config);
    }

    @Test
    public void testProtocolService() {
        ProtocolService ps = new ProtocolService(config);
        ps.setNetty(serverMock);
        assertNotNull(ps);
    }

    @Test
    public void testStart() {
        ProtocolService ps = new ProtocolService(config);
        ps.setNetty(serverMock);
        ps.start();
        verify(serverMock, times(1)).init();
        verify(serverMock, times(1)).start();
    }

    @Test
    public void testStop() {
        ProtocolService ps = new ProtocolService(config);
        ps.setNetty(serverMock);
        ps.stop();
        verify(serverMock, times(1)).shutdown();
        verify(serverMock, times(1)).deInit();
    }

}
