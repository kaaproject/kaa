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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

@Table(name = CassandraModelConstants.EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME)
public class CassandraEPByAccessToken implements Serializable {

    @Transient
    private static final long serialVersionUID = -8826203709978813176L;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_BY_ACCESS_TOKEN_ACCESS_TOKEN_PROPERTY)
    private String accessToken;
    @Column(name = CassandraModelConstants.EP_BY_ACCESS_TOKEN_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;

    public CassandraEPByAccessToken() {
    }

    public CassandraEPByAccessToken(String accessToken, ByteBuffer endpointKeyHash) {
        this.accessToken = accessToken;
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public ByteBuffer getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CassandraEPByAccessToken that = (CassandraEPByAccessToken) o;

        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) {
            return false;
        }
        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = accessToken != null ? accessToken.hashCode() : 0;
        result = 31 * result + (endpointKeyHash != null ? endpointKeyHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEPByAccessToken{" +
                "accessToken='" + accessToken + '\'' +
                ", endpointKeyHash=" + endpointKeyHash +
                '}';
    }
}
