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

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.LogAppenderDao;
import org.kaaproject.kaa.server.common.dao.model.sql.LogAppender;
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
import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_APPENDER_MAX_LOG_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_APPENDER_MIN_LOG_SCHEMA_VERSION;

@Repository
public class HibernateLogAppenderDao extends HibernateAbstractDao<LogAppender> implements LogAppenderDao<LogAppender> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateLogAppenderDao.class);

    @Override
    protected Class<LogAppender> getEntityClass() {
        return LogAppender.class;
    }

    @Override
    public List<LogAppender> findByAppId(String appId) {
        LOG.debug("Searching log appenders by application id [{}]", appId);
        List<LogAppender> appenders = Collections.emptyList();
        if (isNotBlank(appId)) {
            appenders = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(appenders.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, appenders.size());
        }
        return appenders;
    }

    @Override
    public List<LogAppender> findByAppIdAndSchemaVersion(String appId, int schemaVersion) {
        LOG.debug("Searching log appenders by application id [{}] and schema version [{}]", appId, schemaVersion);
        List<LogAppender> appenders = Collections.emptyList();
        if (isNotBlank(appId)) {
            appenders = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.le(LOG_APPENDER_MIN_LOG_SCHEMA_VERSION, schemaVersion),
                            Restrictions.ge(LOG_APPENDER_MAX_LOG_SCHEMA_VERSION, schemaVersion))
            );
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, schemaVersion, Arrays.toString(appenders.toArray()));
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, schemaVersion, appenders.size());
        }
        return appenders;
    }

}
