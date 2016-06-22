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

package org.kaaproject.kaa.client.persistence;

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
import org.kaaproject.kaa.client.exceptions.KaaInvalidConfigurationException;
import org.kaaproject.kaa.client.notification.TopicListHashCalculator;
import org.kaaproject.kaa.client.util.Base64;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Generated;
import java.io.*;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Generated("KaaClientPropertiesState.java.template")
public class KaaClientPropertiesState implements KaaClientState {

    private static final String APP_STATE_SEQ_NUMBER = "APP_STATE_SEQ_NUMBER";
    private static final String PROFILE_HASH = "PROFILE_HASH";
    private static final String ENDPOINT_ACCESS_TOKEN = "ENDPOINT_TOKEN";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(KaaClientPropertiesState.class);

    private static final String ATTACHED_ENDPOINTS = "attached_eps";
    private static final String NF_SUBSCRIPTIONS = "nf_subscriptions";
    private static final String IS_REGISTERED = "is_registered";
    private static final String IS_ATTACHED = "is_attached";

    private static final String EVENT_SEQ_NUM = "event.seq.num";

    private static final String TOPIC_LIST = "topic.list";
    private static final String TOPIC_LIST_HASH = "topic.list.hash";

    private static final String PROPERTIES_HASH = "properties.hash";

    private static final String NEED_PROFILE_RESYNC = "need.profile.resync";

    private final PersistentStorage storage;
    private final Base64 base64;
    private final Properties state;
    private final String stateFileLocation;
    private final String clientPrivateKeyFileLocation;
    private final String clientPublicKeyFileLocation;
    private final Map<Long, Topic> topicMap = new HashMap<>();
    private final Map<Long, Integer> nfSubscriptions = new HashMap<>();
    private final Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints = new HashMap<>();
    private final AtomicInteger eventSequence = new AtomicInteger();
    private Integer topicListHash;

    private KeyPair keyPair;
    private EndpointKeyHash keyHash;
    private boolean isConfigVersionUpdated = false;
    private boolean hasUpdate = false;

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

                if (isSDKPropertiesUpdated(properties)) {
                    LOG.info("SDK properties were updated");
                    setRegistered(false);
                    setPropertiesHash(properties.getPropertiesHash());
                    //TODO: add more intelligent check by comparing part of SDK token.
                    isConfigVersionUpdated = true;
                } else {
                    LOG.info("SDK properties are up to date");
                }

                parseTopics();
                parseNfSubscriptions();

                String attachedEndpointsString = state.getProperty(ATTACHED_ENDPOINTS);
                if (attachedEndpointsString != null) {
                    String[] splittedEndpointsList = attachedEndpointsString.split(",");
                    for (String attachedEndpoint : splittedEndpointsList) {
                        if (!attachedEndpoint.isEmpty()) {
                            String[] splittedValues = attachedEndpoint.split(":");
                            attachedEndpoints.put(new EndpointAccessToken(splittedValues[0]), new EndpointKeyHash(splittedValues[1]));
                        }
                    }
                }

                String eventSeqNumStr = state.getProperty(EVENT_SEQ_NUM);
                if (eventSeqNumStr != null) {
                    Integer eventSeqNum = 0;
                    try { // NOSONAR
                        eventSeqNum = Integer.parseInt(eventSeqNumStr);
                    } catch (NumberFormatException e) {
                        LOG.error("Unexpected exception while parsing event sequence number. Can not parse String: {} to Integer",
                                eventSeqNumStr);
                    }
                    eventSequence.set(eventSeqNum);
                }
                String topicListHashStr = state.getProperty(TOPIC_LIST_HASH);
                if (topicListHashStr != null) {
                    try { // NOSONAR
                        this.topicListHash = Integer.parseInt(topicListHashStr);
                    } catch (NumberFormatException e) {
                        LOG.error("Unexpected exception while parsing topic list hash. Can not parse String: {} to Integer",
                                topicListHashStr);
                    }
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

    private void parseTopics() {
        if (state.getProperty(TOPIC_LIST) != null) {
            byte[] data = base64.decodeBase64(state.getProperty(TOPIC_LIST));
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            SpecificDatumReader<Topic> avroReader = new SpecificDatumReader<>(Topic.class);
            try { // NOSONAR
                Topic decodedTopic;
                while (!decoder.isEnd()) {
                    decodedTopic = avroReader.read(null, decoder);
                    LOG.debug("Loaded {}", decodedTopic);
                    topicMap.put(decodedTopic.getId(), decodedTopic);
                }
            } catch (Exception e) {
                LOG.error("Unexpected exception occurred while reading information from decoder", e);
            }
        } else {
            LOG.info("No topic list found in state");
        }
    }

    @SuppressWarnings("unchecked")
    private void parseNfSubscriptions() {
        if (state.getProperty(NF_SUBSCRIPTIONS) != null) {
            byte[] data = base64.decodeBase64(state.getProperty(NF_SUBSCRIPTIONS));
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            try (ObjectInputStream ois = new ObjectInputStream(is)) {
                nfSubscriptions.putAll((Map<Long, Integer>) ois.readObject());
            } catch (Exception e) {
                LOG.error("Unexpected exception occurred while reading subscription information from state", e);
            }
        } else {
            LOG.info("No subscription info found in state");
        }
    }

    private boolean isSDKPropertiesUpdated(KaaClientProperties sdkProperties) {
        byte[] hashFromSDK = sdkProperties.getPropertiesHash();
        byte[] hashFromStateFile = base64.decodeBase64(state.getProperty(PROPERTIES_HASH,
                new String(base64.encodeBase64(new byte[0]), Charsets.UTF_8)).getBytes(Charsets.UTF_8));

        return !Arrays.equals(hashFromSDK, hashFromStateFile);
    }

    private void setStateStringValue(String propertyKey, String value) {
        Object previous = state.setProperty(propertyKey, value);
        String previousString = previous == null ? null : previous.toString();
        hasUpdate |= !value.equals(previousString);
    }

    private void setStateBooleanValue(String propertyKey, boolean value) {
        Object previous = state.setProperty(propertyKey, Boolean.toString(value));
        boolean previousBoolean = previous == null ? false : Boolean.valueOf(previous.toString());
        hasUpdate |= value != previousBoolean;
    }

    private void setPropertiesHash(byte[] hash) {
        setStateStringValue(PROPERTIES_HASH, new String(base64.encodeBase64(hash), Charsets.UTF_8));
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
        setStateBooleanValue(IS_REGISTERED, registered);
    }

    @Override
    public boolean isNeedProfileResync() {
        return Boolean.parseBoolean(state.getProperty(NEED_PROFILE_RESYNC, Boolean.FALSE.toString()));
    }

    @Override
    public void setIfNeedProfileResync(boolean needProfileResync) {
        setStateBooleanValue(NEED_PROFILE_RESYNC, needProfileResync);
    }

    @Override
    public void persist() {
        if (hasUpdate) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
            SpecificDatumWriter<Topic> datumWriter = new SpecificDatumWriter<>(Topic.class);
            try {
                for (Topic topic : topicMap.values()) {
                    datumWriter.write(topic, encoder);
                    LOG.info("Persisted {}", topic);
                }
                encoder.flush();
                String base64Str = new String(base64.encodeBase64(baos.toByteArray()), Charset.forName("UTF-8"));
                state.setProperty(TOPIC_LIST, base64Str);
            } catch (IOException e) {
                LOG.error("Can't persist topic list info", e);
            }

            baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(nfSubscriptions);
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
            if (topicListHash != null) {
                state.setProperty(TOPIC_LIST_HASH, "" + topicListHash);
            }

            OutputStream os = null;
            try {
                storage.renameTo(stateFileLocation, stateFileLocation + "_bckp");
                os = storage.openForWrite(stateFileLocation);
                state.store(os, null);
                hasUpdate = false;
            } catch (IOException e) {
                LOG.error("Can't persist state file", e);
            } finally {
                IOUtils.closeQuietly(os);
            }
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
        if (keyPair != null) {
            return keyPair;
        }
        if (storage.exists(clientPublicKeyFileLocation) && storage.exists(clientPrivateKeyFileLocation)) {
            InputStream publicKeyInput = null;
            InputStream privateKeyInput = null;
            try {
                publicKeyInput = storage.openForRead(clientPublicKeyFileLocation);
                privateKeyInput = storage.openForRead(clientPrivateKeyFileLocation);

                PublicKey publicKey = KeyUtil.getPublic(publicKeyInput);
                PrivateKey privateKey = KeyUtil.getPrivate(privateKeyInput);

                if (publicKey != null && privateKey != null) {
                    keyPair = new KeyPair(publicKey, privateKey);
                    if (!KeyUtil.validateKeyPair(keyPair)) {
                        throw new InvalidKeyException();
                    }
                }
            } catch (InvalidKeyException e) {
                keyPair = null;
                LOG.error("Unable to parse client RSA keypair. Generating new keys.. Reason {}", e);
            } catch (Exception e) {
                LOG.error("Error loading client RSA keypair. Reason {}", e);
                throw new RuntimeException(e); // NOSONAR
            } finally {
                IOUtils.closeQuietly(publicKeyInput);
                IOUtils.closeQuietly(privateKeyInput);
            }
        }
        if (keyPair == null) {
            LOG.debug("Generating Client Key pair");
            OutputStream privateKeyOutput = null;
            OutputStream publicKeyOutput = null;
            try {
                privateKeyOutput = storage.openForWrite(clientPrivateKeyFileLocation);
                publicKeyOutput = storage.openForWrite(clientPublicKeyFileLocation);
                keyPair = KeyUtil.generateKeyPair(privateKeyOutput, publicKeyOutput);
            } catch (IOException e) {
                LOG.error("Error generating Client Key pair", e);
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(privateKeyOutput);
                IOUtils.closeQuietly(publicKeyOutput);
            }
        } else {
            LOG.debug("Error loading public key", "Project is not trustful and key pair is not found");
            throw new KaaInvalidConfigurationException("This project isn't trustful, is it?");
        }
        return keyPair;
    }

    @Override
    public EndpointKeyHash getEndpointKeyHash() {
        if (keyHash == null) {
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
    public void setAppStateSeqNumber(int appStateSeqNumber) {
        setStateStringValue(APP_STATE_SEQ_NUMBER, Integer.toString(appStateSeqNumber));
    }

    @Override
    public EndpointObjectHash getProfileHash() {
        return EndpointObjectHash.fromBytes(base64.decodeBase64(state.getProperty(PROFILE_HASH,
                new String(base64.encodeBase64(new byte[0]), Charsets.UTF_8)).getBytes(Charsets.UTF_8)));
    }

    @Override
    public void setProfileHash(EndpointObjectHash hash) {
        setStateStringValue(PROFILE_HASH, new String(base64.encodeBase64(hash.getData()), Charsets.UTF_8));
    }

    @Override
    public void addTopic(Topic topic) {
        if (topicMap.get(topic.getId()) == null) {
            topicMap.put(topic.getId(), topic);
            if (topic.getSubscriptionType() == SubscriptionType.MANDATORY_SUBSCRIPTION) {
                nfSubscriptions.put(topic.getId(), 0);
                LOG.info("Adding new seqNumber 0 for {} subscription", topic.getId());
            }
            hasUpdate = true;
            LOG.info("Adding new topic with id {}", topic.getId());
        }
    }

    @Override
    public void removeTopic(Long topicId) {
        if (topicMap.remove(topicId) != null) {
            if (nfSubscriptions.remove(topicId) != null) {
                LOG.info("Removed subscription info for {}", topicId);
            }
            hasUpdate = true;
            LOG.info("Removed topic with id {}", topicId);
        }
    }

    @Override
    public void addTopicSubscription(Long topicId) {
        Integer seqNum = nfSubscriptions.get(topicId);
        if (seqNum == null) {
            nfSubscriptions.put(topicId, 0);
            LOG.info("Adding new seqNumber 0 for {} subscription", topicId);
            hasUpdate = true;
        }
    }

    @Override
    public void removeTopicSubscription(Long topicId) {
        if (nfSubscriptions.remove(topicId) != null) {
            LOG.info("Removed subscription info for {}", topicId);
            hasUpdate = true;
        }
    }

    @Override
    public boolean updateTopicSubscriptionInfo(Long topicId, Integer sequenceNumber) {
        Integer seqNum = nfSubscriptions.get(topicId);
        boolean updated = false;
        if (seqNum != null) {
            if (sequenceNumber > seqNum) {
                updated = true;
                nfSubscriptions.put(topicId, sequenceNumber);
                hasUpdate = true;
                LOG.debug("Updated seqNumber to {} for {} subscription", sequenceNumber, topicId);
            }
        }
        return updated;
    }

    @Override
    public Map<Long, Integer> getNfSubscriptions() {
        return nfSubscriptions;
    }

    @Override
    public Collection<Topic> getTopics() {
        return topicMap.values();
    }

    @Override
    public Integer getTopicListHash() {
        if (topicListHash == null) {
            return TopicListHashCalculator.NULL_LIST_HASH;
        } else {
            return topicListHash;
        }
    }

    @Override
    public void setTopicListHash(Integer topicListHash) {
        if (!Objects.equals(this.topicListHash, topicListHash)) {
            this.topicListHash = topicListHash;
            hasUpdate = true;
        }
    }

    @Override
    public Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointsList() {
        return attachedEndpoints;
    }

    @Override
    public void setAttachedEndpointsList(Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints) {
        this.attachedEndpoints.clear();
        this.attachedEndpoints.putAll(attachedEndpoints);
        hasUpdate = true;
    }

    @Override
    public String getEndpointAccessToken() {
        return state.getProperty(ENDPOINT_ACCESS_TOKEN, "");
    }

    @Override
    public void setEndpointAccessToken(String token) {
        setStateStringValue(ENDPOINT_ACCESS_TOKEN, token);
    }

    @Override
    public int getAndIncrementEventSeqNum() {
        hasUpdate = true;
        return eventSequence.getAndIncrement();
    }

    @Override
    public int getEventSeqNum() {
        return eventSequence.get();
    }

    @Override
    public void setEventSeqNum(int newSeqNum) {
        if (eventSequence.get() != newSeqNum) {
            eventSequence.set(newSeqNum);
            hasUpdate = true;
        }
    }

    @Override
    public boolean isAttachedToUser() {
        return Boolean.parseBoolean(state.getProperty(IS_ATTACHED, Boolean.FALSE.toString()));
    }

    @Override
    public void setAttachedToUser(boolean isAttached) {
        setStateBooleanValue(IS_ATTACHED, isAttached);
    }

    @Override
    public void clean() {
        setRegistered(false);
        setIfNeedProfileResync(false);
        saveFileDelete(stateFileLocation);
        saveFileDelete(stateFileLocation + "_bckp");
        hasUpdate = true;
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
