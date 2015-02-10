/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class NotificationMongoDao extends AbstractMongoDao<MongoNotification, String> implements NotificationDao<MongoNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationMongoDao.class);
    private static final String TOPIC_ID = "topic_id";

    @Override
    protected String getCollectionName() {
        return MongoNotification.COLLECTION_NAME;
    }

    @Override
    protected Class<MongoNotification> getDocumentClass() {
        return MongoNotification.class;
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Remove notification by id [{}]", id);
        remove(query(where(ID).is(id)));
    }

    @Override
    public List<MongoNotification> findNotificationsByTopicId(String topicId) {
        LOG.debug("Find notifications by topic id [{}]", topicId);
        return find(query(where(TOPIC_ID).is(topicId)));
    }

    @Override
    public void removeNotificationsByTopicId(String topicId) {
        LOG.debug("Remove notifications by topic id [{}]", topicId);
        remove(query(where(TOPIC_ID).is(topicId)));
    }

    @Override
    public List<MongoNotification> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNumber, int sysNfVersion, int userNfVersion) {
        LOG.debug("Find notifications by topic id [{}], sequence number start [{}], system schema version [{}], user schema version [{}]",
                topicId, seqNumber, sysNfVersion, userNfVersion);
        return find(query(where(TOPIC_ID).is(topicId).and(SEQUENCE_NUMBER).gt(seqNumber)
                .orOperator(where(VERSION).is(sysNfVersion).and(NOTIFICATION_TYPE).is(NotificationTypeDto.SYSTEM),
                        where(VERSION).is(userNfVersion).and(NOTIFICATION_TYPE).is(NotificationTypeDto.USER))));
    }

    @Override
    public MongoNotification save(NotificationDto notification) {
        return save(new MongoNotification(notification));
    }
}
