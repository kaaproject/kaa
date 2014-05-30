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

package org.kaaproject.kaa.server.common.dao.mongo;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.stringListToObjectIdList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.TopicDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class TopicMongoDao extends AbstractMongoDao<Topic> implements TopicDao<Topic> {

    private static final Logger LOG = LoggerFactory.getLogger(TopicMongoDao.class);

    private static final String TOPIC_TYPE = "topic_type";

    @Override
    protected String getCollectionName() {
        return Topic.COLLECTION_NAME;
    }

    @Override
    protected Class<Topic> getDocumentClass() {
        return Topic.class;
    }

    @Override
    public List<Topic> findTopicsByAppId(String appId) {
        LOG.debug("Find topics by application id [{}]", appId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId))));
    }

    @Override
    public List<Topic> findTopicsByAppIdAndType(String appId, TopicTypeDto type) {
        LOG.debug("Find topics by application id [{}]", appId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(TOPIC_TYPE).is(type.name())));
    }

    @Override
    public List<Topic> findTopicsByIds(List<String> ids) {
        LOG.debug("Find topics by ids");
        return find(query(where(ID).in(stringListToObjectIdList(ids))));
    }

    @Override
    public List<Topic> findVacantTopicsByAppId(String appId, List<String> excludeIds) {
        LOG.debug("Find vacant topics by application id [{}]", appId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(appId)).and(ID).nin(stringListToObjectIdList(excludeIds))));
    }

    @Override
    public void removeTopicsByAppId(String appId) {
        LOG.debug("Remove topics by application id [{}]", appId);
        remove(query(where(APPLICATION_ID).is(new ObjectId(appId))));
    }

    @Override
    public Topic updateSeqNumber(String id) {
        LOG.debug("Increment seq_num for topic found by id [{}] ", id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE)
                .set(UPDATE_PROCESSING_STATUS, ProcessingStatus.IDLE.name());
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }

    @Override
    public Topic getNextSeqNumber(String id) {
        LOG.debug("Increment update.seq_num for topic found by id [{}] ", id);
        Update update = new Update().inc(UPDATE_SEQUENCE_NUMBER, ONE);
        update.set(UPDATE_PROCESSING_STATUS, ProcessingStatus.PENDING.name());
        return findAndModify(query(where(ID).is(new ObjectId(id))
                .and(UPDATE_PROCESSING_STATUS).is(ProcessingStatus.IDLE.name())), update, returnNew);
    }

    @Override
    public Topic forceNextSeqNumber(String id) {
        LOG.debug("Force increment update.seq_num for topic found by id [{}] ", id);
        Update update = new Update().set(UPDATE_PROCESSING_STATUS, ProcessingStatus.PENDING.name());
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }

}
