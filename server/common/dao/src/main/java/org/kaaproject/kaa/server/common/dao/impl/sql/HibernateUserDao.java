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
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.common.dao.impl.UserDao;
import org.kaaproject.kaa.server.common.dao.model.sql.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.AUTHORITY_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.EXTERNAL_UID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.TENANT_REFERENCE;

@Repository
public class HibernateUserDao extends HibernateAbstractDao<User> implements UserDao<User> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateUserDao.class);

    @Override
    public User findByExternalUid(String externalUid) {
        LOG.debug("Searching user by external uid [{}]", externalUid);
        return findOneByCriterion(Restrictions.eq(EXTERNAL_UID_PROPERTY, externalUid));
    }

    @Override
    public List<User> findByTenantIdAndAuthority(String id, String authority) {
        LOG.debug("Searching users by tenant id [{}] and authority [{}]", id, authority);
        List<User> users = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS, Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(id)),
                Restrictions.eq(AUTHORITY_PROPERTY, KaaAuthorityDto.parse(authority))));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", id, authority, Arrays.toString(users.toArray()));
        } else {
            LOG.debug("[{},{}] Search result: {}.", id, authority, users.size());
        }
        return users;
    }

    @Override
    public List<User> findByTenantIdAndAuthorities(String id, String... authorities) {
        if (authorities != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Searching user by tenant id [{}] and authorities [{}]", id, Arrays.toString(authorities));
            } else {
                LOG.debug("Searching user by tenant id [{}] and authorities [{}]", id, authorities.length);
            }
        }
        List<User> users = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS, Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(id)),
                Restrictions.in(AUTHORITY_PROPERTY, KaaAuthorityDto.parseList(authorities))));
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", id, Arrays.toString(users.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", id, users.size());
        }
        return users;
    }

    @Override
    public void removeByTenantId(String tenantId) {
        List<User> users = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)));
        removeList(users);
        LOG.debug("Removed users by tenant id [{}] ", tenantId);
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }
}
