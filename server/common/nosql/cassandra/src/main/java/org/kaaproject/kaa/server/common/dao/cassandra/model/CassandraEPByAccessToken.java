package org.kaaproject.kaa.server.common.dao.cassandra.model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_ACCESS_TOKEN_ACCESS_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_ACCESS_TOKEN_ENDPOINT_KEY_HASH_PROPERTY;

@Table(name = EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME)
public class CassandraEPByAccessToken implements Serializable {

    @Transient
    private static final long serialVersionUID = -8826203709978813176L;

    @PartitionKey
    @Column(name = EP_BY_ACCESS_TOKEN_ACCESS_TOKEN_PROPERTY)
    private String accessToken;
    @Column(name = EP_BY_ACCESS_TOKEN_ENDPOINT_KEY_HASH_PROPERTY)
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraEPByAccessToken that = (CassandraEPByAccessToken) o;

        if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) return false;
        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null)
            return false;

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
