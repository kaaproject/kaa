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

public class CTLSchemaDto implements HasId, Serializable {

    private String id;
    private CTLSchemaMetaInfoDto metaInfo;
    private String tenantId;
    private String appId;
    private CTLSchemaScopeDto scope;
    private String body;
    private String name;
    private String description;
    private String createdUsername;
    private long createdTime;
    private Set<CTLSchemaDto> dependencySet;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public CTLSchemaMetaInfoDto getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CTLSchemaMetaInfoDto metaInfo) {
        this.metaInfo = metaInfo;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public CTLSchemaScopeDto getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScopeDto scope) {
        this.scope = scope;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public Set<CTLSchemaDto> getDependencySet() {
        return dependencySet;
    }

    public void setDependencySet(Set<CTLSchemaDto> dependencySet) {
        this.dependencySet = dependencySet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CTLSchemaDto that = (CTLSchemaDto) o;

        if (createdTime != that.createdTime) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (metaInfo != null ? !metaInfo.equals(that.metaInfo) : that.metaInfo != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (scope != that.scope) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (createdUsername != null ? !createdUsername.equals(that.createdUsername) : that.createdUsername != null)
            return false;
        return !(dependencySet != null ? !dependencySet.equals(that.dependencySet) : that.dependencySet != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (metaInfo != null ? metaInfo.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createdUsername != null ? createdUsername.hashCode() : 0);
        result = 31 * result + (int) (createdTime ^ (createdTime >>> 32));
        result = 31 * result + (dependencySet != null ? dependencySet.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CTLSchemaDto{" +
                "id='" + id + '\'' +
                ", metaInfo=" + metaInfo +
                ", tenantId='" + tenantId + '\'' +
                ", appId='" + appId + '\'' +
                ", scope=" + scope +
                ", body='" + body + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdUsername='" + createdUsername + '\'' +
                ", createdTime=" + createdTime +
                ", dependencySet=" + dependencySet +
                '}';
    }
}
