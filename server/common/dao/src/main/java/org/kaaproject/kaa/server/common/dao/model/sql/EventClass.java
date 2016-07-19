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

import javax.persistence.*;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.*;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = EVENT_CLASS_TABLE_NAME)
public class EventClass extends BaseSchema<EventClassDto> {

    private static final long serialVersionUID = 3766947955702551264L;

    @ManyToOne
    @JoinColumn(name = EVENT_CLASS_TENANT_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tenant tenant;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EventClassFamilyVersion ecf;

    @Column(name = EVENT_CLASS_FQN)
    private String fqn;

    @Column(name = EVENT_CLASS_TYPE)
    @Enumerated(EnumType.STRING)
    private EventClassType type;

    public EventClass() {
    }

    public EventClass(Long id) {
        this.id = id;
    }

    public EventClass(EventClassDto dto) {
        super(dto);
        this.id = getLongId(dto.getId());
        Long tenantId = getLongId(dto.getTenantId());
        if (tenantId != null) {
            this.tenant = new Tenant(tenantId);
        }
        Long ecfId = getLongId(dto.getEcfId());
        if (ecfId != null) {
            this.ecf = new EventClassFamilyVersion(ecfId);
        }
        this.fqn = dto.getFqn();
        this.type = dto.getType();
    }



    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public EventClassFamilyVersion getEcf() {
        return ecf;
    }

    public void setEcf(EventClassFamilyVersion ecf) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ecf == null) ? 0 : ecf.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((getCtlSchema() == null) ? 0 : getCtlSchema().hashCode());
        result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (getCtlSchema() == null) {
            if (other.getCtlSchema() != null) {
                return false;
            }
        } else if (!getCtlSchema().equals(other.getCtlSchema())) {
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
        return true;
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
        dto.setCtlSchemaId(getCtlSchema().getStringId());
        dto.setApplicationId(getApplicationId());
        return dto;
    }

    @Override
    public String toString() {
        return "EventClass [ecf=" + ecf + ", fqn=" + fqn + ", type=" + type + ", ctlSchema=" + getCtlSchema() + ", id=" + id + "]";
    }


}
