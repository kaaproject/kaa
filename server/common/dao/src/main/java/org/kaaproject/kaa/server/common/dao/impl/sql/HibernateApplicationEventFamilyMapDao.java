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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationEventFamilyMapDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventFamilyMap;
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
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECF_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECF_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ECF_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernateApplicationEventFamilyMapDao extends HibernateAbstractDao<ApplicationEventFamilyMap> implements ApplicationEventFamilyMapDao<ApplicationEventFamilyMap> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateApplicationEventFamilyMapDao.class);

    @Override
    protected Class<ApplicationEventFamilyMap> getEntityClass() {
        return ApplicationEventFamilyMap.class;
    }

    @Override
    public List<ApplicationEventFamilyMap> findByApplicationId(String appId) {
        LOG.debug("Searching application event family maps by application id [{}] ", appId);
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = Collections.emptyList();
        if (isNotBlank(appId)) {
            applicationEventFamilyMaps = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, applicationEventFamilyMaps);
        } else {
            LOG.debug("[{}] Search result: {}.", appId, applicationEventFamilyMaps.size());
        }
        return applicationEventFamilyMaps;
    }

    @Override
    public List<ApplicationEventFamilyMap> findByIds(List<String> ids) {
        List<ApplicationEventFamilyMap> applicationEventFamilyMaps = Collections.emptyList();
        String idsArray = "";
        if (ids != null && !ids.isEmpty()) {
            idsArray = Arrays.toString(ids.toArray());
            LOG.debug("Searching application event family maps by ids {} ", idsArray);
            applicationEventFamilyMaps = findListByCriterion(Restrictions.in(ID_PROPERTY, toLongIds(ids)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("{} Search result: {}.", idsArray, Arrays.toString(applicationEventFamilyMaps.toArray()));
        } else {
            LOG.debug("{} Search result: {}.", idsArray, applicationEventFamilyMaps.size());
        }
        return applicationEventFamilyMaps;
    }

    @Override
    public void removeByApplicationId(String appId) {
        if (isNotBlank(appId)) {
            List<ApplicationEventFamilyMap> eventClassFamilies = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
            removeList(eventClassFamilies);
        }
        LOG.debug("Removed application event family map by application id [{}] ", appId);
    }

    @Override
    public boolean validateApplicationEventFamilyMap(String appId, String ecfId, int version) {
        LOG.debug("Validating application event family map by application id [{}], ecf id [{}], version [{}]", appId, ecfId, version);
        Criteria criteria = getCriteria();
        criteria.createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS);
        criteria.createAlias(ECF_PROPERTY, ECF_ALIAS);
        criteria.add(Restrictions.and(
                Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)),
                Restrictions.eq(VERSION_PROPERTY, version)));
        Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        boolean result = count != null ? count == 0 : false;
        LOG.debug("[{},{},{}] Validation result: {}.", appId, ecfId, version, result);
        return result;
    }

    @Override
    public List<ApplicationEventFamilyMap> findByEcfIdAndVersion(String ecfId, int version) {
        LOG.debug("Searching application event family maps by event class family id [{}] and version [{}] ", ecfId, version);
        Criteria criteria = getCriteria();
        criteria.createAlias(ECF_PROPERTY, ECF_ALIAS);
        criteria.add(Restrictions.and(
                Restrictions.eq(ECF_REFERENCE, Long.valueOf(ecfId)),
                Restrictions.eq(VERSION_PROPERTY, version)));
        List<ApplicationEventFamilyMap> maps = findListByCriteria(criteria);
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", ecfId, version, Arrays.toString(maps.toArray()));
        } else {
            LOG.debug("[{},{}] Search result: {}.", ecfId, version, maps.size());
        }
        return maps;
    }
}
