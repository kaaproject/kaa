/*
 * Copyright 2014 CyberVision, Inc.
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

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.iharder.Base64;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.hibernate.StaleObjectStateException;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.AbstractSchemaDto;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileViewDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventSchemaVersionDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInfoDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.dao.PropertiesFacade;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.entity.gen.GeneralProperties;
import org.kaaproject.kaa.server.admin.services.entity.gen.SmtpMailProperties;
import org.kaaproject.kaa.server.admin.services.messaging.MessagingService;
import org.kaaproject.kaa.server.admin.services.schema.ConfigurationSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.schema.EcfSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.schema.SimpleSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;

@Service("kaaAdminService")
public class KaaAdminServiceImpl implements KaaAdminService, InitializingBean {

    /**
     * The Constant logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(KaaAdminServiceImpl.class);

    /**
     * The Constant MAX_LIMIT.
     */
    private static final int MAX_LIMIT = 500;

    /** The application service. */
    @Autowired
    private ControlService controlService;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private PropertiesFacade propertiesFacade;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private CacheService cacheService;

    @Value("#{properties[additional_plugins_scan_package]}")
    private String additionalPluginsScanPackage;

    private PasswordEncoder passwordEncoder;

    private SchemaFormAvroConverter simpleSchemaFormAvroConverter;

    private SchemaFormAvroConverter commonSchemaFormAvroConverter;

    private SchemaFormAvroConverter configurationSchemaFormAvroConverter;

    private SchemaFormAvroConverter ecfSchemaFormAvroConverter;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private Map<PluginType, Map<String, PluginInfoDto>> pluginsInfo = new HashMap<>();

    {
        for (PluginType type : PluginType.values()) {
            pluginsInfo.put(type, new HashMap<String, PluginInfoDto>());
        }
    }

    @Override
    public EndpointProfileViewDto getEndpointProfileViewDtoByEndpointProfileKeyHash(String endpointProfileKeyHash)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileViewDto viewDto = controlService.getEndpointProfileViewDtoByEndpointKeyHash(endpointProfileKeyHash);

            ProfileSchemaDto schemaDto = viewDto.getProfileSchemaDto();
            if (schemaDto != null) {
                convertToSchemaForm(schemaDto, simpleSchemaFormAvroConverter);
                /* check for empty schemas */
                viewDto.setEndpointProfileRecord(
                        generateFormDataFromJson(schemaDto.getSchema(), viewDto.getEndpointProfileDto().getClientProfileBody()));
            }
            viewDto.setProfileSchemaDto(schemaDto);
            for (EndpointGroupDto groupDto : viewDto.getGroupDtoList()) {
                Utils.checkNotNull(groupDto);
            }

            return viewDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private RecordField generateFormDataFromJson(String avroSchema, String json)
            throws KaaAdminServiceException {
        try {
            Schema schema = new Schema.Parser().parse(avroSchema);
            JsonDecoder jsonDecoder = DecoderFactory.get().jsonDecoder(schema, json);
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
            GenericRecord genericRecord = datumReader.read(null, jsonDecoder);
            return FormAvroConverter.createRecordFieldFromGenericRecord(genericRecord);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (Integer.valueOf(limit) > MAX_LIMIT) {
                throw new IllegalArgumentException("Incorrect limit parameter. You must enter value not more than " + MAX_LIMIT);
            }
            EndpointGroupDto endpointGroupDto = getEndpointGroup(endpointGroupId);
            PageLinkDto pageLinkDto = new PageLinkDto(endpointGroupId, limit, offset);
            if (isGroupAll(endpointGroupDto)) {
                pageLinkDto.setApplicationId(endpointGroupDto.getApplicationId());
            }
            EndpointProfilesPageDto endpointProfilesPage = controlService.getEndpointProfileByEndpointGroupId(pageLinkDto);
            if (endpointProfilesPage.getEndpointProfiles() == null || !endpointProfilesPage.hasEndpointProfiles()) {
                throw new KaaAdminServiceException("Requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
            }
            return endpointProfilesPage;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (Integer.valueOf(limit) > MAX_LIMIT) {
                throw new IllegalArgumentException("Incorrect limit parameter. You must enter value not more than " + MAX_LIMIT);
            }
            EndpointGroupDto endpointGroupDto = getEndpointGroup(endpointGroupId);
            PageLinkDto pageLinkDto = new PageLinkDto(endpointGroupId, limit, offset);
            if (isGroupAll(endpointGroupDto)) {
                pageLinkDto.setApplicationId(endpointGroupDto.getApplicationId());
            }
            EndpointProfilesBodyDto endpointProfilesBodyDto = controlService.getEndpointProfileBodyByEndpointGroupId(pageLinkDto);
            if (!endpointProfilesBodyDto.hasEndpointBodies()) {
                throw new KaaAdminServiceException(
                        "Requested item was not found!",
                        ServiceErrorCode.ITEM_NOT_FOUND);
            }
            return endpointProfilesBodyDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto profileDto = controlService.getEndpointProfileByKeyHash(endpointProfileKeyHash);
            if (profileDto == null) {
                throw new KaaAdminServiceException(
                        "Requested item was not found!",
                        ServiceErrorCode.ITEM_NOT_FOUND);
            }
            checkApplicationId(profileDto.getApplicationId());
            return profileDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileBodyDto profileBodyDto = controlService.getEndpointProfileBodyByKeyHash(endpointProfileKeyHash);
            if (profileBodyDto == null) {
                throw new KaaAdminServiceException(
                        "Requested item was not found!",
                        ServiceErrorCode.ITEM_NOT_FOUND);
            }
            checkApplicationId(profileBodyDto.getAppId());
            return profileBodyDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
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

    private void scanPluginsPackage(ClassPathScanningCandidateComponentProvider scanner,
            String packageName) throws Exception {
        Set<BeanDefinition> beans = scanner.findCandidateComponents(packageName);
        for (BeanDefinition bean : beans) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());
            KaaPluginConfig annotation = clazz.getAnnotation(KaaPluginConfig.class);
            PluginConfig pluginConfig = (PluginConfig) clazz.newInstance();
            RecordField fieldConfiguration =
                    FormAvroConverter.createRecordFieldFromSchema(pluginConfig.getPluginConfigSchema());
            PluginInfoDto pluginInfo = new PluginInfoDto(
                    pluginConfig.getPluginTypeName(), fieldConfiguration, pluginConfig.getPluginClassName());
            pluginsInfo.get(annotation.pluginType()).put(pluginInfo.getPluginClassName(), pluginInfo);
        }
    }

    @Override
    public List<TenantUserDto> getTenants() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            List<TenantAdminDto> tenantAdmins = controlService.getTenantAdmins();
            List<TenantUserDto> tenantUsers = new ArrayList<TenantUserDto>(tenantAdmins.size());
            for (TenantAdminDto tenantAdmin : tenantAdmins) {
                TenantUserDto tenantUser = toTenantUser(tenantAdmin);
                if (tenantUser != null) {
                    tenantUsers.add(tenantUser);
                }
            }
            return tenantUsers;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TenantUserDto getTenant(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            UserDto user = controlService.getUser(userId);
            Utils.checkNotNull(user);
            TenantAdminDto tenantAdmin = controlService.getTenantAdmin(user.getTenantId());
            return toTenantUser(tenantAdmin);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TenantUserDto editTenant(TenantUserDto tenantUser)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            Long userId = saveUser(tenantUser);
            tenantUser.setExternalUid(userId.toString());
            TenantAdminDto tenantAdmin = new TenantAdminDto();
            TenantDto tenant = new TenantDto();
            tenant.setId(tenantUser.getTenantId());
            tenant.setName(tenantUser.getTenantName());
            tenantAdmin.setTenant(tenant);
            tenantAdmin.setUserId(tenantUser.getId());
            tenantAdmin.setUsername(tenantUser.getUsername());
            tenantAdmin.setExternalUid(userId.toString());
            TenantAdminDto savedTenantAdmin = controlService.editTenantAdmin(tenantAdmin);
            return toTenantUser(savedTenantAdmin);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteTenant(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            UserDto user = controlService.getUser(userId);
            Utils.checkNotNull(user);
            TenantAdminDto tenantAdmin = controlService.getTenantAdmin(user.getTenantId());
            Utils.checkNotNull(tenantAdmin);
            userFacade.deleteUser(Long.valueOf(tenantAdmin.getExternalUid()));
            controlService.deleteTenantAdmin(user.getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<org.kaaproject.kaa.common.dto.admin.UserDto> getUsers()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            List<UserDto> users = controlService.getTenantUsers(getTenantId());
            List<org.kaaproject.kaa.common.dto.admin.UserDto> tenantUsers = new ArrayList<>(users.size());
            for (UserDto user : users) {
                org.kaaproject.kaa.common.dto.admin.UserDto tenantUser = toUser(user);
                tenantUsers.add(tenantUser);
            }
            return tenantUsers;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto getUser(String userId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            UserDto user = controlService.getUser(userId);
            Utils.checkNotNull(user);
            checkTenantId(user.getTenantId());
            return toUser(user);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto getUserProfile()
            throws KaaAdminServiceException {
        try {
            User user = userFacade.findById(Long.valueOf(getCurrentUser().getExternalUid()));
            Utils.checkNotNull(user);
            org.kaaproject.kaa.common.dto.admin.UserDto result =
                    new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                            user.getUsername(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getMail(),
                            KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
            return result;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkUserProfile(org.kaaproject.kaa.common.dto.admin.UserDto userDto) throws KaaAdminServiceException {
        if (isEmpty(userDto.getUsername())) {
            throw new IllegalArgumentException("Username is not valid.");
        } else if (isEmpty(userDto.getFirstName())) {
            throw new IllegalArgumentException("First name is not valid.");
        } else if (isEmpty(userDto.getLastName())) {
            throw new IllegalArgumentException("Last name is not valid.");
        } else if (isEmpty(userDto.getMail())) {
            throw new IllegalArgumentException("Mail is not valid.");
        } else if (userDto.getAuthority() == null) {
            throw new IllegalArgumentException("Authority is not valid.");
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto editUserProfile(org.kaaproject.kaa.common.dto.admin.UserDto userDto)
            throws KaaAdminServiceException {
        try {
            checkUserProfile(userDto);
            userDto.setExternalUid(getCurrentUser().getExternalUid());
            Long userId = saveUser(userDto);
            User user = userFacade.findById(userId);
            org.kaaproject.kaa.common.dto.admin.UserDto result =
                    new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                            user.getUsername(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getMail(),
                            KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
            return result;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto getMailProperties() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            return propertiesFacade.getPropertiesDto(SmtpMailProperties.class);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            PropertiesDto storedPropertiesDto = propertiesFacade.editPropertiesDto(mailPropertiesDto,
                    SmtpMailProperties.class);
            messagingService.configureMailSender();
            return storedPropertiesDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto getGeneralProperties() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            return propertiesFacade.getPropertiesDto(GeneralProperties.class);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            PropertiesDto storedPropertiesDto = propertiesFacade.editPropertiesDto(generalPropertiesDto,
                    GeneralProperties.class);
            messagingService.configureMailSender();
            return storedPropertiesDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto editUser(
            org.kaaproject.kaa.common.dto.admin.UserDto user)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(user.getId())) {
                UserDto storedUser = controlService.getUser(user.getId());
                Utils.checkNotNull(storedUser);
                checkTenantId(storedUser.getTenantId());
            }
            Long userId = saveUser(user);
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setExternalUid(userId.toString());
            userDto.setTenantId(getTenantId());
            userDto.setAuthority(user.getAuthority());
            UserDto savedUser = controlService.editUser(userDto);
            return toUser(savedUser);

        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteUser(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            UserDto user = controlService.getUser(userId);
            Utils.checkNotNull(user);
            checkTenantId(user.getTenantId());
            userFacade.deleteUser(Long.valueOf(user.getExternalUid()));
            controlService.deleteUser(user.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ApplicationDto> getApplications() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return controlService.getApplicationsByTenantId(getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto getApplication(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return checkApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto getApplicationByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return checkApplicationToken(applicationToken);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto editApplication(ApplicationDto application) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(application.getId())) {
                checkApplicationId(application.getId());
            }
            application.setTenantId(getTenantId());
            return controlService.editApplication(application);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteApplication(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            checkApplicationId(applicationId);
            controlService.deleteApplication(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }

    }

    @Override
    public SchemaVersions getSchemaVersionsByApplicationId(String applicationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);

            SchemaVersions schemaVersions = new SchemaVersions();

            List<SchemaDto> configurationSchemaVersions = controlService.getConfigurationSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setConfigurationSchemaVersions(configurationSchemaVersions);

            List<SchemaDto> profileSchemaVersions = controlService.getProfileSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setProfileSchemaVersions(profileSchemaVersions);

            List<SchemaDto> notificationSchemaVersions = controlService.getNotificationSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setNotificationSchemaVersions(notificationSchemaVersions);

            List<SchemaDto> logSchemaVersions = controlService.getLogSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setLogSchemaVersions(logSchemaVersions);

            return schemaVersions;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SdkProfileDto addSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(sdkProfile.getApplicationId());
            sdkProfile.setCreatedUsername(this.getCurrentUser().getUsername());
            sdkProfile.setCreatedTime(System.currentTimeMillis());
            return controlService.saveSdkProfile(sdkProfile);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void deleteSdkProfile(String sdkProfileId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            SdkProfileDto sdkProfile = this.checkSdkProfileId(sdkProfileId);
            if (!controlService.isSdkProfileUsed(sdkProfile.getToken())) {
                controlService.deleteSdkProfile(sdkProfileId);
            } else {
                throw new IllegalArgumentException("Associated endpoint profiles have been found.");
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public SdkProfileDto getSdkProfile(String sdkProfileId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkSdkProfileId(sdkProfileId);
            return controlService.getSdkProfile(sdkProfileId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(applicationId);
            return controlService.getSdkProfilesByApplicationId(applicationId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public String generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException {
        try {
            doGenerateSdk(sdkProfile, targetPlatform);
            return Base64.encodeObject(new CacheService.SdkKey(sdkProfile, targetPlatform), Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public FileData getSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException {
        try {
            return doGenerateSdk(sdkProfile, targetPlatform);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private FileData doGenerateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(sdkProfile.getApplicationId());
            CacheService.SdkKey sdkKey = new CacheService.SdkKey(sdkProfile, targetPlatform);
            FileData sdkFile = cacheService.getSdk(sdkKey);
            if (sdkFile == null) {
                sdkFile = controlService.generateSdk(sdkProfile, targetPlatform);
                cacheService.putSdk(sdkKey, sdkFile);
            }
            return sdkFile;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void flushSdkCache() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            List<ApplicationDto> applications = getApplications();
            for (ApplicationDto application : applications) {
                for (CacheService.SdkKey key : cacheService.getCachedSdkKeys(application.getId())) {
                    cacheService.flushSdk(key);
                }
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createSimpleEmptySchemaForm()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return simpleSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createCommonEmptySchemaForm()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return commonSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createConfigurationEmptySchemaForm()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return configurationSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createEcfEmptySchemaForm() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return ecfSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateSimpleSchemaForm(String fileItemName)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            return simpleSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateCommonSchemaForm(String fileItemName)
            throws KaaAdminServiceException {
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            return commonSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateConfigurationSchemaForm(String fileItemName)
            throws KaaAdminServiceException {
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            return configurationSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateEcfSchemaForm(String fileItemName)
            throws KaaAdminServiceException {
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            return ecfSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ProfileSchemaDto> getProfileSchemasByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getProfileSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileSchemaDto profileSchema = controlService.getProfileSchema(profileSchemaId);
            Utils.checkNotNull(profileSchema);
            checkApplicationId(profileSchema.getApplicationId());
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto editProfileSchema(ProfileSchemaDto profileSchema,
            byte[] schema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(profileSchema.getId())) {
                profileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(profileSchema.getApplicationId());
                setSchema(profileSchema, schema);
            } else {
                ProfileSchemaDto storedProfileSchema = controlService.getProfileSchema(profileSchema.getId());
                Utils.checkNotNull(storedProfileSchema);
                checkApplicationId(storedProfileSchema.getApplicationId());
                profileSchema.setSchema(storedProfileSchema.getSchema());
            }
            return controlService.editProfileSchema(profileSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void convertToSchemaForm(AbstractSchemaDto dto, SchemaFormAvroConverter converter) throws IOException {
        Schema schema = new Schema.Parser().parse(dto.getSchema());
        RecordField schemaForm = converter.createSchemaFormFromSchema(schema);
        dto.setSchemaForm(schemaForm);
    }

    private void convertToStringSchema(AbstractSchemaDto dto, SchemaFormAvroConverter converter) throws Exception {
        Schema schema = converter.createSchemaFromSchemaForm(dto.getSchemaForm());
        String schemaString = SchemaFormAvroConverter.createSchemaString(schema, true);
        dto.setSchema(schemaString);
    }

    @Override
    public ProfileSchemaDto getProfileSchemaForm(String profileSchemaId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileSchemaDto profileSchema = getProfileSchema(profileSchemaId);
            convertToSchemaForm(profileSchema, simpleSchemaFormAvroConverter);
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto editProfileSchemaForm(ProfileSchemaDto profileSchema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(profileSchema.getId())) {
                profileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(profileSchema.getApplicationId());
                convertToStringSchema(profileSchema, simpleSchemaFormAvroConverter);
            } else {
                ProfileSchemaDto storedProfileSchema = controlService.getProfileSchema(profileSchema.getId());
                Utils.checkNotNull(storedProfileSchema);
                checkApplicationId(storedProfileSchema.getApplicationId());
                profileSchema.setSchema(storedProfileSchema.getSchema());
            }
            return controlService.editProfileSchema(profileSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getConfigurationSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto getConfigurationSchema(
            String configurationSchemaId) throws KaaAdminServiceException {
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
    public ConfigurationSchemaDto editConfigurationSchema(
            ConfigurationSchemaDto configurationSchema, byte[] schema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(configurationSchema.getId())) {
                configurationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(configurationSchema.getApplicationId());
                setSchema(configurationSchema, schema);
            } else {
                ConfigurationSchemaDto storedConfigurationSchema = controlService.getConfigurationSchema(configurationSchema.getId());
                Utils.checkNotNull(storedConfigurationSchema);
                checkApplicationId(storedConfigurationSchema.getApplicationId());
                configurationSchema.setSchema(storedConfigurationSchema.getSchema());
            }
            return controlService.editConfigurationSchema(configurationSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto getConfigurationSchemaForm(
            String configurationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationSchemaDto configurationSchema = getConfigurationSchema(configurationSchemaId);
            convertToSchemaForm(configurationSchema, configurationSchemaFormAvroConverter);
            return configurationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto editConfigurationSchemaForm(
            ConfigurationSchemaDto configurationSchema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(configurationSchema.getId())) {
                configurationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(configurationSchema.getApplicationId());
                convertToStringSchema(configurationSchema, configurationSchemaFormAvroConverter);
            } else {
                ConfigurationSchemaDto storedConfigurationSchema = controlService.getConfigurationSchema(configurationSchema.getId());
                Utils.checkNotNull(storedConfigurationSchema);
                checkApplicationId(storedConfigurationSchema.getApplicationId());
                configurationSchema.setSchema(storedConfigurationSchema.getSchema());
            }
            return controlService.editConfigurationSchema(configurationSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.findNotificationSchemasByAppIdAndType(applicationId,
                    NotificationTypeDto.USER);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaDto> getUserNotificationSchemasByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getUserNotificationSchemasByAppId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            List<NotificationSchemaDto> notificationSchemas = controlService.findNotificationSchemasByAppIdAndType(applicationId,
                    NotificationTypeDto.USER);
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(notificationSchemas.size());
            for (NotificationSchemaDto notificationSchema : notificationSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(notificationSchema);
                Schema schema = new Schema.Parser().parse(notificationSchema.getSchema());
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
    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            NotificationSchemaDto notificationSchema = controlService.getNotificationSchema(notificationSchemaId);
            Utils.checkNotNull(notificationSchema);
            checkApplicationId(notificationSchema.getApplicationId());
            return notificationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaDto editNotificationSchema(
            NotificationSchemaDto notificationSchema, byte[] schema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(notificationSchema.getId())) {
                notificationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(notificationSchema.getApplicationId());
                setSchema(notificationSchema, schema);
            } else {
                NotificationSchemaDto storedNotificationSchema = controlService.getNotificationSchema(notificationSchema.getId());
                Utils.checkNotNull(storedNotificationSchema);
                checkApplicationId(storedNotificationSchema.getApplicationId());
                notificationSchema.setSchema(storedNotificationSchema.getSchema());
            }
            notificationSchema.setType(NotificationTypeDto.USER);
            return controlService.editNotificationSchema(notificationSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaDto getNotificationSchemaForm(
            String notificationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            NotificationSchemaDto notificationSchema = getNotificationSchema(notificationSchemaId);
            convertToSchemaForm(notificationSchema, commonSchemaFormAvroConverter);
            return notificationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaDto editNotificationSchemaForm(
            NotificationSchemaDto notificationSchema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(notificationSchema.getId())) {
                notificationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(notificationSchema.getApplicationId());
                convertToStringSchema(notificationSchema, commonSchemaFormAvroConverter);
            } else {
                NotificationSchemaDto storedNotificationSchema = controlService.getNotificationSchema(notificationSchema.getId());
                Utils.checkNotNull(storedNotificationSchema);
                checkApplicationId(storedNotificationSchema.getApplicationId());
                notificationSchema.setSchema(storedNotificationSchema.getSchema());
            }
            notificationSchema.setType(NotificationTypeDto.USER);
            return controlService.editNotificationSchema(notificationSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<LogSchemaDto> getLogSchemasByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getLogSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogSchemaDto logSchema = controlService.getLogSchema(logSchemaId);
            Utils.checkNotNull(logSchema);
            checkApplicationId(logSchema.getApplicationId());
            return logSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(
            String applicationToken, int version) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationDto storedApplication = controlService.getApplicationByApplicationToken(applicationToken);
            checkApplication(storedApplication);
            LogSchemaDto logSchema = controlService.getLogSchemaByApplicationIdAndVersion(storedApplication.getId(), version);
            Utils.checkNotNull(logSchema);
            return logSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        List<SchemaDto> logSchemaVersions = Collections.emptyList();
        try {
            checkApplicationId(applicationId);
            logSchemaVersions = controlService.getLogSchemaVersionsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        return logSchemaVersions;
    }

    @Override
    public LogSchemaDto editLogSchema(LogSchemaDto logSchema,
            byte[] schema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(logSchema.getId())) {
                logSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(logSchema.getApplicationId());
                setSchema(logSchema, schema);
            } else {
                LogSchemaDto storedLogSchema = controlService.getLogSchema(logSchema.getId());
                Utils.checkNotNull(storedLogSchema);
                checkApplicationId(storedLogSchema.getApplicationId());
                logSchema.setSchema(storedLogSchema.getSchema());
            }
            return controlService.editLogSchema(logSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto getLogSchemaForm(String logSchemaId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogSchemaDto logSchema = getLogSchema(logSchemaId);
            convertToSchemaForm(logSchema, simpleSchemaFormAvroConverter);
            return logSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto editLogSchemaForm(LogSchemaDto logSchema)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(logSchema.getId())) {
                logSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(logSchema.getApplicationId());
                convertToStringSchema(logSchema, simpleSchemaFormAvroConverter);
            } else {
                LogSchemaDto storedLogSchema = controlService.getLogSchema(logSchema.getId());
                Utils.checkNotNull(storedLogSchema);
                checkApplicationId(storedLogSchema.getApplicationId());
                logSchema.setSchema(storedLogSchema.getSchema());
            }
            return controlService.editLogSchema(logSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getEndpointGroupsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return checkEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(endpointGroup.getId())) {
                endpointGroup.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(endpointGroup.getApplicationId());
            } else {
                checkEndpointGroupId(endpointGroup.getId());
            }
            return controlService.editEndpointGroup(endpointGroup);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            controlService.deleteEndpointGroup(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<StructureRecordDto<ProfileFilterDto>> getProfileFilterRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkSchemaId(String schemaId) throws IllegalArgumentException {
        if (isEmpty(schemaId)) {
            throw new IllegalArgumentException("The schemaId parameter is empty.");
        }
    }

    @Override
    public StructureRecordDto<ProfileFilterDto> getProfileFilterRecord(
            String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkSchemaId(schemaId);
            checkEndpointGroupId(endpointGroupId);
            StructureRecordDto<ProfileFilterDto> record = controlService.getProfileFilterRecord(schemaId, endpointGroupId);
            Utils.checkNotNull(record);
            return record;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaDto> getVacantProfileSchemasByEndpointGroupId(
            String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = this.getCurrentUser().getUsername();
            if (isEmpty(profileFilter.getId())) {
                profileFilter.setCreatedUsername(username);
                checkEndpointGroupId(profileFilter.getEndpointGroupId());
            } else {
                profileFilter.setModifiedUsername(username);
                ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilter.getId());
                Utils.checkNotNull(storedProfileFilter);
                checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            }
            return controlService.editProfileFilter(profileFilter);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto activateProfileFilter(String profileFilterId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilterId);
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            return controlService.activateProfileFilter(profileFilterId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto deactivateProfileFilter(String profileFilterId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilterId);
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            return controlService.deactivateProfileFilter(profileFilterId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteProfileFilterRecord(String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkSchemaId(schemaId);
            StructureRecordDto<ProfileFilterDto> record = controlService.getProfileFilterRecord(schemaId, endpointGroupId);
            Utils.checkNotNull(record);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            controlService.deleteProfileFilterRecord(schemaId, endpointGroupId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<StructureRecordDto<ConfigurationDto>> getConfigurationRecordsByEndpointGroupId(
            String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public StructureRecordDto<ConfigurationDto> getConfigurationRecord(
            String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            StructureRecordDto<ConfigurationDto> record = controlService.getConfigurationRecord(schemaId, endpointGroupId);
            Utils.checkNotNull(record);
            return record;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public StructureRecordDto<ConfigurationRecordFormDto> getConfigurationRecordForm(
            String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkSchemaId(schemaId);
            StructureRecordDto<ConfigurationDto> record = getConfigurationRecord(schemaId, endpointGroupId);
            return toConfigurationRecordFormStructure(record);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto editConfiguration(ConfigurationDto configuration)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = this.getCurrentUser().getUsername();
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
        } catch (Exception e) {
            throw Utils.handleExceptionWithCause(e, HibernateOptimisticLockingFailureException.class,
                    "Someone has already updated the configuration. Reload page to be able to edit it", true);
        }
    }

    @Override
    public ConfigurationRecordFormDto editConfigurationRecordForm(ConfigurationRecordFormDto configuration)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto toSave = toConfigurationDto(configuration);
            ConfigurationDto stored = editConfiguration(toSave);
            return toConfigurationRecordFormDto(stored);
        } catch (StaleObjectStateException e) {
            throw new KaaAdminServiceException("Someone has already updated the configuration. Reload page to be able to edit it.",
                    ServiceErrorCode.GENERAL_ERROR);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto activateConfiguration(String configurationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = controlService.getConfiguration(configurationId);
            Utils.checkNotNull(storedConfiguration);
            checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            return controlService.activateConfiguration(configurationId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordFormDto activateConfigurationRecordForm(String configurationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = activateConfiguration(configurationId);
            return toConfigurationRecordFormDto(storedConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto deactivateConfiguration(String configurationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = controlService.getConfiguration(configurationId);
            Utils.checkNotNull(storedConfiguration);
            checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            return controlService.deactivateConfiguration(configurationId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = deactivateConfiguration(configurationId);
            return toConfigurationRecordFormDto(storedConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private StructureRecordDto<ConfigurationRecordFormDto> toConfigurationRecordFormStructure(StructureRecordDto<ConfigurationDto> record)
            throws KaaAdminServiceException, IOException {

        ConfigurationSchemaDto schemaDto = this.getConfigurationSchema(record.getSchemaId());
        EndpointGroupDto endpointGroup = this.getEndpointGroup(record.getEndpointGroupId());

        String rawSchema = endpointGroup.getWeight() == 0 ? schemaDto.getBaseSchema() :
                schemaDto.getOverrideSchema();

        Schema schema = new Schema.Parser().parse(rawSchema);

        ConfigurationRecordFormDto activeConfig = null;
        ConfigurationRecordFormDto inactiveConfig = null;
        if (record.getActiveStructureDto() != null) {
            activeConfig = toConfigurationRecordFormDto(record.getActiveStructureDto(), schema);
        }
        if (record.getInactiveStructureDto() != null) {
            inactiveConfig = toConfigurationRecordFormDto(record.getInactiveStructureDto(), schema);
        }

        StructureRecordDto<ConfigurationRecordFormDto> result = new
                StructureRecordDto<>(activeConfig, inactiveConfig);

        return result;
    }

    private ConfigurationRecordFormDto toConfigurationRecordFormDto(ConfigurationDto configuration)
            throws KaaAdminServiceException, IOException {

        ConfigurationSchemaDto schemaDto = this.getConfigurationSchema(configuration.getSchemaId());
        EndpointGroupDto endpointGroup = this.getEndpointGroup(configuration.getEndpointGroupId());

        String rawSchema = endpointGroup.getWeight() == 0 ? schemaDto.getBaseSchema() :
                schemaDto.getOverrideSchema();

        Schema schema = new Schema.Parser().parse(rawSchema);

        return toConfigurationRecordFormDto(configuration, schema);
    }

    private ConfigurationRecordFormDto toConfigurationRecordFormDto(ConfigurationDto configuration,
            Schema schema)
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

    private ConfigurationDto toConfigurationDto(ConfigurationRecordFormDto configuration)
            throws KaaAdminServiceException, IOException {

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

    @Override
    public List<SchemaDto> getVacantConfigurationSchemasByEndpointGroupId(
            String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getVacantConfigurationSchemaInfosByEndpointGroupId(
            String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointGroupDto endpointGroup = checkEndpointGroupId(endpointGroupId);
            List<SchemaDto> schemas = getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
            return toConfigurationSchemaInfos(schemas, endpointGroup);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private List<SchemaInfoDto> toConfigurationSchemaInfos(List<SchemaDto> schemas, EndpointGroupDto endpointGroup)
            throws KaaAdminServiceException, IOException {
        List<SchemaInfoDto> schemaInfos = new ArrayList<>();
        for (SchemaDto schemaDto : schemas) {
            ConfigurationSchemaDto configSchema = this.getConfigurationSchema(schemaDto.getId());
            String rawSchema = endpointGroup.getWeight() == 0 ? configSchema.getBaseSchema() :
                    configSchema.getOverrideSchema();
            Schema schema = new Schema.Parser().parse(rawSchema);
            SchemaInfoDto schemaInfo = new SchemaInfoDto(schemaDto);
            RecordField schemaForm = FormAvroConverter.createRecordFieldFromSchema(schema);
            schemaInfo.setSchemaForm(schemaForm);
            schemaInfos.add(schemaInfo);
        }
        return schemaInfos;
    }

    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            StructureRecordDto<ConfigurationDto> record = controlService.getConfigurationRecord(schemaId, endpointGroupId);
            Utils.checkNotNull(record);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            controlService.deleteConfigurationRecord(schemaId, endpointGroupId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getTopicsByApplicationId(String applicationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getTopicByAppId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getTopicByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantTopicByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TopicDto getTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            return topic;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(topic.getId())) {
                topic.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(topic.getApplicationId());
            } else {
                throw new KaaAdminServiceException("Unable to edit existing topic!", ServiceErrorCode.INVALID_ARGUMENTS);
            }
            return controlService.editTopic(topic);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkTopicId(topicId);
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.deleteTopicById(topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void setPluginRawConfigurationFromForm(PluginDto plugin) throws IOException {
        RecordField fieldConfiguration = plugin.getFieldConfiguration();
        GenericRecord record = FormAvroConverter.
                createGenericRecordFromRecordField(fieldConfiguration);
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
        byte[] rawConfiguration = converter.encode(record);
        plugin.setRawConfiguration(rawConfiguration);
    }

    private void setPluginRawConfigurationFromJson(PluginDto plugin, PluginType type) {
        LOG.trace("Updating plugin {} configuration using info {}", plugin, pluginsInfo.get(type));
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        if (pluginInfo == null) {
            LOG.error("Plugin configuration for class name {} is not found", plugin.getPluginClassName());
            throw new InvalidParameterException("Plugin configuration for class name " + plugin.getPluginClassName() + " is not found");
        }
        byte[] rawConfiguration = GenericAvroConverter.toRawData(plugin.getJsonConfiguration(),
                pluginInfo.getFieldConfiguration().getSchema());
        plugin.setRawConfiguration(rawConfiguration);
    }

    private void setPluginFormConfigurationFromRaw(PluginDto plugin, PluginType type) throws IOException {
        LOG.trace("Updating plugin {} configuration", plugin);
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        byte[] rawConfiguration = plugin.getRawConfiguration();
        GenericAvroConverter<GenericRecord> converter =
                new GenericAvroConverter<>(pluginInfo.getFieldConfiguration().getSchema());
        GenericRecord record = converter.decodeBinary(rawConfiguration);
        RecordField formData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        plugin.setFieldConfiguration(formData);
    }

    private void setPluginJsonConfigurationFromRaw(PluginDto plugin, PluginType type) {
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        byte[] rawConfiguration = plugin.getRawConfiguration();
        String jsonConfiguration = GenericAvroConverter.toJson(rawConfiguration,
                pluginInfo.getFieldConfiguration().getSchema());
        plugin.setJsonConfiguration(jsonConfiguration);
    }

    @Override
    public List<LogAppenderDto> getLogAppendersByApplicationId(String appId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(appId);
            return controlService.getLogAppendersByApplicationId(appId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogAppenderDto logAppender = controlService.getLogAppender(appenderId);
            Utils.checkNotNull(logAppender);
            checkApplicationId(logAppender.getApplicationId());
            return logAppender;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(appender.getId())) {
                appender.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(appender.getApplicationId());
            } else {
                LogAppenderDto storedlLogAppender = controlService.getLogAppender(appender.getId());
                Utils.checkNotNull(storedlLogAppender);
                checkApplicationId(storedlLogAppender.getApplicationId());
            }
            return controlService.editLogAppender(appender);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteLogAppender(String appenderId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(appenderId)) {
                throw new IllegalArgumentException("The appenderId parameter is empty.");
            }
            LogAppenderDto logAppender = controlService.getLogAppender(appenderId);
            Utils.checkNotNull(logAppender);
            checkApplicationId(logAppender.getApplicationId());
            controlService.deleteLogAppender(appenderId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException {
        LogAppenderDto logAppender = getLogAppender(appenderId);
        try {
            setPluginFormConfigurationFromRaw(logAppender, PluginType.LOG_APPENDER);
            return logAppender;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto editLogAppenderForm(LogAppenderDto logAppender) throws KaaAdminServiceException {
        try {
            setPluginRawConfigurationFromForm(logAppender);
            LogAppenderDto saved = editLogAppender(logAppender);
            return saved;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException {
        List<LogAppenderDto> logAppenders = getLogAppendersByApplicationId(appId);
        for (LogAppenderDto logAppender : logAppenders) {
            setPluginJsonConfigurationFromRaw(logAppender, PluginType.LOG_APPENDER);
        }
        return logAppenders;
    }

    @Override
    public LogAppenderDto getRestLogAppender(String appenderId) throws KaaAdminServiceException {
        LogAppenderDto logAppender = getLogAppender(appenderId);
        setPluginJsonConfigurationFromRaw(logAppender, PluginType.LOG_APPENDER);
        return logAppender;
    }

    @Override
    public LogAppenderDto editRestLogAppender(LogAppenderDto logAppender) throws KaaAdminServiceException {
        setPluginRawConfigurationFromJson(logAppender, PluginType.LOG_APPENDER);
        LogAppenderDto savedLogAppender = editLogAppender(logAppender);
        setPluginJsonConfigurationFromRaw(savedLogAppender, PluginType.LOG_APPENDER);
        return savedLogAppender;
    }

    @Override
    public List<PluginInfoDto> getLogAppenderPluginInfos()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return new ArrayList<PluginInfoDto>(pluginsInfo.get(PluginType.LOG_APPENDER).values());
    }

    @Override
    public List<UserVerifierDto> getUserVerifiersByApplicationId(String appId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(appId);
            return controlService.getUserVerifiersByApplicationId(appId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto getUserVerifier(String userVerifierId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            UserVerifierDto userVerifier = controlService.getUserVerifier(userVerifierId);
            Utils.checkNotNull(userVerifier);
            checkApplicationId(userVerifier.getApplicationId());
            return userVerifier;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto editUserVerifier(UserVerifierDto userVerifier)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(userVerifier.getId())) {
                userVerifier.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(userVerifier.getApplicationId());
            } else {
                UserVerifierDto storedUserVerifier = controlService.getUserVerifier(userVerifier.getId());
                Utils.checkNotNull(storedUserVerifier);
                checkApplicationId(storedUserVerifier.getApplicationId());
            }
            return controlService.editUserVerifier(userVerifier);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteUserVerifier(String userVerifierId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(userVerifierId)) {
                throw new IllegalArgumentException("The userVerifierId parameter is empty.");
            }
            UserVerifierDto userVerifier = controlService.getUserVerifier(userVerifierId);
            Utils.checkNotNull(userVerifier);
            checkApplicationId(userVerifier.getApplicationId());
            controlService.deleteUserVerifier(userVerifierId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto getUserVerifierForm(String userVerifierId)
            throws KaaAdminServiceException {
        UserVerifierDto userVerifier = getUserVerifier(userVerifierId);
        try {
            setPluginFormConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
            return userVerifier;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto editUserVerifierForm(UserVerifierDto userVerifier)
            throws KaaAdminServiceException {
        try {
            setPluginRawConfigurationFromForm(userVerifier);
            UserVerifierDto saved = editUserVerifier(userVerifier);
            return saved;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<UserVerifierDto> getRestUserVerifiersByApplicationId(String appId)
            throws KaaAdminServiceException {
        List<UserVerifierDto> userVerifiers = getUserVerifiersByApplicationId(appId);
        for (UserVerifierDto userVerifier : userVerifiers) {
            setPluginJsonConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
        }
        return userVerifiers;
    }

    @Override
    public UserVerifierDto getRestUserVerifier(String userVerifierId)
            throws KaaAdminServiceException {
        UserVerifierDto userVerifier = getUserVerifier(userVerifierId);
        setPluginJsonConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
        return userVerifier;
    }

    @Override
    public UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier)
            throws KaaAdminServiceException {
        setPluginRawConfigurationFromJson(userVerifier, PluginType.USER_VERIFIER);
        UserVerifierDto savedUserVerifier = editUserVerifier(userVerifier);
        setPluginJsonConfigurationFromRaw(savedUserVerifier, PluginType.USER_VERIFIER);
        return savedUserVerifier;
    }

    @Override
    public List<PluginInfoDto> getUserVerifierPluginInfos()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return new ArrayList<PluginInfoDto>(pluginsInfo.get(PluginType.USER_VERIFIER).values());
    }

    private void checkTopicId(String topicId) throws IllegalArgumentException {
        if (isEmpty(topicId)) {
            throw new IllegalArgumentException("The topicId parameter is empty.");
        }
    }

    @Override
    public void addTopicToEndpointGroup(String endpointGroupId, String topicId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            checkTopicId(topicId);
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.addTopicsToEndpointGroup(endpointGroupId, topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            checkTopicId(topicId);
            TopicDto topic = controlService.getTopic(topicId);
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.removeTopicsFromEndpointGroup(endpointGroupId, topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField getRecordDataFromFile(String schema, String fileItemName)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            byte[] body = getFileContent(fileItemName);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
            GenericRecord record = converter.decodeJson(body);
            RecordField recordData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
            return recordData;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkExpiredDate(NotificationDto notification) throws KaaAdminServiceException {
        if (null != notification.getExpiredAt() && notification.getExpiredAt().before(new Date())) {
            throw new IllegalArgumentException("Overdue expiry time for notification!");
        }
    }

    @Override
    public void sendNotification(NotificationDto notification,
            RecordField notificationData) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkExpiredDate(notification);
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(notificationData);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            byte[] body = converter.encodeToJsonBytes(record);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = controlService.getTopic(notification.getTopicId());
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            controlService.editNotification(notification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationDto sendNotification(NotificationDto notification, byte[] body)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkExpiredDate(notification);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = controlService.getTopic(notification.getTopicId());
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            return controlService.editNotification(notification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointNotificationDto sendUnicastNotification(
            NotificationDto notification, String clientKeyHash, byte[] body)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkExpiredDate(notification);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = controlService.getTopic(notification.getTopicId());
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            EndpointNotificationDto unicastNotification = new EndpointNotificationDto();
            unicastNotification.setEndpointKeyHash(Base64
                    .decode(clientKeyHash.getBytes(Charsets.UTF_8)));
            unicastNotification.setNotificationDto(notification);
            return controlService.editUnicastNotification(unicastNotification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassFamilyDto> getEventClassFamilies()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            return controlService.getEventClassFamiliesByTenantId(getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            EventClassFamilyDto eventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(eventClassFamily);
            checkTenantId(eventClassFamily.getTenantId());
            for (EventSchemaVersionDto eventSchemaVersion : eventClassFamily.getSchemas()) {
                Schema schema = new Schema.Parser().parse(eventSchemaVersion.getSchema());
                RecordField schemaForm = ecfSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
                eventSchemaVersion.setSchemaForm(schemaForm);
            }
            return eventClassFamily;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EventClassFamilyDto editEventClassFamily(
            EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(eventClassFamily.getId())) {
                EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamily.getId());
                Utils.checkNotNull(storedEventClassFamily);
                checkTenantId(storedEventClassFamily.getTenantId());
            } else {
                String username = this.getCurrentUser().getUsername();
                eventClassFamily.setCreatedUsername(username);
            }
            eventClassFamily.setTenantId(getTenantId());
            return controlService.editEventClassFamily(eventClassFamily);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addEventClassFamilySchemaForm(String eventClassFamilyId,
            RecordField schemaForm) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            Schema schema = ecfSchemaFormAvroConverter.createSchemaFromSchemaForm(schemaForm);
            String schemaString = SchemaFormAvroConverter.createSchemaString(schema, true);

            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = this.getCurrentUser().getUsername();
            controlService.addEventClassFamilySchema(eventClassFamilyId, schemaString, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkEventClassFamilyId(String eventClassFamilyId) throws IllegalArgumentException {
        if (isEmpty(eventClassFamilyId)) {
            throw new IllegalArgumentException("The eventClassFamilyId parameter is empty.");
        }
    }

    @Override
    public void addEventClassFamilySchema(String eventClassFamilyId, byte[] data)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            checkEventClassFamilyId(eventClassFamilyId);
            String schema = new String(data);
            validateSchema(schema);

            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = this.getCurrentUser().getUsername();
            controlService.addEventClassFamilySchema(eventClassFamilyId, schema, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(
            String eventClassFamilyId, int version, EventClassType type) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEventClassFamilyId(eventClassFamilyId);
            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            return controlService.getEventClassesByFamilyIdVersionAndType(eventClassFamilyId, version, type);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getApplicationEventFamilyMapsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(
            String applicationEventFamilyMapId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationEventFamilyMapDto aefMap = controlService.getApplicationEventFamilyMap(applicationEventFamilyMapId);
            Utils.checkNotNull(aefMap);
            checkApplicationId(aefMap.getApplicationId());
            return aefMap;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(
            ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(applicationEventFamilyMap.getId())) {
                String username = this.getCurrentUser().getUsername();
                applicationEventFamilyMap.setCreatedUsername(username);
                checkApplicationId(applicationEventFamilyMap.getApplicationId());
            } else {
                ApplicationEventFamilyMapDto storedApplicationEventFamilyMap = controlService.getApplicationEventFamilyMap(applicationEventFamilyMap.getId());
                Utils.checkNotNull(storedApplicationEventFamilyMap);
                checkApplicationId(storedApplicationEventFamilyMap.getApplicationId());
            }
            return controlService.editApplicationEventFamilyMap(applicationEventFamilyMap);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getVacantEventClassFamiliesByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getEventClassFamiliesByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            RecordKey sdkKey = new RecordKey(applicationId, logSchemaVersion, file);
            return Base64.encodeObject(sdkKey, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getRecordDataByApplicationIdAndSchemaVersion(String applicationId, int schemaVersion, RecordKey.RecordFiles file)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            RecordKey sdkKey = new RecordKey(applicationId, schemaVersion, file);
            return Base64.encodeObject(sdkKey, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private TenantUserDto toTenantUser(TenantAdminDto tenantAdmin) {
        User user = userFacade.findById(Long.valueOf(tenantAdmin.getExternalUid()));
        LOG.debug("Convert tenant admin to tenant user {}.", user);
        TenantUserDto tenantUser = null;
        if (user != null) {
            tenantUser = new TenantUserDto(user.getId().toString(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getMail(),
                    KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
            tenantUser.setId(tenantAdmin.getUserId());
            tenantUser.setTenantId(tenantAdmin.getTenant().getId());
            tenantUser.setTenantName(tenantAdmin.getTenant().getName());
        } else {
            LOG.debug("Can't find tenant user by external id {}.", tenantAdmin.getExternalUid());
        }
        return tenantUser;
    }

    private org.kaaproject.kaa.common.dto.admin.UserDto toUser(UserDto tenantUser) {
        User user = userFacade.findById(Long.valueOf(tenantUser.getExternalUid()));
        org.kaaproject.kaa.common.dto.admin.UserDto result = new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getMail(),
                KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
        result.setId(tenantUser.getId());
        result.setTenantId(tenantUser.getTenantId());
        return result;
    }

    private Long saveUser(org.kaaproject.kaa.common.dto.admin.UserDto user)
            throws Exception {
        CreateUserResult result = userFacade.saveUserDto(user, passwordEncoder);
        if (!isEmpty(result.getPassword())) {
            messagingService.sendTempPassword(user.getUsername(),
                    result.getPassword(),
                    user.getMail());
        }
        return result.getUserId();
    }

    private void setSchema(AbstractSchemaDto schemaDto, byte[] data) throws KaaAdminServiceException {
        String schema = new String(data);
        validateSchema(schema);
        schemaDto.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
    }

    private void validateSchema(String schema) throws KaaAdminServiceException {
        Schema.Parser parser = new Schema.Parser();
        try {
            parser.parse(schema);
        } catch (SchemaParseException spe) {
            throw new KaaAdminServiceException(spe.getMessage(), ServiceErrorCode.INVALID_SCHEMA);
        }
    }

    private byte[] getFileContent(String fileItemName) throws KaaAdminServiceException {
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

    private void checkAuthority(KaaAuthorityDto... authorities) throws KaaAdminServiceException {
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

    private void checkTenantId(String tenantId) throws KaaAdminServiceException {
        AuthUserDto authUser = getCurrentUser();
        if (authUser.getTenantId() == null || !authUser.getTenantId().equals(tenantId)) {
            throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    private ApplicationDto checkApplicationId(String applicationId) throws KaaAdminServiceException {
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

    private ApplicationDto checkApplicationToken(String applicationToken) throws KaaAdminServiceException {
        try {
            if (isEmpty(applicationToken)) {
                throw new KaaAdminServiceException(ServiceErrorCode.INVALID_ARGUMENTS);
            }
            ApplicationDto application = controlService.getApplicationByApplicationToken(applicationToken);
            checkApplication(application);
            return application;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkApplication(ApplicationDto application) throws KaaAdminServiceException {
        Utils.checkNotNull(application);
        checkTenantId(application.getTenantId());
    }

    private EndpointGroupDto checkEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
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

    private String getTenantId() throws KaaAdminServiceException {
        return getCurrentUser().getTenantId();
    }

    private AuthUserDto getCurrentUser() throws KaaAdminServiceException {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof AuthUserDto) {
            return (AuthUserDto) authentication.getPrincipal();
        } else {
            throw new KaaAdminServiceException("You are not authorized to perform this operation!", ServiceErrorCode.NOT_AUTHORIZED);
        }
    }

    private boolean isGroupAll(EndpointGroupDto groupDto) {
        boolean result = false;
        if (groupDto != null && groupDto.getWeight() == 0) {
            result = true;
        }
        return result;
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
    public List<SchemaInfoDto> getUserConfigurationSchemaInfosByApplicationId(
            String applicationId) throws KaaAdminServiceException {
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
    public void editUserConfiguration(
            EndpointUserConfigurationDto endpointUserConfiguration,
            String applicationId, RecordField configurationData)
            throws KaaAdminServiceException {
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

    /**
     * Checks whether the current user has permission to access the given CTL
     * schema. The check passes if either the current user and the given CTL
     * schema belong to the same tenant or the current user is a
     * {@link org.kaaproject.kaa.common.dto.KaaAuthorityDto#KAA_ADMIN} and the
     * given CTL schema is
     * {@link org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto#SYSTEM}.
     *
     * @param schema A CTL schema to check
     *
     * @throws KaaAdminServiceException - if the check fails.
     */
    private void checkCTLAuthority(CTLSchemaDto schema) throws KaaAdminServiceException {
        AuthUserDto currentUser = this.getCurrentUser();
        if ((schema.getMetaInfo().getScope() != CTLSchemaScopeDto.SYSTEM || currentUser.getAuthority() != KaaAuthorityDto.KAA_ADMIN)
                && (schema.getMetaInfo().getScope() == CTLSchemaScopeDto.SYSTEM || !schema.getTenantId().equals(currentUser.getTenantId()))) {
            throw new KaaAdminServiceException("You do not have permission to perform this operation!", ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    private void checkCTLSchemaId(String schemaId) throws KaaAdminServiceException {
        if (schemaId == null || schemaId.isEmpty()) {
            throw new IllegalArgumentException("Missing CTL schema ID!");
        }
    }

    private void checkCTLSchemaFqn(String fqn) throws KaaAdminServiceException {
        if (fqn == null || fqn.isEmpty()) {
            throw new IllegalArgumentException("Missing fully qualified CTL schema name!");
        }
    }

    private void checkCTLSchemaVersion(Integer version) throws KaaAdminServiceException {
        if (version == null) {
            throw new IllegalArgumentException("Missing CTL schema version number!");
        } else if (version <= 0) {
            throw new IllegalArgumentException("The CTL schema version is not a positive number!");
        }
    }

    private CTLSchemaScopeDto getCTLSchemaScopeByName(String name) {
        name = name.toUpperCase();
        for (CTLSchemaScopeDto scope : CTLSchemaScopeDto.values()) {
            if (name.equals(scope.name())) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Invalid CTL schema scope name!");
    }

    /**
     * This class is used to validate CTL schemas on save.
     *
     * @author Bohdan Khablenko
     *
     * @since v0.8.0
     *
     * @see #saveCTLSchema(CTLSchemaInfoDto)
     */
    public class CTLSchemaParser {

        private final Schema.Parser parser = new Schema.Parser();

        /**
         * Parses the given CTL schema along with its dependencies as an
         * {@link org.apache.avro.Schema Avro schema}.
         *
         * @param schema A CTL schema to parse
         *
         * @return A parsed CTL schema as an Avro schema
         *
         * @throws IllegalArgumentException - if the given CTL schema is invalid
         *             and thus cannot be parsed.
         */
        public Schema parse(CTLSchemaInfoDto schema) throws KaaAdminServiceException {
            if (schema.getDependencies() != null) {
                for (CTLSchemaMetaInfoDto dependency : schema.getDependencies()) {
                    try {
                        CTLSchemaDto dependencySchema = controlService.getCTLSchemaByFqnVersionAndTenantId(dependency.getFqn(), dependency.getVersion(),
                                getCurrentUser().getTenantId());
                        this.parse(dependencySchema.toCTLSchemaInfoDto());
                    } catch (Exception cause) {
                        String message = "Unable to locate dependency \"" + dependency.getFqn() + "\" (version " + dependency.getVersion() + ")";
                        throw new IllegalArgumentException(message);
                    }
                }
            }

            try {
                /*
                 * Parsed schemas are automatically added to the set of types
                 * known to the parser.
                 */
                return parser.parse(schema.getBody());
            } catch (Exception cause) {
                throw new IllegalArgumentException("Unable to parse CTL schema: " + cause.getMessage());
            }
        }
    }

    @Override
    public CTLSchemaInfoDto saveCTLSchema(String body) throws KaaAdminServiceException {
        CTLSchemaInfoDto schema = new CTLSchemaInfoDto();
        try {
            ObjectNode object = new ObjectMapper().readValue(body, ObjectNode.class);

            if (!object.has("type") || !object.get("type").isTextual() || !object.get("type").getTextValue().equals("record")) {
                throw new IllegalArgumentException("The data provided is not a record!");
            }

            if (!object.has("namespace") || !object.get("namespace").isTextual()) {
                throw new IllegalArgumentException("No namespace specified!");
            } else if (!object.has("name") || !object.get("name").isTextual()) {
                throw new IllegalArgumentException("No name specified!");
            } else {
                schema.setFqn(object.get("namespace").getTextValue() + "." + object.get("name").getTextValue());
            }

            if (!object.has("version") || !object.get("version").isInt()) {
                throw new IllegalArgumentException("No version specified!");
            } else {
                schema.setVersion(object.get("version").asInt());
            }

            schema.setTenantId(this.getCurrentUser().getTenantId());

            if (object.has("application") && object.get("application").isTextual()) {
                schema.setApplicationId(object.get("application").asText());
            }

            String tenantId = this.getCurrentUser().getTenantId();
            if (tenantId != null && schema.getApplicationId() != null) {
                schema.setScope(CTLSchemaScopeDto.APPLICATION);
            } else if (tenantId != null && schema.getApplicationId() == null) {
                schema.setScope(CTLSchemaScopeDto.TENANT);
            } else if (tenantId == null && schema.getApplicationId() == null) {
                schema.setScope(CTLSchemaScopeDto.SYSTEM);
            } else {
                throw new IllegalArgumentException("Unable to determine the scope!");
            }

            Set<CTLSchemaMetaInfoDto> dependencies = new HashSet<>();
            if (!object.has("dependencies")) {
                schema.setDependencies(dependencies);
            } else if (!object.get("dependencies").isArray()) {
                throw new IllegalArgumentException("Illegal dependencies format!");
            } else {
                for (JsonNode child : object.get("dependencies")) {
                    if (!child.isObject() || !child.has("fqn") || !child.get("fqn").isTextual() || !child.has("version") || !child.get("version").isInt()) {
                        throw new IllegalArgumentException("Wrong dependency format!");
                    } else {
                        dependencies.add(new CTLSchemaMetaInfoDto(child.get("fqn").asText(), child.get("version").asInt()));
                    }
                    schema.setDependencies(dependencies);
                }
            }

            schema.setBody(body);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
        return this.saveCTLSchema(schema);
    }

    @Override
    public CTLSchemaInfoDto saveCTLSchema(CTLSchemaInfoDto schema) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            Utils.checkNotNull(schema);

            if (schema.getScope() == CTLSchemaScopeDto.SYSTEM) {
                if (schema.getTenantId() != null) {
                    throw new IllegalArgumentException("A system CTL schema cannot be tied to a tenant!");
                } else if (schema.getApplicationId() != null) {
                    throw new IllegalArgumentException("A system CTL schema cannot be tied to an application!");
                }
            } else if (schema.getScope() == CTLSchemaScopeDto.TENANT) {
                if (schema.getTenantId() == null) {
                    throw new IllegalArgumentException("A tenant CTL schema must include a tenant identifier!");
                } else if (schema.getApplicationId() != null) {
                    throw new IllegalArgumentException("A tenant CTL schema cannot be tied to an application!");
                } else {
                    this.checkTenantId(schema.getTenantId());
                }
            } else {
                this.checkApplicationId(schema.getApplicationId());
            }

            // Check if the schema dependencies are present in the database
            List<CTLSchemaMetaInfoDto> missingDependencies = new ArrayList<>();
            Set<CTLSchemaDto> dependencies = new HashSet<>();
            if (schema.getDependencies() != null) {
                for (CTLSchemaMetaInfoDto dependency : schema.getDependencies()) {
                    CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionAndTenantId(dependency.getFqn(), dependency.getVersion(),
                            schema.getTenantId());
                    if (schemaFound == null) {
                        missingDependencies.add(dependency);
                    } else {
                        dependencies.add(schemaFound);
                    }
                }
            }
            if (!missingDependencies.isEmpty()) {
                String message = "The following dependencies are missing from the database: " + Arrays.toString(missingDependencies.toArray());
                throw new IllegalArgumentException(message);
            }

            // Check if the schema body is valid
            new CTLSchemaParser().parse(schema);

            CTLSchemaDto result = controlService.saveCTLSchema(new CTLSchemaDto(schema, dependencies));
            return result != null ? result.toCTLSchemaInfoDto() : null;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void deleteCTLSchemaById(String schemaId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaId(schemaId);
            CTLSchemaDto schemaFound = controlService.getCTLSchemaById(schemaId);
            Utils.checkNotNull(schemaFound);
            this.checkCTLAuthority(schemaFound);

            List<CTLSchemaDto> schemaDependents = controlService.getCTLSchemaDependents(schemaId);
            if (schemaDependents != null && !schemaDependents.isEmpty()) {
                String message = "Unable to delete the CTL schema as it is referenced by " + Arrays.toString(schemaDependents.toArray());
                throw new IllegalArgumentException(message);
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void deleteCTLSchemaByFqnAndVersion(String fqn, Integer version) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaFqn(fqn);
            this.checkCTLSchemaVersion(version);
            String tenantId = this.getCurrentUser().getTenantId();
            CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionAndTenantId(fqn, version, tenantId);
            Utils.checkNotNull(schemaFound);
            this.checkCTLAuthority(schemaFound);

            List<CTLSchemaDto> schemaDependents = controlService.getCTLSchemaDependents(fqn, version, tenantId);
            if (schemaDependents != null && !schemaDependents.isEmpty()) {
                String message = "Unable to delete the CTL schema as it is referenced by " + Arrays.toString(schemaDependents.toArray());
                throw new IllegalArgumentException(message);
            }

            controlService.deleteCTLSchemaByFqnAndVersionAndTenantId(fqn, version, tenantId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public CTLSchemaInfoDto getCTLSchemaById(String schemaId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaId(schemaId);
            CTLSchemaDto schemaFound = controlService.getCTLSchemaById(schemaId);
            Utils.checkNotNull(schemaFound);
            if (schemaFound.getMetaInfo().getScope() != CTLSchemaScopeDto.SYSTEM) {
                this.checkCTLAuthority(schemaFound);
            }
            return schemaFound != null ? schemaFound.toCTLSchemaInfoDto() : null;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public CTLSchemaInfoDto getCTLSchemaByFqnAndVersion(String fqn, Integer version) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaFqn(fqn);
            this.checkCTLSchemaVersion(version);
            String tenantId = this.getCurrentUser().getTenantId();
            CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionAndTenantId(fqn, version, tenantId);
            Utils.checkNotNull(schemaFound);
            if (schemaFound.getMetaInfo().getScope() != CTLSchemaScopeDto.SYSTEM) {
                this.checkCTLAuthority(schemaFound);
            }
            return schemaFound != null ? schemaFound.toCTLSchemaInfoDto() : null;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public List<CTLSchemaMetaInfoDto> getCTLSchemasAvailable() throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            String tenantId = this.getCurrentUser().getTenantId();
            return controlService.getAvailableCTLSchemasMetaInfoByTenantId(tenantId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public List<CTLSchemaMetaInfoDto> getCTLSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkApplicationId(applicationId);
            return controlService.getCTLSchemasMetaInfoByApplicationId(applicationId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public List<CTLSchemaMetaInfoDto> getCTLSchemasByScope(String scopeName) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            CTLSchemaScopeDto scope = this.getCTLSchemaScopeByName(scopeName);
            AuthUserDto currentUser = this.getCurrentUser();
            if (scope == CTLSchemaScopeDto.TENANT && currentUser.getAuthority() != KaaAuthorityDto.KAA_ADMIN) {
                return controlService.getCTLSchemasMetaInfoByTenantId(currentUser.getTenantId());
            } else if (scope == CTLSchemaScopeDto.SYSTEM) {
                return controlService.getSystemCTLSchemasMetaInfo();
            } else {
                throw new IllegalArgumentException("You do not have permission to perform this operation!");
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    public SdkProfileDto checkSdkProfileId(String sdkProfileId) throws KaaAdminServiceException {
        try {
            if (isEmpty(sdkProfileId)) {
                throw new IllegalArgumentException("The SDK profile identifier is empty!");
            }
            SdkProfileDto sdkProfile = controlService.getSdkProfile(sdkProfileId);
            Utils.checkNotNull(sdkProfile);
            return sdkProfile;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
}
