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

public class EndpointGroupStateDto implements Serializable {

    private static final long serialVersionUID = -1658174097110691624L;

    private String endpointGroupId;
    private String profileFilterId;
    private String configurationId;

    public EndpointGroupStateDto() {
        super();
    }

    public EndpointGroupStateDto(String endpointGroupId, String profileFilterId, String configurationId) {
        super();
        this.endpointGroupId = endpointGroupId;
        this.profileFilterId = profileFilterId;
        this.configurationId = configurationId;
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

        EndpointGroupStateDto that = (EndpointGroupStateDto) o;

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
        return "EndpointGroupStateDto{" +
                "endpointGroupId='" + endpointGroupId + '\'' +
                ", profileFilterId='" + profileFilterId + '\'' +
                ", configurationId='" + configurationId + '\'' +
                '}';
    }
}
