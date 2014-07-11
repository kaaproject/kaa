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
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ECF_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ECF_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ECF_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.VERSION_PROPERTY;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationEventFamilyMapDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventFamilyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateApplicationEventFamilyMapDao extends HibernateAbstractDao<ApplicationEventFamilyMap> implements ApplicationEventFamilyMapDao<ApplicationEventFamilyMap> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateApplicationEventFamilyMapDao.class);

    @Override
    protected Class<ApplicationEventFamilyMap> getEntityClass() {
        return ApplicationEventFamilyMap.class;
    }

    @Override
    public List<ApplicationEventFamilyMap> findByApplicationId(String applicationId) {
        LOG.debug("Find application event family maps by application id [{}] ", applicationId);
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = Collections.emptyList();
        if (isNotBlank(applicationId)) {
            applicationEventFamilyMaps = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
        }
        LOG.info("Found application event family maps {} by tenant id {} ", applicationEventFamilyMaps.size(), applicationId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found application event family maps {} by tenant id {} ", Arrays.toString(applicationEventFamilyMaps.toArray()), applicationId);
        }
        return applicationEventFamilyMaps;
    }

    @Override
    public List<ApplicationEventFamilyMap> findByIds(List<String> ids) {
        LOG.debug("Find applicationEventFamilyMaps by ids [{}] ", ids);
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = Collections.emptyList();
        if (ids != null && !ids.isEmpty()) {
            applicationEventFamilyMaps = findListByCriterion(Restrictions.in(ID_PROPERTY, toLongIds(ids)));
        }
        return applicationEventFamilyMaps;
    }

    @Override
    public void removeByApplicationId(String applicationId) {
        LOG.debug("remove applicationEventFamilyMap by application id [{}] ", applicationId);
        if (isNotBlank(applicationId)) {
            List<ApplicationEventFamilyMap> eventClassFamilies = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)));
            removeList(eventClassFamilies);
        }
    }

    @Override
    public boolean validateApplicationEventFamilyMap(String applicationId,
            String ecfId, int version) {
        LOG.debug("Validate application event family map, application id [{}], ecf id [{}], version [{}]", applicationId, ecfId, version);
        Criteria criteria = getCriteria();
        criteria.createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS);
        criteria.createAlias(ECF_PROPERTY, ECF_ALIAS);
        criteria.add(Restrictions.and(
                Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId)),
                Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)),
                Restrictions.eq(VERSION_PROPERTY, version)));
        Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count == 0;
    }

    @Override
    public List<ApplicationEventFamilyMap> findByEcfIdAndVersion(String ecfId, int version) {
        LOG.debug("Find applicationEventFamilyMap by eventClassFamilyId id [{}] and version {} ", ecfId, version);
        Criteria criteria = getCriteria();
        criteria.createAlias(ECF_PROPERTY, ECF_ALIAS);
        criteria.add(Restrictions.and(
                Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)),
                Restrictions.eq(VERSION_PROPERTY, version)));
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = findListByCriteria(criteria);
        return applicationEventFamilyMaps;
    }
}
