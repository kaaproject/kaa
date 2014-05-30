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

import static org.kaaproject.kaa.server.common.dao.DaoUtil.stringListToObjectIdList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.server.common.dao.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileSchemaMongoDao extends AbstractMongoDao<ProfileSchema> implements ProfileSchemaDao<ProfileSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileSchemaMongoDao.class);

    @Override
    protected String getCollectionName() {
        return ProfileSchema.COLLECTION_NAME;
    }

    @Override
    protected Class<ProfileSchema> getDocumentClass() {
        return ProfileSchema.class;
    }

    @Override
    public List<ProfileSchema> findByApplicationId(String applicationId) {
        LOG.debug("Find profile schema  by application id [{}] ", applicationId);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Direction.ASC, MAJOR_VERSION));
        orders.add(new Sort.Order(Direction.ASC, MINOR_VERSION));
        Sort sort = new Sort(orders);
        return find(query(where(APPLICATION_ID).is(new ObjectId(applicationId))).with(sort));
    }

    @Override
    public ProfileSchema findByAppIdAndVersion(String applicationId, int version) {
        LOG.debug("Find profile schema by application id [{}] and version [{}]", applicationId, version);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(applicationId)).and(MAJOR_VERSION).is(version)));
    }

    @Override
    public void removeByApplicationId(String applicationId) {
        LOG.debug("Remove profile schema  by application id [{}] ", applicationId);
        remove(query(where(APPLICATION_ID).is(new ObjectId(applicationId))));
    }

    @Override
    public ProfileSchema findLatestByAppId(String applicationId) {
        LOG.debug("Find latest profile schema  by application id [{}] ", applicationId);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Direction.DESC, MAJOR_VERSION));
        orders.add(new Sort.Order(Direction.DESC, MINOR_VERSION));
        Sort sort = new Sort(orders);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(applicationId))).with(sort));
    }

    @Override
    public List<ProfileSchema> findVacantSchemas(String applicationId, List<String> usedSchemaIds) {
        LOG.debug("Find vacant schemas, application id [{}] ", applicationId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(applicationId)).and(ID).nin(stringListToObjectIdList(usedSchemaIds))));
    }

}
