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

import java.nio.ByteBuffer;
import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.impl.CredentialsDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoCredentials;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @author Artur Joshi
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Repository
public class CredentialsMongoDao extends AbstractMongoDao<MongoCredentials, ByteBuffer> implements CredentialsDao<MongoCredentials> {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoModelConstants.CREDENTIALS;
    }

    @Override
    protected Class<MongoCredentials> getDocumentClass() {
        return MongoCredentials.class;
    }

    @Override
    public MongoCredentials save(String applicationId, CredentialsDto credentials) {
        LOG.debug("Saving credentials [{}] for application [{}]", credentials.toString(), applicationId);
        return this.save(new MongoCredentials(applicationId, credentials));
    }

    @Override
    public Optional<MongoCredentials> find(String applicationId, String credentialsId) {
        LOG.debug("Searching for credentials by application ID [{}] and credentials ID [{}]", applicationId, credentialsId);
        Query query = Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(credentialsId)
                .and(MongoModelConstants.APPLICATION_ID).is(applicationId));
        return Optional.ofNullable(this.findOne(query));
    }

    @Override
    public Optional<MongoCredentials> updateStatus(String applicationId, String credentialsId, CredentialsStatus status) {
        LOG.debug("Settings status [{}] for credentials [{}] in application [{}]", status, credentialsId, applicationId);
        updateFirst(
                Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(credentialsId).and(MongoModelConstants.APPLICATION_ID).is(applicationId)),
                Update.update(MongoModelConstants.CREDENTIAL_STATUS, status));
        return this.find(applicationId, credentialsId);
    }

    @Override
    public void remove(String applicationId, String credentialsId) {
        LOG.debug("Removing credentials [{}] from application [{}]", credentialsId, applicationId);
        Query query = Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(credentialsId)
                .and(MongoModelConstants.APPLICATION_ID).is(applicationId));
        this.remove(query);
    }
}
