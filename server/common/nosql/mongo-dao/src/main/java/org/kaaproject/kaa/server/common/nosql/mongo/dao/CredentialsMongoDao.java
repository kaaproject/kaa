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

import java.nio.ByteBuffer;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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
    public MongoCredentials save(CredentialsDto credentialsDto) {
        LOG.debug("Saving {}", credentialsDto.toString());
        return this.save(new MongoCredentials(credentialsDto));
    }

    @Override
    public MongoCredentials findById(String id) {
        LOG.debug("Searching credential by ID[{}]", id);
        Query query = Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(id));
        return this.findOne(query);
    }

    @Override
    public MongoCredentials updateStatusById(String id, CredentialsStatus status) {
        LOG.debug("Updating credentials status with ID[{}] to STATUS[{}]", id, status.toString());
        updateFirst(
                query(where(MongoModelConstants.CREDENTIALS_ID).is(id)),
                Update.update(MongoModelConstants.CREDENTIAL_STATUS, status));
        return findById(id);
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Deleting credential by ID[{}]", id);
        Query query = Query.query(Criteria.where(MongoModelConstants.CREDENTIALS_ID).is(id));
        this.remove(query);
    }
}
