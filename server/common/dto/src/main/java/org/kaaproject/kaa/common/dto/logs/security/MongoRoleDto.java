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

package org.kaaproject.kaa.common.dto.logs.security;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;

public class MongoRoleDto implements HasId, Serializable {
    
    private static final long serialVersionUID = -1178384812174668115L;
    
    private String id;
    private String role;
    private List<MongoPrivilegeDto> privileges;
    private List<String> roles;
    
    public MongoRoleDto () {
        
    }
    
    public MongoRoleDto(String role, List<MongoPrivilegeDto> privileges,  List<String> roles) {
        this.role = role;
        this.privileges = privileges;
        this.roles = roles;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public void setRoleName(String role) {
        this.role = role;
    }
    
    public String getRoleName() {
        return role;
    }
    
    public void setPrivileges(List<MongoPrivilegeDto> privileges) {
        this.privileges = privileges;
    }
    
    public List<MongoPrivilegeDto> getPrivileges() {
        return privileges;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public List<String> getRoles() {
        return roles;
    }
}
