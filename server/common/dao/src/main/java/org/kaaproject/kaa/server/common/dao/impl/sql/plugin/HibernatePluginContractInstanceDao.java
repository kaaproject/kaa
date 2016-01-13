/*
 * Copyright 2016 CyberVision, Inc.
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

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.PluginContractInstanceDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.PluginContractInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.ID_PROPERTY;

@Repository
public class HibernatePluginContractInstanceDao extends HibernateAbstractDao<PluginContractInstance>
        implements PluginContractInstanceDao<PluginContractInstance> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernatePluginContractInstanceDao.class);

    @Override
    public List<PluginContractInstance> findByIds(List<String> ids) {
        List<PluginContractInstance> pluginContractInstances = Collections.emptyList();
        String idsArray = "";
        if (ids != null && !ids.isEmpty()) {
            idsArray = Arrays.toString(ids.toArray());
            LOG.debug("Looking for plugin instances by ids: {}", idsArray);
            pluginContractInstances = findListByCriterion(Restrictions.in(ID_PROPERTY, toLongIds(ids)));
        } else {
            LOG.debug("Looking for plugin instances by ids: {}", ids);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("{} Search result: {}.", idsArray, Arrays.toString(pluginContractInstances.toArray()));
        } else {
            LOG.debug("{} Search result: {}.", idsArray, pluginContractInstances.size());
        }
        return pluginContractInstances;
    }

    @Override
    protected Class<PluginContractInstance> getEntityClass() {
        return PluginContractInstance.class;
    }
}
