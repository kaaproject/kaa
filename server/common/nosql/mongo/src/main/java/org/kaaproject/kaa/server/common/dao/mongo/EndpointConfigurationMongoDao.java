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

import com.mongodb.DBObject;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.MongoEndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class EndpointConfigurationMongoDao extends AbstractMongoDao<MongoEndpointConfiguration> implements EndpointConfigurationDao<MongoEndpointConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfigurationMongoDao.class);

    private static final String CONFIGURATION_HASH = "configuration_hash";

    @Override
    protected String getCollectionName() {
        return MongoEndpointConfiguration.COLLECTION_NAME;
    }

    @Override
    protected Class<MongoEndpointConfiguration> getDocumentClass() {
        return MongoEndpointConfiguration.class;
    }

    // These methods use mongo template directly because we had problems with bytes array.
    @Override
    public MongoEndpointConfiguration findByHash(final byte[] hash) {
        LOG.debug("Find endpoint configuration by hash [{}] ", hash);
        DBObject dbObject = query(where(CONFIGURATION_HASH).is(hash)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(MongoEndpointConfiguration.COLLECTION_NAME).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public void removeByHash(final byte[] hash) {
        LOG.debug("Remove endpoint configuration by hash [{}] ", hash);
        remove(query(where(CONFIGURATION_HASH).is(hash)));
    }

    @Override
    public MongoEndpointConfiguration save(EndpointConfigurationDto dto) {
        return save(new MongoEndpointConfiguration(dto));
    }
}
