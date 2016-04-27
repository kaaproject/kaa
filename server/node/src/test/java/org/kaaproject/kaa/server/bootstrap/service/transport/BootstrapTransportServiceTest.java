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

package org.kaaproject.kaa.server.bootstrap.service.transport;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.NoSuchPaddingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import static org.mockito.Mockito.*;

public class BootstrapTransportServiceTest {
    private KeyPairGenerator keyPairGenerator;
    private static final String RSA = "RSA";

    @Before
    public void before() throws NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException {
        keyPairGenerator = KeyPairGenerator.getInstance(RSA);
    }

    @Test
    public void messageHandlerProcessTest() {
        BootstrapTransportService bService = new BootstrapTransportService();
        KeyStoreService keyStoreService = mock(KeyStoreService.class);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        when(keyStoreService.getPublicKey()).thenReturn(publicKey);
        when(keyStoreService.getPrivateKey()).thenReturn(privateKey);
        ReflectionTestUtils.setField(bService, "supportUnencryptedConnection", true);
        ReflectionTestUtils.setField(bService, "bootstrapKeyStoreService", keyStoreService);
        ReflectionTestUtils.setField(bService, "properties", new Properties());
        bService.lookupAndInit();
        MessageHandler handler = (BootstrapTransportService.BootstrapMessageHandler) ReflectionTestUtils.getField(bService, "handler");
        SessionInitMessage encryptedSessionInitMessage = mockForSessionInitMessage(true);
        SessionInitMessage nonEncryptedSessionInitMessage = mockForSessionInitMessage(false);
        handler.process(encryptedSessionInitMessage);
        handler.process(nonEncryptedSessionInitMessage);
        verify(encryptedSessionInitMessage, timeout(1000)).getEncodedMessageData();
        verify(nonEncryptedSessionInitMessage, timeout(1000)).getEncodedMessageData();
    }

    private SessionInitMessage mockForSessionInitMessage(boolean isEncrypted) {
        SessionInitMessage message = mock(SessionInitMessage.class);
        when(message.isEncrypted()).thenReturn(isEncrypted);
        when(message.getEncodedMessageData()).thenReturn(new byte[]{2, 3, 4, 5});
        when(message.getEncodedSessionKey()).thenReturn(new byte[]{10, 11, 12, 13});
        when(message.getChannelContext()).thenReturn(mock(ChannelContext.class));
        when(message.getErrorBuilder()).thenReturn(mock(ErrorBuilder.class));
        return message;
    }
}
