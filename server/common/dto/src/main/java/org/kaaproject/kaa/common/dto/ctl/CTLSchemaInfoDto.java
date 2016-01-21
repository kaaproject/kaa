/*
 * Copyright 2015 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.HasId;

import java.io.Serializable;
import java.util.Set;

/**
 * An object of this class represents
 * {@link org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto} for REST API calls.
 *
 * @author Igor Khanenko
 *
 * @since v0.8.0
 */
public class CTLSchemaInfoDto extends AbstractCTLSchemaDto implements HasId, Serializable {

    private static final long serialVersionUID = 3509310026130071994L;
    
    private String fqn;
    private Integer version;
    private CTLSchemaScopeDto scope;
    private String metaInfoId;
    private Set<CTLSchemaMetaInfoDto> dependencies;

    public Set<CTLSchemaMetaInfoDto> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<CTLSchemaMetaInfoDto> dependencies) {
        this.dependencies = dependencies;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CTLSchemaScopeDto getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScopeDto scope) {
        this.scope = scope;
    }
    
    public String getMetaInfoId() {
        return metaInfoId;
    }

    public void setMetaInfoId(String metaInfoId) {
        this.metaInfoId = metaInfoId;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((dependencies == null) ? 0 : dependencies.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result
                + ((metaInfoId == null) ? 0 : metaInfoId.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        CTLSchemaInfoDto other = (CTLSchemaInfoDto) obj;
        if (dependencies == null) {
            if (other.dependencies != null) {
                return false;
            }
        } else if (!dependencies.equals(other.dependencies)) {
            return false;
        }
        if (fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!fqn.equals(other.fqn)) {
            return false;
        }
        if (metaInfoId == null) {
            if (other.metaInfoId != null) {
                return false;
            }
        } else if (!metaInfoId.equals(other.metaInfoId)) {
            return false;
        }
        if (scope != other.scope) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTLSchemaInfoDto [fqn=");
        builder.append(fqn);
        builder.append(", version=");
        builder.append(version);
        builder.append(", scope=");
        builder.append(scope);
        builder.append(", metaInfoId=");
        builder.append(metaInfoId);
        builder.append(", dependencies=");
        builder.append(dependencies);
        builder.append("]");
        return builder.toString();
    }
 
}
