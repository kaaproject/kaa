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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_GROUP_STATE_CONFIGURATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_GROUP_STATE_ENDPOINT_GROUP_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_GROUP_STATE_PROFILE_FILTER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_GROUP_STATE_USER_TYPE_NAME;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;

@UDT(name = ENDPOINT_GROUP_STATE_USER_TYPE_NAME)
public final class CassandraEndpointGroupState implements ToDto<EndpointGroupStateDto>, Serializable {

    @Transient
    private static final long serialVersionUID = -1658174097110691624L;

    @Field(name = ENDPOINT_GROUP_STATE_ENDPOINT_GROUP_ID_PROPERTY)
    private String endpointGroupId;
    @Field(name = ENDPOINT_GROUP_STATE_PROFILE_FILTER_ID_PROPERTY)
    private String profileFilterId;
    @Field(name = ENDPOINT_GROUP_STATE_CONFIGURATION_ID_PROPERTY)
    private String configurationId;

    public CassandraEndpointGroupState() {
    }

    public CassandraEndpointGroupState(EndpointGroupStateDto dto) {
        this.endpointGroupId = dto.getEndpointGroupId();
        this.profileFilterId = dto.getProfileFilterId();
        this.configurationId = dto.getConfigurationId();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CassandraEndpointGroupState that = (CassandraEndpointGroupState) o;

        if (configurationId != null ? !configurationId.equals(that.configurationId) : that.configurationId != null) {
            return false;
        }
        if (endpointGroupId != null ? !endpointGroupId.equals(that.endpointGroupId) : that.endpointGroupId != null) {
            return false;
        }
        if (profileFilterId != null ? !profileFilterId.equals(that.profileFilterId) : that.profileFilterId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointGroupId != null ? endpointGroupId.hashCode() : 0;
        result = 31 * result + (profileFilterId != null ? profileFilterId.hashCode() : 0);
        result = 31 * result + (configurationId != null ? configurationId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEndpointGroupState{" +
                "endpointGroupId='" + endpointGroupId + '\'' +
                ", profileFilterId='" + profileFilterId + '\'' +
                ", configurationId='" + configurationId + '\'' +
                '}';
    }

    @Override
    public EndpointGroupStateDto toDto() {
        EndpointGroupStateDto groupStateDto = new EndpointGroupStateDto();
        groupStateDto.setEndpointGroupId(endpointGroupId);
        groupStateDto.setProfileFilterId(profileFilterId);
        groupStateDto.setConfigurationId(configurationId);
        return groupStateDto;
    }
}
