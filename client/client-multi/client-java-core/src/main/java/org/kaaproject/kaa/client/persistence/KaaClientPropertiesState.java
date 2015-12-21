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

package org.kaaproject.kaa.client.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.util.Base64;
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
    private static final String PROFILE_HASH = "PROFILE_HASH";
    private static final String ENDPOINT_ACCESS_TOKEN = "ENDPOINT_TOKEN";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(KaaClientPropertiesState.class);

    private static final String ATTACHED_ENDPOINTS = "attached_eps";
    private static final String NF_SUBSCRIPTIONS = "nf_subscriptions";
    private static final String IS_REGISTERED = "is_registered";
    private static final String IS_ATTACHED = "is_attached";

    private static final String EVENT_SEQ_NUM = "event.seq.num";

    private static final String PROPERTIES_HASH = "properties.hash";

    private final PersistentStorage storage;
    private final Base64 base64;
    private final Properties state;
    private final String stateFileLocation;
    private final String clientPrivateKeyFileLocation;
    private final String clientPublicKeyFileLocation;
    private final Map<String, TopicSubscriptionInfo> nfSubscriptions = new HashMap<String, TopicSubscriptionInfo>();
    private final Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints = new HashMap<EndpointAccessToken, EndpointKeyHash>();
    private final AtomicInteger eventSequence = new AtomicInteger();

    private KeyPair kp;
    private EndpointKeyHash keyHash;
    private boolean isConfigVersionUpdated = false;

    public KaaClientPropertiesState(PersistentStorage storage, Base64 base64, KaaClientProperties properties) {
        super();
        this.storage = storage;
        this.base64 = base64;

        properties.setBase64(base64);

        stateFileLocation = properties.getStateFileFullName();

        clientPrivateKeyFileLocation = properties.getPrivateKeyFileFullName();

        clientPublicKeyFileLocation = properties.getPublicKeyFileFullName();

        LOG.info("Version: '{}', commit hash: '{}'", properties.getBuildVersion(), properties.getCommitHash());

        state = new Properties();
        if (storage.exists(stateFileLocation)) {
            InputStream stream = null;
            try {
                stream = storage.openForRead(stateFileLocation);
                state.load(stream);

                if (isSDKPropertiesUpdated(properties, state)) {
                    LOG.info("SDK properties were updated");
                    setRegistered(false);
                    setPropertiesHash(properties.getPropertiesHash());
                    //TODO: add more intelligent check by comparing part of SDK token.
                    isConfigVersionUpdated = true;
                } else {
                    LOG.info("SDK properties are up to date");
                }

                parseNfSubscriptions();

                String attachedEndpointsString = state.getProperty(ATTACHED_ENDPOINTS);
                if(attachedEndpointsString != null){
                    String[] splittedEndpointsList = attachedEndpointsString.split(",");
                    for (String attachedEndpoint : splittedEndpointsList) {
                        if (!attachedEndpoint.isEmpty()) {
                            String[] splittedValues = attachedEndpoint.split(":");
                            attachedEndpoints.put(new EndpointAccessToken(splittedValues[0]), new EndpointKeyHash(splittedValues[1]));
                        }
                    }
                }

                String eventSeqNumStr = state.getProperty(EVENT_SEQ_NUM);
                if(eventSeqNumStr != null){
                    Integer eventSeqNum = 0;
                    try { // NOSONAR
                        eventSeqNum = Integer.parseInt(eventSeqNumStr);
                    } catch (NumberFormatException e) {
                        LOG.error("Unexpected exception while parsing event sequence number. Can not parse String: {} to Integer",
                                eventSeqNumStr);
                    }
                    eventSequence.set(eventSeqNum);
                }
            } catch (Exception e) {
                LOG.error("Can't load state file", e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else {
            LOG.info("First SDK start");
            setPropertiesHash(properties.getPropertiesHash());
        }
    }

    private void parseNfSubscriptions() {
        if(state.getProperty(NF_SUBSCRIPTIONS) != null){
            byte[] data = base64.decodeBase64(state.getProperty(NF_SUBSCRIPTIONS));
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            SpecificDatumReader<TopicSubscriptionInfo> avroReader = new SpecificDatumReader<TopicSubscriptionInfo>(
                    TopicSubscriptionInfo.class);
    
            try { // NOSONAR
                TopicSubscriptionInfo decodedInfo = null;
    
                while (!decoder.isEnd()) {
                    decodedInfo = avroReader.read(null, decoder);
                    LOG.debug("Loaded {}", decodedInfo);
                    nfSubscriptions.put(decodedInfo.getTopicInfo().getId(), decodedInfo);
                }
            } catch (Exception e) {
                LOG.error("Unexpected exception occurred while reading information from decoder", e);
            }
        }else{
            LOG.info("No subscription info found in state");
        }
    }

    private boolean isSDKPropertiesUpdated(KaaClientProperties sdkProperties, Properties stateProperties) {
        byte[] hashFromSDK = sdkProperties.getPropertiesHash();
        byte[] hashFromStateFile = base64.decodeBase64(state.getProperty(PROPERTIES_HASH,
                new String(base64.encodeBase64(new byte[0]), Charsets.UTF_8)).getBytes(Charsets.UTF_8));

        return !Arrays.equals(hashFromSDK, hashFromStateFile);
    }

    private void setPropertiesHash(byte[] hash) {
        state.setProperty(PROPERTIES_HASH, new String(base64.encodeBase64(hash), Charsets.UTF_8));
    }

    @Override
    public boolean isConfigurationVersionUpdated() {
        return isConfigVersionUpdated;
    }

    @Override
    public boolean isRegistered() {
        return Boolean.parseBoolean(state.getProperty(IS_REGISTERED, Boolean.FALSE.toString()));
    }

    @Override
    public void setRegistered(boolean registered) {
        state.setProperty(IS_REGISTERED, Boolean.toString(registered));
    }

    @Override
    public void persist() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        SpecificDatumWriter<TopicSubscriptionInfo> datumWriter = new SpecificDatumWriter<TopicSubscriptionInfo>(TopicSubscriptionInfo.class);

        try {
            for (Map.Entry<String, TopicSubscriptionInfo> cursor : nfSubscriptions.entrySet()) {
                datumWriter.write(cursor.getValue(), encoder);
                LOG.info("Persisted {}", cursor.getValue());
            }

            encoder.flush();
            String base64Str = new String(base64.encodeBase64(baos.toByteArray()), Charset.forName("UTF-8"));
            state.setProperty(NF_SUBSCRIPTIONS, base64Str);
        } catch (IOException e) {
            LOG.error("Can't persist notification subscription info", e);
        }

        StringBuilder attachedEndpointsString = new StringBuilder();
        for (Map.Entry<EndpointAccessToken, EndpointKeyHash> attached : attachedEndpoints.entrySet()) {
            attachedEndpointsString.append(attached.getKey().getToken()).append(":").append(attached.getValue().getKeyHash()).append(',');
        }
        state.setProperty(ATTACHED_ENDPOINTS, attachedEndpointsString.toString());
        state.setProperty(EVENT_SEQ_NUM, "" + eventSequence.get());

        OutputStream os = null;
        try {
            storage.renameTo(stateFileLocation, stateFileLocation + "_bckp");
            os = storage.openForWrite(stateFileLocation);
            state.store(os, null);
        } catch (IOException e) {
            LOG.error("Can't persist state file", e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public String refreshEndpointAccessToken() {
        String newAccessToken = UUID.randomUUID().toString();
        setEndpointAccessToken(newAccessToken);
        return newAccessToken;
    }

    @Override
    public PublicKey getPublicKey() {
        return getOrInitKeyPair().getPublic();
    }
    
    @Override
    public PrivateKey getPrivateKey() {
        return getOrInitKeyPair().getPrivate();
    }

    private KeyPair getOrInitKeyPair() {
        LOG.debug("Check if key pair exists {}, {}", clientPublicKeyFileLocation, clientPrivateKeyFileLocation);
        if(kp != null){
            return kp;
        }
        if (storage.exists(clientPublicKeyFileLocation) && storage.exists(clientPrivateKeyFileLocation)) {
            InputStream publicKeyInput = null;
            InputStream privateKeyInput = null;
            try {
                publicKeyInput = storage.openForRead(clientPublicKeyFileLocation);
                privateKeyInput = storage.openForRead(clientPrivateKeyFileLocation);
                kp = new KeyPair(KeyUtil.getPublic(publicKeyInput), KeyUtil.getPrivate(privateKeyInput));
            } catch (Exception e) {
                LOG.error("Error loading Client Private Key", e);
                throw new RuntimeException(e); // NOSONAR
            } finally {
                IOUtils.closeQuietly(publicKeyInput);
                IOUtils.closeQuietly(privateKeyInput);
            }
        }
        if (kp == null) {
            LOG.debug("Generating Client Key pair");
            OutputStream privateKeyOutput = null;
            OutputStream publicKeyOutput = null;
            try {
                privateKeyOutput = storage.openForWrite(clientPrivateKeyFileLocation);
                publicKeyOutput = storage.openForWrite(clientPublicKeyFileLocation);
                kp = KeyUtil.generateKeyPair(privateKeyOutput, publicKeyOutput);
            } catch (IOException e) {
                LOG.error("Error generating Client Key pair", e);
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(privateKeyOutput);
                IOUtils.closeQuietly(publicKeyOutput);
            }
        }
        return kp;
    }
    
    @Override
    public EndpointKeyHash getEndpointKeyHash() {
        if(keyHash == null){
            EndpointObjectHash publicKeyHash = EndpointObjectHash.fromSHA1(getOrInitKeyPair().getPublic().getEncoded());
            keyHash = new EndpointKeyHash(new String(base64.encodeBase64(publicKeyHash.getData())));
        } 
        return keyHash;
    }

    @Override
    public int getAppStateSeqNumber() {
        return Integer.parseInt(state.getProperty(APP_STATE_SEQ_NUMBER, "1"));
    }

    @Override
    public EndpointObjectHash getProfileHash() {
        return EndpointObjectHash.fromBytes(base64.decodeBase64(state.getProperty(PROFILE_HASH,
                new String(base64.encodeBase64(new byte[0]), Charsets.UTF_8)).getBytes(Charsets.UTF_8)));
    }

    @Override
    public void setAppStateSeqNumber(int appStateSeqNumber) {
        state.setProperty(APP_STATE_SEQ_NUMBER, Integer.toString(appStateSeqNumber));
    }

    @Override
    public void setProfileHash(EndpointObjectHash hash) {
        state.setProperty(PROFILE_HASH, new String(base64.encodeBase64(hash.getData()), Charsets.UTF_8));
    }

    @Override
    public void addTopic(Topic topic) {
        TopicSubscriptionInfo subscriptionInfo = nfSubscriptions.get(topic.getId());
        if (subscriptionInfo == null) {
            nfSubscriptions.put(topic.getId(), new TopicSubscriptionInfo(topic, 0));
            LOG.info("Adding new seqNumber 0 for {} subscription", topic.getId());
        }
    }

    @Override
    public void removeTopic(String topicId) {
        if (nfSubscriptions.remove(topicId) != null) {
            LOG.debug("Removed subscription info for {}", topicId);
        }
    }

    @Override
    public boolean updateTopicSubscriptionInfo(String topicId, Integer sequenceNumber) {
        TopicSubscriptionInfo subscriptionInfo = nfSubscriptions.get(topicId);
        boolean updated = false;
        if (subscriptionInfo != null) {
            if (sequenceNumber > subscriptionInfo.getSeqNumber()) {
                updated = true;
                subscriptionInfo.setSeqNumber(sequenceNumber);
                nfSubscriptions.put(topicId, subscriptionInfo);
                LOG.debug("Updated seqNumber to {} for {} subscription", subscriptionInfo.getSeqNumber(), topicId);
            }
        }
        return updated;
    }

    @Override
    public Map<String, Integer> getNfSubscriptions() {
        HashMap<String, Integer> subscriptions = new HashMap<String, Integer>(); // NOSONAR

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

    @Override
    public int getAndIncrementEventSeqNum() {
        return eventSequence.getAndIncrement();
    }

    @Override
    public int getEventSeqNum() {
        return eventSequence.get();
    }

    @Override
    public void setEventSeqNum(int newSeqNum) {
        eventSequence.set(newSeqNum);
    }

    @Override
    public boolean isAttachedToUser() {
        return Boolean.parseBoolean(state.getProperty(IS_ATTACHED, "false"));
    }

    @Override
    public void setAttachedToUser(boolean isAttached) {
        state.setProperty(IS_ATTACHED, Boolean.toString(isAttached));
    }

    @Override
    public void clean() {
        state.setProperty(IS_REGISTERED, "false");
        saveFileDelete(stateFileLocation);
        saveFileDelete(stateFileLocation + "_bckp");
    }

    private void saveFileDelete(String fileName) {
        try {
            FileUtils.forceDelete(new File(fileName));
        } catch (FileNotFoundException e) {
            LOG.trace("File {} wasn't deleted, as it hadn't existed :", fileName, e);
        } catch (IOException e) {
            LOG.debug("An error occurred during deletion of the file [{}] :", fileName, e);
        }
    }
}
