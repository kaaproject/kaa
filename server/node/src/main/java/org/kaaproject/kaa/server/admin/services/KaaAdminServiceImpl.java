/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.admin.services;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.avro.generic.GenericRecord;
import org.hibernate.StaleObjectStateException;
import org.kaaproject.avro.ui.converter.CtlSource;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.AbstractSchemaDto;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
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
import org.kaaproject.kaa.server.admin.services.schema.CTLSchemaParser;
import org.kaaproject.kaa.server.admin.services.schema.ConfigurationSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.schema.EcfSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.schema.SimpleSchemaFormAvroConverter;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordViewDto;
import org.kaaproject.kaa.server.admin.shared.endpoint.EndpointProfileViewDto;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaExportKey;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.kaaproject.kaa.server.common.plugin.KaaPluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginConfig;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.kaaproject.kaa.server.operations.service.filter.DefaultFilterEvaluator;
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;

import net.iharder.Base64;

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
    public EndpointProfileViewDto getEndpointProfileViewByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto endpointProfile = controlService.getEndpointProfileByKeyHash(endpointProfileKeyHash);
            Utils.checkNotNull(endpointProfile);
            checkApplicationId(endpointProfile.getApplicationId());
            EndpointProfileViewDto endpointProfileView = new EndpointProfileViewDto();
            endpointProfileView.setEndpointKeyHash(endpointProfile.getEndpointKeyHash());
            endpointProfileView.setSdkProfileDto(controlService.findSdkProfileByToken(endpointProfile.getSdkToken()));
            if (endpointProfile.getEndpointUserId() != null) {
                EndpointUserDto endpointUser = controlService.getEndpointUser(endpointProfile.getEndpointUserId());
                if (endpointUser != null) {
                    endpointProfileView.setUserId(endpointUser.getId());
                    endpointProfileView.setUserExternalId(endpointUser.getExternalId());
                }
            }
            EndpointProfileSchemaDto clientProfileSchema = controlService.getProfileSchemaByApplicationIdAndVersion(
                            endpointProfile.getApplicationId(), 
                            endpointProfile.getClientProfileVersion());
            ServerProfileSchemaDto serverProfileSchema = controlService.getServerProfileSchemaByApplicationIdAndVersion(
                            endpointProfile.getApplicationId(), 
                            endpointProfile.getServerProfileVersion());
            endpointProfileView.setProfileSchemaName(clientProfileSchema.getName());
            endpointProfileView.setProfileSchemaVersion(clientProfileSchema.toVersionDto());
            endpointProfileView.setServerProfileSchemaName(serverProfileSchema.getName());
            endpointProfileView.setServerProfileSchemaVersion(serverProfileSchema.toVersionDto());
            endpointProfileView.setProfileRecord(createRecordFieldFromCtlSchemaAndBody(clientProfileSchema.getCtlSchemaId(), 
                    endpointProfile.getClientProfileBody()));
            endpointProfileView.setServerProfileRecord(createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), 
                    endpointProfile.getServerProfileBody()));
            List<TopicDto> topics = new ArrayList<>();
            if (endpointProfile.getSubscriptions() != null) {
                for (String topicId : endpointProfile.getSubscriptions()) {
                    topics.add(controlService.getTopic(topicId));
                }
            }
            endpointProfileView.setTopics(topics);
            Set<EndpointGroupDto> endpointGroupsSet = new HashSet<>();
            if (endpointProfile.getGroupState() != null) {
                for (EndpointGroupStateDto endpointGroupState : endpointProfile.getGroupState()) {
                    endpointGroupsSet.add(controlService.getEndpointGroup(endpointGroupState.getEndpointGroupId()));
                }
            }
            List<EndpointGroupDto> endpointGroups = new ArrayList<EndpointGroupDto>(endpointGroupsSet);
            endpointProfileView.setEndpointGroups(endpointGroups);
            return endpointProfileView;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    private RecordField createRecordFieldFromCtlSchemaAndBody(String ctlSchemaId, String body) throws KaaAdminServiceException {
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

    @Override
    public RecordField generateRecordFromSchemaJson(String avroSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            Schema schema = new Schema.Parser().parse(avroSchema);
            return FormAvroConverter.createRecordFieldFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId, String limit, String offset)
            throws KaaAdminServiceException {
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
            return controlService.getEndpointProfileByEndpointGroupId(pageLinkDto);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(String endpointGroupId, String limit, String offset)
            throws KaaAdminServiceException {
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
            return controlService.getEndpointProfileBodyByEndpointGroupId(pageLinkDto);
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
                throw new KaaAdminServiceException("Requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
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
                throw new KaaAdminServiceException("Requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
            }
            checkApplicationId(profileBodyDto.getAppId());
            return profileBodyDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public EndpointProfileDto updateServerProfile(String endpointKeyHash,
            int serverProfileVersion, String serverProfileBody)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto profileDto = controlService.getEndpointProfileByKeyHash(endpointKeyHash);            
            checkApplicationId(profileDto.getApplicationId());
            ServerProfileSchemaDto serverProfileSchema = controlService.getServerProfileSchemaByApplicationIdAndVersion(
                    profileDto.getApplicationId(), serverProfileVersion);
            RecordField record;
            try {
                record = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), 
                        serverProfileBody);
            } catch (Exception e) {
                throw new KaaAdminServiceException("Provided server profile body is not valid: " 
                                    + e.getMessage(), ServiceErrorCode.BAD_REQUEST_PARAMS);
            }
            if (!record.isValid()) {
                throw new KaaAdminServiceException("Provided server profile body is not valid!", ServiceErrorCode.BAD_REQUEST_PARAMS);
            }
            profileDto = controlService.updateServerProfile(endpointKeyHash, serverProfileVersion, serverProfileBody);
            return profileDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileDto updateServerProfile(String endpointKeyHash,
            int serverProfileVersion, RecordField serverProfileRecord)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(serverProfileRecord);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(record.getSchema());
            String serverProfileBody = converter.encodeToJson(record);
            return updateServerProfile(endpointKeyHash, serverProfileVersion, serverProfileBody);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
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
    public TenantUserDto editTenant(TenantUserDto tenantUser) throws KaaAdminServiceException {
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
    public List<org.kaaproject.kaa.common.dto.admin.UserDto> getUsers() throws KaaAdminServiceException {
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
    public org.kaaproject.kaa.common.dto.admin.UserDto getUser(String userId) throws KaaAdminServiceException {
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
    public org.kaaproject.kaa.common.dto.admin.UserDto getUserProfile() throws KaaAdminServiceException {
        try {
            User user = userFacade.findById(Long.valueOf(getCurrentUser().getExternalUid()));
            Utils.checkNotNull(user);
            org.kaaproject.kaa.common.dto.admin.UserDto result = new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                    user.getUsername(), user.getFirstName(), user.getLastName(), user.getMail(), KaaAuthorityDto.valueOf(user
                            .getAuthorities().iterator().next().getAuthority()));
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
            org.kaaproject.kaa.common.dto.admin.UserDto result = new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                    user.getUsername(), user.getFirstName(), user.getLastName(), user.getMail(), KaaAuthorityDto.valueOf(user
                            .getAuthorities().iterator().next().getAuthority()));
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
    public PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            PropertiesDto storedPropertiesDto = propertiesFacade.editPropertiesDto(mailPropertiesDto, SmtpMailProperties.class);
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
    public PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            PropertiesDto storedPropertiesDto = propertiesFacade.editPropertiesDto(generalPropertiesDto, GeneralProperties.class);
            messagingService.configureMailSender();
            return storedPropertiesDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto editUser(org.kaaproject.kaa.common.dto.admin.UserDto user)
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
    public SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);

            SchemaVersions schemaVersions = new SchemaVersions();

            List<VersionDto> configurationSchemaVersions = controlService.getConfigurationSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setConfigurationSchemaVersions(configurationSchemaVersions);

            List<VersionDto> profileSchemaVersions = controlService.getProfileSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setProfileSchemaVersions(profileSchemaVersions);

            List<VersionDto> notificationSchemaVersions = controlService.getNotificationSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setNotificationSchemaVersions(notificationSchemaVersions);

            List<VersionDto> logSchemaVersions = controlService.getLogSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setLogSchemaVersions(logSchemaVersions);

            return schemaVersions;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(sdkProfile.getApplicationId());
            sdkProfile.setCreatedUsername(getCurrentUser().getUsername());
            sdkProfile.setCreatedTime(System.currentTimeMillis());
            
            ApplicationDto application = controlService.getApplication(sdkProfile.getApplicationId());
            if (application == null) {
                throw new NotFoundException("Application not found!");
            }
            sdkProfile.setApplicationToken(application.getApplicationToken());
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
    public SdkProfileViewDto getSdkProfileView(String sdkProfileId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);

        try {
            SdkProfileViewDto viewDto = new SdkProfileViewDto();
            SdkProfileDto sdkProfile = controlService.getSdkProfile(sdkProfileId);
            viewDto.setSdkProfile(sdkProfile);

            String applicationId = sdkProfile.getApplicationId();
            List<ApplicationEventFamilyMapDto> aefDtoList = new ArrayList<>();
            List<ApplicationEventFamilyMapDto> aefMaps = controlService.
                    getApplicationEventFamilyMapsByApplicationId(applicationId);
            List<String> aefMapIds = sdkProfile.getAefMapIds();
            for (ApplicationEventFamilyMapDto aefDto : aefMaps) {
                if (aefMapIds.contains(aefDto.getId())){
                    aefDtoList.add(aefDto);
                }
            }
            viewDto.setAefMapDtoList(aefDtoList);

            List<ConfigurationSchemaDto> configSchemas =
                    controlService.getConfigurationSchemasByApplicationId(applicationId);
            for (ConfigurationSchemaDto dto : configSchemas) {
                if (dto.getVersion() == sdkProfile.getConfigurationSchemaVersion()) {
                    viewDto.setConfigurationSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setConfigurationSchemaId(dto.getId());
                }
            }

            List<EndpointProfileSchemaDto> profileSchemas = controlService.getProfileSchemasByApplicationId(applicationId);
            for (EndpointProfileSchemaDto dto : profileSchemas) {
                if (dto.getVersion() == sdkProfile.getProfileSchemaVersion()) {
                    viewDto.setProfileSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setProfileSchemaId(dto.getId());
                }
            }

            List<NotificationSchemaDto> notificationSchemas =
                    controlService.getNotificationSchemasByAppId(applicationId);
            for (NotificationSchemaDto dto : notificationSchemas) {
                if (dto.getVersion() == sdkProfile.getNotificationSchemaVersion()) {
                    viewDto.setNotificationSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setNotificationSchemaId(dto.getId());
                }
            }

            List<LogSchemaDto> logSchemas = controlService.getLogSchemasByApplicationId(applicationId);
            for (LogSchemaDto dto : logSchemas) {
                if (dto.getVersion() == sdkProfile.getLogSchemaVersion()) {
                    viewDto.setLogSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setLogSchemaId(dto.getId());
                }
            }

            return viewDto;
        } catch (ControlServiceException e) {
            throw Utils.handleException(e);
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
    public RecordField createSimpleEmptySchemaForm() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return simpleSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createCommonEmptySchemaForm() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return commonSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createConfigurationEmptySchemaForm() throws KaaAdminServiceException {
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
    public RecordField generateSimpleSchemaForm(String fileItemName) throws KaaAdminServiceException {
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
    public RecordField generateCommonSchemaForm(String fileItemName) throws KaaAdminServiceException {
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
    public RecordField generateConfigurationSchemaForm(String fileItemName) throws KaaAdminServiceException {
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
    public RecordField generateEcfSchemaForm(String fileItemName) throws KaaAdminServiceException {
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
    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getProfileSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto profileSchema = controlService.getProfileSchema(profileSchemaId);
            Utils.checkNotNull(profileSchema);
            checkApplicationId(profileSchema.getApplicationId());
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(profileSchema.getId())) {
                profileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(profileSchema.getApplicationId());
            } else {
                EndpointProfileSchemaDto storedProfileSchema = controlService.getProfileSchema(profileSchema.getId());
                Utils.checkNotNull(storedProfileSchema);
                checkApplicationId(storedProfileSchema.getApplicationId());
            }
            return controlService.editProfileSchema(profileSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaViewDto getProfileSchemaView(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto profileSchema = getProfileSchema(profileSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(profileSchema.getCtlSchemaId());
            return new ProfileSchemaViewDto(profileSchema, toCtlSchemaForm(ctlSchemaDto));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaViewDto saveProfileSchemaView(ProfileSchemaViewDto profileSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto profileSchema = profileSchemaView.getSchema();
            String applicationId = profileSchema.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = profileSchema.getCtlSchemaId();
            if (isEmpty(ctlSchemaId)) {
                if (profileSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = profileSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(), 
                            metaInfo.getVersion(), 
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    profileSchema.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = saveCTLSchemaForm(profileSchemaView.getCtlSchemaForm());
                    profileSchema.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }
            EndpointProfileSchemaDto savedProfileSchema = saveProfileSchema(profileSchema);
            return getProfileSchemaView(savedProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public ProfileSchemaViewDto createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            EndpointProfileSchemaDto profileSchema = new EndpointProfileSchemaDto();
            profileSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            profileSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            profileSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = saveCTLSchemaForm(ctlSchemaForm);
            profileSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            EndpointProfileSchemaDto savedProfileSchema = saveProfileSchema(profileSchema);
            return getProfileSchemaView(savedProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getServerProfileSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getServerProfileSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            List<ServerProfileSchemaDto> serverProfileSchemas = controlService.getServerProfileSchemasByApplicationId(applicationId);
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(serverProfileSchemas.size());
            for (ServerProfileSchemaDto serverProfileSchema : serverProfileSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(serverProfileSchema);
                RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), null);
                schemaInfo.setSchemaName(serverProfileSchema.getName());
                schemaInfo.setSchemaForm(schemaForm);
                schemaInfos.add(schemaInfo);
            }
            return schemaInfos;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public List<SchemaInfoDto> getServerProfileSchemaInfosByEndpointKey(String endpointKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto endpointProfile = getEndpointProfileByKeyHash(endpointKeyHash);
            List<ServerProfileSchemaDto> serverProfileSchemas = controlService.getServerProfileSchemasByApplicationId(endpointProfile.getApplicationId());
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(serverProfileSchemas.size());
            for (ServerProfileSchemaDto serverProfileSchema : serverProfileSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(serverProfileSchema);
                String body = null;
                if (schemaInfo.getVersion() == endpointProfile.getServerProfileVersion()) {
                    body = endpointProfile.getServerProfileBody();
                }
                RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), body);
                schemaInfo.setSchemaName(serverProfileSchema.getName());
                schemaInfo.setSchemaForm(schemaForm);
                schemaInfos.add(schemaInfo);
            }
            Collections.sort(schemaInfos, Collections.reverseOrder());
            return schemaInfos;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto profileSchema = controlService.getServerProfileSchema(serverProfileSchemaId);
            Utils.checkNotNull(profileSchema);
            checkApplicationId(profileSchema.getApplicationId());
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(serverProfileSchema.getId())) {
                serverProfileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(serverProfileSchema.getApplicationId());
            } else {
                ServerProfileSchemaDto storedServerProfileSchema = controlService.getServerProfileSchema(serverProfileSchema.getId());
                Utils.checkNotNull(storedServerProfileSchema);
                checkApplicationId(storedServerProfileSchema.getApplicationId());
            }
            return controlService.saveServerProfileSchema(serverProfileSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaViewDto getServerProfileSchemaView(String serverProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto serverProfileSchema = getServerProfileSchema(serverProfileSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(serverProfileSchema.getCtlSchemaId());
            return new ServerProfileSchemaViewDto(serverProfileSchema, toCtlSchemaForm(ctlSchemaDto));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaViewDto saveServerProfileSchemaView(ServerProfileSchemaViewDto serverProfileSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto serverProfileSchema = serverProfileSchemaView.getSchema();
            String applicationId = serverProfileSchema.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = serverProfileSchema.getCtlSchemaId();
            if (isEmpty(ctlSchemaId)) {
                if (serverProfileSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = serverProfileSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(), 
                            metaInfo.getVersion(), 
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    serverProfileSchema.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = saveCTLSchemaForm(serverProfileSchemaView.getCtlSchemaForm());
                    serverProfileSchema.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }
            ServerProfileSchemaDto savedServerProfileSchema = saveServerProfileSchema(serverProfileSchema);
            return getServerProfileSchemaView(savedServerProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public ServerProfileSchemaViewDto createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            ServerProfileSchemaDto serverProfileSchema = new ServerProfileSchemaDto();
            serverProfileSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            serverProfileSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            serverProfileSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = saveCTLSchemaForm(ctlSchemaForm);
            serverProfileSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            ServerProfileSchemaDto savedServerProfileSchema = saveServerProfileSchema(serverProfileSchema);
            return getServerProfileSchemaView(savedServerProfileSchema.getId());
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
    public SchemaInfoDto getEndpointProfileSchemaInfo(String endpointProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto endpointProfileSchema = controlService.getProfileSchema(endpointProfileSchemaId);
            SchemaInfoDto schemaInfo = new SchemaInfoDto(endpointProfileSchema);
            RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(endpointProfileSchema.getCtlSchemaId(), null);
            schemaInfo.setSchemaName(endpointProfileSchema.getName());
            schemaInfo.setSchemaForm(schemaForm);
            return schemaInfo;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SchemaInfoDto getServerProfileSchemaInfo(String serverProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto serverProfileSchema = controlService.getServerProfileSchema(serverProfileSchemaId);
            SchemaInfoDto schemaInfo = new SchemaInfoDto(serverProfileSchema);
            RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), null);
            schemaInfo.setSchemaName(serverProfileSchema.getName());
            schemaInfo.setSchemaForm(schemaForm);
            return schemaInfo;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public boolean testProfileFilter(RecordField endpointProfile, RecordField serverProfile, String filterBody) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            GenericRecord endpointProfileRecord = null;
            GenericRecord serverProfileRecord = null;
            try {
                if (endpointProfile != null) {
                    endpointProfileRecord = FormAvroConverter.createGenericRecordFromRecordField(endpointProfile);
                }
                if (serverProfile != null) {
                    serverProfileRecord = FormAvroConverter.createGenericRecordFromRecordField(serverProfile);
                }
            } catch (Exception e) {
                throw Utils.handleException(e);
            }
            try {
                Expression expression = new SpelExpressionParser().parseExpression(filterBody);
                StandardEvaluationContext evaluationContext;
                if (endpointProfileRecord != null) {
                    evaluationContext = new StandardEvaluationContext(endpointProfileRecord);
                    evaluationContext.setVariable(DefaultFilterEvaluator.CLIENT_PROFILE_VARIABLE_NAME, endpointProfileRecord);
                } else {
                    evaluationContext = new StandardEvaluationContext();
                }
                evaluationContext.addPropertyAccessor(new GenericRecordPropertyAccessor());
                evaluationContext.setVariable(DefaultFilterEvaluator.EP_KEYHASH_VARIABLE_NAME, "test");
                if (serverProfileRecord != null) {
                    evaluationContext.setVariable(DefaultFilterEvaluator.SERVER_PROFILE_VARIABLE_NAME, serverProfileRecord);
                }
                return expression.getValue(evaluationContext, Boolean.class);
            } catch (Exception e) {
                throw new KaaAdminServiceException("Invalid profile filter: " + e.getMessage(), e, ServiceErrorCode.BAD_REQUEST_PARAMS);
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
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
    public ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema, byte[] schema)
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
    public ConfigurationSchemaDto getConfigurationSchemaForm(String configurationSchemaId) throws KaaAdminServiceException {
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
    public ConfigurationSchemaDto editConfigurationSchemaForm(ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException {
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
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.findNotificationSchemasByAppIdAndType(applicationId, NotificationTypeDto.USER);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<VersionDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getUserNotificationSchemasByAppId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException {
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
    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException {
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
    public NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema, byte[] schema)
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
    public NotificationSchemaDto getNotificationSchemaForm(String notificationSchemaId) throws KaaAdminServiceException {
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
    public NotificationSchemaDto editNotificationSchemaForm(NotificationSchemaDto notificationSchema) throws KaaAdminServiceException {
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
    public List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
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
    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException {
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
    public List<VersionDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        List<VersionDto> logSchemaVersions = Collections.emptyList();
        try {
            checkApplicationId(applicationId);
            logSchemaVersions = controlService.getLogSchemaVersionsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        return logSchemaVersions;
    }

    @Override
    public LogSchemaDto editLogSchema(LogSchemaDto logSchema, byte[] schema) throws KaaAdminServiceException {
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
    public LogSchemaDto getLogSchemaForm(String logSchemaId) throws KaaAdminServiceException {
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
    public LogSchemaDto editLogSchemaForm(LogSchemaDto logSchema) throws KaaAdminServiceException {
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
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException {
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
    public List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId,
            boolean includeDeprecated) throws KaaAdminServiceException {
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
    public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
                                                                        String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            ProfileFilterRecordDto record = controlService.getProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId);
            Utils.checkNotNull(record);
            return record;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = getCurrentUser().getUsername();
            if (isEmpty(profileFilter.getId())) {
                profileFilter.setCreatedUsername(username);
                checkEndpointGroupId(profileFilter.getEndpointGroupId());
            } else {
                profileFilter.setModifiedUsername(username);
                ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilter.getId());
                Utils.checkNotNull(storedProfileFilter);
                checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());                
            }
            validateProfileFilterBody(profileFilter.getEndpointProfileSchemaId(),
                    profileFilter.getServerProfileSchemaId(),
                    profileFilter.getBody());
            return controlService.editProfileFilter(profileFilter);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    private void validateProfileFilterBody(String endpointProfileSchemaId, String serverProfileSchemaId, 
            String filterBody) throws KaaAdminServiceException {
        GenericRecord endpointProfileRecord = null;
        GenericRecord serverProfileRecord = null;
        try {
            if (endpointProfileSchemaId != null) {
                EndpointProfileSchemaDto endpointProfileSchema = getProfileSchema(endpointProfileSchemaId);
                endpointProfileRecord = getDefaultRecordFromCtlSchema(endpointProfileSchema.getCtlSchemaId());
            }
            if (serverProfileSchemaId != null) {
                ServerProfileSchemaDto serverProfileSchema = getServerProfileSchema(serverProfileSchemaId);
                serverProfileRecord = getDefaultRecordFromCtlSchema(serverProfileSchema.getCtlSchemaId());
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        try {
            Expression expression = new SpelExpressionParser().parseExpression(filterBody);
            StandardEvaluationContext evaluationContext;
            if (endpointProfileRecord != null) {
                evaluationContext = new StandardEvaluationContext(endpointProfileRecord);
                evaluationContext.setVariable(DefaultFilterEvaluator.CLIENT_PROFILE_VARIABLE_NAME, endpointProfileRecord);
            } else {
                evaluationContext = new StandardEvaluationContext();
            }
            evaluationContext.addPropertyAccessor(new GenericRecordPropertyAccessor());
            evaluationContext.setVariable(DefaultFilterEvaluator.EP_KEYHASH_VARIABLE_NAME, "test");
            if (serverProfileRecord != null) {
                evaluationContext.setVariable(DefaultFilterEvaluator.SERVER_PROFILE_VARIABLE_NAME, serverProfileRecord);
            }
            expression.getValue(evaluationContext, Boolean.class);
        } catch (Exception e) {
            throw new KaaAdminServiceException("Invalid profile filter body!", e, ServiceErrorCode.BAD_REQUEST_PARAMS);
        }
    }
    
    private GenericRecord getDefaultRecordFromCtlSchema(String ctlSchemaId) throws Exception {
        CTLSchemaDto ctlSchema = controlService.getCTLSchemaById(ctlSchemaId);
        Schema schema = controlService.exportCTLSchemaFlatAsSchema(ctlSchema);
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
        GenericRecord defaultRecord = converter.decodeJson(ctlSchema.getDefaultRecord());
        return defaultRecord;
    }

    @Override
    public ProfileFilterDto activateProfileFilter(String profileFilterId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilterId);
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            return controlService.activateProfileFilter(profileFilterId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilterId);
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            return controlService.deactivateProfileFilter(profileFilterId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterRecordDto record = controlService.getProfileFilterRecord(endpointProfileSchemaId,
                    serverProfileSchemaId, endpointGroupId);
            Utils.checkNotNull(record);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            controlService.deleteProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId, username);
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
            Utils.checkNotNull(record);
            return record;
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
            throw new KaaAdminServiceException("Someone has already updated the configuration. Reload page to be able to edit it.",
                    ServiceErrorCode.GENERAL_ERROR);
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
    public ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationDto storedConfiguration = deactivateConfiguration(configurationId);
            return toConfigurationRecordFormDto(storedConfiguration);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private ConfigurationRecordViewDto toConfigurationRecordView(ConfigurationRecordDto record)
            throws KaaAdminServiceException, IOException {

        ConfigurationSchemaDto schemaDto = this.getConfigurationSchema(record.getSchemaId());
        EndpointGroupDto endpointGroup = this.getEndpointGroup(record.getEndpointGroupId());

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

    private ConfigurationRecordFormDto toConfigurationRecordFormDto(ConfigurationDto configuration) throws KaaAdminServiceException,
            IOException {

        ConfigurationSchemaDto schemaDto = this.getConfigurationSchema(configuration.getSchemaId());
        EndpointGroupDto endpointGroup = this.getEndpointGroup(configuration.getEndpointGroupId());

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

    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            StructureRecordDto<ConfigurationDto> record = controlService.getConfigurationRecord(schemaId, endpointGroupId);
            Utils.checkNotNull(record);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            controlService.deleteConfigurationRecord(schemaId, endpointGroupId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getTopicsByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getTopicByAppId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getTopicByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
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
        GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(fieldConfiguration);
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
        byte[] rawConfiguration = GenericAvroConverter.toRawData(plugin.getJsonConfiguration(), pluginInfo.getFieldConfiguration()
                .getSchema());
        plugin.setRawConfiguration(rawConfiguration);
    }

    private void setPluginFormConfigurationFromRaw(PluginDto plugin, PluginType type) throws IOException {
        LOG.trace("Updating plugin {} configuration", plugin);
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        byte[] rawConfiguration = plugin.getRawConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(pluginInfo.getFieldConfiguration().getSchema());
        GenericRecord record = converter.decodeBinary(rawConfiguration);
        RecordField formData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
        plugin.setFieldConfiguration(formData);
    }

    private void setPluginJsonConfigurationFromRaw(PluginDto plugin, PluginType type) {
        PluginInfoDto pluginInfo = pluginsInfo.get(type).get(plugin.getPluginClassName());
        byte[] rawConfiguration = plugin.getRawConfiguration();
        String jsonConfiguration = GenericAvroConverter.toJson(rawConfiguration, pluginInfo.getFieldConfiguration().getSchema());
        plugin.setJsonConfiguration(jsonConfiguration);
    }

    @Override
    public List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException {
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
    public List<PluginInfoDto> getLogAppenderPluginInfos() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return new ArrayList<PluginInfoDto>(pluginsInfo.get(PluginType.LOG_APPENDER).values());
    }

    @Override
    public List<UserVerifierDto> getUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(appId);
            return controlService.getUserVerifiersByApplicationId(appId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto getUserVerifier(String userVerifierId) throws KaaAdminServiceException {
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
    public UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException {
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
    public void deleteUserVerifier(String userVerifierId) throws KaaAdminServiceException {
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
    public UserVerifierDto getUserVerifierForm(String userVerifierId) throws KaaAdminServiceException {
        UserVerifierDto userVerifier = getUserVerifier(userVerifierId);
        try {
            setPluginFormConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
            return userVerifier;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto editUserVerifierForm(UserVerifierDto userVerifier) throws KaaAdminServiceException {
        try {
            setPluginRawConfigurationFromForm(userVerifier);
            UserVerifierDto saved = editUserVerifier(userVerifier);
            return saved;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<UserVerifierDto> getRestUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException {
        List<UserVerifierDto> userVerifiers = getUserVerifiersByApplicationId(appId);
        for (UserVerifierDto userVerifier : userVerifiers) {
            setPluginJsonConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
        }
        return userVerifiers;
    }

    @Override
    public UserVerifierDto getRestUserVerifier(String userVerifierId) throws KaaAdminServiceException {
        UserVerifierDto userVerifier = getUserVerifier(userVerifierId);
        setPluginJsonConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
        return userVerifier;
    }

    @Override
    public UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException {
        setPluginRawConfigurationFromJson(userVerifier, PluginType.USER_VERIFIER);
        UserVerifierDto savedUserVerifier = editUserVerifier(userVerifier);
        setPluginJsonConfigurationFromRaw(savedUserVerifier, PluginType.USER_VERIFIER);
        return savedUserVerifier;
    }

    @Override
    public List<PluginInfoDto> getUserVerifierPluginInfos() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return new ArrayList<PluginInfoDto>(pluginsInfo.get(PluginType.USER_VERIFIER).values());
    }

    private void checkTopicId(String topicId) throws IllegalArgumentException {
        if (isEmpty(topicId)) {
            throw new IllegalArgumentException("The topicId parameter is empty.");
        }
    }

    @Override
    public void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException {
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
    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException {
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
    public RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException {
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
    public void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException {
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
    public NotificationDto sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException {
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
    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, byte[] body)
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
            unicastNotification.setEndpointKeyHash(Base64.decode(clientKeyHash.getBytes(Charsets.UTF_8)));
            unicastNotification.setNotificationDto(notification);
            return controlService.editUnicastNotification(unicastNotification);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            return controlService.getEventClassFamiliesByTenantId(getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws KaaAdminServiceException {
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
    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(eventClassFamily.getId())) {
                EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamily.getId());
                Utils.checkNotNull(storedEventClassFamily);
                checkTenantId(storedEventClassFamily.getTenantId());
            } else {
                String username = getCurrentUser().getUsername();
                eventClassFamily.setCreatedUsername(username);
            }
            eventClassFamily.setTenantId(getTenantId());
            return controlService.editEventClassFamily(eventClassFamily);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addEventClassFamilySchemaForm(String eventClassFamilyId, RecordField schemaForm) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            Schema schema = ecfSchemaFormAvroConverter.createSchemaFromSchemaForm(schemaForm);
            String schemaString = SchemaFormAvroConverter.createSchemaString(schema, true);

            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = getCurrentUser().getUsername();
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
    public void addEventClassFamilySchema(String eventClassFamilyId, byte[] data) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            checkEventClassFamilyId(eventClassFamilyId);
            String schema = new String(data);
            validateSchema(schema);

            EventClassFamilyDto storedEventClassFamily = controlService.getEventClassFamily(eventClassFamilyId);
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = getCurrentUser().getUsername();
            controlService.addEventClassFamilySchema(eventClassFamilyId, schema, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type)
            throws KaaAdminServiceException {
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
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getApplicationEventFamilyMapsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws KaaAdminServiceException {
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
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(applicationEventFamilyMap.getId())) {
                String username = getCurrentUser().getUsername();
                applicationEventFamilyMap.setCreatedUsername(username);
                checkApplicationId(applicationEventFamilyMap.getApplicationId());
            } else {
                ApplicationEventFamilyMapDto storedApplicationEventFamilyMap = controlService
                        .getApplicationEventFamilyMap(applicationEventFamilyMap.getId());
                Utils.checkNotNull(storedApplicationEventFamilyMap);
                checkApplicationId(storedApplicationEventFamilyMap.getApplicationId());
            }
            return controlService.editApplicationEventFamilyMap(applicationEventFamilyMap);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getVacantEventClassFamiliesByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException {
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
            tenantUser = new TenantUserDto(user.getId().toString(), user.getUsername(), user.getFirstName(), user.getLastName(),
                    user.getMail(), KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
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
                user.getUsername(), user.getFirstName(), user.getLastName(), user.getMail(), KaaAuthorityDto.valueOf(user.getAuthorities()
                        .iterator().next().getAuthority()));
        result.setId(tenantUser.getId());
        result.setTenantId(tenantUser.getTenantId());
        return result;
    }

    private Long saveUser(org.kaaproject.kaa.common.dto.admin.UserDto user) throws Exception {
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
            throw new KaaAdminServiceException("Failed to send email with temporary password. See server logs for details.",
                    ServiceErrorCode.GENERAL_ERROR);
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
    
    private CTLSchemaScopeDto detectScope(String tenantId, String applicationId) {
        CTLSchemaScopeDto scope = CTLSchemaScopeDto.SYSTEM;
        if (tenantId != null && !tenantId.isEmpty()) {
            if (applicationId != null && !applicationId.isEmpty()) {
                scope = CTLSchemaScopeDto.APPLICATION;
            } else {
                scope = CTLSchemaScopeDto.TENANT;
            }
        }
        return scope;
    }
    
    private void checkCTLSchemaReadScope(String tenantId, String applicationId) throws KaaAdminServiceException {
        AuthUserDto currentUser = getCurrentUser();
        CTLSchemaScopeDto scope = detectScope(tenantId, applicationId);
        boolean allowed = false;
        switch (currentUser.getAuthority()) {
        case KAA_ADMIN:
            allowed = scope == CTLSchemaScopeDto.SYSTEM;
            break;
        case TENANT_ADMIN:
            if (scope == CTLSchemaScopeDto.TENANT) {
                checkTenantId(tenantId);
            }            
            allowed = scope.getLevel() <= CTLSchemaScopeDto.TENANT.getLevel();
            break;
        case TENANT_DEVELOPER:
        case TENANT_USER:
            if (scope == CTLSchemaScopeDto.TENANT) {
                checkTenantId(tenantId);
            }
            if (scope.getLevel() >= CTLSchemaScopeDto.APPLICATION.getLevel()) {
                checkApplicationId(applicationId);
            }
            allowed = scope.getLevel() >= CTLSchemaScopeDto.SYSTEM.getLevel();
            break;
        default:
            break;
        }
        if (!allowed) {
            throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
        }
    }
    
    private void checkCTLSchemaEditScope(String tenantId, String applicationId) throws KaaAdminServiceException {
        AuthUserDto currentUser = getCurrentUser();
        CTLSchemaScopeDto scope = detectScope(tenantId, applicationId);
        boolean allowed = false;
        switch (currentUser.getAuthority()) {
        case KAA_ADMIN:
            allowed = scope == CTLSchemaScopeDto.SYSTEM;
            break;
        case TENANT_ADMIN:
            checkTenantId(tenantId);
            allowed = scope == CTLSchemaScopeDto.TENANT;
            break;
        case TENANT_DEVELOPER:
        case TENANT_USER:
            checkTenantId(tenantId);
            if (scope.getLevel() >= CTLSchemaScopeDto.APPLICATION.getLevel()) {
                checkApplicationId(applicationId);
            }
            allowed = scope.getLevel() >= CTLSchemaScopeDto.TENANT.getLevel();
            break;
        default:
            break;
        }
        if (!allowed) {
            throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    private void checkCTLSchemaId(String schemaId) throws KaaAdminServiceException {
        if (schemaId == null || schemaId.isEmpty()) {
            throw new IllegalArgumentException("Missing CTL schema ID!");
        }
    }
    
    private void checkCTLSchemaMetaInfoId(String metaInfoId) throws KaaAdminServiceException {
        if (metaInfoId == null || metaInfoId.isEmpty()) {
            throw new IllegalArgumentException("Missing CTL schema meta info ID!");
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

    /**
     * Returns a string that contains fully qualified names and version numbers
     * of the given CTL schemas.
     *
     * @param types
     *            A collection of CTL schemas
     *
     * @return A string that contains fully qualified names and version numbers
     *         of the given CTL schemas
     */
    
    private String asText(Collection<CTLSchemaDto> types) {
        StringBuilder message = new StringBuilder();
        if (types != null) {
            for (CTLSchemaDto type : types) {
                CTLSchemaMetaInfoDto details = type.getMetaInfo();
                message.append("\n").append("FQN: ").append(details.getFqn()).append(", version: ").append(type.getVersion());
            }
        }
        return message.toString();
    }

    @Override
    public CTLSchemaDto saveCTLSchema(String body, String tenantId, String applicationId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            checkCTLSchemaEditScope(tenantId, applicationId);            
            CTLSchemaParser parser = new CTLSchemaParser(controlService, tenantId);
            CTLSchemaDto schema = parser.parse(body, applicationId);
            // Check if the schema body is valid
            parser.validate(schema);
            CTLSchemaDto result = controlService.saveCTLSchema(schema);
            return result;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            Utils.checkNotNull(schema);
            checkCTLSchemaEditScope(schema.getMetaInfo().getTenantId(), schema.getMetaInfo().getApplicationId());
             // Check if the schema dependencies are present in the database
            List<FqnVersion> missingDependencies = new ArrayList<>();
            Set<CTLSchemaDto> dependencies = new HashSet<>();
            if (schema.getDependencySet() != null) {
                for (CTLSchemaDto dependency : schema.getDependencySet()) {
                    CTLSchemaDto schemaFound = 
                            controlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(
                                    dependency.getMetaInfo().getFqn(), dependency.getVersion(),
                            schema.getMetaInfo().getTenantId(), schema.getMetaInfo().getApplicationId());
                    if (schemaFound == null) {
                        missingDependencies.add(new FqnVersion(dependency.getMetaInfo().getFqn(), dependency.getVersion()));
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
            CTLSchemaParser parser = new CTLSchemaParser(controlService, schema.getMetaInfo().getTenantId());
            parser.validate(schema);

            CTLSchemaDto result = controlService.saveCTLSchema(schema);
            return result;
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
            checkCTLSchemaEditScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());

            List<CTLSchemaDto> schemaDependents = controlService.getCTLSchemaDependents(schemaId);
            if (schemaDependents != null && !schemaDependents.isEmpty()) {
                String message = "Can't delete the common type version as it is referenced by the following common type(s): "
                        + this.asText(schemaDependents);
                throw new IllegalArgumentException(message);
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaFqn(fqn);
            this.checkCTLSchemaVersion(version);
            if (!isEmpty(applicationId)) {
                this.checkApplicationId(applicationId);
            }
            CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
            Utils.checkNotNull(schemaFound);
            checkCTLSchemaEditScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
            List<CTLSchemaDto> schemaDependents = controlService.getCTLSchemaDependents(fqn, version, tenantId, applicationId);
            if (schemaDependents != null && !schemaDependents.isEmpty()) {
                String message = "Can't delete the common type version as it is referenced by the following common type(s): "
                        + this.asText(schemaDependents);
                throw new IllegalArgumentException(message);
            }

            controlService.deleteCTLSchemaByFqnAndVersionTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public CTLSchemaDto getCTLSchemaById(String schemaId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaId(schemaId);
            CTLSchemaDto schemaFound = controlService.getCTLSchemaById(schemaId);
            Utils.checkNotNull(schemaFound);
            checkCTLSchemaReadScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
            return schemaFound;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaFqn(fqn);
            this.checkCTLSchemaVersion(version);
            if (!isEmpty(applicationId)) {
                this.checkApplicationId(applicationId);
            }
            CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
            Utils.checkNotNull(schemaFound);
            checkCTLSchemaReadScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
            return schemaFound;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public boolean checkFqnExists(String fqn, String tenantId, String applicationId)
            throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaFqn(fqn);
            List<CTLSchemaMetaInfoDto> result = controlService.getSiblingsByFqnTenantIdAndApplicationId(fqn, tenantId, applicationId);
            return result != null && !result.isEmpty();
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo)
            throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            checkCTLSchemaEditScope(ctlSchemaMetaInfo.getTenantId(), ctlSchemaMetaInfo.getApplicationId());
            return controlService.updateCTLSchemaMetaInfoScope(ctlSchemaMetaInfo);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            return controlService.getSystemCTLSchemasMetaInfo();
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            AuthUserDto currentUser = getCurrentUser();
            return controlService.getAvailableCTLSchemasMetaInfoForTenant(currentUser.getTenantId());
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemas(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(applicationId);
            AuthUserDto currentUser = getCurrentUser();
            return controlService.getAvailableCTLSchemasMetaInfoForApplication(currentUser.getTenantId(), applicationId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public List<CtlSchemaReferenceDto> getAvailableApplicationCTLSchemaReferences(String applicationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(applicationId);
            AuthUserDto currentUser = getCurrentUser();
            List<CtlSchemaReferenceDto> result = new ArrayList<>();
            List<CTLSchemaMetaInfoDto> availableMetaInfo = controlService.getAvailableCTLSchemasMetaInfoForApplication(currentUser.getTenantId(), applicationId);
            for (CTLSchemaMetaInfoDto metaInfo : availableMetaInfo) {
                for (int version : metaInfo.getVersions()) {
                    result.add(new CtlSchemaReferenceDto(metaInfo, version));
                }
            }
            return result;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    private CtlSchemaFormDto toCtlSchemaForm(CTLSchemaDto ctlSchema) throws KaaAdminServiceException {
        try {
            CtlSchemaFormDto ctlSchemaForm = new CtlSchemaFormDto(ctlSchema);
            SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(ctlSchemaForm.getMetaInfo().getTenantId(), ctlSchemaForm.getMetaInfo().getApplicationId());
            RecordField form = converter.createSchemaFormFromSchema(ctlSchema.getBody());
            ctlSchemaForm.setSchema(form);
            List<Integer> availableVersions = controlService.getAllCTLSchemaVersionsByFqnTenantIdAndApplicationId(
                    ctlSchema.getMetaInfo().getFqn(), ctlSchema.getMetaInfo().getTenantId(), ctlSchema.getMetaInfo().getApplicationId());
            availableVersions = availableVersions == null ? Collections.<Integer>emptyList() : availableVersions;
            Collections.sort(availableVersions);
            ctlSchemaForm.getMetaInfo().setVersions(availableVersions);
            return ctlSchemaForm;
        }
        catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public CtlSchemaFormDto getLatestCTLSchemaForm(String metaInfoId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaMetaInfoId(metaInfoId);
            CTLSchemaDto ctlSchema = controlService.getLatestCTLSchemaByMetaInfoId(metaInfoId);
            Utils.checkNotNull(ctlSchema);
            checkCTLSchemaReadScope(ctlSchema.getMetaInfo().getTenantId(), ctlSchema.getMetaInfo().getApplicationId());
            return toCtlSchemaForm(ctlSchema);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public CtlSchemaFormDto getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            this.checkCTLSchemaMetaInfoId(metaInfoId);
            this.checkCTLSchemaVersion(version);
            CTLSchemaDto schemaFound = controlService.getCTLSchemaByMetaInfoIdAndVer(metaInfoId, version);
            Utils.checkNotNull(schemaFound);
            checkCTLSchemaReadScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
            return toCtlSchemaForm(schemaFound);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public CtlSchemaFormDto createNewCTLSchemaFormInstance(String metaInfoId, 
            Integer sourceVersion, String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(getCurrentUser().getTenantId(), applicationId);
            CtlSchemaFormDto sourceCtlSchema = null;
            if (!isEmpty(metaInfoId) && sourceVersion != null) {
                sourceCtlSchema = getCTLSchemaFormByMetaInfoIdAndVer(metaInfoId, sourceVersion);
                Utils.checkNotNull(sourceCtlSchema);
            }
            CtlSchemaFormDto ctlSchemaForm = null;
            if (sourceCtlSchema != null) {
                checkCTLSchemaEditScope(sourceCtlSchema.getMetaInfo().getTenantId(), sourceCtlSchema.getMetaInfo().getApplicationId());
                ctlSchemaForm = new CtlSchemaFormDto();
                ctlSchemaForm.setMetaInfo(sourceCtlSchema.getMetaInfo());
                RecordField form = sourceCtlSchema.getSchema();
                form.updateVersion(form.getContext().getMaxVersion(new Fqn(sourceCtlSchema.getMetaInfo().getFqn()))+1);
                ctlSchemaForm.setSchema(form);
            } else {
                checkCTLSchemaEditScope(getCurrentUser().getTenantId(), applicationId);
                ctlSchemaForm = new CtlSchemaFormDto();
                RecordField form = converter.getEmptySchemaFormInstance();
                form.updateVersion(1);
                ctlSchemaForm.setSchema(form);
                CTLSchemaMetaInfoDto metaInfo = new CTLSchemaMetaInfoDto(null, 
                        getCurrentUser().getTenantId(), applicationId);
                ctlSchemaForm.setMetaInfo(metaInfo);
            }
            return ctlSchemaForm;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public RecordField generateCtlSchemaForm(String fileItemName, String applicationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            checkCTLSchemaReadScope(getCurrentUser().getTenantId(), applicationId);
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(getCurrentUser().getTenantId(), applicationId);
            RecordField form = converter.createSchemaFormFromSchema(avroSchema);
            if (form.getVersion() == null) {
                form.updateVersion(1);
            }
            return form;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public CtlSchemaFormDto saveCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            AuthUserDto currentUser = getCurrentUser();
            CTLSchemaDto ctlSchema = null;
            if (!isEmpty(ctlSchemaForm.getId())) {
                ctlSchema = getCTLSchemaById(ctlSchemaForm.getId());
                if (ctlSchema == null) {
                    throw new KaaAdminServiceException(
                            "Requested item was not found!",
                            ServiceErrorCode.ITEM_NOT_FOUND);
                }
            } else {
                ctlSchema = new CTLSchemaDto();
            }
            if (isEmpty(ctlSchema.getId())) {
                ctlSchema.setCreatedUsername(currentUser.getUsername());
                RecordField schemaForm = ctlSchemaForm.getSchema();
                ctlSchema.setMetaInfo(ctlSchemaForm.getMetaInfo());
                ctlSchema.getMetaInfo().setFqn(schemaForm.getDeclaredFqn().getFqnString());
                ctlSchema.getMetaInfo().setTenantId(currentUser.getTenantId());
                ctlSchema.setVersion(schemaForm.getVersion());
                List<FqnVersion> dependenciesList = schemaForm.getContext().getCtlDependenciesList();
                Set<CTLSchemaDto> dependencies = new HashSet<>();
                List<FqnVersion> missingDependencies = new ArrayList<>();
                for (FqnVersion fqnVersion : dependenciesList) {
                    CTLSchemaDto dependency = controlService.getAnyCTLSchemaByFqnVersionTenantIdAndApplicationId(
                            fqnVersion.getFqnString(), fqnVersion.getVersion(), 
                            ctlSchema.getMetaInfo().getTenantId(), ctlSchema.getMetaInfo().getApplicationId());
                    if (dependency != null) {
                        dependencies.add(dependency);
                    } else {
                        missingDependencies.add(fqnVersion);
                    }
                }
                if (!missingDependencies.isEmpty()) {
                    String message = "The following dependencies are missing from the database: " + Arrays.toString(missingDependencies.toArray());
                    throw new IllegalArgumentException(message);
                }
                ctlSchema.setDependencySet(dependencies);
                SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(ctlSchema.getMetaInfo().getTenantId(), 
                        ctlSchema.getMetaInfo().getApplicationId());
                Schema avroSchema = converter.createSchemaFromSchemaForm(schemaForm);
                String schemaBody = SchemaFormAvroConverter.createSchemaString(avroSchema, true);
                ctlSchema.setBody(schemaBody);
            }
            
            CTLSchemaDto savedCtlSchema = saveCTLSchema(ctlSchema);
            if (savedCtlSchema != null) {
                CtlSchemaFormDto result = new CtlSchemaFormDto(savedCtlSchema);
                SchemaFormAvroConverter converter = getCtlSchemaConverterForScope(savedCtlSchema.getMetaInfo().getTenantId(),
                        savedCtlSchema.getMetaInfo().getApplicationId());
                RecordField form = converter.createSchemaFormFromSchema(savedCtlSchema.getBody());
                result.setSchema(form);
                List<Integer> availableVersions = controlService.getAllCTLSchemaVersionsByFqnTenantIdAndApplicationId(
                        savedCtlSchema.getMetaInfo().getFqn(), savedCtlSchema.getMetaInfo().getTenantId(), savedCtlSchema.getMetaInfo().getApplicationId()); 
                availableVersions = availableVersions == null ? Collections.<Integer>emptyList() : availableVersions;
                Collections.sort(availableVersions);
                result.getMetaInfo().setVersions(availableVersions);
                return result;
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
        return null;
    }
    
    @Override
    public boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            if (isEmpty(ctlSchemaForm.getId())) {
                AuthUserDto currentUser = getCurrentUser();
                RecordField schemaForm = ctlSchemaForm.getSchema();
                String fqn = schemaForm.getDeclaredFqn().getFqnString();
                String tenantId = currentUser.getTenantId();
                return checkFqnExists(fqn, tenantId, ctlSchemaForm.getMetaInfo().getApplicationId());
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
        return false;
    }
    
    private SchemaFormAvroConverter getCtlSchemaConverterForScope(String tenantId, String applicationId) throws KaaAdminServiceException {
        try {
            if (isEmpty(tenantId)) {
                return getCtlSchemaConverterForSystem(); 
            }
            if (isEmpty(applicationId)) {
                return getCtlSchemaConverterForTenant(tenantId);
            }
            return getCtlSchemaConverterForApplication(tenantId, applicationId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    private SchemaFormAvroConverter getCtlSchemaConverterForSystem() throws KaaAdminServiceException {
        try {
            return createSchemaConverterFromCtlTypes(controlService.getAvailableCTLSchemaVersionsForSystem());
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private SchemaFormAvroConverter getCtlSchemaConverterForTenant(String tenantId) throws KaaAdminServiceException {
        try {
            return createSchemaConverterFromCtlTypes(controlService.getAvailableCTLSchemaVersionsForTenant(tenantId));
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    private SchemaFormAvroConverter getCtlSchemaConverterForApplication(String tenantId, String applicationId) throws KaaAdminServiceException {
        try {
            return createSchemaConverterFromCtlTypes(controlService.getAvailableCTLSchemaVersionsForApplication(tenantId, applicationId));
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    private SchemaFormAvroConverter createSchemaConverterFromCtlTypes(final Map<Fqn, List<Integer>> ctlTypes) throws KaaAdminServiceException {
        try {
            CtlSource ctlSource = new CtlSource() {
                @Override
                public Map<Fqn, List<Integer>> getCtlTypes() {
                    return ctlTypes;
                }
            };
            return new SchemaFormAvroConverter(ctlSource);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public FileData exportCTLSchema(String fqn, int version, String applicationId, CTLSchemaExportMethod method) throws KaaAdminServiceException {
        try {
            this.checkCTLSchemaFqn(fqn);
            this.checkCTLSchemaVersion(version);
            String tenantId = getCurrentUser().getTenantId();
            CTLSchemaDto schemaFound = controlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(fqn, version, tenantId, applicationId);
            Utils.checkNotNull(schemaFound);
            checkCTLSchemaReadScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
            switch (method) {
            case SHALLOW:
                return controlService.exportCTLSchemaShallow(schemaFound);
            case FLAT:
                return controlService.exportCTLSchemaFlat(schemaFound);
            case DEEP:
                return controlService.exportCTLSchemaDeep(schemaFound);
            default:
                throw new IllegalArgumentException("The export method " + method.name() + " is not currently supported!");
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }
    
    @Override
    public String prepareCTLSchemaExport(String ctlSchemaId,
            CTLSchemaExportMethod method) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.values());
        try {
            CTLSchemaDto schemaFound = controlService.getCTLSchemaById(ctlSchemaId);
            Utils.checkNotNull(schemaFound);
            checkCTLSchemaReadScope(schemaFound.getMetaInfo().getTenantId(), schemaFound.getMetaInfo().getApplicationId());
            CtlSchemaExportKey key = new CtlSchemaExportKey(ctlSchemaId, method);
            return Base64.encodeObject(key, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
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
