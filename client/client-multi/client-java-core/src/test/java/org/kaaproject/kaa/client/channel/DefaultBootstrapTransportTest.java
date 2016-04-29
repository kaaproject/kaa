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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultBootstrapTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.BootstrapSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.mockito.Mockito;

public class DefaultBootstrapTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);

        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channelManager, Mockito.times(1)).sync(TransportType.BOOTSTRAP);
    }

    @Test
    public void testCreateRequest() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        transport.setChannelManager(channelManager);
        transport.createResolveRequest();
        transport.setClientState(clientState);
        transport.createResolveRequest();

    }

    @Test
    public void onBootstrapResponse() throws Exception {
        BootstrapTransport transport = new DefaultBootstrapTransport("Some token");
        BootstrapManager manager = Mockito.mock(BootstrapManager.class);
        
        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        List<ProtocolMetaData> mdList = new ArrayList<ProtocolMetaData>();
        response.setBootstrapSyncResponse(new BootstrapSyncResponse(1, mdList));

        transport.onResolveResponse(response);
        transport.setBootstrapManager(manager);
        transport.onResolveResponse(response);
        Mockito.verify(manager, Mockito.times(1)).onProtocolListUpdated(mdList);
    }

}
