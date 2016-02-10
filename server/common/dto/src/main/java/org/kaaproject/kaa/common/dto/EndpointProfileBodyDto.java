/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Arrays;

public class EndpointProfileBodyDto implements Serializable {

    private static final long serialVersionUID = 1459044085470010138L;

    private byte[] endpointKeyHash;
    private String profile;
    @JsonIgnore
    private String appId;

    public EndpointProfileBodyDto() {}

    public EndpointProfileBodyDto(byte[] endpointKeyHash, String profile, String appId) {
       this.endpointKeyHash = endpointKeyHash;
       this.profile = profile;
       this.appId = appId;
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getAppId() {
        return appId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(endpointKeyHash);
        result = prime * result + ((profile == null) ? 0 : profile.hashCode());
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
        EndpointProfileBodyDto other = (EndpointProfileBodyDto) obj;
        if (!Arrays.equals(endpointKeyHash, other.endpointKeyHash))
            return false;
        if (profile == null) {
            if (other.profile != null)
                return false;
        } else if (!profile.equals(other.profile))
            return false;
        return true;
    }
}
