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
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.compress.utils.Charsets;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.TopicSubscriptionInfo;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaaClientPropertiesState implements KaaClientState {

    private static final String APP_STATE_SEQ_NUMBER = "APP_STATE_SEQ_NUMBER";
    private static final String CONFIG_SEQ_NUMBER = "CONFIG_SEQ_NUMBER";
    private static final String NOTIFICATION_SEQ_NUMBER = "NOTIFICATION_SEQ_NUMBER";
    private static final String CONFIGURATION_HASH = "CONFIGURATION_HASH";
    private static final String PROFILE_HASH = "PROFILE_HASH";
    private static final String ENDPOINT_ACCESS_TOKEN = "ENDPOINT_TOKEN";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(KaaClientPropertiesState.class);

    private static final String ATTACHED_ENDPOINTS = "attached_eps";
    private static final String NF_SUBSCRIPTIONS = "nf_subscriptions";
    private static final String IS_REGISTERED = "is_registered";
    private static final String STATE_FILE_LOCATION = "state.file_location";
    private static final String CLIENT_PRIVATE_KEY_FILE_LOCATION = "keys.private";
    private static final String CLIENT_PUBLIC_KEY_FILE_LOCATION = "keys.public";

    private static final String STATE_FILE_DEFAULT = "state.properties";
    private static final String CLIENT_PRIVATE_KEY_DEFAULT = "key.private";
    private static final String CLIENT_PUBLIC_KEY_DEFAULT = "key.public";

    private final PersistentStorage storage;
    private final KaaClientProperties state;
    private final String stateFileLocation;
    private final String privateKeyFileLocation;
    private final String clientPublicKeyFileLocation;
    private final Map<String, TopicSubscriptionInfo> nfSubscriptions = new HashMap<String, TopicSubscriptionInfo>();
    private final Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints = new HashMap<EndpointAccessToken, EndpointKeyHash>();
    private final String endpointAccessToken = new String();

    public KaaClientPropertiesState(PersistentStorage storage, KaaClientProperties properties) {
        super();
        this.storage = storage;
        stateFileLocation = properties.containsKey(STATE_FILE_LOCATION) ?
                properties.getProperty(STATE_FILE_LOCATION) :
                STATE_FILE_DEFAULT;

        privateKeyFileLocation = properties.containsKey(CLIENT_PRIVATE_KEY_FILE_LOCATION) ?
                properties.getProperty(CLIENT_PRIVATE_KEY_FILE_LOCATION) :
                CLIENT_PRIVATE_KEY_DEFAULT;

        clientPublicKeyFileLocation = properties.containsKey(CLIENT_PUBLIC_KEY_FILE_LOCATION) ?
                properties.getProperty(CLIENT_PUBLIC_KEY_FILE_LOCATION) :
                CLIENT_PUBLIC_KEY_DEFAULT;

        state = new KaaClientProperties(properties);
        if (storage.exists(stateFileLocation)) {
            try {
                state.load(storage.openForRead(stateFileLocation));

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

                String attachedEndpointsString = state.getProperty(ATTACHED_ENDPOINTS);
                String [] splittedEndpointsList = attachedEndpointsString.split(",");
                for (String attachedEndpoint : splittedEndpointsList) {
                    if (!attachedEndpoint.isEmpty()) {
                        String[] splittedValues = attachedEndpoint.split(":");
                        attachedEndpoints.put(new EndpointAccessToken(splittedValues[0])
                            , new EndpointKeyHash(splittedValues[1]));
                    }
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

        StringBuilder attachedEndpointsString = new StringBuilder();
        for (Map.Entry<EndpointAccessToken, EndpointKeyHash> attached : attachedEndpoints.entrySet()) {
            attachedEndpointsString.append(attached.getKey().getToken())
                .append(":").append(attached.getValue().getKeyHash()).append(',');
        }
        state.setProperty(ATTACHED_ENDPOINTS, attachedEndpointsString.toString());

        try {
            storage.renameTo(stateFileLocation, stateFileLocation+ "_bckp");
            OutputStream os = storage.openForWrite(stateFileLocation);
            state.store(os, null);
            os.close();
        } catch (IOException e) {
            LOG.error("Can't persist state file", e);
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        PrivateKey privateKey = null;
        if (storage.exists(privateKeyFileLocation)) {
            try {
                InputStream input = storage.openForRead(privateKeyFileLocation);
                privateKey = KeyUtil.getPrivate(input);
            } catch (Exception e) {
                LOG.info("Error loading Client Private Key", e);
                throw new RuntimeException(e); //NOSONAR
            }
        }
        if (privateKey == null) {
            LOG.info("Generating Client Key pair");
            try {
                OutputStream privateKeyOutput = storage.openForWrite(privateKeyFileLocation);
                OutputStream publicKeyOutput = storage.openForWrite(clientPublicKeyFileLocation);
                KeyPair kp = KeyUtil.generateKeyPair(privateKeyOutput, publicKeyOutput);
                privateKey = kp.getPrivate();
            }
            catch (IOException e) {
                LOG.info("Error generating Client Key pair", e);
                throw new RuntimeException(e);
            }
        }
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        PublicKey publicKey = null;
        if (storage.exists(clientPublicKeyFileLocation)) {
            try {
                InputStream input = storage.openForRead(clientPublicKeyFileLocation);
                publicKey = KeyUtil.getPublic(input);
            } catch (Exception e) {
                LOG.info("Error loading Client Public Key", e);
                throw new RuntimeException(e); //NOSONAR
            }
        }
        if (publicKey == null) {
            LOG.info("Generating Client Key pair");
            try {
                OutputStream privateKeyOutput = storage.openForWrite(privateKeyFileLocation);
                OutputStream publicKeyOutput = storage.openForWrite(clientPublicKeyFileLocation);
                KeyPair kp = KeyUtil.generateKeyPair(privateKeyOutput, publicKeyOutput);
                publicKey = kp.getPublic();
            }
            catch (IOException e) {
                LOG.info("Error generating Client Key pair", e);
                throw new RuntimeException(e);
            }
        }
        return publicKey;
    }

    @Override
    public int getAppStateSeqNumber() {
        return Integer.parseInt(state.getProperty(APP_STATE_SEQ_NUMBER, "1"));
    }

    @Override
    public EndpointObjectHash getConfigurationHash() {
        return EndpointObjectHash.fromBytes(Base64.decodeBase64(state.getProperty(CONFIGURATION_HASH, new String(Base64.encodeBase64(new byte[0]), Charsets.UTF_8)).getBytes(Charsets.UTF_8)));
    }

    @Override
    public EndpointObjectHash getProfileHash() {
        return EndpointObjectHash.fromBytes(Base64.decodeBase64(state.getProperty(PROFILE_HASH, new String(Base64.encodeBase64(new byte[0]), Charsets.UTF_8)).getBytes(Charsets.UTF_8)));
    }

    @Override
    public void setAppStateSeqNumber(int appStateSeqNumber) {
        state.setProperty(APP_STATE_SEQ_NUMBER, Integer.toString(appStateSeqNumber));
    }

    @Override
    public void setConfigurationHash(EndpointObjectHash hash) {
        state.setProperty(CONFIGURATION_HASH, new String(Base64.encodeBase64(hash.getData()), Charsets.UTF_8));
    }

    @Override
    public void setProfileHash(EndpointObjectHash hash) {
        state.setProperty(PROFILE_HASH, new String(Base64.encodeBase64(hash.getData()), Charsets.UTF_8));
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

    @Override
    public void setAttachedEndpointsList(Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints) {
        this.attachedEndpoints.clear();
        this.attachedEndpoints.putAll(attachedEndpoints);
    }

    @Override
    public Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointsList() {
        return attachedEndpoints;
    }

    @Override
    public void setEndpointAccessToken(String token) {
        state.setProperty(ENDPOINT_ACCESS_TOKEN, token);
    }

    @Override
    public String getEndpointAccessToken() {
        return state.getProperty(ENDPOINT_ACCESS_TOKEN, "");
    }

    @Override
    public void setConfigSeqNumber(int configSeqNumber) {
        state.setProperty(CONFIG_SEQ_NUMBER, Integer.toString(configSeqNumber));
    }

    @Override
    public int getConfigSeqNumber() {
        return Integer.parseInt(state.getProperty(CONFIG_SEQ_NUMBER, "1"));
    }

    @Override
    public void setNotificationSeqNumber(int notificationSeqNumber) {
        state.setProperty(NOTIFICATION_SEQ_NUMBER, Integer.toString(notificationSeqNumber));
    }

    @Override
    public int getNotificationSeqNumber() {
        return Integer.parseInt(state.getProperty(NOTIFICATION_SEQ_NUMBER, "1"));
    }
}
