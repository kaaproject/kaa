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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@Document(collection = ENDPOINT_PROFILE)
public final class MongoEndpointProfile implements EndpointProfile, Serializable {

    private static final long serialVersionUID = -3227246639864687299L;

    @Id
    private String id;
    @Field(EP_APPLICATION_ID)
    private String applicationId;
    @Field(EP_ENDPOINT_KEY)
    private byte[] endpointKey;
    @Indexed
    @Field(EP_ENDPOINT_KEY_HASH)
    private byte[] endpointKeyHash;
    @Indexed
    @Field(EP_USER_ID)
    private String endpointUserId;
    @Indexed
    @Field(EP_ACCESS_TOKEN)
    private String accessToken;
    @Field(EP_GROUP_STATE)
    private List<EndpointGroupState> groupState;
    @Field(EP_SEQ_NUM)
    private int sequenceNumber;
    @Field(EP_CHANGED_FLAG)
    private Boolean changedFlag;
    private DBObject profile;
    @Field(EP_PROFILE_HASH)
    private byte[] profileHash;
    @Field(EP_PROFILE_VERSION)
    private int profileVersion;
    @Field(EP_SERVER_PROFILE_VERSION_PROPERTY)
    private int serverProfileVersion;
    @Field(EP_CONFIGURATION_HASH)
    private byte[] configurationHash;
    @Field(EP_USER_CONFIGURATION_HASH)
    private byte[] userConfigurationHash;
    @Field(EP_CONFIGURATION_VERSION)
    private int configurationVersion;
    @Field(EP_NOTIFICATION_VERSION)
    private int notificationVersion;
    private List<String> subscriptions;
    @Field(EP_TOPIC_HASH)
    private byte[] topicHash;
    @Field(EP_SIMPLE_TOPIC_HASH)
    private int simpleTopicHash;
    @Field(EP_SYSTEM_NF_VERSION)
    private int systemNfVersion;
    @Field(EP_USER_NF_VERSION)
    private int userNfVersion;
    @Field(EP_LOG_SCHEMA_VERSION)
    private int logSchemaVersion;
    @Field(EP_ECF_VERSION_STATE)
    private List<EventClassFamilyVersionState> ecfVersionStates;
    @Field(EP_SERVER_HASH)
    private String serverHash;
    @Indexed
    @Field(EP_SDK_TOKEN)
    private String sdkToken;
    @Field(EP_SERVER_PROFILE_PROPERTY)
    private DBObject serverProfile;
    @Field(EP_USE_RAW_SCHEMA)
    private Boolean useConfigurationRawSchema;
    @Version
    @Field(OPT_LOCK)
    private Long version;

    public MongoEndpointProfile() {
    }

    public MongoEndpointProfile(EndpointProfileDto dto) {
        this(dto, null);
    }

    public MongoEndpointProfile(EndpointProfileDto dto, Long version) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.endpointKey = dto.getEndpointKey();
        this.endpointKeyHash = dto.getEndpointKeyHash();
        this.endpointUserId = dto.getEndpointUserId();
        this.accessToken = dto.getAccessToken();
        this.groupState = MongoDaoUtil.convertDtoToModelList(dto.getGroupState());
        this.sequenceNumber = dto.getSequenceNumber();
        this.profile = MongoDaoUtil.encodeReservedCharacteres((DBObject) JSON.parse(dto.getClientProfileBody()));
        this.profileHash = dto.getProfileHash();
        this.profileVersion = dto.getClientProfileVersion();
        this.serverProfileVersion = dto.getServerProfileVersion();
        this.configurationHash = dto.getConfigurationHash();
        this.userConfigurationHash = dto.getUserConfigurationHash();
        this.configurationVersion = dto.getConfigurationVersion();
        this.subscriptions = dto.getSubscriptions();
        this.notificationVersion = dto.getNotificationVersion();
        this.topicHash = dto.getTopicHash();
        this.simpleTopicHash = dto.getSimpleTopicHash();
        this.systemNfVersion = dto.getSystemNfVersion();
        this.userNfVersion = dto.getUserNfVersion();
        this.logSchemaVersion = dto.getLogSchemaVersion();
        this.ecfVersionStates = MongoDaoUtil.convertECFVersionDtoToModelList(dto.getEcfVersionStates());
        this.serverHash = dto.getServerHash();
        this.sdkToken = dto.getSdkToken();
        this.serverProfile = MongoDaoUtil.encodeReservedCharacteres((DBObject) JSON.parse(dto.getServerProfileBody()));
        this.useConfigurationRawSchema = dto.isUseConfigurationRawSchema();
        this.version = dto.getVersion();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
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

    public List<EndpointGroupState> getGroupState() {
        return groupState;
    }

    public void setGroupState(List<EndpointGroupState> groupState) {
        this.groupState = groupState;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Boolean getChangedFlag() {
        return changedFlag;
    }

    public void setChangedFlag(Boolean changedFlag) {
        this.changedFlag = changedFlag;
    }

    public byte[] getProfileHash() {
        return profileHash;
    }

    public void setProfileHash(byte[] profileHash) {
        this.profileHash = getArrayCopy(profileHash);
    }

    public int getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(int profileVersion) {
        this.profileVersion = profileVersion;
    }

    public int getServerProfileVersion() {
        return serverProfileVersion;
    }

    public void setServerProfileVersion(int serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
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
        this.userConfigurationHash = getArrayCopy(userConfigurationHash);
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(int configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public int getNotificationVersion() {
        return notificationVersion;
    }

    public void setNotificationVersion(int notificationVersion) {
        this.notificationVersion = notificationVersion;
    }

    @Override
    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public byte[] getTopicHash() {
        return topicHash;
    }

    public void setTopicHash(byte[] topicHash) {
        this.topicHash = getArrayCopy(topicHash);
    }

    public int getSimpleTopicHash() {
        return simpleTopicHash;
    }

    public void setSimpleTopicHash(int simpleTopicHash) {
        this.simpleTopicHash = simpleTopicHash;
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

    public List<EventClassFamilyVersionState> getEcfVersionStates() {
        return ecfVersionStates;
    }

    public void setEcfVersionStates(List<EventClassFamilyVersionState> ecfVersionStates) {
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

    public Boolean getUseConfigurationRawSchema() {
        return useConfigurationRawSchema;
    }

    public void setUseConfigurationRawSchema(Boolean useConfigurationRawSchema) {
        this.useConfigurationRawSchema = useConfigurationRawSchema;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MongoEndpointProfile)) {
            return false;
        }

        MongoEndpointProfile that = (MongoEndpointProfile) o;

        if (configurationVersion != that.configurationVersion) {
            return false;
        }
        if (notificationVersion != that.notificationVersion) {
            return false;
        }
        if (profileVersion != that.profileVersion) {
            return false;
        }
        if (serverProfileVersion != that.serverProfileVersion) {
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
        if (changedFlag != null ? !changedFlag.equals(that.changedFlag) : that.changedFlag != null) {
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
        if (!Arrays.equals(topicHash, that.topicHash)) {
            return false;
        }
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) {
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
        if (useConfigurationRawSchema != null ? !useConfigurationRawSchema.equals(that.useConfigurationRawSchema) : that.useConfigurationRawSchema != null) {
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
        result = 31 * result + sequenceNumber;
        result = 31 * result + (changedFlag != null ? changedFlag.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (profileHash != null ? Arrays.hashCode(profileHash) : 0);
        result = 31 * result + profileVersion;
        result = 31 * result + serverProfileVersion;
        result = 31 * result + (configurationHash != null ? Arrays.hashCode(configurationHash) : 0);
        result = 31 * result + (userConfigurationHash != null ? Arrays.hashCode(userConfigurationHash) : 0);
        result = 31 * result + configurationVersion;
        result = 31 * result + notificationVersion;
        result = 31 * result + (subscriptions != null ? subscriptions.hashCode() : 0);
        result = 31 * result + (topicHash != null ? Arrays.hashCode(topicHash) : 0);
        result = 31 * result + systemNfVersion;
        result = 31 * result + userNfVersion;
        result = 31 * result + (useConfigurationRawSchema != null ? useConfigurationRawSchema.hashCode() : 0);
        result = 31 * result + (sdkToken != null ? sdkToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointProfile{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", endpointKey=" + Arrays.toString(endpointKey) +
                ", endpointKeyHash=" + Arrays.toString(endpointKeyHash) +
                ", groupState=" + groupState +
                ", sequenceNumber=" + sequenceNumber +
                ", changedFlag=" + changedFlag +
                ", profile=" + profile +
                ", profileHash=" + Arrays.toString(profileHash) +
                ", profileVersion=" + profileVersion +
                ", serverProfileVersion=" + serverProfileVersion +
                ", configurationHash=" + Arrays.toString(configurationHash) +
                ", userConfigurationHash=" + Arrays.toString(userConfigurationHash) +
                ", configurationVersion=" + configurationVersion +
                ", notificationVersion=" + notificationVersion +
                ", subscriptions=" + subscriptions +
                ", topicHash=" + Arrays.toString(topicHash) +
                ", simpleTopicHash=" + simpleTopicHash +
                ", systemNfVersion=" + systemNfVersion +
                ", userNfVersion=" + userNfVersion +
                ", sdkToken=" + sdkToken +
                ", useRawSchema=" + useConfigurationRawSchema +
                ", version=" + version +
                '}';
    }

    @Override
    public EndpointProfileDto toDto() {
        EndpointProfileDto dto = new EndpointProfileDto();
        dto.setId(id);
        dto.setGroupState(DaoUtil.convertDtoList(groupState));
        dto.setSequenceNumber(sequenceNumber);
        dto.setConfigurationHash(configurationHash);
        dto.setUserConfigurationHash(userConfigurationHash);
        dto.setConfigurationVersion(configurationVersion);
        dto.setApplicationId(applicationId);
        dto.setEndpointKey(endpointKey);
        dto.setEndpointKeyHash(endpointKeyHash);
        dto.setEndpointUserId(endpointUserId);
        dto.setAccessToken(accessToken);
        dto.setClientProfileBody(profile != null ? MongoDaoUtil.decodeReservedCharacteres(profile).toString() : "");
        dto.setProfileHash(profileHash);
        dto.setClientProfileVersion(profileVersion);
        dto.setServerProfileVersion(serverProfileVersion);
        dto.setNotificationVersion(notificationVersion);
        dto.setSubscriptions(subscriptions);
        dto.setTopicHash(topicHash);
        dto.setSimpleTopicHash(simpleTopicHash);
        dto.setSystemNfVersion(systemNfVersion);
        dto.setUserNfVersion(userNfVersion);
        dto.setLogSchemaVersion(logSchemaVersion);
        dto.setEcfVersionStates(DaoUtil.convertDtoList(ecfVersionStates));
        dto.setServerHash(serverHash);
        dto.setSdkToken(sdkToken);
        dto.setServerProfileBody(serverProfile != null ? MongoDaoUtil.decodeReservedCharacteres(serverProfile).toString() : "");
        dto.setUseConfigurationRawSchema(useConfigurationRawSchema);
        dto.setVersion(version);
        return dto;
    }
}
