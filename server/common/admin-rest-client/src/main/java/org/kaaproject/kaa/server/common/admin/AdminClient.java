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


import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.valueOf;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.kaaproject.kaa.common.dto.EndpointSpecificConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaExportMethod;
import org.kaaproject.kaa.common.dto.ctl.CtlSchemaMetaInfoDto;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminClient {

  private static final Logger LOG = LoggerFactory.getLogger(AdminClient.class);
  private static final Pattern fileNamePattern = Pattern.compile("^(.+?)filename=\"(.+?)\"");
  private KaaRestTemplate restTemplate;

  public AdminClient(String host, int port) {
    restTemplate = new KaaRestTemplate(host, port);
  }

  public AdminClient(String hostPortList) {
    restTemplate = new KaaRestTemplate(hostPortList);
  }

  private static String toUrlSafe(String endpointProfileKeyHash) {
    return Base64.encodeBase64URLSafeString(Base64.decodeBase64(endpointProfileKeyHash));
  }

  /**
   * Read file from disk and return it binary format represented as ByteArrayResource.
   *
   * @param resource the name of file resource
   * @throws IOException the io exception
   */
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


  /**
   * Represented string resource as ByteArrayResource. The resource body encoded in UTF-8.
   *
   * @throws IOException the io exception
   */
  public static ByteArrayResource getStringResource(final String resourceName,
                                                    final String resourceBody) throws IOException {
    byte[] data = resourceBody.getBytes("UTF-8");
    ByteArrayResource bar = new ByteArrayResource(data) {
      @Override
      public String getFilename() {
        return resourceName;
      }
    };
    return bar;
  }


  /**
   * Gets the endpoint profile by endpoint group id.
   *
   * @param pageLink contains information about groupId, offset and limit
   */
  public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(PageLinkDto pageLink)
      throws Exception {
    String endpointGroupId = pageLink.getEndpointGroupId();
    String limit = pageLink.getLimit();
    String offset = pageLink.getOffset();
    ParameterizedTypeReference<EndpointProfilesPageDto> typeRef =
        new ParameterizedTypeReference<EndpointProfilesPageDto>() {};
    ResponseEntity<EndpointProfilesPageDto> entity = restTemplate.exchange(restTemplate.getUrl()
        + "endpointProfileByGroupId?endpointGroupId=" + endpointGroupId
        + "&limit=" + limit
        + "&offset=" + offset,
        HttpMethod.GET, null, typeRef);
    return entity.getBody();
  }


  /**
   * Gets the endpoint profile body by endpoint group id.
   *
   * @param pageLink contains information about groupId, offset and limit
   */
  public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(PageLinkDto pageLink)
      throws Exception {
    String endpointGroupId = pageLink.getEndpointGroupId();
    String limit = pageLink.getLimit();
    String offset = pageLink.getOffset();
    ResponseEntity<EndpointProfilesBodyDto> entity = restTemplate.exchange(restTemplate.getUrl()
        + "endpointProfileBodyByGroupId?endpointGroupId=" + endpointGroupId
        + "&limit=" + limit
        + "&offset=" + offset,
        HttpMethod.GET, null, new ParameterizedTypeReference<EndpointProfilesBodyDto>() {});
    return entity.getBody();
  }


  /**
   * Gets the endpoint profile by endpoint key hash.
   *
   * @param endpointProfileKeyHash the endpoint profile key hash
   */
  public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash)
      throws Exception {
    ResponseEntity<EndpointProfileDto> entity = restTemplate.exchange(restTemplate.getUrl()
        + "endpointProfile/" + toUrlSafe(endpointProfileKeyHash),
        HttpMethod.GET, null,  new ParameterizedTypeReference<EndpointProfileDto>() {});
    return entity.getBody();
  }

  /**
   * Gets the endpoint profile body by endpoint key hash.
   *
   * @param endpointProfileKeyHash the endpoint profile key hash
   * @return the endpoint profile body dto
   */

  public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash)
      throws Exception {
    ResponseEntity<EndpointProfileBodyDto> entity = restTemplate.exchange(restTemplate.getUrl()
        + "endpointProfileBody/" + toUrlSafe(endpointProfileKeyHash),
        HttpMethod.GET, null, new ParameterizedTypeReference<EndpointProfileBodyDto>() {});
    return entity.getBody();
  }


  /**
   * Update server profile of endpoint.
   *
   * @param endpointProfileKey the endpoint profile key
   * @param version            the version
   * @param serverProfileBody  the server profile body
   */
  public EndpointProfileDto updateServerProfile(String endpointProfileKey, int version,
                                                String serverProfileBody) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("endpointProfileKey", endpointProfileKey);
    params.add("version", version);
    params.add("serverProfileBody", serverProfileBody);
    return restTemplate.postForObject(restTemplate.getUrl() + "updateServerProfile",
        params, EndpointProfileDto.class);
  }

  public AuthResultDto checkAuth() throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "auth/checkAuth", AuthResultDto.class);
  }

  /**
   * Creates the kaa admin with specific name and password.
   *
   * @param username admin's name
   * @param password admin's password
   */
  public void createKaaAdmin(String username, String password) throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    params.add("username", username);
    params.add("password", password);
    restTemplate.postForObject(restTemplate.getUrl() + "auth/createKaaAdmin", params, Void.class);
  }

  public void login(String username, String password) {
    restTemplate.login(username, password);
  }

  /**
   * Clear credentials of current user.
   */
  public void clearCredentials() {
    HttpComponentsRequestFactoryBasicAuth requestFactory =
        (HttpComponentsRequestFactoryBasicAuth) restTemplate.getRequestFactory();
    requestFactory.getCredentialsProvider().clear();
  }

  /**
   * Change password of user.
   *
   * @param username    the user name
   * @param oldPassword the old password
   * @param newPassword the new password
   * @return the result code
   * @throws Exception the exception
   */
  public ResultCode changePassword(String username, String oldPassword, String newPassword)
      throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    params.add("username", username);
    params.add("oldPassword", oldPassword);
    params.add("newPassword", newPassword);
    return restTemplate.postForObject(restTemplate.getUrl() + "auth/changePassword",
        params, ResultCode.class);
  }

  public TenantDto editTenant(TenantDto tenant) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "tenant", tenant, TenantDto.class);
  }

  /**
   * Gets all tenants.
   *
   * @return the list of tenants
   */
  public List<TenantDto> getTenants() throws Exception {
    ResponseEntity<List<TenantDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "tenants",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<TenantDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets all tenant admins that belongs to one tenant.
   *
   * @param tenantId the tenant id
   * @return the all tenant admins by tenant id
   */
  public List<UserDto> getAllTenantAdminsByTenantId(String tenantId) {
    ResponseEntity<List<UserDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "admins/" + tenantId,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {});
    return entity.getBody();
  }

  public TenantDto getTenant(String userId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "tenant/" + userId, TenantDto.class);
  }

  public ApplicationDto editApplication(ApplicationDto application) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "application",
        application, ApplicationDto.class);
  }

  /**
   * Gets all applications.
   *
   * @return the list of applications
   */
  public List<ApplicationDto> getApplications() throws Exception {

    ResponseEntity<List<ApplicationDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "applications",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ApplicationDto>>() {});
    return entity.getBody();
  }

  public ApplicationDto getApplicationByApplicationToken(String token) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "application/" + token,
        ApplicationDto.class);
  }

  public ConfigurationSchemaDto saveConfigurationSchema(ConfigurationSchemaDto configurationSchema)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "saveConfigurationSchema",
        configurationSchema, ConfigurationSchemaDto.class);
  }

  public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "saveProfileSchema",
        profileSchema, EndpointProfileSchemaDto.class);
  }

  public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "saveServerProfileSchema",
        serverProfileSchema, ServerProfileSchemaDto.class);
  }

  public NotificationSchemaDto createNotificationSchema(NotificationSchemaDto notificationSchema)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "createNotificationSchema",
        notificationSchema, NotificationSchemaDto.class);
  }

  public NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "saveNotificationSchema",
        notificationSchema, NotificationSchemaDto.class);
  }

  public LogSchemaDto createLogSchema(LogSchemaDto logSchema) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "createLogSchema",
        logSchema, LogSchemaDto.class);
  }

  public LogSchemaDto saveLogSchema(LogSchemaDto logSchema) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "saveLogSchema",
        logSchema, LogSchemaDto.class);
  }

  /**
   * Get existing flat schema.
   *
   * @param id the id of the CTL schema
   */
  public String getFlatSchemaByCtlSchemaId(String id) throws Exception {
    return restTemplate.getForObject(
        restTemplate.getUrl() + "CTL/getFlatSchemaByCtlSchemaId?id={id}",
        String.class, id);
  }

  public TopicDto createTopic(TopicDto topic) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "topic", topic, TopicDto.class);
  }

  public TopicDto getTopic(String topicId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "topic/" + topicId, TopicDto.class);
  }


  /**
   * Gets all topics by application token.
   *
   * @param applicationToken the application token
   */
  public List<TopicDto> getTopicsByApplicationToken(String applicationToken) throws Exception {

    ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "topics/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<TopicDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets all topics by endpoint group id.
   *
   * @param endpointGroupId the endpoint group id
   */
  public List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws Exception {
    ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "topics?endpointGroupId={endpointGroupId}", HttpMethod.GET,
        null, new ParameterizedTypeReference<List<TopicDto>>() {}, endpointGroupId);
    return entity.getBody();
  }

  /**
   * Gets all vacant topics by endpoint group id.
   *
   * @param endpointGroupId the endpoint group id
   */
  public List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws Exception {
    ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "vacantTopics/" + endpointGroupId,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<TopicDto>>() {});
    return entity.getBody();
  }

  public void addTopicToEndpointGroup(EndpointGroupDto endpointGroup, TopicDto topic)
      throws Exception {
    addTopicToEndpointGroup(endpointGroup.getId(), topic.getId());
  }

  /**
   * Adds the topic with specific id to endpoint group with specific id.
   *
   * @param endpointGroupId the endpoint group id
   * @param topicId         the topic id
   */
  public void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("endpointGroupId", endpointGroupId);
    params.add("topicId", topicId);
    restTemplate.postForObject(restTemplate.getUrl() + "addTopicToEpGroup", params, Void.class);
  }


  /**
   * Removes the topic with specific id to endpoint group with specific id.
   *
   * @param endpointGroupId the endpoint group id
   * @param topicId         the topic id
   */
  public void removeTopicFromEndpointGroup(String endpointGroupId, String topicId)
      throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("endpointGroupId", endpointGroupId);
    params.add("topicId", topicId);
    restTemplate.postForObject(restTemplate.getUrl() + "removeTopicFromEpGroup",
        params, Void.class);
  }

  /**
   * Remove the endpoint with specific profile key.
   *
   * @param endpointProfileKeyHash the endpoint profile key hash
   */
  public void removeEndpointProfileByKeyHash(String endpointProfileKeyHash) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("endpointProfileKeyHash", endpointProfileKeyHash);
    restTemplate.postForObject(restTemplate.getUrl() + "removeEndpointProfileByKeyHash",
        params, Void.class);
  }

  public NotificationDto sendNotification(NotificationDto notification, String notificationResource)
      throws Exception {
    return sendNotification(notification, getFileResource(notificationResource));
  }

  public NotificationDto sendNotification(NotificationDto notification,
                                          String notificationResourceName,
                                          String notificationResourceBody)
      throws Exception {
    return sendNotification(notification, getStringResource(notificationResourceName,
        notificationResourceBody));
  }

  private NotificationDto sendNotification(NotificationDto notification, ByteArrayResource resource)
      throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("notification", notification);
    params.add("file", resource);
    return restTemplate.postForObject(restTemplate.getUrl() + "sendNotification",
        params, NotificationDto.class);
  }

  public EndpointNotificationDto sendUnicastNotification(NotificationDto notification,
                                                         String clientKeyHash,
                                                         String notificationResource)
      throws Exception {
    return sendUnicastNotification(notification, clientKeyHash,
        getFileResource(notificationResource));
  }

  public EndpointNotificationDto sendUnicastNotification(NotificationDto notification,
                                                         String clientKeyHash,
                                                         String notificationResourceName,
                                                         String notificationResourceBody)
      throws Exception {
    return sendUnicastNotification(notification, clientKeyHash,
        getStringResource(notificationResourceName, notificationResourceBody));
  }

  private EndpointNotificationDto sendUnicastNotification(NotificationDto notification,
                                                          String clientKeyHash,
                                                          ByteArrayResource resource)
      throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("notification", notification);
    params.add("endpointKeyHash", clientKeyHash);
    params.add("file", resource);
    return restTemplate.postForObject(restTemplate.getUrl() + "sendUnicastNotification",
        params, EndpointNotificationDto.class);
  }


  /**
   * Send unicast notification to the client identified by endpointKeyHash.
   *
   * @param notification  the notification
   * @param clientKeyHash the client key hash
   * @param notificationMessage   the body of notification
   * @return the endpoint notification dto
   */
  public EndpointNotificationDto sendUnicastNotificationSimplified(NotificationDto notification,
                                                                   String clientKeyHash,
                                                                   String notificationMessage)
      throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("notification", notification);
    params.add("endpointKeyHash", clientKeyHash);
    params.add("file", getStringResource("notification", notificationMessage));
    return restTemplate.postForObject(restTemplate.getUrl() + "sendUnicastNotification",
        params, EndpointNotificationDto.class);
  }

  public ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "configurationSchema/"
        + configurationSchemaId, ConfigurationSchemaDto.class);
  }

  public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "profileSchema/"
        + profileSchemaId, EndpointProfileSchemaDto.class);
  }

  public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "serverProfileSchema/"
        + serverProfileSchemaId, ServerProfileSchemaDto.class);
  }

  public NotificationSchemaDto getNotificationSchema(String notificationSchemaId)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "notificationSchema/"
        + notificationSchemaId, NotificationSchemaDto.class);
  }

  public LogSchemaDto getLogSchema(String logSchemaId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "logSchema/"
        + logSchemaId, LogSchemaDto.class);
  }

  public SchemaVersions getSchemaVersionsByApplicationToken(String applicationToken)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "schemaVersions/"
        + applicationToken, SchemaVersions.class);
  }

  /**
   * Gets the configuration schemas by application token.
   *
   * @param applicationToken the application token
   */
  public List<ConfigurationSchemaDto> getConfigurationSchemasByAppToken(String applicationToken)
      throws Exception {

    ResponseEntity<List<ConfigurationSchemaDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "configurationSchemas/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ConfigurationSchemaDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets the client profile schemas by application token.
   *
   * @param applicationToken the application token
   */
  public List<EndpointProfileSchemaDto> getProfileSchemas(String applicationToken)
      throws Exception {
    ResponseEntity<List<EndpointProfileSchemaDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "profileSchemas/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<EndpointProfileSchemaDto>>() {});
    return entity.getBody();
  }


  /**
   * Gets the server profile schemas by application token.
   *
   * @param applicationToken the application token
   */
  public List<ServerProfileSchemaDto> getServerProfileSchemasByAppToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<ServerProfileSchemaDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "serverProfileSchemas/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ServerProfileSchemaDto>>() {});
    return entity.getBody();
  }


  /**
   * Gets the notification schemas by application token.
   *
   * @param applicationToken the application token
   */
  public List<NotificationSchemaDto> getNotificationSchemasByAppToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<NotificationSchemaDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "notificationSchemas/" + applicationToken,
        HttpMethod.GET, null,  new ParameterizedTypeReference<List<NotificationSchemaDto>>() {});
    return entity.getBody();
  }


  /**
   * Gets the user notification schemas by application token.
   *
   * @param applicationToken the application token
   */
  public List<VersionDto> getUserNotificationSchemasByAppToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<VersionDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "userNotificationSchemas/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<VersionDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets all log schemas by application token.
   *
   * @param applicationToken the application Token
   * @return the list of log schema dto
   */
  public List<LogSchemaDto> getLogSchemasByAppToken(String applicationToken) throws Exception {
    ResponseEntity<List<LogSchemaDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "logSchemas/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<LogSchemaDto>>() {});
    return entity.getBody();
  }



  /**
   * Gets all topics by application token.
   *
   * @param applicationId the application token
   */
  public List<TopicDto> getTopics(String applicationId) throws Exception {
    ResponseEntity<List<TopicDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "topics/" + applicationId,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<TopicDto>>() {});
    return entity.getBody();
  }

  public TopicDto editTopic(TopicDto topic) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "topic", topic, TopicDto.class);
  }

  public void deleteTopic(TopicDto topic) throws Exception {
    deleteTopic(topic.getId());
  }


  /**
   * Delete topic by its id.
   *
   * @param topicId the topic id
   */
  public void deleteTopic(String topicId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("topicId", topicId);
    restTemplate.postForLocation(restTemplate.getUrl() + "delTopic", params);
  }

  public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "endpointGroup",
        endpointGroup, EndpointGroupDto.class);
  }

  public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "endpointGroup/" + endpointGroupId,
        EndpointGroupDto.class);
  }

  /**
   * Delete endpoint group by its id.
   *
   * @param endpointGroupId the endpoint group id
   */
  public void deleteEndpointGroup(String endpointGroupId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("endpointGroupId", endpointGroupId);
    restTemplate.postForLocation(restTemplate.getUrl() + "delEndpointGroup", params);
  }


  /**
   * Gets all endpoint groups by application token.
   *
   * @param applicationToken the application token
   */
  public List<EndpointGroupDto> getEndpointGroupsByAppToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<EndpointGroupDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "endpointGroups/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<EndpointGroupDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets the vacant configuration schemas by endpoint group id.
   *
   * @param endpointGroupId the endpoint group id
   * @return the list schema dto
   */
  public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId)
      throws Exception {
    ResponseEntity<List<VersionDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "vacantConfigurationSchemas/" + endpointGroupId,
        HttpMethod.GET, null,  new ParameterizedTypeReference<List<VersionDto>>() {});
    return entity.getBody();
  }

  /**
   * Delete configuration record by schema id and endpoint group id.
   *
   * @param schemaId        the schema id
   * @param endpointGroupId the endpoint group id
   */
  public void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("schemaId", schemaId);
    params.add("endpointGroupId", endpointGroupId);
    restTemplate.postForObject(restTemplate.getUrl() + "delConfigurationRecord",
        params, Void.class);
  }

  /**
   * Gets the configuration records by endpoint group id.
   *
   * @param endpointGroupId   the endpoint group id
   * @param includeDeprecated specify if result will contain deprecated records
   */
  public List<ConfigurationRecordDto> getConfigurationRecords(String endpointGroupId,
                                                              boolean includeDeprecated)
      throws Exception {

    ResponseEntity<List<ConfigurationRecordDto>> entity = restTemplate.exchange(
        restTemplate.getUrl()
            + "configurationRecords?endpointGroupId={endpointGroupId}"
            + "&includeDeprecated={includeDeprecated}",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ConfigurationRecordDto>>() {},
        endpointGroupId, includeDeprecated);
    return entity.getBody();
  }

  public ConfigurationDto editConfiguration(ConfigurationDto configuration) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "configuration",
        configuration, ConfigurationDto.class);
  }



  /**
   * Gets the configuration record by schema id and endpoint group id.
   *
   * @param schemaId        the schema id
   * @param endpointGroupId the endpoint group id
   */
  public ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl()
        + "configurationRecord?schemaId={schemaId}&endpointGroupId={endpointGroupId}",
        ConfigurationRecordDto.class, schemaId, endpointGroupId);
  }


  /**
   * Get configuration record body.
   *
   * @param schemaId the schema id
   * @param endpointGroupId the endpoint group id
   */
  public String getConfigurationRecordBody(String schemaId, String endpointGroupId)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl()
        + "configurationRecordBody?schemaId={schemaId}&endpointGroupId={endpointGroupId}",
        String.class, schemaId, endpointGroupId);
  }

  public ConfigurationDto activateConfiguration(String configurationId) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "activateConfiguration",
        configurationId, ConfigurationDto.class);
  }

  public ConfigurationDto deactivateConfiguration(String configurationId) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "deactivateConfiguration",
        configurationId, ConfigurationDto.class);
  }

  public void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration)
      throws Exception {
    restTemplate.postForLocation(restTemplate.getUrl() + "userConfiguration",
        endpointUserConfiguration);
  }

  public EndpointSpecificConfigurationDto editEndpointSpecificConfiguration(EndpointSpecificConfigurationDto configuration) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "endpointSpecificConfiguration", configuration, EndpointSpecificConfigurationDto.class);
  }

  public void deleteActiveEndpointSpecificConfiguration(String endpointKeyHash) throws Exception {
    restTemplate.delete(restTemplate.getUrl() + "endpointSpecificConfiguration/{endpointKeyHash}", toUrlSafe(endpointKeyHash));
  }

  public EndpointSpecificConfigurationDto findActiveEndpointSpecificConfiguration(String endpointKeyHash) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "endpointSpecificConfiguration/{endpointKeyHash}",
        EndpointSpecificConfigurationDto.class, toUrlSafe(endpointKeyHash));
  }

  /**
   * Delete endpoint specific configuration.
   *
   * @param endpointKeyHash endpoint key hash
   * @param configurationSchemaVersion configuration schema version
   */
  public void deleteEndpointSpecificConfigurationByEndpointKeyHashAndConfigurationSchemaVersion(String endpointKeyHash,
                                                                                                int configurationSchemaVersion) throws Exception {
    restTemplate.delete(restTemplate.getUrl()
            + "endpointSpecificConfiguration/{endpointKeyHash}?configurationSchemaVersion={configurationSchemaVersion}",
        toUrlSafe(endpointKeyHash), configurationSchemaVersion);
  }

  /**
   * Find endpoint specific configuration.
   *
   * @param endpointKeyHash endpoint key hash
   * @param configurationSchemaVersion configuration schema version
   */
  public EndpointSpecificConfigurationDto findEndpointSpecificConfigurationByEndpointKeyHashAndConfigurationSchemaVersion(String endpointKeyHash,
                                                                                                                          int configurationSchemaVersion)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl()
            + "endpointSpecificConfiguration/{endpointKeyHash}?configurationSchemaVersion={configurationSchemaVersion}",
        EndpointSpecificConfigurationDto.class, toUrlSafe(endpointKeyHash), configurationSchemaVersion);
  }

  public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "profileFilter",
        profileFilter, ProfileFilterDto.class);
  }

  /**
   * Gets the profile filter record by EP server and client profile schema id and endpoint group id.
   *
   * @param endpointProfileSchemaId the endpoint profile schema id
   * @param serverProfileSchemaId   the server profile schema id
   * @param endpointGroupId         the endpoint group id
   */
  public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId,
                                                       String serverProfileSchemaId,
                                                       String endpointGroupId) throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append(restTemplate.getUrl())
      .append("profileFilterRecord?endpointGroupId={endpointGroupId}");
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
    return restTemplate.getForObject(sb.toString(), ProfileFilterRecordDto.class,
        urlVariables.toArray());
  }

  /**
   * Gets the vacant profile schemas by endpoint group id.
   *
   * @param endpointGroupId the endpoint group id
   */
  public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(
      String endpointGroupId) throws Exception {
    ResponseEntity<List<ProfileVersionPairDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "vacantProfileSchemas/" + endpointGroupId,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ProfileVersionPairDto>>() {});
    return entity.getBody();
  }


  /**
   * Delete profile filter record by schema ids and endpoin group id.
   *
   * @param endpointProfileSchemaId the endpoint profile schema id
   * @param serverProfileSchemaId   the server profile schema id
   * @param endpointGroupId         the endpoint group id
   */
  public void deleteProfileFilterRecord(String endpointProfileSchemaId,
                                        String serverProfileSchemaId,
                                        String endpointGroupId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("endpointProfileSchemaId", endpointProfileSchemaId);
    params.add("serverProfileSchemaId", serverProfileSchemaId);
    params.add("endpointGroupId", endpointGroupId);
    restTemplate.postForObject(restTemplate.getUrl() + "delProfileFilterRecord", params,
        Void.class);
  }



  /**
   * Gets the profile filter records by endpoint group id.
   *
   * @param endpointGroupId   the endpoint group id
   * @param includeDeprecated the include deprecated
   */
  public List<ProfileFilterRecordDto> getProfileFilterRecords(String endpointGroupId,
                                                              boolean includeDeprecated)
      throws Exception {
    ResponseEntity<List<ProfileFilterRecordDto>> entity = restTemplate.exchange(
        restTemplate.getUrl()
            + "profileFilterRecords?endpointGroupId={endpointGroupId}"
            + "&includeDeprecated={includeDeprecated}",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<ProfileFilterRecordDto>>() {},
        endpointGroupId, includeDeprecated);
    return entity.getBody();
  }

  public ProfileFilterDto activateProfileFilter(String profileFilterId) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "activateProfileFilter",
        profileFilterId, ProfileFilterDto.class);
  }

  public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "deactivateProfileFilter",
        profileFilterId, ProfileFilterDto.class);
  }

  public UserDto editUser(UserDto user) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "user", user, UserDto.class);
  }

  public UserDto getUser(String userId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "user/" + userId, UserDto.class);
  }


  /**
   * Delete user by user id.
   *
   * @param userId the user id
   */
  public void deleteUser(String userId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("userId", userId);
    restTemplate.postForLocation(restTemplate.getUrl() + "delUser", params);
  }

  /**
   * Gets all users.
   *
   */
  public List<UserDto> getUsers() throws Exception {
    ResponseEntity<List<UserDto>> entity = restTemplate.exchange(restTemplate.getUrl() + "users",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets the log schema by application token and schema version.
   *
   * @param applicationToken the application token
   * @param schemaVersion    the schema version
   */
  public LogSchemaDto getLogSchemaByApplicationTokenAndSchemaVersion(String applicationToken,
                                                                     int schemaVersion)
      throws Exception {

    ResponseEntity<LogSchemaDto> entity = restTemplate.exchange(
        restTemplate.getUrl() + "logSchema/" + applicationToken + "/" + schemaVersion,
        HttpMethod.GET, null, new ParameterizedTypeReference<LogSchemaDto>() {});
    return entity.getBody();
  }

  public EventClassFamilyDto editEventClassFamily(EventClassFamilyDto eventClassFamily)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "eventClassFamily",
        eventClassFamily, EventClassFamilyDto.class);
  }

  public EventClassFamilyDto getEventClassFamilyById(String ecfId) {
    return restTemplate.getForObject(restTemplate.getUrl() + "eventClassFamily/" + ecfId,
        EventClassFamilyDto.class);
  }

  /**
   * Gets the event class family versions by its id.
   *
   * @param ecfId the event class family id
   * @return the list of event class family version dto
   */
  public List<EventClassFamilyVersionDto> getEventClassFamilyVersionsById(String ecfId) {
    ResponseEntity<List<EventClassFamilyVersionDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "eventClassFamilyVersions/" + ecfId,
        HttpMethod.GET, null,
        new ParameterizedTypeReference<List<EventClassFamilyVersionDto>>() {});
    return entity.getBody();
  }


  /**
   * Gets all event class family by family name.
   */
  public EventClassFamilyDto getEventClassFamily(String familyName) {
    ResponseEntity<List<EventClassFamilyDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "eventClassFamilies",
        HttpMethod.GET, null,  new ParameterizedTypeReference<List<EventClassFamilyDto>>() {});
    List<EventClassFamilyDto> familyList = entity.getBody();
    for (EventClassFamilyDto family : familyList) {
      if (family.getClassName().equals(familyName)) {
        return family;
      }
    }
    throw new RuntimeException("Family with name " + familyName + " not found!");
  }

  /**
   * Gets all event class families.
   *
   * @return the list event class family dto
   */
  public List<EventClassFamilyDto> getEventClassFamilies() {
    ResponseEntity<List<EventClassFamilyDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "eventClassFamilies",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<EventClassFamilyDto>>() {});
    return entity.getBody();
  }


  /**
   * Adds the event class family version to existing event class family with
   * specific id. Current user will be marked as creator of schema.
   *
   * @param eventClassFamilyId      the event class family id
   * @param eventClassFamilyVersion the version of event class family
   */
  public void addEventClassFamilyVersion(String eventClassFamilyId,
                                         EventClassFamilyVersionDto eventClassFamilyVersion)
      throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("eventClassFamilyId", eventClassFamilyId);
    params.add("eventClassFamilyVersion", eventClassFamilyVersion);
    restTemplate.postForLocation(restTemplate.getUrl() + "addEventClassFamilyVersion", params);
  }

  /**
   * Gets the event classes by family its id, version and type.
   *
   * @param eventClassFamilyId the event class family id
   * @param version            the version
   * @param type               the type
   */
  public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(String eventClassFamilyId,
                                                                     int version,
                                                                     EventClassType type)
      throws Exception {
    ResponseEntity<List<EventClassDto>> entity = restTemplate.exchange(restTemplate.getUrl()
        + "eventClasses?eventClassFamilyId={eventClassFamilyId}&"
        + "version={version}"
        + "&type={type}",
        HttpMethod.GET, null,  new ParameterizedTypeReference<List<EventClassDto>>() {},
        eventClassFamilyId, version, type);
    return entity.getBody();
  }

  public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(
      ApplicationEventFamilyMapDto applicationEventFamilyMap)
      throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "applicationEventMap",
        applicationEventFamilyMap, ApplicationEventFamilyMapDto.class);
  }

  public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(String aefMapId)
      throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "applicationEventMap/" + aefMapId,
        ApplicationEventFamilyMapDto.class);
  }

  /**
   * Gets all application event family maps by application token.
   *
   * @param applicationToken the application token
   * @return list the application event family map dto
   */
  public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(
      String applicationToken) throws Exception {
    ResponseEntity<List<ApplicationEventFamilyMapDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "applicationEventMaps/" + applicationToken,
        HttpMethod.GET, null,
        new ParameterizedTypeReference<List<ApplicationEventFamilyMapDto>>() {});
    return entity.getBody();
  }


  /**
   * Gets all vacant event class families by application token.
   *
   * @param applicationToken the application token
   * @return the list ecf info dto
   */
  public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<EcfInfoDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "vacantEventClassFamilies/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<EcfInfoDto>>() {});
    return entity.getBody();
  }

  /**
   * Gets all event class families by application token.
   *
   * @param applicationToken the application token
   */
  public List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<AefMapInfoDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "eventClassFamilies/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<AefMapInfoDto>>() {});
    return entity.getBody();
  }

  public LogAppenderDto editLogAppenderDto(LogAppenderDto logAppenderDto) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "logAppender",
        logAppenderDto, LogAppenderDto.class);
  }

  public LogAppenderDto getLogAppender(String logAppenderId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "logAppender/" + logAppenderId,
        LogAppenderDto.class);
  }

  /**
   * Gets all log appenders by application token.
   *
   * @param applicationToken the application token
   */
  public List<LogAppenderDto> getLogAppendersByAppToken(String applicationToken) throws Exception {
    ResponseEntity<List<LogAppenderDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "logAppenders/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<LogAppenderDto>>() {});
    return entity.getBody();
  }


  /**
   * Delete log appender by its id.
   *
   * @param logAppenderId the log appender id
   */
  public void deleteLogAppender(String logAppenderId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("logAppenderId", logAppenderId);
    restTemplate.postForLocation(restTemplate.getUrl() + "delLogAppender", params);
  }

  public UserVerifierDto getUserVerifier(String userVerifierId) throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "userVerifier/" + userVerifierId,
        UserVerifierDto.class);
  }

  /**
   * Gets all user verifiers by application token.
   *
   * @param applicationToken the application token
   * @return the list user verifier dto
   */
  public List<UserVerifierDto> getUserVerifiersByApplicationToken(String applicationToken) {
    ResponseEntity<List<UserVerifierDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "userVerifiers/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<UserVerifierDto>>() {});
    return entity.getBody();
  }

  public UserVerifierDto editUserVerifierDto(UserVerifierDto userVerifierDto) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "userVerifier",
        userVerifierDto, UserVerifierDto.class);
  }

  /**
   * Delete user verifier by its id.
   *
   * @param userVerifierId the user verifier id
   */
  public void deleteUserVerifier(String userVerifierId) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("userVerifierId", userVerifierId);
    restTemplate.postForLocation(restTemplate.getUrl() + "delUserVerifier", params);
  }

  public SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws Exception {
    return restTemplate.postForObject(restTemplate.getUrl() + "createSdkProfile",
        sdkProfile, SdkProfileDto.class);
  }

  /**
   * Deletes an SDK profile by its identifier.
   *
   * @param sdkProfile the sdk profile
   */
  public void deleteSdkProfile(SdkProfileDto sdkProfile) throws Exception {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("topicId", sdkProfile.getId());
    restTemplate.postForLocation(restTemplate.getUrl() + "deleteSdkProfile", params);
  }


  /**
   * Returns an SDK profile by its identifier.
   *
   * @param sdkProfileId the sdk profile id
   */
  public SdkProfileDto getSdkProfile(String sdkProfileId) throws Exception {
    ResponseEntity<SdkProfileDto> entity = restTemplate.exchange(
        restTemplate.getUrl() + "sdkProfile/" + sdkProfileId,
        HttpMethod.GET, null,  new ParameterizedTypeReference<SdkProfileDto>() {});
    return entity.getBody();
  }

  /**
   * Returns a list of SDK profiles for the given application.
   *
   * @param applicationToken the application token
   */
  public List<SdkProfileDto> getSdkProfilesByApplicationToken(String applicationToken)
      throws Exception {
    ResponseEntity<List<SdkProfileDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "sdkProfiles/" + applicationToken,
        HttpMethod.GET, null, new ParameterizedTypeReference<List<SdkProfileDto>>() {});
    return entity.getBody();
  }

  /**
   * Generates an SDK for the specified target platform from the SDK profile .
   *
   * @param sdkProfileId   the sdk profile id
   * @param targetPlatform the target platform
   */
  public void downloadSdk(String sdkProfileId, SdkPlatform targetPlatform, String destination) {
    FileResponseExtractor extractor = new FileResponseExtractor(new File(destination));
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("sdkProfileId", sdkProfileId);
    parameters.add("targetPlatform", targetPlatform.toString());
    RequestCallback request = new DataRequestCallback<>(parameters);
    restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
  }

  /**
   * Generates an SDK for the specified target platform from specified SDK profile.
   *
   * @param sdkProfileId   the sdk profile id
   * @param targetPlatform the target platform
   */
  public FileData downloadSdk(String sdkProfileId, SdkPlatform targetPlatform) {
    FileDataResponseExtractor extractor = new FileDataResponseExtractor();
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("sdkProfileId", sdkProfileId);
    parameters.add("targetPlatform", targetPlatform.toString());
    RequestCallback request = new DataRequestCallback<>(parameters);
    return restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
  }

  /**
   * Generates a new SDK from specified SDK profile.
   */
  public void downloadSdk(SdkProfileDto key, String destination) throws Exception {
    FileResponseExtractor extractor = new FileResponseExtractor(new File(destination));
    RequestCallback request = new DataRequestCallback<>(key);
    restTemplate.execute(restTemplate.getUrl() + "sdk", HttpMethod.POST, request, extractor);
    LOG.info("Downloaded sdk to file '{}'", extractor.getDestFile());
  }



  /**
   * Generates a new SDK from specified SDK profile.
   */
  public FileData downloadSdk(SdkProfileDto key) throws Exception {
    FileDataResponseExtractor extractor = new FileDataResponseExtractor();
    RequestCallback request = new DataRequestCallback<>(key);
    FileData data = restTemplate.execute(restTemplate.getUrl() + "sdk",
        HttpMethod.POST, request, extractor);
    return data;
  }



  /**
   * Generate log library by record key.
   *
   * @param key  the record key
   */
  public FileData downloadLogRecordLibrary(RecordKey key) throws Exception {
    FileDataResponseExtractor extractor = new FileDataResponseExtractor();
    RequestCallback request = new DataRequestCallback<>(key);
    FileData data = restTemplate.execute(restTemplate.getUrl() + "logLibrary",
        HttpMethod.POST, request, extractor);
    return data;
  }

  /**
   * Get log record schema with header and log schema inside by record key.
   *
   * @param key the record key
   */
  public FileData downloadLogRecordSchema(RecordKey key) throws Exception {
    FileDataResponseExtractor extractor = new FileDataResponseExtractor();
    RequestCallback request = new DataRequestCallback<>(key);
    FileData data = restTemplate.execute(restTemplate.getUrl() + "logRecordSchema",
        HttpMethod.POST, request, extractor);
    return data;
  }

  /**
   * Exports a CTL schema and all of its dependencies depending on the export method specified.
   */
  public FileData downloadCtlSchemaByAppToken(CTLSchemaDto ctlSchemaDto,
                                              CTLSchemaExportMethod method, String appToken) {
    final FileDataResponseExtractor extractor = new FileDataResponseExtractor();
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("fqn", ctlSchemaDto.getMetaInfo().getFqn());
    parameters.add("version", Integer.toString(ctlSchemaDto.getVersion()));
    if (ctlSchemaDto.getMetaInfo().getApplicationId() != null) {
      parameters.add("applicationToken", appToken);
    }
    parameters.add("method", method.name());
    RequestCallback request = new DataRequestCallback<>(parameters);
    return restTemplate.execute(restTemplate.getUrl() + "CTL/exportSchema",
        HttpMethod.POST, request, extractor);
  }



  /**
   * Flushes all cached Sdks within tenant.
   *
   */
  public void flushSdkCache() throws Exception {
    restTemplate.postForLocation(restTemplate.getUrl() + "flushSdkCache", null);
  }


  //CHECKSTYLE:OFF
  public CTLSchemaDto saveCTLSchemaWithAppToken(String body, String tenantId,
                                                String applicationToken) {
    //CHECKSTYLE:ON
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("body", body);
    if (tenantId != null) {
      params.add("tenantId", tenantId);
    }
    if (applicationToken != null) {
      params.add("applicationToken", applicationToken);
    }
    return restTemplate.postForObject(restTemplate.getUrl() + "CTL/saveSchema",
        params, CTLSchemaDto.class);
  }

  //CHECKSTYLE:OFF
  public void deleteCTLSchemaByFqnVersionTenantIdAndApplicationToken(String fqn,
                                                                     Integer version,
                                                                     String tenantId,
                                                                     String applicationToken) {
    //CHECKSTYLE:ON
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

  //CHECKSTYLE:OFF
  public CTLSchemaDto getCTLSchemaByFqnVersionTenantIdAndApplicationToken(
      String fqn, Integer version, String tenantId, String applicationToken) {
    //CHECKSTYLE:ON
    if (tenantId != null && applicationToken != null) {
      return restTemplate.getForObject(
          restTemplate.getUrl() + "CTL/getSchema?fqn={fqn}&version={version}&tenantId={tenantId}"
              + "&applicationToken={applicationToken}",
          CTLSchemaDto.class, fqn, version, tenantId, applicationToken);
    } else if (tenantId != null) {
      return restTemplate.getForObject(
          restTemplate.getUrl() + "CTL/getSchema?fqn={fqn}&version={version}&tenantId={tenantId}",
          CTLSchemaDto.class, fqn, version, tenantId);
    } else {
      return restTemplate.getForObject(
          restTemplate.getUrl() + "CTL/getSchema?fqn={fqn}&version={version}",
          CTLSchemaDto.class, fqn, version);
    }
  }

  //CHECKSTYLE:OFF
  public CTLSchemaDto getCTLSchemaById(String id) {
    //CHECKSTYLE:ON
    return restTemplate.getForObject(restTemplate.getUrl() + "CTL/getSchemaById?id={id}",
        CTLSchemaDto.class, id);
  }

  /**
   * Checks if CTL schema with same fqn is already exists in the sibling applications.
   *
   * @param fqn              the full qualified name
   * @param tenantId         id of the tenant
   * @param applicationToken the application token
   * @return true if CTL schema with same fqn is already exists in other scope
   */
  public boolean checkFqnExistsWithAppToken(String fqn, String tenantId,
                                            String applicationToken) {
    if (tenantId != null && applicationToken != null) {
      return restTemplate.getForObject(
          restTemplate.getUrl() + "CTL/checkFqn?fqn={fqn}&tenantId={tenantId}"
              + "&applicationToken={applicationToken}",
          Boolean.class, fqn, tenantId, applicationToken);
    } else if (tenantId != null) {
      return restTemplate.getForObject(
          restTemplate.getUrl() + "CTL/checkFqn?fqn={fqn}&tenantId={tenantId}",
          Boolean.class, fqn, tenantId);
    } else {
      return restTemplate.getForObject(restTemplate.getUrl() + "CTL/checkFqn?fqn={fqn}",
          Boolean.class, fqn);
    }
  }

  /**
   * Promote existing CTL schema meta info from application to tenant scope
   *
   * @param applicationId the id of application where schema was created
   * @param fqn           the fqn of promoting CTL schema
   * @return CtlSchemaMetaInfoDto the promoted CTL schema meta info object.
   */
  public CtlSchemaMetaInfoDto promoteScopeToTenant(String applicationId, String fqn) {
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("applicationId", applicationId);
    params.add("fqn", fqn);
    return restTemplate.postForObject(restTemplate.getUrl() + "CTL/promoteScopeToTenant",
        params, CtlSchemaMetaInfoDto.class);
  }

  //CHECKSTYLE:OFF
  public List<CtlSchemaMetaInfoDto> getSystemLevelCTLSchemas() {
    //CHECKSTYLE:ON
    ResponseEntity<List<CtlSchemaMetaInfoDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "CTL/getSystemSchemas",
        HttpMethod.GET, null, new ParameterizedTypeReference<List<CtlSchemaMetaInfoDto>>() {});
    return entity.getBody();
  }

  //CHECKSTYLE:OFF
  public List<CtlSchemaMetaInfoDto> getTenantLevelCTLSchemas() {
    //CHECKSTYLE:ON
    ResponseEntity<List<CtlSchemaMetaInfoDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "CTL/getTenantSchemas",
        HttpMethod.GET, null,  new ParameterizedTypeReference<List<CtlSchemaMetaInfoDto>>() {});
    return entity.getBody();
  }

  //CHECKSTYLE:OFF
  public List<CtlSchemaMetaInfoDto> getApplicationLevelCTLSchemasByAppToken(
      String applicationToken) {
    //CHECKSTYLE:ON
    ResponseEntity<List<CtlSchemaMetaInfoDto>> entity = restTemplate.exchange(
        restTemplate.getUrl() + "CTL/getApplicationSchemas/" + applicationToken,
        HttpMethod.GET, null,  new ParameterizedTypeReference<List<CtlSchemaMetaInfoDto>>() {});
    return entity.getBody();
  }


  /**
   * Gets the user profile of current user.
   *
   * @return the user dto
   */
  public UserDto getUserProfile() throws Exception {
    return restTemplate.getForObject(restTemplate.getUrl() + "userProfile", UserDto.class);
  }

  /**
   * Edits user profile to all user profiles.
   *
   * @param userProfileUpdateDto the user profile dto
   */
  public void editUserProfile(UserProfileUpdateDto userProfileUpdateDto) {
    restTemplate.postForObject(restTemplate.getUrl() + "userProfile",
        userProfileUpdateDto, Void.class);
  }

  /**
   * Returns a list of endpoint profiles attached to the endpoint user with
   * the given external id.
   *
   * @param endpointUserExternalId the endpoint user external id
   * @return a list of endpoint profiles for the user with the given external id
   */
  public List<EndpointProfileDto> getEndpointProfilesByUserExternalId(
      String endpointUserExternalId) {
    String address = restTemplate.getUrl() + "endpointProfiles?userExternalId="
        + endpointUserExternalId;

    ResponseEntity<List<EndpointProfileDto>> response = this.restTemplate.exchange(
        address, HttpMethod.GET, null,
        new ParameterizedTypeReference<List<EndpointProfileDto>>() {});
    return response.getBody();
  }

  /**
   * Provides security credentials, allowing an endpoint that uses them to
   * interact with the specified application.
   *
   * @param applicationToken the application token to allow interaction with
   * @param credentialsBody  the security credentials to save
   */
  public CredentialsDto provisionCredentials(String applicationToken, byte[] credentialsBody) {
    MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
    parameters.add("applicationToken", applicationToken);
    parameters.add("credentialsBody", Base64Utils.encodeToString(credentialsBody));
    return this.restTemplate.postForObject(restTemplate.getUrl() + "provisionCredentials",
        parameters, CredentialsDto.class);
  }

  /**
   * Binds credentials to the specified server-side endpoint profile.
   *
   * @param applicationToken     the application token
   * @param credentialsId        the id of the credentials to bind
   * @param serverProfileVersion the server-side endpoint profile version
   * @param serverProfileBody    the server-side endpoint profile body
   */
  public void provisionRegistration(String applicationToken, String credentialsId,
                                    Integer serverProfileVersion, String serverProfileBody) {
    MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
    parameters.add("applicationToken", applicationToken);
    parameters.add("credentialsId", credentialsId);
    parameters.add("serverProfileVersion", serverProfileVersion);
    parameters.add("serverProfileBody", serverProfileBody);
    this.restTemplate.postForLocation(restTemplate.getUrl() + "provisionRegistration", parameters);
  }

  /**
   * Provides the status for given credentials.
   *
   * @param applicationToken the application token
   * @param credentialsId    the id of the credentials
   * @return credentials status
   */
  public CredentialsStatus getCredentialsStatus(String applicationToken, String credentialsId) {
    return this.restTemplate.getForObject(
        restTemplate.getUrl() + "credentialsStatus?applicationToken={applicationToken}"
            + "&credentialsId={credentialsId}",
        CredentialsStatus.class, applicationToken, credentialsId);
  }

  /**
   * Revokes security credentials from the corresponding credentials storage.
   * Also launches an asynchronous process to terminate all active sessions of
   * the endpoint that uses these credentials.
   *
   * @param applicationToken the application token
   * @param credentialsId     the id of the credentials
   */
  public void revokeCredentials(String applicationToken, String credentialsId) {
    MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
    parameters.add("applicationToken", applicationToken);
    parameters.add("credentialsId", credentialsId);
    this.restTemplate.postForLocation(restTemplate.getUrl() + "revokeCredentials", parameters);
  }

  /**
   * Used if credentials stored in external storage and Kaa server can't directly revoke them but
   * can be notified about security credentials revocation by external system.
   *
   * <p>If an endpoint is already registered with the specified credentials, this API
   * call launches an asynchronous process to terminate all active sessions of
   * the corresponding endpoint.</p>
   *
   * @param applicationToken the application token
   * @param credentialsId     the id of the credentials
   */
  public void onCredentialsRevoked(String applicationToken, String credentialsId) {
    MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
    parameters.add("applicationToken", applicationToken);
    parameters.add("credentialsId", credentialsId);
    this.restTemplate.postForLocation(restTemplate.getUrl() + "notifyRevoked", parameters);
  }

  /**
   * Get user configuration of by externalUID, schema version and application token.
   *
   * @param appToken      the application token
   * @param schemaVersion the schema version
   * @param externalUId   the external user id
   */
  public EndpointUserConfigurationDto findUserConfigurationByUserId(String externalUId,
                                                                    String appToken,
                                                                    Integer schemaVersion) {
    return restTemplate.getForObject(
        restTemplate.getUrl() + "configuration/{externalUId}/{appToken}/{schemaVersion}",
        EndpointUserConfigurationDto.class, externalUId, appToken, schemaVersion);
  }

  /**
   * Get configuration of specific endpoint by endpointKeyHash.
   *
   * @param endpointKeyHash the endpoint key hash
   */
  public String findEndpointConfigurationByEndpointKeyHash(String endpointKeyHash) {
    return restTemplate.getForObject(restTemplate.getUrl() + "configuration/{endpointKeyHash}/",
        String.class, endpointKeyHash);
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
        Matcher matcher = fileNamePattern.matcher(contentDisposition);
        if (matcher.matches()) {
          fileName = matcher.group(2);
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
      final String contentType = response.getHeaders().getFirst("Content-Type");
      if (StringUtils.isNotBlank(contentDisposition)) {
        Matcher matcher = fileNamePattern.matcher(contentDisposition);
        if (matcher.matches()) {
          fileName = matcher.group(2);
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

  private class DataRequestCallback<T> implements RequestCallback {

    private List<MediaType> mediaTypes = asList(APPLICATION_JSON, valueOf("application/*+json"));

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
          ((HttpMessageConverter<Object>) messageConverter).write(requestBody,
              requestContentType, httpRequest);
          return;
        }
      }
    }
  }
}
