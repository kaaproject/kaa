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

package org.kaaproject.kaa.client.persistance;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.exceptions.KaaRuntimeException;
import org.kaaproject.kaa.client.persistence.FilePersistentStorage;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.util.CommonsBase64;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class KaaClientPropertiesStateTest {

    private static final String WORK_DIR = "work_dir" + System.getProperty("file.separator");
    private static final String KEY_PUBLIC = "key.public";
    private static final String KEY_PRIVATE = "key.private";
    private static final String STATE_PROPERTIES = "state.properties";
    private static final String STATE_PROPERTIES_BCKP = "state.properties_bckp";

    public static KaaClientProperties getProperties() throws IOException {
        KaaClientProperties props = new KaaClientProperties();
        props.setProperty(KaaClientProperties.WORKING_DIR_PROPERTY, WORK_DIR);
        props.setProperty(KaaClientProperties.STATE_FILE_NAME_PROPERTY, STATE_PROPERTIES);
        props.setProperty(KaaClientProperties.CLIENT_PUBLIC_KEY_FILE_NAME_PROPERTY, KEY_PUBLIC);
        File dir = new File(WORK_DIR);
        dir.deleteOnExit();
        File pub = new File(WORK_DIR + KEY_PUBLIC);
        pub.deleteOnExit();
        props.setProperty(KaaClientProperties.CLIENT_PRIVATE_KEY_FILE_NAME_PROPERTY, KEY_PRIVATE);
        File priv = new File(WORK_DIR + KEY_PRIVATE);
        priv.deleteOnExit();
        File state = new File(WORK_DIR + STATE_PROPERTIES);
        state.deleteOnExit();
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_DELAY, "0");
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_PERIOD, "1");
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_UNIT, "SECONDS");
        props.setProperty(KaaClientProperties.SDK_TOKEN, "123456");
        return props;
    }

    @Test(expected = KaaRuntimeException.class)
    public void testInitKeys() throws IOException, InvalidKeyException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(),
                getProperties());

        assertNull(state.getPrivateKey());
        assertNull(state.getPublicKey());
    }

    @Test
    public void testGenerateKeys() throws IOException, InvalidKeyException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(),
                getProperties(), true);

        assertNotNull(state.getPrivateKey());
        assertNotNull(state.getPublicKey());
    }

    @Test
    public void testDefaultStrategyKeys() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(),
                getProperties());

        PersistentStorage storage = new FilePersistentStorage();
        String clientPrivateKeyFileLocation = getProperties().getPrivateKeyFileFullName();
        String clientPublicKeyFileLocation = getProperties().getPublicKeyFileFullName();

        OutputStream privateKeyOutput = storage.openForWrite(clientPrivateKeyFileLocation);
        OutputStream publicKeyOutput = storage.openForWrite(clientPublicKeyFileLocation);
        KeyPair keyPair = KeyUtil.generateKeyPair(privateKeyOutput, publicKeyOutput);

        assertArrayEquals(keyPair.getPrivate().getEncoded(), state.getPrivateKey().getEncoded());
        assertArrayEquals(keyPair.getPublic().getEncoded(), state.getPublicKey().getEncoded());

        //clean
        new File(WORK_DIR + KEY_PUBLIC).delete();
        new File(WORK_DIR + KEY_PRIVATE).delete();
    }

    @Test(expected = KaaRuntimeException.class)
    public void testDefaultStrategyRecreateKeys() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(),
                getProperties());

        PersistentStorage storage = new FilePersistentStorage();
        String clientPrivateKeyFileLocation = getProperties().getPrivateKeyFileFullName();
        String clientPublicKeyFileLocation = getProperties().getPublicKeyFileFullName();

        OutputStream privateKeyOutput = storage.openForWrite(clientPrivateKeyFileLocation);
        OutputStream publicKeyOutput = storage.openForWrite(clientPublicKeyFileLocation);
        KeyPair keyPair = KeyUtil.generateKeyPair(privateKeyOutput, publicKeyOutput);

        assertArrayEquals(keyPair.getPrivate().getEncoded(), state.getPrivateKey().getEncoded());
        assertArrayEquals(keyPair.getPublic().getEncoded(), state.getPublicKey().getEncoded());

        File pub = new File(WORK_DIR + KEY_PUBLIC);
        File priv = new File(WORK_DIR + KEY_PRIVATE);

        //clean
        Files.delete(Paths.get(WORK_DIR + KEY_PUBLIC));
        new File(WORK_DIR + KEY_PRIVATE).delete();
        state.clean();

        state.getPublicKey();
        state.getPrivateKey();
    }

    @Test(expected = KaaRuntimeException.class)
    public void testInitKeys2() throws IOException, InvalidKeyException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(),
                getProperties());

        assertNull(state.getPrivateKey());
        assertNull(state.getPublicKey());
    }

    @Test
    public void testRecreateKeys() throws IOException, InvalidKeyException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(),
                getProperties(), true);

        state.getPublicKey();
        state.getPrivateKey();

        File pub = new File(WORK_DIR + KEY_PUBLIC);
        File priv = new File(WORK_DIR + KEY_PRIVATE);

        assertArrayEquals(KeyUtil.getPrivate(priv).getEncoded(), state.getPrivateKey().getEncoded());
        assertArrayEquals(KeyUtil.getPublic(pub).getEncoded(), state.getPublicKey().getEncoded());

        pub.delete();
        priv.delete();

        assertNotNull(state.getPublicKey());
        assertNotNull(state.getPrivateKey());
    }

    @Test
    public void testProfileHash() throws IOException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());
        EndpointObjectHash hash = EndpointObjectHash.fromSHA1(new byte[]{1, 2, 3});
        state.setProfileHash(hash);
        assertEquals(hash, state.getProfileHash());
    }

    @Test
    public void testNfSubscription() throws IOException {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());

        Topic topic1 = Topic.newBuilder().setId(1234).setName("testName")
                .setSubscriptionType(SubscriptionType.OPTIONAL_SUBSCRIPTION).build();

        Topic topic2 = Topic.newBuilder().setId(4321).setName("testName")
                .setSubscriptionType(SubscriptionType.MANDATORY_SUBSCRIPTION).build();

        state.addTopic(topic1);
        state.addTopic(topic2);

        state.updateTopicSubscriptionInfo(topic2.getId(), 1);
        state.updateTopicSubscriptionInfo(topic1.getId(), 0);
        state.updateTopicSubscriptionInfo(topic1.getId(), 1);

        Map<Long, Integer> expected = new HashMap<>();
        expected.put(topic2.getId(), 1);

        assertEquals(expected, state.getNfSubscriptions());

        state.persist();
        state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());

        assertEquals(expected, state.getNfSubscriptions());

        state.addTopicSubscription(topic1.getId());

        expected.put(topic1.getId(), 0);
        assertEquals(expected, state.getNfSubscriptions());

        state.updateTopicSubscriptionInfo(topic1.getId(), 5);
        expected.put(topic1.getId(), 5);
        assertEquals(expected, state.getNfSubscriptions());

        state.removeTopic(topic1.getId());
        state.persist();

        state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());
        expected.remove(topic1.getId());

        assertEquals(expected, state.getNfSubscriptions());
    }

    @Test
    public void testSDKPropertiesUpdate() throws IOException {
        KaaClientProperties props = getProperties();
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), props);

        Assert.assertFalse(state.isRegistered());

        state.setRegistered(true);
        state.persist();

        Assert.assertTrue(state.isRegistered());

        KaaClientProperties newProps = getProperties();
        newProps.setProperty(KaaClientProperties.SDK_TOKEN, "SDK_TOKEN_100500");

        KaaClientState newState = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), newProps);

        Assert.assertFalse(newState.isRegistered());
    }

    @Test
    @Ignore
    public void testConfigVersionUpdates() throws Exception {
        KaaClientProperties props = getProperties();
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), props);

        Assert.assertFalse(state.isConfigurationVersionUpdated());

        state.persist();

        KaaClientProperties newProps = getProperties();
        newProps.setProperty(KaaClientProperties.SDK_TOKEN, "SDK_TOKEN_100500");

        KaaClientState newState = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), newProps);

        Assert.assertTrue(newState.isConfigurationVersionUpdated());
    }

    @Test
    public void testNeedProfileResync() throws Exception {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());
        Assert.assertFalse(state.isNeedProfileResync());

        state.setIfNeedProfileResync(true);
        Assert.assertTrue(state.isNeedProfileResync());

        state.persist();
        state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());
        Assert.assertTrue(state.isNeedProfileResync());

        state.setIfNeedProfileResync(false);
        Assert.assertFalse(state.isNeedProfileResync());
    }

    @Test
    public void testClean() throws Exception {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), CommonsBase64.getInstance(), getProperties());
        File stateProps = new File(WORK_DIR + STATE_PROPERTIES);
        File statePropsBckp = new File(WORK_DIR + STATE_PROPERTIES_BCKP);
        statePropsBckp.deleteOnExit();
        state.persist();
        state.setRegistered(true);
        state.persist();
        assertTrue(stateProps.exists());
        assertTrue(statePropsBckp.exists());
        state.clean();
        assertFalse(stateProps.exists());
        assertFalse(statePropsBckp.exists());
    }
}
