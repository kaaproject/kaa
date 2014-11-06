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

package org.kaaproject.kaa.client.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.DefaultChannelManager;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.mockito.Mockito;

public class DefaultChannelManagerTest {

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.UP);
        SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.DOWN);
    }

    @Test(expected=ChannelRuntimeException.class)
    public void testNullBootstrapServer() {
        new DefaultChannelManager(Mockito.mock(BootstrapManager.class), null);
    }

    @Test(expected=ChannelRuntimeException.class)
    public void testEmptyBootstrapServer() {
        new DefaultChannelManager(Mockito.mock(BootstrapManager.class), new HashMap<ChannelType, List<ServerInfo>>());
    }

    @Test(expected=ChannelRuntimeException.class)
    public void testEmptyBootstrapManager() {
        new DefaultChannelManager(null, null);
    }

    @Test
    public void testAddHttpLpChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.HTTP_LP);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);
        channelManager.addChannel(channel);

        ServerInfo opServer = new HttpLongPollServerInfo(
                ServerType.OPERATIONS, "localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer);

        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertEquals(channel, channelManager.getChannel("mock_channel"));
        assertEquals(channel, channelManager.getChannels().get(0));
        assertEquals(channel, channelManager.getChannelsByType(ChannelType.HTTP_LP).get(0));
        channelManager.removeChannel(channel);
        assertNull(channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertNull(channelManager.getChannel("mock_channel"));
        assertTrue(channelManager.getChannels().isEmpty());
        assertTrue(channelManager.getChannelsByType(ChannelType.HTTP_LP).isEmpty());

        channelManager.addChannel(channel);
        Mockito.verify(channel, Mockito.times(2)).setServer(opServer);
        channelManager.clearChannelList();
        assertTrue(channelManager.getChannels().isEmpty());
    }

    @Test
    public void testAddBootstrapChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        ServerInfo server = new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(server));

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.HTTP);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);

        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertEquals(channel, channelManager.getChannel("mock_channel"));
        assertEquals(channel, channelManager.getChannels().get(0));
        assertEquals(channel, channelManager.getChannelsByType(ChannelType.HTTP).get(0));
        channelManager.removeChannel(channel);
        assertNull(channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertNull(channelManager.getChannel("mock_channel"));
        assertTrue(channelManager.getChannels().isEmpty());
        assertTrue(channelManager.getChannelsByType(ChannelType.BOOTSTRAP).isEmpty());

        channelManager.addChannel(channel);
        Mockito.verify(channel, Mockito.times(2)).setServer(server);
    }

    @Test
    public void testOperationServerFailed() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.HTTP_LP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);

        ServerInfo opServer = new HttpLongPollServerInfo(ServerType.OPERATIONS, "localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer);

        channelManager.onServerFailed(opServer);
        Mockito.verify(bootstrapManager, Mockito.times(1)).useNextOperationsServer(ChannelType.HTTP_LP);
    }

    @Test
    public void testBootstrapServerFailed() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())
              , (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost2", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.HTTP);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);

        channelManager.onServerFailed(bootststrapServers.get(ChannelType.HTTP).get(0));
        Mockito.verify(channel, Mockito.times(1)).setServer(bootststrapServers.get(ChannelType.HTTP).get(1));
    }

    @Test
    public void testRemoveHttpLpChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        Map<TransportType, ChannelDirection> typesForChannel2 = new HashMap<>(SUPPORTED_TYPES);
        typesForChannel2.remove(TransportType.USER);
        KaaDataChannel channel1 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel1.getSupportedTransportTypes()).thenReturn(typesForChannel2);
        Mockito.when(channel1.getType()).thenReturn(ChannelType.HTTP_LP);
        Mockito.when(channel1.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel1.getId()).thenReturn("mock_channel");

        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel2.getType()).thenReturn(ChannelType.HTTP);
        Mockito.when(channel2.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel2.getId()).thenReturn("mock_channel2");

        KaaDataChannel channel3 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel3.getSupportedTransportTypes()).thenReturn(typesForChannel2);
        Mockito.when(channel3.getType()).thenReturn(ChannelType.KAATCP);
        Mockito.when(channel3.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel3.getId()).thenReturn("mock_tcp_channel3");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel1);
        channelManager.addChannel(channel2);

        ServerInfo opServer = new HttpLongPollServerInfo(ServerType.OPERATIONS, "localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer);

        ServerInfo opServer2 = new HttpServerInfo(ServerType.OPERATIONS, "localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer2);


        Mockito.verify(channel1, Mockito.times(1)).setServer(opServer);
        Mockito.verify(channel2, Mockito.times(1)).setServer(opServer2);

        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.PROFILE));

        channelManager.removeChannel(channel2);

        ServerInfo opServer3 = new KaaTcpServerInfo(ServerType.OPERATIONS, "localhost", 9009, KeyUtil.generateKeyPair().getPublic());
        channelManager.addChannel(channel3);
        channelManager.onServerUpdated(opServer3);

        Mockito.verify(channel3, Mockito.times(1)).setServer(opServer3);

        assertEquals(channel3, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertNull(channelManager.getChannelByTransportType(TransportType.USER));
    }

    @Test
    public void testServerInfo() throws Exception {
        AbstractServerInfo serverInfo = new KaaTcpServerInfo(ServerType.OPERATIONS, "localhost", 9909, KeyUtil.generateKeyPair().getPublic());
        assertEquals("URL is not empty for TCP server info " + serverInfo.toString(), "", serverInfo.getURL());
        serverInfo = new KaaTcpServerInfo(ServerType.OPERATIONS, "localhost", 9009, KeyUtil.generateKeyPair().getPublic().getEncoded());
        assertEquals("URL is not empty for TCP server info " + serverInfo.toString(), "", serverInfo.getURL());
    }

    @Test
    public void testConnectivityChecker() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);

        ChannelType type = ChannelType.KAATCP;
        KaaDataChannel channel1 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel1.getType()).thenReturn(type);
        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getType()).thenReturn(type);

        channelManager.addChannel(channel1);
        channelManager.addChannel(channel2);

        ConnectivityChecker checker = Mockito.mock(ConnectivityChecker.class);

        channelManager.setConnectivityChecker(checker);

        Mockito.verify(channel1, Mockito.times(1)).setConnectivityChecker(checker);
        Mockito.verify(channel2, Mockito.times(1)).setConnectivityChecker(checker);

        KaaDataChannel channel3 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel3.getType()).thenReturn(type);

        channelManager.addChannel(channel3);
        Mockito.verify(channel3, Mockito.times(1)).setConnectivityChecker(checker);
    }

    @Test
    public void testUpdateForSpecifiedTransport() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        types.put(TransportType.LOGGING, ChannelDirection.UP);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getType()).thenReturn(ChannelType.KAATCP);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getType()).thenReturn(ChannelType.KAATCP);
        Mockito.when(channel2.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel2.getId()).thenReturn("channel2");

        channelManager.addChannel(channel2);
        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.LOGGING));
        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.CONFIGURATION));

        channelManager.setChannel(TransportType.LOGGING, channel);
        channelManager.setChannel(TransportType.LOGGING, null);

        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.LOGGING));
        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.CONFIGURATION));

        channelManager.removeChannel(channel2.getId());
        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.CONFIGURATION));
    }

    @Test(expected=KaaInvalidChannelException.class)
    public void testNegativeUpdateForSpecifiedTransport() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<ChannelType, List<ServerInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(ChannelType.HTTP, Arrays.asList(
                (ServerInfo)new HttpServerInfo(ServerType.BOOTSTRAP, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.DOWN);
        types.put(TransportType.LOGGING, ChannelDirection.UP);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getType()).thenReturn(ChannelType.KAATCP);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);

        channelManager.setChannel(TransportType.CONFIGURATION, channel);
    }

}
