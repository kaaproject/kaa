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

package org.kaaproject.kaa.server.common.dao.model.sql;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DEFAULT_RECORD;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_CHILD_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_CHILD_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_PARENT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_PARENT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_UNIQUE_CONSTRAINT;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_VERSION;
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
@UniqueConstraint(columnNames = {CTL_SCHEMA_META_INFO_ID, CTL_SCHEMA_VERSION}, name = CTL_SCHEMA_UNIQUE_CONSTRAINT))
public class CTLSchema extends GenericModel<CTLSchemaDto> implements Serializable {

    private static final long serialVersionUID = -1179381742235545494L;
    
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = CTL_SCHEMA_META_INFO_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_META_INFO_FK))
    private CTLSchemaMetaInfo metaInfo;
    @Column(name = CTL_SCHEMA_VERSION)
    private Integer version;
    @Lob
    @Column(name = CTL_SCHEMA_BODY)
    private String body;
    @Lob
    @Column(name = CTL_SCHEMA_DEFAULT_RECORD)
    private String defaultRecord;
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
        this.version = dto.getVersion();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
        update(dto);
    }
    
    public void update(CTLSchemaDto dto) {
        this.body = dto.getBody();
        this.defaultRecord = dto.getDefaultRecord();
        Set<CTLSchemaDto> dependencies = dto.getDependencySet();
        if (dependencies != null && !dependencies.isEmpty()) {
            for (CTLSchemaDto dependency : dependencies) {
                dependencySet.add(new CTLSchema(dependency));
            }
        }
    }
    
    public CTLSchemaMetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CTLSchemaMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
    protected GenericModel<CTLSchemaDto> newInstance(Long id) {
        return new CTLSchema(id);
    }

    @Override
    public CTLSchemaDto toDto() {
        CTLSchemaDto ctlSchemaDto = createDto();
        ctlSchemaDto.setId(getStringId());
        ctlSchemaDto.setMetaInfo(metaInfo.toDto());
        ctlSchemaDto.setVersion(version);
        ctlSchemaDto.setCreatedTime(createdTime);
        ctlSchemaDto.setCreatedUsername(createdUsername);
        ctlSchemaDto.setBody(body);
        ctlSchemaDto.setDefaultRecord(defaultRecord);
        ctlSchemaDto.setDependencySet(DaoUtil.convertDtoSet(dependencySet));
        return ctlSchemaDto;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((defaultRecord == null) ? 0 : defaultRecord.hashCode());
        result = prime * result + ((dependencySet == null) ? 0 : dependencySet.hashCode());
        result = prime * result + ((metaInfo == null) ? 0 : metaInfo.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        CTLSchema other = (CTLSchema) obj;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (createdTime != other.createdTime)
            return false;
        if (createdUsername == null) {
            if (other.createdUsername != null)
                return false;
        } else if (!createdUsername.equals(other.createdUsername))
            return false;
        if (defaultRecord == null) {
            if (other.defaultRecord != null)
                return false;
        } else if (!defaultRecord.equals(other.defaultRecord))
            return false;
        if (dependencySet == null) {
            if (other.dependencySet != null)
                return false;
        } else if (!dependencySet.equals(other.dependencySet))
            return false;
        if (metaInfo == null) {
            if (other.metaInfo != null)
                return false;
        } else if (!metaInfo.equals(other.metaInfo))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTLSchema [metaInfo=");
        builder.append(metaInfo);
        builder.append(", version=");
        builder.append(version);
        builder.append(", body=");
        builder.append(body);
        builder.append(", defaultRecord=");
        builder.append(defaultRecord);
        builder.append(", createdUsername=");
        builder.append(createdUsername);
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", dependencySet=");
        builder.append(dependencySet);
        builder.append("]");
        return builder.toString();
    }

}
