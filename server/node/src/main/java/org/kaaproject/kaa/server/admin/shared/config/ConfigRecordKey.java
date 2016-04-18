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

package org.kaaproject.kaa.server.admin.shared.config;

import java.io.Serializable;

public class ConfigRecordKey implements Serializable {

    private static final long serialVersionUID = 3302320660468092090L;

    private String schemaId;
    private String endpointGroupId;

    public ConfigRecordKey() {
    }

    public ConfigRecordKey(String schemaId, String endpointGroupId) {
        this.schemaId = schemaId;
        this.endpointGroupId = endpointGroupId;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
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
        result = prime * result
                + ((schemaId == null) ? 0 : schemaId.hashCode());
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
        ConfigRecordKey other = (ConfigRecordKey) obj;
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null) {
                return false;
            }
        } else if (!endpointGroupId.equals(other.endpointGroupId)) {
            return false;
        }
        if (schemaId == null) {
            if (other.schemaId != null) {
                return false;
            }
        } else if (!schemaId.equals(other.schemaId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConfigRecordKey [schemaId=");
        builder.append(schemaId);
        builder.append(", endpointGroupId=");
        builder.append(endpointGroupId);
        builder.append("]");
        return builder.toString();
    }

}
