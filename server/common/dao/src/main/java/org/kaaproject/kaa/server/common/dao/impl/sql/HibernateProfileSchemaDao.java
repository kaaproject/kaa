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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernateProfileSchemaDao extends HibernateAbstractDao<EndpointProfileSchema> implements ProfileSchemaDao<EndpointProfileSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateProfileSchemaDao.class);

    @Override
    public List<EndpointProfileSchema> findByApplicationId(String appId) {
        List<EndpointProfileSchema> schemas = Collections.emptyList();
        LOG.debug("Searching profile schemas by application id [{}] ", appId);
        if (isNotBlank(appId)) {
            schemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, schemas.size());
        }
        return schemas;
    }

    @Override
    public EndpointProfileSchema findByAppIdAndVersion(String appId, int version) {
        LOG.debug("Searching profile schema by application id [{}] and version [{}]", appId, version);
        EndpointProfileSchema schema = null;
        if (isNotBlank(appId)) {
            schema = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(VERSION_PROPERTY, version)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, version, schema);
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, version, schema != null);
        }
        return schema;
    }

    @Override
    public EndpointProfileSchema findLatestByAppId(String appId) {
        EndpointProfileSchema latestSchema = null;
        LOG.debug("Searching latest profile schema by application id [{}] ", appId);
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria().createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS)
                    .add(Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)))
                    .addOrder(Order.desc(VERSION_PROPERTY)).setMaxResults(FIRST);
            latestSchema = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, latestSchema);
        } else {
            LOG.debug("[{}] Search result: {}.", appId, latestSchema != null);
        }
        return latestSchema;
    }

    @Override
    public List<EndpointProfileSchema> findVacantSchemas(String appId, List<String> usedSchemaIds) {
        LOG.debug("Searching vacant schemas by application id [{}] and used schema ids [{}] ", appId, usedSchemaIds);
        List<EndpointProfileSchema> schemas = Collections.emptyList();
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria().createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS)
                    .add(Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
            if (usedSchemaIds != null && !usedSchemaIds.isEmpty()) {
                criteria.add(Restrictions.not(Restrictions.in(ID_PROPERTY, toLongIds(usedSchemaIds))));
            }
            schemas = findListByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, schemas.size());
        }
        return schemas;
    }

    @Override
    protected Class<EndpointProfileSchema> getEntityClass() {
        return EndpointProfileSchema.class;
    }
}
