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

package org.kaaproject.kaa.common.dto.credentials;

import java.io.Serializable;
import java.util.Optional;

import org.kaaproject.kaa.common.dto.HasId;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public final class EndpointRegistrationInfoDto implements HasId, Serializable {

    private static final long serialVersionUID = 1000L;

    private String id;
    private String applicationId;
    private String endpointId;
    private String credentialsId;
    private Integer serverProfileVersion;
    private String serverProfileBody;

    @Override
    public String getId() {
        return this.id;
    }

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

    public Optional<String> getCredentialsId() {
        return Optional.ofNullable(this.credentialsId);
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Optional<Integer> getServerProfileVersion() {
        return Optional.ofNullable(this.serverProfileVersion);
    }

    public void setServerProfileVersion(Integer serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    public Optional<String> getServerProfileBody() {
        return Optional.ofNullable(this.serverProfileBody);
    }

    public void setServerProfileBody(String serverProfileBody) {
        this.serverProfileBody = serverProfileBody;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.applicationId == null) ? 0 : this.applicationId.hashCode());
        result = prime * result + ((this.credentialsId == null) ? 0 : this.credentialsId.hashCode());
        result = prime * result + ((this.endpointId == null) ? 0 : this.endpointId.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
        EndpointRegistrationInfoDto other = (EndpointRegistrationInfoDto) obj;
        if (this.applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!this.applicationId.equals(other.applicationId)) {
            return false;
        }
        if (this.credentialsId == null) {
            if (other.credentialsId != null) {
                return false;
            }
        } else if (!this.credentialsId.equals(other.credentialsId)) {
            return false;
        }
        if (this.endpointId == null) {
            if (other.endpointId != null) {
                return false;
            }
        } else if (!this.endpointId.equals(other.endpointId)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
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
        builder.append("EndpointRegistrationInfoDto [id=");
        builder.append(this.id);
        builder.append(", applicationId=");
        builder.append(this.applicationId);
        builder.append(", endpointId=");
        builder.append(this.endpointId);
        builder.append(", credentialsId=");
        builder.append(this.credentialsId);
        builder.append(", serverProfileVersion=");
        builder.append(this.serverProfileVersion);
        builder.append(", serverProfileBody=");
        builder.append(this.serverProfileBody);
        builder.append("]");
        return builder.toString();
    }
}
