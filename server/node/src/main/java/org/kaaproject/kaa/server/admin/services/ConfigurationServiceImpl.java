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

package org.kaaproject.kaa.server.admin.services;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hibernate.StaleObjectStateException;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConfigurationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.services.ConfigurationService;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.GroupService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("configurationService")
public class ConfigurationServiceImpl extends AbstractAdminService implements ConfigurationService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    @Autowired
    GroupService groupService;

    @Autowired
    CtlService ctlService;

    @Override
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getConfigurationSchemasByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getConfigurationSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationSchemaDto configurationSchema = controlService.getConfigurationSchema(configurationSchemaId);
            Utils.checkNotNull(configurationSchema);
            checkApplicationId(configurationSchema.getApplicationId());
            return configurationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto saveConfigurationSchema(ConfigurationSchemaDto confSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(confSchema.getId())) {
                confSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(confSchema.getApplicationId());
            } else {
                ConfigurationSchemaDto storedConfSchema = controlService.getConfigurationSchema(confSchema.getId());
                Utils.checkNotNull(storedConfSchema);
                checkApplicationId(storedConfSchema.getApplicationId());
            }
            return controlService.editConfigurationSchema(confSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaViewDto saveConfigurationSchemaView(ConfigurationSchemaViewDto confSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationSchemaDto confSchema = confSchemaView.getSchema();
            String applicationId = confSchema.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = confSchema.getCtlSchemaId();

            if (isEmpty(ctlSchemaId)) {
                if (confSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = confSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = ctlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(),
                            metaInfo.getVersion(),
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    confSchema.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = ctlService.saveCTLSchemaForm(confSchemaView.getCtlSchemaForm(), ConverterType.CONFIGURATION_FORM_AVRO_CONVERTER);
                    confSchema.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }

            ConfigurationSchemaDto savedConfSchema = saveConfigurationSchema(confSchema);
            return getConfigurationSchemaView(savedConfSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaViewDto getConfigurationSchemaView(String configurationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationSchemaDto confSchema = getConfigurationSchema(configurationSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(confSchema.getCtlSchemaId());
            return new ConfigurationSchemaViewDto(confSchema, toCtlSchemaForm(ctlSchemaDto, ConverterType.CONFIGURATION_FORM_AVRO_CONVERTER));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(String endpointGroupId,
                                                                                 boolean includeDeprecated) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            ConfigurationRecordDto record = controlService.getConfigurationRecord(schemaId, endpointGroupId);
            return record;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = getCurrentUser().getUsername();
            if (isEmpty(configuration.getId())) {
                configuration.setCreatedUsername(username);
                checkEndpointGroupId(configuration.getEndpointGroupId());
            } else {
                configuration.setModifiedUsername(username);
                ConfigurationDto storedConfiguration = controlService.getConfiguration(configuration.getId());
                Utils.checkNotNull(storedConfiguration);
                checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
            }
            return controlService.editConfiguration(configuration);
        } catch (StaleObjectStateException e) {
            LOG.error("Someone has already updated the configuration. Reload page to be able to edit it. ", e);
            throw new KaaAdminServiceException("Someone has already updated the configuration. Reload page to be able to edit it.",
                    ServiceErrorCode.GENERAL_ERROR);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationToken(endpointUserConfiguration.getAppToken());
            controlService.editUserConfiguration(endpointUserConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto activateConfiguration(String configurationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = controlService.getConfiguration(configurationId);
            Utils.checkNotNull(storedConfiguration);
            checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            return controlService.activateConfiguration(configurationId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto deactivateConfiguration(String configurationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = controlService.getConfiguration(configurationId);
            Utils.checkNotNull(storedConfiguration);
            checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            return controlService.deactivateConfiguration(configurationId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            StructureRecordDto<ConfigurationDto> record = controlService.getConfigurationRecord(schemaId, endpointGroupId);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            controlService.deleteConfigurationRecord(schemaId, endpointGroupId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateConfigurationSchemaForm(String fileItemName) throws KaaAdminServiceException {
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            validateRecordSchema(schema);
            return configurationSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordViewDto getConfigurationRecordView(String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkSchemaId(schemaId);
            ConfigurationRecordDto record = getConfigurationRecord(schemaId, endpointGroupId);
            return toConfigurationRecordView(record);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordFormDto editConfigurationRecordForm(ConfigurationRecordFormDto configuration) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto toSave = toConfigurationDto(configuration);
            ConfigurationDto stored = editConfiguration(toSave);
            return toConfigurationRecordFormDto(stored);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordFormDto activateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = activateConfiguration(configurationId);
            return toConfigurationRecordFormDto(storedConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = deactivateConfiguration(configurationId);
            return toConfigurationRecordFormDto(storedConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField getConfigurationRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            byte[] body = getFileContent(fileItemName);

            JsonNode json = new ObjectMapper().readTree(body);
            json = injectUuids(json);
            body = json.toString().getBytes();

            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
            GenericRecord record = converter.decodeJson(body);
            RecordField recordData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
            return recordData;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getUserConfigurationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            List<ConfigurationSchemaDto> configurationSchemas = controlService.getConfigurationSchemasByApplicationId(applicationId);
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(configurationSchemas.size());
            for (ConfigurationSchemaDto configurationSchema : configurationSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(configurationSchema);
                Schema schema = new Schema.Parser().parse(configurationSchema.getOverrideSchema());
                RecordField schemaForm = FormAvroConverter.createRecordFieldFromSchema(schema);
                schemaInfo.setSchemaForm(schemaForm);
                schemaInfos.add(schemaInfo);
            }
            return schemaInfos;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration, String applicationId,
                                      RecordField configurationData) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationDto application = checkApplicationId(applicationId);
            endpointUserConfiguration.setAppToken(application.getApplicationToken());
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(configurationData);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            String body = converter.encodeToJson(record);
            endpointUserConfiguration.setBody(body);
            controlService.editUserConfiguration(endpointUserConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getVacantConfigurationSchemaInfosByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointGroupDto endpointGroup = checkEndpointGroupId(endpointGroupId);
            List<VersionDto> schemas = getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
            return toConfigurationSchemaInfos(schemas, endpointGroup);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaViewDto createConfigurationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            ConfigurationSchemaDto confSchema = new ConfigurationSchemaDto();
            confSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            confSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            confSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = ctlService.saveCTLSchemaForm(ctlSchemaForm, ConverterType.CONFIGURATION_FORM_AVRO_CONVERTER);
            confSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            ConfigurationSchemaDto savedConfSchema = saveConfigurationSchema(confSchema);
            return getConfigurationSchemaView(savedConfSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkSchemaId(String schemaId) throws IllegalArgumentException {
        if (isEmpty(schemaId)) {
            throw new IllegalArgumentException("The schemaId parameter is empty.");
        }
    }

    private ConfigurationRecordViewDto toConfigurationRecordView(ConfigurationRecordDto record)
            throws KaaAdminServiceException, IOException {

        ConfigurationSchemaDto schemaDto = this.getConfigurationSchema(record.getSchemaId());
        EndpointGroupDto endpointGroup = groupService.getEndpointGroup(record.getEndpointGroupId());

        String rawSchema = endpointGroup.getWeight() == 0 ? schemaDto.getBaseSchema() : schemaDto.getOverrideSchema();

        Schema schema = new Schema.Parser().parse(rawSchema);

        ConfigurationRecordFormDto activeConfig = null;
        ConfigurationRecordFormDto inactiveConfig = null;
        if (record.getActiveStructureDto() != null) {
            activeConfig = toConfigurationRecordFormDto(record.getActiveStructureDto(), schema);
        }
        if (record.getInactiveStructureDto() != null) {
            inactiveConfig = toConfigurationRecordFormDto(record.getInactiveStructureDto(), schema);
        }

        ConfigurationRecordViewDto result = new ConfigurationRecordViewDto(activeConfig, inactiveConfig);

        return result;
    }

    private ConfigurationDto toConfigurationDto(ConfigurationRecordFormDto configuration) throws KaaAdminServiceException, IOException {

        String body = null;
        RecordField configurationRecord = configuration.getConfigurationRecord();
        if (configurationRecord != null) {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(configurationRecord);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            body = converter.encodeToJson(record);
        }
        ConfigurationDto result = new ConfigurationDto(configuration);
        result.setBody(body);
        return result;
    }

    private ConfigurationRecordFormDto toConfigurationRecordFormDto(ConfigurationDto configuration) throws KaaAdminServiceException,
            IOException {

        ConfigurationSchemaDto schemaDto = this.getConfigurationSchema(configuration.getSchemaId());
        EndpointGroupDto endpointGroup = groupService.getEndpointGroup(configuration.getEndpointGroupId());

        String rawSchema = endpointGroup.getWeight() == 0 ? schemaDto.getBaseSchema() : schemaDto.getOverrideSchema();

        Schema schema = new Schema.Parser().parse(rawSchema);

        return toConfigurationRecordFormDto(configuration, schema);
    }

    private ConfigurationRecordFormDto toConfigurationRecordFormDto(ConfigurationDto configuration, Schema schema)
            throws KaaAdminServiceException, IOException {

        String body = configuration.getBody();

        RecordField configurationRecord;

        if (!isEmpty(body)) {
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
            GenericRecord record = converter.decodeJson(body);
            configurationRecord = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        } else {
            configurationRecord = FormAvroConverter.createRecordFieldFromSchema(schema);
        }

        ConfigurationRecordFormDto configurationRecordForm = new ConfigurationRecordFormDto(configuration);
        configurationRecordForm.setConfigurationRecord(configurationRecord);

        return configurationRecordForm;
    }

    private List<SchemaInfoDto> toConfigurationSchemaInfos(List<VersionDto> schemas, EndpointGroupDto endpointGroup)
            throws KaaAdminServiceException, IOException {
        List<SchemaInfoDto> schemaInfos = new ArrayList<>();
        for (VersionDto schemaDto : schemas) {
            ConfigurationSchemaDto configSchema = this.getConfigurationSchema(schemaDto.getId());
            String rawSchema = endpointGroup.getWeight() == 0 ? configSchema.getBaseSchema() : configSchema.getOverrideSchema();
            Schema schema = new Schema.Parser().parse(rawSchema);
            SchemaInfoDto schemaInfo = new SchemaInfoDto(schemaDto);
            RecordField schemaForm = FormAvroConverter.createRecordFieldFromSchema(schema);
            schemaInfo.setSchemaForm(schemaForm);
            schemaInfos.add(schemaInfo);
        }
        return schemaInfos;
    }

    private JsonNode injectUuids(JsonNode json) {
        boolean containerWithoutId = json.isContainerNode() && !json.has("__uuid");
        boolean notArray = !(json instanceof ArrayNode);
        boolean childIsNotArray = !(json.size() == 1 && json.getElements().next() instanceof ArrayNode);

        if (containerWithoutId && notArray && childIsNotArray) {
            ((ObjectNode) json).put("__uuid", (Integer) null);
        }

        for (JsonNode node : json) {
            if (node.isContainerNode())
                injectUuids(node);
        }

        return json;
    }

}
