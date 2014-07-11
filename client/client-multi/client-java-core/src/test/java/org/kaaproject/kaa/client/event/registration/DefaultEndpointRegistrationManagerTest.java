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

package org.kaaproject.kaa.client.event.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultUserTransport;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;

public class DefaultEndpointRegistrationManagerTest {

    @Test
    public void checkEndpointAttachDetachResponse() throws Exception {
        try {
            KaaClientState state = mock(KaaClientState.class);
            when(state.getEndpointAccessToken()).thenReturn("");

            UserTransport transport = new DefaultUserTransport();
            DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, transport, null));
            transport.setEndpointRegistrationProcessor(manager);

            UserSyncResponse sr = new UserSyncResponse();
            sr.setEndpointAttachResponses(null);
            sr.setEndpointDetachResponses(null);

            List<EndpointAttachResponse> attach = new LinkedList<EndpointAttachResponse>();
            attach.add(new EndpointAttachResponse("requestId", "keyHash", SyncResponseResultType.SUCCESS));
            sr.setEndpointAttachResponses(attach);
            transport.onUserResponse(sr);

            List<EndpointDetachResponse> detach = new LinkedList<EndpointDetachResponse>();
            detach.add(new EndpointDetachResponse("requestId", SyncResponseResultType.SUCCESS));
            sr.setEndpointAttachResponses(null);
            sr.setEndpointDetachResponses(detach);
            transport.onUserResponse(sr);

            verify(manager, times(2)).onUpdate(anyListOf(EndpointAttachResponse.class), anyListOf(EndpointDetachResponse.class), any(UserAttachResponse.class));
        } catch (IOException e) {
            assertTrue("Unexpected exception " + e.getMessage(), false);
        }
    }

    @Test
    public void checkAccessTokenChange() {
        KaaClientState state = mock(KaaClientState.class);
        when(state.getEndpointAccessToken()).thenReturn("token1");

        DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, null, null));

        String accessToken1 = manager.getEndpointAccessToken();

        manager.regenerateEndpointAccessToken();

        String accessToken2 = manager.getEndpointAccessToken();

        assertNotEquals("Endpoint access token is same after regeneration!", accessToken1, accessToken2);
    }

    @Test
    public void checkAttachUser() {
        KaaClientState state = mock(KaaClientState.class);
        when(state.getEndpointAccessToken()).thenReturn("");

        EndpointRegistrationManager manager = new DefaultEndpointRegistrationManager(state, null, null);
        manager.attachUser("userExternalId", "userAccessToken", new UserAuthResultListener() {
            @Override
            public void onAuthResult(UserAttachResponse response) {
            }
        });
        // TODO: verify
    }

    @Test
    public void checkWrappers() {
        String token1 = "token1";
        String token2 = "token2";

        EndpointAccessToken at1 = new EndpointAccessToken(token1);
        EndpointAccessToken at1_2 = new EndpointAccessToken(token1);
        EndpointAccessToken at2 = new EndpointAccessToken(token2);

        assertEquals("EnndpointAccessToken != EndpointAccessToken", at1, at1);
        assertNotEquals("EndpointAccessToken should be not equal to String object", at1, token1);
        assertEquals("toString() returned different value from getToken()", at1.getToken(), at1.toString());
        assertEquals("Objects with equal tokens are not equal", at1, at1_2);
        assertNotEquals("Objects with different tokens are equal", at1, at2);
        assertEquals("Objects' hash codes with equal tokens are not equal", at1.hashCode(), at1_2.hashCode());
        assertNotEquals("Objects' hash codes with different tokens are equal", at1.hashCode(), at2.hashCode());

        at1_2.setToken(token2);
        assertEquals("Objects with equal tokens are not equal", at1_2, at2);
        assertNotEquals("Objects with different tokens are equal", at1, at1_2);

        EndpointAccessToken emptyToken1 = new EndpointAccessToken(null);
        EndpointAccessToken emptyToken2 = new EndpointAccessToken(null);

        assertEquals("Empty objects with are not equal", emptyToken1, emptyToken2);
        assertEquals("Objects' hash codes with empty tokens are not equal", emptyToken1.hashCode(), emptyToken1.hashCode());
        assertNotEquals("Different objects are equal", at1, emptyToken1);
        assertNotEquals("Null-equality of EndpointAccessToken", at1, null);

        String hash1 = "hash1";
        String hash2 = "hash2";

        EndpointKeyHash eoh1 = new EndpointKeyHash(hash1);
        EndpointKeyHash eoh1_2 = new EndpointKeyHash(hash1);
        EndpointKeyHash eoh2 = new EndpointKeyHash(hash2);

        assertEquals("EndpointKeyHash != EndpointKeyHash", eoh1, eoh1);
        assertNotEquals("EndpointKeyHash should be not equal to String object", eoh1, hash1);
        assertEquals("toString() returned different value from getKeyHash()", eoh1.getKeyHash(), eoh1.toString());
        assertEquals("Objects with equal keyHashes are not equal", eoh1, eoh1_2);
        assertNotEquals("Objects with different keyHashes are equal", eoh1, eoh2);
        assertEquals("Objects' hash codes with equal keyHashes are not equal", eoh1.hashCode(), eoh1_2.hashCode());
        assertNotEquals("Objects' hash codes with different keyHashes are equal", eoh1.hashCode(), eoh2.hashCode());

        eoh1_2.setKeyHash(hash2);
        assertEquals("Objects with equal keyHashes are not equal", eoh1_2, eoh2);
        assertNotEquals("Objects with different keyHashes are equal", eoh1, eoh1_2);

        EndpointKeyHash emptyHash1 = new EndpointKeyHash(null);
        EndpointKeyHash emptyHash2 = new EndpointKeyHash(null);

        assertEquals("Empty objects with are not equal", emptyHash1, emptyHash2);
        assertEquals("Objects' hash codes with empty hashes are not equal", emptyHash1.hashCode(), emptyHash1.hashCode());
        assertNotEquals("Different objects are equal", eoh1, emptyHash1);
        assertNotEquals("Null-equality of EndpointKeyHash", eoh1, null);
    }

}
