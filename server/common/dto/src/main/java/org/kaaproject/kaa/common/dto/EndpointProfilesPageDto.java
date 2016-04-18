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

import java.util.List;

public class EndpointProfilesPageDto extends AbstractEndpointProfilesDto {

    private static final long serialVersionUID = 6368165337840879484L;

    private List<EndpointProfileDto> endpointProfiles;

    public EndpointProfilesPageDto() {
    }

    public EndpointProfilesPageDto(List<EndpointProfileDto> endpointProfiles) {
       this.endpointProfiles = endpointProfiles;
    }

    public List<EndpointProfileDto> getEndpointProfiles() {
        return endpointProfiles;
    }

    public void setEndpointProfiles(List<EndpointProfileDto> endpointProfiles) {
        this.endpointProfiles = endpointProfiles;
    }

    public boolean hasEndpointProfiles() {
        boolean result = false;
        if (endpointProfiles != null) {
            result = !endpointProfiles.isEmpty();
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endpointProfiles == null) ? 0 : endpointProfiles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EndpointProfilesPageDto other = (EndpointProfilesPageDto) obj;
        if (endpointProfiles == null) {
            if (other.endpointProfiles != null) {
                return false;
            }
        } else if (!endpointProfiles.equals(other.endpointProfiles)) {
            return false;
        }
        return true;
    }
}
