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
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.common.dao.impl.EventClassDao;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECF_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECF_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECF_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EVENT_CLASS_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.FQN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernateEventClassDao extends HibernateAbstractDao<EventClass> implements EventClassDao<EventClass> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventClassDao.class);

    @Override
    protected Class<EventClass> getEntityClass() {
        return EventClass.class;
    }

    @Override
    public List<EventClass> findByEcfId(String ecfId) {
        LOG.debug("Find event classes by ecf id [{}] ", ecfId);
        List<EventClass> eventClasses = Collections.emptyList();
        if (isNotBlank(ecfId)) {
            eventClasses = findListByCriterionWithAlias(ECF_PROPERTY, ECF_ALIAS, Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)));
        }
        LOG.info("Found event classes {} by ecf id {} ", eventClasses.size(), ecfId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found event classes {} by ecf id {} ", Arrays.toString(eventClasses.toArray()), ecfId);
        }
        return eventClasses;
    }

    @Override
    public List<EventClass> findByEcfIdVersionAndType(String ecfId, int version, EventClassType type) {
        LOG.debug("Find event class by ecf id [{}] version [{}] and type [{}]", ecfId, version, type);
        List<EventClass> eventClasses = Collections.emptyList();
        if (isNotBlank(ecfId)) {
            List<Criterion> predicates = new ArrayList<>();
            predicates.add(Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)));
            predicates.add(Restrictions.eq(VERSION_PROPERTY, version));
            if (type != null) {
                predicates.add(Restrictions.eq(EVENT_CLASS_TYPE_PROPERTY, type));
            }
            eventClasses = findListByCriterionWithAlias(ECF_PROPERTY, ECF_ALIAS,
                    Restrictions.and(predicates.toArray(new Criterion[]{})));
        }
        LOG.debug("Found event classes {} by ecf id {}, version {} and type {}", eventClasses, ecfId, version, type);
        return eventClasses;
    }

    @Override
    public void removeByEcfId(String ecfId) {
        LOG.debug("Remove event class by ecf id [{}] ", ecfId);
        if (isNotBlank(ecfId)) {
            List<EventClass> eventClasses = findListByCriterionWithAlias(ECF_PROPERTY, ECF_ALIAS,
                    Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)));
            removeList(eventClasses);
        }
    }

    @Override
    public List<EventClass> findByTenantIdAndFqn(String tenantId, String fqn) {
        LOG.debug("Find event classes by tenant id [{}] and fqn [{}]", tenantId, fqn);
        List<EventClass> eventClass = null;
        if (isNotBlank(tenantId)) {
            eventClass = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                            Restrictions.eq(FQN_PROPERTY, fqn)));
        }
        return eventClass;
    }

    @Override
    public EventClass findByTenantIdAndFqnAndVersion(String tenantId, String fqn, int version) {
        LOG.debug("Find event classes by tenant id [{}] and fqn [{}]", tenantId, fqn);
        EventClass eventClass = null;
        if (isNotBlank(tenantId)) {
            eventClass = findOneByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                            Restrictions.eq(FQN_PROPERTY, fqn),
                            Restrictions.eq(VERSION_PROPERTY, version)
                            ));
        }
        return eventClass;
    }

    @Override
    public boolean validateFqns(String tenantId, String ecfId, List<String> fqns) {
        LOG.debug("Validate FQNs by tenant id [{}], ecf id [{}] and FQNs [{}]", tenantId, ecfId, fqns);
        List<EventClass> eventClasses = Collections.emptyList();
        if (isNotBlank(tenantId) && isNotBlank(ecfId) && fqns != null && !fqns.isEmpty()) {
            Criteria criteria = getCriteria();
            criteria.createAlias(TENANT_PROPERTY, TENANT_ALIAS);
            criteria.createAlias(ECF_PROPERTY, ECF_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)),
                    Restrictions.ne(ECF_REFERENCE, Long.valueOf(ecfId)),
                    Restrictions.in(FQN_PROPERTY, fqns)));
            eventClasses = findListByCriteria(criteria);
        }
        return eventClasses == null || eventClasses.isEmpty();
    }
}
