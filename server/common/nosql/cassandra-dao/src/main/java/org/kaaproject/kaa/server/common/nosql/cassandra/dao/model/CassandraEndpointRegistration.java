/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointRegistration;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Table(name = CassandraModelConstants.EP_REGISTRATION_COLUMN_FAMILY_NAME)
public final class CassandraEndpointRegistration implements EndpointRegistration, Serializable {

    @Transient
    private static final long serialVersionUID = 1000L;

    @Transient
    private static final String[] EXCLUDE_FIELDS = { "id" };

    @Column(name = CassandraModelConstants.EP_REGISTRATION_ID_PROPERTY)
    private String id;

    @Column(name = CassandraModelConstants.EP_REGISTRATION_APPLICATION_ID_PROPERTY)
    private String applicationId;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_REGISTRATION_CREDENTIALS_ID_PROPERTY)
    private String credentialsId;
    
    @Column(name = CassandraModelConstants.EP_REGISTRATION_ENDPOINT_ID_PROPERTY)
    private String endpointId;

    @Column(name = CassandraModelConstants.EP_REGISTRATION_SERVER_PROFILE_VERSION_PROPERTY)
    private Integer serverProfileVersion;

    @Column(name = CassandraModelConstants.EP_REGISTRATION_SERVER_PROFILE_BODY_PROPERTY)
    private String serverProfileBody;

    public CassandraEndpointRegistration() {
    }

    public CassandraEndpointRegistration(EndpointRegistrationDto endpointRegistrationInfo) {
        this.id = endpointRegistrationInfo.getId();
        this.applicationId = endpointRegistrationInfo.getApplicationId();
        this.endpointId = endpointRegistrationInfo.getEndpointId();
        this.credentialsId = endpointRegistrationInfo.getCredentialsId();
        this.serverProfileVersion = endpointRegistrationInfo.getServerProfileVersion();
        this.serverProfileBody = endpointRegistrationInfo.getServerProfileBody();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String getEndpointId() {
        return this.endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public String getCredentialsId() {
        return this.credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public Integer getServerProfileVersion() {
        return this.serverProfileVersion;
    }

    public void setServerProfileVersion(Integer serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    @Override
    public String getServerProfileBody() {
        return this.serverProfileBody;
    }

    public void setServerProfileBody(String serverProfileBody) {
        this.serverProfileBody = serverProfileBody;
    }

    @Override
    public EndpointRegistrationDto toDto() {
        EndpointRegistrationDto endpointRegistrationInfo = new EndpointRegistrationDto();
        endpointRegistrationInfo.setId(this.id);
        endpointRegistrationInfo.setApplicationId(this.applicationId);
        endpointRegistrationInfo.setEndpointId(this.endpointId);
        endpointRegistrationInfo.setCredentialsId(this.credentialsId);
        endpointRegistrationInfo.setServerProfileVersion(this.serverProfileVersion);
        endpointRegistrationInfo.setServerProfileBody(this.serverProfileBody);
        return endpointRegistrationInfo;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, CassandraEndpointRegistration.EXCLUDE_FIELDS);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other, CassandraEndpointRegistration.EXCLUDE_FIELDS);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    public String generateId() {
        if (isBlank(id)) {
            id = credentialsId;
        }
        return id;
    }
}
