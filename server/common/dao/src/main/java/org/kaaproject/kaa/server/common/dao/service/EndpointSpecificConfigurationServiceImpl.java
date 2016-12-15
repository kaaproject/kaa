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

package org.kaaproject.kaa.server.common.dao.service;

import static org.kaaproject.kaa.server.common.dao.service.Validator.validateNotNull;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateString;

import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EndpointSpecificConfigurationService;
import org.kaaproject.kaa.server.common.dao.impl.EndpointSpecificConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointSpecificConfiguration;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EndpointSpecificConfigurationServiceImpl implements EndpointSpecificConfigurationService {

  private static final Logger LOG = LoggerFactory.getLogger(EndpointSpecificConfigurationServiceImpl.class);

  private EndpointSpecificConfigurationDao<EndpointSpecificConfiguration> endpointSpecificConfigurationDao;
  @Autowired
  private EndpointService endpointService;
  @Autowired
  private ConfigurationService configurationService;

  @Override
  public Optional<EndpointSpecificConfigurationDto> findActiveConfigurationByEndpointKeyHash(byte[] endpointKeyHash) {
    LOG.debug("Looking for active endpoint specific configuration by EP key hash {}", endpointKeyHash);
    EndpointProfileDto profileDto = getEndpointProfileDto(endpointKeyHash);
    if (profileDto == null) {
      return Optional.empty();
    }
    EndpointSpecificConfiguration configuration = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(endpointKeyHash,
        profileDto.getConfigurationVersion());
    return Optional.ofNullable(configuration).map(ToDto::toDto);
  }

  @Override
  public Optional<EndpointSpecificConfigurationDto> findByEndpointKeyHashAndConfSchemaVersion(byte[] endpointKeyHash, Integer confSchemaVersion) {
    LOG.debug("Looking for endpoint specific configuration by EP key hash {} and confSchemaVersion", endpointKeyHash, confSchemaVersion);
    validateNotNull(confSchemaVersion, "Configuration schema version is required");
    EndpointProfileDto profileDto = getEndpointProfileDto(endpointKeyHash);
    if (profileDto == null) {
      return Optional.empty();
    }
    validateConfSchemaVersion(profileDto.getApplicationId(), confSchemaVersion);
    EndpointSpecificConfiguration configuration = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(endpointKeyHash,
        confSchemaVersion);
    return Optional.ofNullable(configuration).map(ToDto::toDto);
  }

  @Override
  public Optional<EndpointSpecificConfigurationDto> findActiveConfigurationByEndpointProfile(EndpointProfileDto endpointProfileDto) {
    byte[] endpointKeyHash = endpointProfileDto.getEndpointKeyHash();
    int configurationVersion = endpointProfileDto.getConfigurationVersion();
    EndpointSpecificConfiguration configuration = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(endpointKeyHash,
        configurationVersion);
    return Optional.ofNullable(configuration).map(ToDto::toDto);
  }

  @Override
  public Optional<EndpointSpecificConfigurationDto> deleteActiveConfigurationByEndpointKeyHash(byte[] endpointKeyHash) {
    Optional<EndpointSpecificConfigurationDto> configuration = findActiveConfigurationByEndpointKeyHash(endpointKeyHash);
    if (configuration.isPresent()) {
      int confSchemaVersion = configuration.get().getConfigurationSchemaVersion();
      endpointSpecificConfigurationDao.removeByEndpointKeyHashAndConfigurationVersion(endpointKeyHash, confSchemaVersion);
    }
    return configuration;
  }

  @Override
  public Optional<EndpointSpecificConfigurationDto> deleteByEndpointKeyHashAndConfSchemaVersion(byte[] endpointKeyHash, Integer confSchemaVersion) {
    Optional<EndpointSpecificConfigurationDto> configuration = findByEndpointKeyHashAndConfSchemaVersion(endpointKeyHash, confSchemaVersion);
    if (configuration.isPresent()) {
      endpointSpecificConfigurationDao.removeByEndpointKeyHashAndConfigurationVersion(endpointKeyHash, confSchemaVersion);
    }
    return configuration;
  }

  @Override
  public EndpointSpecificConfigurationDto save(EndpointSpecificConfigurationDto configurationDto) {
    EndpointProfileDto profileDto = getEndpointProfileDto(configurationDto.getEndpointKeyHash());
    if (configurationDto.getConfigurationSchemaVersion() == null) {
      configurationDto.setConfigurationSchemaVersion(profileDto.getConfigurationVersion());
    }
    validateEndpointSpecificConfiguration(configurationDto, profileDto.getApplicationId());
    return endpointSpecificConfigurationDao.save(configurationDto).toDto();
  }

  private EndpointProfileDto getEndpointProfileDto(byte[] endpointKeyHash) {
    validateNotNull(endpointKeyHash, "Endpoint key hash is required");
    return endpointService.findEndpointProfileByKeyHash(endpointKeyHash);
  }

  private void validateEndpointSpecificConfiguration(EndpointSpecificConfigurationDto configurationDto, String appId) {
    validateString(configurationDto.getConfiguration(), "Endpoint specific configuration body is required");
    int configurationVersion = configurationDto.getConfigurationSchemaVersion();
    String configurationBody = configurationDto.getConfiguration();
    configurationBody = configurationService.normalizeAccordingToOverrideConfigurationSchema(appId, configurationVersion, configurationBody);
    configurationDto.setConfiguration(configurationBody);
  }

  private void validateConfSchemaVersion(String appId, int schemaVersion) {
    ConfigurationSchemaDto confSchema = configurationService.findConfSchemaByAppIdAndVersion(appId, schemaVersion);
    validateNotNull(confSchema, "Configuration schema with provided version doesn't exists.");
  }

  public void setEndpointSpecificConfigurationDao(EndpointSpecificConfigurationDao<EndpointSpecificConfiguration> endpointSpecificConfigurationDao) {
    this.endpointSpecificConfigurationDao = endpointSpecificConfigurationDao;
  }

  public void setEndpointService(EndpointService endpointService) {
    this.endpointService = endpointService;
  }

  public void setConfigurationService(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }
}
