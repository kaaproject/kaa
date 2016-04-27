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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_APP_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_VERSION_PROPERTY;

import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.querybuilder.Select;

@Repository
public class EndpointUserConfigurationCassandraDao extends AbstractCassandraDao<CassandraEndpointUserConfiguration, String> implements EndpointUserConfigurationDao<CassandraEndpointUserConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserConfigurationCassandraDao.class);

    @Override
    protected Class<CassandraEndpointUserConfiguration> getColumnFamilyClass() {
        return CassandraEndpointUserConfiguration.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_USER_CONF_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointUserConfiguration save(EndpointUserConfigurationDto dto) {
        LOG.debug("Saving user specific configuration {}", dto);
        CassandraEndpointUserConfiguration userConfiguration = save(new CassandraEndpointUserConfiguration(dto));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Saving result: {}", userConfiguration);
        } else {
            LOG.debug("Saving result: {}", userConfiguration != null);
        }
        return userConfiguration;
    }

    @Override
    public CassandraEndpointUserConfiguration findByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        LOG.debug("Searching for user specific configuration by user id {}, application token {} and schema version {}", userId, appToken, schemaVersion);
        Select.Where select = select().from(getColumnFamilyName()).where(eq(EP_USER_CONF_USER_ID_PROPERTY, userId))
                .and(eq(EP_USER_CONF_APP_TOKEN_PROPERTY, appToken)).and(eq(EP_USER_CONF_VERSION_PROPERTY, schemaVersion));
        CassandraEndpointUserConfiguration userConfiguration = findOneByStatement(select);
        if (LOG.isTraceEnabled()) {
            LOG.debug("[{},{},{}] Search result: {}.", userId, appToken, schemaVersion, userConfiguration);
        } else {
            LOG.debug("[{},{},{}] Search result: {}.", userId, appToken, schemaVersion, userConfiguration != null);
        }
        return userConfiguration;
    }

    @Override
    public List<CassandraEndpointUserConfiguration> findByUserId(String userId) {
        LOG.debug("Searching for user specific configurations by user id {}", userId);
        Select.Where select = select().from(getColumnFamilyName()).where(eq(EP_USER_CONF_USER_ID_PROPERTY, userId));
        List<CassandraEndpointUserConfiguration> configurationList = findListByStatement(select);
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Search result: {}.", userId, Arrays.toString(configurationList.toArray()));
        } else {
            LOG.debug("[{}] Search result: {}.", userId, configurationList.size());
        }
        return configurationList;
    }

    @Override
    public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        execute(delete().from(getColumnFamilyName())
                .where(eq(EP_USER_CONF_USER_ID_PROPERTY, userId))
                .and(eq(EP_USER_CONF_APP_TOKEN_PROPERTY, appToken))
                .and(eq(EP_USER_CONF_VERSION_PROPERTY, schemaVersion)));
        LOG.debug("Removed user specific configuration by user id {}, application token {} and schema version {}", userId, appToken, schemaVersion);
    }


}
