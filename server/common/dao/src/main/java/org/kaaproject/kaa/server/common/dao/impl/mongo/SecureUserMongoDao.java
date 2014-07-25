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

import org.kaaproject.kaa.server.common.dao.impl.SecureUserDao;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Repository
public class SecureUserMongoDao implements SecureUserDao<SecureUser> {

    private static final Logger LOG = LoggerFactory.getLogger(SecureUserMongoDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public SecureUser saveUser(SecureUser dto) {
        DBObject dbObject = new BasicDBObject();
        mongoTemplate.getConverter().write(dto, dbObject);
        LOG.debug("Save auth user [{}]", dbObject);
        mongoTemplate.getDb().getCollection(SecureUser.COLLECTION_NAME).save(dbObject);
        return dto;
    }

}