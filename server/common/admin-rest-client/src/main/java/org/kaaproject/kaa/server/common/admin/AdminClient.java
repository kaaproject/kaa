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
package org.kaaproject.kaa.server.common.admin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

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

    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);

    private RestTemplate restTemplate;

    private String url;

    public AdminClient(String host, int port) {
        restTemplate = new RestTemplate();
        ClientHttpRequestFactory requestFactory = new HttpComponentsRequestFactoryBasicAuth(new HttpHost(host, port, "http"));
        restTemplate.setRequestFactory(requestFactory);
        url = "http://" + host + ":" + port + "/kaaAdmin/rest/api/";
    }

    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(PageLinkDto pageLink) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("endpointGroupId", pageLink.getEndpointGroupId());
        params.add("limit", pageLink.getLimit());
        params.add("offset", pageLink.getOffset());
        ParameterizedTypeReference<EndpointProfilesPageDto> typeRef = new ParameterizedTypeReference<EndpointProfilesPageDto>() {
        };
        ResponseEntity<EndpointProfilesPageDto> entity = restTemplate.exchange(url + "endpointProfileByGroupId/" + params, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLink) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("endpointGroupId", pageLink.getEndpointGroupId());
        params.add("limit", pageLink.getLimit());
        params.add("offset", pageLink.getOffset());
        ParameterizedTypeReference<EndpointProfilesBodyDto> typeRef = new ParameterizedTypeReference<EndpointProfilesBodyDto>() {
        };
        ResponseEntity<EndpointProfilesBodyDto> entity = restTemplate.exchange(url + "endpointProfileBodyByGroupId/" + params,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws Exception {
        ParameterizedTypeReference<EndpointProfileDto> typeRef = new ParameterizedTypeReference<EndpointProfileDto>() {
        };
        ResponseEntity<EndpointProfileDto> entity = restTemplate.exchange(url + "endpointProfile/" + endpointProfileKeyHash,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash) throws Exception {
        ParameterizedTypeReference<EndpointProfileBodyDto> typeRef = new ParameterizedTypeReference<EndpointProfileBodyDto>() {
        };
        ResponseEntity<EndpointProfileBodyDto> entity = restTemplate.exchange(url + "endpointProfileBody/" + endpointProfileKeyHash,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EndpointProfileDto updateServerProfile(String endpointProfileKey, int version, String serverProfileBody) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointProfileKey", endpointProfileKey);
        params.add("version", version);
        params.add("serverProfileBody", serverProfileBody);
        return restTemplate.postForObject(url + "updateServerProfile", params, EndpointProfileDto.class);
    }

    public AuthResultDto checkAuth() throws Exception {
        return restTemplate.getForObject(url + "auth/checkAuth", AuthResultDto.class);
    }

    public void createKaaAdmin(String username, String password) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("password", password);
        restTemplate.postForObject(url + "auth/createKaaAdmin", params, Void.class);
    }

    public void login(String username, String password) {
        HttpComponentsRequestFactoryBasicAuth requestFactory = (HttpComponentsRequestFactoryBasicAuth) restTemplate.getRequestFactory();
        requestFactory.setCredentials(username, password);
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
        return restTemplate.postForObject(url + "auth/changePassword", params, ResultCode.class);
    }

    public TenantUserDto editTenant(TenantUserDto tenant) throws Exception {
        return restTemplate.postForObject(url + "tenant", tenant, TenantUserDto.class);
    }

    public List<TenantUserDto> getTenants() throws Exception {
        ParameterizedTypeReference<List<TenantUserDto>> typeRef = new ParameterizedTypeReference<List<TenantUserDto>>() {
        };
        ResponseEntity<List<TenantUserDto>> entity = restTemplate.exchange(url + "tenants", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public TenantUserDto getTenant(String userId) throws Exception {
        return restTemplate.getForObject(url + "tenant/" + userId, TenantUserDto.class);
    }

    public void deleteTenant(String userId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("userId", userId);
        restTemplate.postForLocation(url + "delTenant", params);
    }

    public ApplicationDto editApplication(ApplicationDto application) throws Exception {
        return restTemplate.postForObject(url + "application", application, ApplicationDto.class);
    }

    public List<ApplicationDto> getApplications() throws Exception {
        ParameterizedTypeReference<List<ApplicationDto>> typeRef = new ParameterizedTypeReference<List<ApplicationDto>>() {
        };
        ResponseEntity<List<ApplicationDto>> entity = restTemplate.exchange(url + "applications", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public ApplicationDto getApplication(String applicationId) throws Exception {
        return restTemplate.getForObject(url + "application/" + applicationId, ApplicationDto.class);
    }

    public ApplicationDto getApplicationByApplicationToken(String token) throws Exception {
        return restTemplate.getForObject(url + "application/token/" + token, ApplicationDto.class);
    }

    public void deleteApplication(String applicationId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("applicationId", applicationId);
        restTemplate.postForLocation(url + "delApplication", params);
    }

    public ConfigurationSchemaDto createConfigurationSchema(ConfigurationSchemaDto configurationSchema, String schemaResource)
            throws Exception {
        return createConfigurationSchema(configurationSchema, getFileResource(schemaResource));
    }

    public ConfigurationSchemaDto createConfigurationSchema(ConfigurationSchemaDto configurationSchema, ByteArrayResource schemaResource)
            throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("configurationSchema", configurationSchema);
        params.add("file", schemaResource);
        return restTemplate.postForObject(url + "createConfigurationSchema", params, ConfigurationSchemaDto.class);
    }

    public ConfigurationSchemaDto editConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws Exception {
        return restTemplate.postForObject(url + "editConfigurationSchema", configurationSchema, ConfigurationSchemaDto.class);
    }

    public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws Exception {
        return restTemplate.postForObject(url + "saveProfileSchema", profileSchema, EndpointProfileSchemaDto.class);
    }

    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws Exception {
        return restTemplate.postForObject(url + "saveServerProfileSchema", serverProfileSchema, ServerProfileSchemaDto.class);
    }

    public NotificationSchemaDto createNotificationSchema(NotificationSchemaDto notificationSchema, String schemaResource) throws Exception {
        return createNotificationSchema(notificationSchema, getFileResource(schemaResource));
    }

    public NotificationSchemaDto createNotificationSchema(NotificationSchemaDto notificationSchema, ByteArrayResource schemaResource)
            throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("notificationSchema", notificationSchema);
        params.add("file", schemaResource);
        return restTemplate.postForObject(url + "createNotificationSchema", params, NotificationSchemaDto.class);
    }

    public NotificationSchemaDto editNotificationSchema(NotificationSchemaDto notificationSchema) throws Exception {
        return restTemplate.postForObject(url + "editNotificationSchema", notificationSchema, NotificationSchemaDto.class);
    }

    public LogSchemaDto createLogSchema(LogSchemaDto logSchema, String schemaResource) throws Exception {
        return createLogSchema(logSchema, getFileResource(schemaResource));
    }

    public LogSchemaDto createLogSchema(LogSchemaDto logSchema, ByteArrayResource schemaResource) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("logSchema", logSchema);
        params.add("file", schemaResource);
        return restTemplate.postForObject(url + "createLogSchema", params, LogSchemaDto.class);
    }

    public LogSchemaDto editLogSchema(LogSchemaDto logSchema) throws Exception {
        return restTemplate.postForObject(url + "editLogSchema", logSchema, LogSchemaDto.class);
    }

    public TopicDto createTopic(TopicDto topic) throws Exception {
        return restTemplate.postForObject(url + "topic", topic, TopicDto.class);
    }

    public TopicDto getTopic(String topicId) throws Exception {
        return restTemplate.getForObject(url + "topic/" + topicId, TopicDto.class);
    }

    public List<TopicDto> getTopicsByApplicationId(String applicationId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(url + "topics/" + applicationId, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(url + "topics?endpointGroupId={endpointGroupId}", HttpMethod.GET,
                null, typeRef, endpointGroupId);
        return entity.getBody();
    }

    public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(url + "vacantTopics/" + endpointGroupId, HttpMethod.GET, null,
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
        restTemplate.postForObject(url + "addTopicToEpGroup", params, Void.class);
    }

    public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointGroupId", endpointGroupId);
        params.add("topicId", topicId);
        restTemplate.postForObject(url + "removeTopicFromEpGroup", params, Void.class);
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
        return restTemplate.postForObject(url + "sendNotification", params, NotificationDto.class);
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
        params.add("clientKeyHash", clientKeyHash);
        params.add("file", resource);
        return restTemplate.postForObject(url + "sendUnicastNotification", params, EndpointNotificationDto.class);
    }

    public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws Exception {
        return restTemplate.getForObject(url + "configurationSchema/" + configurationSchemaId, ConfigurationSchemaDto.class);
    }

    public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws Exception {
        return restTemplate.getForObject(url + "profileSchema/" + profileSchemaId, EndpointProfileSchemaDto.class);
    }

    public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws Exception {
        return restTemplate.getForObject(url + "serverProfileSchema/" + serverProfileSchemaId, ServerProfileSchemaDto.class);
    }

    public NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws Exception {
        return restTemplate.getForObject(url + "notificationSchema/" + notificationSchemaId, NotificationSchemaDto.class);
    }

    public LogSchemaDto getLogSchema(String logSchemaId) throws Exception {
        return restTemplate.getForObject(url + "logSchema/" + logSchemaId, LogSchemaDto.class);
    }

    public SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws Exception {
        return restTemplate.getForObject(url + "schemaVersions/" + applicationId, SchemaVersions.class);
    }

    public List<ConfigurationSchemaDto> getConfigurationSchemas(String applicationId) throws Exception {
        ParameterizedTypeReference<List<ConfigurationSchemaDto>> typeRef = new ParameterizedTypeReference<List<ConfigurationSchemaDto>>() {
        };
        ResponseEntity<List<ConfigurationSchemaDto>> entity = restTemplate.exchange(url + "configurationSchemas/" + applicationId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<EndpointProfileSchemaDto> getProfileSchemas(String applicationId) throws Exception {
        ParameterizedTypeReference<List<EndpointProfileSchemaDto>> typeRef = new ParameterizedTypeReference<List<EndpointProfileSchemaDto>>() {
        };
        ResponseEntity<List<EndpointProfileSchemaDto>> entity = restTemplate.exchange(url + "profileSchemas/" + applicationId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<ServerProfileSchemaDto> getServerProfileSchemas(String applicationId) throws Exception {
        ParameterizedTypeReference<List<ServerProfileSchemaDto>> typeRef = new ParameterizedTypeReference<List<ServerProfileSchemaDto>>() {
        };
        ResponseEntity<List<ServerProfileSchemaDto>> entity = restTemplate.exchange(url + "serverProfileSchemas/" + applicationId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<NotificationSchemaDto> getNotificationSchemas(String applicationId) throws Exception {
        ParameterizedTypeReference<List<NotificationSchemaDto>> typeRef = new ParameterizedTypeReference<List<NotificationSchemaDto>>() {
        };
        ResponseEntity<List<NotificationSchemaDto>> entity = restTemplate.exchange(url + "notificationSchemas/" + applicationId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<VersionDto> getUserNotificationSchemas(String applicationId) throws Exception {
        ParameterizedTypeReference<List<VersionDto>> typeRef = new ParameterizedTypeReference<List<VersionDto>>() {
        };
        ResponseEntity<List<VersionDto>> entity = restTemplate.exchange(url + "userNotificationSchemas/" + applicationId, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public List<LogSchemaDto> getLogSchemas(String applicationId) throws Exception {
        ParameterizedTypeReference<List<LogSchemaDto>> typeRef = new ParameterizedTypeReference<List<LogSchemaDto>>() {
        };
        ResponseEntity<List<LogSchemaDto>> entity = restTemplate.exchange(url + "logSchemas/" + applicationId, HttpMethod.GET, null,
                typeRef);
        return entity.getBody();
    }

    public List<TopicDto> getTopics(String applicationId) throws Exception {
        ParameterizedTypeReference<List<TopicDto>> typeRef = new ParameterizedTypeReference<List<TopicDto>>() {
        };
        ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(url + "topics/" + applicationId, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void deleteTopic(TopicDto topic) throws Exception {
        deleteTopic(topic.getId());
    }

    public void deleteTopic(String topicId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("topicId", topicId);
        restTemplate.postForLocation(url + "delTopic", params);
    }

    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws Exception {
        return restTemplate.postForObject(url + "endpointGroup", endpointGroup, EndpointGroupDto.class);
    }

    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws Exception {
        return restTemplate.getForObject(url + "endpointGroup/" + endpointGroupId, EndpointGroupDto.class);
    }

    public void deleteEndpointGroup(String endpointGroupId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointGroupId", endpointGroupId);
        restTemplate.postForLocation(url + "delEndpointGroup", params);
    }

    public List<EndpointGroupDto> getEndpointGroups(String applicationId) throws Exception {
        ParameterizedTypeReference<List<EndpointGroupDto>> typeRef = new ParameterizedTypeReference<List<EndpointGroupDto>>() {
        };
        ResponseEntity<List<EndpointGroupDto>> entity = restTemplate.exchange(url + "endpointGroups/" + applicationId, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws Exception {
        ParameterizedTypeReference<List<VersionDto>> typeRef = new ParameterizedTypeReference<List<VersionDto>>() {
        };
        ResponseEntity<List<VersionDto>> entity = restTemplate.exchange(url + "vacantConfigurationSchemas/" + endpointGroupId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("schemaId", schemaId);
        params.add("endpointGroupId", endpointGroupId);
        restTemplate.postForObject(url + "delConfigurationRecord", params, Void.class);
    }

    public List<ConfigurationRecordDto> getConfigurationRecords(String endpointGroupId, boolean includeDeprecated) throws Exception {
        ParameterizedTypeReference<List<ConfigurationRecordDto>> typeRef = new ParameterizedTypeReference<List<ConfigurationRecordDto>>() {
        };
        ResponseEntity<List<ConfigurationRecordDto>> entity = restTemplate.exchange(url
                + "configurationRecords?endpointGroupId={endpointGroupId}&includeDeprecated={includeDeprecated}", HttpMethod.GET, null,
                typeRef, endpointGroupId, includeDeprecated);
        return entity.getBody();
    }

    public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws Exception {
        return restTemplate.postForObject(url + "configuration", configuration, ConfigurationDto.class);
    }

    public ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws Exception {
        return restTemplate.getForObject(url + "configurationRecord?schemaId={schemaId}&endpointGroupId={endpointGroupId}",
                ConfigurationRecordDto.class, schemaId, endpointGroupId);
    }

    public ConfigurationDto activateConfiguration(String configurationId) throws Exception {
        return restTemplate.postForObject(url + "activateConfiguration", configurationId, ConfigurationDto.class);
    }

    public ConfigurationDto deactivateConfiguration(String configurationId) throws Exception {
        return restTemplate.postForObject(url + "deactivateConfiguration", configurationId, ConfigurationDto.class);
    }

    public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws Exception {
        restTemplate.postForLocation(url + "userConfiguration", endpointUserConfiguration);
    }

    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws Exception {
        return restTemplate.postForObject(url + "profileFilter", profileFilter, ProfileFilterDto.class);
    }

    public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
            String endpointGroupId) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("profileFilterRecord?endpointGroupId={endpointGroupId}");
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
        ResponseEntity<List<ProfileVersionPairDto>> entity = restTemplate.exchange(url + "vacantProfileSchemas/" + endpointGroupId, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("endpointProfileSchemaId", endpointProfileSchemaId);
        params.add("serverProfileSchemaId", serverProfileSchemaId);
        params.add("endpointGroupId", endpointGroupId);
        restTemplate.postForObject(url + "delProfileFilterRecord", params, Void.class);
    }

    public List<ProfileFilterRecordDto> getProfileFilterRecords(String endpointGroupId, boolean includeDeprecated) throws Exception {
        ParameterizedTypeReference<List<ProfileFilterRecordDto>> typeRef = new ParameterizedTypeReference<List<ProfileFilterRecordDto>>() {
        };
        ResponseEntity<List<ProfileFilterRecordDto>> entity = restTemplate.exchange(url
                + "profileFilterRecords?endpointGroupId={endpointGroupId}&includeDeprecated={includeDeprecated}", HttpMethod.GET, null,
                typeRef, endpointGroupId, includeDeprecated);
        return entity.getBody();
    }

    public ProfileFilterDto activateProfileFilter(String profileFilterId) throws Exception {
        return restTemplate.postForObject(url + "activateProfileFilter", profileFilterId, ProfileFilterDto.class);
    }

    public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws Exception {
        return restTemplate.postForObject(url + "deactivateProfileFilter", profileFilterId, ProfileFilterDto.class);
    }

    public UserDto editUser(UserDto user) throws Exception {
        return restTemplate.postForObject(url + "user", user, UserDto.class);
    }

    public UserDto getUser(String userId) throws Exception {
        return restTemplate.getForObject(url + "user/" + userId, UserDto.class);
    }

    public void deleteUser(String userId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("userId", userId);
        restTemplate.postForLocation(url + "delUser", params);
    }

    public List<UserDto> getUsers() throws Exception {
        ParameterizedTypeReference<List<UserDto>> typeRef = new ParameterizedTypeReference<List<UserDto>>() {
        };
        ResponseEntity<List<UserDto>> entity = restTemplate.exchange(url + "users", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public LogSchemaDto getLogSchemaByApplicationTokenAndSchemaVersion(String applicationToken, int schemaVersion) throws Exception {
        ParameterizedTypeReference<LogSchemaDto> typeRef = new ParameterizedTypeReference<LogSchemaDto>() {
        };
        ResponseEntity<LogSchemaDto> entity = restTemplate.exchange(url + "logSchema/" + applicationToken + "/" + schemaVersion,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily) throws Exception {
        return restTemplate.postForObject(url + "eventClassFamily", eventClassFamily, EventClassFamilyDto.class);
    }

    public EventClassFamilyDto getEventClassFamilyById(String ecfId) {
        return restTemplate.getForObject(url + "eventClassFamily/" + ecfId, EventClassFamilyDto.class);
    }

    public EventClassFamilyDto getEventClassFamily(String familyName) {
        ParameterizedTypeReference<List<EventClassFamilyDto>> typeRef = new ParameterizedTypeReference<List<EventClassFamilyDto>>() {
        };
        ResponseEntity<List<EventClassFamilyDto>> entity = restTemplate.exchange(url + "eventClassFamilies", HttpMethod.GET, null, typeRef);
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
        ResponseEntity<List<EventClassFamilyDto>> entity = restTemplate.exchange(url + "eventClassFamilies", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public void addEventClassFamilySchema(String eventClassFamilyId, String schemaResource) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("eventClassFamilyId", eventClassFamilyId);
        params.add("file", getFileResource(schemaResource));
        restTemplate.postForLocation(url + "addEventClassFamilySchema", params);
    }

    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId, int version, EventClassType type)
            throws Exception {
        ParameterizedTypeReference<List<EventClassDto>> typeRef = new ParameterizedTypeReference<List<EventClassDto>>() {
        };
        ResponseEntity<List<EventClassDto>> entity = restTemplate.exchange(url
                + "eventClasses?eventClassFamilyId={eventClassFamilyId}&version={version}&type={type}", HttpMethod.GET, null, typeRef,
                eventClassFamilyId, version, type);
        return entity.getBody();
    }

    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws Exception {
        return restTemplate.postForObject(url + "applicationEventMap", applicationEventFamilyMap, ApplicationEventFamilyMapDto.class);
    }

    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String aefMapId) throws Exception {
        return restTemplate.getForObject(url + "applicationEventMap/" + aefMapId, ApplicationEventFamilyMapDto.class);
    }

    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(String applicationId) throws Exception {
        ParameterizedTypeReference<List<ApplicationEventFamilyMapDto>> typeRef = new ParameterizedTypeReference<List<ApplicationEventFamilyMapDto>>() {
        };
        ResponseEntity<List<ApplicationEventFamilyMapDto>> entity = restTemplate.exchange(url + "applicationEventMaps/" + applicationId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(String applicationId) throws Exception {
        ParameterizedTypeReference<List<EcfInfoDto>> typeRef = new ParameterizedTypeReference<List<EcfInfoDto>>() {
        };
        ResponseEntity<List<EcfInfoDto>> entity = restTemplate.exchange(url + "vacantEventClassFamilies/" + applicationId, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(String applicationId) throws Exception {
        ParameterizedTypeReference<List<AefMapInfoDto>> typeRef = new ParameterizedTypeReference<List<AefMapInfoDto>>() {
        };
        ResponseEntity<List<AefMapInfoDto>> entity = restTemplate.exchange(url + "eventClassFamilies/" + applicationId, HttpMethod.GET,
                null, typeRef);
        return entity.getBody();
    }

    public LogAppenderDto editLogAppenderDto(LogAppenderDto logAppenderDto) throws Exception {
        return restTemplate.postForObject(url + "logAppender", logAppenderDto, LogAppenderDto.class);
    }

    public LogAppenderDto getLogAppender(String logAppenderId) throws Exception {
        return restTemplate.getForObject(url + "logAppender/" + logAppenderId, LogAppenderDto.class);
    }

    public List<LogAppenderDto> getLogAppenders(String applicationId) throws Exception {
        ParameterizedTypeReference<List<LogAppenderDto>> typeRef = new ParameterizedTypeReference<List<LogAppenderDto>>() {
        };
        ResponseEntity<List<LogAppenderDto>> entity = restTemplate.exchange(url + "logAppenders/" + applicationId, HttpMethod.GET, null,
                typeRef);
        return entity.getBody();
    }

    public void deleteLogAppender(String logAppenderId) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("logAppenderId", logAppenderId);
        restTemplate.postForLocation(url + "delLogAppender", params);
    }

    public UserVerifierDto editUserVerifierDto(UserVerifierDto userVerifierDto) throws Exception {
        return restTemplate.postForObject(url + "userVerifier", userVerifierDto, UserVerifierDto.class);
    }

    public SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws Exception {
        return restTemplate.postForObject(url + "addSdkProfile", sdkProfile, SdkProfileDto.class);
    }

    public void deleteSdkProfile(SdkProfileDto sdkProfile) throws Exception {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("topicId", sdkProfile.getId());
        restTemplate.postForLocation(url + "deleteSdkProfile", params);
    }

    public SdkProfileDto getSdkProfile(String sdkProfileId) throws Exception {
        ParameterizedTypeReference<SdkProfileDto> typeRef = new ParameterizedTypeReference<SdkProfileDto>() {
        };
        ResponseEntity<SdkProfileDto> entity = restTemplate.exchange(url + "sdkProfile/" + sdkProfileId, HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<SdkProfileDto> getSdkProfiles(String applicationId) throws Exception {
        ParameterizedTypeReference<List<SdkProfileDto>> typeRef = new ParameterizedTypeReference<List<SdkProfileDto>>() {
        };
        ResponseEntity<List<SdkProfileDto>> entity = restTemplate.exchange(url + "sdkProfiles/" + applicationId, HttpMethod.GET, null,
                typeRef);
        return entity.getBody();
    }

    public void downloadSdk(String sdkProfileId, SdkPlatform targetPlatform, String destination) {
        FileResponseExtractor extractor = new FileResponseExtractor(new File(destination));
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sdkProfileId", sdkProfileId);
        parameters.add("targetPlatform", targetPlatform.toString());
        RequestCallback request = new DataRequestCallback<>(parameters);
        restTemplate.execute(url + "sdk", HttpMethod.POST, request, extractor);
    }

    public FileData downloadSdk(String sdkProfileId, SdkPlatform targetPlatform) {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("sdkProfileId", sdkProfileId);
        parameters.add("targetPlatform", targetPlatform.toString());
        RequestCallback request = new DataRequestCallback<>(parameters);
        return restTemplate.execute(url + "sdk", HttpMethod.POST, request, extractor);
    }

    public void downloadSdk(SdkProfileDto key, String destination) throws Exception {
        FileResponseExtractor extractor = new FileResponseExtractor(new File(destination));
        RequestCallback request = new DataRequestCallback<>(key);
        restTemplate.execute(url + "sdk", HttpMethod.POST, request, extractor);
        logger.info("Downloaded sdk to file '{}'", extractor.getDestFile());
    }

    public FileData downloadLogRecordLibrary(RecordKey key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        RequestCallback request = new DataRequestCallback<>(key);
        FileData data = restTemplate.execute(url + "logLibrary", HttpMethod.POST, request, extractor);
        return data;
    }

    public FileData downloadLogRecordSchema(RecordKey key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        RequestCallback request = new DataRequestCallback<>(key);
        FileData data = restTemplate.execute(url + "logRecordSchema", HttpMethod.POST, request, extractor);
        return data;
    }

    public FileData downloadSdk(SdkProfileDto key) throws Exception {
        FileDataResponseExtractor extractor = new FileDataResponseExtractor();
        RequestCallback request = new DataRequestCallback<>(key);
        FileData data = restTemplate.execute(url + "sdk", HttpMethod.POST, request, extractor);
        return data;
    }

    public void flushSdkCache() throws Exception {
        restTemplate.postForLocation(url + "flushSdkCache", null);
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

    public CTLSchemaInfoDto saveCTLSchema(String body, CTLSchemaScopeDto scope, String applicationId) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("body", body);
        if (scope != null) {
            params.add("scope", scope.name());
        }
        if (applicationId != null) {
            params.add("applicationId", applicationId);
        }
        return restTemplate.postForObject(url + "CTL/saveSchema", params, CTLSchemaInfoDto.class);
    }

    public void deleteCTLSchemaByFqnAndVersion(String fqn, Integer version) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("fqn", fqn);
        params.add("version", version);
        restTemplate.postForLocation(url + "CTL/deleteSchema", params);
    }

    public CTLSchemaInfoDto getCTLSchemaByFqnAndVersion(String fqn, Integer version) {
        return restTemplate.getForObject(url + "CTL/getSchema?fqn={fqn}&version={version}", CTLSchemaInfoDto.class, fqn, version);
    }

    public List<CTLSchemaMetaInfoDto> getCTLSchemasAvailable() {
        ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>> typeRef = new ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>>() {
        };
        ResponseEntity<List<CTLSchemaMetaInfoDto>> entity = restTemplate.exchange(url + "CTL/getSchemas", HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<CTLSchemaMetaInfoDto> getCTLSchemasByScope(String scopeName) {
        ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>> typeRef = new ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>>() {
        };
        ResponseEntity<List<CTLSchemaMetaInfoDto>> entity = restTemplate.exchange(url + "CTL/getSchemas?scope=" + scopeName,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

    public List<CTLSchemaMetaInfoDto> getCTLSchemasByApplicationId(String applicationId) {
        ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>> typeRef = new ParameterizedTypeReference<List<CTLSchemaMetaInfoDto>>() {
        };
        ResponseEntity<List<CTLSchemaMetaInfoDto>> entity = restTemplate.exchange(url + "CTL/getSchemas?applicationId=" + applicationId,
                HttpMethod.GET, null, typeRef);
        return entity.getBody();
    }

}
