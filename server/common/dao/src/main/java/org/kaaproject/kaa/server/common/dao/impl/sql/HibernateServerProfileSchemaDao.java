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


import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ServerProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateServerProfileSchemaDao extends HibernateAbstractDao<ServerProfileSchema> implements ServerProfileSchemaDao<ServerProfileSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateServerProfileSchemaDao.class);

    @Override
    protected Class<ServerProfileSchema> getEntityClass() {
        return ServerProfileSchema.class;
    }
    
    @Override
    public ServerProfileSchema findLatestByAppId(String appId) {
        ServerProfileSchema latestSchema = null;
        LOG.debug("Searching latest server profile schema by application id [{}] ", appId);
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria().createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS)
                    .add(Restrictions.eq(APPLICATION_REFERENCE, getLongId(appId)))
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
    public List<ServerProfileSchema> findByAppId(String appId) {
        List<ServerProfileSchema> schemas = Collections.emptyList();
        LOG.debug("Searching server profile schemas by application id [{}] ", appId);
        if (isNotBlank(appId)) {
            schemas = findListByCriterion(Restrictions.eq(APPLICATION_REFERENCE, getLongId(appId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, schemas.size());
        }
        return schemas;
    }
    
    @Override
    public ServerProfileSchema findByAppIdAndVersion(String appId, int version) {
        LOG.debug("Searching server profile schema by application id [{}] and version [{}]", appId, version);
        ServerProfileSchema schema = null;
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
    public void removeByAppId(String appId) {
        if (isNotBlank(appId)) {
            List<ServerProfileSchema> schemaList = findListByCriterion(Restrictions.eq(APPLICATION_REFERENCE, getLongId(appId)));
            removeList(schemaList);
        }
        LOG.debug("Removed server profile schemas by application id [{}]", appId);
    }
}
