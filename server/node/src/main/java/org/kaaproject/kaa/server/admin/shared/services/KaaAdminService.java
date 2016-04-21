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

    public RecordField generateRecordFromSchemaJson(String avroSchema) throws KaaAdminServiceException;

    public EndpointProfileViewDto getEndpointProfileViewByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException;

    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException;

    public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException;

    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException;

    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointKeyHash) throws KaaAdminServiceException;
    
    public EndpointProfileDto updateServerProfile(String endpointKeyHash, int serverProfileVersion, String serverProfileBody) throws KaaAdminServiceException;

    public EndpointProfileDto updateServerProfile(String endpointKeyHash, int serverProfileVersion, RecordField serverProfileRecord) throws KaaAdminServiceException;

    public List<TenantUserDto> getTenants() throws KaaAdminServiceException;

    public TenantUserDto getTenant(String userId) throws KaaAdminServiceException;

    public TenantUserDto editTenant(TenantUserDto tenantUser) throws KaaAdminServiceException;

    public void deleteTenant(String userId) throws KaaAdminServiceException;

    public List<ApplicationDto> getApplications() throws KaaAdminServiceException;

    public ApplicationDto getApplication(String applicationId) throws KaaAdminServiceException;

    public ApplicationDto getApplicationByApplicationToken(String applicationToken) throws KaaAdminServiceException;

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

    public SchemaVersions getSchemaVersionsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException;

    public void deleteSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    public SdkProfileDto getSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    public List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<SdkProfileDto> getSdkProfilesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public SdkProfileViewDto getSdkProfileView(String sdkProfileId) throws KaaAdminServiceException;

    public String generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    public FileData getSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    public void flushSdkCache() throws KaaAdminServiceException;

    public RecordField createSimpleEmptySchemaForm() throws KaaAdminServiceException;

    public RecordField createCommonEmptySchemaForm() throws KaaAdminServiceException;

    public RecordField createConfigurationEmptySchemaForm() throws KaaAdminServiceException;

    public RecordField createEcfEmptySchemaForm() throws KaaAdminServiceException;

    public RecordField generateSimpleSchemaForm(String fileItemName) throws KaaAdminServiceException;

    public RecordField generateCommonSchemaForm(String fileItemName) throws KaaAdminServiceException;

    public RecordField generateConfigurationSchemaForm(String fileItemName) throws KaaAdminServiceException;

    public RecordField generateEcfSchemaForm(String fileItemName) throws KaaAdminServiceException;

    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException;

    public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException;

    public ProfileSchemaViewDto getProfileSchemaView(String profileSchemaId) throws KaaAdminServiceException;

    public ProfileSchemaViewDto saveProfileSchemaView(ProfileSchemaViewDto profileSchema) throws KaaAdminServiceException;

    public ProfileSchemaViewDto createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws KaaAdminServiceException;

    public ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema, byte[] schema) throws KaaAdminServiceException;

    public ConfigurationSchemaDto getConfigurationSchemaForm(String configurationSchemaId) throws KaaAdminServiceException;

    public ConfigurationSchemaDto editConfigurationSchemaForm(ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException;

    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<NotificationSchemaDto> getNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public List<VersionDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<VersionDto> getUserNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException;

    public NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema, byte[] schema) throws KaaAdminServiceException;

    public NotificationSchemaDto getNotificationSchemaForm(String notificationSchemaId) throws KaaAdminServiceException;

    public NotificationSchemaDto editNotificationSchemaForm(NotificationSchemaDto notificationSchema) throws KaaAdminServiceException;

    public List<LogSchemaDto> getLogSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<LogSchemaDto> getLogSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public LogSchemaDto getLogSchema(String logSchemaId) throws KaaAdminServiceException;

    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(String applicationToken, int version) throws KaaAdminServiceException;

    public LogSchemaDto editLogSchema(LogSchemaDto profileSchema, byte[] schema) throws KaaAdminServiceException;

    public LogSchemaDto getLogSchemaForm(String logSchemaId) throws KaaAdminServiceException;

    public LogSchemaDto editLogSchemaForm(LogSchemaDto logSchema) throws KaaAdminServiceException;

    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<EndpointGroupDto> getEndpointGroupsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws KaaAdminServiceException;

    public void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    public List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws KaaAdminServiceException;

    public ProfileFilterDto activateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    public ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public ConfigurationRecordViewDto getConfigurationRecordView(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public List<SchemaInfoDto> getVacantConfigurationSchemaInfosByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws KaaAdminServiceException;

    public ConfigurationRecordFormDto editConfigurationRecordForm(ConfigurationRecordFormDto configuration) throws KaaAdminServiceException;

    public ConfigurationDto activateConfiguration(String configurationId) throws KaaAdminServiceException;

    public ConfigurationRecordFormDto activateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;

    public ConfigurationDto deactivateConfiguration(String configurationId) throws KaaAdminServiceException;

    public ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;

    public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    public List<TopicDto> getTopicsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<TopicDto> getTopicsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    public TopicDto getTopic(String topicId) throws KaaAdminServiceException;

    public TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException;

    public void deleteTopic(String topicId) throws KaaAdminServiceException;

    public void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    public RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

    public void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException;

    public NotificationDto sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException;

    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, byte[] body) throws KaaAdminServiceException;

    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, RecordField notificationData) throws KaaAdminServiceException;

    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException;

    public EventClassFamilyDto getEventClassFamily(String eventClassFamilyId) throws KaaAdminServiceException;

    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException;

    public void addEventClassFamilySchemaForm(String eventClassFamilyId, RecordField schemaForm) throws KaaAdminServiceException;

    public void addEventClassFamilySchema(String eventClassFamilyId, byte[] schema) throws KaaAdminServiceException;

    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type) throws KaaAdminServiceException;

    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String applicationEventFamilyMapId) throws KaaAdminServiceException;

    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap) throws KaaAdminServiceException;

    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public List<LogAppenderDto> getLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    public LogAppenderDto getLogAppender(String appenderId) throws KaaAdminServiceException;

    public LogAppenderDto editLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    public void deleteLogAppender(String appenderId) throws KaaAdminServiceException;

    public LogAppenderDto getLogAppenderForm(String appenderId) throws KaaAdminServiceException;

    public LogAppenderDto editLogAppenderForm(LogAppenderDto appender) throws KaaAdminServiceException;

    public List<LogAppenderDto> getRestLogAppendersByApplicationId(String appId) throws KaaAdminServiceException;

    public List<LogAppenderDto> getRestLogAppendersByApplicationToken(String appToken) throws KaaAdminServiceException;

    public LogAppenderDto getRestLogAppender(String appenderId) throws KaaAdminServiceException;

    public LogAppenderDto editRestLogAppender(LogAppenderDto appender) throws KaaAdminServiceException;

    public List<PluginInfoDto> getLogAppenderPluginInfos() throws KaaAdminServiceException;

    public List<VersionDto> getLogSchemasVersions(String applicationId) throws KaaAdminServiceException;

    public String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

    public String getRecordDataByApplicationIdAndSchemaVersion(String applicationId, int schemaVersion, RecordKey.RecordFiles file) throws KaaAdminServiceException;

    public List<UserVerifierDto> getUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException;

    public UserVerifierDto getUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    public UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    public void deleteUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    public UserVerifierDto getUserVerifierForm(String userVerifierId) throws KaaAdminServiceException;

    public UserVerifierDto editUserVerifierForm(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    public List<UserVerifierDto> getRestUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException;

    public List<UserVerifierDto> getRestUserVerifiersByApplicationToken(String appToken) throws KaaAdminServiceException;

    public UserVerifierDto getRestUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    public UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    public List<PluginInfoDto> getUserVerifierPluginInfos() throws KaaAdminServiceException;

    public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException;

    public List<SchemaInfoDto> getUserConfigurationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration, String applicationId, RecordField configurationData) throws KaaAdminServiceException;

    public CTLSchemaDto saveCTLSchema(String body, String tenantId, String applicationId) throws KaaAdminServiceException;

    public CTLSchemaDto saveCTLSchemaWithAppToken(String body, String tenantId, String applicationToken) throws KaaAdminServiceException;

    public CTLSchemaDto saveCTLSchema(CTLSchemaDto schema) throws KaaAdminServiceException;

    public void deleteCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) throws KaaAdminServiceException;

    public CTLSchemaDto getCTLSchemaById(String schemaId) throws KaaAdminServiceException;

    public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId) throws KaaAdminServiceException;

    public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) throws KaaAdminServiceException;

    public boolean checkFqnExists(String fqn, String tenantId, String applicationId) throws KaaAdminServiceException;

    public boolean checkFqnExistsWithAppToken(String fqn, String tenantId, String applicationToken) throws KaaAdminServiceException;

    public CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo) throws KaaAdminServiceException;

    public List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() throws KaaAdminServiceException;

    public List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() throws KaaAdminServiceException;

    public List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemas(String applicationId) throws KaaAdminServiceException;

    public List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemasByAppToken(String applicationToken) throws KaaAdminServiceException;

    public List<CtlSchemaReferenceDto> getAvailableApplicationCTLSchemaReferences(String applicationId) throws KaaAdminServiceException;

    public CtlSchemaFormDto getLatestCTLSchemaForm(String metaInfoId) throws KaaAdminServiceException;

    public CtlSchemaFormDto getCTLSchemaFormByMetaInfoIdAndVer(String metaInfoId, int version) throws KaaAdminServiceException;

    public CtlSchemaFormDto createNewCTLSchemaFormInstance(String metaInfoId, 
            Integer sourceVersion, String applicationId) throws KaaAdminServiceException;

    public RecordField generateCtlSchemaForm(String fileItemName, String applicationId) throws KaaAdminServiceException;

    public CtlSchemaFormDto saveCTLSchemaForm(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    public boolean checkFqnExists(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    public FileData exportCTLSchema(String fqn, int version, String applicationId, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    public FileData exportCTLSchemaByAppToken(String fqn, int version, String applicationToken, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    public String prepareCTLSchemaExport(String ctlSchemaId, CTLSchemaExportMethod method) throws KaaAdminServiceException;

    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    public List<SchemaInfoDto> getServerProfileSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    public List<SchemaInfoDto> getServerProfileSchemaInfosByEndpointKey(String endpointKeyHash) throws KaaAdminServiceException;

    public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws KaaAdminServiceException;

    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws KaaAdminServiceException;

    public ServerProfileSchemaViewDto getServerProfileSchemaView(String serverProfileSchemaId) throws KaaAdminServiceException;

    public ServerProfileSchemaViewDto saveServerProfileSchemaView(ServerProfileSchemaViewDto serverProfileSchema) throws KaaAdminServiceException;

    public ServerProfileSchemaViewDto createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    public SchemaInfoDto getEndpointProfileSchemaInfo(String endpointProfileSchemaId) throws KaaAdminServiceException;
    
    public SchemaInfoDto getServerProfileSchemaInfo(String serverProfileSchemaId) throws KaaAdminServiceException;

    public boolean testProfileFilter(RecordField endpointProfile, RecordField serverProfile, String filterBody) throws KaaAdminServiceException;

    List<EndpointProfileDto> getEndpointProfilesByUserExternalId(String endpointUserExternalId) throws KaaAdminServiceException;

}
