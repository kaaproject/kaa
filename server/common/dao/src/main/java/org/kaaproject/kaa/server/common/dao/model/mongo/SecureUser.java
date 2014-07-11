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

import java.io.Serializable;
import java.util.List;
import org.kaaproject.kaa.common.dto.logs.security.MongoUserDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = SecureUser.COLLECTION_NAME)
public final class SecureUser  implements ToDto<MongoUserDto>, Serializable {

    private static final long serialVersionUID = 8255911503802013176L;

    public static final String COLLECTION_NAME = "system.users";
    
    @Id
    private String id;
    @Indexed
    private String user;
    private String pwd;
    private List<String> roles;

    public SecureUser() {
        
    }

    public SecureUser(MongoUserDto dto) {
        this.id = dto.getId();
        this.user = dto.getUserName();
        this.pwd = dto.getPassword();
        this.roles = dto.getRoles();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserName() {
        return user;
    }

    public void setUserName(String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return pwd;
    }

    public void setPassword(String pwd) {
        this.pwd = pwd;
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

        SecureUser that = (SecureUser) o;
        
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }
        if (pwd != null ? !pwd.equals(that.pwd) : that.pwd != null) {
            return false;
        }
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (pwd != null ? pwd.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogSchema{" +
                "id='" + id + '\'' +
                "user='" + user + '\'' +
                ", pwd='" + pwd + '\'' +
                ", roles='" + roles +
                '}';
    }

    @Override
    public MongoUserDto toDto() {
        MongoUserDto dto = new MongoUserDto();
        dto.setId(id);
        dto.setUserName(user);
        dto.setPassword(pwd);
        dto.setRoles(roles);
        return dto;
    }

}
