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
package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.NotificationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.MAJOR_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.NOTIFICATION_SCHEMA_TYPE_PROPERTY;

@Repository
public class HibernateNotificationSchemaDao extends HibernateAbstractDao<NotificationSchema> implements NotificationSchemaDao<NotificationSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateNotificationSchemaDao.class);

    @Override
    protected Class<NotificationSchema> getEntityClass() {
        return NotificationSchema.class;
    }

    @Override
    public List<NotificationSchema> findNotificationSchemasByAppId(String appId) {
        LOG.debug("Find notification schemas by application id {} ", appId);
        List<NotificationSchema> schemas = Collections.emptyList();
        if (isNotBlank(appId)) {
            schemas = findListByCriterion(Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        return schemas;
    }

    @Override
    public void removeNotificationSchemasByAppId(String appId) {
        LOG.debug("Remove notification schemas by application id {} ", appId);
        removeList(findNotificationSchemasByAppId(appId));
    }

    @Override
    public List<NotificationSchema> findNotificationSchemasByAppIdAndType(String appId, NotificationTypeDto type) {
        LOG.debug("Find notification schema by application id {} type {}", appId, type);
        List<NotificationSchema> schemas = Collections.emptyList();
        if (isNotBlank(appId)) {
            schemas = findListByCriterion(Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(NOTIFICATION_SCHEMA_TYPE_PROPERTY, type)));
        }
        return schemas;
    }

    @Override
    public NotificationSchema findNotificationSchemasByAppIdAndTypeAndVersion(String appId, NotificationTypeDto type, int majorVersion) {
        LOG.debug("Find notification schema by application id {} type {} version {}", appId, type, majorVersion);
        NotificationSchema schema = null;
        if (isNotBlank(appId)) {
            schema = findOneByCriterion(Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(NOTIFICATION_SCHEMA_TYPE_PROPERTY, type),
                    Restrictions.eq(MAJOR_VERSION_PROPERTY, majorVersion)));
        }
        return schema;
    }

    @Override
    public NotificationSchema findLatestNotificationSchemaByAppId(String appId, NotificationTypeDto type) {
        LOG.debug("Find latest notification schema by application id {} type {}", appId, type);
        NotificationSchema latestSchema = null;
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria().add(Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(NOTIFICATION_SCHEMA_TYPE_PROPERTY, type)
            )).addOrder(Order.desc(MAJOR_VERSION_PROPERTY)).setMaxResults(FIRST);
            latestSchema = findOneByCriteria(criteria);
        }
        return latestSchema;
    }
}
