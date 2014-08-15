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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftChannelType;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftCommunicationParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftIpParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftSupportedChannel;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;

public class EndpointListServiceTest {

    @Test
    public void testEndpointListServiceInitWithNullBootstrapNode() {
        OperationsServerListService endpointListService = new OperationsServerListService();
        endpointListService.init();

        Assert.assertNotNull(endpointListService.getOpsServerList());
    }

    @Test
    public void testEndpointListServiceDeinitWithBootstrapNode() {
        OperationsServerListService endpointListService = new OperationsServerListService();
        endpointListService.init();
        endpointListService.stop();

    }

    @Test
    public void testEndpointListServiceUpdateList() {
        OperationsServerListService endpointListService = new OperationsServerListService();
        endpointListService.init();

        ByteBuffer pk = ByteBuffer.wrap(new byte[] { 1, 2, 3 });

        List<ThriftSupportedChannel> scl1 = new ArrayList<>();
        ThriftIpParameters ip1 = new ThriftIpParameters("host1", 123);
        ThriftCommunicationParameters com11 = new ThriftCommunicationParameters();
        com11.setHttpParams(ip1);
        ThriftSupportedChannel sc11 = new ThriftSupportedChannel(ThriftChannelType.HTTP, com11);
        scl1.add(sc11);
        ThriftOperationsServer s1 = new ThriftOperationsServer("host1", 10, pk, scl1);

        List<ThriftSupportedChannel> scl2 = new ArrayList<>();
        ThriftIpParameters ip2 = new ThriftIpParameters("host2", 123);
        ThriftCommunicationParameters com12 = new ThriftCommunicationParameters();
        com12.setHttpLpParams(ip2);
        ThriftSupportedChannel sc12 = new ThriftSupportedChannel(ThriftChannelType.HTTP_LP, com12);
        scl2.add(sc12);
        ThriftOperationsServer s2 = new ThriftOperationsServer("host2", 10, pk, scl2);

        List<ThriftSupportedChannel> scl3 = new ArrayList<>();
        ThriftIpParameters ip3 = new ThriftIpParameters("host3", 123);
        ThriftCommunicationParameters com13 = new ThriftCommunicationParameters();
        com13.setKaaTcpParams(ip3);
        ThriftSupportedChannel sc13 = new ThriftSupportedChannel(ThriftChannelType.KAATCP, com13);
        scl3.add(sc13);
        ThriftOperationsServer s3 = new ThriftOperationsServer("host3", 10, pk, scl3);

        List<ThriftOperationsServer> operationsServersList = new ArrayList<>();
        operationsServersList.add(s1);
        operationsServersList.add(s2);
        operationsServersList.add(s3);
        endpointListService.updateList(operationsServersList);

        OperationsServerList list = endpointListService.getOpsServerList();
        assertNotNull(list);
        assertNotNull(list.getOperationsServerArray());
        assertEquals(3, list.getOperationsServerArray().size());
    }

}
