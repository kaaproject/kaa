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
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultUserTransport;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.mockito.Mockito;

public class DefaultUserTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        UserTransport transport = new DefaultUserTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.USER)).thenReturn(channel);

        UserTransport transport = new DefaultUserTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channel, Mockito.times(1)).sync(TransportType.USER);
    }

    @Test
    public void testCreateRequest() {
        EndpointRegistrationProcessor processor = Mockito.mock(EndpointRegistrationProcessor.class);

        UserTransport transport = new DefaultUserTransport();
        transport.createUserRequest();
        transport.setEndpointRegistrationProcessor(processor);
        transport.createUserRequest();
        Mockito.verify(processor, Mockito.times(1)).getAttachEndpointRequests();
        Mockito.verify(processor, Mockito.times(1)).getDetachEndpointRequests();
        Mockito.verify(processor, Mockito.times(1)).getUserAttachRequest();
    }

    @Test
    public void onUserResponse() throws Exception {
        EndpointRegistrationProcessor processor = Mockito.mock(EndpointRegistrationProcessor.class);
        UserTransport transport = new DefaultUserTransport();
        UserSyncResponse response = new UserSyncResponse();

        transport.onUserResponse(response);
        transport.setEndpointRegistrationProcessor(processor);
        transport.onUserResponse(response);

        Mockito.verify(processor, Mockito.times(1)).onUpdate(Mockito.anyListOf(EndpointAttachResponse.class), Mockito.anyListOf(EndpointDetachResponse.class), Mockito.any(UserAttachResponse.class), Mockito.any(UserAttachNotification.class), Mockito.any(UserDetachNotification.class));
    }
}
