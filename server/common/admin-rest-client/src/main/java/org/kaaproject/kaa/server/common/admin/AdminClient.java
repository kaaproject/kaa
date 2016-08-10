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

package org.kaaproject.kaa.server.common.admin;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
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
import org.kaaproject.kaa.server.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminClient {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClient.class);

    private KaaRestTemplate restTemplate;

    public AdminClient(String host, int port) {
        restTemplate = new KaaRestTemplate(host, port);
    }

    public AdminClient(String hostPortList) {
        restTemplate = new KaaRestTemplate(hostPortList);
    }

    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(PageLinkDto pageLink) throws Exception {
        String endpointGroupId = pageLink.getEndpointGroupId();
        String limit = pageLink.getLimit();
        String offset = pageLink.getOffset();
        ParameterizedTypeReference<EndpointProfilesPageDto> typeRef = new ParameterizedTypeReference<EndpointProfilesPageDto>() {
        };
        ResponseEntity<EndpointProfilesPageDto> entity = restTemplate.exchange(restTemplate.getUrl() + "endpointProfileByGroupId?endpointGroupId=" + endpointGroupId
                        + "&limit=" + limit + "&offset=" + offset,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLink) throws Exception {
        String endpointGroupId = pageLink.getEndpointGroupId();
        String limit = pageLink.getLimit();
        String offset = pageLink.getOffset();
        ParameterizedTypeReference<EndpointProfilesBodyDto> typeRef = new ParameterizedTypeReference<EndpointProfilesBodyDto>() {
        };
        ResponseEntity<EndpointProfilesBodyDto> entity = restTemplate.exchange(restTemplate.getUrl() + "endpointProfileBodyByGroupId?endpointGroupId=" + endpointGroupId
                        + "&limit=" + limit + "&offset=" + offset,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws Exception {
        ParameterizedTypeReference<EndpointProfileDto> typeRef = new ParameterizedTypeReference<EndpointProfileDto>() {
        };
        ResponseEntity<EndpointProfileDto> entity = restTemplate.exchange(restTemplate.getUrl() + "endpointProfile/" + toUrlSafe(endpointProfileKeyHash),
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    private static String toUrlSafe(String endpointProfileKeyHash) {
        return Base64.encodeBase64URLSafeString(Base64.decodeBase64(endpointProfileKeyHash));
    }

    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash) throws Exception {
        ParameterizedTypeReference<EndpointProfileBodyDto> typeRef = new ParameterizedTypeReference<EndpointProfileBodyDto>() {
        };
        ResponseEntity<EndpointProfileBodyDto> entity = restTemplate.exchange(restTemplate.getUrl() + "endpointProfileBody/" + toUrlSafe(endpointProfileKeyHash),
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EndpointProfileDto updateServerProfile(String endpointProfileKey, int version, String serverProfileBody) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointProfileKey", endpointProfileKey);
        params.add("version", version);
        params.add("serverProfileBody", serverProfileBody);
        return restTemplate.postForObject(restTemplate.getUrl() + "updateServerProfile", params, EndpointProfileDto.class);
    }

    public AuthResultDto checkAuth() throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "auth/checkAuth", AuthResultDto.class);
    }

    public void createKaaAdmin(String username, String password) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("password", password);
        restTemplate.postForObject(restTemplate.getUrl() + "auth/createKaaAdmin", params, Void.class);
    }

    public void login(String username, String password) {
        restTemplate.login(username, password);
    }

    public void clearCredentials() {
        HttpComponentsRequestFactoryBasicAuth requestFactory = (HttpComponentsRequestFactoryBasicAuth) restTemplate.getRequestFactory();
        requestFactory.getCredentialsProvider().clear();
    }

    public ResultCode changePassword(String username, String oldPassword, String newPassword) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("oldPassword", oldPassword);
        params.add("newPassword", newPassword);
        return restTemplate.postForObject(restTemplate.getUrl() + "auth/changePassword", params, ResultCode.class);
    }

    public TenantDto editTenant(TenantDto tenant) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "tenant", tenant, TenantDto.class);
    }

    public List<TenantDto> getTenants() throws Exception {
        ParameterizedTypeReference<List<TenantDto>> typeRef = new ParameterizedTypeReference<List<TenantDto>>() {
        };
        ResponseEntity<List<TenantDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "tenants", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }


    public List<UserDto> getAllTenantAdminsBytenantId(String tenantId){
        ParameterizedTypeReference<List<UserDto>> typeRef = new ParameterizedTypeReference<List<UserDto>>() {
        };
        ResponseEntity<List<UserDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "admins/" + tenantId, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public TenantDto getTenant(String userId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "tenant/" + userId, TenantDto.class);
    }

    public ApplicationDto editApplication(ApplicationDto application) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "application", application, ApplicationDto.class);
    }

    public List<ApplicationDto> getApplications() throws Exception {
        ParameterizedTypeReference<List<ApplicationDto>> typeRef = new ParameterizedTypeReference<List<ApplicationDto>>() {
        };
        ResponseEntity<List<ApplicationDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "applications", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public ApplicationDto getApplicationByApplicationToken(String token) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "application/" + token, ApplicationDto.class);
    }


    public ConfigurationSchemaDto saveConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "saveConfigurationSchema", configurationSchema, ConfigurationSchemaDto.class);
    }

    public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "saveProfileSchema", profileSchema, EndpointProfileSchemaDto.class);
    }

    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "saveServerProfileSchema", serverProfileSchema, ServerProfileSchemaDto.class);
    }

    public NotificationSchemaDto createNotificationSchema(NotificationSchemaDto notificationSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "createNotificationSchema", notificationSchema, NotificationSchemaDto.class);
    }

    public NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "saveNotificationSchema", notificationSchema, NotificationSchemaDto.class);
    }

    public LogSchemaDto createLogSchema(LogSchemaDto logSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "createLogSchema", logSchema, LogSchemaDto.class);
    }

    public LogSchemaDto saveLogSchema(LogSchemaDto logSchema) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "saveLogSchema", logSchema, LogSchemaDto.class);
    }

    public String getFlatSchemaByCtlSchemaId(String id) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "CTL/getFlatSchemaByCtlSchemaId?id={id}", String.class, id);
    }

    public TopicDto createTopic(TopicDto topic) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "topic", topic, TopicDto.class);
    }

    public TopicDto getTopic(String topicId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "topic/" + topicId, TopicDto.class);
    }

    public List<TopicDto> getTopicsByApplicationToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "topics/" + applicationToken, HttpMethod.GET, null,
                typeRef);
        return entity.getBody();
    }

    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "topics?endpointGroupId={endpointGroupId}", HttpMethod.GET,
                null, typeRef, endpointGroupId);
        return entity.getBody();
    }

    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "vacantTopics/" + endpointGroupId, HttpMethod.GET, null,
                typeRef);
        return entity.getBody();
    }

    public void addTopicToEndpointGroup(EndpointGroupDto endpointGroup, TopicDto topic) throws Exception {
        addTopicToEndpointGroup(endpointGroup.getId(), topic.getId());
    }

    public void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointGroupId", endpointGroupId);
        params.add("topicId", topicId);
        restTemplate.postForObject(restTemplate.getUrl() + "addTopicToEpGroup", params, Void.class);
    }

    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointGroupId", endpointGroupId);
        params.add("topicId", topicId);
        restTemplate.postForObject(restTemplate.getUrl() + "removeTopicFromEpGroup", params, Void.class);
    }

    public void removeEndpointProfileByKeyHash(String endpointProfileKeyHash) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointProfileKeyHash", endpointProfileKeyHash);
        restTemplate.postForObject(restTemplate.getUrl() + "removeEndpointProfileByKeyHash", params, Void.class);
    }

    public NotificationDto sendNotification(NotificationDto notification, String notificationResource) throws Exception {
        return sendNotification(notification, getFileResource(notificationResource));
    }

    public NotificationDto sendNotification(NotificationDto notification, String notificationResourceName, String notificationResourceBody)
            throws Exception {
        return sendNotification(notification, getStringResource(notificationResourceName, notificationResourceBody));
    }

    private NotificationDto sendNotification(NotificationDto notification, ByteArrayResource resource) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("notification", notification);
        params.add("file", resource);
        return restTemplate.postForObject(restTemplate.getUrl() + "sendNotification", params, NotificationDto.class);
    }

    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, String notificationResource)
            throws Exception {
        return sendUnicastNotification(notification, clientKeyHash, getFileResource(notificationResource));
    }

    public EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash,
                                                           String notificationResourceName, String notificationResourceBody) throws Exception {
        return sendUnicastNotification(notification, clientKeyHash, getStringResource(notificationResourceName, notificationResourceBody));
    }

    private EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, ByteArrayResource resource)
            throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("notification", notification);
        params.add("endpointKeyHash", clientKeyHash);
        params.add("file", resource);
        return restTemplate.postForObject(restTemplate.getUrl() + "sendUnicastNotification", params, EndpointNotificationDto.class);
    }

    public EndpointNotificationDto sendUnicastNotificationSimplified(NotificationDto notification, String clientKeyHash,
                                                                     String notificationMessage) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("notification", notification);
        params.add("endpointKeyHash", clientKeyHash);
        params.add("file", getStringResource("notification", notificationMessage));
        return restTemplate.postForObject(restTemplate.getUrl() + "sendUnicastNotification", params, EndpointNotificationDto.class);
    }

    public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "configurationSchema/" + configurationSchemaId, ConfigurationSchemaDto.class);
    }

    public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "profileSchema/" + profileSchemaId, EndpointProfileSchemaDto.class);
    }

    public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "serverProfileSchema/" + serverProfileSchemaId, ServerProfileSchemaDto.class);
    }

    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "notificationSchema/" + notificationSchemaId, NotificationSchemaDto.class);
    }

    public LogSchemaDto getLogSchema(String logSchemaId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "logSchema/" + logSchemaId, LogSchemaDto.class);
    }

    public SchemaVersions getSchemaVersionsByApplicationToken(String applicationToken) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "schemaVersions/" + applicationToken, SchemaVersions.class);
    }

    public List<ConfigurationSchemaDto> getConfigurationSchemasByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<ConfigurationSchemaDto>> typeRef = new ParameterizedTypeReference<List<ConfigurationSchemaDto>>() {
        };
        ResponseEntity<List<ConfigurationSchemaDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "configurationSchemas/" +
                applicationToken, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<EndpointProfileSchemaDto> getProfileSchemas(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<EndpointProfileSchemaDto>> typeRef = new ParameterizedTypeReference<List<EndpointProfileSchemaDto>>() {
        };
        ResponseEntity<List<EndpointProfileSchemaDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "profileSchemas/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<ServerProfileSchemaDto> getServerProfileSchemasByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<ServerProfileSchemaDto>> typeRef = new ParameterizedTypeReference<List<ServerProfileSchemaDto>>() {
        };
        ResponseEntity<List<ServerProfileSchemaDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "serverProfileSchemas/" +
                applicationToken, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<NotificationSchemaDto> getNotificationSchemasByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<NotificationSchemaDto>> typeRef = new ParameterizedTypeReference<List<NotificationSchemaDto>>() {
        };
        ResponseEntity<List<NotificationSchemaDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "notificationSchemas/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<VersionDto> getUserNotificationSchemasByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<VersionDto>> typeRef = new ParameterizedTypeReference<List<VersionDto>>() {
        };
        ResponseEntity<List<VersionDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "userNotificationSchemas/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<LogSchemaDto> getLogSchemasByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<LogSchemaDto>> typeRef = new ParameterizedTypeReference<List<LogSchemaDto>>() {
        };
        ResponseEntity<List<LogSchemaDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "logSchemas/" + applicationToken, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public List<TopicDto> getTopics(String applicationId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "topics/" + applicationId, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public TopicDto editTopic(TopicDto topic) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "topic", topic, TopicDto.class);
    }

    public void deleteTopic(TopicDto topic) throws Exception {
        deleteTopic(topic.getId());
    }

    public void deleteTopic(String topicId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("topicId", topicId);
        restTemplate.postForLocation(restTemplate.getUrl() + "delTopic", params);
    }

    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "endpointGroup", endpointGroup, EndpointGroupDto.class);
    }

    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "endpointGroup/" + endpointGroupId, EndpointGroupDto.class);
    }

    public void deleteEndpointGroup(String endpointGroupId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointGroupId", endpointGroupId);
        restTemplate.postForLocation(restTemplate.getUrl() + "delEndpointGroup", params);
    }

    public List<EndpointGroupDto> getEndpointGroupsByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<EndpointGroupDto>> typeRef = new ParameterizedTypeReference<List<EndpointGroupDto>>() {
        };
        ResponseEntity<List<EndpointGroupDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "endpointGroups/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<VersionDto>> typeRef = new ParameterizedTypeReference<List<VersionDto>>() {
        };
        ResponseEntity<List<VersionDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "vacantConfigurationSchemas/" + endpointGroupId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("schemaId", schemaId);
        params.add("endpointGroupId", endpointGroupId);
        restTemplate.postForObject(restTemplate.getUrl() + "delConfigurationRecord", params, Void.class);
    }

    public List<ConfigurationRecordDto> getConfigurationRecords(String endpointGroupId, boolean includeDeprecated) throws Exception {
        ParameterizedTypeReference<List<ConfigurationRecordDto>> typeRef = new ParameterizedTypeReference<List<ConfigurationRecordDto>>() {
        };
        ResponseEntity<List<ConfigurationRecordDto>> entity = restTemplate.exchange(restTemplate.getUrl()
                        + "configurationRecords?endpointGroupId={endpointGroupId}&includeDeprecated={includeDeprecated}", HttpMethod.GET, null,
                typeRef, endpointGroupId, includeDeprecated);
        return entity.getBody();
    }

    public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "configuration", configuration, ConfigurationDto.class);
    }

    public ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "configurationRecord?schemaId={schemaId}&endpointGroupId={endpointGroupId}",
                ConfigurationRecordDto.class, schemaId, endpointGroupId);
    }

    public String getConfigurationRecordBody(String schemaId, String endpointGroupId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "configurationRecordBody?schemaId={schemaId}&endpointGroupId={endpointGroupId}",
                String.class, schemaId, endpointGroupId);
    }

    public ConfigurationDto activateConfiguration(String configurationId) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "activateConfiguration", configurationId, ConfigurationDto.class);
    }

    public ConfigurationDto deactivateConfiguration(String configurationId) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "deactivateConfiguration", configurationId, ConfigurationDto.class);
    }

    public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws Exception {
        restTemplate.postForLocation(restTemplate.getUrl() + "userConfiguration", endpointUserConfiguration);
    }

    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "profileFilter", profileFilter, ProfileFilterDto.class);
    }

    public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
                                                         String endpointGroupId) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(restTemplate.getUrl()).append("profileFilterRecord?endpointGroupId={endpointGroupId}");
        List<Object> urlVariables = new ArrayList<>();
        urlVariables.add(endpointGroupId);
        if (endpointProfileSchemaId != null) {
            sb.append("&endpointProfileSchemaId={endpointProfileSchemaId}");
            urlVariables.add(endpointProfileSchemaId);
        }
        if (serverProfileSchemaId != null) {
            sb.append("&serverProfileSchemaId={serverProfileSchemaId}");
            urlVariables.add(serverProfileSchemaId);
        }
        return restTemplate.getForObject(sb.toString(), ProfileFilterRecordDto.class, urlVariables.toArray());
    }

    public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<ProfileVersionPairDto>> typeRef = new ParameterizedTypeReference<List<ProfileVersionPairDto>>() {
        };
        ResponseEntity<List<ProfileVersionPairDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "vacantProfileSchemas/" + endpointGroupId, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointProfileSchemaId", endpointProfileSchemaId);
        params.add("serverProfileSchemaId", serverProfileSchemaId);
        params.add("endpointGroupId", endpointGroupId);
        restTemplate.postForObject(restTemplate.getUrl() + "delProfileFilterRecord", params, Void.class);
    }

    public List<ProfileFilterRecordDto> getProfileFilterRecords(String endpointGroupId, boolean includeDeprecated) throws Exception {
        ParameterizedTypeReference<List<ProfileFilterRecordDto>> typeRef = new ParameterizedTypeReference<List<ProfileFilterRecordDto>>() {
        };
        ResponseEntity<List<ProfileFilterRecordDto>> entity = restTemplate.exchange(restTemplate.getUrl()
                        + "profileFilterRecords?endpointGroupId={endpointGroupId}&includeDeprecated={includeDeprecated}", HttpMethod.GET, null,
                typeRef, endpointGroupId, includeDeprecated);
        return entity.getBody();
    }

    public ProfileFilterDto activateProfileFilter(String profileFilterId) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "activateProfileFilter", profileFilterId, ProfileFilterDto.class);
    }

    public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "deactivateProfileFilter", profileFilterId, ProfileFilterDto.class);
    }

    public UserDto editUser(UserDto user) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "user", user, UserDto.class);
    }

    public UserDto getUser(String userId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "user/" + userId, UserDto.class);
    }

    public void deleteUser(String userId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("userId", userId);
        restTemplate.postForLocation(restTemplate.getUrl() + "delUser", params);
    }

    public List<UserDto> getUsers() throws Exception {
        ParameterizedTypeReference<List<UserDto>> typeRef = new ParameterizedTypeReference<List<UserDto>>() {
        };
        ResponseEntity<List<UserDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "users", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public LogSchemaDto getLogSchemaByApplicationTokenAndSchemaVersion(String applicationToken, int schemaVersion) throws Exception {
        ParameterizedTypeReference<LogSchemaDto> typeRef = new ParameterizedTypeReference<LogSchemaDto>() {
        };
        ResponseEntity<LogSchemaDto> entity = restTemplate.exchange(restTemplate.getUrl() + "logSchema/" + applicationToken + "/" + schemaVersion,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "eventClassFamily", eventClassFamily, EventClassFamilyDto.class);
    }

    public EventClassFamilyDto getEventClassFamilyById(String ecfId) {
        return restTemplate.getForObject(restTemplate.getUrl() + "eventClassFamily/" + ecfId, EventClassFamilyDto.class);
    }

    public EventClassFamilyDto getEventClassFamily(String familyName) {
        ParameterizedTypeReference<List<EventClassFamilyDto>> typeRef = new ParameterizedTypeReference<List<EventClassFamilyDto>>() {
        };
        ResponseEntity<List<EventClassFamilyDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "eventClassFamilies", HttpMethod.GET, null, typeRef);
        List<EventClassFamilyDto> familyList = entity.getBody();
        for (EventClassFamilyDto family : familyList) {
            if (family.getClassName().equals(familyName)) {
                return family;
            }
        }
        throw new RuntimeException("Family with name " + familyName + " not found!");
    }

    public List<EventClassFamilyDto> getEventClassFamilies() {
        ParameterizedTypeReference<List<EventClassFamilyDto>> typeRef = new ParameterizedTypeReference<List<EventClassFamilyDto>>() {
        };
        ResponseEntity<List<EventClassFamilyDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "eventClassFamilies", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void addEventClassFamilySchema(String eventClassFamilyId, String schemaResource) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("eventClassFamilyId", eventClassFamilyId);
        params.add("file", getFileResource(schemaResource));
        restTemplate.postForLocation(restTemplate.getUrl() + "addEventClassFamilySchema", params);
    }

    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type)
            throws Exception {
        ParameterizedTypeReference<List<EventClassDto>> typeRef = new ParameterizedTypeReference<List<EventClassDto>>() {
        };
        ResponseEntity<List<EventClassDto>> entity = restTemplate.exchange(restTemplate.getUrl()
                        + "eventClasses?eventClassFamilyId={eventClassFamilyId}&version={version}&type={type}", HttpMethod.GET, null, typeRef,
                eventClassFamilyId, version, type);
        return entity.getBody();
    }

    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "applicationEventMap", applicationEventFamilyMap, ApplicationEventFamilyMapDto.class);
    }

    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String aefMapId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "applicationEventMap/" + aefMapId, ApplicationEventFamilyMapDto.class);
    }

    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<ApplicationEventFamilyMapDto>> typeRef = new ParameterizedTypeReference<List<ApplicationEventFamilyMapDto>>() {
        };
        ResponseEntity<List<ApplicationEventFamilyMapDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "applicationEventMaps/" +
                applicationToken, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<EcfInfoDto>> typeRef = new ParameterizedTypeReference<List<EcfInfoDto>>() {
        };
        ResponseEntity<List<EcfInfoDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "vacantEventClassFamilies/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<AefMapInfoDto>> typeRef = new ParameterizedTypeReference<List<AefMapInfoDto>>() {
        };
        ResponseEntity<List<AefMapInfoDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "eventClassFamilies/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public LogAppenderDto editLogAppenderDto(LogAppenderDto logAppenderDto) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "logAppender", logAppenderDto, LogAppenderDto.class);
    }

    public LogAppenderDto getLogAppender(String logAppenderId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "logAppender/" + logAppenderId, LogAppenderDto.class);
    }

    public List<LogAppenderDto> getLogAppendersByAppToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<LogAppenderDto>> typeRef = new ParameterizedTypeReference<List<LogAppenderDto>>() {
        };
        ResponseEntity<List<LogAppenderDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "logAppenders/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void deleteLogAppender(String logAppenderId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("logAppenderId", logAppenderId);
        restTemplate.postForLocation(restTemplate.getUrl() + "delLogAppender", params);
    }

    public UserVerifierDto getUserVerifier(String userVerifierId) throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "userVerifier/" + userVerifierId, UserVerifierDto.class);
    }

    public List<UserVerifierDto> getUserVerifiersByApplicationToken(String applicationToken) {
        ParameterizedTypeReference<List<UserVerifierDto>> typeRef = new ParameterizedTypeReference<List<UserVerifierDto>>() {
        };
        ResponseEntity<List<UserVerifierDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "userVerifiers/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public UserVerifierDto editUserVerifierDto(UserVerifierDto userVerifierDto) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "userVerifier", userVerifierDto, UserVerifierDto.class);
    }

    public void deleteUserVerifier(String userVerifierId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("userVerifierId", userVerifierId);
        restTemplate.postForLocation(restTemplate.getUrl() + "delUserVerifier", params);
    }

    public SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws Exception {
        return restTemplate.postForObject(restTemplate.getUrl() + "createSdkProfile", sdkProfile, SdkProfileDto.class);
    }

    public void deleteSdkProfile(SdkProfileDto sdkProfile) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("topicId", sdkProfile.getId());
        restTemplate.postForLocation(restTemplate.getUrl() + "deleteSdkProfile", params);
    }

    public SdkProfileDto getSdkProfile(String sdkProfileId) throws Exception {
        ParameterizedTypeReference<SdkProfileDto> typeRef = new ParameterizedTypeReference<SdkProfileDto>() {
        };
        ResponseEntity<SdkProfileDto> entity = restTemplate.exchange(restTemplate.getUrl() + "sdkProfile/" + sdkProfileId, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<SdkProfileDto> getSdkProfilesByApplicationToken(String applicationToken) throws Exception {
        ParameterizedTypeReference<List<SdkProfileDto>> typeRef = new ParameterizedTypeReference<List<SdkProfileDto>>() {
        };
        ResponseEntity<List<SdkProfileDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "sdkProfiles/" + applicationToken,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void downloadSdk(String sdkProfileId, SdkPlatform targetPlatform, String destination) {
        FileResponseExtractor extractor = new FileResponseExtractor(new File(destination));
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sdkProfileId", sdkProfileId);
        parameters.add("targetPlatform", targetPlatform.toString());
        RequestCallback request = new DataRequestCallback<>(parameters);
        restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
    }

    public FileData downloadSdk(String sdkProfileId, SdkPlatform targetPlatform) {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sdkProfileId", sdkProfileId);
        parameters.add("targetPlatform", targetPlatform.toString());
        RequestCallback request = new DataRequestCallback<>(parameters);
        return restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
    }

    public void downloadSdk(SdkProfileDto key, String destination) throws Exception {
        FileResponseExtractor extractor = new FileResponseExtractor(new File(destination));
        RequestCallback request = new DataRequestCallback<>(key);
        restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
        LOG.info("Downloaded sdk to file '{}'", extractor.getDestFile());
    }

    public FileData downloadLogRecordLibrary(RecordKey key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        RequestCallback request = new DataRequestCallback<>(key);
        FileData data = restTemplate.execute(restTemplate.getUrl() + "logLibrary", HttpMethod.POST, request, extractor);
        return data;
    }

    public FileData downloadLogRecordSchema(RecordKey key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        RequestCallback request = new DataRequestCallback<>(key);
        FileData data = restTemplate.execute(restTemplate.getUrl() + "logRecordSchema", HttpMethod.POST, request, extractor);
        return data;
    }

    public FileData downloadSdk(SdkProfileDto key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        RequestCallback request = new DataRequestCallback<>(key);
        FileData data = restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
        return data;
    }

    public FileData downloadCtlSchemaByAppToken(CTLSchemaDto ctlSchemaDto, CTLSchemaExportMethod method, String appToken) {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("fqn", ctlSchemaDto.getMetaInfo().getFqn());
        parameters.add("version", Integer.toString(ctlSchemaDto.getVersion()));
        if (ctlSchemaDto.getMetaInfo().getApplicationId() != null) {
            parameters.add("applicationToken", appToken);
        }
        parameters.add("method", method.name());
        RequestCallback request = new DataRequestCallback<>(parameters);
        return restTemplate.execute(restTemplate.getUrl() + "CTL/exportSchema", HttpMethod.POST, request, extractor);
    }

    public void flushSdkCache() throws Exception {
        restTemplate.postForLocation(restTemplate.getUrl() + "flushSdkCache", null);
    }

    private static final Pattern fileNamePattern = Pattern.compile("^(.+?)filename=\"(.+?)\"");

    private class DataRequestCallback<T> implements RequestCallback {

        private List<MediaType> mediaTypes = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.valueOf("application/*+json"));

        private HttpEntity<T> requestEntity;

        public DataRequestCallback(T entity) {
            requestEntity = new HttpEntity<>(entity);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
            httpRequest.getHeaders().setAccept(mediaTypes);
            T requestBody = requestEntity.getBody();
            Class<?> requestType = requestBody.getClass();
            HttpHeaders requestHeaders = requestEntity.getHeaders();
            MediaType requestContentType = requestHeaders.getContentType();

            for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
                if (messageConverter.canWrite(requestType, requestContentType)) {
                    if (!requestHeaders.isEmpty()) {
                        httpRequest.getHeaders().putAll(requestHeaders);
                    }
                    ((HttpMessageConverter<Object>) messageConverter).write(requestBody, requestContentType, httpRequest);
                    return;
                }
            }
        }
    }

    private static class FileResponseExtractor implements ResponseExtractor<Object> {

        private final File destDir;
        private File destFile;

        private FileResponseExtractor(File destDir) {
            this.destDir = destDir;
        }

        public File getDestFile() {
            return destFile;
        }

        @Override
        public Object extractData(ClientHttpResponse response) throws IOException {
            String fileName = "";
            String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
            if (StringUtils.isNotBlank(contentDisposition)) {
                Matcher m = fileNamePattern.matcher(contentDisposition);
                if (m.matches()) {
                    fileName = m.group(2);
                }
            }
            if (StringUtils.isBlank(fileName)) {
                fileName = "downloaded-" + System.currentTimeMillis();
            }
            destFile = new File(destDir, fileName);

            InputStream is = response.getBody();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(destFile));

            IOUtils.copyLarge(is, os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            return null;
        }
    }

    private static class FileDataResponseExtractor implements ResponseExtractor<FileData> {

        private FileDataResponseExtractor() {
        }

        @Override
        public FileData extractData(ClientHttpResponse response) throws IOException {
            String fileName = "";
            String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
            String contentType = response.getHeaders().getFirst("Content-Type");
            if (StringUtils.isNotBlank(contentDisposition)) {
                Matcher m = fileNamePattern.matcher(contentDisposition);
                if (m.matches()) {
                    fileName = m.group(2);
                }
            }
            if (StringUtils.isBlank(fileName)) {
                fileName = "downloaded-" + System.currentTimeMillis();
            }

            InputStream is = response.getBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = new BufferedOutputStream(baos);

            IOUtils.copyLarge(is, os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            FileData data = new FileData();
            data.setFileName(fileName);
            data.setFileData(baos.toByteArray());
            if (contentType != null) {
                data.setContentType(contentType);
            }

            return data;
        }
    }

    public static ByteArrayResource getFileResource(final String resource) throws IOException {
        byte[] data = FileUtils.readResourceBytes(resource);
        ByteArrayResource bar = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return resource;
            }
        };
        return bar;
    }

    public static ByteArrayResource getStringResource(final String resourceName, final String resourceBody) throws IOException {
        byte[] data = resourceBody.getBytes("UTF-8");
        ByteArrayResource bar = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return resourceName;
            }
        };
        return bar;
    }

    public CTLSchemaDto saveCTLSchemaWithAppToken(String body, String tenantId, String applicationToken) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("body", body);
        if (tenantId != null) {
            params.add("tenantId", tenantId);
        }
        if (applicationToken != null) {
            params.add("applicationToken", applicationToken);
        }
        return restTemplate.postForObject(restTemplate.getUrl() + "CTL/saveSchema", params, CTLSchemaDto.class);
    }

    public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn,
                                                                       Integer version,
                                                                       String tenantId,
                                                                       String applicationToken) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("fqn", fqn);
        params.add("version", version);
        if (tenantId != null) {
            params.add("tenantId", tenantId);
        }
        if (applicationToken != null) {
            params.add("applicationToken", applicationToken);
        }
        restTemplate.postForLocation(restTemplate.getUrl() + "CTL/deleteSchema", params);
    }

    public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn, Integer version, String tenantId, String applicationToken) {
        if (tenantId != null && applicationToken != null) {
            return restTemplate.getForObject(restTemplate.getUrl() + "CTL/getSchema?fqn={fqn}&version={version}&tenantId={tenantId}" +
                    "&applicationToken={applicationToken}", CTLSchemaDto.class, fqn, version, tenantId, applicationToken);
        } else if (tenantId != null) {
            return restTemplate.getForObject(restTemplate.getUrl() + "CTL/getSchema?fqn={fqn}&version={version}&tenantId={tenantId}",
                    CTLSchemaDto.class, fqn, version, tenantId);
        } else {
            return restTemplate.getForObject(restTemplate.getUrl() + "CTL/getSchema?fqn={fqn}&version={version}", CTLSchemaDto.class, fqn, version);
        }
    }

    public CTLSchemaDto getCTLSchemaById(String id) {
        return restTemplate.getForObject(restTemplate.getUrl() + "CTL/getSchemaById?id={id}", CTLSchemaDto.class, id);
    }

    public boolean checkFqnExistsWithAppToken(String fqn, String tenantId, String applicationToken) {
        if (tenantId != null && applicationToken != null) {
            return restTemplate.getForObject(restTemplate.getUrl() + "CTL/checkFqn?fqn={fqn}&tenantId={tenantId}&applicationToken={applicationToken}",
                    Boolean.class, fqn, tenantId, applicationToken);
        } else if (tenantId != null) {
            return restTemplate.getForObject(restTemplate.getUrl() + "CTL/checkFqn?fqn={fqn}&tenantId={tenantId}", Boolean.class, fqn, tenantId);
        } else {
            return restTemplate.getForObject(restTemplate.getUrl() + "CTL/checkFqn?fqn={fqn}", Boolean.class, fqn);
        }
    }

    public CTLSchemaMetaInfoDto promoteScopeToTenant(String applicationId, String fqn) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("applicationId", applicationId);
        params.add("fqn", fqn);
        return restTemplate.postForObject(restTemplate.getUrl() + "CTL/promoteScopeToTenant", params, CTLSchemaMetaInfoDto.class);
    }

    public List<CTLSchemaMetaInfoDto> getSystemLevelCTLSchemas() {
        ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>> typeRef = new ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>>() {
        };
        ResponseEntity<List<CTLSchemaMetaInfoDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "CTL/getSystemSchemas", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<CTLSchemaMetaInfoDto> getTenantLevelCTLSchemas() {
        ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>> typeRef = new ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>>() {
        };
        ResponseEntity<List<CTLSchemaMetaInfoDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "CTL/getTenantSchemas", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<CTLSchemaMetaInfoDto> getApplicationLevelCTLSchemasByAppToken(String applicationToken) {
        ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>> typeRef = new ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>>() {
        };
        ResponseEntity<List<CTLSchemaMetaInfoDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "CTL/getApplicationSchemas/" +
                applicationToken, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public UserDto getUserProfile() throws Exception {
        return restTemplate.getForObject(restTemplate.getUrl() + "userProfile", UserDto.class);
    }

    public void editUserProfile(UserProfileUpdateDto userProfileUpdateDto) {
        restTemplate.postForObject(restTemplate.getUrl() + "userProfile", userProfileUpdateDto, Void.class);
    }

    public List<EndpointProfileDto> getEndpointProfilesByUserExternalId(String endpointUserExternalId) {
        String address = restTemplate.getUrl() + "endpointProfiles?userExternalId=" + endpointUserExternalId;
        ParameterizedTypeReference<List<EndpointProfileDto>> typeRef = new ParameterizedTypeReference<List<EndpointProfileDto>>() {
        };
        ResponseEntity<List<EndpointProfileDto>> response = this.restTemplate.exchange(address, HttpMethod.GET, null, typeRef);
        return response.getBody();
    }

    public CredentialsDto provisionCredentials(String applicationToken, byte[] credentialsBody) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("applicationToken", applicationToken);
        parameters.add("credentialsBody", Base64Utils.encodeToString(credentialsBody));
        return this.restTemplate.postForObject(restTemplate.getUrl() + "provisionCredentials", parameters, CredentialsDto.class);
    }

    public void provisionRegistration(String applicationToken, String credentialsId, Integer serverProfileVersion, String serverProfileBody) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("applicationToken", applicationToken);
        parameters.add("credentialsId", credentialsId);
        parameters.add("serverProfileVersion", serverProfileVersion);
        parameters.add("serverProfileBody", serverProfileBody);
        this.restTemplate.postForLocation(restTemplate.getUrl() + "provisionRegistration", parameters);
    }

    public void revokeCredentials(String applicationToken, String credentialsId) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("applicationToken", applicationToken);
        parameters.add("credentialsId", credentialsId);
        this.restTemplate.postForLocation(restTemplate.getUrl() + "revokeCredentials", parameters);
    }

    public void onCredentialsRevoked(String applicationToken, String credentialsId) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("applicationToken", applicationToken);
        parameters.add("credentialsId", credentialsId);
        this.restTemplate.postForLocation(restTemplate.getUrl() + "notifyRevoked", parameters);
    }
}
