package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.io.Serializable;

@Table(name = CassandraModelConstants.NF_BY_APP_COLUMN_FAMILY_NAME)
public class CassandraNfsByAppId implements Serializable {

    @Transient
    private static final long serialVersionUID = -5193245010946339876L;

    @PartitionKey
    @Column(name = CassandraModelConstants.NF_BY_APP_APPLICATION_ID_PROPERTY)
    private String appId;
    @ClusteringColumn
    @Column(name = CassandraModelConstants.NF_BY_APP_NOTIFICATION_ID_PROPERTY)
    private String notificationId;

    public CassandraNfsByAppId() {
    }

    public CassandraNfsByAppId(String appId, String notificationId) {
        this.appId = appId;
        this.notificationId = notificationId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraNfsByAppId that = (CassandraNfsByAppId) o;

        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (notificationId != null ? !notificationId.equals(that.notificationId) : that.notificationId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appId != null ? appId.hashCode() : 0;
        result = 31 * result + (notificationId != null ? notificationId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEPNfsByAppId{" +
                "appId='" + appId + '\'' +
                ", notificationId='" + notificationId + '\'' +
                '}';
    }
}
