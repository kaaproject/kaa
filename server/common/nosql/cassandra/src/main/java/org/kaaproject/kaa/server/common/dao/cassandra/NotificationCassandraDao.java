package org.kaaproject.kaa.server.common.dao.cassandra;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNotification;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;

import java.util.List;


public class NotificationCassandraDao extends AbstractCassandraDao<CassandraNotification> implements NotificationDao<CassandraNotification> {

    @Override
    protected Class<?> getColumnFamilyClass() {
        return null;
    }

    @Override
    protected String getColumnFamilyName() {
        return null;
    }

    @Override
    public CassandraNotification save(NotificationDto notification) {
        return null;
    }

    @Override
    public List<CassandraNotification> findNotificationsBySchemaId(String schemaId) {
        return null;
    }

    @Override
    public void removeNotificationsBySchemaId(String schemaId) {

    }

    @Override
    public List<CassandraNotification> findNotificationsByAppId(String appId) {
        return null;
    }

    @Override
    public void removeNotificationsByAppId(String appId) {

    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicId(String topicId) {
        return null;
    }

    @Override
    public void removeNotificationsByTopicId(String topicId) {

    }

    @Override
    public List<CassandraNotification> findNotificationsBySchemaIdAndType(String schemaId, NotificationTypeDto type) {
        return null;
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion) {
        return null;
    }
}
