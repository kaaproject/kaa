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

package org.kaaproject.kaa.client.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.IPTransportInfo;
import org.kaaproject.kaa.client.channel.IPTransportInfoTest;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.KaaInternalChannelManager;
import org.kaaproject.kaa.client.channel.KaaInvalidChannelException;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolIdConstants;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.DefaultFailoverManager;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;

public class DefaultBootstrapManagerTest {

    public class ChanelManagerMock implements KaaInternalChannelManager {

        private boolean serverUpdated = false;
        private String receivedUrl;

        public ChanelManagerMock() {
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
        public KaaDataChannel getChannel(String id) {
            return null;
        }

        @Override
        public void onServerFailed(TransportConnectionInfo server, FailoverStatus status) {

        }

        @Override
        public void setFailoverManager(FailoverManager failoverManager) {

        }

        @Override
        public void onTransportConnectionInfoUpdated(TransportConnectionInfo newServer) {
            receivedUrl = new IPTransportInfo(newServer).getURL();
            serverUpdated = true;
        }

        @Override
        public void clearChannelList() {

        }

        @Override
        public void setChannel(TransportType transport, KaaDataChannel channel) throws KaaInvalidChannelException {

        }

        @Override
        public void removeChannel(String id) {

        }

        @Override
        public void shutdown() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void setOperationMultiplexer(KaaDataMultiplexer multiplexer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setOperationDemultiplexer(KaaDataDemultiplexer demultiplexer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setBootstrapMultiplexer(KaaDataMultiplexer multiplexer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setBootstrapDemultiplexer(KaaDataDemultiplexer demultiplexer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sync(TransportType type) {
            // TODO Auto-generated method stub

        }

        @Override
        public void syncAck(TransportType type) {
            // TODO Auto-generated method stub

        }

        @Override
        public void syncAll(TransportType type) {
            // TODO Auto-generated method stub

        }

        @Override
        public TransportConnectionInfo getActiveServer(TransportType logging) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @Test
    public void testReceiveOperationsServerList() throws TransportException {
        BootstrapTransport transport = mock(BootstrapTransport.class);
        DefaultBootstrapManager manager = new DefaultBootstrapManager(transport, null, null);

        boolean exception = false;
        try {
            manager.receiveOperationsServerList();
            manager.useNextOperationsServer(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, FailoverStatus.NO_CONNECTIVITY);
        } catch (BootstrapRuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        manager.receiveOperationsServerList();

        verify(transport, times(2)).sync();
    }

    @Test
    public void testOperationsServerInfoRetrieving() throws TransportException, NoSuchAlgorithmException, InvalidKeySpecException {
        ExecutorContext executorContext = mock(ExecutorContext.class);
        DefaultBootstrapManager manager = new DefaultBootstrapManager(null, executorContext, null);

        boolean exception = false;
        try {
            manager.useNextOperationsServer(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, FailoverStatus.NO_CONNECTIVITY);
        } catch (BootstrapRuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        BootstrapTransport transport = mock(BootstrapTransport.class);

        // Generating pseudo bootstrap key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();

        List<ProtocolMetaData> list = new ArrayList<ProtocolMetaData>();
        ProtocolMetaData md = IPTransportInfoTest.buildMetaData(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889,
                keyPair.getPublic());
        list.add(md);

        ChanelManagerMock channelManager = spy(new ChanelManagerMock());
        when(executorContext.getScheduledExecutor()).thenReturn(Executors.newScheduledThreadPool(1));
        FailoverStrategy strategy = new DefaultFailoverStrategy(1, 1, 1, TimeUnit.MILLISECONDS);
        FailoverManager failoverManager =
                spy(new DefaultFailoverManager(channelManager, executorContext, strategy, 1, TimeUnit.MILLISECONDS));

        manager.setChannelManager(channelManager);
        manager.setFailoverManager(failoverManager);
        manager.setTransport(transport);
        manager.onProtocolListUpdated(list);
        manager.useNextOperationsServer(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, FailoverStatus.NO_CONNECTIVITY);
        assertTrue(channelManager.isServerUpdated());
        assertEquals("http://localhost:9889", channelManager.getReceivedUrl());

        manager.useNextOperationsServerByAccessPointId("some.name".hashCode());
        verify(channelManager, times(1)).onTransportConnectionInfoUpdated(any(TransportConnectionInfo.class));
    }

    @Test
    public void testUseServerByDnsName() throws NoSuchAlgorithmException {
        DefaultBootstrapManager manager = new DefaultBootstrapManager(null, null, null);

        ChanelManagerMock channelManager = spy(new ChanelManagerMock());
        manager.setChannelManager(channelManager);

        BootstrapTransport transport = mock(BootstrapTransport.class);
        manager.setTransport(transport);

        // Generating pseudo operation key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();

        List<ProtocolMetaData> list = new ArrayList<ProtocolMetaData>();
        ProtocolMetaData md = IPTransportInfoTest.buildMetaData(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost", 9889,
                keyPair.getPublic());
        list.add(md);

        manager.onProtocolListUpdated(list);
        assertEquals("http://localhost:9889", channelManager.getReceivedUrl());

        manager.useNextOperationsServerByAccessPointId("localhost2:9889".hashCode());
        verify(transport, times(1)).sync();

        list = new ArrayList<ProtocolMetaData>();
        md = IPTransportInfoTest.buildMetaData(TransportProtocolIdConstants.HTTP_TRANSPORT_ID, "localhost2", 9889, keyPair.getPublic());
        list.add(md);

        manager.onProtocolListUpdated(list);
        assertEquals("http://localhost2:9889", channelManager.getReceivedUrl());
        assertTrue(channelManager.isServerUpdated());
    }
}
