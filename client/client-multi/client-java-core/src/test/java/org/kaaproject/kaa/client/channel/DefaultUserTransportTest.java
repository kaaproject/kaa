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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultUserTransport;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
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
        Map<String, EndpointAccessToken> attachedEPs = new HashMap<>();
        attachedEPs.put("key1", new EndpointAccessToken("acessToken1"));

        Map<String, EndpointKeyHash> detachedEPs = new HashMap<>();
        detachedEPs.put("key1", new EndpointKeyHash("keyhash1"));

        EndpointRegistrationProcessor processor = Mockito.mock(EndpointRegistrationProcessor.class);
        Mockito.when(processor.getAttachEndpointRequests()).thenReturn(attachedEPs);
        Mockito.when(processor.getDetachEndpointRequests()).thenReturn(detachedEPs);

        UserTransport transport = new DefaultUserTransport();
        transport.createUserRequest();
        transport.setEndpointRegistrationProcessor(processor);

        UserSyncRequest request = transport.createUserRequest();

        Mockito.verify(processor, Mockito.times(1)).getAttachEndpointRequests();
        Mockito.verify(processor, Mockito.times(1)).getDetachEndpointRequests();
        Mockito.verify(processor, Mockito.times(1)).getUserAttachRequest();

        Assert.assertTrue(!request.getEndpointAttachRequests().isEmpty());
        Assert.assertTrue(!request.getEndpointDetachRequests().isEmpty());
    }

    @Test
    public void onUserResponse() throws Exception {
        String requestId1 = "requestId1";
        String requestId2 = "requestId2";

        Map<String, EndpointAccessToken> attachingEPs = new HashMap<>();
        attachingEPs.put(requestId1, new EndpointAccessToken("token1"));
        attachingEPs.put(requestId2, new EndpointAccessToken("token2"));

        Map<String, EndpointKeyHash> dettachingEPs = new HashMap<>();
        dettachingEPs.put(requestId1, new EndpointKeyHash("keyhash1"));
        dettachingEPs.put(requestId2, new EndpointKeyHash("keyhash2"));

        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        EndpointRegistrationProcessor processor = Mockito.mock(EndpointRegistrationProcessor.class);

        Mockito.when(processor.getAttachEndpointRequests()).thenReturn(attachingEPs);
        Mockito.when(processor.getDetachEndpointRequests()).thenReturn(dettachingEPs);

        UserTransport transport = new DefaultUserTransport();
        UserSyncResponse response1 = new UserSyncResponse();

        response1.setEndpointAttachResponses(Arrays.asList(
                new EndpointAttachResponse(requestId1, "keyhash1", SyncResponseResultType.SUCCESS),
                new EndpointAttachResponse(requestId2, "keyhash2", SyncResponseResultType.SUCCESS),
                new EndpointAttachResponse("anotherAttachedRequest", "keyhash2", SyncResponseResultType.FAILURE)));

        response1.setEndpointDetachResponses(Arrays.asList(
                new EndpointDetachResponse(requestId1, SyncResponseResultType.SUCCESS),
                new EndpointDetachResponse("anotherRequest", SyncResponseResultType.FAILURE)));

        transport.onUserResponse(response1);
        transport.setEndpointRegistrationProcessor(processor);
        transport.setClientState(clientState);
        transport.onUserResponse(response1);

        Mockito.verify(processor, Mockito.times(1)).onUpdate(Mockito.anyListOf(EndpointAttachResponse.class), Mockito.anyListOf(EndpointDetachResponse.class), Mockito.any(UserAttachResponse.class), Mockito.any(UserAttachNotification.class), Mockito.any(UserDetachNotification.class));

        UserSyncResponse response2 = new UserSyncResponse();

        response2.setEndpointDetachResponses(Arrays.asList(
                new EndpointDetachResponse(requestId2, SyncResponseResultType.SUCCESS)));

        transport.onUserResponse(response2);

        Mockito.verify(clientState, Mockito.times(2)).setAttachedEndpointsList(Mockito.anyMap());
    }
}
