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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;
import java.util.List;

public class WrapperEndpointProfileDto implements Serializable {

    private static final long serialVersionUID = -7461716477116042286L;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public List<EndpointProfileDto> getEndpointProfiles() {
        return endpointProfiles;
    }

    public void setEndpointProfiles(List<EndpointProfileDto> endpointProfiles) {
        this.endpointProfiles = endpointProfiles;
    }

    private String next;
    private List<EndpointProfileDto> endpointProfiles;
    
    public WrapperEndpointProfileDto() {}
    public WrapperEndpointProfileDto(List<EndpointProfileDto> endpointProfiles, String endpointGroupId, int limit, String offset) {
        if (endpointProfiles.size() == (limit + 1)) {
            next = "http://localhost:8080/kaaAdmin/rest/api/endpointProfileByGroupId?endpointGroupId="
            + endpointGroupId + "&limit=" + limit + "&offset=" + (limit + Integer.valueOf(offset));
            this.endpointProfiles = endpointProfiles.subList(0, limit);
        } else {
            next = "It is the last page";
            this.endpointProfiles = endpointProfiles;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointProfiles == null) ? 0 : endpointProfiles.hashCode());
        result = prime * result + ((next == null) ? 0 : next.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WrapperEndpointProfileDto other = (WrapperEndpointProfileDto) obj;
        if (endpointProfiles == null) {
            if (other.endpointProfiles != null)
                return false;
        } else if (!endpointProfiles.equals(other.endpointProfiles))
            return false;
        if (next == null) {
            if (other.next != null)
                return false;
        } else if (!next.equals(other.next))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(next);
        builder.append(", endpointProfiles=");
        builder.append(endpointProfiles);
        return builder.toString();
    }
}
