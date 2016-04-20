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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.server.common.dao.impl.TopicListEntryDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoTopicListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.mongodb.DBObject;

@Repository
public class TopicListEntryMongoDao extends AbstractMongoDao<MongoTopicListEntry, ByteBuffer> implements TopicListEntryDao<MongoTopicListEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(TopicListEntryMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoModelConstants.TOPIC_LIST_ENTRY;
    }

    @Override
    protected Class<MongoTopicListEntry> getDocumentClass() {
        return MongoTopicListEntry.class;
    }

    @Override
    public MongoTopicListEntry save(TopicListEntryDto dto) {
        return save(new MongoTopicListEntry(dto));
    }

    @Override
    public MongoTopicListEntry findByHash(byte[] hash) {
        LOG.debug("Find topic list entry by hash [{}] ", hash);
        DBObject dbObject = query(where(ID).is(hash)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public void removeByHash(byte[] hash) {
        LOG.debug("Remove  topic list entry by hash [{}] ", hash);
        remove(query(where(ID).is(hash)));
    }
}
