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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
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
        new DefaultChannelManager(Mockito.mock(BootstrapManager.class), new LinkedList<BootstrapServerInfo>());
    }

    @Test(expected=ChannelRuntimeException.class)
    public void testEmptyBootstrapManager() {
        new DefaultChannelManager(null, null);
    }

    @Test
    public void testAddHttpLpChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<BootstrapServerInfo> bootststrapServers = new LinkedList<>();
        bootststrapServers.add(new BootstrapServerInfo("localhost", 9889, KeyUtil.generateKeyPair().getPublic()));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.HTTP_LP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);
        channelManager.addChannel(channel);

        ServerInfo opServer = new HttpLongPollServerInfo("localhost", 9999, KeyUtil.generateKeyPair().getPublic());
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
        List<BootstrapServerInfo> bootststrapServers = new LinkedList<>();

        BootstrapServerInfo server = new BootstrapServerInfo("localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        bootststrapServers.add(server);
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);

        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertEquals(channel, channelManager.getChannel("mock_channel"));
        assertEquals(channel, channelManager.getChannels().get(0));
        assertEquals(channel, channelManager.getChannelsByType(ChannelType.BOOTSTRAP).get(0));
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
        List<BootstrapServerInfo> bootststrapServers = new LinkedList<>();
        bootststrapServers.add(new BootstrapServerInfo("localhost", 9889, KeyUtil.generateKeyPair().getPublic()));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.HTTP_LP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);

        ServerInfo opServer = new HttpLongPollServerInfo("localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer);

        channelManager.onServerFailed(opServer);
        Mockito.verify(bootstrapManager, Mockito.times(1)).useNextOperationsServer(ChannelType.HTTP_LP);
    }

    @Test
    public void testBootstrapServerFailed() throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<BootstrapServerInfo> bootststrapServers = new LinkedList<>();
        bootststrapServers.add(new BootstrapServerInfo("localhost", 9889, KeyUtil.generateKeyPair().getPublic()));
        bootststrapServers.add(new BootstrapServerInfo("localhost2", 9889, KeyUtil.generateKeyPair().getPublic()));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getType()).thenReturn(ChannelType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel);

        channelManager.onServerFailed(bootststrapServers.get(0));
        Mockito.verify(channel, Mockito.times(1)).setServer(bootststrapServers.get(1));
    }

    @Test
    public void testRemoveHttpLpChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<BootstrapServerInfo> bootststrapServers = new LinkedList<>();
        bootststrapServers.add(new BootstrapServerInfo("localhost", 9889, KeyUtil.generateKeyPair().getPublic()));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        Map<TransportType, ChannelDirection> typesForChannel2 = new HashMap<>(SUPPORTED_TYPES);
        typesForChannel2.remove(TransportType.USER);
        KaaDataChannel channel1 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel1.getSupportedTransportTypes()).thenReturn(typesForChannel2);
        Mockito.when(channel1.getType()).thenReturn(ChannelType.HTTP_LP);
        Mockito.when(channel1.getId()).thenReturn("mock_channel");

        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel2.getType()).thenReturn(ChannelType.HTTP);
        Mockito.when(channel2.getId()).thenReturn("mock_channel2");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers);
        channelManager.addChannel(channel1);
        channelManager.addChannel(channel2);

        ServerInfo opServer = new HttpLongPollServerInfo("localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer);

        ServerInfo opServer2 = new HttpServerInfo("localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        channelManager.onServerUpdated(opServer2);

        Mockito.verify(channel1, Mockito.times(1)).setServer(opServer);
        Mockito.verify(channel2, Mockito.times(1)).setServer(opServer2);

        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.PROFILE));

        channelManager.removeChannel(channel2);
        assertEquals(channel1, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertNull(channelManager.getChannelByTransportType(TransportType.USER));
    }

}
