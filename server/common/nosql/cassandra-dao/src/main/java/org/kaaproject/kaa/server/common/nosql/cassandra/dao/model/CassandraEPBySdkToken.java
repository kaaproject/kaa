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

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_SDK_TOKEN_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_SDK_TOKEN_ENDPOINT_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_SDK_TOKEN_SDK_TOKEN_PROPERTY;

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.8.0
 */
@Table(name = EP_BY_SDK_TOKEN_COLUMN_FAMILY_NAME)
public class CassandraEPBySdkToken implements Serializable {

    @Transient
    private static final long serialVersionUID = 3580976363337794171L;

    @PartitionKey
    @Column(name = EP_BY_SDK_TOKEN_SDK_TOKEN_PROPERTY)
    private String sdkToken;

    @Column(name = EP_BY_SDK_TOKEN_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;

    public CassandraEPBySdkToken() {
    }

    public CassandraEPBySdkToken(String sdkToken, ByteBuffer endpointKeyHash) {
        this.sdkToken = sdkToken;
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public void setSdkToken(String sdkToken) {
        this.sdkToken = sdkToken;
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
        result = prime * result + ((sdkToken == null) ? 0 : sdkToken.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof CassandraEPBySdkToken)) {
            return false;
        }

        CassandraEPBySdkToken other = (CassandraEPBySdkToken) o;

        if (endpointKeyHash == null) {
            if (other.endpointKeyHash != null) {
                return false;
            }
        } else if (!endpointKeyHash.equals(other.endpointKeyHash)) {
            return false;
        }

        if (sdkToken == null) {
            if (other.sdkToken != null) {
                return false;
            }
        } else if (!sdkToken.equals(other.sdkToken)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("CassandraEPBySdkToken [sdkToken=");
        builder.append(sdkToken);
        builder.append(", endpointKeyHash=");
        builder.append(endpointKeyHash);
        builder.append("]");

        return builder.toString();
    }
}
