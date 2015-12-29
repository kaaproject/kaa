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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class CTLSchemaMetaInfoDto implements HasId, Serializable {

    private static final long serialVersionUID = 365372783241958657L;
    
    private String id;
    private String name;
    private String fqn;
    private Integer version;
    private CTLSchemaScopeDto scope;
    @JsonIgnore
    private Long count = 0L;

    public CTLSchemaMetaInfoDto() {
    }

    public CTLSchemaMetaInfoDto(String fqn, Integer version) {
        this.fqn = fqn;
        this.version = version;
    }

    public CTLSchemaMetaInfoDto(String fqn, Integer version, CTLSchemaScopeDto scope) {
        this.fqn = fqn;
        this.version = version;
        this.scope = scope;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CTLSchemaMetaInfoDto that = (CTLSchemaMetaInfoDto) o;

        if (fqn != null ? !fqn.equals(that.fqn) : that.fqn != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        return scope == that.scope;

    }

    @Override
    public int hashCode() {
        int result = fqn != null ? fqn.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CTLSchemaMetaInfoDto{" +
                "id='" + id + '\'' +
                ", fqn='" + fqn + '\'' +
                ", version=" + version +
                ", scope=" + scope +
                '}';
    }
}
