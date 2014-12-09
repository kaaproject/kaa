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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public interface KaaClientState {

    boolean isRegistered();
    void setRegistered(boolean registered);

    PrivateKey getPrivateKey();
    PublicKey getPublicKey();

    EndpointKeyHash getEndpointKeyHash();

    void setAppStateSeqNumber(int appStateSeqNumber);
    int getAppStateSeqNumber();

    void setConfigSeqNumber(int configSeqNumber);
    int getConfigSeqNumber();

    void setNotificationSeqNumber(int notificationSeqNumber);
    int getNotificationSeqNumber();

    void setConfigurationHash(EndpointObjectHash hash);
    EndpointObjectHash getConfigurationHash();

    void setProfileHash(EndpointObjectHash hash);
    EndpointObjectHash getProfileHash();

    void addTopic(Topic topic);
    void removeTopic(String topicId);
    boolean updateTopicSubscriptionInfo(String topicId, Integer sequenceNumber);

    Map<String, Integer> getNfSubscriptions();
    List<Topic> getTopics();

    void setAttachedEndpointsList(Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints);
    Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointsList();

    void setEndpointAccessToken(String token);
    String getEndpointAccessToken();

    int getAndIncrementEventSeqNum();
    int getEventSeqNum();
    void setEventSeqNum(int newSeqNum);

    boolean isAttachedToUser();
    void setAttachedToUser(boolean isAttached);

    boolean isConfigurationVersionUpdated();

    void persist();
}
