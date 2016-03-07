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
import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointCredentials;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Document(collection = MongoModelConstants.ENDPOINT_CREDENTIALS)
public final class MongoEndpointCredentials implements EndpointCredentials, Serializable {

    private static final long serialVersionUID = 1000L;

    private static final String[] EXCLUDE_FIELDS = { "id", "serverProfileExpired" };

    @Id
    private String id;

    @Field(MongoModelConstants.ENDPOINT_CREDENTIALS_APPLICATION_ID)
    private String applicationId;

    @Field(MongoModelConstants.ENDPOINT_CREDENTIALS_ENDPOINT_ID)
    @Indexed
    private String endpointId;

    @Field(MongoModelConstants.ENDPOINT_CREDENTIALS_PUBLIC_KEY)
    private String publicKey;

    @Field(MongoModelConstants.ENDPOINT_CREDENTIALS_SERVER_PROFILE_VERSION)
    private Integer serverProfileVersion;

    @Field(MongoModelConstants.ENDPOINT_CREDENTIALS_SERVER_PROFILE_BODY)
    private String serverProfileBody;

    @Field(MongoModelConstants.ENDPOINT_CREDENTIALS_SERVER_PROFILE_EXPIRED)
    private Boolean serverProfileExpired = false;

    public MongoEndpointCredentials() {
    }

    public MongoEndpointCredentials(EndpointCredentialsDto endpointCredentials) {
        this.id = endpointCredentials.getId();
        this.applicationId = endpointCredentials.getApplicationId();
        this.endpointId = endpointCredentials.getEndpointId();
        this.publicKey = endpointCredentials.getPublicKey();
        this.serverProfileVersion = endpointCredentials.getServerProfileVersion();
        this.serverProfileBody = endpointCredentials.getServerProfileBody();
        this.serverProfileExpired = endpointCredentials.getServerProfileExpired();
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
    public String getPublicKey() {
        return this.publicKey;
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
    public Boolean getServerProfileExpired() {
        return this.serverProfileExpired;
    }

    @Override
    public EndpointCredentialsDto toDto() {
        EndpointCredentialsDto endpointCredentials = new EndpointCredentialsDto();
        endpointCredentials.setId(this.id);
        endpointCredentials.setApplicationId(this.applicationId);
        endpointCredentials.setEndpointId(this.endpointId);
        endpointCredentials.setPublicKey(this.publicKey);
        endpointCredentials.setServerProfileVersion(this.serverProfileVersion);
        endpointCredentials.setServerProfileBody(this.serverProfileBody);
        endpointCredentials.setServerProfileExpired(this.serverProfileExpired);
        return endpointCredentials;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, MongoEndpointCredentials.EXCLUDE_FIELDS);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other, MongoEndpointCredentials.EXCLUDE_FIELDS);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
