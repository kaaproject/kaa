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
import org.springframework.util.Base64Utils;

import java.util.Optional;

import static org.kaaproject.kaa.server.common.dao.service.Validator.validateString;

@Service
public class EndpointSpecificConfigurationServiceImpl implements EndpointSpecificConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointSpecificConfigurationServiceImpl.class);

    private EndpointSpecificConfigurationDao<EndpointSpecificConfiguration> endpointSpecificConfigurationDao;
    @Autowired
    private EndpointService endpointService;
    @Autowired
    private ConfigurationService configurationService;

    @Override
    public Optional<EndpointSpecificConfigurationDto> findByEndpointKeyHash(String endpointKeyHash) {
        LOG.debug("Looking for endpoint specific configuration by EP key hash {}", endpointKeyHash);
        EndpointProfileDto profileDto = getEndpointProfileDto(endpointKeyHash);
        if (profileDto == null) {
            return Optional.empty();
        }
        EndpointSpecificConfiguration configuration = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(endpointKeyHash, profileDto.getConfigurationVersion());
        return Optional.ofNullable(configuration).map(ToDto::toDto);
    }

    @Override
    public Optional<EndpointSpecificConfigurationDto> findByEndpointProfile(EndpointProfileDto endpointProfileDto) {
        String base64EncodedEndpointKeyHash = Base64Utils.encodeToString(endpointProfileDto.getEndpointKeyHash());
        int configurationVersion = endpointProfileDto.getConfigurationVersion();
        EndpointSpecificConfiguration configuration = endpointSpecificConfigurationDao.findByEndpointKeyHashAndConfigurationVersion(base64EncodedEndpointKeyHash, configurationVersion);
        return Optional.ofNullable(configuration).map(ToDto::toDto);

    }

    @Override
    public Optional<EndpointSpecificConfigurationDto> deleteByEndpointKeyHash(String endpointKeyHash) {
        Optional<EndpointSpecificConfigurationDto> configuration = findByEndpointKeyHash(endpointKeyHash);
        if (configuration.isPresent()) {
            endpointSpecificConfigurationDao.removeByEndpointKeyHash(endpointKeyHash);
        }
        return configuration;
    }

    @Override
    public EndpointSpecificConfigurationDto save(EndpointSpecificConfigurationDto configurationDto) {
        EndpointProfileDto profileDto = getEndpointProfileDto(configurationDto.getEndpointKeyHash());
        configurationDto.setConfigurationVersion(profileDto.getConfigurationVersion());
        validateConfigurationBody(configurationDto, profileDto);
        return endpointSpecificConfigurationDao.save(configurationDto).toDto();
    }

    private EndpointProfileDto getEndpointProfileDto(String endpointKeyHash) {
        validateString(endpointKeyHash, "Endpoint key hash is required");
        return endpointService.findEndpointProfileByKeyHash(Base64Utils.decodeFromString(endpointKeyHash));
    }


    private void validateConfigurationBody(EndpointSpecificConfigurationDto configurationDto, EndpointProfileDto ep) {
        validateString(configurationDto.getConfiguration(), "Endpoint specific configuration body is required");
        int configurationVersion = configurationDto.getConfigurationVersion();
        String appId = ep.getApplicationId();
        String configurationBody = configurationDto.getConfiguration();
        configurationBody = configurationService.validateConfiguration(appId, configurationVersion, configurationBody);
        validateString(configurationBody, "Provided configuration body is invalid");
        configurationDto.setConfiguration(configurationBody);
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
