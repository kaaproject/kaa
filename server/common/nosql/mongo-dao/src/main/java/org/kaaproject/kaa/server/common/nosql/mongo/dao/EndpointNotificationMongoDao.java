/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_NOTIFICATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_ENDPOINT_KEY_HASH;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Repository
public class EndpointNotificationMongoDao extends AbstractMongoDao<MongoEndpointNotification, String> implements EndpointNotificationDao<MongoEndpointNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointNotificationMongoDao.class);

    public static final String EP_NF_APPLICATION_ID = "notification.application_id";

    @Override
    protected String getCollectionName() {
        return ENDPOINT_NOTIFICATION;
    }

    @Override
    protected Class<MongoEndpointNotification> getDocumentClass() {
        return MongoEndpointNotification.class;
    }

    // These methods use mongo template directly because we had problems with bytes array.
    @Override
    public List<MongoEndpointNotification> findNotificationsByKeyHash(final byte[] keyHash) {
        LOG.debug("Find unicast notifications by endpoint key hash [{}] ", keyHash);
        DBObject dbObject = query(where(EP_ENDPOINT_KEY_HASH).is(keyHash)).getQueryObject();
        DBCursor cursor = mongoTemplate.getDb().getCollection(getCollectionName()).find(dbObject);
        List<MongoEndpointNotification> endpointNotifications = new ArrayList<>();
        while (cursor.hasNext()) {
            endpointNotifications.add(mongoTemplate.getConverter().read(MongoEndpointNotification.class, cursor.next()));
        }
        return endpointNotifications;
    }

    @Override
    public void removeNotificationsByKeyHash(final byte[] keyHash) {
        LOG.debug("Remove unicast notifications by endpoint key hash [{}] ", keyHash);
        mongoTemplate.remove(query(where(EP_ENDPOINT_KEY_HASH).is(keyHash)), getCollectionName());
    }

    @Override
    public void removeNotificationsByAppId(final String appId) {
        LOG.debug("Remove unicast notifications by application id [{}] ", appId);
        remove(query(where(EP_NF_APPLICATION_ID).is(appId)));
    }

    @Override
    public MongoEndpointNotification save(EndpointNotificationDto dto) {
        return save(new MongoEndpointNotification(dto));
    }
}
