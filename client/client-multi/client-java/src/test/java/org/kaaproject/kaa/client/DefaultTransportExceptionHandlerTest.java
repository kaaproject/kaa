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

package org.kaaproject.kaa.client;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class DefaultTransportExceptionHandlerTest {

    @Test
    public void testHandler() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        KeyPair clientKeyPair = clientKeyGen.genKeyPair();

        BootstrapManager bootstrapManager = mock(BootstrapManager.class);
        when(bootstrapManager.getNextOperationsServer()).thenReturn(new OperationsServerInfo("localhost:9889", clientKeyPair.getPublic().getEncoded()));
        UpdateManager updateManager = mock(UpdateManager.class);
        KaaClientState state = mock(KaaClientState.class);
        when(state.getPrivateKey()).thenReturn(clientKeyPair.getPrivate());
        when(state.getPublicKey()).thenReturn(clientKeyPair.getPublic());

        TransportExceptionHandler handler = new DefaultTransportExceptionHandler(bootstrapManager, updateManager, state);
        handler.onTransportException();
        when(bootstrapManager.getNextOperationsServer()).thenReturn(null);
        handler.onTransportException();
        verify(bootstrapManager, times(2)).getNextOperationsServer();
        verify(updateManager, times(1)).setTransport(any(OperationsTransport.class));
        verify(updateManager, times(2)).failover(any(Long.class));
    }

}
