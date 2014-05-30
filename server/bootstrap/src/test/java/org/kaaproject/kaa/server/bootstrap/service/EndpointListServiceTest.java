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

package org.kaaproject.kaa.server.bootstrap.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.OperationsServer;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;

public class EndpointListServiceTest {

    @Test
    public void testEndpointListServiceInitWithNullBootstrapNode() {
        BootstrapConfig bootstrapConfig = mock(BootstrapConfig.class);

        OperationsServerListService endpointListService = new OperationsServerListService(bootstrapConfig);
        endpointListService.init();

        Assert.assertNotNull(endpointListService.getOpsServerList());
        verify(bootstrapConfig, times(1)).getBootstrapNode();
    }

    @Test

    public void testEndpointListServiceDeinitWithBootstrapNode() {
        BootstrapNode bootstrapNode = mock(BootstrapNode.class);
        List<OperationsNodeInfo> endpointNodes = Arrays.asList(
                new OperationsNodeInfo(new ConnectionInfo("host1", 123, "host1", 123, null), 1, 1, 1, System.currentTimeMillis()),
                new OperationsNodeInfo(new ConnectionInfo("host2", 123, "host2", 123, null), 1, 1, 1, System.currentTimeMillis()),
                new OperationsNodeInfo(new ConnectionInfo("host3", 123, "host3", 123, null), 1, 1, 1, System.currentTimeMillis()));
        when(bootstrapNode.getCurrentOperationServerNodes()).thenReturn(endpointNodes);
        BootstrapConfig config = new BootstrapConfig();
        config.setBootstrapNode(bootstrapNode);

        OperationsServerListService endpointListService = new OperationsServerListService(config);
        endpointListService.init();

        endpointListService.deinit();
        
    }

    @Test
    public void testEndpointListServiceUpdateList() {
        BootstrapNode bootstrapNode = mock(BootstrapNode.class);
        List<OperationsNodeInfo> endpointNodes = Arrays.asList(
                new OperationsNodeInfo(new ConnectionInfo("host1", 123, "host1", 123, null), 1, 1, 1, System.currentTimeMillis()),
                new OperationsNodeInfo(new ConnectionInfo("host2", 123, "host2", 123, null), 1, 1, 1, System.currentTimeMillis()),
                new OperationsNodeInfo(new ConnectionInfo("host3", 123, "host3", 123, null), 1, 1, 1, System.currentTimeMillis()));
        when(bootstrapNode.getCurrentOperationServerNodes()).thenReturn(endpointNodes);
        BootstrapConfig config = new BootstrapConfig();
        config.setBootstrapNode(bootstrapNode);

        OperationsServerListService endpointListService = new OperationsServerListService(config);
        endpointListService.init();
        
        Map<String, OperationsServer> endpointMap = new HashMap<>();
        OperationsServer s1 = new OperationsServer(10, ByteBuffer.wrap(new byte[] {1,2,3}));
        endpointMap.put("host1:123", s1);
        OperationsServer s2 = new OperationsServer(10, ByteBuffer.wrap(new byte[] {1,2,3}));
        endpointMap.put("host2:123", s2);
        OperationsServer s3 = new OperationsServer(10, ByteBuffer.wrap(new byte[] {1,2,3}));
        endpointMap.put("host3:123", s3);
        endpointListService.updateList(endpointMap);
        
        OperationsServerList list = endpointListService.getOpsServerList();
        assertNotNull(list);
        assertNotNull(list.getOperationsServerArray());
        assertEquals(3, list.getOperationsServerArray().size());
    }
    
}
