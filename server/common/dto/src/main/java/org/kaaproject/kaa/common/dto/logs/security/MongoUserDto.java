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

public class MongoUserDto implements HasId, Serializable {
    
    private static final long serialVersionUID = -5098682629917434127L;
    
    private String id;
    private String user;
    private String pwd;
    private List<String> roles;
    
    public MongoUserDto () {
        
    }
    
    public MongoUserDto(String user, String pwd, List<String> roles) {
        this.user = user;
        this.pwd = pwd;
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
    
    public void setUserName(String user) {
        this.user = user;
    }
    
    public String getUserName() {
        return user;
    }
    
    public void setPassword(String pwd) {
        this.pwd = pwd;
    }
    
    public String getPassword() {
        return pwd;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public List<String> getRoles() {
        return roles;
    }
}
