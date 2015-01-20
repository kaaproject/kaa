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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.MAJOR_VERSION_PROPERTY;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ConfigurationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateConfigurationSchemaDao extends HibernateAbstractDao<ConfigurationSchema> implements ConfigurationSchemaDao<ConfigurationSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationSchemaDao.class);

    @Override
    public List<ConfigurationSchema> findByApplicationId(String applicationId) {
        List<ConfigurationSchema> schemas = null;
        LOG.debug("Find configuration schemas by application id {} ", applicationId);
        if (isNotBlank(applicationId)) {
            schemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
        }
        return schemas;
    }

    @Override
    public ConfigurationSchema findLatestByApplicationId(String appId) {
        ConfigurationSchema latestSchema = null;
        LOG.debug("Find latest configuration schema by application id {} ", appId);
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria().createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS)
                    .add(Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)))
                    .addOrder(Order.desc(MAJOR_VERSION_PROPERTY))
                    .setMaxResults(FIRST);
            latestSchema = findOneByCriteria(criteria);
        }
        return latestSchema;
    }

    @Override
    public ConfigurationSchema findByAppIdAndVersion(String appId, int version) {
        ConfigurationSchema schema = null;
        LOG.debug("Find configuration schema by application id {} and major version {}", appId, version);
        if (isNotBlank(appId)) {
            schema = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.eq(MAJOR_VERSION_PROPERTY, version)));
        }
        LOG.debug("Found configuration {} by application id {} and major version {}", schema, appId, version);
        return schema;
    }

    @Override
    public List<ConfigurationSchema> findVacantSchemas(String appId, List<String> usedSchemaIds) {
        LOG.debug("Find vacant schemas, application id [{}] ", appId);
        List<ConfigurationSchema> schemas = Collections.emptyList();
        if (isNotBlank(appId)) {
            List<Long> lids = toLongIds(usedSchemaIds);
            if (lids.isEmpty()) {
                schemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                        Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
            } else {
                schemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                        Restrictions.and(
                                Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                                Restrictions.not(Restrictions.in(ID_PROPERTY, lids))));
            }
        }
        return schemas;
    }

    @Override
    protected Class<ConfigurationSchema> getEntityClass() {
        return ConfigurationSchema.class;
    }
}
