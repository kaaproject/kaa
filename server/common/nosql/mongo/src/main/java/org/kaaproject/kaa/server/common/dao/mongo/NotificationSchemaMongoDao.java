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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationSchemaDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.MongoNotificationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationSchemaMongoDao extends AbstractMongoDao<MongoNotificationSchema> implements NotificationSchemaDao<MongoNotificationSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationSchemaMongoDao.class);

    @Override
    protected String getCollectionName() {
        return MongoNotificationSchema.COLLECTION_NAME;
    }

    @Override
    protected Class<MongoNotificationSchema> getDocumentClass() {
        return MongoNotificationSchema.class;
    }

    @Override
    public List<MongoNotificationSchema> findNotificationSchemasByAppId(String appId) {
        LOG.debug("Find notification schemas by application id [{}]", appId);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Direction.ASC, MAJOR_VERSION));
        orders.add(new Sort.Order(Direction.ASC, MINOR_VERSION));
        Sort sort = new Sort(orders);
        return find(query(where(APPLICATION_ID).is(appId)).with(sort));
    }

    @Override
    public void removeNotificationSchemasByAppId(String appId) {
        LOG.debug("Remove notification schemas by application id [{}]", appId);
        remove(query(where(APPLICATION_ID).is(appId)));
    }

    @Override
    public List<MongoNotificationSchema> findNotificationSchemasByAppIdAndType(String appId, NotificationTypeDto type) {
        LOG.debug("Find notification schemas by application id [{}] and type [{}]", appId, type);
        return find(query(where(APPLICATION_ID).is(appId).and(NOTIFICATION_TYPE).is(type.name())));
    }

    @Override
    public MongoNotificationSchema findNotificationSchemasByAppIdAndTypeAndVersion(String appId, NotificationTypeDto type, int majorVersion) {
        LOG.debug("Find notification schema by application id [{}] type [{}] and major version", appId, type, majorVersion);
        return findOne(query(where(APPLICATION_ID).is(appId).and(NOTIFICATION_TYPE).is(type.name()).and(MAJOR_VERSION).is(majorVersion)));
    }

    @Override
    public MongoNotificationSchema findLatestNotificationSchemaByAppId(String applicationId, NotificationTypeDto type) {
        LOG.debug("Find latest notification schema  by application id [{}] and type [{}] ", applicationId, type);
        Sort sort = new Sort(Direction.DESC, MAJOR_VERSION);
        return findOne(query(where(APPLICATION_ID).is(applicationId).and(NOTIFICATION_TYPE).is(type.name())).with(sort));
    }

    @Override
    public MongoNotificationSchema save(NotificationSchemaDto dto) {
        return save(new MongoNotificationSchema(dto));
    }

}
