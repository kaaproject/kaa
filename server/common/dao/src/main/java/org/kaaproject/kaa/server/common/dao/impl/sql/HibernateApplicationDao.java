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
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.*;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TENANT_REFERENCE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateApplicationDao extends HibernateAbstractDao<Application> implements ApplicationDao<Application> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateApplicationDao.class);

    @Override
    public List<Application> findByTenantId(String tenantId) {
        LOG.debug("Find applications by tenant id {}", tenantId);
        List<Application> applications = Collections.emptyList();
        if (isNotBlank(tenantId)) {
            applications = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS, Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)));
        }
        LOG.info("Found applications {} by tenant id {} ", applications.size(), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found applications {} by tenant id {} ", Arrays.toString(applications.toArray()), tenantId);
        }
        return applications;
    }

    @Override
    public Application findByApplicationToken(String token) {
        LOG.debug("Find application by token {}", token);
        Application app = findOneByCriterion(Restrictions.eq(APPLICATION_TOKEN_PROPERTY, token));
        LOG.debug("Found application {} by token {}", app, token);
        return app;
    }

    @Override
    public Application getNextSeqNumber(String id) {
        Application app = findById(id);
        if (app != null) {
            app.incrementSequenceNumber();
            LOG.debug("Increment application sequence number {}", app);
            save(app);
        }
        return app;
    }

    @Override
    protected Class<Application> getEntityClass() {
        return Application.class;
    }

    @Override
    public Application findByNameAndTenantId(String name, String tenantId) {
        LOG.debug("Find application by name [{}] and tenant id [{}] ", name, tenantId);
        Application application = findOneByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                Restrictions.eq(APPLICATION_NAME_PROPERTY, name)));
        LOG.debug("Found application {} by tenant id [{}] and name [{}]", application, tenantId, name);
        return application;
    }

    @Override
    public void removeByApplicationToken(String token) {
        LOG.debug("Remove application by application token [{}] ", token);
        Application app = findOneByCriterion(Restrictions.eq(APPLICATION_TOKEN_PROPERTY, token));
        remove(app);
    }
}
