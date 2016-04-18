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

import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultLogTransport;
import org.kaaproject.kaa.client.logging.LogProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.mockito.Mockito;

public class DefaultLogTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        LogTransport transport = new DefaultLogTransport();
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);

        LogTransport transport = new DefaultLogTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channelManager, Mockito.times(1)).sync(TransportType.LOGGING);
    }

    @Test
    public void testCreateRequest() {
        LogProcessor processor = Mockito.mock(LogProcessor.class);

        LogTransport transport = new DefaultLogTransport();
        transport.createLogRequest();
        transport.setLogProcessor(processor);
        transport.createLogRequest();
        Mockito.verify(processor, Mockito.times(1)).fillSyncRequest(Mockito.any(LogSyncRequest.class));
    }

    @Test
    public void onEventResponse() throws Exception {
        LogProcessor processor = Mockito.mock(LogProcessor.class);
        LogTransport transport = new DefaultLogTransport();
        LogSyncResponse response = new LogSyncResponse();

        transport.onLogResponse(response);
        transport.setLogProcessor(processor);
        transport.onLogResponse(response);

        Mockito.verify(processor, Mockito.times(1)).onLogResponse(response);
    }

}
