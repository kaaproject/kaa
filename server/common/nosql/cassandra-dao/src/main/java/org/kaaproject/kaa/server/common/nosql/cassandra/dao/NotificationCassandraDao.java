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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_NOTIFICATION_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SEQ_NUM_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_TOPIC_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_VERSION_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;

@Repository
public class NotificationCassandraDao extends AbstractCassandraDao<CassandraNotification, String> implements NotificationDao<CassandraNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationCassandraDao.class);

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
        LOG.debug("Try to find notification by id {}", id);
        CassandraNotification nf = new CassandraNotification(id);
        Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, nf.getTopicId()))
                .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, nf.getType().name()))
                .and(eq(NF_VERSION_PROPERTY, nf.getNfVersion()))
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
                .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, nf.getType().name()))
                .and(eq(NF_VERSION_PROPERTY, nf.getNfVersion()))
                .and(eq(NF_SEQ_NUM_PROPERTY, nf.getSeqNum()));
        LOG.trace("Remove notification by id {}", deleteQuery);
        execute(deleteQuery);
    }

    @Override
    public CassandraNotification save(CassandraNotification notification) {
        LOG.debug("Save notification {} ", notification);
        if (isBlank(notification.getId())) {
            notification.generateId();
        }
        return super.save(notification);
    }

    @Override
    public CassandraNotification save(NotificationDto notification) {
        return save(new CassandraNotification(notification));
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicId(String topicId) {
        LOG.debug("Try to find notifications by topic id {}", topicId);
        Where query = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(QueryBuilder.in(NF_NOTIFICATION_TYPE_PROPERTY, getStringTypes(NotificationTypeDto.values())));
        LOG.trace("Execute query {}", query);
        List<CassandraNotification> notifications = findListByStatement(query);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found notifications {}", Arrays.toString(notifications.toArray()));
        }
        return notifications;
    }

    @Override
    public void removeNotificationsByTopicId(String topicId) {
        LOG.debug("Remove notifications by topic id {}", topicId);
        Delete.Where query = delete().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(QueryBuilder.in(NF_NOTIFICATION_TYPE_PROPERTY, getStringTypes(NotificationTypeDto.values())));
        execute(query);
        LOG.trace("Execute query {}", query);
    }

    @Override
    public List<CassandraNotification> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion) {
        LOG.debug("Try to find notifications by topic id {} start sequence number {} system schema version {} user schema version {}", topicId, seqNum, sysNfVersion, userNfVersion);
        List<CassandraNotification> resultList = new ArrayList<>();
        Where systemQuery = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, NotificationTypeDto.SYSTEM.name()))
                .and(eq(NF_VERSION_PROPERTY, sysNfVersion))
                .and(QueryBuilder.gt(NF_SEQ_NUM_PROPERTY, seqNum));
        Where userQuery = select().from(getColumnFamilyName()).where(eq(NF_TOPIC_ID_PROPERTY, topicId))
                .and(eq(NF_NOTIFICATION_TYPE_PROPERTY, NotificationTypeDto.USER.name()))
                .and(eq(NF_VERSION_PROPERTY, userNfVersion))
                .and(QueryBuilder.gt(NF_SEQ_NUM_PROPERTY, seqNum));
        List<CassandraNotification> systemList = findListByStatement(systemQuery);
        List<CassandraNotification> userList = findListByStatement(userQuery);
        resultList.addAll(systemList);
        resultList.addAll(userList);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found notifications {} by topic id {}, seqNum {}, sysVer {}, userVer {} ",
                    Arrays.toString(resultList.toArray()), topicId, seqNum, sysNfVersion, userNfVersion);
        }
        return resultList;
    }

    private String[] getStringTypes(NotificationTypeDto[] typeArray) {
        String[] types = new String[typeArray.length];
        for (int i = 0; i < typeArray.length; i++) {
            types[i] = typeArray[i].name();
        }
        return types;
    }
}
