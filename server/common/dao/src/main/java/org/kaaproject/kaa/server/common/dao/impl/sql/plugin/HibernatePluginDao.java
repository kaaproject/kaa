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
import org.kaaproject.kaa.server.common.dao.impl.PluginDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractDao;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CLASS_NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.NAME_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.VERSION_PROPERTY;

@Repository
public class HibernatePluginDao extends HibernateAbstractDao<Plugin> implements PluginDao<Plugin> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernatePluginDao.class);

    @Override
    public Plugin findByNameAndVersion(String name, Integer version) {
        LOG.debug("Searching plugin by name and version [{}, {}]", name, version);
        Plugin plugin = null;
        if (StringUtils.isNotBlank(name) && (version != null)) {
            plugin = findOneByCriterion(Restrictions.and(
                    Restrictions.eq(NAME_PROPERTY, name),
                    Restrictions.eq(VERSION_PROPERTY, version)));
        }
        LOG.debug("Found plugin: {}", plugin);
        return plugin;
    }

    @Override
    public Plugin findByClassName(String className) {
        LOG.debug("Searching plugin by class name [{}]", className);
        Plugin plugin = null;
        if (StringUtils.isNotBlank(className)) {
            plugin = findOneByCriterion(Restrictions.eq(CLASS_NAME_PROPERTY, className));
        }
        LOG.debug("Found plugin: {}", plugin);
        return plugin;
    }

    @Override
    protected Class<Plugin> getEntityClass() {
        return Plugin.class;
    }
}
