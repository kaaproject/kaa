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

package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_APPLICATION_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEFAULT_RECORD;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_CHILD_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_CHILD_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_PARENT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_PARENT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TENANT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_UNIQUE_CONSTRAINT;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;

@Entity
@Table(name = CTL_SCHEMA_TABLE_NAME, uniqueConstraints =
@UniqueConstraint(columnNames = {CTL_SCHEMA_META_INFO_ID, CTL_SCHEMA_TENANT_ID}, name = CTL_SCHEMA_UNIQUE_CONSTRAINT))
public class CTLSchema extends GenericModel<CTLSchemaDto> implements Serializable {

    private static final long serialVersionUID = -1179381742235545494L;
    
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = CTL_SCHEMA_META_INFO_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_META_INFO_FK))
    private CTLSchemaMetaInfo metaInfo;
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = CTL_SCHEMA_TENANT_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_TENANT_FK))
    private Tenant tenant;
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = CTL_SCHEMA_APPLICATION_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_APPLICATION_FK))
    private Application application;
    @Lob
    @Column(name = CTL_SCHEMA_BODY)
    private String body;
    @Lob
    @Column(name = CTL_SCHEMA_DEFAULT_RECORD)
    private String defaultRecord;
    @Column(name = CTL_SCHEMA_NAME)
    private String name;
    @Column(length = 1000, name = CTL_SCHEMA_DESCRIPTION)
    private String description;
    @Column(name = CTL_SCHEMA_CREATED_USERNAME)
    private String createdUsername;
    @Column(name = CTL_SCHEMA_CREATED_TIME)
    private long createdTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = CTL_SCHEMA_JOIN_TABLE_NAME,
            joinColumns = {@JoinColumn(name = CTL_SCHEMA_JOIN_TABLE_PARENT_ID)}, foreignKey = @ForeignKey(name = CTL_SCHEMA_JOIN_TABLE_PARENT_FK),
            inverseJoinColumns = {@JoinColumn(name = CTL_SCHEMA_JOIN_TABLE_CHILD_ID)}, inverseForeignKey = @ForeignKey(name = CTL_SCHEMA_JOIN_TABLE_CHILD_FK))
    private Set<CTLSchema> dependencySet = new HashSet<>();

    public CTLSchema() {
    }
    
    public CTLSchema(Long id) {
        this.id = id;
    }

    public CTLSchema(CTLSchemaDto dto) {
        this.id = getLongId(dto.getId());
        this.metaInfo = new CTLSchemaMetaInfo(dto.getMetaInfo());

        Long tenantId = getLongId(dto.getTenantId());
        this.tenant = tenantId != null ? new Tenant(tenantId) : null;

        Long appId = getLongId(dto.getApplicationId());
        this.application = appId != null ? new Application(appId) : null;
        this.body = dto.getBody();
        this.defaultRecord = dto.getDefaultRecord();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();

        Set<CTLSchemaDto> dependencies = dto.getDependencySet();
        if (dependencies != null && !dependencies.isEmpty()) {
            for (CTLSchemaDto dependency : dependencies) {
                dependencySet.add(new CTLSchema(dependency));
            }
        }

        this.name = dto.getName();
        this.createdUsername = dto.getCreatedUsername();
        this.description = dto.getDescription();
    }

    public CTLSchemaMetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CTLSchemaMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDefaultRecord() {
        return defaultRecord;
    }

    public void setDefaultRecord(String defaultRecord) {
        this.defaultRecord = defaultRecord;
    }

    public Set<CTLSchema> getDependencySet() {
        return dependencySet;
    }

    public void setDependencySet(Set<CTLSchema> dependencySet) {
        this.dependencySet = dependencySet;
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

    @Override
    protected CTLSchemaDto createDto() {
        return new CTLSchemaDto();
    }

    @Override
    protected CTLSchema newInstance(Long id) {
        return new CTLSchema(id);
    }

    @Override
    public CTLSchemaDto toDto() {
        CTLSchemaDto ctlSchemaDto = createDto();
        ctlSchemaDto.setId(getStringId());
        ctlSchemaDto.setApplicationId(application != null ? application.getStringId() : null);
        ctlSchemaDto.setTenantId(tenant != null ? tenant.getStringId() : null);
        ctlSchemaDto.setCreatedTime(createdTime);
        ctlSchemaDto.setCreatedUsername(createdUsername);
        ctlSchemaDto.setName(name);
        ctlSchemaDto.setDescription(description);
        ctlSchemaDto.setMetaInfo(metaInfo.toDto());
        ctlSchemaDto.setBody(body);
        ctlSchemaDto.setDefaultRecord(defaultRecord);
        ctlSchemaDto.setDependencySet(DaoUtil.convertDtoSet(dependencySet));
        ctlSchemaDto.setName(name);
        ctlSchemaDto.setCreatedUsername(createdUsername);
        ctlSchemaDto.setDescription(description);
        return ctlSchemaDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CTLSchema schema = (CTLSchema) o;

        if (createdTime != schema.createdTime) return false;
        if (metaInfo != null ? !metaInfo.equals(schema.metaInfo) : schema.metaInfo != null) return false;
        if (tenant != null ? !tenant.equals(schema.tenant) : schema.tenant != null) return false;
        if (application != null ? !application.equals(schema.application) : schema.application != null) return false;
        if (body != null ? !body.equals(schema.body) : schema.body != null) return false;
        if (name != null ? !name.equals(schema.name) : schema.name != null) return false;
        if (description != null ? !description.equals(schema.description) : schema.description != null) return false;
        if (createdUsername != null ? !createdUsername.equals(schema.createdUsername) : schema.createdUsername != null)
            return false;
        return dependencySet != null ? dependencySet.equals(schema.dependencySet) : schema.dependencySet == null;

    }

    @Override
    public int hashCode() {
        int result = metaInfo != null ? metaInfo.hashCode() : 0;
        result = 31 * result + (tenant != null ? tenant.hashCode() : 0);
        result = 31 * result + (application != null ? application.hashCode() : 0);
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
        return "CTLSchema{" +
                "id=" + id +
                ", metaInfo=" + metaInfo +
                ", tenant=" + tenant +
                ", application=" + application +
                ", body='" + body + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdUsername='" + createdUsername + '\'' +
                ", createdTime=" + createdTime +
                ", dependencySet=" + dependencySet +
                '}';
    }
}
