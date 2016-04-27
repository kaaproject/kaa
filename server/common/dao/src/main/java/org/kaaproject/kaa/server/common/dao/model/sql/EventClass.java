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
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_EVENT_CLASS_FAMILY_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_SCHEMA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TYPE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_VERSION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = EVENT_CLASS_TABLE_NAME)
public class EventClass extends GenericModel<EventClassDto> {

    private static final long serialVersionUID = 3766947955702551264L;

    @ManyToOne
    @JoinColumn(name = EVENT_CLASS_TENANT_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = EVENT_CLASS_EVENT_CLASS_FAMILY_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EventClassFamily ecf;

    @Column(name = EVENT_CLASS_FQN)
    private String fqn;

    @Column(name = EVENT_CLASS_TYPE)
    @Enumerated(EnumType.STRING)
    private EventClassType type;

    @Lob
    @Column(name = EVENT_CLASS_SCHEMA)
    private String schema;

    @Column(name = EVENT_CLASS_VERSION)
    private int version;

    public EventClass() {
    }

    public EventClass(Long id) {
        this.id = id;
    }

    public EventClass(EventClassDto dto) {
        this.id = getLongId(dto.getId());
        Long tenantId = getLongId(dto.getTenantId());
        if (tenantId != null) {
            this.tenant = new Tenant(tenantId);
        }
        Long ecfId = getLongId(dto.getEcfId());
        if (ecfId != null) {
            this.ecf = new EventClassFamily(ecfId);
        }
        this.fqn = dto.getFqn();
        this.type = dto.getType();
        this.schema = dto.getSchema();
        this.version = dto.getVersion();
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public EventClassFamily getEcf() {
        return ecf;
    }

    public void setEcf(EventClassFamily ecf) {
        this.ecf = ecf;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public EventClassType getType() {
        return type;
    }

    public void setType(EventClassType type) {
        this.type = type;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ecf == null) ? 0 : ecf.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + version;
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
        EventClass other = (EventClass) obj;
        if (ecf == null) {
            if (other.ecf != null) {
                return false;
            }
        } else if (!ecf.equals(other.ecf)) {
            return false;
        }
        if (fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!fqn.equals(other.fqn)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        if (tenant == null) {
            if (other.tenant != null) {
                return false;
            }
        } else if (!tenant.equals(other.tenant)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return version == other.version;
    }

    @Override
    protected EventClassDto createDto() {
        return new EventClassDto();
    }

    @Override
    protected GenericModel<EventClassDto> newInstance(Long id) {
        return new EventClass(id);
    }

    @Override
    public EventClassDto toDto() {
        EventClassDto dto = createDto();
        dto.setId(getStringId());
        if (tenant != null) {
            dto.setTenantId(tenant.getStringId());
        }
        if (ecf != null) {
            dto.setEcfId(ecf.getStringId());
        }
        dto.setFqn(fqn);
        dto.setType(type);
        dto.setSchema(schema);
        dto.setVersion(version);
        return dto;
    }

    @Override
    public String toString() {
        return "EventClass [ecf=" + ecf + ", fqn=" + fqn + ", type=" + type + ", schema=" + schema + ", version=" + version + ", id=" + id + "]";
    }


}
