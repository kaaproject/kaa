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

package org.kaaproject.kaa.common.endpoint.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MessageEncoderDecoderTest {

    PublicKey clientPublic;
    PrivateKey clientPrivate;

    PublicKey serverPublic;
    PrivateKey serverPrivate;

    PublicKey theifPublic;
    PrivateKey theifPrivate;

    @Before
    public void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        KeyPair kp = clientKeyGen.genKeyPair();
        clientPublic = kp.getPublic();
        clientPrivate = kp.getPrivate();

        KeyPairGenerator serverKeyGen = KeyPairGenerator.getInstance("RSA");
        serverKeyGen.initialize(2048);
        kp = serverKeyGen.genKeyPair();
        serverPublic = kp.getPublic();
        serverPrivate = kp.getPrivate();

        KeyPairGenerator otherKeyGen = KeyPairGenerator.getInstance("RSA");
        otherKeyGen.initialize(2048);
        kp = otherKeyGen.genKeyPair();
        theifPublic = kp.getPublic();
        theifPrivate = kp.getPrivate();
    }

    @Test
    public void basicTest() throws Exception {
        String message = "secret" + new Random().nextInt();

        MessageEncoderDecoder client = new MessageEncoderDecoder(clientPrivate, clientPublic, serverPublic);
        MessageEncoderDecoder server = new MessageEncoderDecoder(serverPrivate, serverPublic, clientPublic);
        MessageEncoderDecoder thief = new MessageEncoderDecoder(theifPrivate, theifPublic, clientPublic);

        byte[] secretData = client.encodeData(message.getBytes());
        byte[] signature = client.sign(secretData);
        byte[] encodedSessionKey = client.getEncodedSessionKey();

        Assert.assertTrue(server.verify(secretData, signature));
        String decodedSecret = new String(server.decodeData(secretData, encodedSessionKey));

        Assert.assertEquals(message, decodedSecret);

        byte[] theifData = thief.encodeData(message.getBytes());
        byte[] theifSignature = thief.sign(theifData);
        Assert.assertFalse(server.verify(theifData, theifSignature));
    }

    @Test
    public void basicSubsequentTest() throws Exception {
        String message = "secret" + new Random().nextInt();
        PrivateKey client2Private = theifPrivate;
        PublicKey client2Public = theifPublic;

        MessageEncoderDecoder client = new MessageEncoderDecoder(clientPrivate, clientPublic, serverPublic);
        MessageEncoderDecoder client2 = new MessageEncoderDecoder(client2Private, client2Public, serverPublic);
        MessageEncoderDecoder server = new MessageEncoderDecoder(serverPrivate, serverPublic);


        byte[] secretData = client.encodeData(message.getBytes());
        byte[] signature = client.sign(secretData);
        byte[] encodedSessionKey = client.getEncodedSessionKey();

        server.setRemotePublicKey(clientPublic);
        Assert.assertTrue(server.verify(secretData, signature));
        String decodedSecret = new String(server.decodeData(secretData, encodedSessionKey));

        Assert.assertEquals(message, decodedSecret);

        byte[] secretData2 = client2.encodeData(message.getBytes());
        byte[] signature2 = client2.sign(secretData2);
        byte[] encodedSessionKey2 = client2.getEncodedSessionKey();

        server.setRemotePublicKey(client2Public);
        Assert.assertTrue(server.verify(secretData2, signature2));
        String decodedSecret2 = new String(server.decodeData(secretData2, encodedSessionKey2));

        Assert.assertEquals(message, decodedSecret2);
    }

    @Test
    public void basicUpdateTest() throws Exception {
        MessageEncoderDecoder client = new MessageEncoderDecoder(clientPrivate, clientPublic, serverPublic);

        Assert.assertNotNull(client.getPublicKey());
        Assert.assertNotNull(client.getPrivateKey());
        Assert.assertNotNull(client.getRemotePublicKey());

        byte[] remoteKey = client.getRemotePublicKey().getEncoded();

        client.setRemotePublicKey(serverPublic);

        Assert.assertTrue(Arrays.equals(remoteKey, client.getRemotePublicKey().getEncoded()));

        client.setRemotePublicKey(serverPublic.getEncoded());

        Assert.assertTrue(Arrays.equals(remoteKey, client.getRemotePublicKey().getEncoded()));

        client.setRemotePublicKey(theifPublic.getEncoded());

        Assert.assertFalse(Arrays.equals(remoteKey, client.getRemotePublicKey().getEncoded()));
    }

    @Test
    public void testExistingCipherAlgorithm() {
        Assert.assertNotNull(MessageEncoderDecoder.cipherForAlgorithm("RSA"));
    }

    @Test
    public void testNotExistingCipherAlgorithm() {
        Assert.assertNull(MessageEncoderDecoder.cipherForAlgorithm("42"));
    }

    @Test
    public void testExistingKeyGeneratorAlgorithm() {
        Assert.assertNotNull(MessageEncoderDecoder.keyGeneratorForAlgorithm("AES", 128));
    }

    @Test
    public void testNotExistingKeyGeneratorAlgorithm() {
        Assert.assertNull(MessageEncoderDecoder.keyGeneratorForAlgorithm("42", 128));
    }

    @Test
    public void testExistingSignatoreAlgorithm() {
        Assert.assertNotNull(MessageEncoderDecoder.signatureForAlgorithm("SHA1withRSA"));
    }

    @Test
    public void testNotExistingSignatoreAlgorithm() {
        Assert.assertNull(MessageEncoderDecoder.signatureForAlgorithm("42"));
    }
}
