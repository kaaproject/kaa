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

import org.kaaproject.kaa.common.dto.TenantDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = TENANT_TABLE_NAME)
public class Tenant extends GenericModel<TenantDto> implements Serializable {

    private static final long serialVersionUID = 4800104335859412180L;

    @Column(name = TENANT_NAME, unique = true, length = 100)
    private String name;

    public Tenant() {
    }

    public Tenant(Long id) {
        this.id = id;
    }

    public Tenant(TenantDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.name = dto.getName();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 67;
        int result = 7;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Tenant other = (Tenant) obj;
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
        return true;
    }

    @Override
    protected TenantDto createDto() {
        return new TenantDto();
    }

    @Override
    protected GenericModel<TenantDto> newInstance(Long id) {
        return new Tenant(id);
    }

    @Override
    public TenantDto toDto() {
        TenantDto dto = createDto();
        dto.setId(getStringId());
        dto.setName(name);
        return dto;
    }

    @Override
    public String toString() {
        return "Tenant [name=" + name + ", id=" + id + "]";
    }

}
