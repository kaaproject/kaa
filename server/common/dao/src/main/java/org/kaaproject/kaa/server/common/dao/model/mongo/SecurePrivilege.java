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

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

import java.io.Serializable;
import java.util.List;
import org.kaaproject.kaa.common.dto.logs.security.MongoPrivilegeDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class SecurePrivilege  implements ToDto<MongoPrivilegeDto>, Serializable {

    private static final long serialVersionUID = 9026847211557212934L;
    
    @Indexed
    private SecureResource resource;
    private List<String> actions;

    public SecurePrivilege() {
        
    }

    public SecurePrivilege(MongoPrivilegeDto dto) {
        this.resource = new SecureResource(dto.getResource());
        this.actions = dto.getActions();
    }
    
    public SecureResource getResource() {
        return resource;
    }

    public void setResource(SecureResource resource) {
        this.resource = resource;
    }
    
    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SecurePrivilege that = (SecurePrivilege) o;
        
        if (resource != null ? !resource.equals(that.resource) : that.resource != null) {
            return false;
        }
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogSchema{" +
                "resource='" + resource + '\'' +
                ", actions='" + actions + '\'' +
                '}';
    }

    @Override
    public MongoPrivilegeDto toDto() {
        MongoPrivilegeDto dto = new MongoPrivilegeDto();
        dto.setResource(getDto(resource));
        dto.setActions(actions);
        return dto;
    }

}
