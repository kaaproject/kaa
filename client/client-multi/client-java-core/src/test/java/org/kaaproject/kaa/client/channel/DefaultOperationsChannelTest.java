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

import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.DefaultFailoverManager;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultOperationsChannel;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.mockito.Mockito;

public class DefaultOperationsChannelTest {

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.DOWN);
    }

    public ScheduledExecutorService fakeExecutor = new FakeExecutorService();

    class DefaultOperationsChannelFake extends DefaultOperationsChannel {

        private final int wantedNumberOfInvocations;
        private KaaDataDemultiplexer demultiplexer;
        private KaaDataMultiplexer multiplexer;

        public DefaultOperationsChannelFake(AbstractKaaClient client,
                KaaClientState state, FailoverManager failoverManager, int wantedNumberOfInvocations) {
            super(client, state, failoverManager);
            this.wantedNumberOfInvocations = wantedNumberOfInvocations;
        }

        @Override
        protected ScheduledExecutorService createExecutor() {
            super.createExecutor();
            return fakeExecutor;
        }

        @Override
        public void setDemultiplexer(KaaDataDemultiplexer demultiplexer) {
            this.demultiplexer = demultiplexer;
            super.setDemultiplexer(demultiplexer);
        }

        @Override
        public void setMultiplexer(KaaDataMultiplexer multiplexer) {
            this.multiplexer = multiplexer;
            super.setMultiplexer(multiplexer);
        }

        @Override
        public void onResponse(byte [] response) {
            super.onResponse(response);
            try {
                Field field = DefaultOperationsChannel.class.getDeclaredField("stopped");
                field.setAccessible(true);
                field.setBoolean(this, true);
            } catch (Exception e) {
                throw new AssertionError(e.getMessage());
            }
        }

        @Override
        public LinkedHashMap<String, byte[]> createRequest(Map<TransportType, ChannelDirection> types) {
            super.createRequest(types);
            return new LinkedHashMap<>();
        }

        public void verify() throws Exception {
            Mockito.verify(multiplexer, Mockito.times(wantedNumberOfInvocations)).compileRequest(Mockito.anyMap());
            Mockito.verify(demultiplexer, Mockito.times(wantedNumberOfInvocations)).processResponse(Mockito.any(byte [].class));
        }
    }

    @Test
    public void testChannelGetters() {
        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        KaaClientState state = Mockito.mock(KaaClientState.class);
        FailoverManager failoverManager = Mockito.mock(FailoverManager.class);
        KaaDataChannel channel = new DefaultOperationsChannel(client, state, failoverManager);

        Assert.assertEquals(SUPPORTED_TYPES, channel.getSupportedTransportTypes());
        Assert.assertEquals(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, channel.getTransportProtocolId());
        Assert.assertEquals("default_operations_long_poll_channel", channel.getId());
    }

    @Test
    public void testChannelSync() throws Exception {
        KaaChannelManager manager = Mockito.mock(KaaChannelManager.class);
        AbstractHttpClient httpClient = Mockito.mock(AbstractHttpClient.class);
        FailoverManager failoverManager = Mockito.mock(FailoverManager.class);
        Mockito.when(
                httpClient.executeHttpRequest(Mockito.anyString(),
                        Mockito.any(LinkedHashMap.class), Mockito.anyBoolean())).thenReturn(
                new byte[] { 5, 5, 5 });

        MessageEncoderDecoder encDec = Mockito.mock(MessageEncoderDecoder.class);
        Mockito.when(httpClient.getEncoderDecoder()).thenReturn(encDec);

        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        Mockito.when(
                client.createHttpClient(Mockito.anyString(),
                        Mockito.any(PrivateKey.class),
                        Mockito.any(PublicKey.class),
                        Mockito.any(PublicKey.class))).thenReturn(httpClient);

        Mockito.when(client.getChannelManager()).thenReturn(manager);

        KaaClientState state = Mockito.mock(KaaClientState.class);
        KaaDataMultiplexer multiplexer = Mockito.mock(KaaDataMultiplexer.class);
        Mockito.when(multiplexer.compileRequest(Mockito.anyMap())).thenReturn(new byte [] { 1, 1 ,1 });
        KaaDataDemultiplexer demultiplexer = Mockito.mock(KaaDataDemultiplexer.class);
        DefaultOperationsChannelFake channel = new DefaultOperationsChannelFake(client, state, failoverManager, 3);

        TransportConnectionInfo server = IPTransportInfoTest.createTestServerInfo(ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID,
                "localhost", 9889, KeyUtil.generateKeyPair().getPublic());

        channel.setDemultiplexer(null);
        channel.setDemultiplexer(demultiplexer);
        channel.setMultiplexer(null);
        channel.setMultiplexer(multiplexer);
        channel.setServer(server);

        channel.sync(TransportType.BOOTSTRAP);
        channel.sync(TransportType.CONFIGURATION);

        channel.syncAll();

        channel.verify();
    }

    @Test
    public void testServerFailed() throws Exception {
        KaaChannelManager manager = Mockito.mock(KaaChannelManager.class);
        MessageEncoderDecoder encDec = Mockito.mock(MessageEncoderDecoder.class);
        AbstractHttpClient httpClient = Mockito.mock(AbstractHttpClient.class);
        ExecutorContext context = Mockito.mock(ExecutorContext.class);
        Mockito.when(context.getScheduledExecutor()).thenReturn(Executors.newScheduledThreadPool(1));
        FailoverStrategy failoverStrategy = new DefaultFailoverStrategy(1, 1, 1, TimeUnit.MILLISECONDS);
        FailoverManager flManager = new DefaultFailoverManager(manager, context, failoverStrategy, 100, TimeUnit.MILLISECONDS);
        FailoverManager failoverManager = Mockito.spy(flManager);
        Mockito.when(
                httpClient.executeHttpRequest(Mockito.anyString(),
                        Mockito.any(LinkedHashMap.class), Mockito.anyBoolean())).thenThrow(new Exception());
        Mockito.when(httpClient.getEncoderDecoder()).thenReturn(encDec);

        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        Mockito.when(
                client.createHttpClient(Mockito.anyString(),
                        Mockito.any(PrivateKey.class),
                        Mockito.any(PublicKey.class),
                        Mockito.any(PublicKey.class))).thenReturn(httpClient);

        Mockito.when(client.getChannelManager()).thenReturn(manager);

        KaaClientState state = Mockito.mock(KaaClientState.class);
        KaaDataMultiplexer multiplexer = Mockito.mock(KaaDataMultiplexer.class);
        KaaDataDemultiplexer demultiplexer = Mockito.mock(KaaDataDemultiplexer.class);
        DefaultOperationsChannelFake channel = new DefaultOperationsChannelFake(client, state, failoverManager, 1);
        channel.setDemultiplexer(demultiplexer);
        channel.setMultiplexer(multiplexer);

        TransportConnectionInfo server = IPTransportInfoTest.createTestServerInfo(ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID,
                "localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        channel.setServer(server);

        Mockito.verify(failoverManager, Mockito.times(1)).onServerFailed(Mockito.any(TransportConnectionInfo.class), Mockito.any(FailoverStatus.class));
    }

    @Test
    public void testShutdown() throws Exception {
        KaaChannelManager manager = Mockito.mock(KaaChannelManager.class);
        AbstractHttpClient httpClient = Mockito.mock(AbstractHttpClient.class);
        FailoverManager failoverManager = Mockito.mock(FailoverManager.class);
        Mockito.when(
                httpClient.executeHttpRequest(Mockito.anyString(),
                        Mockito.any(LinkedHashMap.class), Mockito.anyBoolean())).thenThrow(new Exception());

        AbstractKaaClient client = Mockito.mock(AbstractKaaClient.class);
        Mockito.when(
                client.createHttpClient(Mockito.anyString(),
                        Mockito.any(PrivateKey.class),
                        Mockito.any(PublicKey.class),
                        Mockito.any(PublicKey.class))).thenReturn(httpClient);

        Mockito.when(client.getChannelManager()).thenReturn(manager);

        KaaClientState state = Mockito.mock(KaaClientState.class);
        KaaDataMultiplexer multiplexer = Mockito.mock(KaaDataMultiplexer.class);
        KaaDataDemultiplexer demultiplexer = Mockito.mock(KaaDataDemultiplexer.class);
        DefaultOperationsChannelFake channel = new DefaultOperationsChannelFake(client, state, failoverManager, 0);
        channel.syncAll();
        channel.setDemultiplexer(demultiplexer);
        channel.setMultiplexer(multiplexer);
        channel.shutdown();

        TransportConnectionInfo server = IPTransportInfoTest.createTestServerInfo(ServerType.OPERATIONS, TransportProtocolIdConstants.HTTP_TRANSPORT_ID,
                "localhost", 9889, KeyUtil.generateKeyPair().getPublic());
        channel.setServer(server);

        channel.sync(TransportType.EVENT);
        channel.syncAll();

        channel.verify();
    }

}
