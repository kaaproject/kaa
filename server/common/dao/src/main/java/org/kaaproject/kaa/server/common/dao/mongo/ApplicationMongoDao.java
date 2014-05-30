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

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationMongoDao extends AbstractMongoDao<Application> implements ApplicationDao<Application> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationMongoDao.class);

    private static final String APPLICATION_TOKEN = "application_token";


    @Override
    protected String getCollectionName() {
        return Application.COLLECTION_NAME;
    }

    @Override
    protected Class<Application> getDocumentClass() {
        return Application.class;
    }

    @Override
    public List<Application> findByTenantId(String tenantId) {
        LOG.debug("Find applications by tenant id [{}] ", tenantId);
        return find(query(where(TENANT_ID).is(new ObjectId(tenantId))));
    }

    @Override
    public void removeByTenantId(String tenantId) {
        LOG.debug("Remove applications by tenant id [{}] ", tenantId);
        remove(query(where(TENANT_ID).is(new ObjectId(tenantId))));
    }

    @Override
    public Application findByApplicationToken(String token) {
        LOG.debug("Find application by application token [{}] ", token);
        return findOne(query(where(APPLICATION_TOKEN).is(token)));
    }

    @Override
    public void removeByApplicationToken(String applicationToken) {
        LOG.debug("Remove application by application token [{}] ", applicationToken);
        remove(query(where(APPLICATION_TOKEN).is(applicationToken)));
    }

    @Override
    public Application updateSeqNumber(String id) {
        LOG.debug("Increment seq_num for application found by id [{}] ", id);
        Update update = new Update().inc(SEQUENCE_NUMBER, ONE)
                .set(UPDATE_PROCESSING_STATUS, ProcessingStatus.IDLE.name());
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }

    @Override
    public Application getNextSeqNumber(String id) {
        LOG.debug("Increment update.seq_num for application found by id [{}] ", id);
        Update update = new Update().inc(UPDATE_SEQUENCE_NUMBER, ONE);
        update.set(UPDATE_PROCESSING_STATUS, ProcessingStatus.PENDING.name());
        return findAndModify(query(where(ID).is(new ObjectId(id))
                .and(UPDATE_PROCESSING_STATUS).is(ProcessingStatus.IDLE.name())), update, returnNew);
    }

    @Override
    public Application forceNextSeqNumber(String id) {
        LOG.debug("Increment update.seq_num for application found by id [{}] ", id);
        Update update = new Update().set(UPDATE_PROCESSING_STATUS, ProcessingStatus.PENDING.name());
        return findAndModify(query(where(ID).is(new ObjectId(id))), update, returnNew);
    }
}
