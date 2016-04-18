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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SDK_TOKEN_PROPERTY;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.SdkProfileDao;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateSdkProfileDao extends HibernateAbstractDao<SdkProfile> implements SdkProfileDao<SdkProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateSdkProfileDao.class);

    @Override
    protected Class<SdkProfile> getEntityClass() {
        return SdkProfile.class;
    }

    @Override
    public SdkProfile findSdkProfileByToken(String token) {
        LOG.debug("Searching for an SDK profile by token: [{}]", token);

        SdkProfile found = null;
        if (StringUtils.isNotBlank(token)) {
            found = this.findOneByCriterion(Restrictions.eq(SDK_TOKEN_PROPERTY, token));
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", token, found);
        } else {
            LOG.debug("[{}] Search result: {}.", token, found != null);
        }
        return found;
    }

    @Override
    public List<SdkProfile> findSdkProfileByApplicationId(String applicationId) {
        LOG.debug("Searching for SDK profiles by application ID: [{}]", applicationId);

        List<SdkProfile> found = Collections.emptyList();
        if (StringUtils.isNotBlank(applicationId)) {
            Criterion criterion = Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId));
            found = this.findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, criterion);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", applicationId, Arrays.toString(found.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", applicationId, found.size());
        }

        return found;
    }
}
