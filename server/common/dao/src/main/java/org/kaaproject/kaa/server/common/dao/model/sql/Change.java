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

package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_CONFIGURATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_CONFIGURATION_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_ENDPOINT_GROUP_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_PROFILE_FILTER_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_TOPIC_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CHANGE_TYPE;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;

@Entity
@Table(name = CHANGE_TABLE_NAME)
public class Change extends GenericModel<ChangeDto> implements Serializable {

    private static final long serialVersionUID = 8527934746035638165L;

    @Column(name = CHANGE_CONFIGURATION_VERSION)
    private int configurationVersion;

    @Column(name = CHANGE_TYPE)
    @Enumerated(EnumType.STRING)
    private ChangeType type;

    @Column(name = CHANGE_ENDPOINT_GROUP_ID)
    private Long groupId;

    @Column(name = CHANGE_TOPIC_ID)
    private Long topicId;

    @Column(name = CHANGE_CONFIGURATION_ID)
    private Long configurationId;

    @Column(name = CHANGE_PROFILE_FILTER_ID)
    private Long profileFilterId;

    public Change() {
    }

    public Change(Long id) {
        this.id = id;
    }

    public Change(ChangeDto dto) {
        if (dto != null) {
            this.id = getLongId(dto.getId());
            this.type = dto.getType();
            this.configurationVersion = dto.getCfVersion();
            this.groupId = getLongId(dto.getEndpointGroupId());
            this.profileFilterId = getLongId(dto.getProfileFilterId());
            this.configurationId = getLongId(dto.getConfigurationId());
            this.topicId = getLongId(dto.getTopicId());
        }
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public void setConfigurationVersion(int configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Long configurationId) {
        this.configurationId = configurationId;
    }

    public Long getProfileFilterId() {
        return profileFilterId;
    }

    public void setProfileFilterId(Long profileFilterId) {
        this.profileFilterId = profileFilterId;
    }

    @Override
    public int hashCode() {
        final int prime = 89;
        int result = 3;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + configurationVersion;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((topicId == null) ? 0 : topicId.hashCode());
        result = prime * result + ((configurationId == null) ? 0 : configurationId.hashCode());
        result = prime * result + ((profileFilterId == null) ? 0 : profileFilterId.hashCode());
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
        Change other = (Change) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (this.configurationVersion != other.configurationVersion) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!groupId.equals(other.groupId)) {
            return false;
        }
        if (topicId == null) {
            if (other.topicId != null) {
                return false;
            }
        } else if (!topicId.equals(other.topicId)) {
            return false;
        }
        if (configurationId == null) {
            if (other.configurationId != null) {
                return false;
            }
        } else if (!configurationId.equals(other.configurationId)) {
            return false;
        }
        if (profileFilterId == null) {
            if (other.profileFilterId != null) {
                return false;
            }
        } else if (!profileFilterId.equals(other.profileFilterId)) {
            return false;
        }
        return true;
    }

    @Override
    protected ChangeDto createDto() {
        return new ChangeDto();
    }

    @Override
    protected GenericModel<ChangeDto> newInstance(Long id) {
        return new Change(id);
    }

    @Override
    public ChangeDto toDto() {
        ChangeDto changeDto = createDto();
        changeDto.setId(getStringId());
        changeDto.setType(type);
        changeDto.setConfigurationId(ModelUtils.getStringId(configurationId));
        changeDto.setCfVersion(configurationVersion);
        changeDto.setEndpointGroupId(ModelUtils.getStringId(groupId));
        changeDto.setProfileFilterId(ModelUtils.getStringId(profileFilterId));
        changeDto.setTopicId(ModelUtils.getStringId(topicId));
        return changeDto;
    }

    @Override
    public String toString() {
        return "Change [configurationVersion=" + configurationVersion + ", type=" + type + ", groupId=" + groupId
                + ", topicId=" + topicId + ", configurationId=" + configurationId + ", profileFilterId=" + profileFilterId + "]";
    }

}
