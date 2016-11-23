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

import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Map;

public interface KaaClientState {

  boolean isRegistered();

  void setRegistered(boolean registered);

  PrivateKey getPrivateKey();

  PublicKey getPublicKey();

  EndpointKeyHash getEndpointKeyHash();

  int getAppStateSeqNumber();

  void setAppStateSeqNumber(int appStateSeqNumber);

  void setIfNeedProfileResync(boolean needProfileResync);

  boolean isNeedProfileResync();

  EndpointObjectHash getProfileHash();

  void setProfileHash(EndpointObjectHash hash);

  void addTopic(Topic topic);

  void removeTopic(Long topicId);

  void addTopicSubscription(Long topicId);

  void removeTopicSubscription(Long topicId);

  boolean updateTopicSubscriptionInfo(Long topicId, Integer sequenceNumber);

  Integer getTopicListHash();

  void setTopicListHash(Integer topicListHash);

  Map<Long, Integer> getNfSubscriptions();

  Collection<Topic> getTopics();

  Map<EndpointAccessToken, EndpointKeyHash> getAttachedEndpointsList();

  void setAttachedEndpointsList(Map<EndpointAccessToken, EndpointKeyHash> attachedEndpoints);

  String getEndpointAccessToken();

  void setEndpointAccessToken(String token);

  int getAndIncrementEventSeqNum();

  int getEventSeqNum();

  void setEventSeqNum(int newSeqNum);

  boolean isAttachedToUser();

  void setAttachedToUser(boolean isAttached);

  boolean isConfigurationVersionUpdated();

  void persist();

  String refreshEndpointAccessToken();

  void clean();
}
