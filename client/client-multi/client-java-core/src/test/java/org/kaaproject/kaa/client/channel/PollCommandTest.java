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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.channels.polling.PollCommand;
import org.kaaproject.kaa.client.channel.impl.channels.polling.RawDataProcessor;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.mockito.Mockito;

public class PollCommandTest {

    @Test
    public void testPollCommand() throws Exception {
        AbstractHttpClient client = Mockito.mock(AbstractHttpClient.class);
        RawDataProcessor processor = Mockito.mock(RawDataProcessor.class);
        Map<TransportType, ChannelDirection> transportTypes = new HashMap<>();
        IPTransportInfo serverInfo = Mockito.mock(IPTransportInfo.class);

        Mockito.when(processor.createRequest(transportTypes)).thenReturn(null);

        PollCommand command = new PollCommand(client, processor, transportTypes, serverInfo);
        command.execute();

        Mockito.when(processor.createRequest(transportTypes)).thenReturn(new LinkedHashMap<String, byte[]>());

        command.execute();

        Mockito.verify(client, Mockito.times(1)).executeHttpRequest(Mockito.eq(""), Mockito.any(LinkedHashMap.class), Mockito.anyBoolean());
        Mockito.verify(processor, Mockito.times(1)).onResponse(Mockito.any(byte[].class));
        Mockito.verify(processor, Mockito.times(2)).createRequest(transportTypes);

        Mockito.when(client.canAbort()).thenReturn(false);
        command.cancel();
        Mockito.when(client.canAbort()).thenReturn(true);
        command.cancel();
        Mockito.verify(client, Mockito.times(1)).abort();
    }

    @Test
    public void testOnServerError() throws Exception {
        AbstractHttpClient client = Mockito.mock(AbstractHttpClient.class);
        RawDataProcessor processor = Mockito.mock(RawDataProcessor.class);
        Map<TransportType, ChannelDirection> transportTypes = new HashMap<>();
        IPTransportInfo serverInfo = Mockito.mock(IPTransportInfo.class);

        Mockito.when(client.executeHttpRequest(Mockito.anyString(), Mockito.any(LinkedHashMap.class), Mockito.anyBoolean())).thenThrow(new Exception());

        PollCommand command = new PollCommand(client, processor, transportTypes, serverInfo);
        command.execute();

        Mockito.verify(processor, Mockito.times(1)).onServerError(serverInfo);
    }

}
