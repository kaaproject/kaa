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

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(name = CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_COLUMN_FAMILY_NAME)
public class CassandraEPByEndpointGroupId implements Serializable {

    @Transient
    private static final long serialVersionUID = 4892433114353644609L;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY)
    private String epGroupId;
    @ClusteringColumn
    @Column(name = CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;

    public CassandraEPByEndpointGroupId() {
    }

    public CassandraEPByEndpointGroupId(String epGroupId, ByteBuffer endpointKeyHash) {
        this.epGroupId = epGroupId;
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getEpGroupId() {
        return epGroupId;
    }

    public void setEpGroupId(String epGroupId) {
        this.epGroupId = epGroupId;
    }

    public ByteBuffer getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointKeyHash == null) ? 0 : endpointKeyHash.hashCode());
        result = prime * result + ((epGroupId == null) ? 0 : epGroupId.hashCode());
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
        CassandraEPByEndpointGroupId other = (CassandraEPByEndpointGroupId) obj;
        if (endpointKeyHash == null) {
            if (other.endpointKeyHash != null) {
                return false;
            }
        } else if (!endpointKeyHash.equals(other.endpointKeyHash)) {
            return false;
        }
        if (epGroupId == null) {
            if (other.epGroupId != null) {
                return false;
            }
        } else if (!epGroupId.equals(other.epGroupId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CassandraEPByAccessToken{" +
                "epGroupId='" + epGroupId + '\'' +
                ", endpointKeyHash=" + endpointKeyHash +
                '}';
    }
}
