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

public class EndpointProfilesBodyDto extends AbstractEndpointProfilesDto {

    private static final long serialVersionUID = -3301431577852472525L;

    private List<EndpointProfileBodyDto> endpointProfilesBody;

    public EndpointProfilesBodyDto() {
    }

    public EndpointProfilesBodyDto(List<EndpointProfileBodyDto> endpointProfileBody) {
       this.endpointProfilesBody = endpointProfileBody;
    }

    public boolean hasEndpointBodies() {
        boolean result = false;
        if (endpointProfilesBody != null) {
            result = !endpointProfilesBody.isEmpty();
        }
        return result;
    }

    public List<EndpointProfileBodyDto> getEndpointProfilesBody() {
        return endpointProfilesBody;
    }

    public void setEndpointProfilesBody(List<EndpointProfileBodyDto> endpointProfileBody) {
        this.endpointProfilesBody = endpointProfileBody;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endpointProfilesBody == null) ? 0 : endpointProfilesBody.hashCode());
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
        EndpointProfilesBodyDto other = (EndpointProfilesBodyDto) obj;
        if (endpointProfilesBody == null) {
            if (other.endpointProfilesBody != null) {
                return false;
            }
        } else if (!endpointProfilesBody.equals(other.endpointProfilesBody)) {
            return false;
        }
        return true;
    }
}
