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

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultBootstrapTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.mockito.Mockito;

public class DefaultBootstrapTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.BOOTSTRAP)).thenReturn(channel);

        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channel, Mockito.times(1)).sync(TransportType.BOOTSTRAP);
    }

    @Test
    public void testCreateRequest() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        transport.createResolveRequest();
        transport.setClientState(clientState);
        transport.createResolveRequest();

    }

    @Test
    public void onBootstrapResponse() throws Exception {
        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        BootstrapManager manager = Mockito.mock(BootstrapManager.class);
        OperationsServerList response = new OperationsServerList();
        transport.onResolveResponse(response);
        transport.setBootstrapManager(manager);
        transport.onResolveResponse(response);
        Mockito.verify(manager, Mockito.times(1)).onServerListUpdated(response);
    }

}
