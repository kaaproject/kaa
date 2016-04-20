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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.impl.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_PROFILE_SCHEMA_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_PROFILE_SCHEMA_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_PROFILE_SCHEMA_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_PROFILE_SCHEMA_VERSION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_VERSION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.STATUS_PROPERTY;

@Repository
public class HibernateProfileFilterDao extends HibernateAbstractDao<ProfileFilter> implements ProfileFilterDao<ProfileFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateProfileFilterDao.class);

    @Override
    public List<ProfileFilter> findActualByEndpointGroupId(String groupId) {
        LOG.debug("Searching actual profile filters by endpoint group id [{}] ", groupId);
        List<ProfileFilter> filters = Collections.emptyList();
        if (isNotBlank(groupId)) {
            filters = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", groupId, Arrays.toString(filters.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", groupId, filters.size());
        }
        return filters;
    }

    @Override
    public List<ProfileFilter> findActualBySchemaIdAndGroupId(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        LOG.debug("Searching actual profile filters by endpoint schema id [{}], server schema id [{}] and group id [{}] ",
                endpointProfileSchemaId, serverProfileSchemaId, groupId);
        List<ProfileFilter> filters = Collections.emptyList();
        if (isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    buildEqIdCriterion(ENDPOINT_PROFILE_SCHEMA_REFERENCE, endpointProfileSchemaId),
                    buildEqIdCriterion(SERVER_PROFILE_SCHEMA_REFERENCE, serverProfileSchemaId),
                    Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
            filters = findListByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, Arrays.toString(filters.toArray()));
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, filters.size());
        }
        return filters;
    }

    @Override
    public ProfileFilter findLatestDeprecated(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        LOG.debug("Searching latest deprecated profile filter by endpoint profile schema id [{}], server profile schema id [{}] and group id [{}] ",
                endpointProfileSchemaId, serverProfileSchemaId, groupId);
        ProfileFilter filter = null;
        if (isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion crit = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    buildEqIdCriterion(ENDPOINT_PROFILE_SCHEMA_REFERENCE, endpointProfileSchemaId),
                    buildEqIdCriterion(SERVER_PROFILE_SCHEMA_REFERENCE, serverProfileSchemaId),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.DEPRECATED));
            filter = (ProfileFilter) criteria.add(crit).addOrder(Order.desc(SEQUENCE_NUMBER_PROPERTY)).setMaxResults(FIRST).uniqueResult();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, filter);
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, filter != null);
        }
        return filter;
    }

    @Override
    public List<ProfileFilter> findByAppIdAndSchemaVersionsCombination(String appId, int endpointSchemaVersion, int serverSchemaVersion) {
        List<ProfileFilter> filters = null;
        LOG.debug("Searching configuration by application id [{}] and schema version [{}]", appId, serverSchemaVersion);
        if (isNotBlank(appId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS);
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            Criterion criterion = Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE),
                    Restrictions.or(Restrictions.and(
                            Restrictions.eq(ENDPOINT_PROFILE_SCHEMA_VERSION_REFERENCE, endpointSchemaVersion),
                            Restrictions.eq(SERVER_PROFILE_SCHEMA_VERSION_REFERENCE, serverSchemaVersion)
                    ), Restrictions.and(
                            Restrictions.eq(ENDPOINT_PROFILE_SCHEMA_VERSION_REFERENCE, endpointSchemaVersion),
                            Restrictions.isNull(SERVER_PROFILE_SCHEMA_VERSION_REFERENCE)
                    ), Restrictions.and(
                            Restrictions.eq(SERVER_PROFILE_SCHEMA_VERSION_REFERENCE, serverSchemaVersion),
                            Restrictions.isNull(ENDPOINT_PROFILE_SCHEMA_VERSION_REFERENCE)
                    )));
            criteria.add(criterion);
            filters = findListByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, serverSchemaVersion, Arrays.toString(filters.toArray()));
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, serverSchemaVersion, filters.size());
        }
        return filters;
    }

    @Override
    public ProfileFilter findInactiveFilter(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        ProfileFilter filter = null;
        LOG.debug("Searching inactive profile filter by endpoint profile schema id [{}], server profile schema id [{}] and group id [{}] ",
                endpointProfileSchemaId, serverProfileSchemaId, groupId);
        if (isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    buildEqIdCriterion(ENDPOINT_PROFILE_SCHEMA_REFERENCE, endpointProfileSchemaId),
                    buildEqIdCriterion(SERVER_PROFILE_SCHEMA_REFERENCE, serverProfileSchemaId),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.INACTIVE)));
            filter = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, filter);
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", endpointProfileSchemaId, groupId, serverProfileSchemaId, filter != null);
        }
        return filter;
    }

    @Override
    public ProfileFilter findLatestFilter(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        LOG.debug("Searching latest active profile filter by profile schema id [{}] and group id [{}]",
                endpointProfileSchemaId, serverProfileSchemaId, groupId);
        ProfileFilter filter = null;
        if (isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    buildEqIdCriterion(ENDPOINT_PROFILE_SCHEMA_REFERENCE, endpointProfileSchemaId),
                    buildEqIdCriterion(SERVER_PROFILE_SCHEMA_REFERENCE, serverProfileSchemaId),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            filter = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, filter);
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, filter != null);
        }
        return filter;
    }

    @Override
    public ProfileFilter activate(String id, String username) {
        LOG.debug("Activating profile filter with id [{}] by username [{}]", id, username);
        ProfileFilter filter = findById(id);
        if (filter != null) {
            filter.setStatus(UpdateStatus.ACTIVE);
            filter.setActivatedTime(System.currentTimeMillis());
            filter.setActivatedUsername(username);
            filter.incrementSequenceNumber();
            filter = save(filter);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Activating result: {}.", id, username, filter);
        } else {
            LOG.debug("[{},{}] Activating result: {}.", id, username, filter != null);
        }
        return filter;
    }

    @Override
    public ProfileFilter deactivate(String id, String username) {
        LOG.debug("Deactivating profile filter with id [{}] ", id);
        ProfileFilter filter = findById(id);
        if (filter != null) {
            filter.setStatus(UpdateStatus.DEPRECATED);
            filter.setDeactivatedTime(System.currentTimeMillis());
            filter.setDeactivatedUsername(username);
            filter = save(filter);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Deactivating result: {}.", id, username, filter);
        } else {
            LOG.debug("[{},{}] Deactivating result: {}.", id, username, filter != null);
        }
        return filter;
    }

    @Override
    public ProfileFilter deactivateOldFilter(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId, String username) {
        LOG.debug("Deactivating old profile filter with endpoint profile schema id [{}], server profile schema id [{}], group id [{}] by username [{}]",
                endpointProfileSchemaId, serverProfileSchemaId, groupId, username);
        ProfileFilter filter = null;
        if (isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    buildEqIdCriterion(ENDPOINT_PROFILE_SCHEMA_REFERENCE, endpointProfileSchemaId),
                    buildEqIdCriterion(SERVER_PROFILE_SCHEMA_REFERENCE, serverProfileSchemaId),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            filter = findOneByCriteria(criteria);
        }
        if (filter != null) {
            filter.setStatus(UpdateStatus.DEPRECATED);
            filter.setDeactivatedTime(System.currentTimeMillis());
            filter.setDeactivatedUsername(username);
            filter = save(filter);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{},{},{}] Deactivating result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, username, filter);
        } else {
            LOG.debug("[{},{},{},{}] Deactivating result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, username, filter != null);
        }
        return filter;
    }

    @Override
    public ProfileFilter save(ProfileFilter o) {
        ProfileFilter saved = super.save(o);
        getSession().flush();
        return saved;
    }

    @Override
    public long findActiveFilterCount(String endpointProfileSchemaId, String serverProfileSchemaId, String groupId) {
        long count = 0;
        LOG.debug("Searching active profile filters by endpoint profile schema id [{}], server profile schema id [{}] and group id [{}]",
                endpointProfileSchemaId, serverProfileSchemaId, groupId);
        if (isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(ENDPOINT_PROFILE_SCHEMA_PROPERTY, ENDPOINT_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(SERVER_PROFILE_SCHEMA_PROPERTY, SERVER_PROFILE_SCHEMA_ALIAS, JoinType.LEFT_OUTER_JOIN);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);

            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    buildEqIdCriterion(ENDPOINT_PROFILE_SCHEMA_REFERENCE, endpointProfileSchemaId),
                    buildEqIdCriterion(SERVER_PROFILE_SCHEMA_REFERENCE, serverProfileSchemaId),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        }
        LOG.debug("[{},{},{}] Search result: {}.", endpointProfileSchemaId, serverProfileSchemaId, groupId, count);
        return count;
    }

    @Override
    protected Class<ProfileFilter> getEntityClass() {
        return ProfileFilter.class;
    }

    private Criterion buildEqIdCriterion(String reference, String id) {
        Criterion criterion;
        if (isBlank(id)) {
            criterion = Restrictions.isNull(reference);
        } else {
            criterion = Restrictions.eq(reference, ModelUtils.getLongId(id));
        }
        return criterion;
    }
}
