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

package org.kaaproject.kaa.client.update.listeners;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.schema.SchemaProcessor;
import org.kaaproject.kaa.common.endpoint.gen.ConfSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class ConfigurationUpdateListenerTest {

    @Test
    public void testConfigurationUpdateListener() throws IOException {
        ConfigurationProcessor configurationProcessor = mock(ConfigurationProcessor.class);
        SchemaProcessor schemaProcessor = mock(SchemaProcessor.class);
        ConfigurationHashContainer hashContainer = mock(ConfigurationHashContainer.class);
        KaaClientState clientState = mock(KaaClientState.class);
        ConfigurationUpdateListener listener = new ConfigurationUpdateListener(configurationProcessor, schemaProcessor, hashContainer, clientState);

        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.CONF_RESYNC);
        listener.onDeltaUpdate(response);
        ConfSyncResponse confResponse = new ConfSyncResponse();
        response.setConfSyncResponse(confResponse);
        listener.onDeltaUpdate(response);
        confResponse.setConfDeltaBody(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        confResponse.setConfSchemaBody(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        response.setResponseType(SyncResponseStatus.DELTA);
        listener.onDeltaUpdate(response);

        verify(configurationProcessor, times(1)).processConfigurationData(any(ByteBuffer.class), any(Boolean.class));
        verify(schemaProcessor, times(1)).loadSchema(any(ByteBuffer.class));
        verify(hashContainer, times(1)).getConfigurationHash();
        verify(clientState, times(1)).setConfigurationHash(any(EndpointObjectHash.class));
    }

}
