/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.control.service;

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
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;

import java.util.List;

/**
 * The Interface ControlService.
 */
public interface ControlService {

    /**
     * Gets the tenants.
     *
     * @return the tenants
     * @throws ControlServiceException the control service exception
     */
    List<TenantDto> getTenants() throws ControlServiceException;

    /**
     * Gets the tenant.
     *
     * @param tenantId the tenant id
     * @return the tenant
     * @throws ControlServiceException the control service exception
     */
    TenantDto getTenant(String tenantId) throws ControlServiceException;

    /**
     * Edits the tenant.
     *
     * @param tenant the tenant
     * @return the tenant dto
     * @throws ControlServiceException the control service exception
     */
    TenantDto editTenant(TenantDto tenant) throws ControlServiceException;

    /**
     * Delete tenant.
     *
     * @param tenantId the tenant id
     * @throws ControlServiceException the control service exception
     */
    void deleteTenant(String tenantId) throws ControlServiceException;

    /**
     * Gets the users.
     *
     * @return the users
     * @throws ControlServiceException the control service exception
     */
    List<UserDto> getUsers() throws ControlServiceException;

    /**
     * Gets the tenant users.
     *
     * @param tenantId the tenant id
     * @return the tenant users
     * @throws ControlServiceException the control service exception
     */
    List<UserDto> getTenantUsers(String tenantId) throws ControlServiceException;

    /**
     * Gets the user.
     *
     * @param userId the user id
     * @return the user
     * @throws ControlServiceException the control service exception
     */
    UserDto getUser(String userId) throws ControlServiceException;

    /**
     * Gets the user by external uid.
     *
     * @param uid the uid
     * @return the user by external uid
     * @throws ControlServiceException the control service exception
     */
    UserDto getUserByExternalUid(String uid) throws ControlServiceException;

    /**
     * Edits the user.
     *
     * @param user the user
     * @return the user dto
     * @throws ControlServiceException the control service exception
     */
    UserDto editUser(UserDto user) throws ControlServiceException;

    /**
     * Delete user.
     *
     * @param userId the user id
     * @throws ControlServiceException the control service exception
     */
    void deleteUser(String userId) throws ControlServiceException;

    /**
     * Gets the tenant admins.
     *
     * @return the tenant admins
     * @throws ControlServiceException the control service exception
     */
    List<TenantAdminDto> getTenantAdmins() throws ControlServiceException;

    /**
     * Gets the tenant admin.
     *
     * @param tenantId the tenant id
     * @return the tenant admin
     * @throws ControlServiceException the control service exception
     */
    TenantAdminDto getTenantAdmin(String tenantId) throws ControlServiceException;

    /**
     * Edits the tenant admin.
     *
     * @param tenantAdmin the tenant admin
     * @return the tenant admin dto
     * @throws ControlServiceException the control service exception
     */
    TenantAdminDto editTenantAdmin(TenantAdminDto tenantAdmin) throws ControlServiceException;

    /**
     * Delete tenant admin.
     *
     * @param tenantId the tenant id
     * @throws ControlServiceException the control service exception
     */
    void deleteTenantAdmin(String tenantId) throws ControlServiceException;

    /**
     * Gets the application.
     *
     * @param applicationId the application id
     * @return the application
     * @throws ControlServiceException the control service exception
     */
    ApplicationDto getApplication(String applicationId) throws ControlServiceException;

    /**
     * Gets the application by application token.
     *
     * @param applicationToken the application token
     * @return the application by application token
     * @throws ControlServiceException the control service exception
     */
    ApplicationDto getApplicationByApplicationToken(String applicationToken) throws ControlServiceException;

    /**
     * Gets the applications by tenant id.
     *
     * @param tenantId the tenant id
     * @return the applications by tenant id
     * @throws ControlServiceException the control service exception
     */
    List<ApplicationDto> getApplicationsByTenantId(String tenantId) throws ControlServiceException;

    /**
     * Edits the application.
     *
     * @param application the application
     * @return the application dto
     * @throws ControlServiceException the control service exception
     */
    ApplicationDto editApplication(ApplicationDto application) throws ControlServiceException;

    /**
     * Delete application.
     *
     * @param applicationId the application id
     * @throws ControlServiceException the control service exception
     */
    void deleteApplication(String applicationId) throws ControlServiceException;

    /**
     * Gets the configuration schemas by application id.
     *
     * @param applicationId the application id
     * @return the configuration schemas by application id
     * @throws ControlServiceException the control service exception
     */
    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the configuration schema.
     *
     * @param configurationSchemaId the configuration schema id
     * @return the configuration schema
     * @throws ControlServiceException the control service exception
     */
    ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws ControlServiceException;

    /**
     * Edits the configuration schema.
     *
     * @param configurationSchema the configuration schema
     * @return the configuration schema dto
     * @throws ControlServiceException the control service exception
     */
    ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws ControlServiceException;

    /**
     * Gets the profile schemas by application id.
     *
     * @param applicationId the application id
     * @return the profile schemas by application id
     * @throws ControlServiceException the control service exception
     */
    List<ProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the profile schema.
     *
     * @param profileSchemaId the profile schema id
     * @return the profile schema
     * @throws ControlServiceException the control service exception
     */
    ProfileSchemaDto getProfileSchema(String profileSchemaId) throws ControlServiceException;

    /**
     * Edits the profile schema.
     *
     * @param profileSchema the profile schema
     * @return the profile schema dto
     * @throws ControlServiceException the control service exception
     */
    ProfileSchemaDto editProfileSchema(ProfileSchemaDto profileSchema) throws ControlServiceException;

    /**
     * Gets the server profile schemas by application id.
     *
     * @param applicationId the application id
     * @return the server profile schemas by application id
     * @throws ControlServiceException
     */
    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the server profile schema.
     *
     * @param serverProfileSchemaId the server profile schema id
     * @return the server profile schema dto
     * @throws ControlServiceException
     */
    ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws ControlServiceException;

    /**
     * Edits the server profile schema.
     *
     * @param serverProfileSchema the server profile schema
     * @return the server profile schema dto
     * @throws ControlServiceException
     */
    ServerProfileSchemaDto editServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws ControlServiceException;

    ServerProfileSchemaDto findLatestServerProfileSchema(String applicationId) throws ControlServiceException;

    /**
     * Gets the endpoint groups by application id.
     *
     * @param applicationId the application id
     * @return the endpoint groups by application id
     * @throws ControlServiceException the control service exception
     */
    List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the endpoint group.
     *
     * @param endpointGroupId the endpoint group id
     * @return the endpoint group
     * @throws ControlServiceException the control service exception
     */
    EndpointGroupDto getEndpointGroup(String endpointGroupId) throws ControlServiceException;

    /**
     * Edits the endpoint group.
     *
     * @param endpointGroup the endpoint group
     * @return the endpoint group dto
     * @throws ControlServiceException the control service exception
     */
    EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws ControlServiceException;

    /**
     * Delete endpoint group.
     *
     * @param endpointGroupId the endpoint group id
     * @throws ControlServiceException the control service exception
     */
    void deleteEndpointGroup(String endpointGroupId) throws ControlServiceException;

    /**
     * Removes the topics from endpoint group.
     *
     * @param endpointGroupId the endpoint group id
     * @param topicId the topic id
     * @return the endpoint group dto
     * @throws ControlServiceException the control service exception
     */
    EndpointGroupDto removeTopicsFromEndpointGroup(String endpointGroupId, String topicId) throws ControlServiceException;

    /**
     * Adds the topics to endpoint group.
     *
     * @param endpointGroupId the endpoint group id
     * @param topicId the topic id
     * @return the endpoint group dto
     * @throws ControlServiceException the control service exception
     */
    EndpointGroupDto addTopicsToEndpointGroup(String endpointGroupId, String topicId) throws ControlServiceException;

    /**
     * Gets the profile filter.
     *
     * @param profileFilterId the profile filter id
     * @return the profile filter
     * @throws ControlServiceException the control service exception
     */
    ProfileFilterDto getProfileFilter(String profileFilterId) throws ControlServiceException;

    /**
     * Gets the profile filter records by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @param includeDeprecated the include deprecated
     * @return the profile filter records by endpoint group id
     * @throws ControlServiceException the control service exception
     */
    List<StructureRecordDto<ProfileFilterDto>> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated)
            throws ControlServiceException;

    /**
     * Gets the profile filter record.
     *
     * @param schemaId the schema id
     * @param endpointGroupId the endpoint group id
     * @return the profile filter record
     * @throws ControlServiceException the control service exception
     */
    StructureRecordDto<ProfileFilterDto> getProfileFilterRecord(String schemaId, String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the vacant profile schemas by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the vacant profile schemas by endpoint group id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Edits the profile filter.
     *
     * @param profileFilter the profile filter
     * @return the profile filter dto
     * @throws ControlServiceException the control service exception
     */
    ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws ControlServiceException;

    /**
     * Gets the configuration records by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @param includeDeprecated the include deprecated
     * @return the configuration records by endpoint group id
     * @throws ControlServiceException the control service exception
     */
    List<StructureRecordDto<ConfigurationDto>> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated)
            throws ControlServiceException;

    /**
     * Gets the configuration record.
     *
     * @param schemaId the schema id
     * @param endpointGroupId the endpoint group id
     * @return the configuration record
     * @throws ControlServiceException the control service exception
     */
    StructureRecordDto<ConfigurationDto> getConfigurationRecord(String schemaId, String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the vacant configuration schemas by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the vacant configuration schemas by endpoint group id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the configuration.
     *
     * @param configurationId the configuration id
     * @return the configuration
     * @throws ControlServiceException the control service exception
     */
    ConfigurationDto getConfiguration(String configurationId) throws ControlServiceException;

    /**
     * Edits the configuration.
     *
     * @param configuration the configuration
     * @return the configuration dto
     * @throws ControlServiceException the control service exception
     */
    ConfigurationDto editConfiguration(ConfigurationDto configuration) throws ControlServiceException;

    /**
     * Edits the user configuration.
     *
     * @param configuration the configuration
     * @throws ControlServiceException the control service exception
     */
    void editUserConfiguration(EndpointUserConfigurationDto configuration) throws ControlServiceException;

    /**
     * Activate configuration.
     *
     * @param configurationId the configuration id
     * @param activatedUsername the activated username
     * @return the configuration dto
     * @throws ControlServiceException the control service exception
     */
    ConfigurationDto activateConfiguration(String configurationId, String activatedUsername) throws ControlServiceException;

    /**
     * Deactivate configuration.
     *
     * @param configurationId the configuration id
     * @param deactivatedUsername the deactivated username
     * @return the configuration dto
     * @throws ControlServiceException the control service exception
     */
    ConfigurationDto deactivateConfiguration(String configurationId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Delete configuration record.
     *
     * @param schemaId the schema id
     * @param endpointGroupId the endpoint group id
     * @param deactivatedUsername the deactivated username
     * @throws ControlServiceException the control service exception
     */
    void deleteConfigurationRecord(String schemaId, String endpointGroupId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Activate profile filter.
     *
     * @param profileFilterId the profile filter id
     * @param activatedUsername the activated username
     * @return the profile filter dto
     * @throws ControlServiceException the control service exception
     */
    ProfileFilterDto activateProfileFilter(String profileFilterId, String activatedUsername) throws ControlServiceException;

    /**
     * Deactivate profile filter.
     *
     * @param profileFilterId the profile filter id
     * @param deactivatedUsername the deactivated username
     * @return the profile filter dto
     * @throws ControlServiceException the control service exception
     */
    ProfileFilterDto deactivateProfileFilter(String profileFilterId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Delete profile filter record.
     *
     * @param schemaId the schema id
     * @param endpointGroupId the endpoint group id
     * @param deactivatedUsername the deactivated username
     * @throws ControlServiceException the control service exception
     */
    void deleteProfileFilterRecord(String schemaId, String endpointGroupId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Generate sdk.
     *
     * @param sdkProperties the sdk properties
     * @return the file data
     * @throws ControlServiceException the control service exception
     */
    FileData generateSdk(SdkProfileDto sdkProperties, SdkPlatform platform) throws ControlServiceException;

    /**
     * Generate record structure library.
     *
     * @param applicationId the application id
     * @param logSchemaVersion the log schema version
     * @return the file data
     * @throws ControlServiceException the control service exception
     */
    FileData generateRecordStructureLibrary(String applicationId, int logSchemaVersion) throws ControlServiceException;

    /**
     * Edits the notification schema.
     *
     * @param notificationSchema the notification schema
     * @return the notification schema dto
     * @throws ControlServiceException the control service exception
     */
    NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema) throws ControlServiceException;

    /**
     * Gets the notification schema.
     *
     * @param notificationSchemaId the notification schema id
     * @return the notification schema
     * @throws ControlServiceException the control service exception
     */
    NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws ControlServiceException;

    /**
     * Gets the notification schemas by app id.
     *
     * @param applicationId the application id
     * @return the notification schemas by app id
     * @throws ControlServiceException the control service exception
     */
    List<NotificationSchemaDto> getNotificationSchemasByAppId(String applicationId) throws ControlServiceException;

    /**
     * Gets the user notification schemas by app id.
     *
     * @param applicationId the application id
     * @return the user notification schemas by app id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getUserNotificationSchemasByAppId(String applicationId) throws ControlServiceException;

    /**
     * Find notification schemas by app id and type.
     *
     * @param applicationId the application id
     * @param type the type
     * @return the list
     * @throws ControlServiceException the control service exception
     */
    List<NotificationSchemaDto> findNotificationSchemasByAppIdAndType(String applicationId, NotificationTypeDto type) throws ControlServiceException;

    /**
     * Edits the log schema.
     *
     * @param logSchemaDto the log schema dto
     * @return the log schema dto
     * @throws ControlServiceException the control service exception
     */
    LogSchemaDto editLogSchema(LogSchemaDto logSchemaDto) throws ControlServiceException;

    /**
     * Gets the log schemas by application id.
     *
     * @param applicationId the application id
     * @return the log schemas by application id
     * @throws ControlServiceException the control service exception
     */
    List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the log schema.
     *
     * @param logSchemaId the log schema id
     * @return the log schema
     * @throws ControlServiceException the control service exception
     */
    LogSchemaDto getLogSchema(String logSchemaId) throws ControlServiceException;

    /**
     * Gets the log schema by application id and version.
     *
     * @param applicationId the application id
     * @param version the version
     * @return the log schema by application id and version
     * @throws ControlServiceException the control service exception
     */
    LogSchemaDto getLogSchemaByApplicationIdAndVersion(String applicationId, int version) throws ControlServiceException;

    /**
     * Edits the notification.
     *
     * @param notification the notification
     * @return the notification dto
     * @throws ControlServiceException the control service exception
     */
    NotificationDto editNotification(NotificationDto notification) throws ControlServiceException;

    /**
     * Gets the notification.
     *
     * @param notificationId the notification id
     * @return the notification
     * @throws ControlServiceException the control service exception
     */
    NotificationDto getNotification(String notificationId) throws ControlServiceException;

    /**
     * Gets the notifications by topic id.
     *
     * @param topicId the topic id
     * @return the notifications by topic id
     * @throws ControlServiceException the control service exception
     */
    List<NotificationDto> getNotificationsByTopicId(String topicId) throws ControlServiceException;

    /**
     * Edits the topic.
     *
     * @param topic the topic
     * @return the topic dto
     * @throws ControlServiceException the control service exception
     */
    TopicDto editTopic(TopicDto topic) throws ControlServiceException;

    /**
     * Gets the topic.
     *
     * @param topicId the topic id
     * @return the topic
     * @throws ControlServiceException the control service exception
     */
    TopicDto getTopic(String topicId) throws ControlServiceException;

    /**
     * Gets the topic by app id.
     *
     * @param appId the app id
     * @return the topic by app id
     * @throws ControlServiceException the control service exception
     */
    List<TopicDto> getTopicByAppId(String appId) throws ControlServiceException;

    /**
     * Gets the topic by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the topic by endpoint group id
     * @throws ControlServiceException the control service exception
     */
    List<TopicDto> getTopicByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the vacant topic by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the vacant topic by endpoint group id
     * @throws ControlServiceException the control service exception
     */
    List<TopicDto> getVacantTopicByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Delete topic by id.
     *
     * @param topicId the topic id
     * @throws ControlServiceException the control service exception
     */
    void deleteTopicById(String topicId) throws ControlServiceException;

    /**
     * Gets the unicast notification.
     *
     * @param notificationId the notification id
     * @return the unicast notification
     * @throws ControlServiceException the control service exception
     */
    EndpointNotificationDto getUnicastNotification(String notificationId) throws ControlServiceException;

    /**
     * Edits the unicast notification.
     *
     * @param notification the notification
     * @return the endpoint notification dto
     * @throws ControlServiceException the control service exception
     */
    EndpointNotificationDto editUnicastNotification(EndpointNotificationDto notification) throws ControlServiceException;

    /**
     * Gets the unicast notifications by key hash.
     *
     * @param keyhash the keyhash
     * @return the unicast notifications by key hash
     * @throws ControlServiceException the control service exception
     */
    List<EndpointNotificationDto> getUnicastNotificationsByKeyHash(byte[] keyhash) throws ControlServiceException;

    /**
     * Gets the configuration schema versions by application id.
     *
     * @param applicationId the application id
     * @return the configuration schema versions by application id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getConfigurationSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the profile schema versions by application id.
     *
     * @param applicationId the application id
     * @return the profile schema versions by application id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getProfileSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the notification schema versions by application id.
     *
     * @param applicationId the application id
     * @return the notification schema versions by application id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getNotificationSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the log schema versions by application id.
     *
     * @param applicationId the application id
     * @return the log schema versions by application id
     * @throws ControlServiceException the control service exception
     */
    List<SchemaDto> getLogSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Edits the event class family.
     *
     * @param eventClassFamily the event class family
     * @return the event class family dto
     * @throws ControlServiceException the control service exception
     */
    EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws ControlServiceException;

    /**
     * Gets the event class families by tenant id.
     *
     * @param tenantId the tenant id
     * @return the event class families by tenant id
     * @throws ControlServiceException the control service exception
     */
    List<EventClassFamilyDto> getEventClassFamiliesByTenantId(String tenantId) throws ControlServiceException;

    /**
     * Gets the event class family.
     *
     * @param eventClassFamilyId the event class family id
     * @return the event class family
     * @throws ControlServiceException the control service exception
     */
    EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws ControlServiceException;

    /**
     * Adds the event class family schema.
     *
     * @param eventClassFamilyId the event class family id
     * @param eventClassFamilySchema the event class family schema
     * @param createdUsername the created username
     * @throws ControlServiceException the control service exception
     */
    void addEventClassFamilySchema(String eventClassFamilyId, String eventClassFamilySchema, String createdUsername) throws ControlServiceException;

    /**
     * Gets the event classes by family id version and type.
     *
     * @param ecfId the ecf id
     * @param version the version
     * @param type the type
     * @return the event classes by family id version and type
     * @throws ControlServiceException the control service exception
     */
    List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String ecfId, int version, EventClassType type) throws ControlServiceException;

    /**
     * Edits the application event family map.
     *
     * @param applicationEventFamilyMap the application event family map
     * @return the application event family map dto
     * @throws ControlServiceException the control service exception
     */
    ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap) throws ControlServiceException;

    /**
     * Gets the application event family map.
     *
     * @param applicationEventFamilyMapId the application event family map id
     * @return the application event family map
     * @throws ControlServiceException the control service exception
     */
    ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws ControlServiceException;

    /**
     * Gets the application event family maps by application id.
     *
     * @param applicationId the application id
     * @return the application event family maps by application id
     * @throws ControlServiceException the control service exception
     */
    List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the vacant event class families by application id.
     *
     * @param applicationId the application id
     * @return the vacant event class families by application id
     * @throws ControlServiceException the control service exception
     */
    List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the event class families by application id.
     *
     * @param applicationId the application id
     * @return the event class families by application id
     * @throws ControlServiceException the control service exception
     */
    List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the endpoint users.
     *
     * @return the endpoint users
     * @throws ControlServiceException the control service exception
     */
    List<EndpointUserDto> getEndpointUsers() throws ControlServiceException;

    /**
     * Gets the endpoint user.
     *
     * @param endpointUserId the endpoint user id
     * @return the endpoint user
     * @throws ControlServiceException the control service exception
     */
    EndpointUserDto getEndpointUser(String endpointUserId) throws ControlServiceException;

    /**
     * Edits the endpoint user.
     *
     * @param endpointUser the endpoint user
     * @return the endpoint user dto
     * @throws ControlServiceException the control service exception
     */
    EndpointUserDto editEndpointUser(EndpointUserDto endpointUser) throws ControlServiceException;

    /**
     * Delete endpoint user.
     *
     * @param endpointUserId the endpoint user id
     * @throws ControlServiceException the control service exception
     */
    void deleteEndpointUser(String endpointUserId) throws ControlServiceException;

    /**
     * Generate endpoint user access token.
     *
     * @param externalUid the external uid
     * @param tenantId the tenant id
     * @return the string
     * @throws ControlServiceException the control service exception
     */
    String generateEndpointUserAccessToken(String externalUid, String tenantId) throws ControlServiceException;

    /**
     * Gets the log appenders by application id.
     *
     * @param applicationId the application id
     * @return the log appenders by application id
     * @throws ControlServiceException the control service exception
     */
    List<LogAppenderDto> getLogAppendersByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the log appender.
     *
     * @param logAppenderId the log appender id
     * @return the log appender
     * @throws ControlServiceException the control service exception
     */
    LogAppenderDto getLogAppender(String logAppenderId) throws ControlServiceException;

    /**
     * Edits the log appender.
     *
     * @param logAppender the log appender
     * @return the log appender dto
     * @throws ControlServiceException the control service exception
     */
    LogAppenderDto editLogAppender(LogAppenderDto logAppender) throws ControlServiceException;

    /**
     * Delete log appender.
     *
     * @param logAppenderId the log appender id
     * @throws ControlServiceException the control service exception
     */
    void deleteLogAppender(String logAppenderId) throws ControlServiceException;

    /**
     * Gets the user verifiers by application id.
     *
     * @param applicationId the application id
     * @return the user verifiers by application id
     * @throws ControlServiceException the control service exception
     */
    List<UserVerifierDto> getUserVerifiersByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the user verifier.
     *
     * @param userVerifierId the user verifier id
     * @return the user verifier
     * @throws ControlServiceException the control service exception
     */
    UserVerifierDto getUserVerifier(String userVerifierId) throws ControlServiceException;

    /**
     * Edits the user verifier.
     *
     * @param userVerifier the user verifier
     * @return the user verifier dto
     * @throws ControlServiceException the control service exception
     */
    UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws ControlServiceException;

    /**
     * Delete user verifier.
     *
     * @param userVerifierId the user verifier id
     * @throws ControlServiceException the control service exception
     */
    void deleteUserVerifier(String userVerifierId) throws ControlServiceException;

    /**
     * Gets the record structure schema.
     *
     * @param applicationId the application id
     * @param logSchemaVersion the log schema version
     * @return the record structure schema
     * @throws ControlServiceException the control service exception
     */
    FileData getRecordStructureSchema(String applicationId, int logSchemaVersion) throws ControlServiceException;

    /**
     * Gets the record structure data.
     *
     * @param key the key
     * @return the record structure data
     * @throws ControlServiceException the control service exception
     */
    FileData getRecordStructureData(RecordKey key) throws ControlServiceException;

    /**
     * Gets page of EndpointProfilesBodyDto objects by endpoint group id.
     *
     * @param pageLinkDto the page object include endpoint group id, limit and
     *            offset.
     * @return the EndpointProfilesBodyDto object.
     * @throws ControlServiceException the control service exception
     */
    EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLinkDto) throws ControlServiceException;

    /**
     * Gets endpoint profile by endpoint key hash.
     *
     * @param endpointProfileKeyHash the endpoint key hash in string
     *            representation.
     * @return the EndpointProfileDto object.
     * @throws ControlServiceException the control service exception
     */
    EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws ControlServiceException;

    /**
     ** Gets endpoint profile body by endpoint key hash.
     *
     * @param endpointProfileKeyHash the endpoint key hash in string
     *            representation.
     * @return the EndpointProfileBodyDto object.
     * @throws ControlServiceException the control service exception
     */
    EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash) throws ControlServiceException;

    /**
     * Gets page of EndpointProfilesBodyDto objects by endpoint group id.
     *
     * @param pageLinkDto the page object include endpoint group id, limit and
     *            offset.
     * @return the EndpointProfilesPageDto object.
     * @throws ControlServiceException the control service exception
     */
    EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(PageLinkDto pageLinkDto) throws ControlServiceException;

    CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws ControlServiceException;

    void deleteCTLSchemaById(String schemaId) throws ControlServiceException;

    void deleteCTLSchemaByFqnAndVersionAndTenantId(String fqn, int version, String tenantId) throws ControlServiceException;

    CTLSchemaDto getCTLSchemaById(String schemaId) throws ControlServiceException;

    CTLSchemaDto getCTLSchemaByFqnVersionAndTenantId(String fqn, int version, String tenantId) throws ControlServiceException;

    List<CTLSchemaMetaInfoDto> getAvailableCTLSchemasMetaInfoByTenantId(String tenantId) throws ControlServiceException;

    List<CTLSchemaMetaInfoDto> getCTLSchemasMetaInfoByTenantId(String tenantId) throws ControlServiceException;

    List<CTLSchemaMetaInfoDto> getCTLSchemasMetaInfoByApplicationId(String applicationId) throws ControlServiceException;

    List<CTLSchemaMetaInfoDto> getSystemCTLSchemasMetaInfo() throws ControlServiceException;

    List<CTLSchemaDto> getCTLSchemaDependents(String schemaId) throws ControlServiceException;

    List<CTLSchemaDto> getCTLSchemaDependents(String fqn, int version, String tenantId) throws ControlServiceException;

    /**
     * Gets SdkProfileDto object by sdk profile id.
     *
     * @param sdkProfileId sdk profile id
     * @return the SdkProfileDto
     * @throws ControlServiceException
     */
    SdkProfileDto getSdkProfile(String sdkProfileId) throws ControlServiceException;

    /**
     *
     * @param applicationId
     * @return
     * @throws ControlServiceException
     */
    List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId)  throws ControlServiceException ;

    /**
     *
     * @param sdkProfileId
     * @throws ControlServiceException
     */
    void deleteSdkProfile(String sdkProfileId)  throws ControlServiceException ;

    /**
     *
     * @param token
     * @return
     * @throws ControlServiceException
     */
    boolean isSdkProfileUsed(String token)  throws ControlServiceException ;

    /**
     *
     * @param sdkProfile
     * @return saved SdkProfileDto object.
     * @throws ControlServiceException
     */
    SdkProfileDto saveSdkProfile(SdkProfileDto sdkProfile)  throws ControlServiceException ;

    /**
     * Gets endpoint profile view for web ui.
     *
     * @param endpointProfileKeyHash the endpoint key hash in string representation.
     * @return the EndpointProfileViewDto object
     * @throws ControlServiceException the control service exception.
     */
    EndpointProfileViewDto getEndpointProfileViewDtoByEndpointKeyHash(String endpointProfileKeyHash) throws ControlServiceException;
}
