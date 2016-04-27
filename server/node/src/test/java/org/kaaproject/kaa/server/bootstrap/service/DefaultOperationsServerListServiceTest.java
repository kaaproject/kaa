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

package org.kaaproject.kaa.server.bootstrap.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolConnectionData;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolVersionId;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Andrew Shvayka
 *
 */
public class DefaultOperationsServerListServiceTest {

    private DefaultOperationsServerListService service;

    @Before
    public void before() {
        service = new DefaultOperationsServerListService();
        BootstrapNode zkNode = Mockito.mock(BootstrapNode.class);
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        nodeInfo.setConnectionInfo(new ConnectionInfo("localhost", 8000, ByteBuffer.wrap(new byte[0])));
        List<TransportMetaData> mdList = new ArrayList<TransportMetaData>();
        mdList.add(new TransportMetaData(1, 42, 42, Collections.singletonList(new VersionConnectionInfoPair(42, ByteBuffer.wrap("test"
                .getBytes())))));
        mdList.add(new TransportMetaData(2, 73, 73, Collections.singletonList(new VersionConnectionInfoPair(73, ByteBuffer.wrap("test"
                .getBytes())))));
        mdList.add(new TransportMetaData(3, 1, 3, Arrays.asList(new VersionConnectionInfoPair(1, ByteBuffer.wrap("test1".getBytes())),
                new VersionConnectionInfoPair(2, ByteBuffer.wrap("test2".getBytes())),
                new VersionConnectionInfoPair(3, ByteBuffer.wrap("test3".getBytes())))));
        nodeInfo.setTransports(mdList);

        Mockito.when(zkNode.getCurrentOperationServerNodes()).thenReturn(Arrays.asList(nodeInfo));
        service.init(zkNode);
    }

    @Test
    public void testFilterOneResult() {
        Set<ProtocolConnectionData> result = service.filter(Collections.singletonList(new ProtocolVersionId(1, 42)));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.iterator().next());
        Assert.assertEquals(1, result.iterator().next().getProtocolId());
        Assert.assertEquals(42, result.iterator().next().getProtocolVersion());
    }

    @Test
    public void testFilterResultDeduplication() {
        Set<ProtocolConnectionData> result = service.filter(Arrays.asList(new ProtocolVersionId(3, 1), new ProtocolVersionId(3, 1)));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.iterator().next());
        Assert.assertEquals(3, result.iterator().next().getProtocolId());
        Assert.assertEquals(1, result.iterator().next().getProtocolVersion());
    }

    @Test
    public void testFilterMultipleResults() {
        Set<ProtocolConnectionData> result = service.filter(Arrays.asList(new ProtocolVersionId(1, 42), new ProtocolVersionId(2, 73)));
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testFilterNoResults() {
        Set<ProtocolConnectionData> result = service.filter(Arrays.asList(new ProtocolVersionId(2, 42), new ProtocolVersionId(1, 73)));
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test(expected = RuntimeException.class)
    public void filterInterrupted() throws InterruptedException {
        DefaultOperationsServerListService.Memorizer memorizer = Mockito.mock(DefaultOperationsServerListService.Memorizer.class);
        Mockito.doThrow(new InterruptedException()).when(memorizer).compute(Mockito.any(Object.class));
        ReflectionTestUtils.setField(service, "cache", memorizer);
        service.filter(Collections.<ProtocolVersionId>emptyList());
    }

    @Test
    public void testOnNodeAddedAndUpdated() {
        OperationsNodeInfo nodeInfo = Mockito.mock(OperationsNodeInfo.class);
        ConnectionInfo connectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(nodeInfo.getConnectionInfo()).thenReturn(connectionInfo);
        Map<String, OperationsNodeInfo> opsMap = Mockito.mock(Map.class);
        ReflectionTestUtils.setField(service, "opsMap", opsMap);
        DefaultOperationsServerListService.Memorizer memorizer = Mockito.mock(DefaultOperationsServerListService.Memorizer.class);
        ReflectionTestUtils.setField(service, "cache", memorizer);
        service.onNodeUpdated(nodeInfo);
        service.onNodeAdded(nodeInfo);
        Mockito.verify(opsMap, Mockito.times(2)).put(Mockito.anyString(), Mockito.eq(nodeInfo));
        Mockito.verify(memorizer, Mockito.times(2)).clear();
    }

    @Test
    public void testOnNodeRemoved() {
        OperationsNodeInfo nodeInfo = Mockito.mock(OperationsNodeInfo.class);
        ConnectionInfo connectionInfo = Mockito.mock(ConnectionInfo.class);
        Mockito.when(nodeInfo.getConnectionInfo()).thenReturn(connectionInfo);
        Map<String, OperationsNodeInfo> opsMap = Mockito.mock(Map.class);
        ReflectionTestUtils.setField(service, "opsMap", opsMap);
        DefaultOperationsServerListService.Memorizer memorizer = Mockito.mock(DefaultOperationsServerListService.Memorizer.class);
        ReflectionTestUtils.setField(service, "cache", memorizer);
        service.onNodeRemoved(nodeInfo);
        Mockito.verify(opsMap, Mockito.only()).remove(Mockito.anyString());
        Mockito.verify(memorizer, Mockito.only()).clear();
    }
}
