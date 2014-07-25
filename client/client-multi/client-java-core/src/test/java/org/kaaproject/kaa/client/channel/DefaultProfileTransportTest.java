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

import java.io.IOException;

import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultProfileTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.profile.DefaultProfileListener;
import org.kaaproject.kaa.client.profile.ProfileListener;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.client.profile.SerializedProfileContainer;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.mockito.Mockito;

public class DefaultProfileTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        ProfileTransport transport = new DefaultProfileTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.PROFILE)).thenReturn(channel);

        ProfileTransport transport = new DefaultProfileTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channel, Mockito.times(1)).syncAll();
    }

    @Test
    public void testCreateRequest() throws IOException {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        Mockito.when(clientState.isRegistered()).thenReturn(true);
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ProfileManager profileManager = Mockito.mock(ProfileManager.class);
        SerializedProfileContainer profileContainer = Mockito.mock(SerializedProfileContainer.class);
        Mockito.when(profileContainer.getSerializedProfile()).thenReturn(new byte [] { 1, 2, 3 });
        Mockito.when(profileManager.getSerializedProfileContainer()).thenReturn(profileContainer);

        ProfileTransport transport = new DefaultProfileTransport();
        transport.createProfileRequest();
        transport.setClientProperties(properties);
        transport.createProfileRequest();
        transport.setClientState(clientState);
        transport.createProfileRequest();
        transport.setProfileManager(profileManager);

        transport.createProfileRequest();
        Mockito.verify(clientState, Mockito.times(1)).getEndpointAccessToken();
        Mockito.verify(properties, Mockito.times(1)).getVersionInfo();
        Mockito.verify(profileContainer, Mockito.times(1)).getSerializedProfile();
    }

    @Test
    public void onProfileResponse() throws Exception {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        KaaDataChannel channel = Mockito.mock(KaaDataChannel.class);
        Mockito.when(channelManager.getChannelByTransportType(TransportType.PROFILE)).thenReturn(channel);

        ProfileTransport transport = new DefaultProfileTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);

        ProfileSyncResponse response = new ProfileSyncResponse();
        response.setResponseStatus(SyncResponseStatus.RESYNC);

        transport.onProfileResponse(response);

        Mockito.verify(channel, Mockito.times(1)).syncAll();
    }

    @Test
    public void testProfileListener() throws Exception {
        ProfileTransport transport = Mockito.mock(ProfileTransport.class);
        ProfileListener listener = new DefaultProfileListener(transport);

        listener.onProfileUpdated(null);
        listener.onProfileUpdated(new byte[1]);

        Mockito.verify(transport, Mockito.times(1)).sync();
    }

}
