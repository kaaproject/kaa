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
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.EventClassFamilyDao;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClassFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CLASS_NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_REFERENCE;

@Repository
public class HibernateEventClassFamilyDao extends HibernateAbstractDao<EventClassFamily> implements EventClassFamilyDao<EventClassFamily> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventClassFamilyDao.class);

    @Override
    protected Class<EventClassFamily> getEntityClass() {
        return EventClassFamily.class;
    }

    @Override
    public List<EventClassFamily> findByTenantId(String tenantId) {
        LOG.debug("Searching event class families by tenant id [{}] ", tenantId);
        List<EventClassFamily> eventClassFamilies = Collections.emptyList();
        if (isNotBlank(tenantId)) {
            eventClassFamilies = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS, Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", tenantId, Arrays.toString(eventClassFamilies.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", tenantId, eventClassFamilies.size());
        }
        return eventClassFamilies;
    }

    @Override
    public EventClassFamily findByTenantIdAndName(String tenantId, String name) {
        LOG.debug("Searching event class family by tenant id [{}] and name [{}]", tenantId, name);
        EventClassFamily eventClassFamily = null;
        if (isNotBlank(tenantId)) {
            eventClassFamily = findOneByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                            Restrictions.eq(NAME_PROPERTY, name)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", tenantId, name, eventClassFamily);
        } else {
            LOG.debug("[{},{}] Search result: {}.", tenantId, name, eventClassFamily != null);
        }
        return eventClassFamily;
    }

    @Override
    public void removeByTenantId(String tenantId) {
        if (isNotBlank(tenantId)) {
            List<EventClassFamily> eventClassFamilies = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                    Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)));
            removeList(eventClassFamilies);
        }
        LOG.debug("Removed event class families by tenant id [{}] ", tenantId);
    }

    @Override
    public boolean validateName(String tenantId, String ecfId, String name) {
        LOG.debug("Validating by tenant id [{}], ecf id [{}], name [{}]", tenantId, ecfId, name);
        Criteria criteria = getCriteria();
        criteria.createAlias(TENANT_PROPERTY, TENANT_ALIAS);
        criteria.add(Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                Restrictions.eq(NAME_PROPERTY, name)));
        if (isNotBlank(ecfId)) {
            criteria = criteria.add(Restrictions.ne(ID_PROPERTY, Long.valueOf(ecfId)));
        }
        List<EventClassFamily> eventClassFamilies = findListByCriteria(criteria);
        boolean result = eventClassFamilies == null || eventClassFamilies.isEmpty();
        LOG.debug("[{},{},{}] Validating result: {}", tenantId, ecfId, name, result);
        return result;
    }

    @Override
    public boolean validateClassName(String tenantId, String ecfId, String className) {
        LOG.debug("Validating fqn, tenant id [{}], ecf id [{}], className [{}]", tenantId, ecfId, className);
        Criteria criteria = getCriteria();
        criteria.createAlias(TENANT_PROPERTY, TENANT_ALIAS);
        criteria.add(Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                Restrictions.eq(CLASS_NAME_PROPERTY, className)));
        if (isNotBlank(ecfId)) {
            criteria = criteria.add(Restrictions.ne(ID_PROPERTY, Long.valueOf(ecfId)));
        }
        List<EventClassFamily> eventClassFamilies = findListByCriteria(criteria);
        boolean result = eventClassFamilies == null || eventClassFamilies.isEmpty();
        LOG.debug("[{},{},{}] Validating result: {}", tenantId, ecfId, className, result);
        return result;
    }
}
