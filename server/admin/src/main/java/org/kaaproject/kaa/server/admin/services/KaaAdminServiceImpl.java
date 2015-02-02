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
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDtoList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.iharder.Base64;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.AbstractSchemaDto;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
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
import org.kaaproject.kaa.common.dto.admin.SdkKey;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.dao.PropertiesFacade;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.entity.gen.GeneralProperties;
import org.kaaproject.kaa.server.admin.services.entity.gen.SmtpMailProperties;
import org.kaaproject.kaa.server.admin.services.messaging.MessagingService;
import org.kaaproject.kaa.server.admin.services.thrift.ControlThriftClientProvider;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.file.FileData;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.log.shared.annotation.KaaAppenderConfig;
import org.kaaproject.kaa.server.common.log.shared.config.AppenderConfig;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("kaaAdminService")
public class KaaAdminServiceImpl implements KaaAdminService, InitializingBean {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(KaaAdminServiceImpl.class);

    @Autowired
    private ControlThriftClientProvider clientProvider;

    @Autowired
    private UserFacade userFacade;
    
    @Autowired
    private PropertiesFacade propertiesFacade;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private CacheService cacheService;

    private PasswordEncoder passwordEncoder;
    
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    private List<LogAppenderInfoDto> appenders = new ArrayList<>();
    private Map<String, Schema> appenderConfigSchemas = new HashMap<>();
    
    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(KaaAppenderConfig.class));
        Set<BeanDefinition> beans = scanner.findCandidateComponents("org.kaaproject.kaa.server.appenders");
        for (BeanDefinition bean : beans) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());
            AppenderConfig appenderConfig = (AppenderConfig)clazz.newInstance();
            appenderConfigSchemas.put(appenderConfig.getLogAppenderClass(), appenderConfig.getConfigSchema());
            RecordField appenderConfigForm = FormAvroConverter.createRecordFieldFromSchema(appenderConfig.getConfigSchema());
            LogAppenderInfoDto appender = new LogAppenderInfoDto(
                    appenderConfig.getName(), appenderConfigForm, appenderConfig.getLogAppenderClass());
            appenders.add(appender);
        }
    }

    @Override
    public List<TenantUserDto> getTenants() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            List<TenantAdminDto> tenantAdmins = toDtoList(clientProvider.getClient().getTenantAdmins());
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
            UserDto user = toDto(clientProvider.getClient().getUser(userId));
            Utils.checkNotNull(user);
            TenantAdminDto tenantAdmin = toDto(clientProvider.getClient().getTenantAdmin(user.getTenantId()));
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
            TenantAdminDto savedTenantAdmin = toDto(clientProvider.getClient().editTenantAdmin(toDataStruct(tenantAdmin)));
            return toTenantUser(savedTenantAdmin);

        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteTenant(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            UserDto user = toDto(clientProvider.getClient().getUser(userId));
            Utils.checkNotNull(user);
            TenantAdminDto tenantAdmin = toDto(clientProvider.getClient().getTenantAdmin(user.getTenantId()));
            Utils.checkNotNull(tenantAdmin);
            userFacade.deleteUser(Long.valueOf(tenantAdmin.getExternalUid()));
            clientProvider.getClient().deleteTenantAdmin(user.getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<org.kaaproject.kaa.common.dto.admin.UserDto> getUsers()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            List<UserDto> users = toDtoList(clientProvider.getClient().getTenantUsers(getTenantId()));
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
            UserDto user = toDto(clientProvider.getClient().getUser(userId));
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

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto editUserProfile(org.kaaproject.kaa.common.dto.admin.UserDto userDto)
            throws KaaAdminServiceException {
        try {
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
                UserDto storedUser = toDto(clientProvider.getClient().getUser(user.getId()));
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
            UserDto savedUser = toDto(clientProvider.getClient().editUser(toDataStruct(userDto)));
            return toUser(savedUser);

        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteUser(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            UserDto user = toDto(clientProvider.getClient().getUser(userId));
            Utils.checkNotNull(user);
            checkTenantId(user.getTenantId());
            userFacade.deleteUser(Long.valueOf(user.getExternalUid()));
            clientProvider.getClient().deleteUser(user.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }

    }

    @Override
    public List<ApplicationDto> getApplications() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return toDtoList(clientProvider.getClient().getApplicationsByTenantId(getTenantId()));
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
    public ApplicationDto editApplication(ApplicationDto application) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(application.getId())) {
                checkApplicationId(application.getId());
            }
            application.setTenantId(getTenantId());
            return toDto(clientProvider.getClient().editApplication(toDataStruct(application)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteApplication(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            checkApplicationId(applicationId);
            clientProvider.getClient().deleteApplication(applicationId);
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

            List<SchemaDto> configurationSchemaVersions = toDtoList(clientProvider.getClient().getConfigurationSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setConfigurationSchemaVersions(configurationSchemaVersions);

            List<SchemaDto> profileSchemaVersions = toDtoList(clientProvider.getClient().getProfileSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setProfileSchemaVersions(profileSchemaVersions);

            List<SchemaDto> notificationSchemaVersions = toDtoList(clientProvider.getClient().getNotificationSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setNotificationSchemaVersions(notificationSchemaVersions);

            List<SchemaDto> logSchemaVersions = toDtoList( clientProvider.getClient().getLogSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setLogSchemaVersions(logSchemaVersions);

            return schemaVersions;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getSdk(String applicationId, Integer configurationSchemaVersion,
            Integer profileSchemaVersion, Integer notificationSchemaVersion,
            SdkPlatform targetPlatform, List<String> aefMapIds, Integer logSchemaVersion) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            SdkKey sdkKey = new SdkKey(applicationId, configurationSchemaVersion, profileSchemaVersion, notificationSchemaVersion, logSchemaVersion, targetPlatform, aefMapIds);
            return Base64.encodeObject(sdkKey, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public FileData getSdk(SdkKey key) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(key.getApplicationId());
            Sdk sdk = cacheService.getSdk(key);
            FileData data = new FileData();
            data.setFileName(sdk.getFileName());
            data.setContentType(key.getTargetPlatform().getContentType());
            data.setFileData(sdk.getData());
            return data;
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
                for (SdkKey key : cacheService.getCachedSdkKeys(application.getId())) {
                    cacheService.flushSdk(key);
                }
            }
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
            return toDtoList(clientProvider.getClient().getProfileSchemasByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileSchemaDto profileSchema = toDto(clientProvider.getClient().getProfileSchema(profileSchemaId));
            Utils.checkNotNull(profileSchema);
            checkApplicationId(profileSchema.getApplicationId());
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto editProfileSchema(ProfileSchemaDto profileSchema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(profileSchema.getId())) {
                profileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(profileSchema.getApplicationId());
                setSchema(profileSchema, fileItemName);
            }
            else {
                ProfileSchemaDto storedProfileSchema = toDto(clientProvider.getClient().getProfileSchema(profileSchema.getId()));
                Utils.checkNotNull(storedProfileSchema);
                checkApplicationId(storedProfileSchema.getApplicationId());
                profileSchema.setSchema(storedProfileSchema.getSchema());
            }
            return toDto(clientProvider.getClient().editProfileSchema(toDataStruct(profileSchema)));
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
            }
            else {
                ProfileSchemaDto storedProfileSchema = toDto(clientProvider.getClient().getProfileSchema(profileSchema.getId()));
                Utils.checkNotNull(storedProfileSchema);
                checkApplicationId(storedProfileSchema.getApplicationId());
                profileSchema.setSchema(storedProfileSchema.getSchema());
            }
            return toDto(clientProvider.getClient().editProfileSchema(toDataStruct(profileSchema)));
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
            return toDtoList(clientProvider.getClient().getConfigurationSchemasByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto getConfigurationSchema(
            String configurationSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ConfigurationSchemaDto configurationSchema = toDto(clientProvider.getClient().getConfigurationSchema(configurationSchemaId));
            Utils.checkNotNull(configurationSchema);
            checkApplicationId(configurationSchema.getApplicationId());
            return configurationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto editConfigurationSchema(
            ConfigurationSchemaDto configurationSchema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(configurationSchema.getId())) {
                configurationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(configurationSchema.getApplicationId());
                setSchema(configurationSchema, fileItemName);
            }
            else {
                ConfigurationSchemaDto storedConfigurationSchema = toDto(clientProvider.getClient().getConfigurationSchema(configurationSchema.getId()));
                Utils.checkNotNull(storedConfigurationSchema);
                checkApplicationId(storedConfigurationSchema.getApplicationId());
                configurationSchema.setSchema(storedConfigurationSchema.getSchema());
            }
            return toDto(clientProvider.getClient().editConfigurationSchema(toDataStruct(configurationSchema)));
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
            }
            else {
                ConfigurationSchemaDto storedConfigurationSchema = toDto(clientProvider.getClient().getConfigurationSchema(configurationSchema.getId()));
                Utils.checkNotNull(storedConfigurationSchema);
                checkApplicationId(storedConfigurationSchema.getApplicationId());
                configurationSchema.setSchema(storedConfigurationSchema.getSchema());
            }
            return toDto(clientProvider.getClient().editConfigurationSchema(toDataStruct(configurationSchema)));
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
            return toDtoList(clientProvider.getClient().findNotificationSchemasByAppIdAndType(applicationId,
                    toGenericDataStruct(NotificationTypeDto.USER)));
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
            return toDtoList(clientProvider.getClient().getUserNotificationSchemasByAppId(applicationId));
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
            List<NotificationSchemaDto> notificationSchemas = toDtoList(clientProvider.getClient().findNotificationSchemasByAppIdAndType(applicationId,
                    toGenericDataStruct(NotificationTypeDto.USER)));
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
            NotificationSchemaDto notificationSchema = toDto(clientProvider.getClient().getNotificationSchema(notificationSchemaId));
            Utils.checkNotNull(notificationSchema);
            checkApplicationId(notificationSchema.getApplicationId());
            return notificationSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaDto editNotificationSchema(
            NotificationSchemaDto notificationSchema, String fileItemName)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(notificationSchema.getId())) {
                notificationSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(notificationSchema.getApplicationId());
                setSchema(notificationSchema, fileItemName);
            }
            else {
                NotificationSchemaDto storedNotificationSchema = toDto(clientProvider.getClient().getNotificationSchema(notificationSchema.getId()));
                Utils.checkNotNull(storedNotificationSchema);
                checkApplicationId(storedNotificationSchema.getApplicationId());
                notificationSchema.setSchema(storedNotificationSchema.getSchema());
            }
            notificationSchema.setType(NotificationTypeDto.USER);
            return toDto(clientProvider.getClient().editNotificationSchema(toDataStruct(notificationSchema)));
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
            }
            else {
                NotificationSchemaDto storedNotificationSchema = toDto(clientProvider.getClient().getNotificationSchema(notificationSchema.getId()));
                Utils.checkNotNull(storedNotificationSchema);
                checkApplicationId(storedNotificationSchema.getApplicationId());
                notificationSchema.setSchema(storedNotificationSchema.getSchema());
            }
            notificationSchema.setType(NotificationTypeDto.USER);
            return toDto(clientProvider.getClient().editNotificationSchema(toDataStruct(notificationSchema)));
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
            return toDtoList(clientProvider.getClient().getLogSchemasByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogSchemaDto logSchema = toDto(clientProvider.getClient().getLogSchema(logSchemaId));
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
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplicationByApplicationToken(applicationToken));
            Utils.checkNotNull(storedApplication);
            checkTenantId(storedApplication.getTenantId());
            LogSchemaDto logSchema = toDto(clientProvider.getClient().getLogSchemaByApplicationIdAndVersion(storedApplication.getId(), version));
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
            logSchemaVersions = toDtoList(clientProvider.getClient().getLogSchemaVersionsByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        return logSchemaVersions;
    }

    @Override
    public LogSchemaDto editLogSchema(LogSchemaDto logSchema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(logSchema.getId())) {
                logSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(logSchema.getApplicationId());
                setSchema(logSchema, fileItemName);
            }
            else {
                LogSchemaDto storedLogSchema = toDto(clientProvider.getClient().getLogSchema(logSchema.getId()));
                Utils.checkNotNull(storedLogSchema);
                checkApplicationId(storedLogSchema.getApplicationId());
                logSchema.setSchema(storedLogSchema.getSchema());
            }
            return toDto(clientProvider.getClient().editLogSchema(toDataStruct(logSchema)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
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
            }
            else {
                LogSchemaDto storedLogSchema = toDto(clientProvider.getClient().getLogSchema(logSchema.getId()));
                Utils.checkNotNull(storedLogSchema);
                checkApplicationId(storedLogSchema.getApplicationId());
                logSchema.setSchema(storedLogSchema.getSchema());
            }
            return toDto(clientProvider.getClient().editLogSchema(toDataStruct(logSchema)));
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
            return toDtoList(clientProvider.getClient().getEndpointGroupsByApplicationId(applicationId));
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
            }
            else {
                checkEndpointGroupId(endpointGroup.getId());
            }
            return toDto(clientProvider.getClient().editEndpointGroup(toDataStruct(endpointGroup)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            clientProvider.getClient().deleteEndpointGroup(endpointGroupId);
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
            return toGenericDtoList(clientProvider.getClient().getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public StructureRecordDto<ProfileFilterDto> getProfileFilterRecord(
            String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            StructureRecordDto<ProfileFilterDto> record = toGenericDto(clientProvider.getClient().getProfileFilterRecord(schemaId, endpointGroupId));
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
            return toDtoList(clientProvider.getClient().getVacantProfileSchemasByEndpointGroupId(endpointGroupId));
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
            }
            else {
                profileFilter.setModifiedUsername(username);
                ProfileFilterDto storedProfileFilter = toDto(clientProvider.getClient().getProfileFilter(profileFilter.getId()));
                Utils.checkNotNull(storedProfileFilter);
                checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            }
            return toDto(clientProvider.getClient().editProfileFilter(toDataStruct(profileFilter)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto activateProfileFilter(String profileFilterId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = toDto(clientProvider.getClient().getProfileFilter(profileFilterId));
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            return toDto(clientProvider.getClient().activateProfileFilter(profileFilterId, username));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto deactivateProfileFilter(String profileFilterId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = toDto(clientProvider.getClient().getProfileFilter(profileFilterId));
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            return toDto(clientProvider.getClient().deactivateProfileFilter(profileFilterId, username));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteProfileFilterRecord(String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            StructureRecordDto<ProfileFilterDto> record = toGenericDto(clientProvider.getClient().getProfileFilterRecord(schemaId, endpointGroupId));
            Utils.checkNotNull(record);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            clientProvider.getClient().deleteProfileFilterRecord(schemaId, endpointGroupId, username);
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
            return toGenericDtoList(clientProvider.getClient().getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
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
            StructureRecordDto<ConfigurationDto> record = toGenericDto(clientProvider.getClient().getConfigurationRecord(schemaId, endpointGroupId));
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
          }
          else {
              configuration.setModifiedUsername(username);
              ConfigurationDto storedConfiguration = toDto(clientProvider.getClient().getConfiguration(configuration.getId()));
              Utils.checkNotNull(storedConfiguration);
              checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
          }
          return toDto(clientProvider.getClient().editConfiguration(toDataStruct(configuration)));
      } catch (Exception e) {
          throw Utils.handleException(e);
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
      } catch (Exception e) {
          throw Utils.handleException(e);
      }
  }
  
  @Override
  public ConfigurationDto activateConfiguration(String configurationId)
          throws KaaAdminServiceException {
      checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
      try {
          ConfigurationDto storedConfiguration = toDto(clientProvider.getClient().getConfiguration(configurationId));
          Utils.checkNotNull(storedConfiguration);
          checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
          String username = this.getCurrentUser().getUsername();
          ConfigurationDto stored = toDto(clientProvider.getClient().activateConfiguration(configurationId, username));
          return toConfigurationRecordFormDto(stored);
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
          ConfigurationDto storedConfiguration = toDto(clientProvider.getClient().getConfiguration(configurationId));
          Utils.checkNotNull(storedConfiguration);
          checkEndpointGroupId(storedConfiguration.getEndpointGroupId());
          String username = this.getCurrentUser().getUsername();
          ConfigurationDto stored = toDto(clientProvider.getClient().deactivateConfiguration(configurationId, username));
          return toConfigurationRecordFormDto(stored);
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
            body = converter.endcodeToJson(record);
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
            return toDtoList(clientProvider.getClient().getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId));
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
    
    private List<SchemaInfoDto> toConfigurationSchemaInfos (List<SchemaDto> schemas, EndpointGroupDto endpointGroup) 
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
            StructureRecordDto<ConfigurationDto> record = toGenericDto(clientProvider.getClient().getConfigurationRecord(schemaId, endpointGroupId));
            Utils.checkNotNull(record);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = this.getCurrentUser().getUsername();
            clientProvider.getClient().deleteConfigurationRecord(schemaId, endpointGroupId, username);
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
            return toDtoList(clientProvider.getClient().getTopicByAppId(applicationId));
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
            return toDtoList(clientProvider.getClient().getTopicByEndpointGroupId(endpointGroupId));
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
            return toDtoList(clientProvider.getClient().getVacantTopicByEndpointGroupId(endpointGroupId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TopicDto getTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            TopicDto topic = toDto(clientProvider.getClient().getTopic(topicId));
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
            }
            else {
                TopicDto storedTopic = toDto(clientProvider.getClient().getTopic(topic.getId()));
                Utils.checkNotNull(storedTopic);
                checkApplicationId(storedTopic.getApplicationId());
            }
            return toDto(clientProvider.getClient().editTopic(toDataStruct(topic)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            TopicDto topic = toDto(clientProvider.getClient().getTopic(topicId));
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            clientProvider.getClient().deleteTopicById(topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<LogAppenderDto> getLogAppendersByApplicationId(String appId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(appId);
            return toDtoList(clientProvider.getClient().getLogAppendersByApplicationId(appId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogAppenderDto logAppender = toDto(clientProvider.getClient().getLogAppender(appenderId));
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
            }
            else {
                LogAppenderDto storedlLogAppender = toDto(clientProvider.getClient().getLogAppender(appender.getId()));
                Utils.checkNotNull(storedlLogAppender);
                checkApplicationId(storedlLogAppender.getApplicationId());
            }
            return toDto(clientProvider.getClient().editLogAppender(toDataStruct(appender)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException {
        List<LogAppenderDto> logAppenders = getLogAppendersByApplicationId(appId);
        List<LogAppenderDto> restLogAppenders = new ArrayList<>(logAppenders.size());
        for (LogAppenderDto logAppender : logAppenders) {
            restLogAppenders.add(toRestLogAppender(logAppender));
        }
        return restLogAppenders;
    }

    @Override
    public LogAppenderDto getRestLogAppender(String appenderId) throws KaaAdminServiceException {
        LogAppenderDto logAppender = getLogAppender(appenderId);
        return toRestLogAppender(logAppender);
    }

    @Override
    public LogAppenderDto editRestLogAppender(LogAppenderDto restLogAppender) throws KaaAdminServiceException {
        LogAppenderDto logAppender = toLogAppender(restLogAppender);
        LogAppenderDto savedLogAppender = editLogAppender(logAppender);
        return toRestLogAppender(savedLogAppender);
    }

    private LogAppenderDto toRestLogAppender(LogAppenderDto logAppender) {
        LogAppenderDto restLogAppender = new LogAppenderDto(logAppender);
        Schema schema = appenderConfigSchemas.get(restLogAppender.getPluginClassName());
        String configuration = GenericAvroConverter.toJson(logAppender.getRawConfiguration(), schema.toString());
        restLogAppender.setJsonConfiguration(configuration);
        return restLogAppender;
    }
    
    private LogAppenderDto toLogAppender(LogAppenderDto restLogAppender) {
        LogAppenderDto logAppender = new LogAppenderDto(restLogAppender);
        Schema schema = appenderConfigSchemas.get(restLogAppender.getPluginClassName());
        byte[] rawConfiguration = GenericAvroConverter.toRawData(restLogAppender.getJsonConfiguration(), schema.toString());
        logAppender.setRawConfiguration(rawConfiguration);
        return logAppender;
    }

    @Override
    public LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException {
        LogAppenderDto logAppender =  getLogAppender(appenderId);
        try {
            LogAppenderDto wrapper = new LogAppenderDto(logAppender);
            Schema schema = appenderConfigSchemas.get(wrapper.getPluginClassName());
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
            GenericRecord record = converter.decodeBinary(logAppender.getRawConfiguration());
            RecordField formData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
            wrapper.setFieldConfiguration(formData);
            return wrapper;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public LogAppenderDto editLogAppenderForm(LogAppenderDto wrapper) throws KaaAdminServiceException {
        LogAppenderDto logAppender = new LogAppenderDto(wrapper);
        try {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(wrapper.getFieldConfiguration());
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            byte[] rawConfiguration = converter.encode(record);
            logAppender.setRawConfiguration(rawConfiguration);
            LogAppenderDto saved = editLogAppender(logAppender);
            LogAppenderDto savedWrapper = new LogAppenderDto(saved);
            return savedWrapper;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteLogAppender(String appenderId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            LogAppenderDto logAppender = toDto(clientProvider.getClient().getLogAppender(appenderId));
            Utils.checkNotNull(logAppender);
            checkApplicationId(logAppender.getApplicationId());
            clientProvider.getClient().deleteLogAppender(appenderId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public List<LogAppenderInfoDto> getLogAppenderInfos()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return appenders;
    }

    @Override
    public void addTopicToEndpointGroup(String endpointGroupId, String topicId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            TopicDto topic = toDto(clientProvider.getClient().getTopic(topicId));
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            clientProvider.getClient().addTopicsToEndpointGroup(endpointGroupId, topicId);
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
            TopicDto topic = toDto(clientProvider.getClient().getTopic(topicId));
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            clientProvider.getClient().removeTopicsFromEndpointGroup(endpointGroupId, topicId);
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
    
    @Override
    public void sendNotification(NotificationDto notification,
            RecordField notificationData) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(notificationData);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(record.getSchema());
            byte[] body = converter.encodeToJsonBytes(record);
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = toDto(clientProvider.getClient().getTopic(notification.getTopicId()));
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            clientProvider.getClient().editNotification(toDataStruct(notification));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    @Override
    public void sendNotification(NotificationDto notification, byte[] body)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            notification.setBody(body);
            checkApplicationId(notification.getApplicationId());
            TopicDto topic = toDto(clientProvider.getClient().getTopic(notification.getTopicId()));
            Utils.checkNotNull(topic);
            checkApplicationId(topic.getApplicationId());
            clientProvider.getClient().editNotification(toDataStruct(notification));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassFamilyDto> getEventClassFamilies()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            return toDtoList(clientProvider.getClient().getEventClassFamiliesByTenantId(getTenantId()));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            EventClassFamilyDto eventClassFamily = toDto(clientProvider.getClient().getEventClassFamily(eventClassFamilyId));
            Utils.checkNotNull(eventClassFamily);
            checkTenantId(eventClassFamily.getTenantId());
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
                EventClassFamilyDto storedEventClassFamily = toDto(clientProvider.getClient().getEventClassFamily(eventClassFamily.getId()));
                Utils.checkNotNull(storedEventClassFamily);
                checkTenantId(storedEventClassFamily.getTenantId());
            }
            else {
                String username = this.getCurrentUser().getUsername();
                eventClassFamily.setCreatedUsername(username);
            }
            eventClassFamily.setTenantId(getTenantId());
            return toDto(clientProvider.getClient().editEventClassFamily(toDataStruct(eventClassFamily)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addEventClassFamilySchema(String eventClassFamilyId,
            String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            byte[] data = getFileContent(fileItemName);
            String schema = new String(data);
            validateSchema(schema);

            EventClassFamilyDto storedEventClassFamily = toDto(clientProvider.getClient().getEventClassFamily(eventClassFamilyId));
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = this.getCurrentUser().getUsername();
            clientProvider.getClient().addEventClassFamilySchema(eventClassFamilyId, schema, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addEventClassFamilySchema(String eventClassFamilyId, byte[] data)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            String schema = new String(data);
            validateSchema(schema);

            EventClassFamilyDto storedEventClassFamily = toDto(clientProvider.getClient().getEventClassFamily(eventClassFamilyId));
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            String username = this.getCurrentUser().getUsername();
            clientProvider.getClient().addEventClassFamilySchema(eventClassFamilyId, schema, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(
            String eventClassFamilyId, int version, EventClassType type) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EventClassFamilyDto storedEventClassFamily = toDto(clientProvider.getClient().getEventClassFamily(eventClassFamilyId));
            Utils.checkNotNull(storedEventClassFamily);
            checkTenantId(storedEventClassFamily.getTenantId());

            return toDtoList(clientProvider.getClient().getEventClassesByFamilyIdVersionAndType(eventClassFamilyId, version, toGenericDataStruct(type)));
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
            return toDtoList(clientProvider.getClient().getApplicationEventFamilyMapsByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(
            String applicationEventFamilyMapId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationEventFamilyMapDto aefMap = toDto(clientProvider.getClient().getApplicationEventFamilyMap(applicationEventFamilyMapId));
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
            }
            else {
                ApplicationEventFamilyMapDto storedApplicationEventFamilyMap = toDto(clientProvider.getClient().getApplicationEventFamilyMap(applicationEventFamilyMap.getId()));
                Utils.checkNotNull(storedApplicationEventFamilyMap);
                checkApplicationId(storedApplicationEventFamilyMap.getApplicationId());
            }
            return toDto(clientProvider.getClient().editApplicationEventFamilyMap(toDataStruct(applicationEventFamilyMap)));
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
            return toGenericDtoList(clientProvider.getClient().getVacantEventClassFamiliesByApplicationId(applicationId));
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
            return toGenericDtoList(clientProvider.getClient().getEventClassFamiliesByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            RecordKey sdkKey = new RecordKey(applicationId, logSchemaVersion, file);
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

    private void setSchema (AbstractSchemaDto schemaDto, String fileItemName) throws KaaAdminServiceException {
        byte[] data = getFileContent(fileItemName);
        setSchema(schemaDto, data);
    }

    private void setSchema (AbstractSchemaDto schemaDto, byte[] data) throws KaaAdminServiceException {
        String schema = new String(data);
        validateSchema(schema);
        schemaDto.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
    }

    private void validateSchema(String schema) throws KaaAdminServiceException {
        Schema.Parser parser = new Schema.Parser();
        try {
            parser.parse(schema);
        }
        catch (SchemaParseException spe) {
            throw new KaaAdminServiceException(spe.getMessage(), ServiceErrorCode.INVALID_SCHEMA);
        }
    }

    private byte[] getFileContent (String fileItemName) throws KaaAdminServiceException {
        if (!isEmpty(fileItemName)) {
            try {
                byte[] data = cacheService.uploadedFile(fileItemName, null);
                if (data == null) {
                    throw new KaaAdminServiceException("Unable to get file content!", ServiceErrorCode.GENERAL_ERROR);
                }
                return data;
            }
            finally {
                cacheService.removeUploadedFile(fileItemName);
            }
        }
        else {
            throw new KaaAdminServiceException("Unable to get file content, file item name is empty!", ServiceErrorCode.GENERAL_ERROR);
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
            throw new KaaAdminServiceException(ServiceErrorCode.PERMISSION_DENIED);
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
                throw new KaaAdminServiceException(ServiceErrorCode.INVALID_ARGUMENTS);
            }
            ApplicationDto application = toDto(clientProvider.getClient().getApplication(applicationId));
            Utils.checkNotNull(application);
            checkTenantId(application.getTenantId());
            return application;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private EndpointGroupDto checkEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        try {
            if (isEmpty(endpointGroupId)) {
                throw new KaaAdminServiceException(ServiceErrorCode.INVALID_ARGUMENTS);
            }
            EndpointGroupDto endpointGroup = toDto(clientProvider.getClient().getEndpointGroup(endpointGroupId));
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
            return (AuthUserDto)authentication.getPrincipal();
        }
        else {
            throw new KaaAdminServiceException(ServiceErrorCode.NOT_AUTHORIZED);
        }
    }

}
