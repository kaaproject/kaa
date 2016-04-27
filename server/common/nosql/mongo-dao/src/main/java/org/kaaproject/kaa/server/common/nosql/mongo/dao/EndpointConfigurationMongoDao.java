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

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointConfiguration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.mongodb.DBObject;

@Repository
public class EndpointConfigurationMongoDao extends AbstractMongoDao<MongoEndpointConfiguration, ByteBuffer> implements EndpointConfigurationDao<MongoEndpointConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfigurationMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoModelConstants.ENDPOINT_CONFIGURATION;
    }

    @Override
    protected Class<MongoEndpointConfiguration> getDocumentClass() {
        return MongoEndpointConfiguration.class;
    }

    // These methods use mongo template directly because we had problems with bytes array.
    @Override
    public MongoEndpointConfiguration findByHash(final byte[] hash) {
        LOG.debug("Find endpoint configuration by hash [{}] ", hash);
        DBObject dbObject = query(where(ID).is(hash)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(getCollectionName()).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public void removeByHash(final byte[] hash) {
        LOG.debug("Remove endpoint configuration by hash [{}] ", hash);
        remove(query(where(ID).is(hash)));
    }

    @Override
    public MongoEndpointConfiguration findById(ByteBuffer key) {
        MongoEndpointConfiguration mongoEndpointConfiguration = null;
        if (key != null) {
            mongoEndpointConfiguration = findByHash(key.array());
        }
        return mongoEndpointConfiguration;
    }

    @Override
    public void removeById(ByteBuffer key) {
        if (key != null) {
            removeByHash(key.array());
        }
    }

    @Override
    public MongoEndpointConfiguration save(EndpointConfigurationDto dto) {
        return save(new MongoEndpointConfiguration(dto));
    }
}
