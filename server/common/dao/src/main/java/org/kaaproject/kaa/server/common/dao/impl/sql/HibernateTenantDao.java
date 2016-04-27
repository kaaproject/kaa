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

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.TenantDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.NAME_PROPERTY;

@Repository
public class HibernateTenantDao extends HibernateAbstractDao<Tenant> implements TenantDao<Tenant> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateTenantDao.class);

    public static final String DELETE_BY_NAME_HQL = "delete from Tenant where " + NAME_PROPERTY + "= :name";

    @Override
    public Tenant findByName(String tenantName) {
        LOG.debug("Searching tenant by name [{}]", tenantName);
        Tenant tenant = findOneByCriterion(Restrictions.eq(NAME_PROPERTY, tenantName));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", tenantName, tenant);
        } else {
            LOG.debug("[{}] Search result: {}.", tenantName, tenant != null);
        }
        return tenant;
    }

    @Override
    public void removeByName(String tenantName) {
        Query query = getQuery(DELETE_BY_NAME_HQL);
        int number = query.setString(NAME_PROPERTY, tenantName).executeUpdate();
        LOG.debug("Removed [{}] tenant by name [{}]", number, tenantName);
    }

    @Override
    protected Class<Tenant> getEntityClass() {
        return Tenant.class;
    }
}
