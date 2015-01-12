package org.kaaproject.kaa.server.common.dao.cassandra.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_USER_ID_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_USER_ID_ENDPOINT_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_USER_ID_USER_ID_PROPERTY;

@Table(name = EP_BY_USER_ID_COLUMN_FAMILY_NAME)
public class CassandraEPByUserId implements Serializable {

    @Transient
    private static final long serialVersionUID = 2973306661658010374L;

    @PartitionKey
    @Column(name = EP_BY_USER_ID_USER_ID_PROPERTY)
    private String userId;
    @ClusteringColumn
    @Column(name = EP_BY_USER_ID_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;

    public CassandraEPByUserId() {
    }

    public CassandraEPByUserId(String userId, ByteBuffer endpointKeyHash) {
        this.userId = userId;
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

        CassandraEPByUserId that = (CassandraEPByUserId) o;

        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (endpointKeyHash != null ? endpointKeyHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEPByUserId{" +
                "userId='" + userId + '\'' +
                ", endpointKeyHash=" + endpointKeyHash +
                '}';
    }
}
