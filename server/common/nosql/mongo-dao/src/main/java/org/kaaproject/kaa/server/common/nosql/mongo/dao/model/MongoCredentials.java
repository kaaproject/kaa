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

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.*;

@Document(collection = CREDENTIALS)
public class MongoCredentials implements Credentials, Serializable {

    private static final long serialVersionUID = 817998992561126368L;

    @Id
    private String id;
    @Field(CREDENTIALS_APPLICATION_ID)
    private String applicationId;
    @Field(CREDENTIALS_BODY)
    private byte[] credentialsBody;
    @Field(CREDENTIAL_STATUS)
    private CredentialsStatus status;

    public MongoCredentials() {
    }

    public MongoCredentials(String applicationId, CredentialsDto dto) {
        this.id = dto.getId();
        this.applicationId = applicationId;
        this.credentialsBody = Arrays.copyOf(dto.getCredentialsBody(), dto.getCredentialsBody().length);
        this.status = dto.getStatus();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public byte[] getCredentialsBody() {
        return credentialsBody;
    }

    public void setCredentialsBody(byte[] credentialsBody) {
        this.credentialsBody = Arrays.copyOf(credentialsBody, credentialsBody.length);
    }

    @Override
    public CredentialsStatus getStatus() {
        return status;
    }

    public void setStatus(CredentialsStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.applicationId == null) ? 0 : this.applicationId.hashCode());
        result = prime * result + Arrays.hashCode(this.credentialsBody);
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
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
        MongoCredentials other = (MongoCredentials) obj;
        if (this.applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!this.applicationId.equals(other.applicationId)) {
            return false;
        }
        if (!Arrays.equals(this.credentialsBody, other.credentialsBody)) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MongoCredentials [id=");
        builder.append(this.id);
        builder.append(", applicationId=");
        builder.append(this.applicationId);
        builder.append(", credentialsBody=");
        builder.append(Arrays.toString(this.credentialsBody));
        builder.append(", status=");
        builder.append(this.status);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public CredentialsDto toDto() {
        CredentialsDto dto = new CredentialsDto();
        dto.setId(id);
        dto.setCredentialsBody(credentialsBody);
        dto.setStatus(status);
        return dto;
    }
}
