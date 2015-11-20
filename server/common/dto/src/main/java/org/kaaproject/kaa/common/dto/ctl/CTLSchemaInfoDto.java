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

    private String fqn;
    private Integer version;
    private CTLSchemaScopeDto scope;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CTLSchemaInfoDto infoDto = (CTLSchemaInfoDto) o;

        if (fqn != null ? !fqn.equals(infoDto.fqn) : infoDto.fqn != null)
            return false;
        if (version != null ? !version.equals(infoDto.version) : infoDto.version != null)
            return false;
        if (scope != infoDto.scope)
            return false;
        return dependencies != null ? dependencies.equals(infoDto.dependencies) : infoDto.dependencies == null;

    }

    @Override
    public int hashCode() {
        int result = fqn != null ? fqn.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CTLSchemaInfoDto{" +
                "fqn='" + fqn + '\'' +
                ", version=" + version +
                ", scope=" + scope +
                ", dependencies=" + dependencies +
                '}';
    }
}
