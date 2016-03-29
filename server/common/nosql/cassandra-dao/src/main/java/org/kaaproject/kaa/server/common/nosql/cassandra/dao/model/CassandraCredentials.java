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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;

import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.CREDENTIALS_STATUS_PROPERTY;

@Table(name = CREDENTIALS_COLUMN_FAMILY_NAME)
public class CassandraCredentials implements Credentials, Serializable {

    @Transient
    private static final long serialVersionUID = 5814711856025319827L;

    @Column(name = CREDENTIALS_APPLICATION_ID_PROPERTY)
    private String applicationId;
    @PartitionKey
    @Column(name = CREDENTIALS_ID_PROPERTY)
    private String id;
    @Column(name = CREDENTIALS_BODY_PROPERTY)
    private ByteBuffer cassandraCredentialsBody;
    @Column(name = CREDENTIALS_STATUS_PROPERTY)
    private String credentialStatus;

    public CassandraCredentials() {
    }

    public CassandraCredentials(String applicationId, CredentialsDto dto) {
        this.applicationId = applicationId;
        this.id = dto.getId();
        this.cassandraCredentialsBody = getByteBuffer(dto.getCredentialsBody());
        this.credentialStatus = dto.getStatus().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public byte[] getCredentialsBody() {
        return cassandraCredentialsBody.array();
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public ByteBuffer getCassandraCredentialsBody() {
        return cassandraCredentialsBody;
    }

    public void setCassandraCredentialsBody(ByteBuffer cassandraCredentialsBody) {
        this.cassandraCredentialsBody = cassandraCredentialsBody.duplicate();
    }

    @Override
    public CredentialsStatus getStatus() {
        return CredentialsStatus.valueOf(credentialStatus);
    }

    public String getCredentialStatus() {
        return credentialStatus;
    }

    public void setCredentialStatus(String status) {
        this.credentialStatus = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraCredentials that = (CassandraCredentials) o;

        if (!applicationId.equals(that.applicationId)) return false;
        if (!credentialStatus.equals(that.credentialStatus)) return false;
        if (!cassandraCredentialsBody.equals(that.cassandraCredentialsBody)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationId.hashCode();
        result = 31 * result + cassandraCredentialsBody.hashCode();
        result = 31 * result + credentialStatus.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CassandraCredentials{" +
                "applicationId='" + applicationId + '\'' +
                ", id='" + id + '\'' +
                ", cassandraCredentialsBody=" + cassandraCredentialsBody +
                ", credentialStatus='" + credentialStatus + '\'' +
                '}';
    }

    @Override
    public CredentialsDto toDto() {
        CredentialsDto credentialsDto = new CredentialsDto();
        credentialsDto.setId(id);
        credentialsDto.setCredentialsBody(getBytes(cassandraCredentialsBody));
        credentialsDto.setStatus(CredentialsStatus.valueOf(credentialStatus));
        return credentialsDto;
    }
}
