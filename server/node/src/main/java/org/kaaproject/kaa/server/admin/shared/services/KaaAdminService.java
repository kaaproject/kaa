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
import org.kaaproject.kaa.common.dto.admin.*;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;
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
import org.kaaproject.kaa.server.admin.shared.schema.*;

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

    void removeEndpointProfileByKeyHash(String endpointKeyHash) throws KaaAdminServiceException;;

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

    void editUserProfile(UserProfileUpdateDto userProfileUpdateDto) throws KaaAdminServiceException;

    PropertiesDto getMailProperties() throws KaaAdminServiceException;

    PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto) throws KaaAdminServiceException;

    PropertiesDto getGeneralProperties() throws KaaAdminServiceException;

    PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto) throws KaaAdminServiceException;

    List<UserDto> getUsers() throws KaaAdminServiceException;

    UserDto getUser(String userId) throws KaaAdminServiceException;

    UserDto editUser(UserDto user) throws KaaAdminServiceException;

    void deleteUser(String userId) throws KaaAdminServiceException;

    SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws KaaAdminServiceException;

    SchemaVersions getSchemaVersionsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException;

    void deleteSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    SdkProfileDto getSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<SdkProfileDto> getSdkProfilesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    SdkProfileViewDto getSdkProfileView(String sdkProfileId) throws KaaAdminServiceException;

    String generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    FileData getSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    void flushSdkCache() throws KaaAdminServiceException;

    RecordField createSimpleEmptySchemaForm() throws KaaAdminServiceException;

    RecordField createCommonEmptySchemaForm() throws KaaAdminServiceException;

    ConfigurationSchemaViewDto createConfigurationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    RecordField createEcfEmptySchemaForm() throws KaaAdminServiceException;

    RecordField generateSimpleSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateCommonSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateConfigurationSchemaForm(String fileItemName) throws KaaAdminServiceException;

    RecordField generateEcfSchemaForm(String fileItemName) throws KaaAdminServiceException;

    List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<EndpointProfileSchemaDto> getProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException;

    EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException;

    ProfileSchemaViewDto getProfileSchemaView(String profileSchemaId) throws KaaAdminServiceException;

    ProfileSchemaViewDto saveProfileSchemaView(ProfileSchemaViewDto profileSchema) throws KaaAdminServiceException;

    ProfileSchemaViewDto createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws KaaAdminServiceException;

    ConfigurationSchemaDto saveConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException;

    ConfigurationSchemaViewDto getConfigurationSchemaView(String configurationSchemaId) throws KaaAdminServiceException;

    ConfigurationSchemaViewDto saveConfigurationSchemaView(ConfigurationSchemaViewDto confSchemaView) throws KaaAdminServiceException;

    List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<NotificationSchemaDto> getNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<VersionDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<VersionDto> getUserNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException;

    NotificationSchemaViewDto getNotificationSchemaView(String notificationSchemaId) throws KaaAdminServiceException;

    NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema) throws KaaAdminServiceException;

    NotificationSchemaViewDto saveNotificationSchemaView(NotificationSchemaViewDto notificationSchema) throws KaaAdminServiceException;

    NotificationSchemaViewDto createNotificationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<LogSchemaDto> getLogSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException;

    LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException;

    LogSchemaDto saveLogSchema(LogSchemaDto profileSchema) throws KaaAdminServiceException;

    LogSchemaViewDto getLogSchemaView(String logSchemaId) throws KaaAdminServiceException;

    LogSchemaViewDto saveLogSchemaView(LogSchemaViewDto logSchema) throws KaaAdminServiceException;

    LogSchemaViewDto createLogSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    String getFlatSchemaByCtlSchemaId(String logSchemaId) throws KaaAdminServiceException;

    List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<EndpointGroupDto> getEndpointGroupsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

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

    List<TopicDto> getTopicsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    TopicDto getTopic(String topicId) throws KaaAdminServiceException;

    TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException;

    void deleteTopic(String topicId) throws KaaAdminServiceException;

    void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

    RecordField getConfigurationRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

    void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException;

    NotificationDto sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException;

    EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, byte[] body) throws KaaAdminServiceException;

    List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException;

    EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, RecordField notificationData) throws KaaAdminServiceException;

    EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws KaaAdminServiceException;

    List<EventClassFamilyVersionDto> getEventClassFamilyVersions(String eventClassFamilyId) throws KaaAdminServiceException;

    EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException;

    CtlSchemaReferenceDto getLastCtlSchemaReferenceDto(String ctlSchemaId) throws KaaAdminServiceException;

    EventClassViewDto getEventClassView(String eventClassId) throws KaaAdminServiceException;

    EventClassViewDto getEventClassViewByCtlSchemaId(EventClassDto eventClassDto) throws KaaAdminServiceException;

    EventClassDto getEventClass(String eventClassId) throws KaaAdminServiceException;

    EventClassViewDto saveEventClassView(EventClassViewDto eventClassViewDto) throws KaaAdminServiceException;

    EventClassViewDto createEventClassFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    List<String> getEventClassTypes() throws KaaAdminServiceException;

    void addEventClassFamilyVersion(String eventClassFamilyId, EventClassFamilyVersionDto eventClassFamilyVersion) throws KaaAdminServiceException;

    void saveEventClassFamilyVersion(String eventClassFamilyId, List<EventClassViewDto> eventClassViewDto) throws KaaAdminServiceException;

    List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, Integer version, EventClassType type) throws KaaAdminServiceException;

    List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws KaaAdminServiceException;

    ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap) throws KaaAdminServiceException;

    List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    void deleteLogAppender(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException;

    LogAppenderDto editLogAppenderForm(LogAppenderDto appender) throws KaaAdminServiceException;

    List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    List<LogAppenderDto> getRestLogAppendersByApplicationToken(String appToken) throws KaaAdminServiceException;

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

    List<UserVerifierDto> getRestUserVerifiersByApplicationToken(String appToken) throws KaaAdminServiceException;

    UserVerifierDto getRestUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    List<PluginInfoDto> getUserVerifierPluginInfos() throws KaaAdminServiceException;

    void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException;

    List<SchemaInfoDto> getUserConfigurationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration, String applicationId, RecordField configurationData) throws KaaAdminServiceException;

    CTLSchemaDto saveCTLSchema(String body, String tenantId, String applicationId) throws KaaAdminServiceException;

    CTLSchemaDto saveCTLSchemaWithAppToken(String body, String tenantId, String applicationToken) throws KaaAdminServiceException;

    CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws KaaAdminServiceException;

    void deleteCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    void deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    void deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) throws KaaAdminServiceException;

    boolean checkFqnExists(String fqn, String tenantId, String applicationId) throws KaaAdminServiceException;

    boolean checkFqnExistsWithAppToken(String fqn, String tenantId, String applicationToken) throws KaaAdminServiceException;

    CTLSchemaMetaInfoDto promoteScopeToTenant(String applicationToken, String fqn) throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() throws KaaAdminServiceException;

    CTLSchemaMetaInfoDto getLastCreatedCTLSchemaByFqnAndVersion(String fqn, Integer version) throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemas(String applicationId) throws KaaAdminServiceException;

    List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemasByAppToken(String applicationToken) throws KaaAdminServiceException;

    List<CtlSchemaReferenceDto> getAvailableApplicationCTLSchemaReferences(String applicationId) throws KaaAdminServiceException;

    CtlSchemaFormDto getLatestCTLSchemaForm(String metaInfoId) throws KaaAdminServiceException;

    CtlSchemaFormDto getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version) throws KaaAdminServiceException;

    CtlSchemaFormDto createNewCTLSchemaFormInstance(String metaInfoId, Integer sourceVersion, String applicationId, ConverterType converterType) throws KaaAdminServiceException;

    RecordField generateCtlSchemaForm(String fileItemName, String applicationId) throws KaaAdminServiceException;

    CtlSchemaFormDto saveCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm, ConverterType converterType) throws KaaAdminServiceException;

    boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    FileData exportCTLSchema(String fqn, int version, String applicationId, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    FileData exportCTLSchemaByAppToken(String fqn, int version, String applicationToken, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    String prepareCTLSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

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

    CredentialsDto provisionCredentials(String applicationToken, String credentialsBody) throws KaaAdminServiceException;

    void revokeCredentials(String applicationToken, String credentialsId) throws KaaAdminServiceException;

    void provisionRegistration(String applicationToken, String credentialsId, Integer serverProfileVersion, String serverProfileBody) throws KaaAdminServiceException;

    void onCredentialsRevoked(String applicationToken, String credentialsId) throws KaaAdminServiceException;

    List<String> getCredentialsServiceNames() throws KaaAdminServiceException;
}
