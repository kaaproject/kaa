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

package org.kaaproject.kaa.client.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.DefaultChannelManager;
import org.kaaproject.kaa.client.channel.failover.DefaultFailoverManager;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.mockito.Mockito;

public class DefaultChannelManagerTest {

    private static final ExecutorContext CONTEXT = Mockito.mock(ExecutorContext.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.UP);
        SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.DOWN);

        Mockito.when(CONTEXT.getScheduledExecutor()).thenReturn(Executors.newScheduledThreadPool(1));
    }

    @Test(expected = ChannelRuntimeException.class)
    public void testNullBootstrapServer() {
        new DefaultChannelManager(Mockito.mock(BootstrapManager.class), null, null, null);
    }

    @Test(expected = ChannelRuntimeException.class)
    public void testEmptyBootstrapServer() {
        new DefaultChannelManager(Mockito.mock(BootstrapManager.class), new HashMap<TransportProtocolId, List<TransportConnectionInfo>>(), null, null);
    }

    @Test(expected = ChannelRuntimeException.class)
    public void testEmptyBootstrapManager() {
        new DefaultChannelManager(null, null, null, null);
    }

    @Test
    public void testAddHttpLpChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, Collections.singletonList(IPTransportInfoTest.createTestServerInfo(
                ServerType.BOOTSTRAP, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaInternalChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);
        FailoverManager failoverManager = Mockito.spy(new DefaultFailoverManager(channelManager, CONTEXT));
        channelManager.setFailoverManager(failoverManager);
        channelManager.addChannel(channel);
        channelManager.addChannel(channel);

        TransportConnectionInfo server = IPTransportInfoTest.createTestServerInfo(
                ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onTransportConnectionInfoUpdated(server);
        Mockito.verify(failoverManager, Mockito.times(1)).onServerChanged(Mockito.any(TransportConnectionInfo.class));

//        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertEquals(channel, channelManager.getChannel("mock_channel"));
        assertEquals(channel, channelManager.getChannels().get(0));
        channelManager.removeChannel(channel);
//        assertNull(channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertNull(channelManager.getChannel("mock_channel"));
        assertTrue(channelManager.getChannels().isEmpty());

        channelManager.addChannel(channel);
        Mockito.verify(failoverManager, Mockito.times(2)).onServerChanged(Mockito.any(TransportConnectionInfo.class));
        Mockito.verify(channel, Mockito.times(2)).setServer(server);
        channelManager.clearChannelList();
        assertTrue(channelManager.getChannels().isEmpty());
    }

    @Test
    public void testAddBootstrapChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = new HashMap<>();
        TransportConnectionInfo server = IPTransportInfoTest.createTestServerInfo(
                ServerType.BOOTSTRAP, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        bootststrapServers.put(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, Collections.singletonList(server));

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);
        FailoverManager failoverManager = Mockito.spy(new DefaultFailoverManager(channelManager, CONTEXT));
        channelManager.setFailoverManager(failoverManager);

        channelManager.addChannel(channel);

        Mockito.verify(failoverManager, Mockito.times(1)).onServerChanged(Mockito.any(TransportConnectionInfo.class));
//        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertEquals(channel, channelManager.getChannel("mock_channel"));
        assertEquals(channel, channelManager.getChannels().get(0));
        channelManager.removeChannel(channel);
//        assertNull(channelManager.getChannelByTransportType(TransportType.PROFILE));
        assertNull(channelManager.getChannel("mock_channel"));
        assertTrue(channelManager.getChannels().isEmpty());

        channelManager.addChannel(channel);
        Mockito.verify(channel, Mockito.times(2)).setServer(server);
    }

    @Test
    public void testOperationServerFailed() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaInternalChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);
        channelManager.addChannel(channel);

        TransportConnectionInfo opServer = IPTransportInfoTest.createTestServerInfo(
                ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onTransportConnectionInfoUpdated(opServer);

        channelManager.onServerFailed(opServer, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(bootstrapManager, Mockito.times(1))
                .useNextOperationsServer(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, FailoverStatus.NO_CONNECTIVITY);
    }

    @Test
    public void testBootstrapServerFailed() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, Arrays.asList(
                IPTransportInfoTest.createTestServerInfo(
                        ServerType.BOOTSTRAP, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889, KeyUtil.generateKeyPair().getPublic()),
                IPTransportInfoTest.createTestServerInfo(
                        ServerType.BOOTSTRAP, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost2", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        final KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        ExecutorContext context = Mockito.mock(ExecutorContext.class);
        Mockito.when(context.getScheduledExecutor()).thenReturn(Executors.newScheduledThreadPool(1));
        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, context, null);

        FailoverStrategy failoverStrategy = new DefaultFailoverStrategy(1, 1, 1, TimeUnit.MILLISECONDS);
        FailoverManager failoverManager = Mockito.spy(new DefaultFailoverManager(channelManager, CONTEXT, failoverStrategy, 1, TimeUnit.MILLISECONDS));
        channelManager.setFailoverManager(failoverManager);

        channelManager.addChannel(channel);

        Mockito.verify(failoverManager, Mockito.times(1)).onServerChanged(Mockito.any(TransportConnectionInfo.class));

        channelManager.onServerFailed(bootststrapServers.get(TransportProtocolIdConstants.HTTP_TRANSPORT_ID).get(0),
                FailoverStatus.CURRENT_BOOTSTRAP_SERVER_NA);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Mockito.verify(channel, Mockito.timeout(100).times(1)).setServer(bootststrapServers.get(TransportProtocolIdConstants.HTTP_TRANSPORT_ID).get(1));
            }
        });
        Mockito.verify(failoverManager, Mockito.times(1)).onFailover(FailoverStatus.CURRENT_BOOTSTRAP_SERVER_NA);
    }

    @Test
    public void testSingleBootstrapServerFailed() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = new HashMap<>();
        bootststrapServers.put(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, Arrays.asList(
                IPTransportInfoTest.createTestServerInfo(
                        ServerType.BOOTSTRAP, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889, KeyUtil.generateKeyPair().getPublic())));
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel.getServerType()).thenReturn(ServerType.BOOTSTRAP);
        Mockito.when(channel.getId()).thenReturn("mock_channel");

        KaaChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, CONTEXT, null);
        FailoverManager failoverManager = Mockito.spy(new DefaultFailoverManager(channelManager, CONTEXT));
        channelManager.setFailoverManager(failoverManager);

        channelManager.addChannel(channel);

        Mockito.verify(failoverManager, Mockito.times(1)).onServerChanged(Mockito.any(TransportConnectionInfo.class));

        channelManager.onServerFailed(bootststrapServers.get(TransportProtocolIdConstants.HTTP_TRANSPORT_ID).get(0),
                FailoverStatus.CURRENT_BOOTSTRAP_SERVER_NA);
    }

    @Test
    public void testRemoveHttpLpChannel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);

        Map<TransportType, ChannelDirection> typesForChannel2 = new HashMap<>(SUPPORTED_TYPES);
        typesForChannel2.remove(TransportType.USER);
        KaaDataChannel channel1 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel1.getSupportedTransportTypes()).thenReturn(typesForChannel2);
        Mockito.when(channel1.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel1.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel1.getId()).thenReturn("mock_channel");

        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getSupportedTransportTypes()).thenReturn(SUPPORTED_TYPES);
        Mockito.when(channel2.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.HTTP_TRANSPORT_ID);
        Mockito.when(channel2.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel2.getId()).thenReturn("mock_channel2");

        KaaDataChannel channel3 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel3.getSupportedTransportTypes()).thenReturn(typesForChannel2);
        Mockito.when(channel3.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel3.getServerType()).thenReturn(ServerType.OPERATIONS);
        Mockito.when(channel3.getId()).thenReturn("mock_tcp_channel3");


        KaaInternalChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        FailoverManager failoverManager = Mockito.spy(new DefaultFailoverManager(channelManager, CONTEXT));
        channelManager.setFailoverManager(failoverManager);

        channelManager.addChannel(channel1);
        channelManager.addChannel(channel2);

        TransportConnectionInfo opServer = IPTransportInfoTest.createTestServerInfo(
                ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9999, KeyUtil.generateKeyPair().getPublic());
        channelManager.onTransportConnectionInfoUpdated(opServer);

        TransportConnectionInfo opServer2 = IPTransportInfoTest.createTestServerInfo(
                ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889, KeyUtil.generateKeyPair().getPublic());

        channelManager.onTransportConnectionInfoUpdated(opServer2);

        Mockito.verify(channel1, Mockito.times(1)).setServer(opServer);
        Mockito.verify(channel2, Mockito.times(1)).setServer(opServer2);

//        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.PROFILE));

        channelManager.removeChannel(channel2);

        TransportConnectionInfo opServer3 = IPTransportInfoTest.createTestServerInfo(
                ServerType.OPERATIONS, TransportProtocolIdConstants.TCP_TRANSPORT_ID, "localhost", 9009, KeyUtil.generateKeyPair().getPublic());
        channelManager.addChannel(channel3);
        channelManager.onTransportConnectionInfoUpdated(opServer3);

        Mockito.verify(channel3, Mockito.times(1)).setServer(opServer3);

//        assertEquals(channel3, channelManager.getChannelByTransportType(TransportType.PROFILE));
//        assertNull(channelManager.getChannelByTransportType(TransportType.USER));
    }

    @Test
    public void testConnectivityChecker() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        TransportProtocolId type = TransportProtocolIdConstants.TCP_TRANSPORT_ID;
        KaaDataChannel channel1 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel1.getTransportProtocolId()).thenReturn(type);
        Mockito.when(channel1.getId()).thenReturn("Channel1");
        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getTransportProtocolId()).thenReturn(type);
        Mockito.when(channel2.getId()).thenReturn("Channel2");

        channelManager.addChannel(channel1);
        channelManager.addChannel(channel2);

        ConnectivityChecker checker = Mockito.mock(ConnectivityChecker.class);

        channelManager.setConnectivityChecker(checker);

        Mockito.verify(channel1, Mockito.times(1)).setConnectivityChecker(checker);
        Mockito.verify(channel2, Mockito.times(1)).setConnectivityChecker(checker);

        KaaDataChannel channel3 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel3.getTransportProtocolId()).thenReturn(type);
        Mockito.when(channel3.getId()).thenReturn("Channel3");

        channelManager.addChannel(channel3);
        Mockito.verify(channel3, Mockito.times(1)).setConnectivityChecker(checker);
    }

    @Test
    public void testUpdateForSpecifiedTransport() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        types.put(TransportType.LOGGING, ChannelDirection.UP);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        KaaDataChannel channel2 = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel2.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel2.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel2.getId()).thenReturn("channel2");

        channelManager.addChannel(channel2);
//        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.LOGGING));
//        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.CONFIGURATION));

        channelManager.setChannel(TransportType.LOGGING, channel);
        channelManager.setChannel(TransportType.LOGGING, null);

//        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.LOGGING));
//        assertEquals(channel2, channelManager.getChannelByTransportType(TransportType.CONFIGURATION));

        channelManager.removeChannel(channel2.getId());
//        assertEquals(channel, channelManager.getChannelByTransportType(TransportType.CONFIGURATION));
    }

    @Test(expected = KaaInvalidChannelException.class)
    public void testNegativeUpdateForSpecifiedTransport() throws NoSuchAlgorithmException, InvalidKeySpecException,
            KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();
        
        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.DOWN);
        types.put(TransportType.LOGGING, ChannelDirection.UP);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);

        channelManager.setChannel(TransportType.CONFIGURATION, channel);
    }

    @Test
    public void testShutdown() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        types.put(TransportType.LOGGING, ChannelDirection.UP);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        channelManager.addChannel(channel);

        channelManager.shutdown();
        channelManager.onServerFailed(null, FailoverStatus.BOOTSTRAP_SERVERS_NA);
        channelManager.onTransportConnectionInfoUpdated(null);
        channelManager.addChannel(null);
        channelManager.setChannel(null, null);
        channelManager.setConnectivityChecker(null);
        Mockito.verify(channel, Mockito.times(1)).shutdown();
    }

    @Test
    public void testPauseAfterAdd() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        channelManager.addChannel(channel);

        channelManager.pause();
        channelManager.pause();
        Mockito.verify(channel, Mockito.times(1)).pause();
    }

    @Test
    public void testPauseBeforeAdd() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        channelManager.pause();
        channelManager.addChannel(channel);
        Mockito.verify(channel, Mockito.times(1)).pause();
    }

    @Test
    public void testPauseBeforeSet() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        channelManager.pause();
        channelManager.setChannel(TransportType.CONFIGURATION, channel);
        Mockito.verify(channel, Mockito.times(1)).pause();
    }

    @Test
    public void testResume() throws NoSuchAlgorithmException, InvalidKeySpecException, KaaInvalidChannelException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = getDefaultBootstrapServers();

        BootstrapManager bootstrapManager = Mockito.mock(BootstrapManager.class);
        DefaultChannelManager channelManager = new DefaultChannelManager(bootstrapManager, bootststrapServers, null, null);

        Map<TransportType, ChannelDirection> types = new HashMap<TransportType, ChannelDirection>();
        types.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channel.getTransportProtocolId()).thenReturn(TransportProtocolIdConstants.TCP_TRANSPORT_ID);
        Mockito.when(channel.getSupportedTransportTypes()).thenReturn(types);
        Mockito.when(channel.getId()).thenReturn("channel1");

        channelManager.pause();
        channelManager.addChannel(channel);
        channelManager.resume();
        Mockito.verify(channel, Mockito.times(1)).pause();
        Mockito.verify(channel, Mockito.times(1)).resume();
    }

    private Map<TransportProtocolId, List<TransportConnectionInfo>> getDefaultBootstrapServers() throws NoSuchAlgorithmException {
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers = new HashMap<>();
        TransportConnectionInfo server = IPTransportInfoTest.createTestServerInfo(
                ServerType.BOOTSTRAP, TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        bootststrapServers.put(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, Collections.singletonList(server));
        return bootststrapServers;
    }

}
