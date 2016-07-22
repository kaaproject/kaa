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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.common.dto.UpdateStatus.ACTIVE;
import static org.kaaproject.kaa.common.dto.UpdateStatus.INACTIVE;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.idToString;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.validateSqlObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeNotificationDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaCreationException;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactory;
import org.kaaproject.kaa.server.common.core.algorithms.validator.DefaultUuidValidator;
import org.kaaproject.kaa.server.common.core.algorithms.validator.UuidValidator;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.BaseDataFactory;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;
import org.kaaproject.kaa.server.common.core.configuration.OverrideDataFactory;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;
import org.kaaproject.kaa.server.common.core.schema.ProtocolSchema;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Configuration;
import org.kaaproject.kaa.server.common.dao.model.sql.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private static final String DEFAULT_STRUCT_DESC = "Generated";

    @Autowired
    private ConfigurationDao<Configuration> configurationDao;

    @Autowired
    private EndpointGroupDao<EndpointGroup> endpointGroupDao;

    @Autowired
    private ConfigurationSchemaDao<ConfigurationSchema> configurationSchemaDao;

    @Autowired
    private HistoryService historyService;


    @Autowired
    private CTLService ctlService;

    @Autowired
    private SchemaGenerationAlgorithmFactory schemaGeneratorFactory;

    private UuidValidator uuidValidator;

    @Override
    @Deprecated
    public ConfigurationDto findConfigurationByAppIdAndVersion(String applicationId, int version) {
        validateSqlId(applicationId, "Application id is incorrect. Can't find configuration by application id " + applicationId
                + " and version " + version);
        return getDto(configurationDao.findConfigurationByAppIdAndVersion(applicationId, version));
    }

    @Override
    public ConfigurationDto findConfigurationByEndpointGroupIdAndVersion(String endpointGroupId, int version) {
        validateId(endpointGroupId, "Endpoint group id is incorrect. Can't find configuration by endpoint group id " + endpointGroupId
                + " and version " + version);
        return getDto(configurationDao.findConfigurationByEndpointGroupIdAndVersion(endpointGroupId, version));
    }

    @Override
    public ConfigurationDto findDefaultConfigurationBySchemaId(String schemaId) {
        validateId(schemaId, "Schema id is incorrect. Can't find default configuration by schema id " + schemaId);
        ConfigurationDto configuration = null;
        ConfigurationSchema configurationSchema = configurationSchemaDao.findById(schemaId);
        if (configurationSchema != null) {
            String appId = configurationSchema.getApplicationId();
            EndpointGroup endpointGroup = endpointGroupDao.findByAppIdAndWeight(appId, 0);
            if (endpointGroup != null) {
                String endpointGroupId = String.valueOf(endpointGroup.getId());
                configuration = getDto(configurationDao.findConfigurationByEndpointGroupIdAndVersion(endpointGroupId,
                        configurationSchema.getVersion()));
            } else {
                LOG.warn("Can't find default group for application [{}]", appId);
            }
        } else {
            LOG.warn("Can't find configuration schema for id [{}]", schemaId);
        }
        return configuration;
    }

    @Override
    public ConfigurationDto findConfigurationById(String id) {
        validateSqlId(id, "Configuration id is incorrect. Can't find configuration by id " + id);
        return getDto(configurationDao.findById(id));
    }

    @Override
    public Collection<ConfigurationRecordDto> findAllConfigurationRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) {
        Collection<ConfigurationDto> configurations = convertDtoList(configurationDao.findActualByEndpointGroupId(endpointGroupId));
        List<ConfigurationRecordDto> records = ConfigurationRecordDto.convertToConfigurationRecords(configurations);
        if (includeDeprecated) {
            List<VersionDto> schemas = findVacantSchemasByEndpointGroupId(endpointGroupId);
            for (VersionDto schema : schemas) {
                ConfigurationDto deprecatedConfiguration = getDto(configurationDao.findLatestDeprecated(schema.getId(), endpointGroupId));
                if (deprecatedConfiguration != null) {
                    ConfigurationRecordDto record = new ConfigurationRecordDto();
                    record.setActiveStructureDto(deprecatedConfiguration);
                    records.add(record);
                }
            }
        }
        Collections.sort(records);
        return records;
    }

    @Override
    public ConfigurationRecordDto findConfigurationRecordBySchemaIdAndEndpointGroupId(
            String schemaId, String endpointGroupId) {
        ConfigurationRecordDto record = new ConfigurationRecordDto();
        Collection<ConfigurationDto> configurations = convertDtoList(configurationDao.findActualBySchemaIdAndGroupId(schemaId, endpointGroupId));
        if (configurations != null) {
            for (ConfigurationDto configuration : configurations) {
                if (configuration.getStatus() == UpdateStatus.ACTIVE) {
                    record.setActiveStructureDto(configuration);
                } else if (configuration.getStatus() == UpdateStatus.INACTIVE) {
                    record.setInactiveStructureDto(configuration);
                }
            }
        }
        if (!record.hasActive()) {
            ConfigurationDto deprecatedConfiguration = getDto(configurationDao.findLatestDeprecated(schemaId, endpointGroupId));
            if (deprecatedConfiguration != null) {
                record.setActiveStructureDto(deprecatedConfiguration);
            }
        }
        if (record.isEmpty()) {
            LOG.debug("Can't find related Configuration record.");
            throw new NotFoundException("Configuration record not found, schemaId: " + schemaId + ", endpointGroupId: "
                    + endpointGroupId); // NOSONAR
        }
        return record;
    }

    @Override
    public List<VersionDto> findVacantSchemasByEndpointGroupId(String endpointGroupId) {
        validateId(endpointGroupId, "Can't find vacant schemas. Invalid endpoint group id: " + endpointGroupId);
        EndpointGroup group = endpointGroupDao.findById(endpointGroupId);
        List<Configuration> configurations = configurationDao.findActualByEndpointGroupId(endpointGroupId);
        List<String> usedSchemaIds = new ArrayList<>();
        for (Configuration configuration : configurations) {
            ConfigurationSchema schema = configuration.getConfigurationSchema();
            if (schema != null) {
                usedSchemaIds.add(idToString(schema.getId()));
            }
        }
        List<ConfigurationSchema> schemas = configurationSchemaDao.findVacantSchemas(group.getApplicationId(), usedSchemaIds);
        List<VersionDto> schemaDtoList = new ArrayList<>();
        for (ConfigurationSchema schema : schemas) {
            schemaDtoList.add(schema.toVersionDto());
        }
        return schemaDtoList;
    }

    @Override
    public ConfigurationDto saveConfiguration(ConfigurationDto configurationDto) {
        validateConfiguration(configurationDto);
        String id = configurationDto.getId();
        ConfigurationSchemaDto configurationSchemaDto;
        Configuration oldActiveConfiguration = null;
        if (StringUtils.isNotBlank(id)) {
            ConfigurationDto oldConfiguration = findConfigurationById(configurationDto.getId());
            if (oldConfiguration != null && oldConfiguration.getStatus() != INACTIVE) {
                throw new UpdateStatusConflictException("Can't update configuration, invalid id " + id);
            }
            configurationSchemaDto = findConfSchemaById(configurationDto.getSchemaId());
            configurationDto.setSchemaVersion(configurationSchemaDto.getVersion());
            configurationDto.setCreatedTime(oldConfiguration.getCreatedTime());
            configurationDto.setCreatedUsername(oldConfiguration.getCreatedUsername());
            LOG.debug("Update existing configuration with id: [{}]", configurationDto.getId());
        } else {
            String schemaId = configurationDto.getSchemaId();
            String groupId = configurationDto.getEndpointGroupId();
            configurationSchemaDto = findConfSchemaById(schemaId);
            if (configurationSchemaDto != null) {
                Configuration oldInactiveConfiguration = configurationDao.findInactiveBySchemaIdAndGroupId(schemaId, groupId);
                oldActiveConfiguration = configurationDao.findLatestActiveBySchemaIdAndGroupId(schemaId, groupId);
                if (oldInactiveConfiguration != null) {
                    configurationDto.setId(idToString(oldInactiveConfiguration.getId()));
                    configurationDto.setSequenceNumber(oldInactiveConfiguration.getSequenceNumber());
                } else if (oldActiveConfiguration != null) {
                    configurationDto.setSequenceNumber(oldActiveConfiguration.getSequenceNumber());
                }
                configurationDto.setApplicationId(configurationSchemaDto.getApplicationId());
                configurationDto.setSchemaVersion(configurationSchemaDto.getVersion());
                configurationDto.setProtocolSchema(configurationSchemaDto.getProtocolSchema());
                configurationDto.setCreatedTime(System.currentTimeMillis());
            } else {
                LOG.debug("Can't find related Configuration schema.");
                throw new IncorrectParameterException("Configuration schema not found, id:" + schemaId);
            }
        }
        ConfigurationDto oldActiveConfigurationDto = oldActiveConfiguration != null ? oldActiveConfiguration.toDto() : null;
        validateUuids(configurationDto, oldActiveConfigurationDto, configurationSchemaDto);

        configurationDto.setStatus(UpdateStatus.INACTIVE);
        configurationDto.setLastModifyTime(System.currentTimeMillis());
        return getDto(configurationDao.save(new Configuration(configurationDto)));
    }

    private void validateUuids(ConfigurationDto currentConfiguration, ConfigurationDto previousConfiguration,
            ConfigurationSchemaDto configurationSchema) {
        try {
            EndpointGroup endpointGroup = endpointGroupDao.findById(currentConfiguration.getEndpointGroupId());
            GenericAvroConverter<GenericRecord> avroConverter;
            KaaData body = null;
            if (endpointGroup != null) {
                if (endpointGroup.getWeight() == 0) {
                    LOG.debug("Create default UUID validator with base schema: {}", configurationSchema.getBaseSchema());
                    BaseSchema baseSchema = new BaseSchema(configurationSchema.getBaseSchema());
                    uuidValidator = new DefaultUuidValidator(baseSchema, new BaseDataFactory());
                    avroConverter = new GenericAvroConverter<GenericRecord>(baseSchema.getRawSchema());
                } else {
                    LOG.debug("Create default UUID validator with override schema: {}", configurationSchema.getOverrideSchema());
                    OverrideSchema overrideSchema = new OverrideSchema(configurationSchema.getOverrideSchema());
                    uuidValidator = new DefaultUuidValidator(overrideSchema, new OverrideDataFactory());
                    avroConverter = new GenericAvroConverter<GenericRecord>(overrideSchema.getRawSchema());
                }
                GenericRecord previousRecord = null;
                if (previousConfiguration != null) {
                    previousRecord = avroConverter.decodeJson(previousConfiguration.getBody());
                }
                GenericRecord currentRecord = avroConverter.decodeJson(currentConfiguration.getBody());
                body = uuidValidator.validateUuidFields(currentRecord, previousRecord);
            }
            if (body != null) {
                currentConfiguration.setBody(body.getRawData());
            } else {
                throw new RuntimeException("Can't generate json configuration body."); // NOSONAR
            }
        } catch (Exception e) {
            LOG.warn("Can't generate uuid fields for configuration {}", currentConfiguration);
            LOG.error("Can't generate uuid fields for configuration!", e);
            throw new IncorrectParameterException("Incorrect configuration. Can't generate uuid fields.");
        }
    }

    @Override
    public ChangeConfigurationNotification activateConfiguration(String id, String activatedUsername) {
        ChangeConfigurationNotification configurationNotification;
        validateSqlId(id, "Incorrect configuration id. Can't activate configuration with id " + id);
        Configuration oldConfiguration = configurationDao.findById(id);
        if (oldConfiguration != null) {
            UpdateStatus status = oldConfiguration.getStatus();
            if (status != null && status == INACTIVE) {
                String schemaId = oldConfiguration.getSchemaId();
                String groupId = oldConfiguration.getEndpointGroupId();
                if (schemaId != null && groupId != null) {
                    configurationDao.deactivateOldConfiguration(schemaId, groupId, activatedUsername);
                } else {
                    throw new DatabaseProcessingException(
                            "Incorrect old configuration. Configuration schema or endpoint group id is empty.");
                }
                ConfigurationDto configurationDto = getDto(configurationDao.activate(id, activatedUsername));
                HistoryDto historyDto = addHistory(configurationDto, ChangeType.ADD_CONF);
                ChangeNotificationDto changeNotificationDto = createNotification(configurationDto, historyDto);
                configurationNotification = new ChangeConfigurationNotification();
                configurationNotification.setConfigurationDto(configurationDto);
                configurationNotification.setChangeNotificationDto(changeNotificationDto);
            } else {
                throw new UpdateStatusConflictException("Incorrect status for activating configuration " + status);
            }
        } else {
            throw new IncorrectParameterException("Can't find configuration with id " + id);
        }
        return configurationNotification;
    }

    @Override
    public ChangeConfigurationNotification deactivateConfiguration(String id, String deactivatedUsername) {
        ChangeConfigurationNotification configurationNotification;
        validateSqlId(id, "Incorrect configuration id. Can't deactivate configuration with id " + id);
        Configuration oldConfiguration = configurationDao.findById(id);
        if (oldConfiguration != null) {
            UpdateStatus status = oldConfiguration.getStatus();
            if (status != null && status == ACTIVE) {
                ConfigurationDto configurationDto = getDto(configurationDao.deactivate(id, deactivatedUsername));
                HistoryDto historyDto = addHistory(configurationDto, ChangeType.REMOVE_CONF);
                ChangeNotificationDto changeNotificationDto = createNotification(configurationDto, historyDto);
                configurationNotification = new ChangeConfigurationNotification();
                configurationNotification.setConfigurationDto(configurationDto);
                configurationNotification.setChangeNotificationDto(changeNotificationDto);
            } else {
                throw new UpdateStatusConflictException("Incorrect status for activating configuration " + status);
            }
        } else {
            throw new IncorrectParameterException("Can't find configuration with id " + id);
        }
        return configurationNotification;
    }

    @Override
    public ChangeConfigurationNotification deleteConfigurationRecord(String schemaId, String groupId, String deactivatedUsername) {
        ChangeConfigurationNotification configurationNotification = null;
        validateSqlId(schemaId, "Incorrect configuration schema id " + schemaId + ".");
        validateSqlId(groupId, "Incorrect group id " + groupId + ".");
        ConfigurationDto configurationDto = getDto(configurationDao.deactivateOldConfiguration(schemaId, groupId, deactivatedUsername));
        if (configurationDto != null) {
            HistoryDto historyDto = addHistory(configurationDto, ChangeType.REMOVE_CONF);
            ChangeNotificationDto changeNotificationDto = createNotification(configurationDto, historyDto);
            configurationNotification = new ChangeConfigurationNotification();
            configurationNotification.setConfigurationDto(configurationDto);
            configurationNotification.setChangeNotificationDto(changeNotificationDto);
        }
        Configuration configuration = configurationDao.findInactiveBySchemaIdAndGroupId(schemaId, groupId);
        if (configuration != null) {
            configurationDao.removeById(idToString(configuration));
        }
        return configurationNotification;
    }

    @Override
    public List<ConfigurationDto> findConfigurationsByEndpointGroupId(String endpointGroupId) {
        validateSqlId(endpointGroupId, "Incorrect endpoint group id " + endpointGroupId);
        return convertDtoList(configurationDao.findActiveByEndpointGroupId(endpointGroupId));
    }

    @Override
    public List<ConfigurationSchemaDto> findConfSchemasByAppId(String applicationId) {
        validateSqlId(applicationId, "Incorrect application id " + applicationId + ". Can't find configuration schemas.");
        return convertDtoList(configurationSchemaDao.findByApplicationId(applicationId));
    }

    @Override
    public List<VersionDto> findConfigurationSchemaVersionsByAppId(String applicationId) {
        validateSqlId(applicationId, "Incorrect application id " + applicationId + ". Can't find configuration schema versions.");
        List<ConfigurationSchema> configurationSchemas = configurationSchemaDao.findByApplicationId(applicationId);
        List<VersionDto> schemas = new ArrayList<>();
        for (ConfigurationSchema configurationSchema : configurationSchemas) {
            schemas.add(configurationSchema.toVersionDto());
        }
        return schemas;
    }

    @Override
    public ConfigurationSchemaDto findConfSchemaByAppIdAndVersion(String applicationId, int version) {
        validateSqlId(applicationId, "Incorrect application id " + applicationId + ". Can't find configuration schema.");
        return getDto(configurationSchemaDao.findByAppIdAndVersion(applicationId, version));
    }

    @Override
    public ConfigurationSchemaDto saveConfSchema(ConfigurationSchemaDto schemaDto, String groupId) {
        ConfigurationSchemaDto savedSchema = saveConfigurationSchema(schemaDto);
        if (savedSchema != null) {
            LOG.debug("Configuration schema with id [{}] saved. Generating default configuration", savedSchema.getId());
            try {
                BaseSchema baseSchema = new BaseSchema(savedSchema.getBaseSchema());
                DefaultRecordGenerationAlgorithm<BaseData> configurationProcessor = new DefaultRecordGenerationAlgorithmImpl<BaseSchema, BaseData>(
                        baseSchema, new BaseDataFactory());
                KaaData body = configurationProcessor.getRootData();
                LOG.debug("Default configuration {} ", body.getRawData());
                ConfigurationDto configurationDto = new ConfigurationDto();
                configurationDto.setBody(body.getRawData());
                configurationDto.setSchemaId(savedSchema.getId());
                configurationDto.setDescription(DEFAULT_STRUCT_DESC);
                configurationDto.setEndpointGroupId(groupId);
                configurationDto.setCreatedUsername(savedSchema.getCreatedUsername());
                ConfigurationDto savedConfiguration = saveConfiguration(configurationDto);
                if (savedConfiguration != null) {
                    activateConfiguration(savedConfiguration.getId(), savedSchema.getCreatedUsername());
                } else {
                    LOG.warn("Can't save default configuration.");
                    removeCascadeConfigurationSchema(savedSchema.getId());
                    throw new IncorrectParameterException("Can't save default configuration.");
                }
            } catch (Exception e) {
                LOG.error("Can't generate configuration based on protocol schema.", e);
                removeCascadeConfigurationSchema(savedSchema.getId());
                throw new IncorrectParameterException("Can't save default configuration.");
            }
        }
        return savedSchema;
    }

    @Override
    public ConfigurationSchemaDto saveConfSchema(ConfigurationSchemaDto configurationSchema) {
        ConfigurationSchemaDto savedConfigSchema = null;
        if (configurationSchema != null) {
            String appId = configurationSchema.getApplicationId();
            if (isValidId(appId)) {
                LOG.debug("Finding default endpoint group for application id [{}]", appId);
                EndpointGroup endpointGroup = endpointGroupDao.findByAppIdAndWeight(appId, 0);
                if (endpointGroup != null) {
                    savedConfigSchema = saveConfSchema(configurationSchema, idToString(endpointGroup));
                } else {
                    LOG.warn("Can't find default group for application [{}]", appId);
                }
            } else {
                LOG.warn("Can't find endpoint group. Invalid application id [{}]", appId);
            }
        } else {
            LOG.warn("Configuration schema object is null");
        }
        return savedConfigSchema;
    }

    @Override
    public ConfigurationSchemaDto findConfSchemaById(String id) {
        validateSqlId(id, "Incorrect configuration schema id " + id + ". Can't find configuration schema.");
        return getDto(configurationSchemaDao.findById(id));
    }

    @Override
    public void removeConfSchemasByAppId(String appId) {
        validateSqlId(appId, "Incorrect application id " + appId + ". Can't remove configuration schema.");
        LOG.debug("Removing configuration schemas and correspond configuration by application id");
        List<ConfigurationSchema> configurationSchemaList = configurationSchemaDao.findByApplicationId(appId);
        for (ConfigurationSchema configurationSchema : configurationSchemaList) {
            if (configurationSchema != null) {
                removeCascadeConfigurationSchema(idToString(configurationSchema));
            }
        }
    }

    private ChangeNotificationDto createNotification(ConfigurationDto configurationDto, HistoryDto historyDto) {
        LOG.debug("Create notification after configuration update.");
        ChangeNotificationDto changeNotificationDto = null;
        if (historyDto != null) {
            changeNotificationDto = new ChangeNotificationDto();
            changeNotificationDto.setAppId(configurationDto.getApplicationId());
            changeNotificationDto.setAppSeqNumber(historyDto.getSequenceNumber());
            String endpointGroupId = configurationDto.getEndpointGroupId();
            if (isValidId(endpointGroupId)) {
                EndpointGroup group = endpointGroupDao.findById(endpointGroupId);
                if (group != null) {
                    changeNotificationDto.setGroupId(idToString(group));
                    changeNotificationDto.setGroupSeqNumber(group.getSequenceNumber());
                } else {
                    LOG.debug("Can't find endpoint group by id [{}].", endpointGroupId);
                }
            } else {
                LOG.debug("Incorrect endpoint group id [{}].", endpointGroupId);
            }
        } else {
            LOG.debug("Can't save history information.");
        }
        return changeNotificationDto;
    }

    private void generateSchemas(ConfigurationSchemaDto schema) throws SchemaCreationException {
        CTLSchemaDto ctlSchema = ctlService.findCTLSchemaById(schema.getCtlSchemaId());
        String sch = ctlService.flatExportAsString(ctlSchema);
        DataSchema dataSchema = new DataSchema(sch);
        if (!dataSchema.isEmpty()) {
            SchemaGenerationAlgorithm schemaGenerator = schemaGeneratorFactory.createSchemaGenerator(dataSchema);
            ProtocolSchema protocol = schemaGenerator.getProtocolSchema();
            BaseSchema base = schemaGenerator.getBaseSchema();
            OverrideSchema override = schemaGenerator.getOverrideSchema();
            if (!protocol.isEmpty() && !base.isEmpty() && !override.isEmpty()) {
                schema.setBaseSchema(base.getRawSchema());
                schema.setProtocolSchema(protocol.getRawSchema());
                schema.setOverrideSchema(override.getRawSchema());
            } else {
                LOG.trace("One or more generated schemas are empty. base: {} protocol {}  override {}", base, protocol, override);
                throw new IncorrectParameterException("Can't generate schemas. Check your data schema");
            }
        } else {
            LOG.warn("Can't generate schemas because data schema is empty.");
        }
    }

    private void removeCascadeConfigurationSchema(String id) {
        LOG.debug("Removing configurations and configuration schema by id " + id);
        // configurationDao.removeByConfigurationSchemaId(id);
        configurationSchemaDao.removeById(id);
    }

    private HistoryDto addHistory(ConfigurationDto dto, ChangeType type) {
        LOG.debug("Add history information about configuration update");
        HistoryDto history = new HistoryDto();
        history.setApplicationId(dto.getApplicationId());
        ChangeDto change = new ChangeDto();
        change.setConfigurationId(dto.getId());
        change.setCfVersion(dto.getSchemaVersion());
        change.setEndpointGroupId(dto.getEndpointGroupId());
        change.setType(type);
        history.setChange(change);
        return historyService.saveHistory(history);
    }

    private void validateConfiguration(ConfigurationDto dto) {
        validateSqlObject(dto, "Can't save configuration, object is invalid.");
        validateSqlId(dto.getSchemaId(), "Configuration object invalid. Incorrect configuration schema id : " + dto.getSchemaId());
        validateSqlId(dto.getEndpointGroupId(), "Configuration object invalid. Incorrect endpoint group id : " + dto.getEndpointGroupId());
    }

    private ConfigurationSchemaDto saveConfigurationSchema(ConfigurationSchemaDto configurationSchema) {
        ConfigurationSchemaDto configurationSchemaDto;
        validateSqlObject(configurationSchema, "Can't save configuration schema. Configuration schema invalid.");
        String id = configurationSchema.getId();
        if (isBlank(id)) {
            ConfigurationSchemaDto oldConfigurationSchemaDto = findLatestConfSchemaByAppId(configurationSchema.getApplicationId());
            int version = 0;
            if (oldConfigurationSchemaDto != null) {
                version = oldConfigurationSchemaDto.getVersion();
            }
            configurationSchema.setVersion(++version);
            configurationSchema.setCreatedTime(System.currentTimeMillis());
            try {
                generateSchemas(configurationSchema);
            } catch (SchemaCreationException e) {
                LOG.warn("Can't generate protocol schema from configuration schema.", e);
                throw new IncorrectParameterException("Incorrect configuration schema. Can't generate protocol schema.");
            }
        } else {
            ConfigurationSchemaDto oldConfigurationSchemaDto = getDto(configurationSchemaDao.findById(id));
            if (oldConfigurationSchemaDto != null) {
                oldConfigurationSchemaDto.editFields(configurationSchema);
                configurationSchema = oldConfigurationSchemaDto;
            } else {
                LOG.error("Can't find configuration schema with given id [{}].", id);
                throw new IncorrectParameterException("Invalid configuration schema id: " + id);
            }
        }
        configurationSchemaDto = getDto(configurationSchemaDao.save(new ConfigurationSchema(configurationSchema)));
        return configurationSchemaDto;
    }

    private ConfigurationSchemaDto findLatestConfSchemaByAppId(String applicationId) {
        validateSqlId(applicationId, "Incorrect application id " + applicationId + ". Can't find latest configuration schema.");
        return getDto(configurationSchemaDao.findLatestByApplicationId(applicationId));
    }
}
