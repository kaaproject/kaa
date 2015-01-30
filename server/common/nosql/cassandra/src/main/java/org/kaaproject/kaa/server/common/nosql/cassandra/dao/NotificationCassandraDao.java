package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraTopicKeysByTopicIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraTopicLastSeqNumDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraNotification;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraTopicLastSeqNum;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraNfSchemaVersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SCHEMA_VER_USER_TYPE_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SEQ_NUM_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_TOPIC_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraNotification.MAX_BATCH_SIZE;


@Repository(value = "notificationDao")
public class NotificationCassandraDao extends AbstractCassandraDao<CassandraNotification, String> implements NotificationDao<CassandraNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationCassandraDao.class);

    @Autowired
    private CassandraTopicLastSeqNumDao topicLastSeqNumDao;
    @Autowired
    private CassandraTopicKeysByTopicIdDao topicKeysByTopicIdDao;

    @Override
    protected Class<CassandraNotification> getColumnFamilyClass() {
        return CassandraNotification.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.NF_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraNotification findById(String id) {
        LOG.debug("Try to find notification by id {}", id);
        CassandraNotification nf = new CassandraNotification(id);
        Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, nf.getTopicId()))
                .and(eq(NF_SCHEMA_VER_USER_TYPE_NAME, nf.getVersionType()))
                .and(eq(NF_SEQ_NUM_PROPERTY, nf.getSeqNum()));
        LOG.trace("Execute query {}", query);
        nf = findOneByStatement(query);
        LOG.trace("Found notification {} by id {}", nf, id);
        return nf;
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Remove notification by id {}", id);
        CassandraNotification nf = new CassandraNotification(id);
        Delete.Where deleteQuery = delete().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, nf.getTopicId()))
                .and(eq(NF_SCHEMA_VER_USER_TYPE_NAME, nf.getVersionType()))
                .and(eq(NF_SEQ_NUM_PROPERTY, nf.getSeqNum()));
        LOG.trace("Remove notification by id {}", deleteQuery);
        execute(deleteQuery);
    }

    @Override
    public CassandraNotification save(CassandraNotification notification) {
        if (isBlank(notification.getId())) {
            notification.generateId();
        }
        LOG.debug("Save notification {} ", notification);
        Statement topicSeqSaveSt = getSaveQuery(new CassandraTopicLastSeqNum(notification.getTopicId(), notification.getSeqNum()),
                CassandraTopicLastSeqNum.class);
        Statement nfSaveSt = getSaveQuery(notification);
        executeBatch(topicSeqSaveSt, nfSaveSt);
        return notification;
    }

    @Override
    public CassandraNotification save(NotificationDto notification) {
        return save(new CassandraNotification(notification));
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicId(String topicId) {
        LOG.debug("Try to find notifications by topic id {}", topicId);
        List<CassandraNotification> notifications = Collections.emptyList();
        CassandraNfSchemaVersionType[] topicKeyArray = topicKeysByTopicIdDao.getTopicKeysByTopicId(topicId);
        if (topicKeyArray.length > 0) {
            Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                    .and(QueryBuilder.in(NF_SCHEMA_VER_USER_TYPE_NAME, topicKeyArray));
            LOG.trace("Execute query {}", query);
            notifications = findListByStatement(query);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Found notifications {}", Arrays.toString(notifications.toArray()));
            }
        }
        return notifications;
    }

    @Override
    public void removeNotificationsByTopicId(String topicId) {
        LOG.debug("Remove notifications by topic id {}", topicId);
        CassandraNfSchemaVersionType[] topicKeyArray = topicKeysByTopicIdDao.getTopicKeysByTopicId(topicId);
        if (topicKeyArray.length > 0) {
            Delete.Where query = delete().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                    .and(QueryBuilder.in(NF_SCHEMA_VER_USER_TYPE_NAME, topicKeyArray));
            execute(query);
            LOG.trace("Execute query {}", query);
        }
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion) {
        int maxSeqNum = topicLastSeqNumDao.getLastTopicSequenceNumber(topicId);
        int startBatchNum = seqNum / MAX_BATCH_SIZE;
        int endBatchNum = maxSeqNum / MAX_BATCH_SIZE;

        CassandraNfSchemaVersionType[] inArray;
        if (startBatchNum >= endBatchNum) {
            inArray = new CassandraNfSchemaVersionType[2];
            generateSchemaVersionArray(inArray, sysNfVersion, userNfVersion, startBatchNum, 0);
        } else {
            int batchCount = endBatchNum - startBatchNum + 1;
            inArray = new CassandraNfSchemaVersionType[batchCount * 2];
            for (int i = 0; i < batchCount * 2; i += 2) {
                generateSchemaVersionArray(inArray, sysNfVersion, userNfVersion, startBatchNum++, i);
            }
        }

        Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(in(NF_SCHEMA_VER_USER_TYPE_NAME, inArray))
                .and(QueryBuilder.gt(NF_SEQ_NUM_PROPERTY, seqNum));
        List<CassandraNotification> resultList = findListByStatement(query);

        LOG.trace("Execute query {}", query);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found notifications {} by topic id {}, seqNum {}, sysVer {}, userVer {} ",
                    Arrays.toString(resultList.toArray()), topicId, seqNum, sysNfVersion, userNfVersion);
        }
        return resultList;
    }

    private void generateSchemaVersionArray(CassandraNfSchemaVersionType[] inArray, int sysNfVersion, int userNfVersion, int batchNumber, int startArrayPos) {
        inArray[startArrayPos] = new CassandraNfSchemaVersionType(NotificationTypeDto.USER, userNfVersion, batchNumber);
        inArray[++startArrayPos] = new CassandraNfSchemaVersionType(NotificationTypeDto.SYSTEM, sysNfVersion, batchNumber);
    }
}
