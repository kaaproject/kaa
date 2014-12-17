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

package org.kaaproject.kaa.server.common.dao.cassandra.model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.cassandra.CassandraDaoUtil.convertDtoToModelList;
import static org.kaaproject.kaa.server.common.dao.cassandra.CassandraDaoUtil.convertECFVersionDtoToModelList;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_ACCESS_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_CONFIGURATION_GROUP_STATE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_CONFIGURATION_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_CONFIGURATION_SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_CONFIGURATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_ECF_VERSION_STATE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_ENDPOINT_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_ENDPOINT_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_ENDPOINT_KEY_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_LOG_SCHEMA_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_NOTIFICATION_GROUP_STATE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_NOTIFICATION_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_NOTIFICATION_SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_NOTIFICATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_PROFILE_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_PROFILE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_PROFILE_SCHEMA_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_PROFILE_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_SERVER_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_SUBSCRIPTIONS_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_SYSTEM_NOTIFICATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.ENDPOINT_PROFILE_USER_NOTIFICATION_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;

@Table(name = ENDPOINT_PROFILE_COLUMN_FAMILY_NAME)
public final class CassandraEndpointProfile implements EndpointProfile, Serializable {

    @Transient
    private static final long serialVersionUID = -3227246639864687299L;

    @PartitionKey
    @Column(name = ENDPOINT_PROFILE_ENDPOINT_KEY_HASH_PROPERTY)
    private byte[] endpointKeyHash;
    @Column(name = ENDPOINT_PROFILE_ENDPOINT_ID_PROPERTY)
    private String id;
    @Column(name = ENDPOINT_PROFILE_ENDPOINT_KEY_PROPERTY)
    private byte[] endpointKey;
    @Column(name = ENDPOINT_PROFILE_APPLICATION_ID_PROPERTY)
    private String applicationId;
    @Column(name = ENDPOINT_PROFILE_USER_ID_PROPERTY)
    private String endpointUserId;
    @Column(name = ENDPOINT_PROFILE_ACCESS_TOKEN_PROPERTY)
    private String accessToken;
    @Column(name = ENDPOINT_PROFILE_PROFILE_SCHEMA_ID_PROPERTY)
    private String profileSchemaId;
    @Column(name = ENDPOINT_PROFILE_CONFIGURATION_GROUP_STATE_PROPERTY)
    private List<CassandraEndpointGroupState> cfGroupState;
    @Column(name = ENDPOINT_PROFILE_NOTIFICATION_GROUP_STATE_PROPERTY)
    private List<CassandraEndpointGroupState> nfGroupState;
    @Column(name = ENDPOINT_PROFILE_CONFIGURATION_SEQUENCE_NUMBER_PROPERTY)
    private int cfSequenceNumber;
    @Column(name = ENDPOINT_PROFILE_NOTIFICATION_SEQUENCE_NUMBER_PROPERTY)
    private int nfSequenceNumber;
    @Column(name = ENDPOINT_PROFILE_PROFILE_PROPERTY)
    private String profile;
    @Column(name = ENDPOINT_PROFILE_PROFILE_HASH_PROPERTY)
    private byte[] profileHash;
    @Column(name = ENDPOINT_PROFILE_PROFILE_VERSION_PROPERTY)
    private int profileVersion;
    @Column(name = ENDPOINT_PROFILE_CONFIGURATION_HASH_PROPERTY)
    private byte[] configurationHash;
    @Column(name = ENDPOINT_PROFILE_CONFIGURATION_VERSION_PROPERTY)
    private int configurationVersion;
    @Column(name = ENDPOINT_PROFILE_NOTIFICATION_VERSION_PROPERTY)
    private int notificationVersion;
    @Column(name = ENDPOINT_PROFILE_SUBSCRIPTIONS_PROPERTY)
    private List<String> subscriptions;
    @Column(name = ENDPOINT_PROFILE_NOTIFICATION_HASH_PROPERTY)
    private byte[] ntHash;
    @Column(name = ENDPOINT_PROFILE_SYSTEM_NOTIFICATION_VERSION_PROPERTY)
    private int systemNfVersion;
    @Column(name = ENDPOINT_PROFILE_USER_NOTIFICATION_VERSION_PROPERTY)
    private int userNfVersion;
    @Column(name = ENDPOINT_PROFILE_LOG_SCHEMA_VERSION_PROPERTY)
    private int logSchemaVersion;
    @Column(name = ENDPOINT_PROFILE_ECF_VERSION_STATE_PROPERTY)
    private List<CassandraEventClassFamilyVersionState> ecfVersionStates;
    @Column(name = ENDPOINT_PROFILE_SERVER_HASH_PROPERTY)
    private String serverHash;


    public CassandraEndpointProfile() {
    }

    public CassandraEndpointProfile(EndpointProfileDto dto) {
        this.id = dto.getId();
        this.applicationId = dto.getApplicationId();
        this.endpointKey = dto.getEndpointKey();
        this.endpointKeyHash = dto.getEndpointKeyHash();
        this.endpointUserId = dto.getEndpointUserId();
        this.accessToken = dto.getAccessToken();
        this.profileSchemaId = dto.getProfileSchemaId();
        this.cfGroupState = convertDtoToModelList(dto.getCfGroupStates());
        this.nfGroupState = convertDtoToModelList(dto.getNfGroupStates());
        this.cfSequenceNumber = dto.getCfSequenceNumber();
        this.nfSequenceNumber = dto.getNfSequenceNumber();
        this.profile = dto.getProfile();
        this.profileHash = dto.getProfileHash();
        this.profileVersion = dto.getProfileVersion();
        this.configurationHash = dto.getConfigurationHash();
        this.configurationVersion = dto.getConfigurationVersion();
        this.subscriptions = dto.getSubscriptions();
        this.notificationVersion = dto.getNotificationVersion();
        this.ntHash = dto.getNtHash();
        this.systemNfVersion = dto.getSystemNfVersion();
        this.userNfVersion = dto.getUserNfVersion();
        this.logSchemaVersion = dto.getLogSchemaVersion();
        this.ecfVersionStates = convertECFVersionDtoToModelList(dto.getEcfVersionStates());
        this.serverHash = dto.getServerHash();
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

    @Override
    public String getId() {
        return id;
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

    public String getProfileSchemaId() {
        return profileSchemaId;
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

    public byte[] getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(byte[] configurationHash) {
        this.configurationHash = getArrayCopy(configurationHash);
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

    public byte[] getNtHash() {
        return ntHash;
    }

    public void setNtHash(byte[] ntHash) {
        this.ntHash = getArrayCopy(ntHash);
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

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setProfileSchemaId(String profileSchemaId) {
        this.profileSchemaId = profileSchemaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraEndpointProfile that = (CassandraEndpointProfile) o;

        if (cfSequenceNumber != that.cfSequenceNumber) return false;
        if (configurationVersion != that.configurationVersion) return false;
        if (logSchemaVersion != that.logSchemaVersion) return false;
        if (nfSequenceNumber != that.nfSequenceNumber) return false;
        if (notificationVersion != that.notificationVersion) return false;
        if (profileVersion != that.profileVersion) return false;
        if (systemNfVersion != that.systemNfVersion) return false;
        if (userNfVersion != that.userNfVersion) return false;
        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) return false;
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        if (cfGroupState != null ? !cfGroupState.equals(that.cfGroupState) : that.cfGroupState != null) return false;
        if (!Arrays.equals(configurationHash, that.configurationHash)) return false;
        if (ecfVersionStates != null ? !ecfVersionStates.equals(that.ecfVersionStates) : that.ecfVersionStates != null)
            return false;
        if (!Arrays.equals(endpointKey, that.endpointKey)) return false;
        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) return false;
        if (endpointUserId != null ? !endpointUserId.equals(that.endpointUserId) : that.endpointUserId != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (nfGroupState != null ? !nfGroupState.equals(that.nfGroupState) : that.nfGroupState != null) return false;
        if (!Arrays.equals(ntHash, that.ntHash)) return false;
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
        if (!Arrays.equals(profileHash, that.profileHash)) return false;
        if (profileSchemaId != null ? !profileSchemaId.equals(that.profileSchemaId) : that.profileSchemaId != null)
            return false;
        if (serverHash != null ? !serverHash.equals(that.serverHash) : that.serverHash != null) return false;
        if (subscriptions != null ? !subscriptions.equals(that.subscriptions) : that.subscriptions != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (endpointKey != null ? Arrays.hashCode(endpointKey) : 0);
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (endpointUserId != null ? endpointUserId.hashCode() : 0);
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        result = 31 * result + (profileSchemaId != null ? profileSchemaId.hashCode() : 0);
        result = 31 * result + (cfGroupState != null ? cfGroupState.hashCode() : 0);
        result = 31 * result + (nfGroupState != null ? nfGroupState.hashCode() : 0);
        result = 31 * result + cfSequenceNumber;
        result = 31 * result + nfSequenceNumber;
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (profileHash != null ? Arrays.hashCode(profileHash) : 0);
        result = 31 * result + profileVersion;
        result = 31 * result + (configurationHash != null ? Arrays.hashCode(configurationHash) : 0);
        result = 31 * result + configurationVersion;
        result = 31 * result + notificationVersion;
        result = 31 * result + (subscriptions != null ? subscriptions.hashCode() : 0);
        result = 31 * result + (ntHash != null ? Arrays.hashCode(ntHash) : 0);
        result = 31 * result + systemNfVersion;
        result = 31 * result + userNfVersion;
        result = 31 * result + logSchemaVersion;
        result = 31 * result + (ecfVersionStates != null ? ecfVersionStates.hashCode() : 0);
        result = 31 * result + (serverHash != null ? serverHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointProfile{" +
                "endpointKeyHash=" + Arrays.toString(endpointKeyHash) +
                ", id='" + id + '\'' +
                ", endpointKey=" + Arrays.toString(endpointKey) +
                ", applicationId='" + applicationId + '\'' +
                ", endpointUserId='" + endpointUserId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", profileSchemaId='" + profileSchemaId + '\'' +
                ", cfGroupState=" + cfGroupState +
                ", nfGroupState=" + nfGroupState +
                ", cfSequenceNumber=" + cfSequenceNumber +
                ", nfSequenceNumber=" + nfSequenceNumber +
                ", profile='" + profile + '\'' +
                ", profileHash=" + Arrays.toString(profileHash) +
                ", profileVersion=" + profileVersion +
                ", configurationHash=" + Arrays.toString(configurationHash) +
                ", configurationVersion=" + configurationVersion +
                ", notificationVersion=" + notificationVersion +
                ", subscriptions=" + subscriptions +
                ", ntHash=" + Arrays.toString(ntHash) +
                ", systemNfVersion=" + systemNfVersion +
                ", userNfVersion=" + userNfVersion +
                ", logSchemaVersion=" + logSchemaVersion +
                ", ecfVersionStates=" + ecfVersionStates +
                ", serverHash='" + serverHash + '\'' +
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
        dto.setConfigurationHash(configurationHash);
        dto.setConfigurationVersion(configurationVersion);
        dto.setApplicationId(applicationId);
        dto.setEndpointKey(endpointKey);
        dto.setEndpointKeyHash(endpointKeyHash);
        dto.setEndpointUserId(endpointUserId);
        dto.setAccessToken(accessToken);
        dto.setProfile(profile);
        dto.setProfileHash(profileHash);
        dto.setProfileVersion(profileVersion);
        dto.setProfileSchemaId(profileSchemaId);
        dto.setNotificationVersion(notificationVersion);
        dto.setSubscriptions(subscriptions);
        dto.setNtHash(ntHash);
        dto.setSystemNfVersion(systemNfVersion);
        dto.setUserNfVersion(userNfVersion);
        dto.setLogSchemaVersion(logSchemaVersion);
        dto.setEcfVersionStates(DaoUtil.<EventClassFamilyVersionStateDto>convertDtoList(ecfVersionStates));
        dto.setServerHash(serverHash);
        return dto;
    }
}
