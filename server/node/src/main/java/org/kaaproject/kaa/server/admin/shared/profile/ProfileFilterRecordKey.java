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

package org.kaaproject.kaa.server.admin.shared.profile;

import java.io.Serializable;

public class ProfileFilterRecordKey implements Serializable {

    private static final long serialVersionUID = 5092336381178145561L;
    
    private String endpointProfileSchemaId;
    private String serverProfileSchemaId;
    private String endpointGroupId;

    public ProfileFilterRecordKey() {
    }

    public ProfileFilterRecordKey(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) {
        this.endpointProfileSchemaId = endpointProfileSchemaId;
        this.serverProfileSchemaId = serverProfileSchemaId;
        this.endpointGroupId = endpointGroupId;
    }

    public String getEndpointProfileSchemaId() {
        return endpointProfileSchemaId;
    }

    public void setEndpointProfileSchemaId(String endpointProfileSchemaId) {
        this.endpointProfileSchemaId = endpointProfileSchemaId;
    }

    public String getServerProfileSchemaId() {
        return serverProfileSchemaId;
    }

    public void setServerProfileSchemaId(String serverProfileSchemaId) {
        this.serverProfileSchemaId = serverProfileSchemaId;
    }

    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    public void setEndpointGroupId(String endpointGroupId) {
        this.endpointGroupId = endpointGroupId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((endpointGroupId == null) ? 0 : endpointGroupId.hashCode());
        result = prime
                * result
                + ((endpointProfileSchemaId == null) ? 0
                        : endpointProfileSchemaId.hashCode());
        result = prime
                * result
                + ((serverProfileSchemaId == null) ? 0 : serverProfileSchemaId
                        .hashCode());
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
        ProfileFilterRecordKey other = (ProfileFilterRecordKey) obj;
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null) {
                return false;
            }
        } else if (!endpointGroupId.equals(other.endpointGroupId)) {
            return false;
        }
        if (endpointProfileSchemaId == null) {
            if (other.endpointProfileSchemaId != null) {
                return false;
            }
        } else if (!endpointProfileSchemaId
                .equals(other.endpointProfileSchemaId)) {
            return false;
        }
        if (serverProfileSchemaId == null) {
            if (other.serverProfileSchemaId != null) {
                return false;
            }
        } else if (!serverProfileSchemaId.equals(other.serverProfileSchemaId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProfileFilterRecordKey [endpointProfileSchemaId=");
        builder.append(endpointProfileSchemaId);
        builder.append(", serverProfileSchemaId=");
        builder.append(serverProfileSchemaId);
        builder.append(", endpointGroupId=");
        builder.append(endpointGroupId);
        builder.append("]");
        return builder.toString();
    }

}
