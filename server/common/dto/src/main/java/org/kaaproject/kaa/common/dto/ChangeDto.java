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

import java.io.Serializable;

public class ChangeDto implements Serializable, HasId {

    private static final long serialVersionUID = 8830413612147261296L;

    private String id;
    private ChangeType type;
    private String endpointGroupId;
    private String profileFilterId;
    private String configurationId;
    private int cfVersion;
    private String topicId;

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    public void setEndpointGroupId(String endpointGroupId) {
        this.endpointGroupId = endpointGroupId;
    }

    public String getProfileFilterId() {
        return profileFilterId;
    }

    public void setProfileFilterId(String profileFilterId) {
        this.profileFilterId = profileFilterId;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public int getCfVersion() {
        return cfVersion;
    }

    public void setCfVersion(int cfVersion) {
        this.cfVersion = cfVersion;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChangeDto)) {
            return false;
        }

        ChangeDto changeDto = (ChangeDto) o;

        if (cfVersion != changeDto.cfVersion) {
            return false;
        }
        if (configurationId != null ? !configurationId.equals(changeDto.configurationId) : changeDto.configurationId != null) {
            return false;
        }
        if (endpointGroupId != null ? !endpointGroupId.equals(changeDto.endpointGroupId) : changeDto.endpointGroupId != null) {
            return false;
        }
        if (profileFilterId != null ? !profileFilterId.equals(changeDto.profileFilterId) : changeDto.profileFilterId != null) {
            return false;
        }
        if (topicId != null ? !topicId.equals(changeDto.topicId) : changeDto.topicId != null) {
            return false;
        }
        if (type != changeDto.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (endpointGroupId != null ? endpointGroupId.hashCode() : 0);
        result = 31 * result + (profileFilterId != null ? profileFilterId.hashCode() : 0);
        result = 31 * result + (configurationId != null ? configurationId.hashCode() : 0);
        result = 31 * result + cfVersion;
        result = 31 * result + (topicId != null ? topicId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChangeDto{" +
                "id=" + id +
                "type=" + type +
                ", endpointGroupId='" + endpointGroupId + '\'' +
                ", profileFilterId='" + profileFilterId + '\'' +
                ", configurationId='" + configurationId + '\'' +
                ", cfVersion=" + cfVersion +
                ", topicId='" + topicId + '\'' +
                '}';
    }
}
