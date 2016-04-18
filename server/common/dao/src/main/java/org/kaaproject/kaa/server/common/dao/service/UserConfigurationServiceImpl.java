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

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.core.algorithms.validator.DefaultUuidValidator;
import org.kaaproject.kaa.server.common.core.algorithms.validator.UuidValidator;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;
import org.kaaproject.kaa.server.common.core.configuration.OverrideData;
import org.kaaproject.kaa.server.common.core.configuration.OverrideDataFactory;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.UserConfigurationService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserConfigurationDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;

@Service
public class UserConfigurationServiceImpl implements UserConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserConfigurationServiceImpl.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ConfigurationService configurationService;

    private EndpointUserConfigurationDao<EndpointUserConfiguration> endpointUserConfigurationDao;


    @Override
    public EndpointUserConfigurationDto saveUserConfiguration(EndpointUserConfigurationDto userConfig) {
        EndpointUserConfigurationDto userConfigurationDto = null;
        if (userConfig != null) {
            String userConfigBody = userConfig.getBody();
            if (isNotBlank(userConfigBody)) {
                String appToken = userConfig.getAppToken();
                ApplicationDto applicationDto = applicationService.findAppByApplicationToken(appToken);
                if (applicationDto != null) {
                    int schemaVersion = userConfig.getSchemaVersion();
                    ConfigurationSchemaDto schemaDto = configurationService.findConfSchemaByAppIdAndVersion(applicationDto.getId(), schemaVersion);
                    if (schemaDto != null) {
                        OverrideSchema overrideSchema = new OverrideSchema(schemaDto.getOverrideSchema());
                        LOG.debug("Create default UUID validator with override schema: {}", overrideSchema.getRawSchema());
                        UuidValidator<OverrideData> uuidValidator = new DefaultUuidValidator<>(overrideSchema, new OverrideDataFactory());
                        GenericAvroConverter<GenericRecord> avroConverter = new GenericAvroConverter<>(overrideSchema.getRawSchema());
                        try {
                            GenericRecord configRecord = avroConverter.decodeJson(userConfigBody);
                            // TODO: Need to use last active configuration instead of null. Will be changed after supporting delta configuration
                            KaaData<OverrideSchema> body = uuidValidator.validateUuidFields(configRecord, null);
                            if (body != null) {
                                userConfig.setBody(body.getRawData());
                                userConfigurationDto = getDto(endpointUserConfigurationDao.save(userConfig));
                            } else {
                                LOG.warn("Validated endpoint user configuration body is empty");
                                throw new IncorrectParameterException("Validated endpoint user configuration body is empty");
                            }
                        } catch (IOException e) {
                            LOG.error("Invalid endpoint user configuration for override schema.", e);
                            throw new IncorrectParameterException("Invalid endpoint user configuration for override schema.");
                        }
                    } else {
                        LOG.warn("Can't find configuration schema with version {} for endpoint user configuration.", schemaVersion);
                        throw new IncorrectParameterException("Can't find configuration schema for specified version.");
                    }
                } else {
                    LOG.warn("Can't find application with token {} for endpoint user configuration.", appToken);
                    throw new IncorrectParameterException("Can't find application for specified token.");
                }
            } else {
                LOG.warn("Invalid endpoint user configuration. Configuration body is empty");
                throw new IncorrectParameterException("Configuration body is empty.");
            }
        }
        return userConfigurationDto;
    }

    @Override
    public EndpointUserConfigurationDto findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        return getDto(endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(userId, appToken, schemaVersion));
    }

    @Override
    public List<EndpointUserConfigurationDto> findUserConfigurationByUserId(String userId) {
        return convertDtoList(endpointUserConfigurationDao.findByUserId(userId));
    }

    @Override
    public void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion) {
        endpointUserConfigurationDao.removeByUserIdAndAppTokenAndSchemaVersion(userId, appToken, schemaVersion);
    }


    public void setEndpointUserConfigurationDao(EndpointUserConfigurationDao<EndpointUserConfiguration> endpointUserConfigurationDao) {
        this.endpointUserConfigurationDao = endpointUserConfigurationDao;
    }
}
