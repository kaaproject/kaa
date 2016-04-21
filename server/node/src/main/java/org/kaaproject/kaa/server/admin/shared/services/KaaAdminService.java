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

package org.kaaproject.kaa.server.admin.shared.services;

import java.util.List;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
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
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordViewDto;
import org.kaaproject.kaa.server.admin.shared.endpoint.EndpointProfileViewDto;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/kaaAdminService")
public interface KaaAdminService extends RemoteService {

    RecordField generateRecordFromSchemaJson(String avroSchema) throws KaaAdminServiceException;

    EndpointProfileViewDto getEndpointProfileViewByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException;

    EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException;

    EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException;

    EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException;

    EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointKeyHash) throws KaaAdminServiceException;
    
    EndpointProfileDto updateServerProfile(String endpointKeyHash, int serverProfileVersion, String serverProfileBody) throws KaaAdminServiceException;

    EndpointProfileDto updateServerProfile(String endpointKeyHash, int serverProfileVersion, RecordField serverProfileRecord) throws KaaAdminServiceException;

    List<TenantUserDto> getTenants() throws KaaAdminServiceException;

    TenantUserDto getTenant(String userId) throws KaaAdminServiceException;

    TenantUserDto editTenant(TenantUserDto tenantUser) throws KaaAdminServiceException;

    void deleteTenant(String userId) throws KaaAdminServiceException;

    List<ApplicationDto> getApplications() throws KaaAdminServiceException;

    ApplicationDto getApplication(String applicationId) throws KaaAdminServiceException;

    ApplicationDto getApplicationByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    ApplicationDto editApplication(ApplicationDto application) throws KaaAdminServiceException;

    void deleteApplication(String applicationId) throws KaaAdminServiceException;

    UserDto getUserProfile() throws KaaAdminServiceException;

    UserDto editUserProfile(UserDto userDto) throws KaaAdminServiceException;

    PropertiesDto getMailProperties() throws KaaAdminServiceException;

    PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto) throws KaaAdminServiceException;

    PropertiesDto getGeneralProperties() throws KaaAdminServiceException;

    PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto) throws KaaAdminServiceException;

    List<UserDto> getUsers() throws KaaAdminServiceException;

    UserDto getUser(String userId) throws KaaAdminServiceException;

    UserDto editUser(UserDto user) throws KaaAdminServiceException;

    void deleteUser(String userId) throws KaaAdminServiceException;

    SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws KaaAdminServiceException;

    SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException;

    void deleteSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    SdkProfileDto getSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws KaaAdminServiceException;

    SdkProfileViewDto getSdkProfileView(String sdkProfileId) throws KaaAdminServiceException;

    String generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    FileData getSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    void flushSdkCache() throws KaaAdminServiceException;

    RecordField createSimpleEmptySchemaForm() throws KaaAdminServiceException;

    RecordField createCommonEmptySchemaForm() throws KaaAdminServiceException;

    RecordField createConfigurationEmptySchemaForm() throws KaaAdminServiceException;

    RecordField createEcfEmptySchemaForm() throws KaaAdminServiceException;

    RecordField generateSimpleSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateCommonSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateConfigurationSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateEcfSchemaForm(String fileItemName) throws KaaAdminServiceException;

    List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException;

    EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException;

    ProfileSchemaViewDto getProfileSchemaView(String profileSchemaId) throws KaaAdminServiceException;

    ProfileSchemaViewDto saveProfileSchemaView(ProfileSchemaViewDto profileSchema) throws KaaAdminServiceException;

    ProfileSchemaViewDto createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws KaaAdminServiceException;

    ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema, byte[] schema) throws KaaAdminServiceException;

    ConfigurationSchemaDto getConfigurationSchemaForm(String configurationSchemaId) throws KaaAdminServiceException;

    ConfigurationSchemaDto editConfigurationSchemaForm(ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException;

    List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<VersionDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException;

    NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema, byte[] schema) throws KaaAdminServiceException;

    NotificationSchemaDto getNotificationSchemaForm(String notificationSchemaId) throws KaaAdminServiceException;

    NotificationSchemaDto editNotificationSchemaForm(NotificationSchemaDto notificationSchema) throws KaaAdminServiceException;

    List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException;

    LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException;

    LogSchemaDto editLogSchema(LogSchemaDto profileSchema, byte[] schema) throws KaaAdminServiceException;

    LogSchemaDto getLogSchemaForm(String logSchemaId) throws KaaAdminServiceException;

    LogSchemaDto editLogSchemaForm(LogSchemaDto logSchema) throws KaaAdminServiceException;

    List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException;

    EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws KaaAdminServiceException;

    void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException;

    List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws KaaAdminServiceException;

    ProfileFilterDto activateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException;

    List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    ConfigurationRecordViewDto getConfigurationRecordView(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    List<SchemaInfoDto> getVacantConfigurationSchemaInfosByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    ConfigurationDto editConfiguration(ConfigurationDto configuration) throws KaaAdminServiceException;

    ConfigurationRecordFormDto editConfigurationRecordForm(ConfigurationRecordFormDto configuration) throws KaaAdminServiceException;

    ConfigurationDto activateConfiguration(String configurationId) throws KaaAdminServiceException;

    ConfigurationRecordFormDto activateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;

    ConfigurationDto deactivateConfiguration(String configurationId) throws KaaAdminServiceException;

    ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;

    void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    List<TopicDto> getTopicsByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    TopicDto getTopic(String topicId) throws KaaAdminServiceException;

    TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException;

    void deleteTopic(String topicId) throws KaaAdminServiceException;

    void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

    void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException;

    NotificationDto sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException;

    EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, byte[] body) throws KaaAdminServiceException;

    List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException;

    EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, RecordField notificationData) throws KaaAdminServiceException;

    EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws KaaAdminServiceException;

    EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException;

    void addEventClassFamilySchemaForm(String eventClassFamilyId, RecordField schemaForm) throws KaaAdminServiceException;

    void addEventClassFamilySchema(String eventClassFamilyId, byte[] schema) throws KaaAdminServiceException;

    List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type) throws KaaAdminServiceException;

    List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws KaaAdminServiceException;

    ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws KaaAdminServiceException;

    ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap) throws KaaAdminServiceException;

    List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    void deleteLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editLogAppenderForm(LogAppenderDto appender) throws KaaAdminServiceException;

    List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    LogAppenderDto getRestLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editRestLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    List<PluginInfoDto> getLogAppenderPluginInfos() throws KaaAdminServiceException;

    List<VersionDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException;

    String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

    String getRecordDataByApplicationIdAndSchemaVersion(String applicationId, int schemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

    List<UserVerifierDto> getUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException;

    UserVerifierDto getUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    void deleteUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto getUserVerifierForm(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto editUserVerifierForm(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    List<UserVerifierDto> getRestUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException;

    UserVerifierDto getRestUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    List<PluginInfoDto> getUserVerifierPluginInfos() throws KaaAdminServiceException;

    void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException;

    List<SchemaInfoDto> getUserConfigurationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration, String applicationId, RecordField configurationData) throws KaaAdminServiceException;

    CTLSchemaDto saveCTLSchema(String body, String tenantId, String applicationId) throws KaaAdminServiceException;
    
    CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws KaaAdminServiceException;

    void deleteCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    void deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    boolean checkFqnExists(String fqn, String tenantId, String applicationId) throws KaaAdminServiceException;
    
    CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo) throws KaaAdminServiceException;
    
    List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() throws KaaAdminServiceException;
    
    List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() throws KaaAdminServiceException;
    
    List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemas(String applicationId) throws KaaAdminServiceException;
    
    List<CtlSchemaReferenceDto> getAvailableApplicationCTLSchemaReferences(String applicationId) throws KaaAdminServiceException;
    
    CtlSchemaFormDto getLatestCTLSchemaForm(String metaInfoId) throws KaaAdminServiceException;
    
    CtlSchemaFormDto getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version) throws KaaAdminServiceException;
    
    CtlSchemaFormDto createNewCTLSchemaFormInstance(String metaInfoId, 
            Integer sourceVersion, String applicationId) throws KaaAdminServiceException;
    
    RecordField generateCtlSchemaForm(String fileItemName, String applicationId) throws KaaAdminServiceException;
    
    CtlSchemaFormDto saveCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;
    
    boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;
    
    FileData exportCTLSchema(String fqn, int version, String applicationId, CTLSchemaExportMethod method) throws KaaAdminServiceException;
    
    String prepareCTLSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method) throws KaaAdminServiceException;
    
    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;
    
    List<SchemaInfoDto> getServerProfileSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;
     
    List<SchemaInfoDto> getServerProfileSchemaInfosByEndpointKey(String endpointKeyHash) throws KaaAdminServiceException;
    
    ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws KaaAdminServiceException;
    
    ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws KaaAdminServiceException;
    
    ServerProfileSchemaViewDto getServerProfileSchemaView(String serverProfileSchemaId) throws KaaAdminServiceException;

    ServerProfileSchemaViewDto saveServerProfileSchemaView(ServerProfileSchemaViewDto serverProfileSchema) throws KaaAdminServiceException;
    
    ServerProfileSchemaViewDto createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    SchemaInfoDto getEndpointProfileSchemaInfo(String endpointProfileSchemaId) throws KaaAdminServiceException;
    
    SchemaInfoDto getServerProfileSchemaInfo(String serverProfileSchemaId) throws KaaAdminServiceException;
    
    boolean testProfileFilter(RecordField endpointProfile, RecordField serverProfile, String filterBody) throws KaaAdminServiceException;
    
    List<EndpointProfileDto> getEndpointProfilesByUserExternalId(String endpointUserExternalId) throws KaaAdminServiceException;

    CredentialsDto provisionCredentials(String applicationId, String credentialsBody) throws KaaAdminServiceException;

    void revokeCredentials(String applicationId, String credentialsId) throws KaaAdminServiceException;

    void provisionRegistration(String applicationId, String credentialsId, Integer serverProfileVersion, String serverProfileBody) throws KaaAdminServiceException;

    void onCredentialsRevoked(String applicationId, String credentialsId) throws KaaAdminServiceException;

    List<String> getCredentialsServiceNames() throws KaaAdminServiceException;
}
