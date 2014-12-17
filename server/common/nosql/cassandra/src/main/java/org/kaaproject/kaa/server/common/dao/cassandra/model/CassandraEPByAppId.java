package org.kaaproject.kaa.server.common.dao.cassandra.model;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_ENDPOINT_KEY_HASH_PROPERTY;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Table(name = EP_BY_APP_ID_COLUMN_FAMILY_NAME)
public class CassandraEPByAppId implements Serializable {

    @Transient
    private static final long serialVersionUID = 4620788066149588088L;

    @PartitionKey
    @Column(name = EP_BY_APP_ID_APPLICATION_ID_PROPERTY)
    private String appId;
    @ClusteringColumn
    @Column(name = EP_BY_APP_ID_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;

    public CassandraEPByAppId() {
    }

    public CassandraEPByAppId(String appId, ByteBuffer endpointKeyHash) {
        this.appId = appId;
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

        CassandraEPByAppId that = (CassandraEPByAppId) o;

        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appId != null ? appId.hashCode() : 0;
        result = 31 * result + (endpointKeyHash != null ? endpointKeyHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEPByAppId{" +
                "appId='" + appId + '\'' +
                ", endpointKeyHash=" + endpointKeyHash +
                '}';
    }
}
