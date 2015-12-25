/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.ContractMessageDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.ContractMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.FQN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernateContractMessageDao extends HibernateAbstractDao<ContractMessage> implements ContractMessageDao<ContractMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateContractMessageDao.class);

    @Override
    public ContractMessage findByFqnAndVersion(String fqn, Integer version) {
        LOG.debug("Searching for a contract message by fqn and version [{}, {}]", fqn, version);
        ContractMessage contractMessage = null;
        if (StringUtils.isNotBlank(fqn) && (version != null)) {
            contractMessage = findOneByCriterion(Restrictions.and(
                    Restrictions.eq(FQN_PROPERTY, fqn),
                    Restrictions.eq(VERSION_PROPERTY, version)));
        }
        LOG.debug("Found contract message: {}", contractMessage);
        return contractMessage;
    }

    @Override
    protected Class<ContractMessage> getEntityClass() {
        return ContractMessage.class;
    }
}
