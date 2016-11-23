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

package org.kaaproject.kaa.common.dto.event;

import org.kaaproject.kaa.common.dto.BaseSchemaDto;

//TODO: Unique Key should be {tenantId, FQN, version}
public class EventClassDto extends BaseSchemaDto {

  private static final long serialVersionUID = 2052580632293959408L;

  private String tenantId;
  private String ecfvId;
  private String fqn;
  private EventClassType type;

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getEcfvId() {
    return ecfvId;
  }

  public void setEcfvId(String ecfvId) {
    this.ecfvId = ecfvId;
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
    result = prime * result + ((ecfvId == null) ? 0 : ecfvId.hashCode());
    result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result
        + ((tenantId == null) ? 0 : tenantId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + version;
    result = prime * result + ((ctlSchemaId == null) ? 0 : ctlSchemaId.hashCode());
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
    EventClassDto other = (EventClassDto) obj;
    if (ecfvId == null) {
      if (other.ecfvId != null) {
        return false;
      }
    } else if (!ecfvId.equals(other.ecfvId)) {
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
    if (tenantId == null) {
      if (other.tenantId != null) {
        return false;
      }
    } else if (!tenantId.equals(other.tenantId)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    if (version != other.version) {
      return false;
    }
    if (ctlSchemaId != other.ctlSchemaId) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("EventClassDto [id=");
    builder.append(id);
    builder.append(", tenantId=");
    builder.append(tenantId);
    builder.append(", ecfvId=");
    builder.append(ecfvId);
    builder.append(", fqn=");
    builder.append(fqn);
    builder.append(", type=");
    builder.append(type);
    builder.append(", version=");
    builder.append(version);
    builder.append(", ctlSchemaId=");
    builder.append(ctlSchemaId);
    builder.append("]");
    return builder.toString();
  }

}
