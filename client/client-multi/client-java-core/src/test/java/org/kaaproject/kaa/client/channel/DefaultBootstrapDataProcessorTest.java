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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.DefaultBootstrapDataProcessor;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.BootstrapSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.mockito.Mockito;

public class DefaultBootstrapDataProcessorTest {

    @Test
    public void testRequestCreation() throws IOException {
        DefaultBootstrapDataProcessor processor = new DefaultBootstrapDataProcessor();
        BootstrapTransport transport = Mockito.mock(BootstrapTransport.class);
        Mockito.when(transport.createResolveRequest()).thenReturn(new SyncRequest());
        processor.setBootstrapTransport(transport);
        Assert.assertNotNull(processor.compileRequest(null));
        Mockito.verify(transport, Mockito.times(1)).createResolveRequest();
    }

    @Test
    public void testRequestCreationWithNullTransport() throws IOException {
        DefaultBootstrapDataProcessor processor = new DefaultBootstrapDataProcessor();
        Assert.assertNull(processor.compileRequest(null));
    }

    @Test
    public void testResponse() throws IOException {
        DefaultBootstrapDataProcessor processor = new DefaultBootstrapDataProcessor();
        BootstrapTransport transport = Mockito.mock(BootstrapTransport.class);
        processor.setBootstrapTransport(transport);
        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        List<ProtocolMetaData> mdList = new ArrayList<ProtocolMetaData>();
        response.setBootstrapSyncResponse(new BootstrapSyncResponse(1, mdList));
        AvroByteArrayConverter<SyncResponse> converter = new AvroByteArrayConverter<>(SyncResponse.class);
        processor.processResponse(converter.toByteArray(response));
        Mockito.verify(transport, Mockito.times(1)).onResolveResponse(response);
    }

    @Test
    public void testNullResponse() throws IOException {
        DefaultBootstrapDataProcessor processor = new DefaultBootstrapDataProcessor();
        BootstrapTransport transport = Mockito.mock(BootstrapTransport.class);
        processor.setBootstrapTransport(transport);
        processor.processResponse(null);
        Mockito.verify(transport, Mockito.times(0)).onResolveResponse(Mockito.any(SyncResponse.class));
    }

    @Test
    public void testNullResponseWithNullTransport() throws IOException {
        DefaultBootstrapDataProcessor processor = new DefaultBootstrapDataProcessor();
        processor.processResponse(null);
    }

}
