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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.CONFIGURATION_SCHEMA_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.CONFIGURATION_SCHEMA_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.CONFIGURATION_SCHEMA_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ENDPOINT_GROUP_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ENDPOINT_GROUP_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.ENDPOINT_GROUP_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.MAJOR_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.STATUS_PROPERTY;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateConfigurationDao extends HibernateAbstractDao<Configuration> implements ConfigurationDao<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationDao.class);

    @Override
    public Configuration findConfigurationByAppIdAndVersion(String appId, int version) {
        Configuration configuration = null;
        LOG.debug("Find configuration by application id {} and major version {}", appId, version);
        if (isNotBlank(appId)) {
            configuration = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(MAJOR_VERSION_PROPERTY, version),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        LOG.debug("Found configuration {} by application id {} and major version {}", configuration, appId, version);
        return configuration;
    }

    @Override
    public Configuration findConfigurationByEndpointGroupIdAndVersion(String groupId, int version) {
        Configuration configuration = null;
        LOG.debug("Find configuration by endpoint group id {} and major version {}", groupId, version);
        if (isNotBlank(groupId)) {
            configuration = findOneByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.eq(MAJOR_VERSION_PROPERTY, version),
                            Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        LOG.debug("Found configuration {} by endpoint group id {} and major version {}", configuration, groupId, version);
        return configuration;
    }

    @Override
    public Configuration findLatestActiveBySchemaIdAndGroupId(String schemaId, String groupId) {
        Configuration configuration = null;
        LOG.debug("Find latest active configuration by configuration schema id {} and group id {} ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            configuration = (Configuration) findOneByCriteria(criteria);
        }
        LOG.debug("Find latest active configuration {} by configuration schema id {} and group id {} ", configuration, schemaId, groupId);
        return configuration;
    }

    @Override
    public Configuration findInactiveBySchemaIdAndGroupId(String schemaId, String groupId) {
        Configuration configuration = null;
        LOG.debug("Find inactive configuration by configuration schema id {} and group id {} ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria cr = getCriteria();
            cr.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            cr.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion crit = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.INACTIVE));
            configuration = (Configuration) cr.add(crit).uniqueResult();
        }
        LOG.debug("Find inactive configuration {} by configuration schema id {} and group id {} ", configuration, schemaId, groupId);
        return configuration;
    }

    @Override
    public List<Configuration> findActiveByEndpointGroupId(String groupId) {
        List<Configuration> configurations = null;
        LOG.debug("Find active configurations by endpoint group id {} ", groupId);
        if (isNotBlank(groupId)) {
            configurations = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        return configurations;
    }

    @Override
    public List<Configuration> findActualByEndpointGroupId(String groupId) {
        List<Configuration> configurations = null;
        LOG.debug("Find actual configurations by endpoint group id {} ", groupId);
        if (isNotBlank(groupId)) {
            configurations = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
        }
        return configurations;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Configuration> findActualBySchemaIdAndGroupId(String schemaId, String groupId) {
        List<Configuration> configurations = Collections.emptyList();
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria cr = getCriteria();
            cr.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            cr.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion crit = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED));
            configurations = cr.add(crit).list();
        }
        LOG.debug("Find {} actual configurations {} by configuration schema id {} and group id {} ", configurations.size(), schemaId, groupId);
        return configurations;
    }

    @Override
    public Configuration findLatestDeprecated(String schemaId, String groupId) {
        Configuration configuration = null;
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria cr = getCriteria();
            cr.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            cr.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion crit = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.DEPRECATED));
            configuration = (Configuration) cr.add(crit).addOrder(Order.desc(SEQUENCE_NUMBER_PROPERTY)).setMaxResults(FIRST).uniqueResult();
        }
        LOG.debug("Found latest deprecated configuration {} by configuration schema id {} and group id {} ", configuration, schemaId, groupId);
        return configuration;
    }

    @Override
    public void removeByConfigurationSchemaId(String schemaId) {
        LOG.debug("Remove configurations by configuration schema id [{}]", schemaId);
        if (isNotBlank(schemaId)) {
            List<Configuration> configurations = findListByCriterionWithAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS,
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)));
            removeList(configurations);
        }
    }

    @Override
    public void removeByEndpointGroupId(String groupId) {
        LOG.debug("Remove configurations by endpoint group id [{}]", groupId);
        if (isNotBlank(groupId)) {
            List<Configuration> configurations = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)));
            removeList(configurations);
        }
    }

    @Override
    public Configuration activate(String id, String activatedUsername) {
        LOG.debug("Activate configuration with id [{}] and increment sequence number.", id);
        Configuration configuration = findById(id);
        if (configuration != null) {
            configuration.setStatus(UpdateStatus.ACTIVE);
            configuration.setSequenceNumber(configuration.getSequenceNumber() + 1);
            configuration.setActivatedUsername(activatedUsername);
            configuration.setActivatedTime(System.currentTimeMillis());
            save(configuration);
        }
        return configuration;
    }

    @Override
    public Configuration deactivate(String id, String deactivatedUsername) {
        LOG.debug("Deactivate configuration with id [{}] and increment sequence number.", id);
        Configuration configuration = findById(id);
        if (configuration != null) {
            configuration.setStatus(UpdateStatus.DEPRECATED);
            configuration.setDeactivatedUsername(deactivatedUsername);
            configuration.setDeactivatedTime(System.currentTimeMillis());
            save(configuration);
        }
        return configuration;
    }

    @Override
    public Configuration deactivateOldConfiguration(String schemaId, String groupId, String deactivatedUsername) {
        LOG.debug("Deactivate old configuration, by configuration schema id [{}] and endpoint group id [{}] ", schemaId, groupId);
        Configuration configuration = null;
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria cr = getCriteria();
            cr.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            cr.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion crit = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE));
            configuration = (Configuration) cr.add(crit).uniqueResult();
            if (configuration != null) {
                configuration.setDeactivatedUsername(deactivatedUsername);
                configuration.setDeactivatedTime(System.currentTimeMillis());
                configuration.setStatus(UpdateStatus.DEPRECATED);
                save(configuration);
            }
        }
        LOG.debug("Deactivated old configuration {} by configuration schema id {} and group id {} ", configuration, schemaId, groupId);
        return configuration;
    }

    @Override
    protected Class<Configuration> getEntityClass() {
        return Configuration.class;
    }
}
