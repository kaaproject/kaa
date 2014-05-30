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

package org.kaaproject.kaa.client.update.listeners;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.bootstrap.OperationsServerInfo;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.transport.OperationsTransport;
import org.kaaproject.kaa.client.update.UpdateManager;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

public class RedirectionUpdateListenerTest {

    @Test
    public void test() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        BootstrapManager bootstrapManager = mock(BootstrapManager.class);
        UpdateManager updateManager = mock(UpdateManager.class);
        KaaClientState state = mock(KaaClientState.class);

        // Generating pseudo bootstrap key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        OperationsServerInfo info = new OperationsServerInfo("some.name", keyPair.getPublic().getEncoded());

        when(bootstrapManager.getOperationsServerByDnsName(any(String.class))).thenReturn(info);

        RedirectionUpdateListener listener = new RedirectionUpdateListener(bootstrapManager, updateManager, state);

        SyncResponse response = new SyncResponse();
        listener.onDeltaUpdate(response);
        response.setResponseType(SyncResponseStatus.REDIRECT);
        RedirectSyncResponse redirectionResponse = new RedirectSyncResponse();
        redirectionResponse.setDnsName("some.name");
        response.setRedirectSyncResponse(redirectionResponse);
        listener.onDeltaUpdate(response);

        verify(updateManager, times(1)).setTransport(any(OperationsTransport.class));
        verify(bootstrapManager, times(1)).getOperationsServerByDnsName(eq("some.name"));
    }

}
