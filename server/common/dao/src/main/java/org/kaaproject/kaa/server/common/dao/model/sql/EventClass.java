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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_EVENT_CLASS_FAMILY_VERSION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TYPE;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = EVENT_CLASS_TABLE_NAME)
public class EventClass extends BaseSchema<EventClassDto> {

  private static final long serialVersionUID = 3766947955702551264L;

  @ManyToOne
  @JoinColumn(name = EVENT_CLASS_TENANT_ID, nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Tenant tenant;

  @ManyToOne
  @JoinColumn(name = EVENT_CLASS_EVENT_CLASS_FAMILY_VERSION_ID, nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private EventClassFamilyVersion ecfv;

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

  /**
   * Create new instance of EventClass.
   *
   * @param dto data transfer object, contain data for new instance
   */
  public EventClass(EventClassDto dto) {
    super(dto);
    this.id = getLongId(dto.getId());
    Long tenantId = getLongId(dto.getTenantId());
    if (tenantId != null) {
      this.tenant = new Tenant(tenantId);
    }
    this.fqn = dto.getFqn();
    this.type = dto.getType();
    Long ecfvId = getLongId(dto.getEcfvId());
    if (ecfvId != null) {
      this.ecfv = new EventClassFamilyVersion(ecfvId);
    }
    this.version = dto.getVersion();
    this.name = dto.getName();
    this.description = dto.getDescription();
    this.createdUsername = dto.getCreatedUsername();
    this.createdTime = dto.getCreatedTime();
    Long ctlId = getLongId(dto.getCtlSchemaId());
    if (ctlId != null) {
      this.setCtlSchema(new CtlSchema(ctlId));
    }
  }


  public Tenant getTenant() {
    return tenant;
  }

  public void setTenant(Tenant tenant) {
    this.tenant = tenant;
  }

  public EventClassFamilyVersion getEcfv() {
    return ecfv;
  }

  public void setEcfv(EventClassFamilyVersion ecfv) {
    this.ecfv = ecfv;
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
    result = prime * result + ((ecfv == null) ? 0 : ecfv.hashCode());
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
    if (ecfv == null) {
      if (other.ecfv != null) {
        return false;
      }
    } else if (!ecfv.equals(other.ecfv)) {
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
    if (ecfv != null) {
      dto.setEcfvId(ecfv.getStringId());
    }
    dto.setFqn(fqn);
    dto.setType(type);
    dto.setCreatedUsername(createdUsername);
    dto.setCreatedTime(createdTime);
    dto.setDescription(description);
    dto.setName(name);
    dto.setVersion(version);
    dto.setCtlSchemaId(getCtlSchema().getStringId());
    return dto;
  }

  @Override
  public String toString() {
    return "EventClass [ecfv=" + ecfv + ", fqn=" + fqn + ", type=" + type + ", ctlSchema="
            + getCtlSchema() + ", id=" + id + "]";
  }


}
