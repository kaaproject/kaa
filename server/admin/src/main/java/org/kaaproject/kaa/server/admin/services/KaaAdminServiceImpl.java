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

import java.util.ArrayList;
import java.util.List;

import net.iharder.Base64;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
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
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.entity.AuthUserDto;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.messaging.MessagingService;
import org.kaaproject.kaa.server.admin.services.thrift.ControlThriftClientProvider;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.dto.SchemaVersions;
import org.kaaproject.kaa.server.admin.shared.dto.SdkKey;
import org.kaaproject.kaa.server.admin.shared.dto.SdkPlatform;
import org.kaaproject.kaa.server.admin.shared.dto.TenantUserDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("kaaAdminService")
public class KaaAdminServiceImpl implements KaaAdminService {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(KaaAdminServiceImpl.class);

    @Autowired
    private ControlThriftClientProvider clientProvider;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private CacheService cacheService;

    private PasswordEncoder passwordEncoder;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<TenantUserDto> getTenants() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            List<TenantAdminDto> tenantAdmins = toDtoList(clientProvider.getClient().getTenantAdmins());
            List<TenantUserDto> tenantUsers = new ArrayList<TenantUserDto>(tenantAdmins.size());
            for (TenantAdminDto tenantAdmin : tenantAdmins) {
                TenantUserDto tenantUser = toTenantUser(tenantAdmin);
                tenantUsers.add(tenantUser);
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
            TenantAdminDto tenantAdmin = toDto(clientProvider.getClient().getTenantAdmin(user.getTenantId()));
            userFacade.deleteUser(Long.valueOf(tenantAdmin.getExternalUid()));
            clientProvider.getClient().deleteTenantAdmin(user.getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<org.kaaproject.kaa.server.admin.shared.dto.UserDto> getUsers()
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            List<UserDto> users = toDtoList(clientProvider.getClient().getTenantUsers(getTenantId()));
            List<org.kaaproject.kaa.server.admin.shared.dto.UserDto> tenantUsers = new ArrayList<>(users.size());
            for (UserDto user : users) {
                org.kaaproject.kaa.server.admin.shared.dto.UserDto tenantUser = toUser(user);
                tenantUsers.add(tenantUser);
            }
            return tenantUsers;

        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.server.admin.shared.dto.UserDto getUser(String userId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            UserDto user = toDto(clientProvider.getClient().getUser(userId));
            checkTenantId(user.getTenantId());
            return toUser(user);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.server.admin.shared.dto.UserDto getUserProfile()
            throws KaaAdminServiceException {
        try {
            User user = userFacade.findById(Long.valueOf(getCurrentUser().getExternalUid()));
            org.kaaproject.kaa.server.admin.shared.dto.UserDto result =
                    new org.kaaproject.kaa.server.admin.shared.dto.UserDto(user.getId().toString(),
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
    public org.kaaproject.kaa.server.admin.shared.dto.UserDto editUserProfile(org.kaaproject.kaa.server.admin.shared.dto.UserDto userDto)
            throws KaaAdminServiceException {
        try {
            userDto.setExternalUid(getCurrentUser().getExternalUid());
            Long userId = saveUser(userDto);
            User user = userFacade.findById(userId);
            org.kaaproject.kaa.server.admin.shared.dto.UserDto result =
                    new org.kaaproject.kaa.server.admin.shared.dto.UserDto(user.getId().toString(),
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
    public org.kaaproject.kaa.server.admin.shared.dto.UserDto editUser(
            org.kaaproject.kaa.server.admin.shared.dto.UserDto user)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(user.getId())) {
                UserDto storedUser = toDto(clientProvider.getClient().getUser(user.getId()));
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
            ApplicationDto application = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(application.getTenantId());
            return application;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto editApplication(ApplicationDto application) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(application.getId())) {
                ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(application.getId()));
                checkTenantId(storedApplication.getTenantId());
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
            ApplicationDto application = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(application.getTenantId());
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
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());

            SchemaVersions schemaVersions = new SchemaVersions();

            List<SchemaDto> configurationSchemaVersions = toDtoList(clientProvider.getClient().getConfigurationSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setConfigurationSchemaVersions(configurationSchemaVersions);

            List<SchemaDto> profileSchemaVersions = toDtoList(clientProvider.getClient().getProfileSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setProfileSchemaVersions(profileSchemaVersions);

            List<SchemaDto> notificationSchemaVersions = toDtoList(clientProvider.getClient().getNotificationSchemaVersionsByApplicationId(applicationId));
            schemaVersions.setNotificationSchemaVersions(notificationSchemaVersions);

            return schemaVersions;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getSdk(String applicationId, Integer configurationSchemaVersion,
            Integer profileSchemaVersion, Integer notificationSchemaVersion,
            SdkPlatform targetPlatform) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
            SdkKey sdkKey = new SdkKey(applicationId, configurationSchemaVersion, profileSchemaVersion, notificationSchemaVersion, targetPlatform);
            return Base64.encodeObject(sdkKey, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ProfileSchemaDto> getProfileSchemasByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
            return toDtoList(clientProvider.getClient().getProfileSchemasByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return toDto(clientProvider.getClient().getProfileSchema(profileSchemaId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaDto editProfileSchema(ProfileSchemaDto profileSchema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            setSchema(profileSchema, fileItemName);
            if (isEmpty(profileSchema.getId())) {
                profileSchema.setCreatedUsername(getCurrentUser().getUsername());
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
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
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
            return toDto(clientProvider.getClient().getConfigurationSchema(configurationSchemaId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationSchemaDto editConfigurationSchema(
            ConfigurationSchemaDto configurationSchema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            setSchema(configurationSchema, fileItemName);
            if (isEmpty(configurationSchema.getId())) {
                configurationSchema.setCreatedUsername(getCurrentUser().getUsername());
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
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
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
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
            return toDtoList(clientProvider.getClient().getUserNotificationSchemasByAppId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return toDto(clientProvider.getClient().getNotificationSchema(notificationSchemaId));
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
            setSchema(notificationSchema, fileItemName);
            if (isEmpty(notificationSchema.getId())) {
                notificationSchema.setCreatedUsername(getCurrentUser().getUsername());
            }
            notificationSchema.setType(NotificationTypeDto.USER);
            return toDto(clientProvider.getClient().editNotificationSchema(toDataStruct(notificationSchema)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(
            String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
            return toDtoList(clientProvider.getClient().getEndpointGroupsByApplicationId(applicationId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return toDto(clientProvider.getClient().getEndpointGroup(endpointGroupId));
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
            return toGenericDto(clientProvider.getClient().getProfileFilterRecord(schemaId, endpointGroupId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaDto> getVacantProfileSchemasByEndpointGroupId(
            String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
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
            }
            else {
                profileFilter.setModifiedUsername(username);
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
            return toGenericDto(clientProvider.getClient().getConfigurationRecord(schemaId, endpointGroupId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaDto> getVacantConfigurationSchemasByEndpointGroupId(
            String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return toDtoList(clientProvider.getClient().getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId));
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
            }
            else {
                configuration.setModifiedUsername(username);
            }
            return toDto(clientProvider.getClient().editConfiguration(toDataStruct(configuration)));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto activateConfiguration(String configurationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = this.getCurrentUser().getUsername();
            return toDto(clientProvider.getClient().activateConfiguration(configurationId, username));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ConfigurationDto deactivateConfiguration(String configurationId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = this.getCurrentUser().getUsername();
            return toDto(clientProvider.getClient().deactivateConfiguration(configurationId, username));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
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
            ApplicationDto storedApplication = toDto(clientProvider.getClient().getApplication(applicationId));
            checkTenantId(storedApplication.getTenantId());
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
            return toDtoList(clientProvider.getClient().getVacantTopicByEndpointGroupId(endpointGroupId));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TopicDto getTopic(String topicId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return toDto(clientProvider.getClient().getTopic(topicId));
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
            clientProvider.getClient().deleteTopicById(topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void addTopicToEndpointGroup(String endpointGroupId, String topicId)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
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
            clientProvider.getClient().removeTopicsFromEndpointGroup(endpointGroupId, topicId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void sendNotification(NotificationDto notification, String fileItemName)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            byte[] body = getFileContent(fileItemName);
            notification.setBody(body);
            clientProvider.getClient().editNotification(toDataStruct(notification));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private TenantUserDto toTenantUser(TenantAdminDto tenantAdmin) {
        User user = userFacade.findById(Long.valueOf(tenantAdmin.getExternalUid()));
        TenantUserDto tenantUser = new TenantUserDto(user.getId().toString(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getMail(),
                KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
        tenantUser.setId(tenantAdmin.getUserId());
        tenantUser.setTenantId(tenantAdmin.getTenant().getId());
        tenantUser.setTenantName(tenantAdmin.getTenant().getName());
        return tenantUser;
    }

    private org.kaaproject.kaa.server.admin.shared.dto.UserDto toUser(UserDto tenantUser) {
        User user = userFacade.findById(Long.valueOf(tenantUser.getExternalUid()));
        org.kaaproject.kaa.server.admin.shared.dto.UserDto result = new org.kaaproject.kaa.server.admin.shared.dto.UserDto(user.getId().toString(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getMail(),
                KaaAuthorityDto.valueOf(user.getAuthorities().iterator().next().getAuthority()));
        result.setId(tenantUser.getId());
        result.setTenantId(tenantUser.getTenantId());
        return result;
    }

    private Long saveUser(org.kaaproject.kaa.server.admin.shared.dto.UserDto user)
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
        String schema = new String(data);
        Schema.Parser parser = new Schema.Parser();
        try {
            parser.parse(schema);
        }
        catch (SchemaParseException spe) {
            throw new KaaAdminServiceException(spe.getMessage(), ServiceErrorCode.INVALID_SCHEMA);
        }
        schemaDto.setSchema(schema);
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
