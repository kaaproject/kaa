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

package org.kaaproject.kaa.server.common.dao.impl.mongo;

import java.util.List;

import org.kaaproject.kaa.server.common.dao.impl.LogEventDao;
import org.kaaproject.kaa.server.common.dao.model.mongo.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LogEventMongoDao implements LogEventDao<LogEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventMongoDao.class);

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected Class<LogEvent> getDocumentClass() {
        return LogEvent.class;
    }

    @Override
    public void createCollection(String collectionName) {
        try{
            if(!mongoTemplate.collectionExists(collectionName)){
                mongoTemplate.createCollection(collectionName);
            }
        }catch(UncategorizedMongoDbException e){
            LOG.warn("Failed to create collection {} due to", collectionName, e.getMessage());
        }
    }

    @Override
    public List<LogEvent> save(List<LogEvent> dtos, String collectionName) {
        mongoTemplate.insert(dtos, collectionName);
        return dtos;
    }

    @Override
    public void removeAll(String collectionName) {
        LOG.debug("Remove all documents from [{}] collection.", collectionName);
        mongoTemplate.dropCollection(collectionName);
    }

}