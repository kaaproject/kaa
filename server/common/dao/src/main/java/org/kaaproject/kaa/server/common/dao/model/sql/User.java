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
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_AUTHORITY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_EXTERNAL_UID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = USER_TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {USER_TENANT_ID, USER_EXTERNAL_UID}))
public class User extends GenericModel<UserDto> implements Serializable {

    private static final long serialVersionUID = -6651349022301623429L;

    @Column(name = USER_NAME)
    private String username;

    @Column(name = USER_EXTERNAL_UID)
    private String externalUid;

    @ManyToOne
    @JoinColumn(name = USER_TENANT_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tenant tenant;

    @Column(name = USER_AUTHORITY)
    @Enumerated(EnumType.STRING)
    private KaaAuthorityDto authority;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public User(UserDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.username = dto.getUsername();
            this.externalUid = dto.getExternalUid();
            this.authority = dto.getAuthority();
            Long tenantId = getLongId(dto.getTenantId());
            this.tenant = tenantId != null ? new Tenant(tenantId) : null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExternalUid() {
        return externalUid;
    }

    public void setExternalUid(String externalUid) {
        this.externalUid = externalUid;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public KaaAuthorityDto getAuthority() {
        return authority;
    }

    public void setAuthority(KaaAuthorityDto authority) {
        this.authority = authority;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", externalUid=" + externalUid + ", authority=" + authority + ", id=" + id + "]";
    }

    @Override
    protected UserDto createDto() {
        return new UserDto();
    }

    @Override
    protected GenericModel<UserDto> newInstance(Long id) {
        return new User(id);
    }

    @Override
    public UserDto toDto() {
        UserDto dto = createDto();
        dto.setId(getStringId());
        dto.setUsername(username);
        dto.setExternalUid(externalUid);
        dto.setTenantId(ModelUtils.getStringId(tenant));
        dto.setAuthority(authority);
        return dto;
    }

    @Override
    public int hashCode() {
        final int prime = 45;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((externalUid == null) ? 0 : externalUid.hashCode());
        result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
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
        User other = (User) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        if (externalUid == null) {
            if (other.externalUid != null) {
                return false;
            }
        } else if (!externalUid.equals(other.externalUid)) {
            return false;
        }
        if (tenant == null) {
            if (other.tenant != null) {
                return false;
            }
        } else if (!tenant.equals(other.tenant)) {
            return false;
        }
        if (authority == null) {
            if (other.authority != null) {
                return false;
            }
        } else if (!authority.equals(other.authority)) {
            return false;
        }
        return true;
    }
}
