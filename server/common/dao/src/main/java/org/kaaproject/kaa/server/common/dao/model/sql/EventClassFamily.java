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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_CLASS_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_NAMESPACE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FAMILY_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_SCHEMA_VERSION_EVENT_CLASS_FAMILY_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = EVENT_CLASS_FAMILY_TABLE_NAME)
public class EventClassFamily extends GenericModel<EventClassFamilyDto> {

    private static final long serialVersionUID = 3766947955702551264L;

    @ManyToOne
    @JoinColumn(name = EVENT_CLASS_FAMILY_TENANT_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tenant tenant;

    @Column(name = EVENT_CLASS_FAMILY_NAME)
    private String name;

    @Column(name = EVENT_CLASS_FAMILY_NAMESPACE)
    private String namespace;

    @Column(name = EVENT_CLASS_FAMILY_CLASS_NAME)
    private String className;

    @Column(name = EVENT_CLASS_FAMILY_DESCRIPTION)
    protected String description;

    @Column(name = EVENT_CLASS_FAMILY_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = EVENT_CLASS_FAMILY_CREATED_TIME)
    protected long createdTime;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = EVENT_SCHEMA_VERSION_EVENT_CLASS_FAMILY_ID, nullable = false)
    private List<EventSchemaVersion> schemas;

    public EventClassFamily() {
    }

    public EventClassFamily(Long id) {
        this.id = id;
    }

    public EventClassFamily(EventClassFamilyDto dto) {
        this.id = getLongId(dto.getId());
        Long tenantId = getLongId(dto.getTenantId());
        if (tenantId != null) {
            this.tenant = new Tenant(tenantId);
        }
        this.name = dto.getName();
        this.namespace = dto.getNamespace();
        this.className = dto.getClassName();
        this.description = dto.getDescription();
        this.createdUsername = dto.getCreatedUsername();
        this.createdTime = dto.getCreatedTime();
        if (dto.getSchemas() != null) {
            this.schemas = new ArrayList<>(dto.getSchemas().size());
            for (EventSchemaVersionDto schema : dto.getSchemas()) {
                this.schemas.add(new EventSchemaVersion(schema));
            }
        }
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EventSchemaVersion> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<EventSchemaVersion> schemas) {
        this.schemas = schemas;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((className == null) ? 0 : className.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
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
        EventClassFamily other = (EventClassFamily) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (createdTime != other.createdTime) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (tenant == null) {
            if (other.tenant != null) {
                return false;
            }
        } else if (!tenant.equals(other.tenant)) {
            return false;
        }
        return true;
    }

    @Override
    protected EventClassFamilyDto createDto() {
        return new EventClassFamilyDto();
    }

    @Override
    protected GenericModel<EventClassFamilyDto> newInstance(Long id) {
        return new EventClassFamily(id);
    }

    @Override
    public EventClassFamilyDto toDto() {
        EventClassFamilyDto dto = createDto();
        dto.setId(getStringId());
        if (tenant != null) {
            dto.setTenantId(tenant.getStringId());
        }
        dto.setName(name);
        dto.setNamespace(namespace);
        dto.setClassName(className);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        if (schemas != null) {
            List<EventSchemaVersionDto> schemasDto = new ArrayList<>(schemas.size());
            for (EventSchemaVersion schema : schemas) {
                schemasDto.add(schema.toDto());
            }
            dto.setSchemas(schemasDto);
        }
        return dto;
    }

    @Override
    public String toString() {
        return "EventClassFamily [name=" + name + ", namespace=" + namespace + ", className=" + className + ", description=" + description
                + ", createdUsername=" + createdUsername + ", createdTime=" + createdTime + ", id=" + id + "]";
    }

}
