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
import org.kaaproject.kaa.server.common.dao.model.EndpointCredentials;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Table(name = CassandraModelConstants.EP_CREDS_BY_APP_ID_COLUMN_FAMILY_NAME)
public class CassandraEPCredentialsByAppID implements Serializable {

    @Transient
    private static final long serialVersionUID = 1000L;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_CREDS_BY_APP_ID_APPLICATION_ID_PROPERTY)
    private String applicationId;

    @ClusteringColumn
    @Column(name = CassandraModelConstants.EP_CREDS_BY_APP_ID_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHashWrapper;

    public static CassandraEPCredentialsByAppID fromEndpointCredentials(EndpointCredentials endpointCredentials) {
        String applicationId = endpointCredentials.getApplicationId();
        ByteBuffer endpointKeyHashWrapper = CassandraDaoUtil.getByteBuffer(endpointCredentials.getEndpointKeyHash());
        return new CassandraEPCredentialsByAppID(applicationId, endpointKeyHashWrapper);
    }

    public CassandraEPCredentialsByAppID() {
    }

    public CassandraEPCredentialsByAppID(String applicationId, ByteBuffer endpointKeyHashWrapper) {
        this.applicationId = applicationId;
        this.endpointKeyHashWrapper = endpointKeyHashWrapper;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public ByteBuffer getEndpointKeyHashWrapper() {
        return this.endpointKeyHashWrapper;
    }

    public void setEndpointKeyHashWrapper(ByteBuffer endpointKeyHashWrapper) {
        this.endpointKeyHashWrapper = endpointKeyHashWrapper;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
