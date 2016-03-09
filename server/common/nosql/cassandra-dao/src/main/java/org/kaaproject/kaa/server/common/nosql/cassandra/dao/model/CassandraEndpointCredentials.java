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

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointCredentials;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Table(name = CassandraModelConstants.EP_CREDS_COLUMN_FAMILY_NAME)
public final class CassandraEndpointCredentials implements EndpointCredentials, Serializable {

    @Transient
    private static final long serialVersionUID = 1000L;

    @Transient
    private static final String[] EXCLUDE_FIELDS = { "id" };

    @Column(name = CassandraModelConstants.EP_CREDS_ID_PROPERTY)
    private String id;

    @Column(name = CassandraModelConstants.EP_CREDS_APPLICATION_ID_PROPERTY)
    private String applicationId;

    @Column(name = CassandraModelConstants.EP_CREDS_ENDPOINT_KEY_PROPERTY)
    private ByteBuffer endpointKeyWrapper;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_CREDS_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHashWrapper;

    @Column(name = CassandraModelConstants.EP_CREDS_SERVER_PROFILE_VERSION_PROPERTY)
    private Integer serverProfileVersion;

    @Column(name = CassandraModelConstants.EP_CREDS_SERVER_PROFILE_BODY_PROPERTY)
    private String serverProfileBody;

    public CassandraEndpointCredentials() {
    }

    public CassandraEndpointCredentials(EndpointCredentialsDto endpointCredentials) {
        this.id = endpointCredentials.getId();
        this.applicationId = endpointCredentials.getApplicationId();
        this.endpointKeyWrapper = CassandraDaoUtil.getByteBuffer(endpointCredentials.getEndpointKey());
        this.endpointKeyHashWrapper = CassandraDaoUtil.getByteBuffer(endpointCredentials.getEndpointKeyHash());
        this.serverProfileVersion = endpointCredentials.getServerProfileVersion();
        this.serverProfileBody = endpointCredentials.getServerProfileBody();
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
    public byte[] getEndpointKey() {
        return CassandraDaoUtil.getBytes(this.endpointKeyWrapper);
    }

    public ByteBuffer getEndpointKeyWrapper() {
        return this.endpointKeyWrapper;
    }

    public void setEndpointKeyWrapper(ByteBuffer endpointKeyWrapper) {
        this.endpointKeyWrapper = endpointKeyWrapper;
    }

    @Override
    public byte[] getEndpointKeyHash() {
        return CassandraDaoUtil.getBytes(this.endpointKeyHashWrapper);
    }

    public ByteBuffer getEndpointKeyHashWrapper() {
        return this.endpointKeyHashWrapper;
    }

    public void setEndpointKeyHashWrapper(ByteBuffer endpointKeyHashWrapper) {
        this.endpointKeyHashWrapper = endpointKeyHashWrapper;
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
    public EndpointCredentialsDto toDto() {
        EndpointCredentialsDto endpointCredentials = new EndpointCredentialsDto();
        endpointCredentials.setId(this.id);
        endpointCredentials.setApplicationId(this.applicationId);
        endpointCredentials.setEndpointKey(this.getEndpointKey());
        endpointCredentials.setEndpointKeyHash(this.getEndpointKeyHash());
        endpointCredentials.setServerProfileVersion(this.serverProfileVersion);
        endpointCredentials.setServerProfileBody(this.serverProfileBody);
        return endpointCredentials;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, CassandraEndpointCredentials.EXCLUDE_FIELDS);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other, CassandraEndpointCredentials.EXCLUDE_FIELDS);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
