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

package org.kaaproject.kaa.server.operations.pojo;

import java.util.Map;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.NotificationServerSync;
import org.kaaproject.kaa.server.sync.ProfileServerSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.UserServerSync;

/**
 * The Class SyncContext.
 */
public class SyncContext {

    /** The response. */
    private final ServerSync response;

    private String endpointKey;

    private int requestHash;

    private ClientSyncMetaData metaData;

    private EndpointProfileDto endpointProfile;

    private AppSeqNumber appSeqNumber;

    /** The subscription states. */
    private Map<String, Integer> subscriptionStates;

    /** The system nf version. */
    private int systemNfVersion;

    /** The user nf version. */
    private int userNfVersion;

    public static SyncContext failure(Integer requestId) {
        ServerSync response = new ServerSync();
        response.setRequestId(requestId);
        response.setStatus(SyncStatus.FAILURE);
        return new SyncContext(response);
    }

    /**
     * Instantiates a new sync response holder.
     *
     * @param response
     *            the response
     */
    public SyncContext(ServerSync response) {
        super();
        this.response = response;
    }

    public void setEndpointProfile(EndpointProfileDto profile) {
        this.endpointProfile = profile;
        if (profile != null) {
            this.systemNfVersion = profile.getSystemNfVersion();
            this.userNfVersion = profile.getUserNfVersion();
        }
    }

    public void setSubscriptionStates(Map<String, Integer> subscriptionStates) {
        this.subscriptionStates = subscriptionStates;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public ServerSync getResponse() {
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

    public SyncStatus getStatus() {
        return response.getStatus();
    }

    public void setStatus(SyncStatus status) {
        this.response.setStatus(status);
    }

    public void setUserSyncResponse(UserServerSync response) {
        this.response.setUserSync(response);
    }

    public void setEventSyncResponse(EventServerSync response) {
        this.response.setEventSync(response);
    }

    public void setConfigurationSyncResponse(ConfigurationServerSync response) {
        this.response.setConfigurationSync(response);
    }

    public void setNotificationSyncResponse(NotificationServerSync response) {
        this.response.setNotificationSync(response);
    }

    public void setProfileSyncResponse(ProfileServerSync response) {
        this.response.setProfileSync(response);
    }

    public void setRequestId(Integer value) {
        this.response.setRequestId(value);
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public int getRequestHash() {
        return requestHash;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    public void setRequestHash(int requestHash) {
        this.requestHash = requestHash;
    }

    public AppSeqNumber getAppSeqNumber() {
        return appSeqNumber;
    }

    public void setAppSeqNumber(AppSeqNumber appSeqNumber) {
        this.appSeqNumber = appSeqNumber;
    }

    public ClientSyncMetaData getMetaData() {
        return metaData;
    }
    
    public String getAppToken(){
        return metaData.getApplicationToken();
    }

    public void setMetaData(ClientSyncMetaData md) {
        this.metaData = md;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + requestHash;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SyncContext other = (SyncContext) obj;
        if (requestHash != other.requestHash) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
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
     * @return true, if successful
     */
    public boolean requireImmediateReply() {
        ServerSync response = getResponse();
        if (response.getProfileSync() != null && response.getProfileSync().getResponseStatus() != SyncResponseStatus.NO_DELTA) {
            return true;
        }
        if (response.getConfigurationSync() != null && response.getConfigurationSync().getResponseStatus() != SyncResponseStatus.NO_DELTA) {
            return true;
        }
        if (response.getNotificationSync() != null && response.getNotificationSync().getResponseStatus() != SyncResponseStatus.NO_DELTA) {
            return true;
        }
        if (response.getEventSync() != null) {
            if (response.getEventSync().getEventSequenceNumberResponse() != null) {
                return true;
            }
            if (response.getEventSync().getEvents() != null && !response.getEventSync().getEvents().isEmpty()) {
                return true;
            }
            if (response.getEventSync().getEventListenersResponses() != null
                    && !response.getEventSync().getEventListenersResponses().isEmpty()) {
                return true;
            }
        }
        if (response.getUserSync() != null) {
            UserServerSync userResponse = response.getUserSync();
            if (userResponse.getEndpointAttachResponses() != null && !userResponse.getEndpointAttachResponses().isEmpty()) {
                return true;
            }
            if (userResponse.getEndpointDetachResponses() != null && !userResponse.getEndpointDetachResponses().isEmpty()) {
                return true;
            }
            if (userResponse.getUserAttachResponse() != null) {
                return true;
            }
            if (userResponse.getUserAttachNotification() != null) {
                return true;
            }
            if (userResponse.getUserDetachNotification() != null) {
                return true;
            }
        }
        if (response.getLogSync() != null) {
            return true;
        }
        return false;
    }
}
