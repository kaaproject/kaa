/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.ChannelMap.ChannelMetaData;

public class EndpointActorState {

    /** The map of active communication channels. */
    private final ChannelMap channelMap;
    private EndpointProfileDto endpointProfile;
    private String userId;
    private boolean userRegistrationRequestSent;

    public EndpointActorState(String endpointKey, String actorKey) {
        this.channelMap = new ChannelMap(endpointKey, actorKey);
    }

    public void addChannel(ChannelMetaData channel) {
        this.channelMap.addChannel(channel);
    }

    public boolean isNoChannels() {
        return channelMap.isEmpty();
    }

    List<ChannelMetaData> getChannelsByType(TransportType type) {
        return this.channelMap.getByTransportType(type);
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

    boolean isValidForEvents() {
        if (endpointProfile != null) {
            return endpointProfile.isValidForEvents();
        } else {
            return false;
        }
    }

    boolean userIdMismatch() {
        return userId != null && !userId.equals(getProfileUserId());
    }

    public boolean isUserRegistrationPending() {
        return userRegistrationRequestSent;
    }

    public void setUserRegistrationPending(boolean userRegistrationRequestSent) {
        this.userRegistrationRequestSent = userRegistrationRequestSent;
    }
}
