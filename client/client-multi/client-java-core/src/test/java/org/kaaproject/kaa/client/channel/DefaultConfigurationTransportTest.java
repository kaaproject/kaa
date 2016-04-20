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

import java.nio.ByteBuffer;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultConfigurationTransport;
import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.schema.SchemaProcessor;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.mockito.Mockito;

public class DefaultConfigurationTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        ConfigurationTransport transport = new DefaultConfigurationTransport();
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);

        ConfigurationTransport transport = new DefaultConfigurationTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channelManager, Mockito.times(1)).sync(TransportType.CONFIGURATION);
    }

    @Test
    public void testCreateRequest() {
        ConfigurationHashContainer hashContainer = Mockito.mock(ConfigurationHashContainer.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);

        ConfigurationTransport transport = new DefaultConfigurationTransport();
        transport.createConfigurationRequest();
        transport.setConfigurationHashContainer(hashContainer);
        transport.createConfigurationRequest();
        transport.setClientState(clientState);

        ConfigurationSyncRequest request = transport.createConfigurationRequest();

        Mockito.verify(hashContainer, Mockito.times(1)).getConfigurationHash();
    }

    @Test
    public void testOnConfigurationResponse() throws Exception {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        SchemaProcessor schemaProcessor = Mockito.mock(SchemaProcessor.class);
        ConfigurationProcessor configProcessor = Mockito.mock(ConfigurationProcessor.class);

        ConfigurationSyncResponse response = new ConfigurationSyncResponse();
        response.setResponseStatus(SyncResponseStatus.DELTA);

        KaaChannelManager channelManagerMock = Mockito.mock(KaaChannelManager.class);

        ConfigurationTransport transport = new DefaultConfigurationTransport();
        transport.setChannelManager(channelManagerMock);
        transport.onConfigurationResponse(response);
        transport.setClientState(clientState);
        transport.onConfigurationResponse(response);
        transport.setConfigurationProcessor(configProcessor);
        transport.onConfigurationResponse(response);
        transport.setSchemaProcessor(schemaProcessor);
        transport.onConfigurationResponse(response);

        response.setConfDeltaBody(ByteBuffer.wrap(new byte[] { 1, 2, 3 }));
        transport.onConfigurationResponse(response);
        response.setConfSchemaBody(ByteBuffer.wrap(new byte[] { 1, 2, 3 }));
        transport.onConfigurationResponse(response);

        Mockito.verify(schemaProcessor, Mockito.times(1)).loadSchema(Mockito.eq(ByteBuffer.wrap(new byte[] { 1, 2, 3 })));
        Mockito.verify(configProcessor, Mockito.times(2)).processConfigurationData(Mockito.eq(ByteBuffer.wrap(new byte[] { 1, 2, 3 })), Mockito.eq(false));
    }

}
