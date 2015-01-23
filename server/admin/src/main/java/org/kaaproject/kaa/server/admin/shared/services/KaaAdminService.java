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

package org.kaaproject.kaa.server.admin.shared.services;

import java.util.List;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkKey;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderRestDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.file.FileData;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderFormWrapper;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/kaaAdminService")
public interface KaaAdminService extends RemoteService {

    public List<TenantUserDto> getTenants() throws KaaAdminServiceException;

    public TenantUserDto getTenant(String userId) throws KaaAdminServiceException;

    public TenantUserDto editTenant(TenantUserDto tenantUser) throws KaaAdminServiceException;

    public void deleteTenant(String userId) throws KaaAdminServiceException;

    public List<ApplicationDto> getApplications() throws KaaAdminServiceException;

    public ApplicationDto getApplication(String applicationId) throws KaaAdminServiceException;

    public ApplicationDto editApplication(ApplicationDto application) throws KaaAdminServiceException;

    public void deleteApplication(String applicationId) throws KaaAdminServiceException;

    public UserDto getUserProfile() throws KaaAdminServiceException;

    public UserDto editUserProfile(UserDto userDto) throws KaaAdminServiceException;
    
    public PropertiesDto getMailProperties() throws KaaAdminServiceException;

    public PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto) throws KaaAdminServiceException;

    public PropertiesDto getGeneralProperties() throws KaaAdminServiceException;

    public PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto) throws KaaAdminServiceException;
    
    public List<UserDto> getUsers() throws KaaAdminServiceException;

    public UserDto getUser(String userId) throws KaaAdminServiceException;

    public UserDto editUser(UserDto user) throws KaaAdminServiceException;

    public void deleteUser(String userId) throws KaaAdminServiceException;

    public SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public String getSdk(String applicationId, Integer configurationSchemaVersion, Integer profileSchemaVersion, Integer notificationSchemaVersion, SdkPlatform targetPlatform, List<String> aefMapIds, Integer logSchemaVersion) throws KaaAdminServiceException;
    
    public FileData getSdk(SdkKey key) throws KaaAdminServiceException;
    
    public void flushSdkCache() throws KaaAdminServiceException;

    public List<ProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public ProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException;

    public ProfileSchemaDto editProfileSchema(ProfileSchemaDto profileSchema, String fileItemName) throws KaaAdminServiceException;

    public ProfileSchemaDto editProfileSchema(ProfileSchemaDto profileSchema, byte[] schema) throws KaaAdminServiceException;

    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws KaaAdminServiceException;

    public ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema, String fileItemName) throws KaaAdminServiceException;

    public ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema, byte[] schema) throws KaaAdminServiceException;

    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<SchemaDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException;

    public NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema, String fileItemName) throws KaaAdminServiceException;

    public NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema, byte[] schema) throws KaaAdminServiceException;

    public List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException;

    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException;

    public LogSchemaDto editLogSchema(LogSchemaDto logSchema, String fileItemName) throws KaaAdminServiceException;

    public LogSchemaDto editLogSchema(LogSchemaDto profileSchema, byte[] schema) throws KaaAdminServiceException;

    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws KaaAdminServiceException;

    public void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    public List<StructureRecordDto<ProfileFilterDto>> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    public StructureRecordDto<ProfileFilterDto> getProfileFilterRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<SchemaDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws KaaAdminServiceException;

    public ProfileFilterDto activateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    public void deleteProfileFilterRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<StructureRecordDto<ConfigurationDto>> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    public StructureRecordDto<ConfigurationDto> getConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public StructureRecordDto<ConfigurationRecordFormDto> getConfigurationRecordForm(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<SchemaDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws KaaAdminServiceException;

    public ConfigurationRecordFormDto editConfigurationRecordForm(ConfigurationRecordFormDto configuration) throws KaaAdminServiceException;
    
    public ConfigurationDto activateConfiguration(String configurationId) throws KaaAdminServiceException;

    public ConfigurationRecordFormDto activateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;
    
    public ConfigurationDto deactivateConfiguration(String configurationId) throws KaaAdminServiceException;

    public ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;
    
    public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<TopicDto> getTopicsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public TopicDto getTopic(String topicId) throws KaaAdminServiceException;

    public TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException;

    public void deleteTopic(String topicId) throws KaaAdminServiceException;

    public void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;
    
    public RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

    public void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException;
    
    public void sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException;

    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException;

    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws KaaAdminServiceException;

    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException;

    public void addEventClassFamilySchema(String eventClassFamilyId, String fileItemName) throws KaaAdminServiceException;

    public void addEventClassFamilySchema(String eventClassFamilyId, byte[] schema) throws KaaAdminServiceException;

    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type) throws KaaAdminServiceException;

    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws KaaAdminServiceException;

    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap) throws KaaAdminServiceException;

    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    public LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException;

    public LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    public List<LogAppenderRestDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    public LogAppenderRestDto getRestLogAppender(String appenderId) throws KaaAdminServiceException;

    public LogAppenderRestDto editRestLogAppender(LogAppenderRestDto appender) throws KaaAdminServiceException;
    
    public LogAppenderFormWrapper getLogAppenderForm(String appenderId) throws KaaAdminServiceException;

    public LogAppenderFormWrapper editLogAppenderForm(LogAppenderFormWrapper wrapper) throws KaaAdminServiceException;

    public void deleteLogAppender(String appenderId) throws KaaAdminServiceException;

    public List<LogAppenderInfoDto> getLogAppenderInfos() throws KaaAdminServiceException;
    
    public List<SchemaDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException;

    public String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

}
