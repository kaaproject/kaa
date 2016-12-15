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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_CONFIGURATION_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SPECIFIC_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_SPECIFIC_CONFIGURATION_KEY_HASH;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.apache.commons.lang.NotImplementedException;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointSpecificConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointSpecificConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class EndpointSpecificConfigurationMongoDao extends AbstractVersionableMongoDao<MongoEndpointSpecificConfiguration, String>
    implements EndpointSpecificConfigurationDao<MongoEndpointSpecificConfiguration> {

  private static final Logger LOG = LoggerFactory.getLogger(EndpointSpecificConfigurationMongoDao.class);

  @Override
  public void removeByEndpointKeyHashAndConfigurationVersion(byte[] endpointKeyHash, Integer confSchemaVersion) {
    LOG.debug("Remove endpoint specific configuration by endpoint key hash [{}] ", endpointKeyHash);
    mongoTemplate.remove(
        query(where(EP_SPECIFIC_CONFIGURATION_KEY_HASH).is(endpointKeyHash)
            .and(EP_CONFIGURATION_VERSION).is(confSchemaVersion)), getCollectionName());
  }

  @Override
  public EndpointSpecificConfiguration findByEndpointKeyHashAndConfigurationVersion(byte[] endpointKeyHash, int configurationVersion) {
    LOG.debug("Try to find endpoint specific configuration by endpointKeyHash {} and configurationVersion {}", endpointKeyHash, configurationVersion);
    EndpointSpecificConfiguration configuration = findOne(query(where(EP_SPECIFIC_CONFIGURATION_KEY_HASH).is(endpointKeyHash)
        .and(EP_CONFIGURATION_VERSION).is(configurationVersion)));
    LOG.trace("Found {}  endpoint specific configuration", configuration);
    return configuration;
  }

  @Override
  public EndpointSpecificConfiguration save(EndpointSpecificConfigurationDto dto) {
    LOG.debug("Saving endpoint specific configuration {}", dto);
    MongoEndpointSpecificConfiguration configuration =
            (MongoEndpointSpecificConfiguration) findByEndpointKeyHashAndConfigurationVersion(dto.getEndpointKeyHash(), dto.getConfigurationSchemaVersion());
    if (configuration != null) {
      dto.setVersion(configuration.getVersion());
    }
    configuration = save(new MongoEndpointSpecificConfiguration(dto));
    if (LOG.isTraceEnabled()) {
      LOG.trace("Saved: {}", configuration);
    } else {
      LOG.debug("Saved: {}", configuration != null);
    }
    return configuration;
  }

  @Override
  public MongoEndpointSpecificConfiguration findById(String key) {
    throw new NotImplementedException();
  }

  @Override
  public void removeById(String key) {
    throw new NotImplementedException();
  }

  @Override
  protected String getCollectionName() {
    return EP_SPECIFIC_CONFIGURATION;
  }

  @Override
  protected Class<MongoEndpointSpecificConfiguration> getDocumentClass() {
    return MongoEndpointSpecificConfiguration.class;
  }
}
