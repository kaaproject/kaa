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

package org.kaaproject.kaa.common.dto.ctl;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CTLSchemaMetaInfoDto implements HasId, Serializable {

    private static final long serialVersionUID = 365372783241958657L;
    
    private String id;
    private String fqn;
    private String tenantId;
    private String applicationId;
    private List<Integer> versions;

    public CTLSchemaMetaInfoDto() {
    }

    public CTLSchemaMetaInfoDto(String fqn) {
        this.fqn = fqn;
    }

    public CTLSchemaMetaInfoDto(String fqn, String tenantId) {
        this.fqn = fqn;
        this.tenantId = tenantId;
    }
    
    public CTLSchemaMetaInfoDto(String fqn, String tenantId, String applicationId) {
        this.fqn = fqn;
        this.tenantId = tenantId;
        this.applicationId = applicationId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }
    
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @JsonIgnore
    public CTLSchemaScopeDto getScope() {
        if (tenantId != null && !tenantId.isEmpty()) {
            if (applicationId != null && !applicationId.isEmpty()) {
                return CTLSchemaScopeDto.APPLICATION;
            } else {
                return CTLSchemaScopeDto.TENANT;
            }
        }
        return CTLSchemaScopeDto.SYSTEM;
    }

    public List<Integer> getVersions() {
        return versions;
    }

    public void setVersions(List<Integer> versions) {
        this.versions = versions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
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
        CTLSchemaMetaInfoDto other = (CTLSchemaMetaInfoDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        } else if (!applicationId.equals(other.applicationId))
            return false;
        if (fqn == null) {
            if (other.fqn != null)
                return false;
        } else if (!fqn.equals(other.fqn))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTLSchemaMetaInfoDto [id=");
        builder.append(id);
        builder.append(", fqn=");
        builder.append(fqn);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append("]");
        return builder.toString();
    }

}
