package org.kaaproject.kaa.server.common.dao.cassandra.model;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.*;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(name = NOTIFICATIONS_BY_APPLICATION_COLUMN_FAMILY_NAME)
public class CassandraNotificationsByApplication {

    @PartitionKey
    @Column(name = NOTIFICATIONS_BY_APPLICATION_APPLICATION_ID_PROPERTY)
    private String appId;
    @Column(name = NOTIFICATIONS_BY_APPLICATION_NOTIFICATION_ID_PROPERTY)
    private String notificationId;

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

        CassandraNotificationsByApplication that = (CassandraNotificationsByApplication) o;

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
        return "CassandraNotificationsByApplication{" +
                "appId='" + appId + '\'' +
                ", notificationId='" + notificationId + '\'' +
                '}';
    }
}
