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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointRegistration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Document(collection = MongoModelConstants.ENDPOINT_REGISTRATION)
public final class MongoEndpointRegistration implements EndpointRegistration, Serializable {

    private static final long serialVersionUID = 1000L;

    private static final String[] EXCLUDE_FIELDS = { "id" };

    @Id
    private String id;

    @Field(MongoModelConstants.EP_REGISTRATION_APPLICATION_ID)
    private String applicationId;

    @Indexed
    @Field(MongoModelConstants.EP_REGISTRATION_ENDPOINT_ID)
    private String endpointId;

    @Indexed
    @Field(MongoModelConstants.EP_REGISTRATION_CREDENTIALS_ID)
    private String credentialsId;

    @Field(MongoModelConstants.EP_REGISTRATION_SERVER_PROFILE_VERSION)
    private Integer serverProfileVersion;

    @Field(MongoModelConstants.EP_REGISTRATION_SERVER_PROFILE_BODY)
    private String serverProfileBody;

    public MongoEndpointRegistration() {
    }

    public MongoEndpointRegistration(EndpointRegistrationDto endpointRegistration) {
        this.id = endpointRegistration.getId();
        this.applicationId = endpointRegistration.getApplicationId();
        this.endpointId = endpointRegistration.getEndpointId();
        this.credentialsId = endpointRegistration.getCredentialsId();
        this.serverProfileVersion = endpointRegistration.getServerProfileVersion();
        this.serverProfileBody = endpointRegistration.getServerProfileBody();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getApplicationId() {
        return this.applicationId;
    }

    @Override
    public String getEndpointId() {
        return this.endpointId;
    }

    @Override
    public String getCredentialsId() {
        return this.credentialsId;
    }

    @Override
    public Integer getServerProfileVersion() {
        return this.serverProfileVersion;
    }

    @Override
    public String getServerProfileBody() {
        return this.serverProfileBody;
    }

    @Override
    public EndpointRegistrationDto toDto() {
        EndpointRegistrationDto endpointRegistration = new EndpointRegistrationDto();
        endpointRegistration.setId(this.id);
        endpointRegistration.setApplicationId(this.applicationId);
        endpointRegistration.setEndpointId(this.endpointId);
        endpointRegistration.setCredentialsId(this.credentialsId);
        endpointRegistration.setServerProfileVersion(this.serverProfileVersion);
        endpointRegistration.setServerProfileBody(this.serverProfileBody);
        return endpointRegistration;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, MongoEndpointRegistration.EXCLUDE_FIELDS);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other, MongoEndpointRegistration.EXCLUDE_FIELDS);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
