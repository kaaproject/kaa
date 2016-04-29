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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUser;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_USER;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_ACCESS_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_ENDPOINT_IDS;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_EXTERNAL_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_TENANT_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_USER_USERNAME;

@Document(collection = ENDPOINT_USER)
public final class MongoEndpointUser implements EndpointUser, Serializable {

    private static final long serialVersionUID = 3766947955702551264L;

    @Id
    private String id;
    @Field(EP_USER_USERNAME)
    private String username;
    @Field(EP_USER_EXTERNAL_ID)
    private String externalId;
    @Field(EP_USER_TENANT_ID)
    private String tenantId;
    @Field(EP_USER_ACCESS_TOKEN)
    private String accessToken;
    @Field(EP_USER_ENDPOINT_IDS)
    private List<String> endpointIds;
    @Version
    @Field(OPT_LOCK)
    private Long version;
    
    public MongoEndpointUser() {
    }

    public MongoEndpointUser(EndpointUserDto dto) {
        this.id = dto.getId();
        this.username = dto.getUsername();
        this.externalId = dto.getExternalId();
        this.tenantId = dto.getTenantId();
        this.accessToken = dto.getAccessToken();
        this.endpointIds = dto.getEndpointIds();
        this.version = dto.getVersion();
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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<String> getEndpointIds() {
        return endpointIds;
    }

    public void setEndpointIds(List<String> endpointIds) {
        this.endpointIds = endpointIds;
    }
    
    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
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
        MongoEndpointUser other = (MongoEndpointUser) obj;
        if (externalId == null) {
            if (other.externalId != null) {
                return false;
            }
        } else if (!externalId.equals(other.externalId)) {
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
        return "EndpointUser [id=" + id + ", username=" + username + ", externalId=" + externalId + ", tenantId=" + tenantId + ", accessToken=" + accessToken
                + ", endpointIds=" + endpointIds + "]";
    }

    @Override
    public EndpointUserDto toDto() {
        EndpointUserDto dto = new EndpointUserDto();
        dto.setId(id);
        dto.setUsername(username);
        dto.setExternalId(externalId);
        dto.setTenantId(tenantId);
        dto.setAccessToken(accessToken);
        dto.setEndpointIds(endpointIds);
        dto.setVersion(version);
        return dto;
    }
}