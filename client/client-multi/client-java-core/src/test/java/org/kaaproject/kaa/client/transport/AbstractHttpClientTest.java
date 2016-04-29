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

package org.kaaproject.kaa.client.transport;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;

public class AbstractHttpClientTest {
    private class TestHttpClient extends AbstractHttpClient {
        public TestHttpClient(String url, PrivateKey privateKey, PublicKey publicKey, PublicKey remotePublicKey) {
            super(url, privateKey, publicKey, remotePublicKey);
        }

        @Override
        public byte[] executeHttpRequest(String uri, LinkedHashMap<String, byte[]> entity
                , boolean verifyResponse) throws Exception {
            return null;
        }
        @Override
        public void close() throws IOException {}

        @Override
        public void abort() {}

        @Override
        public boolean canAbort() { return false; }
    }

    @Test
    public void testDisableVerification() throws GeneralSecurityException  {
        TestHttpClient client = new TestHttpClient("test_url", null, null, null);
        client.disableVerification();

        byte[] body = {1, 2, 3};
        byte[] signature = {1, 2, 3};
        Assert.assertArrayEquals(body, client.verifyResponse(body, signature));
    }

    @Test(expected=GeneralSecurityException.class)
    public void testVerifyResponseFailure() throws NoSuchAlgorithmException, GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(512);

        KeyPair clientKeyPair = gen.generateKeyPair();
        KeyPair remoteKeyPair = gen.generateKeyPair();

        TestHttpClient client = new TestHttpClient("test_url", clientKeyPair.getPrivate()
                , clientKeyPair.getPublic(), remoteKeyPair.getPublic());

        byte[] body = {1, 2, 3};
        byte[] signature = {1, 2, 3};
        client.verifyResponse(body, signature);
    }

    @Test
    public void testSignature() throws NoSuchAlgorithmException, GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(512);

        KeyPair clientKeyPair = gen.generateKeyPair();
        KeyPair remoteKeyPair = gen.generateKeyPair();

        TestHttpClient client = new TestHttpClient("test_url", clientKeyPair.getPrivate()
                , clientKeyPair.getPublic(), remoteKeyPair.getPublic());

        MessageEncoderDecoder serverEncoder = new MessageEncoderDecoder(
                        remoteKeyPair.getPrivate(), remoteKeyPair.getPublic());

        byte[] message = {1, 2, 3};
        byte[] signature = serverEncoder.sign(message);

        Assert.assertArrayEquals(message, client.verifyResponse(message, signature));
        Assert.assertTrue(client.getEncoderDecoder().verify(message, signature));
    }
}
