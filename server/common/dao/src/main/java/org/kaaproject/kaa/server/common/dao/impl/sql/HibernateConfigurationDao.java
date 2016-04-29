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
import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Configuration;
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
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_SCHEMA_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_SCHEMA_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_SCHEMA_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_ALIAS;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.ENDPOINT_GROUP_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SCHEMA_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.STATUS_PROPERTY;

@Repository
public class HibernateConfigurationDao extends HibernateAbstractDao<Configuration> implements ConfigurationDao<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationDao.class);

    @Override
    public Configuration findConfigurationByAppIdAndVersion(String appId, int version) {
        Configuration configuration = null;
        LOG.debug("Searching configuration by application id [{}] and schema version [{}]", appId, version);
        if (isNotBlank(appId)) {
            configuration = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS, Restrictions.and(
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                    Restrictions.eq(SCHEMA_VERSION_PROPERTY, version),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", appId, version, configuration);
        } else {
            LOG.debug("[{},{}] Search result: {}.", appId, version, configuration != null);
        }
        return configuration;
    }

    @Override
    public Configuration findConfigurationByEndpointGroupIdAndVersion(String groupId, int version) {
        Configuration configuration = null;
        LOG.debug("Searching configuration by endpoint group id [{}] and schema version [{}]", groupId, version);
        if (isNotBlank(groupId)) {
            configuration = findOneByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.eq(SCHEMA_VERSION_PROPERTY, version),
                            Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", groupId, version, configuration);
        } else {
            LOG.debug("[{},{}] Search result: {}.", groupId, version, configuration != null);
        }
        return configuration;
    }

    @Override
    public Configuration findLatestActiveBySchemaIdAndGroupId(String schemaId, String groupId) {
        Configuration configuration = null;
        LOG.debug("Searching latest active configuration by configuration schema id [{}] and group id [{}]", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
            configuration = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", schemaId, groupId, configuration);
        } else {
            LOG.debug("[{},{}] Search result: {}.", schemaId, groupId, configuration != null);
        }
        return configuration;
    }

    @Override
    public Configuration findInactiveBySchemaIdAndGroupId(String schemaId, String groupId) {
        Configuration configuration = null;
        LOG.debug("Searching inactive configuration by configuration schema id [{}] and group id [{}] ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.INACTIVE)));
            configuration = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", schemaId, groupId, configuration);
        } else {
            LOG.debug("[{},{}] Search result: {}.", schemaId, groupId, configuration != null);
        }
        return configuration;
    }

    @Override
    public List<Configuration> findActiveByEndpointGroupId(String groupId) {
        List<Configuration> configurations = null;
        LOG.debug("Searching active configurations by endpoint group id [{}] ", groupId);
        if (isNotBlank(groupId)) {
            configurations = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", groupId, Arrays.toString(configurations.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", groupId, configurations.size());
        }
        return configurations;
    }

    @Override
    public List<Configuration> findActualByEndpointGroupId(String groupId) {
        List<Configuration> configurations = null;
        LOG.debug("Searching actual configurations by endpoint group id [{}] ", groupId);
        if (isNotBlank(groupId)) {
            configurations = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                            Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", groupId, Arrays.toString(configurations.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", groupId, configurations.size());
        }
        return configurations;
    }

    @Override
    public List<Configuration> findActualBySchemaIdAndGroupId(String schemaId, String groupId) {
        List<Configuration> configurations = Collections.emptyList();
        LOG.debug("Searching actual configurations by configuration schema id [{}] and group id [{}] ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.ne(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
            configurations = findListByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", schemaId, groupId, Arrays.toString(configurations.toArray()));
        } else {
            LOG.debug("[{},{}] Search result: {}.", schemaId, groupId, configurations.size());
        }
        return configurations;
    }

    @Override
    public Configuration findLatestDeprecated(String schemaId, String groupId) {
        Configuration configuration = null;
        LOG.debug("Searching latest deprecated configurations by configuration schema id [{}] and group id [{}] ", schemaId, groupId);
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            criteria.add(Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.DEPRECATED)));
            criteria.addOrder(Order.desc(SEQUENCE_NUMBER_PROPERTY)).setMaxResults(FIRST);
            configuration = findOneByCriteria(criteria);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{},{}] Search result: {}.", schemaId, groupId, configuration);
        } else {
            LOG.debug("[{},{}] Search result: {}.", schemaId, groupId, configuration != null);
        }
        return configuration;
    }

    @Override
    public void removeByConfigurationSchemaId(String schemaId) {
        if (isNotBlank(schemaId)) {
            List<Configuration> configurations = findListByCriterionWithAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS,
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)));
            removeList(configurations);
        }
        LOG.debug("Removed configurations by configuration schema id [{}]", schemaId);
    }

    @Override
    public void removeByEndpointGroupId(String groupId) {
        if (isNotBlank(groupId)) {
            List<Configuration> configurations = findListByCriterionWithAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS,
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)));
            removeList(configurations);
        }
        LOG.debug("Removed configurations by endpoint group id [{}]", groupId);
    }

    @Override
    public Configuration activate(String id, String username) {
        LOG.debug("Activating configuration with id [{}] by user [{}]", id, username);
        Configuration configuration = findById(id);
        if (configuration != null) {
            configuration.setStatus(UpdateStatus.ACTIVE);
            configuration.setSequenceNumber(configuration.getSequenceNumber() + 1);
            configuration.setActivatedUsername(username);
            configuration.setActivatedTime(System.currentTimeMillis());
            save(configuration);
        }
        LOG.debug("[{},{}] Configuration activated.", id, username);
        return configuration;
    }

    @Override
    public Configuration deactivate(String id, String username) {
        LOG.debug("Deactivating configuration with id [{}] by user [{}]", id, username);
        Configuration configuration = findById(id);
        if (configuration != null) {
            configuration.setStatus(UpdateStatus.DEPRECATED);
            configuration.setDeactivatedUsername(username);
            configuration.setDeactivatedTime(System.currentTimeMillis());
            save(configuration);
        }
        LOG.debug("[{},{}] Configuration deactivated.", id, username);
        return configuration;
    }

    @Override
    public Configuration deactivateOldConfiguration(String schemaId, String groupId, String username) {
        LOG.debug("Deactivating old configurations by configuration schema id [{}] and endpoint group id [{}] ", schemaId, groupId);
        Configuration configuration = null;
        if (isNotBlank(schemaId) && isNotBlank(groupId)) {
            Criteria criteria = getCriteria();
            criteria.createAlias(CONFIGURATION_SCHEMA_PROPERTY, CONFIGURATION_SCHEMA_ALIAS);
            criteria.createAlias(ENDPOINT_GROUP_PROPERTY, ENDPOINT_GROUP_ALIAS);
            Criterion criterion = Restrictions.and(
                    Restrictions.eq(ENDPOINT_GROUP_REFERENCE, Long.valueOf(groupId)),
                    Restrictions.eq(CONFIGURATION_SCHEMA_REFERENCE, Long.valueOf(schemaId)),
                    Restrictions.eq(STATUS_PROPERTY, UpdateStatus.ACTIVE));
            criteria.add(criterion);
            configuration = findOneByCriteria(criteria);
            if (configuration != null) {
                configuration.setDeactivatedUsername(username);
                configuration.setDeactivatedTime(System.currentTimeMillis());
                configuration.setStatus(UpdateStatus.DEPRECATED);
                save(configuration);
            }
        }
        LOG.debug("[{},{},{}] Configuration deactivated.", schemaId, groupId, username);
        return configuration;
    }

    @Override
    public Configuration save(Configuration o) {
        Configuration saved = super.save(o);
        getSession().flush();
        return saved;
    }

    @Override
    protected Class<Configuration> getEntityClass() {
        return Configuration.class;
    }
}
