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

public class ProfileVersionPairDto implements Serializable, Comparable<ProfileVersionPairDto> {

    private static final long serialVersionUID = -7073061086587077053L;

    private String endpointProfileSchemaid;
    private Integer endpointProfileSchemaVersion;
    private String serverProfileSchemaid;
    private Integer serverProfileSchemaVersion;

    public ProfileVersionPairDto() {
    }

    public ProfileVersionPairDto(Integer endpointProfileSchemaVersion, String endpointProfileSchemaid) {
        this.endpointProfileSchemaid = endpointProfileSchemaid;
        this.endpointProfileSchemaVersion = endpointProfileSchemaVersion;
    }

    public ProfileVersionPairDto(String serverProfileSchemaid, Integer serverProfileSchemaVersion) {
        this.serverProfileSchemaid = serverProfileSchemaid;
        this.serverProfileSchemaVersion = serverProfileSchemaVersion;
    }

    public ProfileVersionPairDto(String endpointProfileSchemaid, Integer endpointProfileSchemaVersion, String serverProfileSchemaid,
            Integer serverProfileSchemaVersion) {
        this.endpointProfileSchemaid = endpointProfileSchemaid;
        this.endpointProfileSchemaVersion = endpointProfileSchemaVersion;
        this.serverProfileSchemaid = serverProfileSchemaid;
        this.serverProfileSchemaVersion = serverProfileSchemaVersion;
    }

    public String getEndpointProfileSchemaid() {
        return endpointProfileSchemaid;
    }

    public void setEndpointProfileSchemaid(String endpointProfileSchemaid) {
        this.endpointProfileSchemaid = endpointProfileSchemaid;
    }

    public Integer getEndpointProfileSchemaVersion() {
        return endpointProfileSchemaVersion;
    }

    public void setEndpointProfileSchemaVersion(Integer endpointProfileSchemaVersion) {
        this.endpointProfileSchemaVersion = endpointProfileSchemaVersion;
    }

    public String getServerProfileSchemaid() {
        return serverProfileSchemaid;
    }

    public void setServerProfileSchemaid(String serverProfileSchemaid) {
        this.serverProfileSchemaid = serverProfileSchemaid;
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
        int result = 1;
        result = prime * result + ((endpointProfileSchemaVersion == null) ? 0 : endpointProfileSchemaVersion.hashCode());
        result = prime * result + ((endpointProfileSchemaid == null) ? 0 : endpointProfileSchemaid.hashCode());
        result = prime * result + ((serverProfileSchemaVersion == null) ? 0 : serverProfileSchemaVersion.hashCode());
        result = prime * result + ((serverProfileSchemaid == null) ? 0 : serverProfileSchemaid.hashCode());
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
        ProfileVersionPairDto other = (ProfileVersionPairDto) obj;
        if (endpointProfileSchemaVersion == null) {
            if (other.endpointProfileSchemaVersion != null) {
                return false;
            }
        } else if (!endpointProfileSchemaVersion.equals(other.endpointProfileSchemaVersion)) {
            return false;
        }
        if (endpointProfileSchemaid == null) {
            if (other.endpointProfileSchemaid != null) {
                return false;
            }
        } else if (!endpointProfileSchemaid.equals(other.endpointProfileSchemaid)) {
            return false;
        }
        if (serverProfileSchemaVersion == null) {
            if (other.serverProfileSchemaVersion != null) {
                return false;
            }
        } else if (!serverProfileSchemaVersion.equals(other.serverProfileSchemaVersion)) {
            return false;
        }
        if (serverProfileSchemaid == null) {
            if (other.serverProfileSchemaid != null) {
                return false;
            }
        } else if (!serverProfileSchemaid.equals(other.serverProfileSchemaid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProfileVersionPairDto [endpointProfileSchemaid=" + endpointProfileSchemaid + ", endpointProfileSchemaVersion="
                + endpointProfileSchemaVersion + ", serverProfileSchemaid=" + serverProfileSchemaid + ", serverProfileSchemaVersion="
                + serverProfileSchemaVersion + "]";
    }

    @Override
    public int compareTo(ProfileVersionPairDto o) {
        if (this.getEndpointProfileSchemaid() != null && o.getEndpointProfileSchemaid() == null) {
            return 1;
        } else if (this.getEndpointProfileSchemaid() == null && o.getEndpointProfileSchemaid() != null) {
            return -1;
        }
        if (this.getEndpointProfileSchemaid() != null && o.getEndpointProfileSchemaid() != null) {
            if (this.getEndpointProfileSchemaVersion() != o.getEndpointProfileSchemaVersion()) {
                return this.getEndpointProfileSchemaVersion() - o.getEndpointProfileSchemaVersion();
            }
        }
        if (this.getServerProfileSchemaid() != null && o.getServerProfileSchemaid() == null) {
            return 1;
        } else if (this.getServerProfileSchemaid() == null && o.getServerProfileSchemaid() != null) {
            return -1;
        } else if (this.getServerProfileSchemaid() != null && o.getServerProfileSchemaid() != null) {
            return this.getServerProfileSchemaVersion() - o.getServerProfileSchemaVersion();
        } else {
            return 0;
        }
    }

}
