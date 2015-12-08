/*
 * Copyright 2015 CyberVision, Inc.
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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEndpointGroupState;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraEventClassFamilyVersionState;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertDtoToModelList;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.convertECFVersionDtoToModelList;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_ACCESS_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_APP_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_CONFIGURATION_SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_CONFIGURATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_CONFIG_GROUP_STATE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_CONFIG_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_ECF_VERSION_STATE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_ENDPOINT_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_EP_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_EP_KEY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_LOG_SCHEMA_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_NOTIFICATION_GROUP_STATE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_NOTIFICATION_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_NOTIFICATION_SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_NOTIFICATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_PROFILE_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_PROFILE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_PROFILE_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_SDK_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_SERVER_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_SERVER_PROFILE_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_SERVER_PROFILE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_SUBSCRIPTIONS_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_SYSTEM_NOTIFICATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONFIG_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_NOTIFICATION_VERSION_PROPERTY;

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
    @Column(name = EP_CONFIG_GROUP_STATE_PROPERTY)
    @FrozenValue
    private List<CassandraEndpointGroupState> cfGroupState;
    @Column(name = EP_NOTIFICATION_GROUP_STATE_PROPERTY)
    @FrozenValue
    private List<CassandraEndpointGroupState> nfGroupState;
    @Column(name = EP_CONFIGURATION_SEQUENCE_NUMBER_PROPERTY)
    private int cfSequenceNumber;
    @Column(name = EP_NOTIFICATION_SEQUENCE_NUMBER_PROPERTY)
    private int nfSequenceNumber;
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
    @Column(name = EP_NOTIFICATION_HASH_PROPERTY)
    private ByteBuffer ntHash;
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

    public CassandraEndpointProfile() {
    }

    public CassandraEndpointProfile(EndpointProfileDto dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.endpointProfileKey = getByteBuffer(dto.getEndpointKey());
        this.endpointKeyHash = getByteBuffer(dto.getEndpointKeyHash());
        this.endpointUserId = dto.getEndpointUserId();
        this.accessToken = dto.getAccessToken();
        this.cfGroupState = convertDtoToModelList(dto.getCfGroupStates());
        this.nfGroupState = convertDtoToModelList(dto.getNfGroupStates());
        this.cfSequenceNumber = dto.getCfSequenceNumber();
        this.nfSequenceNumber = dto.getNfSequenceNumber();
        this.profile = dto.getClientProfileBody();
        this.profileHash = getByteBuffer(dto.getProfileHash());
        this.profileVersion = dto.getClientProfileVersion();
        this.serverProfileVersion = dto.getServerProfileVersion();
        this.configurationHash = getByteBuffer(dto.getConfigurationHash());
        this.userConfigurationHash = getByteBuffer(dto.getUserConfigurationHash());
        this.configurationVersion = dto.getConfigurationVersion();
        this.subscriptions = dto.getSubscriptions();
        this.notificationVersion = dto.getNotificationVersion();
        this.ntHash = getByteBuffer(dto.getNtHash());
        this.systemNfVersion = dto.getSystemNfVersion();
        this.userNfVersion = dto.getUserNfVersion();
        this.logSchemaVersion = dto.getLogSchemaVersion();
        this.ecfVersionStates = convertECFVersionDtoToModelList(dto.getEcfVersionStates());
        this.serverHash = dto.getServerHash();
        this.sdkToken = dto.getSdkToken();
        this.serverProfile = dto.getServerProfileBody();
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

    public List<CassandraEndpointGroupState> getCfGroupState() {
        return cfGroupState;
    }

    public void setCfGroupState(List<CassandraEndpointGroupState> cfGroupState) {
        this.cfGroupState = cfGroupState;
    }

    public List<CassandraEndpointGroupState> getNfGroupState() {
        return nfGroupState;
    }

    public void setNfGroupState(List<CassandraEndpointGroupState> nfGroupState) {
        this.nfGroupState = nfGroupState;
    }

    public int getCfSequenceNumber() {
        return cfSequenceNumber;
    }

    public void setCfSequenceNumber(int cfSequenceNumber) {
        this.cfSequenceNumber = cfSequenceNumber;
    }

    public int getNfSequenceNumber() {
        return nfSequenceNumber;
    }

    public void setNfSequenceNumber(int nfSequenceNumber) {
        this.nfSequenceNumber = nfSequenceNumber;
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

    @Override
    public int getServerProfileVersion() {
        return serverProfileVersion;
    }

    public void setServerProfileVersion(int serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    @Override
    public String getServerProfile() {
        return serverProfile;
    }

    public void setServerProfile(String serverProfile) {
        this.serverProfile = serverProfile;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public ByteBuffer getNtHash() {
        return ntHash;
    }

    public void setNtHash(ByteBuffer ntHash) {
        this.ntHash = ntHash;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraEndpointProfile that = (CassandraEndpointProfile) o;

        if (cfSequenceNumber != that.cfSequenceNumber) return false;
        if (nfSequenceNumber != that.nfSequenceNumber) return false;
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
        if (cfGroupState != null ? !cfGroupState.equals(that.cfGroupState) : that.cfGroupState != null) return false;
        if (nfGroupState != null ? !nfGroupState.equals(that.nfGroupState) : that.nfGroupState != null) return false;
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
        if (profileHash != null ? !profileHash.equals(that.profileHash) : that.profileHash != null) return false;
        if (configurationHash != null ? !configurationHash.equals(that.configurationHash) : that.configurationHash != null) return false;
        if (userConfigurationHash != null ? !userConfigurationHash.equals(that.userConfigurationHash) : that.userConfigurationHash != null)
            return false;
        if (subscriptions != null ? !subscriptions.equals(that.subscriptions) : that.subscriptions != null) return false;
        if (ntHash != null ? !ntHash.equals(that.ntHash) : that.ntHash != null) return false;
        if (ecfVersionStates != null ? !ecfVersionStates.equals(that.ecfVersionStates) : that.ecfVersionStates != null) return false;
        if (serverHash != null ? !serverHash.equals(that.serverHash) : that.serverHash != null) return false;
        if (sdkToken != null ? !sdkToken.equals(that.sdkToken) : that.sdkToken != null) return false;
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
        result = 31 * result + (cfGroupState != null ? cfGroupState.hashCode() : 0);
        result = 31 * result + (nfGroupState != null ? nfGroupState.hashCode() : 0);
        result = 31 * result + cfSequenceNumber;
        result = 31 * result + nfSequenceNumber;
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (profileHash != null ? profileHash.hashCode() : 0);
        result = 31 * result + profileVersion;
        result = 31 * result + serverProfileVersion;
        result = 31 * result + (configurationHash != null ? configurationHash.hashCode() : 0);
        result = 31 * result + (userConfigurationHash != null ? userConfigurationHash.hashCode() : 0);
        result = 31 * result + configurationVersion;
        result = 31 * result + notificationVersion;
        result = 31 * result + (subscriptions != null ? subscriptions.hashCode() : 0);
        result = 31 * result + (ntHash != null ? ntHash.hashCode() : 0);
        result = 31 * result + systemNfVersion;
        result = 31 * result + userNfVersion;
        result = 31 * result + logSchemaVersion;
        result = 31 * result + (ecfVersionStates != null ? ecfVersionStates.hashCode() : 0);
        result = 31 * result + (serverHash != null ? serverHash.hashCode() : 0);
        result = 31 * result + (sdkToken != null ? sdkToken.hashCode() : 0);
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
                ", cfGroupState=" + cfGroupState +
                ", nfGroupState=" + nfGroupState +
                ", cfSequenceNumber=" + cfSequenceNumber +
                ", nfSequenceNumber=" + nfSequenceNumber +
                ", profile='" + profile + '\'' +
                ", profileHash=" + profileHash +
                ", profileVersion=" + profileVersion +
                ", serverProfileVersion=" + serverProfileVersion +
                ", configurationHash=" + configurationHash +
                ", userConfigurationHash=" + userConfigurationHash +
                ", configurationVersion=" + configurationVersion +
                ", notificationVersion=" + notificationVersion +
                ", subscriptions=" + subscriptions +
                ", ntHash=" + ntHash +
                ", systemNfVersion=" + systemNfVersion +
                ", userNfVersion=" + userNfVersion +
                ", logSchemaVersion=" + logSchemaVersion +
                ", ecfVersionStates=" + ecfVersionStates +
                ", serverHash='" + serverHash + '\'' +
                ", sdkToken='" + sdkToken + '\'' +
                ", serverProfile='" + serverProfile + '\'' +
                '}';
    }

    @Override
    public EndpointProfileDto toDto() {
        EndpointProfileDto dto = new EndpointProfileDto();
        dto.setId(id);
        dto.setCfGroupStates(DaoUtil.<EndpointGroupStateDto>convertDtoList(cfGroupState));
        dto.setNfGroupStates(DaoUtil.<EndpointGroupStateDto>convertDtoList(nfGroupState));
        dto.setCfSequenceNumber(cfSequenceNumber);
        dto.setNfSequenceNumber(nfSequenceNumber);
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
        dto.setNtHash(getBytes(ntHash));
        dto.setSystemNfVersion(systemNfVersion);
        dto.setUserNfVersion(userNfVersion);
        dto.setLogSchemaVersion(logSchemaVersion);
        dto.setEcfVersionStates(DaoUtil.<EventClassFamilyVersionStateDto>convertDtoList(ecfVersionStates));
        dto.setServerHash(serverHash);
        dto.setSdkToken(sdkToken);
        dto.setServerProfileBody(serverProfile);
        return dto;
    }
}
