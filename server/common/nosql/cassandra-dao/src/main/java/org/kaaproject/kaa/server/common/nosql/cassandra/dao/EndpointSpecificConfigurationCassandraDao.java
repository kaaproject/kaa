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
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EPS_CONFIGURATION_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_CONFIGURATION_VERSION_PROPERTY;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select;
import org.apache.commons.lang.NotImplementedException;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointSpecificConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointSpecificConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class EndpointSpecificConfigurationCassandraDao extends AbstractVersionableCassandraDao<CassandraEndpointSpecificConfiguration, String>
    implements EndpointSpecificConfigurationDao<CassandraEndpointSpecificConfiguration> {

  private static final Logger LOG = LoggerFactory.getLogger(EndpointSpecificConfigurationCassandraDao.class);

  @Override
  protected Class<CassandraEndpointSpecificConfiguration> getColumnFamilyClass() {
    return CassandraEndpointSpecificConfiguration.class;
  }

  @Override
  protected String getColumnFamilyName() {
    return EPS_CONFIGURATION_COLUMN_FAMILY_NAME;
  }

  @Override
  public void removeById(String id) {
    throw new NotImplementedException();
  }

  @Override
  public CassandraEndpointSpecificConfiguration findById(String id) {
    throw new NotImplementedException();
  }

  @Override
  public void removeByEndpointKeyHashAndConfigurationVersion(byte[] endpointKeyHash, Integer confSchemaVersion) {
    LOG.debug("Remove endpoint specific configuration by endpointKeyHash {} and confSchemaVersion {}", endpointKeyHash, confSchemaVersion);
    Delete.Where deleteQuery = delete().from(getColumnFamilyName())
        .where(eq(EPS_CONFIGURATION_KEY_HASH_PROPERTY, getByteBuffer(endpointKeyHash)))
        .and(eq(EP_CONFIGURATION_VERSION_PROPERTY, confSchemaVersion));
    LOG.trace("Remove endpoint specific configuration by endpointKeyHash and confSchemaVersion query {}", deleteQuery);
    execute(deleteQuery);
  }

  @Override
  public CassandraEndpointSpecificConfiguration findByEndpointKeyHashAndConfigurationVersion(byte[] endpointKeyHash, int configurationVersion) {
    LOG.debug("Try to find endpoint specific configuration by endpointKeyHash {} and configurationVersion {}", endpointKeyHash, configurationVersion);
    Select.Where where = select().from(getColumnFamilyName())
        .where(eq(EPS_CONFIGURATION_KEY_HASH_PROPERTY, getByteBuffer(endpointKeyHash)))
        .and(eq(EP_CONFIGURATION_VERSION_PROPERTY, configurationVersion));
    LOG.trace("Try to find endpoint specific configuration by cql select {}", where);
    CassandraEndpointSpecificConfiguration configuration = findOneByStatement(where);
    LOG.trace("Found {} endpoint specific configuration", configuration);
    return configuration;
  }

  @Override
  public EndpointSpecificConfiguration save(EndpointSpecificConfigurationDto dto) {
    LOG.debug("Saving endpoint specific configuration {}", dto);
    CassandraEndpointSpecificConfiguration configuration =
            findByEndpointKeyHashAndConfigurationVersion(dto.getEndpointKeyHash(), dto.getConfigurationSchemaVersion());
    if (configuration != null) {
      dto.setVersion(configuration.getVersion());
    }
    configuration = save(new CassandraEndpointSpecificConfiguration(dto));
    if (LOG.isTraceEnabled()) {
      LOG.trace("Saved: {}", configuration);
    } else {
      LOG.debug("Saved: {}", configuration != null);
    }
    return configuration;

  }

}
