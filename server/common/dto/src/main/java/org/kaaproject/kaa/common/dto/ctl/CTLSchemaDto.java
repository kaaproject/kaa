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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Common Type Library schema.
 *
 * @since v0.8.0
 */
public class CTLSchemaDto extends AbstractCTLSchemaDto implements HasId, Serializable {

    private static final long serialVersionUID = -7601241323233814152L;

    private CTLSchemaMetaInfoDto metaInfo;
    private Set<CTLSchemaDto> dependencySet;

    public CTLSchemaDto() {
    }

    public CTLSchemaDto(CTLSchemaInfoDto infoDto, Set<CTLSchemaDto> dependencySet) {
        Objects.requireNonNull(infoDto);
        if (infoDto != null) {
            this.dependencySet = dependencySet;
            this.metaInfo = new CTLSchemaMetaInfoDto(infoDto.getFqn(), infoDto.getVersion(), infoDto.getScope());
            this.metaInfo.setId(infoDto.getMetaInfoId());
            id = infoDto.getId();
            applicationId = infoDto.getApplicationId();
            tenantId = infoDto.getTenantId();
            body = infoDto.getBody();
            createdTime = infoDto.getCreatedTime();
            createdUsername = infoDto.getCreatedUsername();
            name = infoDto.getName();
            description = infoDto.getDescription();
        }
    }

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

    public Set<CTLSchemaDto> getDependencySet() {
        return dependencySet;
    }

    public void setDependencySet(Set<CTLSchemaDto> dependencySet) {
        this.dependencySet = dependencySet;
    }

    public CTLSchemaInfoDto toCTLSchemaInfoDto() {
        CTLSchemaInfoDto infoDto = new CTLSchemaInfoDto();
        infoDto.setId(id);
        infoDto.setFqn(metaInfo.getFqn());
        infoDto.setVersion(metaInfo.getVersion());
        infoDto.setScope(metaInfo.getScope());
        infoDto.setMetaInfoId(metaInfo.getId());
        infoDto.setApplicationId(applicationId);
        infoDto.setTenantId(tenantId);
        infoDto.setBody(body);
        infoDto.setCreatedTime(createdTime);
        infoDto.setCreatedUsername(createdUsername);
        infoDto.setName(name);
        infoDto.setDescription(description);
        if (dependencySet != null && !dependencySet.isEmpty()) {
            Set<CTLSchemaMetaInfoDto> dependencies = new HashSet<>();
            for (CTLSchemaDto dep : dependencySet) {
                CTLSchemaMetaInfoDto mi = dep.getMetaInfo();
                dependencies.add(new CTLSchemaMetaInfoDto(mi.getFqn(), mi.getVersion()));
            }
            infoDto.setDependencies(dependencies);
        }
        return infoDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CTLSchemaDto that = (CTLSchemaDto) o;

        if (createdTime != that.createdTime)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (metaInfo != null ? !metaInfo.equals(that.metaInfo) : that.metaInfo != null)
            return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null)
            return false;
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        if (body != null ? !body.equals(that.body) : that.body != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (createdUsername != null ? !createdUsername.equals(that.createdUsername) : that.createdUsername != null)
            return false;
        return !(dependencySet != null ? !dependencySet.equals(that.dependencySet) : that.dependencySet != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (metaInfo != null ? metaInfo.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
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
                ", appId='" + applicationId + '\'' +
                ", body='" + body + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdUsername='" + createdUsername + '\'' +
                ", createdTime=" + createdTime +
                ", dependencySet=" + dependencySet +
                '}';
    }
}
