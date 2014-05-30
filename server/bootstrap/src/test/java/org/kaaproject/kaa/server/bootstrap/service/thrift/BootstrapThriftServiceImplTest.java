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

/**
 * 
 */
package org.kaaproject.kaa.server.bootstrap.service.thrift;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.OperationsServer;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class BootstrapThriftServiceImplTest {

    private static BootstrapConfig confMock;
    private static OperationsServerListService endpointListMock;
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        confMock = mock(BootstrapConfig.class);
        endpointListMock = mock(OperationsServerListService.class);
        when(confMock.getOperationsServerListService()).thenReturn(endpointListMock);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#getServerShortName()}.
     */
    @Test
    public void testGetServerShortName() {
        BootstrapThriftServiceImpl b = new BootstrapThriftServiceImpl();
        assertNotNull(b);
        assertEquals("bootstrap", b.getServerShortName());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#initServiceCommands()}.
     */
    @Ignore
    @Test
    public void testInitServiceCommands() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#onOperationsServerListUpdate(java.util.Map)}.
     */
    @Test
    public void testOnEndpointServerListUpdate() {
        BootstrapThriftServiceImpl b = new BootstrapThriftServiceImpl();
        assertNotNull(b);
        b.setConfig(confMock);
        Map<String, OperationsServer> endpointMap = new HashMap<String, OperationsServer>();
        OperationsServer s1 = new OperationsServer(10, ByteBuffer.wrap(new byte[] {1,2,3}));
        endpointMap.put("host1:123", s1);
        OperationsServer s2 = new OperationsServer(10, ByteBuffer.wrap(new byte[] {1,2,3}));
        endpointMap.put("host2:123", s2);
        OperationsServer s3 = new OperationsServer(10, ByteBuffer.wrap(new byte[] {1,2,3}));
        endpointMap.put("host3:123", s3);
        try {
            b.onOperationsServerListUpdate(endpointMap);
            verify(endpointListMock, times(1)).updateList(endpointMap);
        } catch (TException e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#getConfig()}.
     */
    @Test
    public void testGetConfig() {
        BootstrapThriftServiceImpl b = new BootstrapThriftServiceImpl();
        assertNotNull(b);
        b.setConfig(confMock);
        assertEquals(confMock, b.getConfig());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#setConfig(org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig)}.
     */
    @Test
    public void testSetConfig() {
        BootstrapThriftServiceImpl b = new BootstrapThriftServiceImpl();
        assertNotNull(b);
        b.setConfig(confMock);
        assertEquals(confMock, b.getConfig());
    }

}
