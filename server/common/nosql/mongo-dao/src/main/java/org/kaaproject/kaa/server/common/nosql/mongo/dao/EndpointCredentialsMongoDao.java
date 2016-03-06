/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointCredentialsDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointCredentials;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Repository
public class EndpointCredentialsMongoDao extends AbstractMongoDao<MongoEndpointCredentials, String> implements EndpointCredentialsDao<MongoEndpointCredentials> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointCredentialsMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoModelConstants.ENDPOINT_CREDENTIALS;
    }

    @Override
    protected Class<MongoEndpointCredentials> getDocumentClass() {
        return MongoEndpointCredentials.class;
    }

    @Override
    public MongoEndpointCredentials save(EndpointCredentialsDto endpointCredentials) {
        LOG.debug("Saving {}", endpointCredentials.toString());
        return this.save(new MongoEndpointCredentials(endpointCredentials));
    }

    @Override
    public MongoEndpointCredentials findByEndpointId(String endpointId) {
        LOG.debug("Searching for endpoint credentials by endpoint ID [{}]", endpointId);
        Query query = Query.query(Criteria.where(MongoModelConstants.ENDPOINT_CREDENTIALS_ENDPOINT_ID).is(endpointId));
        return this.findOne(query);
    }

    @Override
    public void removeByEndpointId(String endpointId) {
        LOG.debug("Removing endpoint credentials by endpoint ID [{}]", endpointId);
        Query query = Query.query(Criteria.where(MongoModelConstants.ENDPOINT_CREDENTIALS_ENDPOINT_ID).is(endpointId));
        this.remove(query);
    }
}
