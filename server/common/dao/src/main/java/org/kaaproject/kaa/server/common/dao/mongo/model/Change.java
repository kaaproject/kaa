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

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

public class Change implements ToDto<ChangeDto>, Serializable {

    private static final long serialVersionUID = 570258142189899994L;

    private ChangeType type;
    @Field("endpoint_group_id")
    private ObjectId endpointGroupId;
    @Field("profile_filter_id")
    private ObjectId profileFilterId;
    @Field("pf_major_version")
    private int pfMajorVersion;
    @Field("configuration_id")
    private ObjectId configurationId;
    @Field("cf_major_version")
    private int cfMajorVersion;
    @Field("topic_id")
    private ObjectId topicId;


    public Change() {
    }

    public Change(ChangeDto dto) {
        this.type = dto.getType();
        this.endpointGroupId = idToObjectId(dto.getEndpointGroupId());
        this.profileFilterId = idToObjectId(dto.getProfileFilterId());
        this.pfMajorVersion = dto.getPfMajorVersion();
        this.configurationId = idToObjectId(dto.getConfigurationId());
        this.cfMajorVersion = dto.getCfMajorVersion();
        this.topicId = idToObjectId(dto.getTopicId());
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public ObjectId getEndpointGroupId() {
        return endpointGroupId;
    }

    public void setEndpointGroupId(ObjectId endpointGroupId) {
        this.endpointGroupId = endpointGroupId;
    }

    public ObjectId getProfileFilterId() {
        return profileFilterId;
    }

    public void setProfileFilterId(ObjectId profileFilterId) {
        this.profileFilterId = profileFilterId;
    }

    public int getPfMajorVersion() {
        return pfMajorVersion;
    }

    public void setPfMajorVersion(int pfMajorVersion) {
        this.pfMajorVersion = pfMajorVersion;
    }

    public ObjectId getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(ObjectId configurationId) {
        this.configurationId = configurationId;
    }

    public int getCfMajorVersion() {
        return cfMajorVersion;
    }

    public void setCfMajorVersion(int cfMajorVersion) {
        this.cfMajorVersion = cfMajorVersion;
    }

    public ObjectId getTopicId() {
        return topicId;
    }

    public void setTopicId(ObjectId topicId) {
        this.topicId = topicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Change)) {
            return false;
        }

        Change change = (Change) o;

        if (cfMajorVersion != change.cfMajorVersion) {
            return false;
        }
        if (pfMajorVersion != change.pfMajorVersion) {
            return false;
        }
        if (configurationId != null ? !configurationId.equals(change.configurationId) : change.configurationId != null) {
            return false;
        }
        if (endpointGroupId != null ? !endpointGroupId.equals(change.endpointGroupId) : change.endpointGroupId != null) {
            return false;
        }
        if (profileFilterId != null ? !profileFilterId.equals(change.profileFilterId) : change.profileFilterId != null) {
            return false;
        }
        if (topicId != null ? !topicId.equals(change.topicId) : change.topicId != null) {
            return false;
        }
        if (type != change.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (endpointGroupId != null ? endpointGroupId.hashCode() : 0);
        result = 31 * result + (profileFilterId != null ? profileFilterId.hashCode() : 0);
        result = 31 * result + pfMajorVersion;
        result = 31 * result + (configurationId != null ? configurationId.hashCode() : 0);
        result = 31 * result + cfMajorVersion;
        result = 31 * result + (topicId != null ? topicId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Change{" +
                "type=" + type +
                ", endpointGroupId=" + endpointGroupId +
                ", profileFilterId=" + profileFilterId +
                ", pfMajorVersion=" + pfMajorVersion +
                ", configurationId=" + configurationId +
                ", cfMajorVersion=" + cfMajorVersion +
                ", topicId=" + topicId +
                '}';
    }

    @Override
    public ChangeDto toDto() {
        ChangeDto changeDto = new ChangeDto();
        changeDto.setType(type);
        changeDto.setConfigurationId(idToString(configurationId));
        changeDto.setCfMajorVersion(cfMajorVersion);
        changeDto.setEndpointGroupId(idToString(endpointGroupId));
        changeDto.setProfileFilterId(idToString(profileFilterId));
        changeDto.setPfMajorVersion(pfMajorVersion);
        changeDto.setTopicId(idToString(topicId));
        return changeDto;
    }
}
