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

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_APP_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_VERSION_PROPERTY;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(name = EP_USER_CONF_COLUMN_FAMILY_NAME)
public class CassandraEndpointUserConfiguration implements EndpointUserConfiguration, Serializable {

    @Transient
    private static final long serialVersionUID = 7678593961823855167L;

    @PartitionKey
    @Column(name = EP_USER_CONF_USER_ID_PROPERTY)
    private String userId;
    @ClusteringColumn(value = 0)
    @Column(name = EP_USER_CONF_APP_TOKEN_PROPERTY)
    private String appToken;
    @ClusteringColumn(value = 1)
    @Column(name = EP_USER_CONF_VERSION_PROPERTY)
    private Integer schemaVersion;
    @Column(name = EP_USER_CONF_BODY_PROPERTY)
    private String body;

    public CassandraEndpointUserConfiguration() {
    }

    public CassandraEndpointUserConfiguration(EndpointUserConfigurationDto dto) {
        this.userId = dto.getUserId();
        this.appToken = dto.getAppToken();
        this.schemaVersion = dto.getSchemaVersion();
        this.body = dto.getBody();
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getAppToken() {
        return appToken;
    }

    @Override
    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    @Override
    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    @Override
    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public EndpointUserConfigurationDto toDto() {
        EndpointUserConfigurationDto dto = new EndpointUserConfigurationDto();
        dto.setAppToken(appToken);
        dto.setBody(body);
        dto.setSchemaVersion(schemaVersion);
        dto.setUserId(userId);
        return dto;
    }
}
