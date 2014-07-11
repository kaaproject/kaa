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

package org.kaaproject.kaa.server.common.dao.model.mongo;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.kaaproject.kaa.common.dto.logs.security.MongoPrivilegeDto;
import org.kaaproject.kaa.common.dto.logs.security.MongoRoleDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = SecureRole.COLLECTION_NAME)
public final class SecureRole  implements ToDto<MongoRoleDto>, Serializable {

    private static final long serialVersionUID = -4340537297110929016L;

    public static final String COLLECTION_NAME = "system.roles";
    
    @Id
    private String id;
    @Indexed
    private String role;
    private List<SecurePrivilege> privileges;
    private List<String> roles;

    public SecureRole() {
        
    }

    public SecureRole(MongoRoleDto dto) {
        this.id = dto.getId();
        this.role = dto.getRoleName();
        this.privileges = convertPrivileges(dto.getPrivileges());
        this.roles = dto.getRoles();
    }
    
    private List<SecurePrivilege> convertPrivileges(List<MongoPrivilegeDto> dtos) {
        if (dtos == null) {
            return null;
        }
        List<SecurePrivilege> securePrivileges = new ArrayList<>();
        for (MongoPrivilegeDto dto : dtos) {
            securePrivileges.add(new SecurePrivilege(dto));
        }
        return securePrivileges;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getRoleName() {
        return role;
    }

    public void setRoleName(String role) {
        this.role = role;
    }
    
    public List<SecurePrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<SecurePrivilege> privileges) {
        this.privileges = privileges;
    }
    
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SecureRole that = (SecureRole) o;
        
        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }
        if (privileges != null ? !privileges.equals(that.privileges) : that.privileges != null) {
            return false;
        }
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (privileges != null ? privileges.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogSchema{" +
                "id='" + id + '\'' +
                "role='" + role + '\'' +
                ", privileges='" + privileges + '\'' +
                ", roles='" + roles +
                '}';
    }

    @Override
    public MongoRoleDto toDto() {
        MongoRoleDto dto = new MongoRoleDto();
        dto.setId(id);
        dto.setRoleName(role);
        dto.setPrivileges(convertDtoList(privileges));
        dto.setRoles(roles);
        return dto;
    }

}
