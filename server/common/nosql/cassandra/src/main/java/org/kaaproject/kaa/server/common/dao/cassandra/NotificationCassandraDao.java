package org.kaaproject.kaa.server.common.dao.cassandra;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select.Where;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNotification;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.KEY_DELIMITER;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_NOTIFICATION_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_SEQ_NUM_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_TOPIC_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_VERSION_PROPERTY;


@Repository("notificationDao")
public class NotificationCassandraDao extends AbstractCassandraDao<CassandraNotification, String> implements NotificationDao<CassandraNotification> {

    @Override
    protected Class<CassandraNotification> getColumnFamilyClass() {
        return CassandraNotification.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return NF_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraNotification findById(String id) {
        CassandraNotification cassandraNotification = null;
        String[] ids = parseId(id);
        if (ids != null && ids.length == 4) {
            Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, ids[0]))
                    .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, ids[1]))
                    .and(eq(NF_VERSION_PROPERTY, Integer.valueOf(ids[2])))
                    .and(eq(NF_SEQ_NUM_PROPERTY, Integer.valueOf(ids[3])));
            cassandraNotification = findOneByStatement(query);
        }
        return cassandraNotification;
    }

    @Override
    public void removeById(String id) {
        String[] ids = parseId(id);
        if (ids != null && ids.length == 4) {
            Delete.Where deleteQuery = delete().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, ids[0]))
                    .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, ids[1]))
                    .and(eq(NF_VERSION_PROPERTY, Integer.valueOf(ids[2])))
                    .and(eq(NF_SEQ_NUM_PROPERTY, Integer.valueOf(ids[3])));
            execute(deleteQuery);
        }
    }

    @Override
    public CassandraNotification save(CassandraNotification notification) {
        StringBuilder builder = new StringBuilder(notification.getTopicId());
        builder.append(KEY_DELIMITER);
        builder.append(notification.getType().name());
        builder.append(KEY_DELIMITER);
        builder.append(notification.getVersion());
        builder.append(KEY_DELIMITER);
        builder.append(notification.getSeqNum());
        notification.setId(builder.toString());
        return super.save(notification);
    }

    @Override
    public CassandraNotification save(NotificationDto notification) {
        return save(new CassandraNotification(notification));
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicId(String topicId) {
        Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(in(NF_NOTIFICATION_TYPE_PROPERTY, getStringTypes(NotificationTypeDto.values())));
        return findListByStatement(query);
    }

    @Override
    public void removeNotificationsByTopicId(String topicId) {
        Delete.Where query = delete().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(in(NF_NOTIFICATION_TYPE_PROPERTY, getStringTypes(NotificationTypeDto.values())));
        execute(query);
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion) {
        List<CassandraNotification> resultList = new ArrayList<>();
        Where systemQuery = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, NotificationTypeDto.SYSTEM.name()))
                .and(eq(NF_VERSION_PROPERTY, sysNfVersion))
                .and(gt(NF_SEQ_NUM_PROPERTY, seqNum));
        Where userQuery = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, NotificationTypeDto.USER.name()))
                .and(eq(NF_VERSION_PROPERTY, userNfVersion))
                .and(gt(NF_SEQ_NUM_PROPERTY, seqNum));
        List<CassandraNotification> systemList = findListByStatement(systemQuery);
        List<CassandraNotification> userList = findListByStatement(userQuery);
        resultList.addAll(systemList);
        resultList.addAll(userList);
        return resultList;
    }

    private String[] parseId(String id) {
        String[] ids = null;
        if (isNotBlank(id) && id.contains(KEY_DELIMITER)) {
            ids = id.split(KEY_DELIMITER);
        }
        return ids;
    }

    private String[] getStringTypes(NotificationTypeDto[] typeArray) {
        String[] types = new String[typeArray.length];
        for (int i = 0; i < typeArray.length; i++) {
            types[i] = typeArray[i].name();
        }
        return types;
    }
}
