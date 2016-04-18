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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.LogSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernateLogSchemaDao extends HibernateAbstractDao<LogSchema> implements LogSchemaDao<LogSchema> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateLogSchemaDao.class);

    @Override
    protected Class<LogSchema> getEntityClass() {
        return LogSchema.class;
    }

    @Override
    public List<LogSchema> findByApplicationId(String applicationId) {
        LOG.debug("Find versions by applicationId [{}] ", applicationId);
        List<LogSchema> schemas = null;
        if (isNotBlank(applicationId)) {
            schemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", applicationId, Arrays.toString(schemas.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", applicationId, schemas.size());
        }
        return schemas;
    }

    @Override
    public LogSchema findByApplicationIdAndVersion(String applicationId, int version) {
        LOG.debug("Searching log schema by applicationId [{}] and version [{}] ", applicationId, version);
        LogSchema logSchema = null;
        if (isNotBlank(applicationId)) {
            logSchema = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)),
                    Restrictions.eq(VERSION_PROPERTY, version)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", applicationId, version, logSchema);
        } else {
            LOG.debug("[{},{}] Search result: {}.", applicationId, version, logSchema != null);
        }
        return logSchema;
    }

    @Override
    public void removeByApplicationId(String applicationId) {
        if (isNotBlank(applicationId)) {
            List<LogSchema> logSchemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
            removeList(logSchemas);
        }
        LOG.debug("Removed log schema  by application id [{}] ", applicationId);
    }

    @Override
    public LogSchema findLatestLogSchemaByAppId(String applicationId) {
        LOG.debug("Searching latest log schema  by application id [{}]", applicationId);
        LogSchema logSchema = null;
        if (isNotBlank(applicationId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS);
            Criterion criterion = Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId));
            logSchema = (LogSchema) criteria.add(criterion).addOrder(Order.desc(VERSION_PROPERTY))
                    .setMaxResults(FIRST).uniqueResult();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", applicationId, logSchema);
        } else {
            LOG.debug("[{}] Search result: {}.", applicationId, logSchema != null);
        }
        return logSchema;
    }

}
