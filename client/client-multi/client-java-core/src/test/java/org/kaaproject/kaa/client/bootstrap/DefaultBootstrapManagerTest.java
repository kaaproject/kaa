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

package org.kaaproject.kaa.client.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.HttpLongPollServerInfo;
import org.kaaproject.kaa.client.channel.HttpServerInfo;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaInvalidChannelException;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPLPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.mockito.Mockito;

public class DefaultBootstrapManagerTest {

    public class ChanelManagerMock implements KaaChannelManager {

        private boolean serverUpdated = false;
        private String receivedUrl;
        private final boolean isLongPoll;

        public ChanelManagerMock(boolean longPoll) {
            this.isLongPoll = longPoll;
        }

        public String getReceivedUrl() {
            return receivedUrl;
        }

        public boolean isServerUpdated() {
            return serverUpdated;
        }

        @Override
        public void setConnectivityChecker(ConnectivityChecker checker) {

        }

        @Override
        public void addChannel(KaaDataChannel channel) {

        }

        @Override
        public void removeChannel(KaaDataChannel channel) {

        }

        @Override
        public List<KaaDataChannel> getChannels() {
            return null;
        }

        @Override
        public List<KaaDataChannel> getChannelsByType(ChannelType type) {
            return null;
        }

        @Override
        public KaaDataChannel getChannelByTransportType(TransportType type) {
            return null;
        }

        @Override
        public KaaDataChannel getChannel(String id) {
            return null;
        }

        @Override
        public void onServerFailed(ServerInfo server) {

        }

        @Override
        public void onServerUpdated(ServerInfo newServer) {
            if (isLongPoll) {
                assertTrue(newServer instanceof HttpLongPollServerInfo);
                receivedUrl = ((HttpLongPollServerInfo) newServer).getURL();
            } else {
                assertTrue(newServer instanceof HttpServerInfo);
                receivedUrl = ((HttpServerInfo) newServer).getURL();
            }
            serverUpdated = true;
        }

        @Override
        public void clearChannelList() {

        }

        @Override
        public void setChannel(TransportType transport, KaaDataChannel channel)
                throws KaaInvalidChannelException {

        }

        @Override
        public void removeChannel(String id) {

        }
    }

    @Test
    public void testReceiveOperationsServerList() throws TransportException {
        BootstrapTransport transport = mock(BootstrapTransport.class);
        DefaultBootstrapManager manager = new DefaultBootstrapManager(transport);

        OperationsServerList serverList = new OperationsServerList();

        boolean exception = false;
        try {
            manager.receiveOperationsServerList();
            manager.useNextOperationsServer(ChannelType.HTTP_LP);
        } catch (BootstrapRuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        LinkedList<OperationsServer> list = new LinkedList<OperationsServer>();
        list.add(new OperationsServer());
        list.add(new OperationsServer());
        serverList.setOperationsServerArray(list);

        manager.receiveOperationsServerList();

        verify(transport, times(2)).sync();
    }

    @Test
    public void testOperationsServerInfoRetrieving() throws TransportException, NoSuchAlgorithmException, InvalidKeySpecException {
        DefaultBootstrapManager manager = new DefaultBootstrapManager(null);

        boolean exception = false;
        try {
            manager.useNextOperationsServer(ChannelType.HTTP_LP);
        } catch (BootstrapRuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        BootstrapTransport transport = mock(BootstrapTransport.class);

        // Generating pseudo bootstrap key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();

        OperationsServerList serverList = new OperationsServerList();
        OperationsServer server = new OperationsServer();
        server.setName("localhost:9889");
        server.setPublicKey(ByteBuffer.wrap(keyPair.getPublic().getEncoded()));
        List<SupportedChannel> channels = new LinkedList<>();
        server.setSupportedChannelsArray(channels);
        channels.add(new SupportedChannel(ChannelType.HTTP_LP, new HTTPLPComunicationParameters("localhost", 9889)));
        LinkedList<OperationsServer> list = new LinkedList<OperationsServer>();
        list.add(server);
        serverList.setOperationsServerArray(list);

        ChanelManagerMock channelManager = spy(new ChanelManagerMock(true));

        manager.setChannelManager(channelManager);
        manager.setTransport(transport);
        manager.onServerListUpdated(serverList);
        manager.useNextOperationsServer(ChannelType.HTTP_LP);
        assertTrue(channelManager.isServerUpdated());
        assertEquals("http://localhost:9889/EP/LongSync", channelManager.getReceivedUrl());

        manager.useNextOperationsServerByDnsName(null);
        manager.useNextOperationsServerByDnsName("some.name");
        verify(channelManager, times(1)).onServerUpdated(any(ServerInfo.class));
    }

    @Test
    public void testUseServerByDnsName() throws NoSuchAlgorithmException {
        DefaultBootstrapManager manager = new DefaultBootstrapManager(null);

        ChanelManagerMock channelManager = spy(new ChanelManagerMock(false));
        manager.setChannelManager(channelManager);

        BootstrapTransport transport = mock(BootstrapTransport.class);
        manager.setTransport(transport);

        // Generating pseudo operation key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();

        OperationsServerList serverList = new OperationsServerList();
        OperationsServer server1 = new OperationsServer();
        server1.setName("localhost:9889");
        server1.setPublicKey(ByteBuffer.wrap(keyPair.getPublic().getEncoded()));
        List<SupportedChannel> channels1 = new LinkedList<>();
        server1.setSupportedChannelsArray(channels1);
        channels1.add(new SupportedChannel(ChannelType.HTTP, new HTTPComunicationParameters("localhost", 9889)));

        LinkedList<OperationsServer> list = new LinkedList<OperationsServer>();
        serverList.setOperationsServerArray(list);
        list.add(server1);

        manager.onServerListUpdated(serverList);
        assertEquals("http://localhost:9889/EP/Sync", channelManager.getReceivedUrl());

        manager.useNextOperationsServerByDnsName("localhost2:9889");
        Mockito.verify(transport, Mockito.times(1)).sync();

        OperationsServer server2 = new OperationsServer();
        server2.setName("localhost2:9889");
        server2.setPublicKey(ByteBuffer.wrap(keyPair.getPublic().getEncoded()));
        List<SupportedChannel> channels2 = new LinkedList<>();
        server2.setSupportedChannelsArray(channels2);
        channels2.add(new SupportedChannel(ChannelType.HTTP, new HTTPComunicationParameters("localhost2", 9889)));

        list.add(server2);

        manager.onServerListUpdated(serverList);
        assertEquals("http://localhost2:9889/EP/Sync", channelManager.getReceivedUrl());
        assertTrue(channelManager.isServerUpdated());
    }

    @Test
    public void testGetOperationsServerList() throws NoSuchAlgorithmException {
        DefaultBootstrapManager manager = new DefaultBootstrapManager(null);
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        manager.setChannelManager(channelManager);
        assertNull(manager.getOperationsServerList());

        // Generating pseudo operation key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();

        OperationsServerList serverList = new OperationsServerList();
        OperationsServer server1 = new OperationsServer();
        server1.setName("localhost:9889");
        server1.setPublicKey(ByteBuffer.wrap(keyPair.getPublic().getEncoded()));
        List<SupportedChannel> channels1 = new LinkedList<>();
        server1.setSupportedChannelsArray(channels1);
        channels1.add(new SupportedChannel(ChannelType.HTTP, new HTTPComunicationParameters("localhost", 9889)));

        LinkedList<OperationsServer> list = new LinkedList<OperationsServer>();
        serverList.setOperationsServerArray(list);
        list.add(server1);

        manager.onServerListUpdated(serverList);

        assertNotNull(manager.getOperationsServerList());
    }

}
