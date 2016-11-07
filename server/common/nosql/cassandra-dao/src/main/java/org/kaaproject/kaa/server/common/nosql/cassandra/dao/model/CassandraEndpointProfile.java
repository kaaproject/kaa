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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertDtoToModelList;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertECFVersionDtoToModelList;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.*;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEndpointGroupState;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEventClassFamilyVersionState;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(name = EP_COLUMN_FAMILY_NAME)
public final class CassandraEndpointProfile implements EndpointProfile, Serializable {

    @Transient
    private static final long serialVersionUID = -3227246639864687299L;

    @PartitionKey
    @Column(name = EP_EP_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;
    @Column(name = EP_ENDPOINT_ID_PROPERTY)
    private String id;
    @Column(name = EP_EP_KEY_PROPERTY)
    private ByteBuffer endpointProfileKey;
    @Column(name = EP_APP_ID_PROPERTY)
    private String applicationId;
    @Column(name = EP_USER_ID_PROPERTY)
    private String endpointUserId;
    @Column(name = EP_ACCESS_TOKEN_PROPERTY)
    private String accessToken;
    @Column(name = EP_GROUP_STATE_PROPERTY)
    @FrozenValue
    private List<CassandraEndpointGroupState> groupStates;
    @Column(name = EP_SEQUENCE_NUMBER_PROPERTY)
    private int sequenceNumber;
    @Column(name = EP_PROFILE_PROPERTY)
    private String profile;
    @Column(name = EP_PROFILE_HASH_PROPERTY)
    private ByteBuffer profileHash;
    @Column(name = EP_PROFILE_VERSION_PROPERTY)
    private int profileVersion;
    @Column(name = EP_SERVER_PROFILE_VERSION_PROPERTY)
    private int serverProfileVersion;
    @Column(name = EP_CONFIG_HASH_PROPERTY)
    private ByteBuffer configurationHash;
    @Column(name = EP_USER_CONFIG_HASH_PROPERTY)
    private ByteBuffer userConfigurationHash;
    @Column(name = EP_CONFIGURATION_VERSION_PROPERTY)
    private int configurationVersion;
    @Column(name = EP_NOTIFICATION_VERSION_PROPERTY)
    private int notificationVersion;
    @Column(name = EP_SUBSCRIPTIONS_PROPERTY)
    private List<String> subscriptions;
    @Column(name = EP_TOPIC_HASH_PROPERTY)
    private ByteBuffer topicHash;
    @Column(name = EP_SIMPLE_TOPIC_HASH_PROPERTY)
    private int simpleTopicHash;
    @Column(name = EP_SYSTEM_NOTIFICATION_VERSION_PROPERTY)
    private int systemNfVersion;
    @Column(name = EP_USER_NOTIFICATION_VERSION_PROPERTY)
    private int userNfVersion;
    @Column(name = EP_LOG_SCHEMA_VERSION_PROPERTY)
    private int logSchemaVersion;
    @Column(name = EP_ECF_VERSION_STATE_PROPERTY)
    @FrozenValue
    private List<CassandraEventClassFamilyVersionState> ecfVersionStates;
    @Column(name = EP_SERVER_HASH_PROPERTY)
    private String serverHash;
    @Column(name = EP_SDK_TOKEN_PROPERTY)
    private String sdkToken;
    @Column(name = EP_SERVER_PROFILE_PROPERTY)
    private String serverProfile;

    @Column(name = EP_USE_RAW_SCHEMA)
    private Boolean useConfigurationRawSchema;

    @Column(name = OPT_LOCK)
    private Long version;

    public CassandraEndpointProfile() {
    }

    public CassandraEndpointProfile(EndpointProfileDto dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.endpointProfileKey = getByteBuffer(dto.getEndpointKey());
        this.endpointKeyHash = getByteBuffer(dto.getEndpointKeyHash());
        this.endpointUserId = dto.getEndpointUserId();
        this.accessToken = dto.getAccessToken();
        this.groupStates = convertDtoToModelList(dto.getGroupState());
        this.sequenceNumber = dto.getSequenceNumber();
        this.profile = dto.getClientProfileBody();
        this.profileHash = getByteBuffer(dto.getProfileHash());
        this.profileVersion = dto.getClientProfileVersion();
        this.serverProfileVersion = dto.getServerProfileVersion();
        this.configurationHash = getByteBuffer(dto.getConfigurationHash());
        this.userConfigurationHash = getByteBuffer(dto.getUserConfigurationHash());
        this.configurationVersion = dto.getConfigurationVersion();
        this.subscriptions = dto.getSubscriptions();
        this.notificationVersion = dto.getNotificationVersion();
        this.topicHash = getByteBuffer(dto.getTopicHash());
        this.simpleTopicHash = dto.getSimpleTopicHash();
        this.systemNfVersion = dto.getSystemNfVersion();
        this.userNfVersion = dto.getUserNfVersion();
        this.logSchemaVersion = dto.getLogSchemaVersion();
        this.ecfVersionStates = convertECFVersionDtoToModelList(dto.getEcfVersionStates());
        this.serverHash = dto.getServerHash();
        this.sdkToken = dto.getSdkToken();
        this.serverProfile = dto.getServerProfileBody();
        this.useConfigurationRawSchema = dto.isUseConfigurationRawSchema();
        this.version = dto.getVersion();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public byte[] getEndpointKey() {
        return getBytes(endpointProfileKey);
    }

    @Override
    public String getId() {
        return id;
    }

    public ByteBuffer getEndpointProfileKey() {
        return endpointProfileKey;
    }

    public void setEndpointProfileKey(ByteBuffer endpointProfileKey) {
        this.endpointProfileKey = endpointProfileKey;
    }

    public ByteBuffer getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
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

    public List<CassandraEndpointGroupState> getGroupStates() {
        return groupStates;
    }

    public void setGroupStates(List<CassandraEndpointGroupState> groupStates) {
        this.groupStates = groupStates;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public ByteBuffer getProfileHash() {
        return profileHash;
    }

    public void setProfileHash(ByteBuffer profileHash) {
        this.profileHash = profileHash;
    }

    public int getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(int profileVersion) {
        this.profileVersion = profileVersion;
    }

    public ByteBuffer getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(ByteBuffer configurationHash) {
        this.configurationHash = configurationHash;
    }

    public ByteBuffer getUserConfigurationHash() {
        return userConfigurationHash;
    }

    public void setUserConfigurationHash(ByteBuffer userConfigurationHash) {
        this.userConfigurationHash = userConfigurationHash;
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

    public int getServerProfileVersion() {
        return serverProfileVersion;
    }

    public void setServerProfileVersion(int serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    public String getServerProfile() {
        return serverProfile;
    }

    public void setServerProfile(String serverProfile) {
        this.serverProfile = serverProfile;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public ByteBuffer getTopicHash() {
        return topicHash;
    }

    public void setTopicHash(ByteBuffer topicHash) {
        this.topicHash = topicHash;
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

    public List<CassandraEventClassFamilyVersionState> getEcfVersionStates() {
        return ecfVersionStates;
    }

    public void setEcfVersionStates(List<CassandraEventClassFamilyVersionState> ecfVersionStates) {
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

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Boolean getUseConfigurationRawSchema() {
        return useConfigurationRawSchema;
    }

    public void setUseConfigurationRawSchema(Boolean useConfigurationRawSchema) {
        this.useConfigurationRawSchema = useConfigurationRawSchema;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraEndpointProfile that = (CassandraEndpointProfile) o;

        if (sequenceNumber != that.sequenceNumber) return false;
        if (profileVersion != that.profileVersion) return false;
        if (serverProfileVersion != that.serverProfileVersion) return false;
        if (configurationVersion != that.configurationVersion) return false;
        if (notificationVersion != that.notificationVersion) return false;
        if (systemNfVersion != that.systemNfVersion) return false;
        if (userNfVersion != that.userNfVersion) return false;
        if (logSchemaVersion != that.logSchemaVersion) return false;
        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (endpointProfileKey != null ? !endpointProfileKey.equals(that.endpointProfileKey) : that.endpointProfileKey != null)
            return false;
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) return false;
        if (endpointUserId != null ? !endpointUserId.equals(that.endpointUserId) : that.endpointUserId != null) return false;
        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) return false;
        if (groupStates != null ? !groupStates.equals(that.groupStates) : that.groupStates != null) return false;
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
        if (profileHash != null ? !profileHash.equals(that.profileHash) : that.profileHash != null) return false;
        if (configurationHash != null ? !configurationHash.equals(that.configurationHash) : that.configurationHash != null) return false;
        if (userConfigurationHash != null ? !userConfigurationHash.equals(that.userConfigurationHash) : that.userConfigurationHash != null)
            return false;
        if (subscriptions != null ? !subscriptions.equals(that.subscriptions) : that.subscriptions != null) return false;
        if (topicHash != null ? !topicHash.equals(that.topicHash) : that.topicHash != null) return false;
        if (ecfVersionStates != null ? !ecfVersionStates.equals(that.ecfVersionStates) : that.ecfVersionStates != null) return false;
        if (serverHash != null ? !serverHash.equals(that.serverHash) : that.serverHash != null) return false;
        if (sdkToken != null ? !sdkToken.equals(that.sdkToken) : that.sdkToken != null) return false;
        if (useConfigurationRawSchema != null ? !useConfigurationRawSchema.equals(that.useConfigurationRawSchema) : that.useConfigurationRawSchema != null) {
            return false;
        }
        return serverProfile != null ? serverProfile.equals(that.serverProfile) : that.serverProfile == null;

    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? endpointKeyHash.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (endpointProfileKey != null ? endpointProfileKey.hashCode() : 0);
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (endpointUserId != null ? endpointUserId.hashCode() : 0);
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        result = 31 * result + (groupStates != null ? groupStates.hashCode() : 0);
        result = 31 * result + sequenceNumber;
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (profileHash != null ? profileHash.hashCode() : 0);
        result = 31 * result + profileVersion;
        result = 31 * result + serverProfileVersion;
        result = 31 * result + (configurationHash != null ? configurationHash.hashCode() : 0);
        result = 31 * result + (userConfigurationHash != null ? userConfigurationHash.hashCode() : 0);
        result = 31 * result + configurationVersion;
        result = 31 * result + notificationVersion;
        result = 31 * result + (subscriptions != null ? subscriptions.hashCode() : 0);
        result = 31 * result + (topicHash != null ? topicHash.hashCode() : 0);
        result = 31 * result + systemNfVersion;
        result = 31 * result + userNfVersion;
        result = 31 * result + logSchemaVersion;
        result = 31 * result + (ecfVersionStates != null ? ecfVersionStates.hashCode() : 0);
        result = 31 * result + (serverHash != null ? serverHash.hashCode() : 0);
        result = 31 * result + (sdkToken != null ? sdkToken.hashCode() : 0);
        result = 31 * result + (useConfigurationRawSchema != null ? useConfigurationRawSchema.hashCode() : 0);
        result = 31 * result + (serverProfile != null ? serverProfile.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEndpointProfile{" +
                "endpointKeyHash=" + endpointKeyHash +
                ", id='" + id + '\'' +
                ", endpointProfileKey=" + endpointProfileKey +
                ", applicationId='" + applicationId + '\'' +
                ", endpointUserId='" + endpointUserId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", groupStates=" + groupStates +
                ", sequenceNumber=" + sequenceNumber +
                ", profile='" + profile + '\'' +
                ", profileHash=" + profileHash +
                ", profileVersion=" + profileVersion +
                ", serverProfileVersion=" + serverProfileVersion +
                ", configurationHash=" + configurationHash +
                ", userConfigurationHash=" + userConfigurationHash +
                ", configurationVersion=" + configurationVersion +
                ", notificationVersion=" + notificationVersion +
                ", subscriptions=" + subscriptions +
                ", topicHash=" + topicHash +
                ", simpleTopicHash=" + simpleTopicHash +
                ", systemNfVersion=" + systemNfVersion +
                ", userNfVersion=" + userNfVersion +
                ", logSchemaVersion=" + logSchemaVersion +
                ", ecfVersionStates=" + ecfVersionStates +
                ", serverHash='" + serverHash + '\'' +
                ", sdkToken='" + sdkToken + '\'' +
                ", useRawSchema=" + useConfigurationRawSchema +
                ", serverProfile='" + serverProfile + '\'' +
                '}';
    }

    @Override
    public EndpointProfileDto toDto() {
        EndpointProfileDto dto = new EndpointProfileDto();
        dto.setId(id);
        dto.setGroupState(DaoUtil.convertDtoList(groupStates));
        dto.setSequenceNumber(sequenceNumber);
        dto.setConfigurationHash(getBytes(configurationHash));
        dto.setUserConfigurationHash(getBytes(userConfigurationHash));
        dto.setConfigurationVersion(configurationVersion);
        dto.setApplicationId(applicationId);
        dto.setEndpointKey(getBytes(endpointProfileKey));
        dto.setEndpointKeyHash(getBytes(endpointKeyHash));
        dto.setEndpointUserId(endpointUserId);
        dto.setAccessToken(accessToken);
        dto.setClientProfileBody(profile);
        dto.setProfileHash(getBytes(profileHash));
        dto.setClientProfileVersion(profileVersion);
        dto.setServerProfileVersion(serverProfileVersion);
        dto.setNotificationVersion(notificationVersion);
        dto.setSubscriptions(subscriptions);
        dto.setTopicHash(getBytes(topicHash));
        dto.setSimpleTopicHash(simpleTopicHash);
        dto.setSystemNfVersion(systemNfVersion);
        dto.setUserNfVersion(userNfVersion);
        dto.setLogSchemaVersion(logSchemaVersion);
        dto.setEcfVersionStates(DaoUtil.<EventClassFamilyVersionStateDto>convertDtoList(ecfVersionStates));
        dto.setServerHash(serverHash);
        dto.setSdkToken(sdkToken);
        dto.setServerProfileBody(serverProfile);
        dto.setUseConfigurationRawSchema(useConfigurationRawSchema);
        dto.setVersion(version);
        return dto;
    }
}
