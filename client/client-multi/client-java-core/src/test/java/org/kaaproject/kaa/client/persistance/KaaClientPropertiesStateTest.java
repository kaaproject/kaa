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

package org.kaaproject.kaa.client.persistance;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.persistence.FilePersistentStorage;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class KaaClientPropertiesStateTest {

    public static KaaClientProperties getProperties() throws IOException {
        KaaClientProperties props = new KaaClientProperties();
        props.setProperty("state.file_location", "state.properties");
        props.setProperty("keys.public", "key.public");
        File pub = new File("key.public");
        pub.deleteOnExit();
        props.setProperty("keys.private", "key.private");
        File priv = new File("key.private");
        priv.deleteOnExit();
        File state = new File("state.properties");
        state.deleteOnExit();
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_DELAY, "0");
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_PERIOD, "1");
        props.setProperty(KaaClientProperties.TRANSPORT_POLL_UNIT, "SECONDS");
        props.setProperty(KaaClientProperties.CONFIG_VERSION, "1");
        props.setProperty(KaaClientProperties.PROFILE_VERSION, "1");
        props.setProperty(KaaClientProperties.SYSTEM_NT_VERSION, "1");
        props.setProperty(KaaClientProperties.USER_NT_VERSION, "1");
        props.setProperty(KaaClientProperties.APPLICATION_TOKEN, "123456");
        return props;
    }

    @Test
    public void testKeys() throws Exception {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), getProperties());
        state.getPublicKey();
        state.getPrivateKey();
        File pub = new File("key.public");
        File priv = new File("key.private");
        assertArrayEquals(KeyUtil.getPrivate(priv).getEncoded(), state.getPrivateKey().getEncoded());
        assertArrayEquals(KeyUtil.getPublic(pub).getEncoded(), state.getPublicKey().getEncoded());
        pub.delete();
        priv.delete();
        state.getPrivateKey();
        state.getPublicKey();
        assertArrayEquals(KeyUtil.getPrivate(priv).getEncoded(), state.getPrivateKey().getEncoded());
        assertArrayEquals(KeyUtil.getPublic(pub).getEncoded(), state.getPublicKey().getEncoded());
    }

    @Test
    public void testProfileHash() throws IOException  {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), getProperties());
        EndpointObjectHash hash = EndpointObjectHash.fromSHA1(new byte[]{1, 2, 3});
        state.setProfileHash(hash);
        assertEquals(hash, state.getProfileHash());
    }

    @Test
    public void testConfigHash() throws IOException  {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), getProperties());
        EndpointObjectHash hash = EndpointObjectHash.fromSHA1(new byte[]{1, 2, 3});
        state.setConfigurationHash(hash);
        assertEquals(hash, state.getConfigurationHash());
    }

    @Test
    public void testNfSubscription() throws IOException  {
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), getProperties());

        Topic topic1 = Topic.newBuilder().setId("1234").setName("testName")
                .setSubscriptionType(SubscriptionType.VOLUNTARY).build();

        Topic topic2 = Topic.newBuilder().setId("4321").setName("testName")
                .setSubscriptionType(SubscriptionType.MANDATORY).build();

        state.addTopic(topic1);
        state.addTopic(topic2);

        state.updateTopicSubscriptionInfo(topic2.getId(), 1);

        state.updateTopicSubscriptionInfo(topic1.getId(), 0);
        state.updateTopicSubscriptionInfo(topic1.getId(), 5);
        state.updateTopicSubscriptionInfo(topic1.getId(), 1);

        Map<String, Integer> expected = new HashMap<String, Integer>();
        expected.put(topic1.getId(), 5);
        expected.put(topic2.getId(), 1);

        assertEquals(expected, state.getNfSubscriptions());

        state.persist();
        state = new KaaClientPropertiesState(new FilePersistentStorage(), getProperties());

        assertEquals(expected, state.getNfSubscriptions());

        state.removeTopic(topic1.getId());
        state.persist();

        state = new KaaClientPropertiesState(new FilePersistentStorage(), getProperties());
        expected.remove(topic1.getId());

        assertEquals(expected, state.getNfSubscriptions());
    }

    @Test
    public void testSDKPropertiesUpdate() throws IOException {
        KaaClientProperties props = getProperties();
        KaaClientState state = new KaaClientPropertiesState(new FilePersistentStorage(), props);

        Assert.assertFalse(state.isRegistered());

        state.setRegistered(true);
        state.persist();

        Assert.assertTrue(state.isRegistered());

        KaaClientProperties newProps = getProperties();
        newProps.setProperty(KaaClientProperties.LOG_SCHEMA_VERSION, Integer.toString(100500));

        KaaClientState newState = new KaaClientPropertiesState(new FilePersistentStorage(), newProps);

        Assert.assertFalse(newState.isRegistered());
    }
}
