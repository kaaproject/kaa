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

package org.kaaproject.kaa.common.dto;

import static org.kaaproject.kaa.common.dto.Util.getArrayCopy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EndpointProfileDto implements HasId, HasVersion, Serializable {


    private static final List<EndpointGroupStateDto> EMPTY_GROUP_STATE = Collections.unmodifiableList(new ArrayList<EndpointGroupStateDto>());

    private static final long serialVersionUID = -7122736699758720540L;

    private String id;
    private String applicationId;
    private byte[] endpointKey;
    private byte[] endpointKeyHash;
    private String endpointUserId;
    private String accessToken;
    private int sequenceNumber;
    private List<EndpointGroupStateDto> groupState;
    private List<String> subscriptions;
    private int simpleTopicHash;
    private byte[] topicHash;
    private String clientProfileBody;
    private String serverProfileBody;
    private byte[] profileHash;
    private byte[] configurationHash;
    private byte[] userConfigurationHash;
    private int clientProfileVersion;
    private int serverProfileVersion;
    private int configurationVersion;
    private int notificationVersion;
    private int systemNfVersion;
    private int userNfVersion;
    private int logSchemaVersion;
    private List<EventClassFamilyVersionStateDto> ecfVersionStates;
    private String serverHash;
    private String sdkToken;
    private Long version;
    private boolean useConfigurationRawSchema;
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public byte[] getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(byte[] endpointKey) {
        this.endpointKey = getArrayCopy(endpointKey);
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = getArrayCopy(endpointKeyHash);
    }

    public String getEndpointUserId() {
        return endpointUserId;
    }

    public void setEndpointUserId(String endpointUserId) {
        this.endpointUserId = endpointUserId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<EndpointGroupStateDto> getGroupState() {
        return groupState;
    }

    public void setGroupState(List<EndpointGroupStateDto> groupState) {
        this.groupState = groupState;
    }
    
    public void clearGroupState(){
        this.groupState = EMPTY_GROUP_STATE;
    }

    public String getClientProfileBody() {
        return clientProfileBody;
    }

    public void setClientProfileBody(String clientProfileBody) {
        this.clientProfileBody = clientProfileBody;
    }

    public String getServerProfileBody() {
        return serverProfileBody;
    }

    public void setServerProfileBody(String serverProfileBody) {
        this.serverProfileBody = serverProfileBody;
    }

    public byte[] getProfileHash() {
        return profileHash;
    }

    public void setProfileHash(byte[] profileHash) {
        this.profileHash = getArrayCopy(profileHash);
    }

    public byte[] getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(byte[] configurationHash) {
        this.configurationHash = getArrayCopy(configurationHash);
    }

    public byte[] getUserConfigurationHash() {
        return userConfigurationHash;
    }

    public void setUserConfigurationHash(byte[] userConfigurationHash) {
        this.userConfigurationHash = userConfigurationHash;
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(int configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public int getNotificationVersion() {
        return notificationVersion;
    }

    public void setNotificationVersion(int notificationVersion) {
        this.notificationVersion = notificationVersion;
    }

    public int getSimpleTopicHash() {
        return simpleTopicHash;
    }

    public void setSimpleTopicHash(int simpleTopicHash) {
        this.simpleTopicHash = simpleTopicHash;
    }

    public byte[] getTopicHash() {
        return topicHash;
    }

    public void setTopicHash(byte[] topicHash) {
        this.topicHash = topicHash;
    }

    public int getClientProfileVersion() {
        return clientProfileVersion;
    }

    public void setClientProfileVersion(int clientProfileVersion) {
        this.clientProfileVersion = clientProfileVersion;
    }

    public int getServerProfileVersion() {
        return serverProfileVersion;
    }

    public void setServerProfileVersion(int serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    public int getSystemNfVersion() {
        return systemNfVersion;
    }

    public void setSystemNfVersion(int systemNfVersion) {
        this.systemNfVersion = systemNfVersion;
    }

    public int getUserNfVersion() {
        return userNfVersion;
    }

    public void setUserNfVersion(int userNfVersion) {
        this.userNfVersion = userNfVersion;
    }

    public int getLogSchemaVersion() {
        return logSchemaVersion;
    }

    public void setLogSchemaVersion(int logSchemaVersion) {
        this.logSchemaVersion = logSchemaVersion;
    }

    public List<EventClassFamilyVersionStateDto> getEcfVersionStates() {
        return ecfVersionStates;
    }

    public void setEcfVersionStates(List<EventClassFamilyVersionStateDto> ecfVersionStates) {
        this.ecfVersionStates = ecfVersionStates;
    }

    public String getServerHash() {
        return serverHash;
    }

    public void setServerHash(String serverHash) {
        this.serverHash = serverHash;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public void setSdkToken(String sdkToken) {
        this.sdkToken = sdkToken;
    }
    
    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }


    public boolean isUseConfigurationRawSchema() {
        return useConfigurationRawSchema;
    }

    public void setUseConfigurationRawSchema(boolean useConfigurationRawSchema) {
        this.useConfigurationRawSchema = useConfigurationRawSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointProfileDto)) {
            return false;
        }

        EndpointProfileDto that = (EndpointProfileDto) o;

        if (configurationVersion != that.configurationVersion) {
            return false;
        }
        if (notificationVersion != that.notificationVersion) {
            return false;
        }
        if (clientProfileVersion != that.clientProfileVersion) {
            return false;
        }
        if (sequenceNumber != that.sequenceNumber) {
            return false;
        }
        if (systemNfVersion != that.systemNfVersion) {
            return false;
        }
        if (userNfVersion != that.userNfVersion) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (!Arrays.equals(configurationHash, that.configurationHash)) {
            return false;
        }
        if (!Arrays.equals(userConfigurationHash, that.userConfigurationHash)) {
            return false;
        }
        if (groupState != null ? !groupState.equals(that.groupState) : that.groupState != null) {
            return false;
        }
        if (!Arrays.equals(endpointKey, that.endpointKey)) {
            return false;
        }
        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
            return false;
        }
        if (simpleTopicHash != that.simpleTopicHash) {
            return false;
        }
        if (!Arrays.equals(topicHash, that.topicHash)) {
            return false;
        }
        if (clientProfileBody != null ? !clientProfileBody.equals(that.clientProfileBody) : that.clientProfileBody != null) {
            return false;
        }
        if (!Arrays.equals(profileHash, that.profileHash)) {
            return false;
        }
        if (subscriptions != null ? !subscriptions.equals(that.subscriptions) : that.subscriptions != null) {
            return false;
        }
        if (sdkToken != null ? !sdkToken.equals(that.sdkToken) : that.sdkToken != null) {
            return false;
        }

        if (useConfigurationRawSchema != that.useConfigurationRawSchema) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (endpointKey != null ? Arrays.hashCode(endpointKey) : 0);
        result = 31 * result + (endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0);
        result = 31 * result + (groupState != null ? groupState.hashCode() : 0);
        result = 31 * result + (subscriptions != null ? subscriptions.hashCode() : 0);
        result = 31 * result + (topicHash != null ? Arrays.hashCode(topicHash) : 0);
        result = 31 * result + sequenceNumber;
        result = 31 * result + (clientProfileBody != null ? clientProfileBody.hashCode() : 0);
        result = 31 * result + (profileHash != null ? Arrays.hashCode(profileHash) : 0);
        result = 31 * result + (configurationHash != null ? Arrays.hashCode(configurationHash) : 0);
        result = 31 * result + (userConfigurationHash != null ? Arrays.hashCode(userConfigurationHash) : 0);
        result = 31 * result + clientProfileVersion;
        result = 31 * result + configurationVersion;
        result = 31 * result + notificationVersion;
        result = 31 * result + systemNfVersion;
        result = 31 * result + userNfVersion;
        result = 31 * result + (sdkToken != null ? sdkToken.hashCode() : 0);
        result = 31 * result + (useConfigurationRawSchema ? 1 : 0);
        return result;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointProfileDto [id=");
        builder.append(id);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", endpointKey=");
        builder.append(Arrays.toString(endpointKey));
        builder.append(", endpointKeyHash=");
        builder.append(Arrays.toString(endpointKeyHash));
        builder.append(", endpointUserId=");
        builder.append(endpointUserId);
        builder.append(", accessToken=");
        builder.append(accessToken);
        builder.append(", groupState=");
        builder.append(groupState);
        builder.append(", subscriptions=");
        builder.append(subscriptions);
        builder.append(", simpleNotificationHash=");
        builder.append(simpleTopicHash);
        builder.append(", notificationHash=");
        builder.append(Arrays.toString(topicHash));
        builder.append(", sequenceNumber=");
        builder.append(sequenceNumber);
        builder.append(", clientProfileBody=");
        builder.append(clientProfileBody);
        builder.append(", profileHash=");
        builder.append(Arrays.toString(profileHash));
        builder.append(", configurationHash=");
        builder.append(Arrays.toString(configurationHash));
        builder.append(", userConfigurationHash=");
        builder.append(Arrays.toString(userConfigurationHash));
        builder.append(", clientProfileVersion=");
        builder.append(clientProfileVersion);
        builder.append(", configurationVersion=");
        builder.append(configurationVersion);
        builder.append(", notificationVersion=");
        builder.append(notificationVersion);
        builder.append(", systemNfVersion=");
        builder.append(systemNfVersion);
        builder.append(", userNfVersion=");
        builder.append(userNfVersion);
        builder.append(", ecfVersionStates=");
        builder.append(ecfVersionStates);
        builder.append(", serverHash=");
        builder.append(serverHash);
        builder.append(", sdkToken=");
        builder.append(sdkToken);
        builder.append(", useRawSchema=");
        builder.append(useConfigurationRawSchema);
        builder.append("]");
        return builder.toString();
    }
}
