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
import org.kaaproject.avro.ui.converter.CtlSource;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.dao.PropertiesFacade;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.messaging.MessagingService;
import org.kaaproject.kaa.server.admin.services.schema.CTLSchemaParser;
import org.kaaproject.kaa.server.admin.services.schema.ConfigurationSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.schema.EcfSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.schema.SimpleSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.sdk.SchemaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.schema.ConverterType.CONFIGURATION_FORM_AVRO_CONVERTER;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;


public abstract class AbstractAdminService implements InitializingBean {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdminService.class);

    @Autowired
    ControlService controlService;

    @Autowired
    UserFacade userFacade;

    @Autowired
    PropertiesFacade propertiesFacade;

    @Autowired
    MessagingService messagingService;

    @Autowired
    CacheService cacheService;

    @Value("#{properties[additional_plugins_scan_package]}")
    String additionalPluginsScanPackage;

    @Autowired
    PasswordEncoder passwordEncoder;

    SchemaFormAvroConverter simpleSchemaFormAvroConverter;

    SchemaFormAvroConverter commonSchemaFormAvroConverter;

    SchemaFormAvroConverter configurationSchemaFormAvroConverter;

    SchemaFormAvroConverter ecfSchemaFormAvroConverter;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    Map<PluginType, Map<String, PluginInfoDto>> pluginsInfo = new HashMap<>();

    {
        for (PluginType type : PluginType.values()) {
            pluginsInfo.put(type, new HashMap<String, PluginInfoDto>());
        }
    }

    void validateRecordSchema(Schema schema) throws KaaAdminServiceException {
        SchemaUtil.compileAvroSchema(schema);
        if (schema.getType() != Schema.Type.RECORD) {
            throw new KaaAdminServiceException("Schema " + schema.getFullName() + " is not a record schema!", ServiceErrorCode.INVALID_SCHEMA);
        }
    }

    byte[] getFileContent(String fileItemName) throws KaaAdminServiceException {
        if (!isEmpty(fileItemName)) {
            try {
                byte[] data = cacheService.uploadedFile(fileItemName, null);
                if (data == null) {
                    throw new KaaAdminServiceException("Unable to get file content!", ServiceErrorCode.FILE_NOT_FOUND);
                }
                return data;
            } finally {
                cacheService.removeUploadedFile(fileItemName);
            }
        } else {
            throw new KaaAdminServiceException("Unable to get file content, file item name is empty!", ServiceErrorCode.FILE_NOT_FOUND);
        }
    }

    void checkAuthority(KaaAuthorityDto... authorities) throws KaaAdminServiceException {
        AuthUserDto authUser = getCurrentUser();
        boolean matched = false;
        for (KaaAuthorityDto authority : authorities) {
            if (authUser.getAuthority() == authority) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            throw new KaaAdminServiceException("You do not have permission to perform this operation!", ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    void checkTenantId(String tenantId) throws KaaAdminServiceException {
        AuthUserDto authUser = getCurrentUser();
        if (authUser.getTenantId() == null || !authUser.getTenantId().equals(tenantId)) {
            throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    ApplicationDto checkApplicationId(String applicationId) throws KaaAdminServiceException {
        try {
            if (isEmpty(applicationId)) {
                throw new IllegalArgumentException("The applicationId parameter is empty.");
            }
            ApplicationDto application = controlService.getApplication(applicationId);
            checkApplication(application);
            return application;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    String checkApplicationToken(String applicationToken) throws KaaAdminServiceException {
        try {
            if (isEmpty(applicationToken)) {
                throw new KaaAdminServiceException(ServiceErrorCode.INVALID_ARGUMENTS);
            }
            ApplicationDto application = controlService.getApplicationByApplicationToken(applicationToken);
            checkApplication(application);
            return application.getId();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    void checkApplication(ApplicationDto application) throws KaaAdminServiceException {
        Utils.checkNotNull(application);
        checkTenantId(application.getTenantId());
    }

    EndpointGroupDto checkEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        try {
            if (isEmpty(endpointGroupId)) {
                throw new IllegalArgumentException("The endpointGroupId parameter is empty.");
            }
            EndpointGroupDto endpointGroup = controlService.getEndpointGroup(endpointGroupId);
            Utils.checkNotNull(endpointGroup);
            checkApplicationId(endpointGroup.getApplicationId());
            return endpointGroup;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    String getTenantId() throws KaaAdminServiceException {
        return getCurrentUser().getTenantId();
    }

    boolean isGroupAll(EndpointGroupDto groupDto) {
        boolean result = false;
        if (groupDto != null && groupDto.getWeight() == 0) {
            result = true;
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(KaaPluginConfig.class));
        scanPluginsPackage(scanner, "org.kaaproject.kaa.server.appenders");
        scanPluginsPackage(scanner, "org.kaaproject.kaa.server.verifiers");
        if (!isEmpty(additionalPluginsScanPackage)) {
            scanPluginsPackage(scanner, additionalPluginsScanPackage);
        }
        simpleSchemaFormAvroConverter = new SimpleSchemaFormAvroConverter();
        commonSchemaFormAvroConverter = new SchemaFormAvroConverter();
        configurationSchemaFormAvroConverter = new ConfigurationSchemaFormAvroConverter();
        ecfSchemaFormAvroConverter = new EcfSchemaFormAvroConverter();
    }

    private void scanPluginsPackage(ClassPathScanningCandidateComponentProvider scanner, String packageName) throws Exception {
        Set<BeanDefinition> beans = scanner.findCandidateComponents(packageName);
        for (BeanDefinition bean : beans) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());
            KaaPluginConfig annotation = clazz.getAnnotation(KaaPluginConfig.class);
            PluginConfig pluginConfig = (PluginConfig) clazz.newInstance();
            RecordField fieldConfiguration = FormAvroConverter.createRecordFieldFromSchema(pluginConfig.getPluginConfigSchema());
            PluginInfoDto pluginInfo = new PluginInfoDto(pluginConfig.getPluginTypeName(), fieldConfiguration,
                    pluginConfig.getPluginClassName());
            pluginsInfo.get(annotation.pluginType()).put(pluginInfo.getPluginClassName(), pluginInfo);
        }
    }

    void setSchema(AbstractSchemaDto schemaDto, byte[] data) throws KaaAdminServiceException {
        String schema = new String(data);
        validateRecordSchema(schema, false);
        schemaDto.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
    }

    void validateRecordSchema(String avroSchema, boolean isCtl) throws KaaAdminServiceException {
        Schema schema = validateSchema(avroSchema, isCtl);
        validateRecordSchema(schema);
    }

    Schema validateSchema(String avroSchema, boolean isCtl) throws KaaAdminServiceException {
        try {
            if (isCtl) {
                return CTLSchemaParser.parseStringCtlSchema(avroSchema);
            } else {
                Schema.Parser parser = new Schema.Parser();
                return parser.parse(avroSchema);
            }
        } catch (Exception spe) {
            LOG.error("Exception catched: ", spe);
            throw new KaaAdminServiceException(spe.getMessage(), ServiceErrorCode.INVALID_SCHEMA);
        }
    }

    RecordField createRecordFieldFromCtlSchemaAndBody(String ctlSchemaId, String body) throws KaaAdminServiceException {
        try {
            RecordField recordField;
            CTLSchemaDto ctlSchema = controlService.getCTLSchemaById(ctlSchemaId);
            Schema schema = controlService.exportCTLSchemaFlatAsSchema(ctlSchema);
            if (!isEmpty(body)) {
                GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
                GenericRecord record = converter.decodeJson(body);
                recordField = FormAvroConverter.createRecordFieldFromGenericRecord(record);
            } else {
                recordField = FormAvroConverter.createRecordFieldFromSchema(schema);
            }
            return recordField;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    Long saveUser(org.kaaproject.kaa.common.dto.admin.UserDto user) throws Exception {
        CreateUserResult result = userFacade.saveUserDto(user, passwordEncoder);
        try {
            if (!isEmpty(result.getPassword())) {
                messagingService.sendTempPassword(user.getUsername(),
                        result.getPassword(),
                        user.getMail());
            }
        } catch (Exception e) {
            LOG.error("Can't send temporary password. Exception was catched: ", e);
            if (isEmpty(user.getExternalUid())) {
                userFacade.deleteUser(result.getUserId());
            }
            StringBuilder errorMessage = new StringBuilder("Failed to send email with temporary password. ");
            if (e instanceof MailException) {
                errorMessage.append("Please, check outgoing email settings. ");
            }
            throw new KaaAdminServiceException(String.valueOf(errorMessage.append("See server logs for details.")), ServiceErrorCode.GENERAL_ERROR);
        }
        return result.getUserId();
    }

    void setPluginJsonConfigurationFromRaw(PluginDto plugin, PluginType type) {
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        byte[] rawConfiguration = plugin.getRawConfiguration();
        String jsonConfiguration = GenericAvroConverter.toJson(rawConfiguration, pluginInfo.getFieldConfiguration().getSchema());
        plugin.setJsonConfiguration(jsonConfiguration);
    }

    void setPluginRawConfigurationFromJson(PluginDto plugin, PluginType type) {
        LOG.trace("Updating plugin {} configuration using info {}", plugin, pluginsInfo.get(type));
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        if (pluginInfo == null) {
            LOG.error("Plugin configuration for class name {} is not found", plugin.getPluginClassName());
            throw new InvalidParameterException("Plugin configuration for class name " + plugin.getPluginClassName() + " is not found");
        }
        byte[] rawConfiguration = GenericAvroConverter.toRawData(plugin.getJsonConfiguration(), pluginInfo.getFieldConfiguration()
                .getSchema());
        plugin.setRawConfiguration(rawConfiguration);
    }

    CtlSchemaFormDto toCtlSchemaForm(CTLSchemaDto ctlSchema, ConverterType converterType) throws KaaAdminServiceException {
        try {
            CtlSchemaFormDto ctlSchemaForm = new CtlSchemaFormDto(ctlSchema);
            SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(ctlSchemaForm.getMetaInfo().getTenantId(),
                    ctlSchemaForm.getMetaInfo().getApplicationId(), converterType);
            RecordField form = converter.createSchemaFormFromSchema(ctlSchema.getBody());
            ctlSchemaForm.setSchema(form);
            List<Integer> availableVersions = controlService.getAllCTLSchemaVersionsByFqnTenantIdAndApplicationId(
                    ctlSchema.getMetaInfo().getFqn(), ctlSchema.getMetaInfo().getTenantId(), ctlSchema.getMetaInfo().getApplicationId());
            availableVersions = availableVersions == null ? Collections.<Integer>emptyList() : availableVersions;
            Collections.sort(availableVersions);
            ctlSchemaForm.getMetaInfo().setVersions(availableVersions);
            return ctlSchemaForm;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    SchemaFormAvroConverter getCtlSchemaConverterForScope(String tenantId, String applicationId, ConverterType converterType) throws KaaAdminServiceException {
        try {
            if (isEmpty(tenantId)) {
                return getCtlSchemaConverterForSystem(converterType);
            }
            if (isEmpty(applicationId)) {
                return getCtlSchemaConverterForTenant(tenantId, converterType);
            }
            return getCtlSchemaConverterForApplication(tenantId, applicationId, converterType);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private SchemaFormAvroConverter getCtlSchemaConverterForSystem(ConverterType converterType) throws KaaAdminServiceException {
        try {
            return createSchemaConverterFromCtlTypes(controlService.getAvailableCTLSchemaVersionsForSystem(), converterType);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private SchemaFormAvroConverter getCtlSchemaConverterForTenant(String tenantId, ConverterType converterType) throws KaaAdminServiceException {
        try {
            return createSchemaConverterFromCtlTypes(controlService.getAvailableCTLSchemaVersionsForTenant(tenantId), converterType);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private SchemaFormAvroConverter getCtlSchemaConverterForApplication(String tenantId, String applicationId, ConverterType converterType) throws KaaAdminServiceException {
        try {
            return createSchemaConverterFromCtlTypes(controlService.getAvailableCTLSchemaVersionsForApplication(tenantId, applicationId), converterType);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private SchemaFormAvroConverter createSchemaConverterFromCtlTypes(final Map<Fqn, List<Integer>> ctlTypes, ConverterType converterType) throws KaaAdminServiceException {
        try {
            CtlSource ctlSource = new CtlSource() {
                @Override
                public Map<Fqn, List<Integer>> getCtlTypes() {
                    return ctlTypes;
                }
            };
            if (converterType == CONFIGURATION_FORM_AVRO_CONVERTER) {
                return new ConfigurationSchemaFormAvroConverter(ctlSource);
            } else {
                return new SchemaFormAvroConverter(ctlSource);
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    void convertToSchemaForm(AbstractSchemaDto dto, SchemaFormAvroConverter converter) throws IOException {
        Schema schema = new Schema.Parser().parse(dto.getSchema());
        RecordField schemaForm = converter.createSchemaFormFromSchema(schema);
        dto.setSchemaForm(schemaForm);
    }

    void convertToStringSchema(AbstractSchemaDto dto, SchemaFormAvroConverter converter) throws Exception {
        Schema schema = converter.createSchemaFromSchemaForm(dto.getSchemaForm());
        validateRecordSchema(schema);
        String schemaString = SchemaFormAvroConverter.createSchemaString(schema, true);
        dto.setSchema(schemaString);
    }

    void setPluginRawConfigurationFromForm(PluginDto plugin) throws IOException {
        RecordField fieldConfiguration = plugin.getFieldConfiguration();
        GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(fieldConfiguration);
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
        byte[] rawConfiguration = converter.encode(record);
        plugin.setRawConfiguration(rawConfiguration);
    }

    void setPluginFormConfigurationFromRaw(PluginDto plugin, PluginType type) throws IOException {
        LOG.trace("Updating plugin {} configuration", plugin);
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        byte[] rawConfiguration = plugin.getRawConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(pluginInfo.getFieldConfiguration().getSchema());
        GenericRecord record = converter.decodeBinary(rawConfiguration);
        RecordField formData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        plugin.setFieldConfiguration(formData);
    }

    protected org.kaaproject.kaa.common.dto.admin.UserDto toUser(UserDto tenantUser) {
        User user = userFacade.findById(Long.valueOf(tenantUser.getExternalUid()));
        org.kaaproject.kaa.common.dto.admin.UserDto result = new org.kaaproject.kaa.common.dto.admin.UserDto(
                user.getId().toString(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getMail(),
                KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
        result.setId(tenantUser.getId());
        result.setTenantId(tenantUser.getTenantId());
        return result;
    }

}
