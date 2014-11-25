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

package org.kaaproject.kaa.server.operations.pojo;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;


/**
 * The Class SyncResponseHolder.
 */
public class SyncResponseHolder {

    /** The response. */
    private final SyncResponse response;

    private EndpointProfileDto endpointProfile;

    /** The subscription states. */
    private Map<String, Integer> subscriptionStates;

    /** The system nf version. */
    private int systemNfVersion;

    /** The user nf version. */
    private int userNfVersion;

    public static SyncResponseHolder failure(Integer requestId){
        SyncResponse response = new SyncResponse();
        response.setRequestId(requestId);
        response.setStatus(SyncResponseResultType.FAILURE);
        return new SyncResponseHolder(response);
    }

    /**
     * Instantiates a new sync response holder.
     *
     * @param response the response
     */
    public SyncResponseHolder(SyncResponse response){
        super();
        this.response = response;
        this.subscriptionStates = new HashMap<>();
    }

    public void setEndpointProfile(EndpointProfileDto profile) {
        this.endpointProfile = profile;
        this.systemNfVersion = profile.getSystemNfVersion();
        this.userNfVersion = profile.getUserNfVersion();
    }

    public void setSubscriptionStates(Map<String, Integer> subscriptionStates) {
        this.subscriptionStates = subscriptionStates;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public SyncResponse getResponse() {
        return response;
    }

    /**
     * Gets the subscription states.
     *
     * @return the subscription states
     */
    public Map<String, Integer> getSubscriptionStates() {
        return subscriptionStates;
    }

    /**
     * Gets the system nf version.
     *
     * @return the system nf version
     */
    public int getSystemNfVersion() {
        return systemNfVersion;
    }

    /**
     * Gets the user nf version.
     *
     * @return the user nf version
     */
    public int getUserNfVersion() {
        return userNfVersion;
    }

    public EndpointProfileDto getEndpointProfile() {
        return endpointProfile;
    }

    public SyncResponseResultType getStatus() {
        return response.getStatus();
    }

    public void setStatus(SyncResponseResultType status) {
        this.response.setStatus(status);
    }

    public void setUserSyncResponse(UserSyncResponse response) {
        this.response.setUserSyncResponse(response);
    }

    public void setEventSyncResponse(EventSyncResponse response) {
        this.response.setEventSyncResponse(response);
    }

    public void setConfigurationSyncResponse(ConfigurationSyncResponse response) {
        this.response.setConfigurationSyncResponse(response);
    }

    public void setNotificationSyncResponse(NotificationSyncResponse response) {
        this.response.setNotificationSyncResponse(response);
    }

    public void setProfileSyncResponse(ProfileSyncResponse response) {
        this.response.setProfileSyncResponse(response);
    }

    public void setRequestId(Integer value) {
        this.response.setRequestId(value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SyncResponseHolder [response=");
        builder.append(response);
        builder.append(", subscriptionStates=");
        builder.append(subscriptionStates);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Require immediate reply.
     *
     * @param response
     *            the response
     * @return true, if successful
     */
    public boolean requireImmediateReply() {
        SyncResponse response = getResponse();
        if (response.getProfileSyncResponse() != null && response.getProfileSyncResponse().getResponseStatus() != SyncResponseStatus.NO_DELTA) {
            return true;
        }
        if (response.getConfigurationSyncResponse() != null && response.getConfigurationSyncResponse().getResponseStatus() != SyncResponseStatus.NO_DELTA) {
            return true;
        }
        if (response.getNotificationSyncResponse() != null && response.getNotificationSyncResponse().getResponseStatus() != SyncResponseStatus.NO_DELTA) {
            return true;
        }
        if (response.getEventSyncResponse() != null) {
            if(response.getEventSyncResponse().getEventSequenceNumberResponse() != null){
                return true;
            }
            if(response.getEventSyncResponse().getEvents() != null && !response.getEventSyncResponse().getEvents().isEmpty()){
                return true;
            }
            if(response.getEventSyncResponse().getEventListenersResponses() != null && !response.getEventSyncResponse().getEventListenersResponses().isEmpty()){
                return true;
            }
        }
        if (response.getUserSyncResponse() != null) {
            UserSyncResponse userResponse = response.getUserSyncResponse();
            if (userResponse.getEndpointAttachResponses() != null && !userResponse.getEndpointAttachResponses().isEmpty()) {
                return true;
            }
            if (userResponse.getEndpointDetachResponses() != null && !userResponse.getEndpointDetachResponses().isEmpty()) {
                return true;
            }
            if (userResponse.getUserAttachResponse() != null) {
                return true;
            }
        }
        if (response.getLogSyncResponse() != null) {
            return true;
        }
        return false;
    }
}
