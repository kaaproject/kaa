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
import org.kaaproject.kaa.server.common.dao.impl.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.MAJOR_VERSION_PROPERTY;

@Repository
public class HibernateProfileSchemaDao extends HibernateAbstractDao<ProfileSchema> implements ProfileSchemaDao<ProfileSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateProfileSchemaDao.class);

    @Override
    public List<ProfileSchema> findByApplicationId(String applicationId) {
        List<ProfileSchema> schemas = null;
        LOG.debug("Find profile schemas by application id {} ", applicationId);
        if (isNotBlank(applicationId)) {
            schemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
        }
        return schemas;
    }

    @Override
    public ProfileSchema findByAppIdAndVersion(String appId, int version) {
        ProfileSchema schema = null;
        LOG.debug("Find profile schema by application id {} and major version {}", appId, version);
        if (isNotBlank(appId)) {
            schema = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(MAJOR_VERSION_PROPERTY, version)));
        }
        return schema;
    }

    @Override
    public ProfileSchema findLatestByAppId(String appId) {
        ProfileSchema latestSchema = null;
        LOG.debug("Find latest profile schema by application id {} ", appId);
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria().createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS)
                    .add(Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)))
                    .addOrder(Order.desc(MAJOR_VERSION_PROPERTY)).setMaxResults(FIRST);
            latestSchema = findOneByCriteria(criteria);
        }
        return latestSchema;
    }

    @Override
    public List<ProfileSchema> findVacantSchemas(String applicationId, List<String> usedSchemaIds) {
        LOG.debug("Find vacant schemas, application id [{}], used schema ids [{}] ", applicationId, usedSchemaIds);
        if (isNotBlank(applicationId)) {
            Criteria criteria = getCriteria().createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS)
                    .add(Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
            if (usedSchemaIds != null && !usedSchemaIds.isEmpty()) {
                criteria.add(Restrictions.not(Restrictions.in(ID_PROPERTY, toLongIds(usedSchemaIds))));
            }
            return findListByCriteria(criteria);
        }
        return Collections.emptyList();
    }

    @Override
    protected Class<ProfileSchema> getEntityClass() {
        return ProfileSchema.class;
    }
}
