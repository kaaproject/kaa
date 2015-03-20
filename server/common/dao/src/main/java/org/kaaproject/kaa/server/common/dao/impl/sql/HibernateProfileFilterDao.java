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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.impl.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.MAJOR_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_SCHEMA_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_SCHEMA_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_SCHEMA_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.STATUS_PROPERTY;

@Repository
public class HibernateProfileFilterDao extends HibernateAbstractDao<ProfileFilter> implements ProfileFilterDao<ProfileFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateProfileFilterDao.class);

    @Override
    public List<ProfileFilter> findAllByProfileSchemaId(String schemaId) {
        LOG.debug("Find profile filter by profile schema id [{}] ", schemaId);
        List<ProfileFilter> filters = Collections.emptyList();
        if (isNotBlank(schemaId)) {
            filters = findListByCriterionWithAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS,
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)));
        }
        return filters;
    }

    @Override
    public List<ProfileFilter> findActualByEndpointGroupId(String groupId) {
        LOG.debug("Find actual profile filters by endpoint group id [{}] ", groupId);
        List<ProfileFilter> filters = Collections.emptyList();
        if (isNotBlank(groupId)) {
            filters = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
        }
        return filters;
    }

    @Override
    public List<ProfileFilter> findActualBySchemaIdAndGroupId(String schemaId, String groupId) {
        LOG.debug("Find actual profile filters by schema id and group id [{}] ", schemaId, groupId);
        List<ProfileFilter> filters = Collections.emptyList();
        if (isNotBlank(groupId) && isNotBlank(schemaId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.createAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
            filters = findListByCriteria(criteria);
        }
        return filters;
    }

    @Override
    public ProfileFilter findLatestDeprecated(String schemaId, String groupId) {
        ProfileFilter filter = null;
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria cr = getCriteria();
            cr.createAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS);
            cr.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion crit = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.DEPRECATED));
            filter = (ProfileFilter) cr.add(crit).addOrder(Order.desc(SEQUENCE_NUMBER_PROPERTY)).setMaxResults(FIRST).uniqueResult();
        }
        LOG.debug("Found latest deprecated profile filter {} by profile schema id {} and group id {} ", filter, schemaId, groupId);
        return filter;
    }

    @Override
    public void removeByEndpointGroupId(String groupId) {
        LOG.debug("Remove profile filters by endpoint group id [{}] ", groupId);
        List<ProfileFilter> filters = Collections.emptyList();
        if (isNotBlank(groupId)) {
            filters = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)));
        }
        removeList(filters);
    }

    @Override
    public List<ProfileFilter> findByAppIdAndSchemaVersion(String appId, int schemaVersion) {
        List<ProfileFilter> filters = null;
        LOG.debug("Find configuration by application id {} and major version {}", appId, schemaVersion);
        if (isNotBlank(appId)) {
            filters = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(MAJOR_VERSION_PROPERTY, schemaVersion),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        return filters;
    }

    @Override
    public ProfileFilter findInactiveFilter(String schemaId, String groupId) {
        ProfileFilter filter = null;
        LOG.debug("Find inactive profile filter by profile schema id {} and group id {} ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.INACTIVE)));
            filter = findOneByCriteria(criteria);
        }
        return filter;
    }

    @Override
    public ProfileFilter findLatestFilter(String schemaId, String groupId) {
        LOG.debug("Find latest active profile filter by profile schema id [{}] and group id [{}]", schemaId, groupId);
        ProfileFilter filter = null;
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            filter = findOneByCriteria(criteria);
        }
        return filter;
    }

    @Override
    public ProfileFilter activate(String id, String activatedUsername) {
        LOG.debug("Activate profile filter and increment seq_num, found by id [{}] ", id);
        ProfileFilter filter = findById(id);
        if (filter != null) {
            filter.setStatus(UpdateStatus.ACTIVE);
            filter.setActivatedTime(System.currentTimeMillis());
            filter.setActivatedUsername(activatedUsername);
            filter.incrementSequenceNumber();
            save(filter);
        }
        return filter;
    }

    @Override
    public ProfileFilter deactivate(String id, String deactivatedUsername) {
        LOG.debug("Deactivate profile filter, found by id [{}] ", id);
        ProfileFilter filter = findById(id);
        if (filter != null) {
            filter.setStatus(UpdateStatus.DEPRECATED);
            filter.setDeactivatedTime(System.currentTimeMillis());
            filter.setDeactivatedUsername(deactivatedUsername);
            save(filter);
        }
        return filter;
    }

    @Override
    public ProfileFilter deactivateOldFilter(String schemaId, String groupId, String deactivatedUsername) {
        ProfileFilter filter = null;
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            filter = findOneByCriteria(criteria);
        }
        if (filter != null) {
            filter.setStatus(UpdateStatus.DEPRECATED);
            filter.setDeactivatedTime(System.currentTimeMillis());
            filter.setDeactivatedUsername(deactivatedUsername);
            save(filter);
        }
        return filter;
    }

    @Override
    public long findActiveFilterCount(String schemaId, String groupId) {
        long count = 0;
        LOG.debug("Find count of active profile filters by profile schema id {} and group id {} ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(PROFILE_SCHEMA_PROPERTY, PROFILE_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(PROFILE_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        }
        return count;
    }

    @Override
    protected Class<ProfileFilter> getEntityClass() {
        return ProfileFilter.class;
    }
}
