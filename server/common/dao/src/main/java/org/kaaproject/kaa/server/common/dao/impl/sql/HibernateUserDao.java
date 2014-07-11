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

import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.AUTHORITY_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.EXTERNAL_UID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TENANT_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TENANT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.TENANT_REFERENCE;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.common.dao.impl.UserDao;
import org.kaaproject.kaa.server.common.dao.model.sql.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateUserDao extends HibernateAbstractDao<User> implements UserDao<User> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateUserDao.class);

    @Override
    public User findByExternalUid(String externalUid) {
        LOG.debug("Find user by external uid [{}]", externalUid);
        return findOneByCriterion(Restrictions.eq(EXTERNAL_UID_PROPERTY, externalUid));
    }

    @Override
    public List<User> findByTenantIdAndAuthority(String id, String authority) {
        LOG.debug("Find users by tenant id [{}] and authority [{}]", id, authority);
        return findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS, Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(id)),
                Restrictions.eq(AUTHORITY_PROPERTY, KaaAuthorityDto.parse(authority))));
    }

    @Override
    public List<User> findByTenantIdAndAuthorities(String id, String... authorities) {
        if (LOG.isDebugEnabled()) {
            String authoritiesString = "";
            for (int i = 0; i < authorities.length; i++) {
                if (i > 0) {
                    authoritiesString += ", ";
                }
                authoritiesString += authorities[i];
            }
            LOG.debug("Find user by tenant id [{}] and authorities [{}]", id, authoritiesString);
        }
        return findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS, Restrictions.and(
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(id)),
                Restrictions.in(AUTHORITY_PROPERTY, KaaAuthorityDto.parseList(authorities))));
    }

    @Override
    public void removeByTenantId(String tenantId) {
        LOG.debug("Remove users by tenant id [{}] ", tenantId);
        List<User> users = findListByCriterionWithAlias(TENANT_PROPERTY, TENANT_ALIAS,
                Restrictions.eq(TENANT_REFERENCE, Long.valueOf(tenantId)));
        removeList(users);
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }
}
