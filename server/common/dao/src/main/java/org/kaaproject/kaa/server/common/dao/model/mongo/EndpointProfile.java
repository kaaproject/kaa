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

package org.kaaproject.kaa.server.common.dao.model.mongo;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoToModelList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertECFVersionDtoToModelList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@Document(collection = EndpointProfile.COLLECTION_NAME)
public final class EndpointProfile implements ToDto<EndpointProfileDto>, Serializable {

    private static final long serialVersionUID = -3227246639864687299L;

    public static final String COLLECTION_NAME = "endpoint_profile";

    @Id
    private String id;
    @Field("application_id")
    private String applicationId;
    @Field("endpoint_key")
    private byte[] endpointKey;
    @Indexed
    @Field("endpoint_key_hash")
    private byte[] endpointKeyHash;
    @Indexed
    @Field("endpoint_user_id")
    private String endpointUserId;
    @Indexed
    @Field("access_token")
    private String accessToken;
    @Field("profile_schema_id")
    private String profileSchemaId;
    @Field("cf_group_state")
    private List<EndpointGroupState> cfGroupState;
    @Field("nf_group_state")
    private List<EndpointGroupState> nfGroupState;
    @Field("cf_seq_num")
    private int cfSequenceNumber;
    @Field("nf_seq_num")
    private int nfSequenceNumber;
    @Field("changed_flag")
    private Boolean changedFlag;
    private DBObject profile;
    @Field("profile_hash")
    private byte[] profileHash;
    @Field("profile_version")
    private int profileVersion;
    @Field("configuration_hash")
    private byte[] configurationHash;
    @Field("configuration_version")
    private int configurationVersion;
    @Field("notification_version")
    private int notificationVersion;
    private List<String> subscriptions;
    @Field("nt_hash")
    private byte[] ntHash;
    @Field("system_nf_version")
    private int systemNfVersion;
    @Field("user_nf_version")
    private int userNfVersion;
    @Field("log_schema_version")
    private int logSchemaVersion;
    @Field("ecf_version_state")
    private List<EventClassFamilyVersionState> ecfVersionStates;
    @Field("server_hash")
    private String serverHash;


    public EndpointProfile() {
    }

    public EndpointProfile(EndpointProfileDto dto) {
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
        this.changedFlag = dto.getChangedFlag();
        this.profile = (DBObject) JSON.parse(dto.getProfile());
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

    public String getProfileSchemaId() {
        return profileSchemaId;
    }

    public List<EndpointGroupState> getCfGroupState() {
        return cfGroupState;
    }

    public void setCfGroupState(List<EndpointGroupState> cfGroupState) {
        this.cfGroupState = cfGroupState;
    }

    public List<EndpointGroupState> getNfGroupState() {
        return nfGroupState;
    }

    public void setNfGroupState(List<EndpointGroupState> nfGroupState) {
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

    public Boolean getChangedFlag() {
        return changedFlag;
    }

    public void setChangedFlag(Boolean changedFlag) {
        this.changedFlag = changedFlag;
    }

    public DBObject getProfile() {
        return profile;
    }

    public void setProfile(DBObject profile) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointProfile)) {
            return false;
        }

        EndpointProfile that = (EndpointProfile) o;

        if (configurationVersion != that.configurationVersion) {
            return false;
        }
        if (notificationVersion != that.notificationVersion) {
            return false;
        }
        if (profileVersion != that.profileVersion) {
            return false;
        }
        if (cfSequenceNumber != that.cfSequenceNumber) {
            return false;
        }
        if (nfSequenceNumber != that.nfSequenceNumber) {
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
        if (cfGroupState != null ? !cfGroupState.equals(that.cfGroupState) : that.cfGroupState != null) {
            return false;
        }
        if (nfGroupState != null ? !nfGroupState.equals(that.nfGroupState) : that.nfGroupState != null) {
            return false;
        }
        if (!Arrays.equals(endpointKey, that.endpointKey)) {
            return false;
        }
        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
            return false;
        }
        if (!Arrays.equals(ntHash, that.ntHash)) {
            return false;
        }
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) {
            return false;
        }
        if (!Arrays.equals(profileHash, that.profileHash)) {
            return false;
        }
        if (profileSchemaId != null ? !profileSchemaId.equals(that.profileSchemaId) : that.profileSchemaId != null) {
            return false;
        }
        if (subscriptions != null ? !subscriptions.equals(that.subscriptions) : that.subscriptions != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId != null ? applicationId.hashCode() : 0;
        result = 31 * result + (endpointKey != null ? Arrays.hashCode(endpointKey) : 0);
        result = 31 * result + (endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0);
        result = 31 * result + (profileSchemaId != null ? profileSchemaId.hashCode() : 0);
        result = 31 * result + (cfGroupState != null ? cfGroupState.hashCode() : 0);
        result = 31 * result + (nfGroupState != null ? nfGroupState.hashCode() : 0);
        result = 31 * result + cfSequenceNumber;
        result = 31 * result + nfSequenceNumber;
        result = 31 * result + (changedFlag != null ? changedFlag.hashCode() : 0);
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
        return result;
    }

    @Override
    public String toString() {
        return "EndpointProfile{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", endpointKey=" + Arrays.toString(endpointKey) +
                ", endpointKeyHash=" + Arrays.toString(endpointKeyHash) +
                ", profileSchemaId=" + profileSchemaId +
                ", cfGroupState=" + cfGroupState +
                ", nfGroupState=" + nfGroupState +
                ", cfSequenceNumber=" + cfSequenceNumber +
                ", nfSequenceNumber=" + nfSequenceNumber +
                ", changedFlag=" + changedFlag +
                ", profile=" + profile +
                ", profileHash=" + Arrays.toString(profileHash) +
                ", profileVersion=" + profileVersion +
                ", configurationHash=" + Arrays.toString(configurationHash) +
                ", configurationVersion=" + configurationVersion +
                ", notificationVersion=" + notificationVersion +
                ", subscriptions=" + subscriptions +
                ", ntHash=" + Arrays.toString(ntHash) +
                ", systemNfVersion=" + systemNfVersion +
                ", userNfVersion=" + userNfVersion +
                '}';
    }

    @Override
    public EndpointProfileDto toDto() {
        EndpointProfileDto dto = new EndpointProfileDto();
        dto.setId(id);
        dto.setCfGroupStates(DaoUtil.<EndpointGroupStateDto>convertDtoList(cfGroupState));
        dto.setNfGroupStates(DaoUtil.<EndpointGroupStateDto>convertDtoList(nfGroupState));
        dto.setChangedFlag(changedFlag);
        dto.setCfSequenceNumber(cfSequenceNumber);
        dto.setNfSequenceNumber(nfSequenceNumber);
        dto.setConfigurationHash(configurationHash);
        dto.setConfigurationVersion(configurationVersion);
        dto.setApplicationId(applicationId);
        dto.setEndpointKey(endpointKey);
        dto.setEndpointKeyHash(endpointKeyHash);
        dto.setEndpointUserId(endpointUserId);
        dto.setAccessToken(accessToken);
        dto.setProfile(profile != null ? profile.toString() : null);
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
