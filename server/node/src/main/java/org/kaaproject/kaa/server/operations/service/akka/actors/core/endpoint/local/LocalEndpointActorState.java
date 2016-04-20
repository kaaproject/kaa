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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.AbstractEndpointActorState;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.ChannelMap.ChannelMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalEndpointActorState extends AbstractEndpointActorState {
    private static final Logger LOG = LoggerFactory.getLogger(LocalEndpointActorState.class);

    /** The map of active communication channels. */
    private final ChannelMap channelMap;
    private EndpointProfileDto endpointProfile;
    private String userId;
    private boolean userRegistrationRequestSent;
    private int processedEventSeqNum = Integer.MIN_VALUE;
    private Map<String, Integer> subscriptionStates;

    private boolean ucfHashIntialized;
    private byte[] ucfHash;

    public LocalEndpointActorState(String endpointKey, String actorKey) {
        super(endpointKey, actorKey);
        this.channelMap = new ChannelMap(endpointKey, actorKey);
        this.subscriptionStates = new HashMap<String, Integer>();
    }

    public void addChannel(ChannelMetaData channel) {
        this.channelMap.addChannel(channel);
    }

    public boolean isNoChannels() {
        return channelMap.isEmpty();
    }

    Set<ChannelMetaData> getAllChannels() {
        return getChannelsByTypes(TransportType.values());
    }

    Set<ChannelMetaData> getChannelsByType(TransportType type) {
        return new HashSet<>(this.channelMap.getByTransportType(type));
    }

    Set<ChannelMetaData> getChannelsByTypes(TransportType... types) {
        Set<ChannelMetaData> channels = new HashSet<>();

        for (TransportType type : types) {
            channels.addAll(channelMap.getByTransportType(type));
        }

        return channels;
    }

    ChannelMetaData getChannelByRequestId(UUID requestId) {
        return channelMap.getByRequestId(requestId);
    }

    ChannelMetaData getChannelById(UUID requestId) {
        return channelMap.getById(requestId);
    }

    public void removeChannel(ChannelMetaData channel) {
        channelMap.removeChannel(channel);
    }

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    EndpointProfileDto getProfile() {
        return endpointProfile;
    }

    void setProfile(EndpointProfileDto endpointProfile) {
        this.endpointProfile = endpointProfile;
    }

    boolean isProfileSet() {
        return this.endpointProfile != null;
    }

    String getProfileUserId() {
        if (endpointProfile != null) {
            return endpointProfile.getEndpointUserId();
        } else {
            return null;
        }
    }

    void setProfileUserId(String userId) {
        endpointProfile.setEndpointUserId(userId);
    }

    boolean isValidForUser() {
        return endpointProfile != null && endpointProfile.getEndpointUserId() != null && !endpointProfile.getEndpointUserId().isEmpty();
    }

    boolean isValidForEvents() {
        return endpointProfile != null && endpointProfile.getEndpointUserId() != null && !endpointProfile.getEndpointUserId().isEmpty()
                && endpointProfile.getEcfVersionStates() != null && !endpointProfile.getEcfVersionStates().isEmpty();
    }

    boolean userIdMismatch() {
        return userId != null && !userId.equals(getProfileUserId());
    }

    boolean isUserRegistrationPending() {
        return userRegistrationRequestSent;
    }

    void setUserRegistrationPending(boolean userRegistrationRequestSent) {
        this.userRegistrationRequestSent = userRegistrationRequestSent;
    }

    int getEventSeqNumber() {
        return processedEventSeqNum;
    }

    void resetEventSeqNumber() {
        processedEventSeqNum = Integer.MIN_VALUE;
    }

    void setEventSeqNumber(int maxSentEventSeqNum) {
        processedEventSeqNum = maxSentEventSeqNum;
    }

    public void setSubscriptionStates(Map<String, Integer> subscriptionStates) {
        this.subscriptionStates = new HashMap<String, Integer>(subscriptionStates);
    }

    public Map<String, Integer> getSubscriptionStates() {
        return subscriptionStates;
    }

    public boolean isUcfHashRequiresIntialization() {
        if (!isValidForUser()) {
            return false;
        }
        return !ucfHashIntialized;
    }

    public boolean isUserConfigurationUpdatePending() {
        if (!isValidForUser() || isUcfHashRequiresIntialization()) {
            return false;
        }
        return !Arrays.equals(ucfHash, endpointProfile.getUserConfigurationHash());
    }

    public void setUcfHash(byte[] ucfHash) {
        this.ucfHashIntialized = true;
        this.ucfHash = ucfHash;
    }

    public byte[] getUcfHash() {
        return ucfHash;
    }

    public List<NotificationDto> filter(List<NotificationDto> notifications) {
        List<NotificationDto> list = new ArrayList<NotificationDto>(notifications.size());
        for (NotificationDto nf : notifications) {
            if (subscriptionStates.containsKey(nf.getTopicId())) {
                list.add(nf);
            } else {
                LOG.trace("[{}][{}] Notification {} is no longer valid due to subscription state", endpointKey, actorKey, nf);
            }
        }
        return list;
    }
}
