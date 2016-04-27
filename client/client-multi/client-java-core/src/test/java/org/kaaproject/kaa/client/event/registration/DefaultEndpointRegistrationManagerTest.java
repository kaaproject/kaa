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

package org.kaaproject.kaa.client.event.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultUserTransport;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultEndpointRegistrationManagerTest {

    private static final int REQUEST_ID = 42;
    
    private static ExecutorContext executorContext;
    private static ExecutorService executor;
    
    @BeforeClass
    public static void beforeSuite(){
        executorContext = Mockito.mock(ExecutorContext.class);
        executor = Executors.newSingleThreadExecutor();
        Mockito.when(executorContext.getApiExecutor()).thenReturn(executor);
        Mockito.when(executorContext.getCallbackExecutor()).thenReturn(executor);
    }
    
    @AfterClass
    public static void afterSuite(){
        executor.shutdown();
    }
    
    @Test(expected=IllegalStateException.class)
    public void checkUserAttachWithNoDefaultVerifier() throws Exception {
        KaaClientState state = mock(KaaClientState.class);

        when(state.getEndpointAccessToken()).thenReturn("");

        UserTransport transport = mock(UserTransport.class);
        DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, executorContext, transport, null));

        manager.attachUser("externalId", "token", null);
    }
    
    @Test
    public void checkUserAttachWithCustomVerifier() throws Exception {
        KaaClientState state = mock(KaaClientState.class);
        ExecutorContext executorContext = Mockito.mock(ExecutorContext.class);
        when(state.getEndpointAccessToken()).thenReturn("");

        UserTransport transport = mock(UserTransport.class);
        DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, executorContext, transport, null));

        manager.attachUser("verifierToken", "externalId", "token", null);
        verify(transport, times(1)).sync();
    }

    @Test
    public void checkAttachEndpoint() throws Exception {
        KaaClientState state = mock(KaaClientState.class);
        ExecutorContext executorContext = Mockito.mock(ExecutorContext.class);
        when(state.getEndpointAccessToken()).thenReturn("");
        UserTransport transport = mock(UserTransport.class);
        DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, executorContext, transport, null));
        OnAttachEndpointOperationCallback listener = mock(OnAttachEndpointOperationCallback.class);
        manager.attachEndpoint(new EndpointAccessToken("accessToken1"), listener);
        manager.attachEndpoint(new EndpointAccessToken("accessToken2"), listener);

        ReflectionTestUtils.setField(manager, "userTransport", null);
        manager.attachEndpoint(new EndpointAccessToken("accessToken3"), null);
        verify(transport, times(2)).sync();
    }

    @Test
    public void checkDetachEndpoint() throws Exception {
        KaaClientState state = mock(KaaClientState.class);
        ExecutorContext executorContext = Mockito.mock(ExecutorContext.class);
        when(state.getEndpointAccessToken()).thenReturn("");

        OnDetachEndpointOperationCallback listener = mock(OnDetachEndpointOperationCallback.class);
        UserTransport transport = mock(UserTransport.class);

        DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, executorContext, transport, null));
        manager.detachEndpoint(new EndpointKeyHash("keyHash1"), listener);
        manager.detachEndpoint(new EndpointKeyHash("keyHash2"), listener);

        ReflectionTestUtils.setField(manager, "userTransport", null);
        manager.detachEndpoint(new EndpointKeyHash("keyHash3"), null);
        verify(transport, times(2)).sync();
    }

    @Ignore("Ignore during removing endpoint list change listener .")
    @Test
    public void checkEndpointAttachDetachResponse() throws Exception {
        try {
            KaaClientState state = mock(KaaClientState.class);
            ExecutorContext executorContext = Mockito.mock(ExecutorContext.class);
            when(state.getEndpointAccessToken()).thenReturn("");

            UserTransport transport = new DefaultUserTransport();
            DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, executorContext, transport, null));

            ChangedAttachedEndpointListCallback listListener = mock(ChangedAttachedEndpointListCallback.class);
//            manager.addAttachedEndpointListChangeListener(listListener);

            transport.setEndpointRegistrationProcessor(manager);

            UserSyncResponse sr = new UserSyncResponse();
            sr.setEndpointAttachResponses(null);
            sr.setEndpointDetachResponses(null);

            List<EndpointAttachResponse> attach = new LinkedList<EndpointAttachResponse>();
            attach.add(new EndpointAttachResponse(REQUEST_ID, "keyHash", SyncResponseResultType.SUCCESS));
            sr.setEndpointAttachResponses(attach);
            transport.onUserResponse(sr);

//            manager.removeAttachedEndpointListChangeListener(listListener);

            List<EndpointDetachResponse> detach = new LinkedList<EndpointDetachResponse>();
            detach.add(new EndpointDetachResponse(REQUEST_ID, SyncResponseResultType.SUCCESS));
            sr.setEndpointAttachResponses(null);
            sr.setEndpointDetachResponses(detach);
            transport.onUserResponse(sr);

            verify(manager, times(2)).onUpdate(anyListOf(EndpointAttachResponse.class), anyListOf(EndpointDetachResponse.class), any(UserAttachResponse.class), any(UserAttachNotification.class), any(UserDetachNotification.class));
            verify(listListener, times(1)).onAttachedEndpointListChanged(anyMapOf(EndpointAccessToken.class, EndpointKeyHash.class));
        } catch (IOException e) {
            assertTrue("Unexpected exception " + e.getMessage(), false);
        }
    }

    @Ignore("Ignore during removing endpoint list change listener .")
    @Test
    public void checkAccessTokenChange() {
        KaaClientState state = mock(KaaClientState.class);
        ExecutorContext executorContext = Mockito.mock(ExecutorContext.class);
        
        when(state.getEndpointAccessToken()).thenReturn("token1");

        DefaultEndpointRegistrationManager manager = spy(new DefaultEndpointRegistrationManager(state, executorContext, null, null));

        String accessToken1 = state.getEndpointAccessToken();

        state.refreshEndpointAccessToken();

        String accessToken2 = state.getEndpointAccessToken();

        assertNotEquals("Endpoint access token is same after regeneration!", accessToken1, accessToken2);
    }

    @Test
    public void checkAttachUser() {
        ExecutorContext executorContext = Mockito.mock(ExecutorContext.class);
        KaaClientState state = mock(KaaClientState.class);
        when(state.getEndpointAccessToken()).thenReturn("");

        UserTransport transport = mock(UserTransport.class);
        EndpointRegistrationManager manager = new DefaultEndpointRegistrationManager(state, executorContext, transport, null);
        manager.attachUser("externalId", "userExternalId", "userAccessToken", new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
            }
        });
        verify(transport, times(1)).sync();
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

    @Test
    public void checkOnAttachedCallback() throws IOException {
        KaaClientState state = mock(KaaClientState.class);
        when(state.getEndpointAccessToken()).thenReturn("");

        AttachEndpointToUserCallback listener = mock(AttachEndpointToUserCallback.class);

        DefaultEndpointRegistrationManager manager = new DefaultEndpointRegistrationManager(state, executorContext, null, null);
        manager.setAttachedCallback(null);
        manager.onUpdate(null, null, null, new UserAttachNotification("foo", "bar"), null);
        manager.setAttachedCallback(listener);
        manager.onUpdate(null, null, null, new UserAttachNotification("foo", "bar"), null);

        verify(listener, Mockito.timeout(1000).times(1)).onAttachedToUser("foo", "bar");
        verify(state, Mockito.timeout(1000).times(2)).setAttachedToUser(true);

        manager.setAttachedCallback(null);
        manager.attachUser("externalId", "foo", "bar", null);
        manager.onUpdate(null, null, new UserAttachResponse(SyncResponseResultType.SUCCESS, null, null), null, null);

        manager.setAttachedCallback(listener);
        manager.attachUser("externalId", "foo", "bar", null);
        manager.onUpdate(null, null, new UserAttachResponse(SyncResponseResultType.SUCCESS, null, null), null, null);

        verify(listener, Mockito.timeout(1000).times(1)).onAttachedToUser(anyString(), anyString());
        verify(state, Mockito.timeout(1000).times(4)).setAttachedToUser(true);
    }

    @Test
    public void checkOnDetachedCallback() throws IOException {
        KaaClientState state = mock(KaaClientState.class);
        when(state.getEndpointAccessToken()).thenReturn("");

        DetachEndpointFromUserCallback listener = mock(DetachEndpointFromUserCallback.class);

        DefaultEndpointRegistrationManager manager = new DefaultEndpointRegistrationManager(state, executorContext, null, null);
        manager.setDetachedCallback(null);
        manager.onUpdate(null, null, null, null, new UserDetachNotification("foo"));

        manager.setDetachedCallback(listener);
        manager.onUpdate(null, null, null, null, new UserDetachNotification("foo"));


        verify(listener, Mockito.timeout(1000).times(1)).onDetachedFromUser("foo");
        verify(state, Mockito.timeout(1000).times(2)).setAttachedToUser(false);
    }

}
