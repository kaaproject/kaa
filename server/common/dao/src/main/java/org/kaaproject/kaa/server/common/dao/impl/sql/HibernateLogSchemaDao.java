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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.LogSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.MAJOR_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.MINOR_VERSION_PROPERTY;

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
        return schemas;
    }

    @Override
    public LogSchema findByApplicationIdAndVersion(String applicationId, int version) {
        LOG.debug("Find log schema by applicationId [{}] and version [{}] ", applicationId, version);
        LogSchema schema = null;
        if (isNotBlank(applicationId)) {
            schema = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)),
                    Restrictions.eq(MAJOR_VERSION_PROPERTY, version)));
        }
        return schema;
    }

    @Override
    public void removeByApplicationId(String applicationId) {
        LOG.debug("Remove log schema  by application id [{}] ", applicationId);
        if (isNotBlank(applicationId)) {
            List<LogSchema> logSchemas = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
            removeList(logSchemas);
        }
    }

    @Override
    public LogSchema findLatestLogSchemaByAppId(String applicationId) {
        LOG.debug("Find latest log schema  by application id [{}]", applicationId);
        LogSchema logSchema = null;
        if (isNotBlank(applicationId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS);
            Criterion criterion = Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId));
            logSchema = (LogSchema) criteria.add(criterion).addOrder(Order.desc(MAJOR_VERSION_PROPERTY))
                    .addOrder(Order.desc(MINOR_VERSION_PROPERTY)).setMaxResults(FIRST).uniqueResult();
        }
        return logSchema;
    }

    @Override
    public List<LogSchema> findVacantSchemasByAppenderId(String appenderId) {
        LOG.debug("Find vacant log schemas by appender id [{}]", appenderId);
        List<LogSchema> logSchemas = java.util.Collections.emptyList();
//        List<LogSchema> logSchemas = findListByCriterionWithAlias(LOG_APPENDER_PROPERTY, LOG_APPENDER_ALIAS,
//                Restrictions.ne(LOG_APPENDER_REFERENCE, Long.valueOf(appenderId)));
//
//
//        if (isNotBlank(appenderId)) {
//            logSchemas = findListByCriterionWithAlias(LOG_APPENDER_PROPERTY, LOG_APPENDER_ALIAS, JoinType.LEFT_OUTER_JOIN,
//                    Restrictions.or(
//                            Restrictions.ne(LOG_APPENDER_REFERENCE, Long.valueOf(appenderId)),
//                            Restrictions.isNull(LOG_APPENDER_REFERENCE)));
//        }
        return logSchemas;
    }
}
