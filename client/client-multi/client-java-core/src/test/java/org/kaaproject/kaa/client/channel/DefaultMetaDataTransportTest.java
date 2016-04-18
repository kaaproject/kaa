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

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultMetaDataTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.mockito.Mockito;

public class DefaultMetaDataTransportTest {

    @Test
    public void testCreateMetaDataRequest() {
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        KaaClientState state = Mockito.mock(KaaClientState.class);
        Mockito.when(state.getProfileHash()).thenReturn(EndpointObjectHash.fromSHA1("123"));
        EndpointObjectHash publicKeyHash = EndpointObjectHash.fromSHA1("567");
        MetaDataTransport transport = new DefaultMetaDataTransport();
        transport.createMetaDataRequest();
        transport.setClientProperties(properties);
        transport.createMetaDataRequest();
        transport.setClientState(state);
        transport.createMetaDataRequest();
        transport.setEndpointPublicKeyhash(publicKeyHash);
        transport.setTimeout(5);

        SyncRequestMetaData request = transport.createMetaDataRequest();

        Mockito.verify(state, Mockito.times(1)).getProfileHash();
        Mockito.verify(properties, Mockito.times(1)).getSdkToken();

        Assert.assertEquals(new Long(5), request.getTimeout());
    }

}
