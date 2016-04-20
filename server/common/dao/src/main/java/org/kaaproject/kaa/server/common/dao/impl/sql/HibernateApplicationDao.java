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
import org.kaaproject.kaa.server.common.dao.impl.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_ENTITY_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_REFERENCE;

@Repository
public class HibernateApplicationDao extends HibernateAbstractDao<Application> implements ApplicationDao<Application> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateApplicationDao.class);

    @Override
    protected Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public List<Application> findByTenantId(String tenantId) {
        LOG.debug("Searching applications by tenant id [{}]", tenantId);
        List<Application> applications = findListByCriterionWithAlias(TENANT_ENTITY_NAME, TENANT_ALIAS, Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", tenantId, Arrays.toString(applications.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", tenantId, applications.size());
        }
        return applications;
    }

    @Override
    public Application findByApplicationToken(String token) {
        LOG.debug("Searching for application by token {}", token);
        Application app = findOneByCriterion(Restrictions.eq(APPLICATION_TOKEN_PROPERTY, token));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", token, app);
        } else {
            LOG.debug("[{}] Search result: {}.", token, app != null);
        }
        return app;
    }

    // TODO: Need to add optimistic lock
    @Override
    public Application getNextSeqNumber(String id) {
        Application app = findById(id);
        if (app != null) {
            app.incrementSequenceNumber();
            save(app);
            LOG.debug("Incremented application sequence number to {}", app.getSequenceNumber());
        }
        return app;
    }

    @Override
    public Application findByNameAndTenantId(String name, String tenantId) {
        LOG.debug("Searching for application by name [{}] and tenant id [{}] ", name, tenantId);
        Application application = findOneByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                Restrictions.and(
                        Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                        Restrictions.eq(APPLICATION_NAME_PROPERTY, name)));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", name, tenantId, application);
        } else {
            LOG.debug("[{},{}] Search result: {}.", name, tenantId, application != null);
        }
        return application;
    }

    @Override
    public void removeByApplicationToken(String token) {
        Application app = findByApplicationToken(token);
        if (app != null) {
            remove(app);
        }
        LOG.debug("Removed application by application token [{}] ", token);
    }
}
