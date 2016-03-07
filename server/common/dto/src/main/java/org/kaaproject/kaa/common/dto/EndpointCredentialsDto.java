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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
    private String endpointId;
    private String publicKey;
    private Integer serverProfileVersion;
    private String serverProfileBody;

    @JsonIgnore
    private Boolean serverProfileExpired = false;

    public EndpointCredentialsDto() {
    }

    public EndpointCredentialsDto(String applicationId, String endpointId, String publicKey) {
        this(applicationId, endpointId, publicKey, null, null);
    }

    public EndpointCredentialsDto(String applicationId, String endpointId, String publicKey, Integer serverProfileVersion, String serverProfileBody) {
        this.applicationId = applicationId;
        this.endpointId = endpointId;
        this.publicKey = publicKey;
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

    public String getEndpointId() {
        return this.endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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

    public Boolean getServerProfileExpired() {
        return this.serverProfileExpired;
    }

    public void setServerProfileExpired(Boolean serverProfileExpired) {
        this.serverProfileExpired = serverProfileExpired;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.applicationId == null) ? 0 : this.applicationId.hashCode());
        result = prime * result + ((this.endpointId == null) ? 0 : this.endpointId.hashCode());
        result = prime * result + ((this.publicKey == null) ? 0 : this.publicKey.hashCode());
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
        if (this.endpointId == null) {
            if (other.endpointId != null) {
                return false;
            }
        } else if (!this.endpointId.equals(other.endpointId)) {
            return false;
        }
        if (this.publicKey == null) {
            if (other.publicKey != null) {
                return false;
            }
        } else if (!this.publicKey.equals(other.publicKey)) {
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
        builder.append(", endpointId=");
        builder.append(this.endpointId);
        builder.append(", publicKey=");
        builder.append(this.publicKey);
        builder.append(", serverProfileVersion=");
        builder.append(this.serverProfileVersion);
        builder.append(", serverProfileBody=");
        builder.append(this.serverProfileBody);
        builder.append(", serverProfileExpired=");
        builder.append(this.serverProfileExpired);
        builder.append("]");
        return builder.toString();
    }
}
