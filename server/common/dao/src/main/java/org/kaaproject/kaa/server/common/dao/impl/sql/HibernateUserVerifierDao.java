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
import org.kaaproject.kaa.server.common.dao.impl.UserVerifierDao;
import org.kaaproject.kaa.server.common.dao.model.sql.UserVerifier;
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
import static org.kaaproject.kaa.server.common.dao.DaoConstants.USER_VERIFIER_TOKEN_PROPERTY;

@Repository
public class HibernateUserVerifierDao extends HibernateAbstractDao<UserVerifier> implements UserVerifierDao<UserVerifier> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateUserVerifierDao.class);

    @Override
    protected Class<UserVerifier> getEntityClass() {
        return UserVerifier.class;
    }

    @Override
    public List<UserVerifier> findByAppId(String appId) {
        List<UserVerifier> appenders = Collections.emptyList();
        LOG.debug("Searching user verifiers by application id [{}]", appId);
        if (isNotBlank(appId)) {
            appenders = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)))
            );
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", appId, Arrays.toString(appenders.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", appId, appenders.size());
        }
        return appenders;
    }

    @Override
    public UserVerifier findByAppIdAndVerifierToken(String appId, String verifierToken) {
        LOG.debug("Searching user verifier by application id [{}] and verifier token [{}]", appId, verifierToken);
        UserVerifier verifier = null;
        if (isNotBlank(appId) && isNotBlank(verifierToken)) {
            verifier = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.eq(USER_VERIFIER_TOKEN_PROPERTY, verifierToken))
            );
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, verifierToken, verifier);
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, verifierToken, verifier != null);
        }
        return verifier;
    }
}
