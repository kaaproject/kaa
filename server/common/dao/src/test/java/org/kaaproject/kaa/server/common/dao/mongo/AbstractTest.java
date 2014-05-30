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

package org.kaaproject.kaa.server.common.dao.mongo;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.HistoryDao;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.TenantDao;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.configuration.DefaultConfigurationProcessor;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.Configuration;
import org.kaaproject.kaa.server.common.dao.mongo.model.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.mongo.model.History;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
import org.kaaproject.kaa.server.common.dao.mongo.model.Tenant;
import org.kaaproject.kaa.server.common.dao.mongo.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);

    protected static final Random random = new Random(0);

    protected static final String UNIQUE_ID = "uid";
    protected static final String APPLICATION = "Application";
    protected static final String TENANT = "Tenant";
    protected static final String UPDATED_OBJECT = "Updated Object";
    protected static final String TENANT_NAME = "Generated Test Tenant";
    protected static final String USER_NAME = "Generated Test Username";
    protected static final String TOPIC_NAME = "Generated Topic Name";
    protected static final String NOTIFICATION_SCHEMA_NAME = "Generated Notification Schema Name";

    protected List<String> tenants = new ArrayList<String>();
    protected List<String> users = new ArrayList<String>();
    protected List<String> apps = new ArrayList<String>();
    protected List<String> schemas = new ArrayList<String>();
    protected List<String> configurations = new ArrayList<String>();
    protected List<String> endGroups = new ArrayList<String>();
    protected List<String> endConf = new ArrayList<String>();
    protected List<String> hists = new ArrayList<String>();
    protected List<String> endProfiles = new ArrayList<String>();

    @Autowired
    protected ApplicationDao<Application> applicationDao;
    @Autowired
    protected ApplicationService applicationService;
    @Autowired
    protected TenantDao<Tenant> tenantDao;
    @Autowired
    protected TopicService topicService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected ConfigurationDao<Configuration> configurationDao;
    @Autowired
    protected ConfigurationSchemaDao<ConfigurationSchema> configurationSchemaDao;
    @Autowired
    protected EndpointConfigurationDao<EndpointConfiguration> endpointConfigurationDao;
    @Autowired
    protected EndpointProfileDao<EndpointProfile> endpointProfileDao;
    @Autowired
    protected EndpointGroupDao<EndpointGroup> endpointGroupDao;
    @Autowired
    protected EndpointService endpointService;
    @Autowired
    protected HistoryDao<History> historyDao;
    @Autowired
    protected HistoryService historyService;
    @Autowired
    protected ProfileSchemaDao<ProfileSchema> profileSchemaDao;
    @Autowired
    protected ProfileFilterDao<ProfileFilter> profileFilterDao;
    @Autowired
    protected ProfileService profileService;
    @Autowired
    protected ConfigurationService configurationService;
    @Autowired
    protected NotificationService notificationService;

    protected Application application;

    public AbstractTest() {
        tenants.add("530cb57de4b08e6930468c99");
        users.add("530cb57de4b08e6930468c9a");

        apps.add("52fe2de7e4b0082727dd214b");
        apps.add("52fe2de7e4b0082727dd214c");
        apps.add("52fe2de7e4b0082727dd214d");

        schemas.add("530cc8f108c05463a7797b9a");
        schemas.add("530cc8f408c05463a7797b9b");
        schemas.add("530cc8fa08c05463a7797b9c");

        configurations.add("530cc8fa08c05463a7797b8c");
        configurations.add("530cc8fa08c05463a7797b9d");
        configurations.add("530cc8fa08c05463a7797b9e");
        configurations.add("530cc8fa08c05463a7797b9f");
        configurations.add("530cc8fa08c05463a7797b10");
        configurations.add("530cc8fa08c05463a7797b91");
        configurations.add("530cc8fa08c05463a7797b92");
        configurations.add("530cc8fa08c05463a7797b93");

        endGroups.add("530db663687f16fec35271f2");

        endConf.add("530cc8fa08c05463a7797b92");

        hists.add("530db773687f16fec3527100");
        hists.add("530db773687f16fec3527101");

        endProfiles.add("530db773687f16fec3529243");
    }

    protected String generateString(String string) {
        return string + "_" + UUID.randomUUID().toString();
    }

    /**
     * Gets the resource as string.
     *
     * @param path
     *            the path
     * @return the resource as string
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    protected static String getResourceAsString(String path) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        File file = new File(url.getPath());
        String result;
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            result = sb.toString();
        } finally {
            br.close();
        }
        return result;
    }

    protected String readSchemaFileAsString(String filePath) throws IOException {
        try {
            Path path = Paths.get(Thread.currentThread().getContextClassLoader().getResource(filePath).toURI());
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (URISyntaxException e) {
            LOGGER.error("Can't generate configs {}", e);
        }
        return null;
    }

    protected ApplicationDto generateApplication() {
        return generateApplication(new ObjectId().toString());
    }

    protected ApplicationDto generateApplication(String tenantId) {
        Application app = new Application();
        if (isBlank(tenantId)) {
            app.setTenantId(new ObjectId());
        } else {
            app.setTenantId(new ObjectId(tenantId));
        }
        app.setName("Test app");
        Update update = new Update();
        update.setStatus(ProcessingStatus.IDLE);
        app.setUpdate(update);
        return applicationService.saveApp(app.toDto());
    }

    protected EndpointGroupDto generateEndpointGroup(String appId) {
        EndpointGroup group = new EndpointGroup();
        if (isBlank(appId)) {
            appId = generateApplication().getId();
        }
        group.setApplicationId(new ObjectId(appId));
        group.setName("GROUP_ALL");
        group.setWeight(random.nextInt());
        return endpointService.saveEndpointGroup(group.toDto());
    }

    protected List<ConfigurationSchemaDto> generateConfSchema(String appId, int count) {
        List<ConfigurationSchemaDto> schemas = Collections.emptyList();
        try {
            if (isBlank(appId)) {
                appId = generateApplication().getId();
            }
            ConfigurationSchemaDto schemaDto;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new ConfigurationSchemaDto();
                schemaDto.setApplicationId(appId);
                schemaDto.setSchema(readSchemaFileAsString("dao/schema/testDataSchema.json"));
                schemaDto.setStatus(UpdateStatus.ACTIVE);
                schemaDto = configurationService.saveConfSchema(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOGGER.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations.");
        }
        return schemas;
    }

    protected List<ConfigurationDto> generateConfiguration(String schemaId, String groupId, int count, boolean activate, boolean useBaseSchema) {
        List<ConfigurationDto> ids = Collections.emptyList();
        try {
            ConfigurationSchemaDto schemaDto;
            if (isNotBlank(schemaId)) {
                schemaDto = configurationService.findConfSchemaById(schemaId);
            } else {
                schemaDto = generateConfSchema(null, 1).get(0);
            }
            Assert.assertNotNull(schemaDto);
            DefaultConfigurationProcessor configurationProcessor;
            if (useBaseSchema) {
                configurationProcessor = new DefaultConfigurationProcessor(schemaDto.getBaseSchema());
            } else {
                configurationProcessor = new DefaultConfigurationProcessor(schemaDto.getOverrideSchema());
            }
            ids = new ArrayList();
            for (int i = 0; i < count; i++) {
                ConfigurationDto dto = new ConfigurationDto();
                dto.setId(null);
                dto.setStatus(null);
                dto.setBinaryBody(configurationProcessor.getRootJsonConfiguration().getBytes(Charset.forName("UTF-8")));
                dto.setSchemaId(schemaDto.getId());
                if (isBlank(groupId)) {
                    groupId = generateEndpointGroup(schemaDto.getApplicationId()).getId();
                }
                dto.setEndpointGroupId(groupId);
                ConfigurationDto saved = configurationService.saveConfiguration(dto);
                Assert.assertNotNull(saved);
                if (activate) {
                    ChangeConfigurationNotification notification = configurationService.activateConfiguration(saved.getId(), schemaDto.getCreatedUsername());
                    saved = notification.getConfigurationDto();
                }
                ids.add(saved);
            }
        } catch (Exception e) {
            LOGGER.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations.");
        }
        return ids;
    }

    protected List<ProfileSchemaDto> generateProfSchema(String appId, int count) {
        List<ProfileSchemaDto> schemas = Collections.emptyList();
        try {
            if (isBlank(appId)) {
                appId = generateApplication().getId();
            }
            ProfileSchemaDto schemaDto;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new ProfileSchemaDto();
                schemaDto.setApplicationId(appId);
                schemaDto.setSchema(readSchemaFileAsString("dao/schema/testDataSchema.json"));
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setName("Test Name");
                schemaDto = profileService.saveProfileSchema(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOGGER.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations.");
        }
        return schemas;
    }

    protected List<ProfileFilterDto> generateFilter(String schemaId, String groupId, int count, boolean activate) {
        List<ProfileFilterDto> filters = Collections.emptyList();
        try {
            ProfileSchemaDto schemaDto = null;
            if (isBlank(schemaId)) {
                schemaDto = generateProfSchema(null, 1).get(0);
                schemaId = schemaDto.getId();
            } else {
                schemaDto = profileService.findProfileSchemaById(schemaId);
            }

            filters = new ArrayList();
            for (int i = 0; i < count; i++) {
                ProfileFilterDto dto = new ProfileFilterDto();
                dto.setId(null);
                dto.setStatus(null);
                if (isBlank(groupId)) {
                    groupId = generateEndpointGroup(schemaDto.getApplicationId()).getId();
                }
                dto.setEndpointGroupId(groupId);
                dto.setSchemaId(schemaId);
                dto.setApplicationId(schemaDto.getApplicationId());
                ProfileFilterDto saved = profileService.saveProfileFilter(dto);
                Assert.assertNotNull(saved);
                if (activate) {
                    ChangeProfileFilterNotification notification = profileService.activateProfileFilter(saved.getId(), schemaDto.getCreatedUsername());
                    saved = notification.getProfileFilterDto();
                }
                filters.add(saved);
            }
        } catch (Exception e) {
            LOGGER.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations.");
        }
        return filters;
    }

    protected TenantDto generateTenant() {
        TenantDto tenant = new TenantDto();
        tenant.setName(TENANT_NAME);
        tenant = userService.saveTenant(tenant);
        return tenant;
    }

    protected TenantAdminDto generateTenantAdmin(TenantDto tenantDto, String userId) {
        TenantAdminDto tenant = new TenantAdminDto();
        tenant.setName(TENANT_NAME);
        if (tenantDto == null) {
            tenantDto = generateTenant();
        }
        tenant.setTenant(tenantDto);
        if (isBlank(userId)) {
            List<UserDto> users = generateUsers(tenantDto.getId(), KaaAuthorityDto.TENANT_ADMIN, 1);
            tenant.setUserId(users.get(0).getId());
        } else {
            tenant.setUserId(userId);
        }
        tenant.setExternalUid(UUID.randomUUID().toString());
        tenant = userService.saveTenantAdmin(tenant);
        return tenant;
    }

    protected List<UserDto> generateUsers(String tenantId, KaaAuthorityDto authority, int count) {
        List<UserDto> users = new ArrayList<>(count);
        UserDto userDto = null;
        for (int i = 0; i < count; i++) {
            userDto = new UserDto();
            userDto.setUsername(USER_NAME);
            userDto.setTenantId(tenantId);
            userDto.setExternalUid(UUID.randomUUID().toString());
            userDto.setAuthority(authority);
            userDto = userService.saveUser(userDto);
            users.add(userDto);
        }
        return users;
    }

    protected TopicDto generateTopic(String appId, TopicTypeDto type) {
        TopicDto topic = new TopicDto();
        topic.setName(TOPIC_NAME);
        if (isBlank(appId)) {
            appId = generateApplication().getId();
        }
        topic.setApplicationId(appId);
        if (type == null) {
            type = TopicTypeDto.MANDATORY;
        }
        topic.setType(type);
        return topicService.saveTopic(topic);
    }

    protected NotificationSchemaDto generateNotificationSchema(String appId, NotificationTypeDto type) {
        NotificationSchemaDto schema = new NotificationSchemaDto();
        if (isBlank(appId)) {
            appId = generateApplication().getId();
        }
        schema.setApplicationId(appId);
        schema.setName(NOTIFICATION_SCHEMA_NAME);
        String schemaBody= null;
        try {
            schemaBody = readSchemaFileAsString("dao/schema/testBaseSchema.json");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        schema.setSchema(schemaBody);
        schema.setType(type != null ? type : NotificationTypeDto.USER);
        return notificationService.saveNotificationSchema(schema);
    }

    protected List<NotificationDto> generateNotifications(String schemaId, String topicId, int count, NotificationTypeDto type) {
        List<NotificationDto> notifications = new ArrayList<>(count);
        NotificationDto notification = null;
        NotificationSchemaDto schema;
        for (int i = 0; i < count; i++) {
            notification = new NotificationDto();
            if (isBlank(schemaId)) {
                schema = generateNotificationSchema(null, NotificationTypeDto.USER);
            } else {
                schema = notificationService.findNotificationSchemaById(schemaId);
            }
            notification.setApplicationId(schema.getApplicationId());
            notification.setSchemaId(schema.getId());
            if (isBlank(topicId)) {
                topicId = generateTopic(schema.getApplicationId(), null).getId();
            }
            notification.setTopicId(topicId);
            notification.setType(type != null ? type : NotificationTypeDto.USER);
            byte[] body = null;
            try {
                body = readSchemaFileAsString("dao/schema/testBaseData.json").getBytes(Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            notification.setBody(body);
            UpdateNotificationDto<NotificationDto> update = notificationService.saveNotification(notification);
            notifications.add(update.getPayload());
        }
        return notifications;
    }

    protected EndpointNotificationDto generateUnicastNotification(String schemaId, String topicId, byte[] keyHash) {
        EndpointNotificationDto endpointNotification = new EndpointNotificationDto();
        endpointNotification.setEndpointKeyHash(keyHash);
        NotificationDto notification = new NotificationDto();
        NotificationSchemaDto schema;
        if (isBlank(schemaId)) {
            schema = generateNotificationSchema(null, NotificationTypeDto.USER);
        } else {
            schema = notificationService.findNotificationSchemaById(schemaId);
        }
        if (isBlank(topicId)) {
            topicId = generateTopic(schema.getApplicationId(), null).getId();
        }
        notification.setTopicId(topicId);
        notification.setApplicationId(schema.getApplicationId());
        notification.setSchemaId(schema.getId());
        notification.setType(NotificationTypeDto.USER);
        byte[] body = null;
        try {
            body = readSchemaFileAsString("dao/schema/testBaseData.json").getBytes(Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        notification.setBody(body);
        endpointNotification.setNotificationDto(notification);
        UpdateNotificationDto<EndpointNotificationDto> update = notificationService.saveUnicastNotification(endpointNotification);
        return update.getPayload();
    }

    protected EndpointProfileDto generateEndpointprofile(String appId, List<String> topicIds) {
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setSubscriptions(topicIds);
        profileDto.setEndpointKeyHash("TEST_KEY_HASH".getBytes());
        return endpointProfileDao.save(new EndpointProfile(profileDto)).toDto();
    }
}
