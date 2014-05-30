/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToString;

@Document(collection = User.COLLECTION_NAME)
public final class User implements ToDto<UserDto>, Serializable {

    private static final long serialVersionUID = 3766947955702551264L;

    public static final String COLLECTION_NAME = "user";

    @Id
    private String id;
    @Field("username")
    private String username;
    @Field("external_uid")
    private String externalUid;
    @Field("tenant_id")
    private ObjectId tenantId;
    @Field("authority")
    private KaaAuthorityDto authority;

    public User() {
    }

    public User(UserDto dto) {
        this.id = dto.getId();
        this.username = dto.getUsername();
        this.externalUid = dto.getExternalUid();
        this.tenantId = idToObjectId(dto.getTenantId());
        this.authority = dto.getAuthority();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ObjectId getTenantId() {
        return tenantId;
    }

    public void setTenantId(ObjectId tenantId) {
        this.tenantId = tenantId;
    }

    public KaaAuthorityDto getAuthority() {
        return authority;
    }

    public void setAuthority(KaaAuthorityDto authority) {
        this.authority = authority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((externalUid == null) ? 0 : externalUid.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
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
        if (externalUid == null) {
            if (other.externalUid != null) {
                return false;
            }
        } else if (!externalUid.equals(other.externalUid)) {
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
        return true;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", externalUid=" + externalUid
                + ", tenantId=" + tenantId + ", authority=" + authority + "]";
    }

    @Override
    public UserDto toDto() {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUsername(username);
        dto.setExternalUid(externalUid);
        dto.setTenantId(idToString(tenantId));
        dto.setAuthority(authority);
        return dto;
    }
}
