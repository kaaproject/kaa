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
import org.kaaproject.kaa.server.common.dao.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class EndpointConfigurationMongoDao extends AbstractMongoDao<EndpointConfiguration> implements EndpointConfigurationDao<EndpointConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfigurationMongoDao.class);

    private static final String CONFIGURATION_HASH = "configuration_hash";

    @Override
    protected String getCollectionName() {
        return EndpointConfiguration.COLLECTION_NAME;
    }

    @Override
    protected Class<EndpointConfiguration> getDocumentClass() {
        return EndpointConfiguration.class;
    }

    // These methods use mongo template directly because we had problems with bytes array.
    @Override
    public EndpointConfiguration findByHash(final byte[] hash) {
        LOG.debug("Find endpoint configuration by hash [{}] ", hash);
        DBObject dbObject = query(where(CONFIGURATION_HASH).is(hash)).getQueryObject();
        DBObject result = mongoTemplate.getDb().getCollection(EndpointConfiguration.COLLECTION_NAME).findOne(dbObject);
        return mongoTemplate.getConverter().read(getDocumentClass(), result);
    }

    @Override
    public void removeByHash(final byte[] hash) {
        LOG.debug("Remove endpoint configuration by hash [{}] ", hash);
        mongoTemplate.remove(query(where(CONFIGURATION_HASH).is(hash)), getCollectionName());
    }
}
