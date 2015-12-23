package org.kaaproject.kaa.common.endpoint.security;

import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyUtilsTest {

    @Test
    public void validateKeyPairTest() throws Exception {
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        KeyPair kp = clientKeyGen.genKeyPair();
        PublicKey clientPublic = kp.getPublic();
        PrivateKey clientPrivate = kp.getPrivate();

        Assert.assertTrue(KeyUtil.validateKeyPair(new KeyPair(clientPublic, clientPrivate)));
    }
}
