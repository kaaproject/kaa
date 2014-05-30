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

import static org.kaaproject.kaa.common.dto.UpdateStatus.ACTIVE;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.stringListToObjectIdList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.kaaproject.kaa.server.common.dao.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.ConfigurationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationSchemaMongoDao extends AbstractMongoDao<ConfigurationSchema> implements ConfigurationSchemaDao<ConfigurationSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationSchemaMongoDao.class);

    private static final String SCHEMA = "schema";
    private static final String PROTOCOL_SCHEMA = "protocol_schema";

    @Override
    protected String getCollectionName() {
        return ConfigurationSchema.COLLECTION_NAME;
    }

    @Override
    protected Class<ConfigurationSchema> getDocumentClass() {
        return ConfigurationSchema.class;
    }

    @Override
    public List<ConfigurationSchema> findByApplicationId(String applicationId) {
        LOG.debug("Find configuration schemas by application id [{}] ", applicationId);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Direction.ASC, MAJOR_VERSION));
        orders.add(new Sort.Order(Direction.ASC, MINOR_VERSION));
        Sort sort = new Sort(orders);
        Query query = query(where(APPLICATION_ID).is(new ObjectId(applicationId))).with(sort);
        query.fields().exclude(SCHEMA).exclude(PROTOCOL_SCHEMA);
        return find(query);
    }

    @Override
    public ConfigurationSchema findByAppIdAndVersion(String applicationId, int version) {
        LOG.debug("Find configurations by application id [{}] ", applicationId);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(applicationId))
                .and(MAJOR_VERSION).is(version)));
    }

    @Override
    public ConfigurationSchema findLatestByApplicationId(String applicationId) {
        LOG.debug("Find configuration schemas by application id [{}] ", applicationId);
        List<Order> orders = new ArrayList<>();
        orders.add(new Order(Direction.DESC, MAJOR_VERSION));
        orders.add(new Order(Direction.DESC, MINOR_VERSION));
        Sort sort = new Sort(orders);
        return findOne(query(where(APPLICATION_ID).is(new ObjectId(applicationId))
                .and(STATUS).is(ACTIVE.name())).with(sort));
    }

    @Override
    public void removeByAppId(String applicationId) {
        LOG.debug("Remove configuration schemas by application id [{}] ", applicationId);
        remove(query(where(APPLICATION_ID).is(new ObjectId(applicationId))));
    }

    @Override
    public List<ConfigurationSchema> findVacantSchemas(String applicationId, List<String> usedSchemaIds) {
        LOG.debug("Find vacant schemas, application id [{}] ", applicationId);
        return find(query(where(APPLICATION_ID).is(new ObjectId(applicationId)).and(ID).nin(stringListToObjectIdList(usedSchemaIds))));
    }

}
