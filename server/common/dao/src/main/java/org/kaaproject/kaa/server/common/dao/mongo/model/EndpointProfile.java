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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.convertDtoToModelList;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.DaoUtil;
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
    private ObjectId applicationId;
    @Field("endpoint_key")
    private byte[] endpointKey;
    @Indexed
    @Field("endpoint_key_hash")
    private byte[] endpointKeyHash;
    @Field("profile_schema_id")
    private ObjectId profileSchemaId;
    @Field("endpoint_group_state")
    private List<EndpointGroupState> endpointGroup;
    @Field("seq_num")
    private int sequenceNumber;
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

    public EndpointProfile() {
    }

    public EndpointProfile(EndpointProfileDto dto) {
        this.id = dto.getId();
        this.applicationId = idToObjectId(dto.getApplicationId());
        this.endpointKey = dto.getEndpointKey();
        this.endpointKeyHash = dto.getEndpointKeyHash();
        this.profileSchemaId = idToObjectId(dto.getProfileSchemaId());
        this.endpointGroup = convertDtoToModelList(dto.getEndpointGroups());
        this.sequenceNumber = dto.getSequenceNumber();
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
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectId getApplicationId() {
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

    public ObjectId getProfileSchemaId() {
        return profileSchemaId;
    }

    public List<EndpointGroupState> getEndpointGroup() {
        return endpointGroup;
    }

    public void setEndpointGroup(List<EndpointGroupState> endpointGroup) {
        this.endpointGroup = endpointGroup;
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
        if (endpointGroup != null ? !endpointGroup.equals(that.endpointGroup) : that.endpointGroup != null) {
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
        result = 31 * result + (endpointGroup != null ? endpointGroup.hashCode() : 0);
        result = 31 * result + sequenceNumber;
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
                ", endpointGroup=" + endpointGroup +
                ", sequenceNumber=" + sequenceNumber +
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
        dto.setEndpointGroups(DaoUtil.<EndpointGroupStateDto>convertDtoList(endpointGroup));
        dto.setChangedFlag(changedFlag);
        dto.setSequenceNumber(sequenceNumber);
        dto.setConfigurationHash(configurationHash);
        dto.setConfigurationVersion(configurationVersion);
        dto.setApplicationId(idToString(applicationId));
        dto.setEndpointKey(endpointKey);
        dto.setEndpointKeyHash(endpointKeyHash);
        dto.setProfile(profile != null ? profile.toString() : null);
        dto.setProfileHash(profileHash);
        dto.setProfileVersion(profileVersion);
        dto.setProfileSchemaId(idToString(profileSchemaId));
        dto.setNotificationVersion(notificationVersion);
        dto.setSubscriptions(subscriptions);
        dto.setNtHash(ntHash);
        dto.setSystemNfVersion(systemNfVersion);
        dto.setUserNfVersion(userNfVersion);
        return dto;
    }
}
