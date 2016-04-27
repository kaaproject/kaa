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
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultProfileTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.profile.DefaultProfileManager;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.mockito.Mockito;

public class DefaultProfileTransportTest {

    @Test(expected = ChannelRuntimeException.class)
    public void testSyncNegative() {
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        ProfileTransport transport = new DefaultProfileTransport();
        transport.setClientState(clientState);
        transport.sync();
    }

    @Test
    public void testSync() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);

        ProfileTransport transport = new DefaultProfileTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState);
        transport.sync();

        Mockito.verify(channelManager, Mockito.times(1)).syncAll(TransportType.PROFILE);
    }

    @Test
    public void testCreateEmptyRequest() {

    }

    @Test
    public void testCreateRequest() throws IOException {
        PublicKey k = Mockito.mock(PublicKey.class);
        Mockito.when(k.getEncoded()).thenReturn(new byte[1]);
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        Mockito.when(clientState.getPublicKey()).thenReturn(k);
        Mockito.when(clientState.isRegistered()).thenReturn(false);
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ProfileManager profileManager = Mockito.mock(ProfileManager.class);
        Mockito.when(profileManager.getSerializedProfile()).thenReturn(new byte [] { 1, 2, 3 });

        ProfileTransport transport = new DefaultProfileTransport();
        transport.createProfileRequest();
        transport.setClientState(clientState);
        transport.createProfileRequest();
        transport.setProfileManager(profileManager);
        transport.createProfileRequest();
        transport.setClientProperties(properties);

        transport.createProfileRequest();
        Mockito.verify(clientState, Mockito.times(1)).getEndpointAccessToken();
        Mockito.verify(profileManager, Mockito.times(1)).getSerializedProfile();
    }

    @Test
    public void testUpToDateProfile() throws Exception {
        byte [] profile = new byte [] { 1, 2, 3 };
        KaaClientState clientState = Mockito.mock(KaaClientState.class);
        Mockito.when(clientState.isRegistered()).thenReturn(true);
        Mockito.when(clientState.getProfileHash()).thenReturn(EndpointObjectHash.fromSHA1(profile));
        KaaClientProperties properties = Mockito.mock(KaaClientProperties.class);
        ProfileManager profileManager = Mockito.mock(ProfileManager.class);
        Mockito.when(profileManager.getSerializedProfile()).thenReturn(new byte [] { 1, 2, 3 });

        ProfileTransport transport = new DefaultProfileTransport();
        transport.createProfileRequest();
        transport.setClientState(clientState);
        transport.createProfileRequest();
        transport.setProfileManager(profileManager);
        transport.createProfileRequest();
        transport.setClientProperties(properties);

        Assert.assertNull(transport.createProfileRequest());
        Mockito.verify(clientState, Mockito.times(0)).getEndpointAccessToken();
    }

    @Test
    public void onProfileResponse() throws Exception {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        KaaClientState clientState1 = Mockito.mock(KaaClientState.class);

        ProfileTransport transport = new DefaultProfileTransport();
        transport.setChannelManager(channelManager);
        transport.setClientState(clientState1);

        ProfileSyncResponse response1 = new ProfileSyncResponse();
        response1.setResponseStatus(SyncResponseStatus.RESYNC);

        transport.onProfileResponse(response1);

        Mockito.verify(channelManager, Mockito.times(1)).syncAll(TransportType.PROFILE);


        ProfileSyncResponse response2 = new ProfileSyncResponse();
        response2.setResponseStatus(SyncResponseStatus.DELTA);

        transport.setClientState(null);
        transport.onProfileResponse(response2);
        Mockito.verify(clientState1, Mockito.times(0)).setRegistered(Mockito.anyBoolean());

        Mockito.when(clientState1.isRegistered()).thenReturn(false);
        transport.setClientState(clientState1);
        transport.onProfileResponse(response2);
        Mockito.verify(clientState1, Mockito.times(1)).setRegistered(Mockito.eq(true));

        KaaClientState clientState2 = Mockito.mock(KaaClientState.class);
        Mockito.when(clientState2.isRegistered()).thenReturn(true);
        transport.setClientState(clientState2);
        transport.onProfileResponse(response2);
        Mockito.verify(clientState2, Mockito.times(0)).setRegistered(Mockito.anyBoolean());
    }

    @Test
    public void testProfileListener() throws Exception {
        ProfileTransport transport = Mockito.mock(ProfileTransport.class);
        ProfileManager manager = new DefaultProfileManager(transport);

        manager.setProfileContainer(new ProfileContainer() {
            
            @Override
            public EmptyData getProfile() {
                return new EmptyData();
            }
        });
        manager.updateProfile();

        Mockito.verify(transport, Mockito.times(1)).sync();
    }

}
