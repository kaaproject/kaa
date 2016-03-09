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

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@JsonInclude(Include.NON_NULL)
public class EndpointCredentialsDto implements HasId, Serializable {

    private static final long serialVersionUID = 1000L;

    @JsonIgnore
    private String id;

    private String applicationId;

    @JsonProperty("publicKey")
    private byte[] endpointKey;

    @JsonProperty("endpointId")
    private byte[] endpointKeyHash;

    private Integer serverProfileVersion;
    private String serverProfileBody;

    public EndpointCredentialsDto() {
    }

    public EndpointCredentialsDto(String applicationId, byte[] endpointKey, byte[] endpointKeyHash) {
        this(applicationId, endpointKey, endpointKeyHash, null, null);
    }

    public EndpointCredentialsDto(String applicationId, byte[] endpointKey, byte[] endpointKeyHash, Integer serverProfileVersion, String serverProfileBody) {
        this.applicationId = applicationId;
        this.endpointKey = (endpointKey != null ? Arrays.copyOf(endpointKey, endpointKey.length) : null);
        this.endpointKeyHash = (endpointKeyHash != null ? Arrays.copyOf(endpointKeyHash, endpointKeyHash.length) : null);
        this.serverProfileVersion = serverProfileVersion;
        this.serverProfileBody = serverProfileBody;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public byte[] getEndpointKey() {
        return (this.endpointKey != null ? Arrays.copyOf(this.endpointKey, this.endpointKey.length) : null);
    }

    public void setEndpointKey(byte[] endpointKey) {
        this.endpointKey = (endpointKey != null ? Arrays.copyOf(endpointKey, endpointKey.length) : null);
    }

    public byte[] getEndpointKeyHash() {
        return (this.endpointKeyHash != null ? Arrays.copyOf(this.endpointKeyHash, this.endpointKeyHash.length) : null);
    }

    public void setEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointKeyHash = (endpointKeyHash != null ? Arrays.copyOf(endpointKeyHash, endpointKeyHash.length) : null);
    }

    public Integer getServerProfileVersion() {
        return this.serverProfileVersion;
    }

    public void setServerProfileVersion(Integer serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    public String getServerProfileBody() {
        return this.serverProfileBody;
    }

    public void setServerProfileBody(String serverProfileBody) {
        this.serverProfileBody = serverProfileBody;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.applicationId == null) ? 0 : this.applicationId.hashCode());
        result = prime * result + Arrays.hashCode(this.endpointKey);
        result = prime * result + Arrays.hashCode(this.endpointKeyHash);
        result = prime * result + ((this.serverProfileBody == null) ? 0 : this.serverProfileBody.hashCode());
        result = prime * result + ((this.serverProfileVersion == null) ? 0 : this.serverProfileVersion.hashCode());
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
        EndpointCredentialsDto other = (EndpointCredentialsDto) obj;
        if (this.applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!this.applicationId.equals(other.applicationId)) {
            return false;
        }
        if (!Arrays.equals(this.endpointKey, other.endpointKey)) {
            return false;
        }
        if (!Arrays.equals(this.endpointKeyHash, other.endpointKeyHash)) {
            return false;
        }
        if (this.serverProfileBody == null) {
            if (other.serverProfileBody != null) {
                return false;
            }
        } else if (!this.serverProfileBody.equals(other.serverProfileBody)) {
            return false;
        }
        if (this.serverProfileVersion == null) {
            if (other.serverProfileVersion != null) {
                return false;
            }
        } else if (!this.serverProfileVersion.equals(other.serverProfileVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointCredentialsDto [id=");
        builder.append(this.id);
        builder.append(", applicationId=");
        builder.append(this.applicationId);
        builder.append(", endpointKey=");
        builder.append(Arrays.toString(this.endpointKey));
        builder.append(", endpointKeyHash=");
        builder.append(Arrays.toString(this.endpointKeyHash));
        builder.append(", serverProfileVersion=");
        builder.append(this.serverProfileVersion);
        builder.append(", serverProfileBody=");
        builder.append(this.serverProfileBody);
        builder.append("]");
        return builder.toString();
    }
}
