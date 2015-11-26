/*
 * Copyright 2015 CyberVision, Inc.
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
import org.kaaproject.kaa.server.common.dao.impl.ServerProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CREATED_TIME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Repository
public class HibernateServerProfileSchemaDao extends HibernateAbstractDao<ServerProfileSchema> implements ServerProfileSchemaDao<ServerProfileSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateServerProfileSchemaDao.class);

    @Override
    protected Class<ServerProfileSchema> getEntityClass() {
        return ServerProfileSchema.class;
    }

    @Override
    public ServerProfileSchema findLatestByAppId(String appId) {
        LOG.debug("Searching for server profile schema by application id: [{}]", appId);

        ServerProfileSchema found = null;
        Criteria criteria = getCriteria().add(Restrictions.eq(APPLICATION_REFERENCE, getLongId(appId)))
                .addOrder(Order.desc(CREATED_TIME_PROPERTY))
                .setMaxResults(FIRST);
        if (isNotBlank(appId)) {
            found = this.findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, found);
        } else {
            LOG.debug("[{}] Search result: {}.", appId, found != null);
        }
        return found;
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
    public void removeByAppId(String appId) {
        if (isNotBlank(appId)) {
            List<ServerProfileSchema> schemaList = findListByCriterion(Restrictions.eq(APPLICATION_REFERENCE, getLongId(appId)));
            removeList(schemaList);
        }
        LOG.debug("Removed server profile schemas by application id [{}]", appId);
    }
}
