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

import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointRegistrationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointRegistration;
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
public class EndpointRegistrationMongoDao extends AbstractMongoDao<MongoEndpointRegistration, String> implements
        EndpointRegistrationDao<MongoEndpointRegistration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointRegistrationMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoModelConstants.ENDPOINT_REGISTRATION;
    }

    @Override
    protected Class<MongoEndpointRegistration> getDocumentClass() {
        return MongoEndpointRegistration.class;
    }

    @Override
    public MongoEndpointRegistration save(EndpointRegistrationDto endpointRegistration) {
        LOG.debug("Saving [{}]", endpointRegistration.toString());
        return this.save(new MongoEndpointRegistration(endpointRegistration));
    }

    @Override
    public Optional<MongoEndpointRegistration> findByEndpointId(String endpointId) {
        LOG.debug("Searching for endpoint registration by endpoint ID [{}]", endpointId);
        Query query = Query.query(Criteria.where(MongoModelConstants.EP_REGISTRATION_ENDPOINT_ID).is(endpointId));
        return Optional.ofNullable(this.findOne(query));
    }

    @Override
    public Optional<MongoEndpointRegistration> findByCredentialsId(String credentialsId) {
        LOG.debug("Searching for endpoint registration by credentials ID [{}]", credentialsId);
        Query query = Query.query(Criteria.where(MongoModelConstants.EP_REGISTRATION_CREDENTIALS_ID).is(credentialsId));
        return Optional.ofNullable(this.findOne(query));
    }

    @Override
    public void removeByEndpointId(String endpointId) {
        LOG.debug("Removing endpoint registration by endpoint ID [{}]", endpointId);
        Query query = Query.query(Criteria.where(MongoModelConstants.EP_REGISTRATION_ENDPOINT_ID).is(endpointId));
        this.remove(query);
    }
}
