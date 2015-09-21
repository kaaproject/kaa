/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.SdkKeyDao;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.*;

@Repository
public class HibernateSdkKeyDao extends HibernateAbstractDao<SdkKey> implements SdkKeyDao<SdkKey> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateSdkKeyDao.class);

    @Override
    protected Class<SdkKey> getEntityClass() {
        return SdkKey.class;
    }

    @Override
    public SdkKey findSdkKeyByToken(String token) {
        SdkKey sdkKey = null;
        LOG.debug("Searching SDK token object by SDK token [{}]", token);
        if (isNotBlank(token)) {
            sdkKey = findOneByCriterion(Restrictions.eq(SDK_TOKEN_PROPERTY, token));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", token, sdkKey);
        } else {
            LOG.debug("[{}] Search result: {}.", token, sdkKey != null);
        }
        return sdkKey;
    }

    @Override
    public List<SdkKey> findSdkKeysByApplicationId(String applicationId) {
        LOG.debug("Searching for SDK profiles by application ID: [{}]", applicationId);
        List<SdkKey> sdkProfiles = Collections.emptyList();
        if (StringUtils.isNotBlank(applicationId)) {
            Criterion criterion = Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(applicationId));
            sdkProfiles = this.findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, criterion);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", applicationId, Arrays.toString(sdkProfiles.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", applicationId, sdkProfiles.size());
        }
        return sdkProfiles;
    }
}
