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

package org.kaaproject.kaa.server.control.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.avro.Schema;
import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
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

/**
 * The Interface ControlService.
 */
public interface ControlService {

    /**
     * Gets the tenants.
     *
     * @return the tenants
     * @throws ControlServiceException
     *             the control service exception
     */
    List<TenantDto> getTenants() throws ControlServiceException;

    /**
     * Gets the tenant.
     *
     * @param tenantId
     *            the tenant id
     * @return the tenant
     * @throws ControlServiceException
     *             the control service exception
     */
    TenantDto getTenant(String tenantId) throws ControlServiceException;

    /**
     * Edits the tenant.
     *
     * @param tenant
     *            the tenant
     * @return the tenant dto
     * @throws ControlServiceException
     *             the control service exception
     */
    TenantDto editTenant(TenantDto tenant) throws ControlServiceException;

    /**
     * Delete tenant.
     *
     * @param tenantId
     *            the tenant id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteTenant(String tenantId) throws ControlServiceException;

    /**
     * Gets the users.
     *
     * @return the users
     * @throws ControlServiceException
     *             the control service exception
     */
    List<UserDto> getUsers() throws ControlServiceException;

    /**
     * Gets the tenant users.
     *
     * @param tenantId
     *            the tenant id
     * @return the tenant users
     * @throws ControlServiceException
     *             the control service exception
     */
    List<UserDto> getTenantUsers(String tenantId) throws ControlServiceException;

    /**
     * Gets the user.
     *
     * @param userId
     *            the user id
     * @return the user
     * @throws ControlServiceException
     *             the control service exception
     */
    UserDto getUser(String userId) throws ControlServiceException;

    /**
     * Gets the user by external uid.
     *
     * @param uid
     *            the uid
     * @return the user by external uid
     * @throws ControlServiceException
     *             the control service exception
     */
    UserDto getUserByExternalUid(String uid) throws ControlServiceException;

    /**
     * Edits the user.
     *
     * @param user
     *            the user
     * @return the user dto
     * @throws ControlServiceException
     *             the control service exception
     */
    UserDto editUser(UserDto user) throws ControlServiceException;

    /**
     * Delete user.
     *
     * @param userId
     *            the user id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteUser(String userId) throws ControlServiceException;

    /**
     * Gets the tenant admins by tenant id.
     *
     * @return the tenant admins
     * @throws ControlServiceException
     *             the control service exception
     */
    List<UserDto> findAllTenantAdminsByTenantId(String tenantId) throws ControlServiceException;


    /**
     * Gets the application.
     *
     * @param applicationId
     *            the application id
     * @return the application
     * @throws ControlServiceException
     *             the control service exception
     */
    ApplicationDto getApplication(String applicationId) throws ControlServiceException;

    /**
     * Gets the application by application token.
     *
     * @param applicationToken
     *            the application token
     * @return the application by application token
     * @throws ControlServiceException
     *             the control service exception
     */
    ApplicationDto getApplicationByApplicationToken(String applicationToken) throws ControlServiceException;

    /**
     * Gets the applications by tenant id.
     *
     * @param tenantId
     *            the tenant id
     * @return the applications by tenant id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<ApplicationDto> getApplicationsByTenantId(String tenantId) throws ControlServiceException;

    /**
     * Edits the application.
     *
     * @param application
     *            the application
     * @return the application dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ApplicationDto editApplication(ApplicationDto application) throws ControlServiceException;

    /**
     * Delete application.
     *
     * @param applicationId
     *            the application id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteApplication(String applicationId) throws ControlServiceException;

    /**
     * Gets the configuration schemas by application id.
     *
     * @param applicationId
     *            the application id
     * @return the configuration schemas by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the configuration schema.
     *
     * @param configurationSchemaId
     *            the configuration schema id
     * @return the configuration schema
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws ControlServiceException;

    /**
     * Edits the configuration schema.
     *
     * @param configurationSchema
     *            the configuration schema
     * @return the configuration schema dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws ControlServiceException;

    /**
     * Gets the profile schemas by application id.
     *
     * @param applicationId
     *            the application id
     * @return the profile schemas by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the profile schema.
     *
     * @param profileSchemaId
     *            the profile schema id
     * @return the profile schema
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws ControlServiceException;

    /**
     * Gets the profile schema by application id and profile schema version.
     *
     * @param applicationId
     *            the application id
     * @param version
     *            the profile schema version
     * @return the profile schema
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfileSchemaDto getProfileSchemaByApplicationIdAndVersion(String applicationId, int version) throws ControlServiceException;

    /**
     * Edits the profile schema.
     *
     * @param profileSchema
     *            the profile schema
     * @return the profile schema dto
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfileSchemaDto editProfileSchema(EndpointProfileSchemaDto profileSchema) throws ControlServiceException;

    /**
     * Gets the server profile schemas by application id.
     *
     * @param applicationId
     *            the application id
     * @return the server profile schemas by application id
     * @throws ControlServiceException 
     *          the control service exception
     */
    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the server profile schema.
     *
     * @param serverProfileSchemaId
     *            the server profile schema id
     * @return the server profile schema dto
     * @throws ControlServiceException 
     *          the control service exception
     */
    ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws ControlServiceException;

    /**
     * Gets the server profile schema by application id and server profile
     * schema version.
     *
     * @param applicationId
     *            the application id
     * @param version
     *            the server profile schema version
     * @return the server profile schema
     * @throws ControlServiceException
     *             the control service exception
     */
    ServerProfileSchemaDto getServerProfileSchemaByApplicationIdAndVersion(String applicationId, int version)
            throws ControlServiceException;

    /**
     * Edits the server profile schema.
     *
     * @param serverProfileSchema
     *            the server profile schema
     * @return the server profile schema dto
     * @throws ControlServiceException 
     *          the control service exception
     */
    ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws ControlServiceException;

    /**
     * Gets the latest created server profile schema for application.
     *
     * @param applicationId
     *            the application id
     * @return the server profile schema dto
     * @throws ControlServiceException 
     *          the control service exception
     */
    ServerProfileSchemaDto findLatestServerProfileSchema(String applicationId) throws ControlServiceException;

    /**
     * Gets the endpoint groups by application id.
     *
     * @param applicationId
     *            the application id
     * @return the endpoint groups by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the endpoint group.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @return the endpoint group
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointGroupDto getEndpointGroup(String endpointGroupId) throws ControlServiceException;

    /**
     * Edits the endpoint group.
     *
     * @param endpointGroup
     *            the endpoint group
     * @return the endpoint group dto
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws ControlServiceException;

    /**
     * Delete endpoint group.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteEndpointGroup(String endpointGroupId) throws ControlServiceException;

    /**
     * Removes the topics from endpoint group.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @param topicId
     *            the topic id
     * @return the endpoint group dto
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointGroupDto removeTopicsFromEndpointGroup(String endpointGroupId, String topicId) throws ControlServiceException;

    /**
     * Adds the topics to endpoint group.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @param topicId
     *            the topic id
     * @return the endpoint group dto
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointGroupDto addTopicsToEndpointGroup(String endpointGroupId, String topicId) throws ControlServiceException;

    /**
     * Gets the profile filter.
     *
     * @param profileFilterId
     *            the profile filter id
     * @return the profile filter
     * @throws ControlServiceException
     *             the control service exception
     */
    ProfileFilterDto getProfileFilter(String profileFilterId) throws ControlServiceException;

    /**
     * Gets the profile filter records by endpoint group id.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @param includeDeprecated
     *            the include deprecated
     * @return the profile filter records by endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated)
            throws ControlServiceException;

    /**
     * Gets the profile filter record.
     *
     * @param endpointProfileSchemaId
     *            the endpoint profile schema id
     * @param serverProfileSchemaId
     *            the server profile schema id
     * @param endpointGroupId
     *            the endpoint group id
     * @return the profile filter record
     * @throws ControlServiceException
     *             the control service exception
     */
    ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId)
            throws ControlServiceException;

    /**
     * Gets the vacant profile schemas by endpoint group id.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @return the vacant profile schemas by endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Edits the profile filter.
     *
     * @param profileFilter
     *            the profile filter
     * @return the profile filter dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws ControlServiceException;

    /**
     * Gets the configuration records by endpoint group id.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @param includeDeprecated
     *            the include deprecated
     * @return the configuration records by endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated)
            throws ControlServiceException;

    /**
     * Gets the configuration record.
     *
     * @param schemaId
     *            the schema id
     * @param endpointGroupId
     *            the endpoint group id
     * @return the configuration record
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the vacant configuration schemas by endpoint group id.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @return the vacant configuration schemas by endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the configuration.
     *
     * @param configurationId
     *            the configuration id
     * @return the configuration
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationDto getConfiguration(String configurationId) throws ControlServiceException;

    /**
     * Edits the configuration.
     *
     * @param configuration
     *            the configuration
     * @return the configuration dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationDto editConfiguration(ConfigurationDto configuration) throws ControlServiceException;

    /**
     * Edits the user configuration.
     *
     * @param configuration
     *            the configuration
     * @throws ControlServiceException
     *             the control service exception
     */
    void editUserConfiguration(EndpointUserConfigurationDto configuration) throws ControlServiceException;

    /**
     * Activate configuration.
     *
     * @param configurationId
     *            the configuration id
     * @param activatedUsername
     *            the activated username
     * @return the configuration dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationDto activateConfiguration(String configurationId, String activatedUsername) throws ControlServiceException;

    /**
     * Deactivate configuration.
     *
     * @param configurationId
     *            the configuration id
     * @param deactivatedUsername
     *            the deactivated username
     * @return the configuration dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ConfigurationDto deactivateConfiguration(String configurationId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Delete configuration record.
     *
     * @param schemaId
     *            the schema id
     * @param endpointGroupId
     *            the endpoint group id
     * @param deactivatedUsername
     *            the deactivated username
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteConfigurationRecord(String schemaId, String endpointGroupId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Activate profile filter.
     *
     * @param profileFilterId
     *            the profile filter id
     * @param activatedUsername
     *            the activated username
     * @return the profile filter dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ProfileFilterDto activateProfileFilter(String profileFilterId, String activatedUsername) throws ControlServiceException;

    /**
     * Deactivate profile filter.
     *
     * @param profileFilterId
     *            the profile filter id
     * @param deactivatedUsername
     *            the deactivated username
     * @return the profile filter dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ProfileFilterDto deactivateProfileFilter(String profileFilterId, String deactivatedUsername) throws ControlServiceException;

    /**
     * Delete profile filter record.
     *
     * @param endpointProfileSchemaId
     *            the endpoint profile schema id
     * @param serverProfileSchemaId
     *            the server profile schema id
     * @param endpointGroupId
     *            the endpoint group id
     * @param deactivatedUsername
     *            the deactivated username
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId,
            String deactivatedUsername) throws ControlServiceException;

    /**
     * Generate sdk.
     *
     * @param sdkProperties
     *            the sdk properties
     * @param platform
     *            the platform
     * @return the file data
     * @throws ControlServiceException
     *             the control service exception
     */
    FileData generateSdk(SdkProfileDto sdkProperties, SdkPlatform platform) throws ControlServiceException;

    /**
     * Generate record structure library.
     *
     * @param applicationId
     *            the application id
     * @param logSchemaVersion
     *            the log schema version
     * @return the file data
     * @throws ControlServiceException
     *             the control service exception
     */
    FileData generateRecordStructureLibrary(String applicationId, int logSchemaVersion) throws ControlServiceException;

    /**
     * Edits the notification schema.
     *
     * @param notificationSchema
     *            the notification schema
     * @return the notification schema dto
     * @throws ControlServiceException
     *             the control service exception
     */
    NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema) throws ControlServiceException;

    /**
     * Gets the notification schema.
     *
     * @param notificationSchemaId
     *            the notification schema id
     * @return the notification schema
     * @throws ControlServiceException
     *             the control service exception
     */
    NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws ControlServiceException;

    /**
     * Gets the notification schemas by app id.
     *
     * @param applicationId
     *            the application id
     * @return the notification schemas by app id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<NotificationSchemaDto> getNotificationSchemasByAppId(String applicationId) throws ControlServiceException;

    /**
     * Gets the user notification schemas by app id.
     *
     * @param applicationId
     *            the application id
     * @return the user notification schemas by app id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<VersionDto> getUserNotificationSchemasByAppId(String applicationId) throws ControlServiceException;

    /**
     * Find notification schemas by app id and type.
     *
     * @param applicationId
     *            the application id
     * @param type
     *            the type
     * @return the list
     * @throws ControlServiceException
     *             the control service exception
     */
    List<NotificationSchemaDto> findNotificationSchemasByAppIdAndType(String applicationId, NotificationTypeDto type)
            throws ControlServiceException;

    /**
     * Edits the log schema.
     *
     * @param logSchemaDto
     *            the log schema dto
     * @return the log schema dto
     * @throws ControlServiceException
     *             the control service exception
     */
    LogSchemaDto saveLogSchema(LogSchemaDto logSchemaDto) throws ControlServiceException;

    /**
     * Get the log schema.
     *
     * @param schemaId
     *            the log schema string
     * @return the log schema dto
     * @throws ControlServiceException
     *             the control service exception
     */
    String getFlatSchemaByCtlSchemaId(String schemaId) throws ControlServiceException;

    /**
     * Gets the log schemas by application id.
     *
     * @param applicationId
     *            the application id
     * @return the log schemas by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the log schema.
     *
     * @param logSchemaId
     *            the log schema id
     * @return the log schema
     * @throws ControlServiceException
     *             the control service exception
     */
    LogSchemaDto getLogSchema(String logSchemaId) throws ControlServiceException;

    /**
     * Gets the log schema by application id and version.
     *
     * @param applicationId
     *            the application id
     * @param version
     *            the version
     * @return the log schema by application id and version
     * @throws ControlServiceException
     *             the control service exception
     */
    LogSchemaDto getLogSchemaByApplicationIdAndVersion(String applicationId, int version) throws ControlServiceException;

    /**
     * Edits the notification.
     *
     * @param notification
     *            the notification
     * @return the notification dto
     * @throws ControlServiceException
     *             the control service exception
     */
    NotificationDto editNotification(NotificationDto notification) throws ControlServiceException;

    /**
     * Gets the notification.
     *
     * @param notificationId
     *            the notification id
     * @return the notification
     * @throws ControlServiceException
     *             the control service exception
     */
    NotificationDto getNotification(String notificationId) throws ControlServiceException;

    /**
     * Gets the notifications by topic id.
     *
     * @param topicId
     *            the topic id
     * @return the notifications by topic id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<NotificationDto> getNotificationsByTopicId(String topicId) throws ControlServiceException;

    /**
     * Edits the topic.
     *
     * @param topic
     *            the topic
     * @return the topic dto
     * @throws ControlServiceException
     *             the control service exception
     */
    TopicDto editTopic(TopicDto topic) throws ControlServiceException;

    /**
     * Gets the topic.
     *
     * @param topicId
     *            the topic id
     * @return the topic
     * @throws ControlServiceException
     *             the control service exception
     */
    TopicDto getTopic(String topicId) throws ControlServiceException;

    /**
     * Gets the topic by app id.
     *
     * @param appId
     *            the app id
     * @return the topic by app id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<TopicDto> getTopicByAppId(String appId) throws ControlServiceException;

    /**
     * Gets the topic by endpoint group id.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @return the topic by endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<TopicDto> getTopicByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Gets the vacant topic by endpoint group id.
     *
     * @param endpointGroupId
     *            the endpoint group id
     * @return the vacant topic by endpoint group id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<TopicDto> getVacantTopicByEndpointGroupId(String endpointGroupId) throws ControlServiceException;

    /**
     * Delete topic by id.
     *
     * @param topicId
     *            the topic id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteTopicById(String topicId) throws ControlServiceException;

    /**
     * Gets the unicast notification.
     *
     * @param notificationId
     *            the notification id
     * @return the unicast notification
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointNotificationDto getUnicastNotification(String notificationId) throws ControlServiceException;

    /**
     * Edits the unicast notification.
     *
     * @param notification
     *            the notification
     * @return the endpoint notification dto
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointNotificationDto editUnicastNotification(EndpointNotificationDto notification) throws ControlServiceException;

    /**
     * Gets the unicast notifications by key hash.
     *
     * @param keyhash
     *            the keyhash
     * @return the unicast notifications by key hash
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EndpointNotificationDto> getUnicastNotificationsByKeyHash(byte[] keyhash) throws ControlServiceException;

    /**
     * Gets the configuration schema versions by application id.
     *
     * @param applicationId
     *            the application id
     * @return the configuration schema versions by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<VersionDto> getConfigurationSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the profile schema versions by application id.
     *
     * @param applicationId
     *            the application id
     * @return the profile schema versions by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<VersionDto> getProfileSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the notification schema versions by application id.
     *
     * @param applicationId
     *            the application id
     * @return the notification schema versions by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<VersionDto> getNotificationSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the log schema versions by application id.
     *
     * @param applicationId
     *            the application id
     * @return the log schema versions by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<VersionDto> getLogSchemaVersionsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Edits the event class family.
     *
     * @param eventClassFamily
     *            the event class family
     * @return the event class family dto
     * @throws ControlServiceException
     *             the control service exception
     */
    EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws ControlServiceException;

    /**
     * Gets the event class families by tenant id.
     *
     * @param tenantId
     *            the tenant id
     * @return the event class families by tenant id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EventClassFamilyDto> getEventClassFamiliesByTenantId(String tenantId) throws ControlServiceException;

    /**
     * Gets the event class family.
     *
     * @param eventClassFamilyId
     *            the event class family id
     * @return the event class family
     * @throws ControlServiceException
     *             the control service exception
     */
    EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws ControlServiceException;

    /**
     * Adds the event class family schema.
     *
     * @param eventClassFamilyId
     *            the event class family id
     * @param eventClassFamilySchema
     *            the event class family schema
     * @param createdUsername
     *            the created username
     * @throws ControlServiceException
     *             the control service exception
     */
    void addEventClassFamilySchema(String eventClassFamilyId, String eventClassFamilySchema, String createdUsername)
            throws ControlServiceException;

    /**
     * Gets the event classes by family id version and type.
     *
     * @param ecfId
     *            the ecf id
     * @param version
     *            the version
     * @param type
     *            the type
     * @return the event classes by family id version and type
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String ecfId, int version, EventClassType type)
            throws ControlServiceException;

    /**
     * Edits the application event family map.
     *
     * @param applicationEventFamilyMap
     *            the application event family map
     * @return the application event family map dto
     * @throws ControlServiceException
     *             the control service exception
     */
    ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws ControlServiceException;

    /**
     * Gets the application event family map.
     *
     * @param applicationEventFamilyMapId
     *            the application event family map id
     * @return the application event family map
     * @throws ControlServiceException
     *             the control service exception
     */
    ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws ControlServiceException;

    /**
     * Gets the application event family maps by application id.
     *
     * @param applicationId
     *            the application id
     * @return the application event family maps by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the vacant event class families by application id.
     *
     * @param applicationId
     *            the application id
     * @return the vacant event class families by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the event class families by application id.
     *
     * @param applicationId
     *            the application id
     * @return the event class families by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the endpoint users.
     *
     * @return the endpoint users
     * @throws ControlServiceException
     *             the control service exception
     */
    List<EndpointUserDto> getEndpointUsers() throws ControlServiceException;

    /**
     * Gets the endpoint user.
     *
     * @param endpointUserId
     *            the endpoint user id
     * @return the endpoint user
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointUserDto getEndpointUser(String endpointUserId) throws ControlServiceException;

    /**
     * Generate endpoint user access token.
     *
     * @param externalUid
     *            the external uid
     * @param tenantId
     *            the tenant id
     * @return the string
     * @throws ControlServiceException
     *             the control service exception
     */
    String generateEndpointUserAccessToken(String externalUid, String tenantId) throws ControlServiceException;

    /**
     * Gets the log appenders by application id.
     *
     * @param applicationId
     *            the application id
     * @return the log appenders by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<LogAppenderDto> getLogAppendersByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the log appender.
     *
     * @param logAppenderId
     *            the log appender id
     * @return the log appender
     * @throws ControlServiceException
     *             the control service exception
     */
    LogAppenderDto getLogAppender(String logAppenderId) throws ControlServiceException;

    /**
     * Edits the log appender.
     *
     * @param logAppender
     *            the log appender
     * @return the log appender dto
     * @throws ControlServiceException
     *             the control service exception
     */
    LogAppenderDto editLogAppender(LogAppenderDto logAppender) throws ControlServiceException;

    /**
     * Delete log appender.
     *
     * @param logAppenderId
     *            the log appender id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteLogAppender(String logAppenderId) throws ControlServiceException;

    /**
     * Gets the user verifiers by application id.
     *
     * @param applicationId
     *            the application id
     * @return the user verifiers by application id
     * @throws ControlServiceException
     *             the control service exception
     */
    List<UserVerifierDto> getUserVerifiersByApplicationId(String applicationId) throws ControlServiceException;

    /**
     * Gets the user verifier.
     *
     * @param userVerifierId
     *            the user verifier id
     * @return the user verifier
     * @throws ControlServiceException
     *             the control service exception
     */
    UserVerifierDto getUserVerifier(String userVerifierId) throws ControlServiceException;

    /**
     * Edits the user verifier.
     *
     * @param userVerifier
     *            the user verifier
     * @return the user verifier dto
     * @throws ControlServiceException
     *             the control service exception
     */
    UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws ControlServiceException;

    /**
     * Delete user verifier.
     *
     * @param userVerifierId
     *            the user verifier id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteUserVerifier(String userVerifierId) throws ControlServiceException;

    /**
     * Gets the record structure schema.
     *
     * @param applicationId
     *            the application id
     * @param logSchemaVersion
     *            the log schema version
     * @return the record structure schema
     * @throws ControlServiceException
     *             the control service exception
     */
    FileData getRecordStructureSchema(String applicationId, int logSchemaVersion) throws ControlServiceException;

    /**
     * Gets the record structure data.
     *
     * @param key
     *            the key
     * @return the record structure data
     * @throws ControlServiceException
     *             the control service exception
     */
    FileData getRecordStructureData(RecordKey key) throws ControlServiceException;

    /**
     * Gets page of EndpointProfilesBodyDto objects by endpoint group id.
     *
     * @param pageLinkDto
     *            the page object include endpoint group id, limit and offset.
     * @return the EndpointProfilesBodyDto object.
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLinkDto) throws ControlServiceException;

    /**
     * Gets endpoint profile by endpoint key hash.
     *
     * @param endpointKeyHash
     *            the endpoint key hash in string representation.
     * @return the EndpointProfileDto object.
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfileDto getEndpointProfileByKeyHash(String endpointKeyHash) throws ControlServiceException;

    /**
     ** Gets endpoint profile body by endpoint key hash.
     *
     * @param endpointKeyHash
     *            the endpoint key hash in string representation.
     * @return the EndpointProfileBodyDto object.
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointKeyHash) throws ControlServiceException;

    /**
     * Gets page of EndpointProfilesBodyDto objects by endpoint group id.
     *
     * @param pageLinkDto
     *            the page object include endpoint group id, limit and offset.
     * @return the EndpointProfilesPageDto object.
     * @throws ControlServiceException
     *             the control service exception
     */
    EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(PageLinkDto pageLinkDto) throws ControlServiceException;

    /**
     * Updates server profile of endpoint profile
     *
     * @param endpointKeyHash
     *            the endpoint key hash identifier.
     * @param version
     *            the server profile schema version
     * @param serverProfile
     *            server profile data in string representation.
     * @return the updated endpoint profile.
     * @throws ControlServiceException 
     *          the control service exception
     */
    EndpointProfileDto updateServerProfile(String endpointKeyHash, int version, String serverProfile) throws ControlServiceException;

    /**
     * Saves a CTL schema to the database.
     * 
     * @param schema
     *            A CTL schema to save
     * 
     * @return The saved CTL schema
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Deletes a CTL schema from the database by its fully qualified name,
     * version number, tenant and application identifier.
     * 
     * @param fqn
     *            A fully qualified CTL schema name
     * @param version
     *            A CTL schema version number
     * @param tenantId
     *            A tenant identifier
     * @param applicationId
     *            An application identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    void deleteCTLSchemaByFqnAndVersionTenantIdAndApplicationId(String fqn, int version, String tenantId, String applicationId) throws ControlServiceException;

    /**
     * Returns a CTL schema by its identifier.
     * 
     * @param schemaId
     *            A CTL schema identifier
     * 
     * @return A CTL schema with the given identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    CTLSchemaDto getCTLSchemaById(String schemaId) throws ControlServiceException;

    /**
     * Returns a CTL schema by its fully qualified name, version number,
     * tenant and application identifier.
     * 
     * @param fqn
     *            A fully qualified CTL schema name
     * @param version
     *            A CTL schema version number
     * @param tenantId
     *            A tenant identifier
     * @param applicationId
     *            An application identifier
     * 
     * @return A CTL schema with the given fully qualified name, version number,
     *         tenant and application identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, int version, String tenantId, String applicationId)
            throws ControlServiceException;

    /**
     * Returns a CTL schema with the given meta info id and version.
     *
     * @param metaInfoId the id of meta info object.
     * @param version    the schema version.
     * @return the CTL schema with the given meta info id and version.
     */    
    CTLSchemaDto getCTLSchemaByMetaInfoIdAndVer(String metaInfoId, Integer version);
    
    /**
     * Returns any CTL schema by its fully qualified name, version number,
     * tenant and application identifier.
     * 
     * @param fqn
     *            A fully qualified CTL schema name
     * @param version
     *            A CTL schema version number
     * @param tenantId
     *            A tenant identifier
     * @param applicationId
     *            An application identifier
     * 
     * @return Any CTL schema with the given fully qualified name, version number,
     *         tenant and application identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    CTLSchemaDto getAnyCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, int version, String tenantId, String applicationId) throws ControlServiceException;
    
    /**
     * Get CTL schema meta infos which are the application level siblings to the CTL 
     * of the given fully qualified name, tenant and application identifiers.
     *
     * @param fqn     the fully qualified.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the CTL schema meta information objects which are the siblings to the given CTL.
     */
    List<CTLSchemaMetaInfoDto> getSiblingsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);
    
    /**
     * Update existing CTL schema meta info scope by the given CTL schema meta info object.
     *
     * @param ctlSchemaMetaInfo
     *            the CTL schema meta info object.
     * @return CTLSchemaMetaInfoDto the updated CTL schema meta info object.
     */
    CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo);
    
    /**
     * Returns meta information about system CTL schemas.
     * 
     * @return Meta information about system CTL schemas
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    List<CTLSchemaMetaInfoDto> getSystemCTLSchemasMetaInfo() throws ControlServiceException;

    Map<Fqn, List<Integer>> getAvailableCTLSchemaVersionsForSystem() throws ControlServiceException;

    /**
     * Returns meta information about CTL schemas that are available for use by
     * a tenant with the given identifier
     * 
     * @param tenantId
     *            A tenant identifier
     * 
     * @return Meta information about CTL schemas that are available for use by
     *         a tenant with the given identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    List<CTLSchemaMetaInfoDto> getAvailableCTLSchemasMetaInfoForTenant(String tenantId) throws ControlServiceException;
    
    Map<Fqn, List<Integer>> getAvailableCTLSchemaVersionsForTenant(String tenantId) throws ControlServiceException;
    
    /**
     * Returns meta information about CTL schemas that are available for use by
     * an application with the given identifier
     * 
     * @param tenantId
     *            A tenant identifier
     * @param appId
     *            An application identifier
     * @return Meta information about CTL schemas that are available for use by
     *         a tenant with the given identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    List<CTLSchemaMetaInfoDto> getAvailableCTLSchemasMetaInfoForApplication(String tenantId, String appId) throws ControlServiceException;

    Map<Fqn, List<Integer>> getAvailableCTLSchemaVersionsForApplication(String tenantId, String appId) throws ControlServiceException;

    /**
     * Returns CTL schemas that reference a CTL schema with the given
     * identifier.
     * 
     * @param schemaId
     *            A CTL schema identifier
     * 
     * @return CTL schemas that reference a CTL schema with the given identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    List<CTLSchemaDto> getCTLSchemaDependents(String schemaId) throws ControlServiceException;

    /**
     * Returns CTL schemas that reference a CTL schema with the given fully
     * qualified name, version number, tenant and application identifier.
     * 
     * @param fqn
     *            A fully qualified CTL schema name
     * @param version
     *            A CTL schema version number
     * @param tenantId
     *            A tenant identifier
     * @param applicationId
     *            An application identifier
     *              
     * @return CTL schemas that reference a CTL schema with the given fully
     *         qualified name, version number, tenant and application identifier
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    List<CTLSchemaDto> getCTLSchemaDependents(String fqn, int version, String tenantId, String applicationId) throws ControlServiceException;

    CTLSchemaDto getLatestCTLSchemaByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) throws ControlServiceException;

    List<Integer> getAllCTLSchemaVersionsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId) throws ControlServiceException;

    /**
     * Returns the last version of CTL schema with the given meta info id.
     *
     * @param metaInfoId the id of meta info object.
     * @return the latest version of  CTL schema with the given meta info id.
     */    
    CTLSchemaDto getLatestCTLSchemaByMetaInfoId(String metaInfoId);
    
    /**
     * Exports the body of a CTL schema.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return A file containing the body of a CTL schema
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    FileData exportCTLSchemaShallow(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Exports the body of a CTL schema with all dependencies inline,
     * recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * @return A file containing the body of a CTL schema with all dependencies
     *         inline, recursively
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    FileData exportCTLSchemaFlat(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Exports the body of a CTL schema as java library with all dependencies
     * inline, recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return A java archive file containing compiled classes of a CTL avro
     *         schema with all dependencies inline, recursively
     * @throws ControlServiceException 
     *          the control service exception
     */
    FileData exportCTLSchemaFlatAsLibrary(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Exports the body of a CTL schema with all dependencies inline,
     * recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * @return A string containing the body of a CTL schema with all
     *         dependencies inline, recursively
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    String exportCTLSchemaFlatAsString(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Exports the CTL schema as avro schema with all dependencies inline,
     * recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * @return An avro schema of a CTL schema with all dependencies inline,
     *         recursively
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    Schema exportCTLSchemaFlatAsSchema(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Exports the body of a CTL schema with all dependencies as different
     * files, recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return An archive containing the body of a CTL schema as a file and all
     *         dependencies as different files, recursively.
     * 
     * @throws ControlServiceException
     *             - if an exception occures.
     */
    FileData exportCTLSchemaDeep(CTLSchemaDto schema) throws ControlServiceException;

    /**
     * Gets SdkProfileDto object by sdk profile id.
     *
     * @param sdkProfileId
     *            sdk profile id
     * @return the SdkProfileDto
     * @throws ControlServiceException
     *             the control service exception
     */
    SdkProfileDto getSdkProfile(String sdkProfileId) throws ControlServiceException;

    /**
     *
     * @param applicationId
     *            the application id
     * @return the list sdk profile dto
     * @throws ControlServiceException
     *             the control service exception
     */
    List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws ControlServiceException;

    /**
     *
     * @param sdkProfileId
     *            the sdk profile id
     * @throws ControlServiceException
     *             the control service exception
     */
    void deleteSdkProfile(String sdkProfileId) throws ControlServiceException;

    /**
     *
     * @param token
     *            the token
     * @return boolean the sdk profile usage
     * @throws ControlServiceException
     *             the control service exception
     */
    boolean isSdkProfileUsed(String token) throws ControlServiceException;

    /**
     *
     * @param sdkProfile
     *            the sdk profile
     * @return saved SdkProfileDto object.
     * @throws ControlServiceException
     *             the control service exception
     */
    SdkProfileDto saveSdkProfile(SdkProfileDto sdkProfile) throws ControlServiceException;

    /**
     * Gets SdkProfileDto object by sdk token.
     *
     * @param sdkToken
     *            sdk token
     * @return the SdkProfileDto
     * @throws ControlServiceException
     *             the control service exception
     */
    SdkProfileDto findSdkProfileByToken(String sdkToken) throws ControlServiceException;

    /**
     * Removes the given endpoint profile from the database.
     *
     * @param endpointProfile The endpoint profile
     *
     * @throws ControlServiceException - if an exception occures.
     */
    void removeEndpointProfile(EndpointProfileDto endpointProfile) throws ControlServiceException;

    /**
     * Returns a list of endpoint profiles for the endpoint user with the given external ID and tenant ID.
     *
     * @param endpointUserExternalId The endpoint user external ID
     * @param tenantId The tenant ID
     *
     * @return A list of endpoint profiles for the endpoint user with the given ID.
     *
     * @throws ControlServiceException - if an exception occures.
     */
    List<EndpointProfileDto> getEndpointProfilesByUserExternalIdAndTenantId(String endpointUserExternalId, String tenantId) throws ControlServiceException;

    /**
     * Provides security credentials, allowing an endpoint that uses them to
     * interact with the specified application.
     *
     * @param applicationId The application ID to allow interaction with
     * @param credentialsBody The security credentials to save
     *
     * @return The security credentials saved
     *
     * @throws ControlServiceException - if an exception occures.
     */
    CredentialsDto provisionCredentials(String applicationId, String credentialsBody) throws ControlServiceException;

    /**
     * Returns credentials by application ID and credentials ID.
     *
     * @param applicationId The application ID
     * @param credentialsId The credentials ID
     *
     * @return The credentials found
     *
     * @throws ControlServiceException - if an exception occures.
     */
    Optional<CredentialsDto> getCredentials(String applicationId, String credentialsId) throws ControlServiceException;

    /**
     * Revokes security credentials from the corresponding credentials storage.
     * Also launches an asynchronous process to terminate all active sessions of
     * the endpoint that uses these credentials.
     *
     * @param applicationId The application ID
     * @param credentialsId The credentials ID
     *
     * @throws ControlServiceException - if an exception occures.
     */
    void revokeCredentials(String applicationId, String credentialsId) throws ControlServiceException;
    
    /**
     * Notifies the Kaa cluster about security credentials revocation. If an
     * endpoint is already registered with the specified credentials, this API
     * call launches an asynchronous process to terminate all active sessions of
     * the corresponding endpoint.
     *
     * @param applicationId The application ID
     * @param credentialsId The credentials ID
     *
     * @throws ControlServiceException - if an exception occures.
     */
    void onCredentailsRevoked(String applicationId, String credentialsId) throws ControlServiceException;

    /**
     * Binds credentials to the server-side endpoint profile specified.
     *
     * @param applicationId The application ID
     * @param credentialsId The ID of the credentials to bind
     * @param serverProfileVersion The server-side endpoint profile version
     * @param serverProfileBody The server-side endpoint profile body
     *
     * @throws ControlServiceException - if an exception occures.
     */
    void provisionRegistration(String applicationId, String credentialsId, Integer serverProfileVersion, String serverProfileBody) throws ControlServiceException;

    /**
     * Returns the names of credentials services configured.
     *
     * @return The names of credentials services configured
     *
     * @throws ControlServiceException - if an exception occures.
     */
    List<String> getCredentialsServiceNames() throws ControlServiceException;
}
