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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.TopicSubscriptionInfo;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaaClientPropertiesState implements KaaClientState {

    private static final String APP_STATE_SEQ_NUMBER = "APP_STATE_SEQ_NUMBER";
    private static final String CONFIGURATION_HASH = "CONFIGURATION_HASH";
    private static final String PROFILE_HASH = "PROFILE_HASH";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(KaaClientPropertiesState.class);

    private static final String NF_SUBSCRIPTIONS = "nf_subscriptions";
    private static final String IS_REGISTERED = "is_registered";
    private static final String STATE_FILE_LOCATION = "state.file_location";
    private static final String CLIENT_PRIVATE_KEY_FILE_LOCATION = "keys.private";
    private static final String CLIENT_PUBLIC_KEY_FILE_LOCATION = "keys.public";

    private static final String STATE_FILE_DEFAULT = "state.properties";
    private static final String CLIENT_PRIVATE_KEY_DEFAULT = "key.private";
    private static final String CLIENT_PUBLIC_KEY_DEFAULT = "key.public";

    private final KaaClientProperties state;
    private final String stateFileLocation;
    private final String privateKeyFileLocation;
    private final String clientPublicKeyFileLocation;
    private final Map<String, TopicSubscriptionInfo> nfSubscriptions = new HashMap<String, TopicSubscriptionInfo>();

    public KaaClientPropertiesState(KaaClientProperties properties) {
        super();
        stateFileLocation = properties.containsKey(STATE_FILE_LOCATION) ?
                properties.getProperty(STATE_FILE_LOCATION) :
                STATE_FILE_DEFAULT;

        privateKeyFileLocation = properties.containsKey(CLIENT_PRIVATE_KEY_FILE_LOCATION) ?
                properties.getProperty(CLIENT_PRIVATE_KEY_FILE_LOCATION) :
                CLIENT_PRIVATE_KEY_DEFAULT;

        clientPublicKeyFileLocation = properties.containsKey(CLIENT_PUBLIC_KEY_FILE_LOCATION) ?
                properties.getProperty(CLIENT_PUBLIC_KEY_FILE_LOCATION) :
                CLIENT_PUBLIC_KEY_DEFAULT;

        state = new KaaClientProperties();
        File stateFile = new File(stateFileLocation);
        if (stateFile.exists()) {
            try {
                state.load(new FileInputStream(stateFile));

                BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(state.getProperty(NF_SUBSCRIPTIONS).getBytes(), null);
                SpecificDatumReader<TopicSubscriptionInfo> avroReader =
                        new SpecificDatumReader<TopicSubscriptionInfo>(TopicSubscriptionInfo.class);

                try {
                    TopicSubscriptionInfo decodedInfo = null;

                    //FIXME: research avro documentation for more convenient approach of iteration through encoded record
                    while (true) {
                      decodedInfo = avroReader.read(null, decoder);
                      LOG.info("Loaded {}", decodedInfo);
                      nfSubscriptions.put(decodedInfo.getTopicInfo().getId(), decodedInfo);
                    }
                } catch (Exception e) {

                }
            } catch (IOException e) {
                LOG.error("Can't load state file", e);
            }
        }
    }

    @Override
    public boolean isRegistered() {
        return Boolean.parseBoolean(state.getProperty(IS_REGISTERED,
                Boolean.FALSE.toString()));
    }

    @Override
    public void setRegistered(boolean registered) {
        state.setProperty(IS_REGISTERED, Boolean.toString(registered));
    }

    @Override
    public void persist() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        SpecificDatumWriter<TopicSubscriptionInfo> datumWriter =
                new SpecificDatumWriter<TopicSubscriptionInfo>(TopicSubscriptionInfo.class);

        try {
            for (Map.Entry<String, TopicSubscriptionInfo> cursor : nfSubscriptions.entrySet()) {
                datumWriter.write(cursor.getValue(), encoder);
                LOG.info("Persisted {}", cursor.getValue());
            }

            encoder.flush();
            state.setProperty(NF_SUBSCRIPTIONS, baos.toString());
        } catch (IOException e) {
            LOG.error("Can't persist notification subscription info", e);
        }

        try {
            new File(stateFileLocation).renameTo(new File(stateFileLocation+ "_bckp"));
            File stateFile = new File(stateFileLocation);
            OutputStream os = new FileOutputStream(stateFile);
            state.store(os, null);
            os.close();
        } catch (IOException e) {
            LOG.error("Can't persist state file", e);
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        PrivateKey privateKey = null;
        File f = new File(privateKeyFileLocation);
        if (f.exists()) {
            try {
                privateKey = KeyUtil.getPrivate(f);
            } catch (Exception e) {
                LOG.info("Error loading Client Private Key", e);
                throw new RuntimeException(e);
            }
        }
        if (privateKey == null) {
            LOG.info("Generating Client Key pair");
            KeyPair kp = KeyUtil.generateKeyPair(privateKeyFileLocation, clientPublicKeyFileLocation);
            privateKey = kp.getPrivate();
        }
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        PublicKey publicKey = null;
        File f = new File(clientPublicKeyFileLocation);
        if (f.exists()) {
            try {
                publicKey = KeyUtil.getPublic(f);
            } catch (Exception e) {
                LOG.info("Error loading Client Public Key", e);
                throw new RuntimeException(e);
            }
        }
        if (publicKey == null) {
            LOG.info("Generating Client Key pair");
            KeyPair kp = KeyUtil.generateKeyPair(privateKeyFileLocation, clientPublicKeyFileLocation);
            publicKey = kp.getPublic();
        }
        return publicKey;
    }

    @Override
    public int getAppStateSeqNumber() {
        return Integer.parseInt(state.getProperty(APP_STATE_SEQ_NUMBER, "1"));
    }

    @Override
    public EndpointObjectHash getConfigurationHash() {
        return EndpointObjectHash.fromBytes(Base64.decodeBase64(state.getProperty(CONFIGURATION_HASH, Base64.encodeBase64String(new byte[0]))));
    }

    @Override
    public EndpointObjectHash getProfileHash() {
        return EndpointObjectHash.fromBytes(Base64.decodeBase64(state.getProperty(PROFILE_HASH, Base64.encodeBase64String(new byte[0]))));
    }

    @Override
    public void setAppStateSeqNumber(int appStateSeqNumber) {
        state.setProperty(APP_STATE_SEQ_NUMBER, Integer.toString(appStateSeqNumber));
    }

    @Override
    public void setConfigurationHash(EndpointObjectHash hash) {
        state.setProperty(CONFIGURATION_HASH, Base64.encodeBase64String(hash.getData()));
    }

    @Override
    public void setProfileHash(EndpointObjectHash hash) {
        state.setProperty(PROFILE_HASH, Base64.encodeBase64String(hash.getData()));
    }

    @Override
    public void addTopic(Topic topic) {
        TopicSubscriptionInfo subscriptionInfo = nfSubscriptions.get(topic.getId());

        if (subscriptionInfo == null) {
            subscriptionInfo = TopicSubscriptionInfo.newBuilder().setTopicInfo(topic).setSeqNumber(0).build();
            nfSubscriptions.put(topic.getId(), subscriptionInfo);
            LOG.info("Adding new seqNumber 0 for {} subscription", topic.getId());
        }
    }

    @Override
    public void removeTopic(String topicId) {
        if (nfSubscriptions.remove(topicId) != null) {
            LOG.info("Removed subscription info for {}", topicId);
        }
    }

    @Override
    public void updateTopicSubscriptionInfo(String topicId, Integer sequenceNumber) {
        TopicSubscriptionInfo subscriptionInfo = nfSubscriptions.get(topicId);

        if (subscriptionInfo != null) {
            subscriptionInfo.setSeqNumber(Math.max(sequenceNumber, subscriptionInfo.getSeqNumber()));
            nfSubscriptions.put(topicId, subscriptionInfo);
            LOG.info("Updated seqNumber to {} for {} subscription", subscriptionInfo.getSeqNumber(), topicId);
        }
    }

    @Override
    public Map<String, Integer> getNfSubscriptions() {
        HashMap<String, Integer> subscriptions = new HashMap<String, Integer>();

        for (Map.Entry<String, TopicSubscriptionInfo> cursor : nfSubscriptions.entrySet()) {
            subscriptions.put(cursor.getKey(), cursor.getValue().getSeqNumber());
        }

        return subscriptions;
    }

    @Override
    public List<Topic> getTopics() {
        List<Topic> topics = new LinkedList<Topic>();

        for (Map.Entry<String, TopicSubscriptionInfo> cursor : nfSubscriptions.entrySet()) {
            topics.add(cursor.getValue().getTopicInfo());
        }

        return topics;
    }
}
