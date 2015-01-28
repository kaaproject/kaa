package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Table(name = CassandraModelConstants.EP_KEY_HASH_BY_APP_COLUMN_FAMILY_NAME)
public class CassandraEPKeyHashByAppId implements Serializable {

    @Transient
    private static final long serialVersionUID = -674348892947613949L;

    @PartitionKey
    @Column(name = CassandraModelConstants.EP_KEY_HASH_BY_APP_APPLICATION_ID_PROPERTY)
    private String appId;
    @ClusteringColumn
    @Column(name = CassandraModelConstants.EP_KEY_HASH_BY_APP_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer epKeyHash;

    public CassandraEPKeyHashByAppId() {
    }

    public CassandraEPKeyHashByAppId(String appId, ByteBuffer epKeyHash) {
        this.appId = appId;
        this.epKeyHash = epKeyHash;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public ByteBuffer getEpKeyHash() {
        return epKeyHash;
    }

    public void setEpKeyHash(ByteBuffer epKeyHash) {
        this.epKeyHash = epKeyHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraEPKeyHashByAppId that = (CassandraEPKeyHashByAppId) o;

        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (epKeyHash != null ? !epKeyHash.equals(that.epKeyHash) : that.epKeyHash != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appId != null ? appId.hashCode() : 0;
        result = 31 * result + (epKeyHash != null ? epKeyHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEPKeyHashByAppId{" +
                "appId='" + appId + '\'' +
                ", epKeyHash=" + epKeyHash +
                '}';
    }
}
