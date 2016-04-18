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

public class ProfileFilterDto extends AbstractStructureDto {

    private static final long serialVersionUID = 3068910692262107362L;

    private String endpointProfileSchemaId;
    private Integer endpointProfileSchemaVersion;

    private String serverProfileSchemaId;
    private Integer serverProfileSchemaVersion;

    public String getEndpointProfileSchemaId() {
        return endpointProfileSchemaId;
    }

    public void setEndpointProfileSchemaId(String endpointProfileSchemaId) {
        this.endpointProfileSchemaId = endpointProfileSchemaId;
    }

    public Integer getEndpointProfileSchemaVersion() {
        return endpointProfileSchemaVersion;
    }

    public void setEndpointProfileSchemaVersion(Integer endpointProfileSchemaVersion) {
        this.endpointProfileSchemaVersion = endpointProfileSchemaVersion;
    }

    public String getServerProfileSchemaId() {
        return serverProfileSchemaId;
    }

    public void setServerProfileSchemaId(String serverProfileSchemaId) {
        this.serverProfileSchemaId = serverProfileSchemaId;
    }

    public Integer getServerProfileSchemaVersion() {
        return serverProfileSchemaVersion;
    }

    public void setServerProfileSchemaVersion(Integer serverProfileSchemaVersion) {
        this.serverProfileSchemaVersion = serverProfileSchemaVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endpointProfileSchemaId == null) ? 0 : endpointProfileSchemaId.hashCode());
        result = prime * result + ((endpointProfileSchemaVersion == null) ? 0 : endpointProfileSchemaVersion.hashCode());
        result = prime * result + ((serverProfileSchemaId == null) ? 0 : serverProfileSchemaId.hashCode());
        result = prime * result + ((serverProfileSchemaVersion == null) ? 0 : serverProfileSchemaVersion.hashCode());
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
        ProfileFilterDto other = (ProfileFilterDto) obj;
        if (endpointProfileSchemaId == null) {
            if (other.endpointProfileSchemaId != null) {
                return false;
            }
        } else if (!endpointProfileSchemaId.equals(other.endpointProfileSchemaId)) {
            return false;
        }
        if (endpointProfileSchemaVersion == null) {
            if (other.endpointProfileSchemaVersion != null) {
                return false;
            }
        } else if (!endpointProfileSchemaVersion.equals(other.endpointProfileSchemaVersion)) {
            return false;
        }
        if (serverProfileSchemaId == null) {
            if (other.serverProfileSchemaId != null) {
                return false;
            }
        } else if (!serverProfileSchemaId.equals(other.serverProfileSchemaId)) {
            return false;
        }
        if (serverProfileSchemaVersion == null) {
            if (other.serverProfileSchemaVersion != null) {
                return false;
            }
        } else if (!serverProfileSchemaVersion.equals(other.serverProfileSchemaVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProfileFilterDto{" + super.toString() + "}";
    }

}
