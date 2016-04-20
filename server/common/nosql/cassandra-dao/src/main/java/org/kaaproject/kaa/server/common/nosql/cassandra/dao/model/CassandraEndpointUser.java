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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.parseId;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_ACCESS_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_ENDPOINT_IDS_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_EXTERNAL_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_TENANT_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_USERNAME_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.KEY_DELIMITER;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUser;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(name = EP_USER_COLUMN_FAMILY_NAME)
public final class CassandraEndpointUser implements EndpointUser, Serializable {

    @Transient
    private static final long serialVersionUID = 3766947955702551264L;

    @PartitionKey(value = 0)
    @Column(name = EP_USER_EXTERNAL_ID_PROPERTY)
    private String externalId;
    @PartitionKey(value = 1)
    @Column(name = EP_USER_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = EP_USER_USER_ID_PROPERTY)
    private String id;
    @Column(name = EP_USER_USERNAME_PROPERTY)
    private String username;

    @Column(name = EP_USER_ACCESS_TOKEN_PROPERTY)
    private String accessToken;
    @Column(name = EP_USER_ENDPOINT_IDS_PROPERTY)
    private List<String> endpointIds;
    
    @Column(name = OPT_LOCK)
    private Long version;

    public CassandraEndpointUser() {
    }

    public CassandraEndpointUser(String id) {
        String[] columns = parseId(id);
        if (columns.length == 2) {
            externalId = columns[0];
            tenantId = columns[1];
        }
    }

    public CassandraEndpointUser(EndpointUserDto dto) {
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
        CassandraEndpointUser other = (CassandraEndpointUser) obj;
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

    public String generateId() {
        if (isBlank(id)) {
            id = externalId + KEY_DELIMITER + tenantId;
        }
        return id;
    }
}