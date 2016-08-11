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

package org.kaaproject.kaa.server.common.dao;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Assert;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.BaseDataFactory;
import org.kaaproject.kaa.server.common.core.configuration.OverrideDataFactory;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;
import org.kaaproject.kaa.server.common.dao.exception.DatabaseProcessingException;
import org.kaaproject.kaa.server.common.dao.impl.LogAppenderDao;
import org.kaaproject.kaa.server.common.dao.impl.TenantDao;
import org.kaaproject.kaa.server.common.dao.impl.UserDao;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicDao;
import org.kaaproject.kaa.server.common.dao.impl.HistoryDao;
import org.kaaproject.kaa.server.common.dao.impl.EventClassFamilyDao;
import org.kaaproject.kaa.server.common.dao.impl.EventClassDao;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationEventFamilyMapDao;
import org.kaaproject.kaa.server.common.dao.impl.LogSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.UserVerifierDao;
import org.kaaproject.kaa.server.common.dao.impl.SdkProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.CTLSchemaMetaInfoDao;
import org.kaaproject.kaa.server.common.dao.impl.ServerProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.sql.H2DBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.sql.MariaDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.sql.PostgreDBTestRunner;
import org.kaaproject.kaa.server.common.dao.model.Notification;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.ApplicationEventFamilyMap;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.kaaproject.kaa.server.common.dao.model.sql.Configuration;
import org.kaaproject.kaa.server.common.dao.model.sql.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClass;
import org.kaaproject.kaa.server.common.dao.model.sql.EventClassFamily;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.History;
import org.kaaproject.kaa.server.common.dao.model.sql.LogAppender;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.NotificationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.ServerProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.kaaproject.kaa.server.common.dao.model.sql.User;
import org.kaaproject.kaa.server.common.dao.model.sql.UserVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"h2"})
public class AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    protected static final Random RANDOM = new Random(0);
    protected static final String SUPER_TENANT = "SuperTenant";

    protected static final String TENANT_NAME = "Generated Test Tenant";
    protected static final String USER_NAME = "Generated Test Username";
    protected static final String TOPIC_NAME = "Generated Topic Name";
    protected static final String NOTIFICATION_SCHEMA_NAME = "Generated Notification Schema Name";
    protected static final String ENDPOINT_USER_EXTERNAL_ID = "Generated Test Endpoint User External Id";
    protected static final String ENDPOINT_USER_NAME = "Generated Test Endpoint User Name";
    public static final String DEFAULT_FQN = "org.kaaproject.kaa.ctl.TestSchema";
    public static final String TEST_PROFILE_BODY_PATH = "dao/schema/testProfileBody.json";

    @Autowired
    protected DataSource dataSource;
    @Autowired
    protected LogSchemaService logSchemaService;
    @Autowired
    protected ApplicationService applicationService;
    @Autowired
    protected TopicService topicService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected UserVerifierService verifierService;
    @Autowired
    protected UserConfigurationService userConfigurationService;
    @Autowired
    protected EndpointService endpointService;
    @Autowired
    protected HistoryService historyService;
    @Autowired
    protected ProfileService profileService;
    @Autowired
    protected ConfigurationService configurationService;
    @Autowired
    protected NotificationService notificationService;
    @Autowired
    protected LogAppendersService logAppendersService;
    @Autowired
    protected CTLService ctlService;
    @Autowired
    protected ServerProfileService serverProfileService;

    @Autowired
    protected LogAppenderDao<LogAppender> appenderDao;
    @Autowired
    protected UserDao<User> userDao;
    @Autowired
    protected TenantDao<Tenant> tenantDao;
    @Autowired
    protected ApplicationDao<Application> applicationDao;
    @Autowired
    protected EndpointGroupDao<EndpointGroup> endpointGroupDao;
    @Autowired
    protected ConfigurationSchemaDao<ConfigurationSchema> configurationSchemaDao;
    @Autowired
    protected ConfigurationDao<Configuration> configurationDao;
    @Autowired
    protected ProfileSchemaDao<EndpointProfileSchema> profileSchemaDao;
    @Autowired
    protected ProfileFilterDao<ProfileFilter> profileFilterDao;
    @Autowired
    protected TopicDao<Topic> topicDao;
    @Autowired
    protected HistoryDao<History> historyDao;
    @Autowired
    protected EventClassFamilyDao<EventClassFamily> eventClassFamilyDao;
    @Autowired
    protected EventClassDao<EventClass> eventClassDao;
    @Autowired
    protected ApplicationEventFamilyMapDao<ApplicationEventFamilyMap> applicationEventFamilyMapDao;
    @Autowired
    protected LogSchemaDao<LogSchema> logSchemaDao;
    @Autowired
    protected NotificationSchemaDao<NotificationSchema> notificationSchemaDao;
    @Autowired(required = false)
    protected NotificationDao<Notification> notificationDao;
    @Autowired
    protected UserVerifierDao<UserVerifier> verifierDao;
    @Autowired
    protected SdkProfileDao<SdkProfile> sdkProfileDao;
    @Autowired
    protected CTLSchemaDao<CTLSchema> ctlSchemaDao;
    @Autowired
    protected CTLSchemaMetaInfoDao<CTLSchemaMetaInfo> ctlSchemaMetaInfoDao;
    @Autowired
    protected ServerProfileSchemaDao<ServerProfileSchema> serverProfileSchemaDao;

    protected Application application;

    public AbstractTest() {
    }

    protected String generateString(String string) {
        return string + "_" + UUID.randomUUID().toString();
    }

    protected void clearDBData() {
        try {
            String url;
            try (Connection connection = dataSource.getConnection()) {
                url = connection.getMetaData().getURL();
            }
            if (url.contains("h2")) {
                LOG.info("Deleting data from H2 database");
                new H2DBTestRunner().truncateTables(dataSource);
            } else if (url.contains("postgres")) {
                LOG.info("Deleting data from PostgreSQL database");
                new PostgreDBTestRunner().truncateTables(dataSource);
            } else {
                LOG.info("Deleting data from MariaDB database");
                new MariaDBTestRunner().truncateTables(dataSource);
            }
        } catch (SQLException ex) {
            LOG.error("Can't delete data from databases.", ex);
        }
    }

    protected String readSchemaFileAsString(String filePath) throws IOException {
        try {
            URI uri = this.getClass().getClassLoader().getResource(filePath).toURI();
            String[] array = uri.toString().split("!");
            Path path;
            if (array.length > 1) {
                LOG.info("Creating fs for {}", array[0]);
                FileSystem fs;
                try {
                    fs = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<String, String>());
                } catch (FileSystemAlreadyExistsException e) {
                    fs = FileSystems.getFileSystem(URI.create(array[0]));
                }
                path = fs.getPath(array[1]);
            } else {
                path = Paths.get(uri);
            }
            return new String(Files.readAllBytes(path));
        } catch (URISyntaxException e) {
            LOG.error("Can't generate configs {}", e);
        }
        return null;
    }

    protected ApplicationDto generateApplicationDto() {
        return generateApplicationDto(null);
    }

    protected ApplicationDto generateApplicationDto(String tenantId) {
        return generateApplicationDto(tenantId, null);
    }

    protected ApplicationDto generateApplicationDto(String tenantId, String appName) {
        ApplicationDto app = new ApplicationDto();
        if (isBlank(tenantId)) {
            app.setTenantId(generateTenantDto().getId());
        } else {
            app.setTenantId(tenantId);
        }
        if (!isBlank(appName)) {
            app.setName(appName);
        } else {
            app.setName("Test app");
        }
        return applicationService.saveApp(app);
    }

    protected EndpointGroupDto generateEndpointGroupDto(String appId) {
        return generateEndpointGroupDto(appId, generateString("GROUP_ALL"));
    }

    protected EndpointGroupDto generateEndpointGroupDto(String appId, String endpointGroupName) {
        EndpointGroupDto group = new EndpointGroupDto();
        if (isBlank(appId)) {
            appId = generateApplicationDto(null).getId();
        }
        group.setApplicationId(appId);
        group.setName(endpointGroupName);
        group.setWeight(RANDOM.nextInt());
        return endpointService.saveEndpointGroup(group);
    }

    protected List<ConfigurationSchemaDto> generateConfSchemaDto(String tenantId, String appId, int count) {
        List<ConfigurationSchemaDto> schemas = Collections.emptyList();
        try {
            if (isBlank(tenantId)) {
                tenantId = generateTenantDto().getId();
            }
            if (isBlank(appId)) {
                appId = generateApplicationDto().getId();
            }

            ConfigurationSchemaDto schemaDto;
            CTLSchemaDto ctlSchemaDto = ctlService.saveCTLSchema(generateCTLSchemaDto(tenantId));
            schemas = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                schemaDto = new ConfigurationSchemaDto();
                schemaDto.setApplicationId(appId);
                schemaDto.setCtlSchemaId(ctlSchemaDto.getId());
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setName("Test Name");
                schemaDto = configurationService.saveConfSchema(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }

        } catch (Exception e) {
            LOG.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configuration schemas." + e.getMessage());
        }
        return schemas;
    }


    protected List<ConfigurationDto> generateConfigurationDto(String schemaId, String groupId, int count, boolean activate,
                                                              boolean useBaseSchema) {
        List<ConfigurationDto> ids = Collections.emptyList();
        try {
            ConfigurationSchemaDto schemaDto;
            if (isNotBlank(schemaId)) {
                schemaDto = configurationService.findConfSchemaById(schemaId);
            } else {
                schemaDto = generateConfSchemaDto(null, null, 1).get(0);
            }
            Assert.assertNotNull(schemaDto);
            KaaSchema kaaSchema = useBaseSchema ? new BaseSchema(schemaDto.getBaseSchema()) : new OverrideSchema(
                    schemaDto.getOverrideSchema());
            DefaultRecordGenerationAlgorithmImpl configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(kaaSchema,
                    useBaseSchema ? new BaseDataFactory() : new OverrideDataFactory());
            ids = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ConfigurationDto dto = new ConfigurationDto();
                dto.setId(null);
                dto.setStatus(null);
                dto.setBody(configurationProcessor.getRootData().getRawData());
                dto.setSchemaId(schemaDto.getId());
                if (isBlank(groupId)) {
                    groupId = generateEndpointGroupDto(schemaDto.getApplicationId()).getId();
                }
                dto.setEndpointGroupId(groupId);
                ConfigurationDto saved = configurationService.saveConfiguration(dto);
                Assert.assertNotNull(saved);
                if (activate) {
                    ChangeConfigurationNotification notification = configurationService.activateConfiguration(saved.getId(),
                            schemaDto.getCreatedUsername());
                    saved = notification.getConfigurationDto();
                }
                ids.add(saved);
            }
        } catch (Exception e) {
            LOG.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations. " + e.getMessage());
        }
        return ids;
    }

    protected List<EndpointProfileSchemaDto> generateProfSchemaDto(String tenantId, String appId, int count) {
        List<EndpointProfileSchemaDto> schemas = Collections.emptyList();
        try {
            if (isBlank(tenantId)) {
                tenantId = generateTenantDto().getId();
            }
            if (isBlank(appId)) {
                appId = generateApplicationDto(tenantId).getId();
            }
            EndpointProfileSchemaDto schemaDto;
            CTLSchemaDto ctlSchemaDto = ctlService.saveCTLSchema(generateCTLSchemaDto(tenantId));
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new EndpointProfileSchemaDto();
                schemaDto.setApplicationId(appId);
                schemaDto.setCtlSchemaId(ctlSchemaDto.getId());
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setName("Test Name");
                schemaDto = profileService.saveProfileSchema(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOG.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations.");
        }
        return schemas;
    }

    protected List<ProfileFilterDto> generateFilterDto(String schemaId, String serverSchemaId, String groupId, int count, boolean activate) {
        List<ProfileFilterDto> filters = Collections.emptyList();
        try {
            EndpointProfileSchemaDto schemaDto;
            if (isBlank(schemaId)) {
                schemaDto = generateProfSchemaDto(null, null, 1).get(0);
            } else {
                schemaDto = profileService.findProfileSchemaById(schemaId);
            }
            ApplicationDto app = applicationService.findAppById(schemaDto.getApplicationId());

            ServerProfileSchemaDto serverProfileSchemaDto;
            if (isBlank(serverSchemaId)) {
                serverProfileSchemaDto = generateServerProfileSchema(app.getId(), app.getTenantId());
            } else {
                serverProfileSchemaDto = serverProfileService.findServerProfileSchema(serverSchemaId);
            }

            filters = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ProfileFilterDto dto = new ProfileFilterDto();
                dto.setId(null);
                dto.setStatus(null);
                if (isBlank(groupId)) {
                    groupId = generateEndpointGroupDto(schemaDto.getApplicationId()).getId();
                }
                dto.setEndpointGroupId(groupId);
                dto.setEndpointProfileSchemaId(schemaDto.getId());
                dto.setEndpointProfileSchemaVersion(schemaDto.getVersion());
                dto.setServerProfileSchemaId(serverProfileSchemaDto.getId());
                dto.setServerProfileSchemaVersion(serverProfileSchemaDto.getVersion());
                dto.setApplicationId(schemaDto.getApplicationId());
                ProfileFilterDto saved = profileService.saveProfileFilter(dto);
                Assert.assertNotNull(saved);
                if (activate) {
                    ChangeProfileFilterNotification notification = profileService.activateProfileFilter(saved.getId(),
                            schemaDto.getCreatedUsername());
                    saved = notification.getProfileFilterDto();
                }
                filters.add(saved);
            }
        } catch (Exception e) {
            LOG.error("Can't generate configs {}", e);
            Assert.fail("Can't generate configurations.");
        }
        return filters;
    }

    protected TenantDto generateTenantDto() {
        return generateTenantDto(generateString(TENANT_NAME));
    }

    protected TenantDto generateTenantDto(String name) {
        TenantDto tn = new TenantDto();
        tn.setName(name);
        return userService.saveTenant(tn);
    }

    protected List<LogSchemaDto> generateLogSchemaDto(String appId, int count) {
        List<LogSchemaDto> schemas = Collections.emptyList();
        ApplicationDto app = null;
        try {
            if (isBlank(appId)) {
                app = generateApplicationDto();
                appId = app.getId();
            } else {
                app = applicationService.findAppById(appId);
            }
            LogSchemaDto schemaDto;
            schemas = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                schemaDto = new LogSchemaDto();
                schemaDto.setApplicationId(appId);
                CTLSchemaDto ctlSchema = null;
                try {
                    ctlSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(app.getTenantId()));
                } catch (DatabaseProcessingException e) {
                    ctlSchema = ctlService.getOrCreateEmptySystemSchema(USER_NAME);

                }
                schemaDto.setCtlSchemaId(ctlSchema.getId());
                schemaDto.setCreatedUsername("Test User");
                schemaDto.setName("Test Name");
                schemaDto = logSchemaService.saveLogSchema(schemaDto);
                Assert.assertNotNull(schemaDto);
                schemas.add(schemaDto);
            }
        } catch (Exception e) {
            LOG.error("Can't generate log schemas {}", e);
            Assert.fail("Can't generate log schemas.");
        }
        return schemas;
    }

    protected UserDto generateTenantAdmin(TenantDto tenantDto, String userId) {
        UserDto tenant = new UserDto();
        tenant.setUsername(generateString(TENANT_NAME));
        if (tenantDto == null) {
            tenantDto = generateTenantDto();
        }
        tenant.setTenantId(tenantDto.getId());
        if (isBlank(userId)) {
            List<UserDto> users = generateUsersDto(tenantDto.getId(), KaaAuthorityDto.TENANT_ADMIN, 1);
            tenant.setId(users.get(0).getId());
        } else {
            tenant.setId(userId);
        }
        tenant.setExternalUid(UUID.randomUUID().toString());
        tenant = userService.saveUser(tenant);
        return tenant;
    }


    protected UserVerifierDto generateUserVerifierDto(String appId, String verifierToken) {
        UserVerifierDto verifier = new UserVerifierDto();
        verifier.setName("GENERATED test Verifier");
        if (isBlank(appId)) {
            appId = generateApplicationDto().getId();
        }
        verifier.setApplicationId(appId);
        if (verifierToken == null) {
            verifierToken = "token";
        }
        verifier.setVerifierToken(verifierToken);
        return verifierService.saveUserVerifier(verifier);
    }

    protected List<UserDto> generateUsersDto(String tenantId, KaaAuthorityDto authority, int count) {
        List<UserDto> users = new ArrayList<>(count);
        UserDto userDto = null;
        for (int i = 0; i < count; i++) {
            userDto = new UserDto();
            userDto.setUsername(generateString(USER_NAME));
            userDto.setTenantId(tenantId);
            userDto.setExternalUid(UUID.randomUUID().toString());
            userDto.setAuthority(authority);
            userDto = userService.saveUser(userDto);
            users.add(userDto);
        }
        return users;
    }

    protected TopicDto generateTopicDto(String appId, TopicTypeDto type) {
        TopicDto topic = new TopicDto();
        topic.setName(TOPIC_NAME);
        if (isBlank(appId)) {
            appId = generateApplicationDto().getId();
        }
        topic.setApplicationId(appId);
        if (type == null) {
            type = TopicTypeDto.MANDATORY;
        }
        topic.setType(type);
        return topicService.saveTopic(topic);
    }

    protected NotificationSchemaDto generateNotificationSchemaDto(String appId, NotificationTypeDto type) {
        NotificationSchemaDto schema = new NotificationSchemaDto();
        ApplicationDto app = null;
        if (isBlank(appId)) {
            app = generateApplicationDto();
            appId = app.getId();
        } else {
            app = applicationService.findAppById(appId);
        }
        schema.setApplicationId(appId);
        schema.setName(NOTIFICATION_SCHEMA_NAME);
        schema.setType(type != null ? type : NotificationTypeDto.USER);
        CTLSchemaDto ctlSchema = null;
        try {
            ctlSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(app.getTenantId()));
        } catch (DatabaseProcessingException e) {
            ctlSchema = ctlService.getOrCreateEmptySystemSchema(USER_NAME);

        }
        schema.setCtlSchemaId(ctlSchema.getId());
        return notificationService.saveNotificationSchema(schema);
    }

    protected List<NotificationDto> generateNotificationsDto(String schemaId, String topicId, int count, NotificationTypeDto type) {
        List<NotificationDto> notifications = new ArrayList<>(count);
        NotificationDto notification = null;
        NotificationSchemaDto schema;
        for (int i = 0; i < count; i++) {
            notification = new NotificationDto();
            if (isBlank(schemaId)) {
                schema = generateNotificationSchemaDto(null, NotificationTypeDto.USER);
            } else {
                schema = notificationService.findNotificationSchemaById(schemaId);
            }
            notification.setApplicationId(schema.getApplicationId());
            notification.setSchemaId(schema.getId());
            if (isBlank(topicId)) {
                topicId = generateTopicDto(schema.getApplicationId(), null).getId();
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

    protected EndpointNotificationDto generateUnicastNotificationDto(String schemaId, String topicId, byte[] keyHash) {
        EndpointNotificationDto endpointNotification = new EndpointNotificationDto();
        endpointNotification.setEndpointKeyHash(keyHash);
        NotificationDto notification = new NotificationDto();
        NotificationSchemaDto schema;
        if (isBlank(schemaId)) {
            schema = generateNotificationSchemaDto(null, NotificationTypeDto.USER);
        } else {
            schema = notificationService.findNotificationSchemaById(schemaId);
        }
        if (isBlank(topicId)) {
            topicId = generateTopicDto(schema.getApplicationId(), null).getId();
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

    protected EndpointUserDto generateEndpointUserDto(String tenantId) {
        EndpointUserDto endpointUser = new EndpointUserDto();
        endpointUser.setExternalId(ENDPOINT_USER_EXTERNAL_ID + UUID.randomUUID().toString());
        endpointUser.setUsername(ENDPOINT_USER_NAME + UUID.randomUUID().toString());
        if (tenantId == null) {
            tenantId = UUID.randomUUID().toString();
        }
        endpointUser.setTenantId(tenantId);
        endpointUser = endpointService.saveEndpointUser(endpointUser);
        return endpointUser;
    }

    protected LogAppenderDto generateLogAppenderDto(String appId, String schemaId) {
        LogAppenderDto logAppender = null;
        ApplicationDto app = null;
        LogSchemaDto schema = null;
        if (isBlank(appId)) {
            app = generateApplicationDto();
            appId = app.getId();
        } else {
            app = applicationService.findAppById(appId);
        }
        if (isBlank(schemaId)) {
            schema = generateLogSchemaDto(appId, 1).get(0);
        }
        logAppender = new LogAppenderDto();
        logAppender.setApplicationId(appId);
        logAppender.setName("Generated Appender");
        int version = schema.getVersion();
        logAppender.setMinLogSchemaVersion(version);
        logAppender.setMaxLogSchemaVersion(version);
        logAppender.setTenantId(app.getTenantId());
        logAppender.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.values()));
        return logAppendersService.saveLogAppender(logAppender);
    }

    protected EndpointUserConfigurationDto generateEndpointUserConfigurationDto(EndpointUserDto endpointUser, ApplicationDto applicationDto,
                                                                                ConfigurationSchemaDto configurationSchema) {
        return generateEndpointUserConfigurationDto(endpointUser, applicationDto, configurationSchema, UUID.randomUUID().toString());
    }

    protected EndpointUserConfigurationDto generateEndpointUserConfigurationDto(EndpointUserDto endpointUser, ApplicationDto applicationDto,
                                                                                ConfigurationSchemaDto configurationSchema, String body) {
        return generateEndpointUserConfigurationDto(endpointUser, applicationDto, configurationSchema, body, false);
    }

    protected EndpointUserConfigurationDto generateEndpointUserConfigurationDto(EndpointUserDto endpointUser, ApplicationDto applicationDto,
                                                                                ConfigurationSchemaDto configurationSchema, String body, boolean isNullAppDto) {
        EndpointUserConfigurationDto configurationDto = new EndpointUserConfigurationDto();
        configurationDto.setBody(body);

        if (endpointUser == null) {
            endpointUser = generateEndpointUserDto(null);
        }
        if (!isNullAppDto) {
            if (applicationDto == null) {
                applicationDto = generateApplicationDto();
            }
            configurationDto.setAppToken(applicationDto.getApplicationToken());
        } else {
            return userConfigurationService.saveUserConfiguration(configurationDto);
        }
        configurationDto.setUserId(endpointUser.getId());

        if (configurationSchema == null) {
            configurationSchema = generateConfSchemaDto(null, applicationDto.getId(), 1).get(0);
        }
        configurationDto.setSchemaVersion(configurationSchema.getVersion());

        return userConfigurationService.saveUserConfiguration(configurationDto);
    }

    protected EndpointProfileDto generateEndpointProfileDto(String appId, List<String> topicIds) {
        if (isBlank(appId)) {
            appId = generateApplicationDto().getId();
        }
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setSubscriptions(topicIds);
        profileDto.setEndpointKeyHash("TEST_KEY_HASH".getBytes());
        profileDto.setServerProfileBody("{\"serverTitle\": \"SERVER_TEST\"}");
        try {
            profileDto.setClientProfileBody(readSchemaFileAsString(TEST_PROFILE_BODY_PATH));
        } catch (IOException e) {
            LOG.error("Can't set client-side EP body {}", e);
        }
        profileDto.setSdkToken(UUID.randomUUID().toString());
        return endpointService.saveEndpointProfile(profileDto);
    }

    protected EndpointProfileDto generateEndpointProfileDtoWithSchemaVersion(String appId, int schemaVersion, String srvProfileBody) {
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setServerProfileVersion(schemaVersion);
        profileDto.setEndpointKeyHash("TEST_KEY_HASH".getBytes());
        try {
            profileDto.setClientProfileBody(readSchemaFileAsString(TEST_PROFILE_BODY_PATH));
        } catch (IOException e) {
            LOG.error("Can't set client-side EP body {}", e);
        }
        profileDto.setSdkToken(UUID.randomUUID().toString());
        profileDto.setServerProfileBody(srvProfileBody);
        return endpointService.saveEndpointProfile(profileDto);
    }

    protected EndpointProfileDto generateEndpointProfileWithGroupIdDto(String endpointGroupId) {
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setEndpointKeyHash(generateString("TEST_KEY_HASH").getBytes());
        String appId = generateApplicationDto().getId();
        profileDto.setApplicationId(appId);
        List<EndpointGroupStateDto> groupState = new ArrayList<>();
        groupState.add(new EndpointGroupStateDto(endpointGroupId, null, null));
        profileDto.setGroupState(groupState);
        try {
            profileDto.setClientProfileBody(readSchemaFileAsString(TEST_PROFILE_BODY_PATH));
        } catch (IOException e) {
            LOG.error("Can't set client-side EP body {}", e);
        }
        profileDto.setServerProfileBody("{\"serverTitle\": \"SERVER_TEST\"}");
        profileDto.setSdkToken(UUID.randomUUID().toString());
        return endpointService.saveEndpointProfile(profileDto);
    }

    protected CTLSchemaDto generateCTLSchemaDto(String tenantId) {
        return generateCTLSchemaDto(DEFAULT_FQN, tenantId, null, 100);
    }

    protected CTLSchemaDto generateCTLSchemaDto(String fqn, String tenantId, String applicationId, int version) {
        CTLSchemaDto ctlSchema = new CTLSchemaDto();
        ctlSchema.setMetaInfo(new CTLSchemaMetaInfoDto(fqn, tenantId, applicationId));
        ctlSchema.setVersion(version);
        String name = fqn.substring(fqn.lastIndexOf(".") + 1);
        String namespace = fqn.substring(0, fqn.lastIndexOf("."));
        StringBuilder body = new StringBuilder("{\"type\": \"record\",");
        body = body.append("\"name\": \"").append(name).append("\",");
        body = body.append("\"namespace\": \"").append(namespace).append("\",");
        body = body.append("\"version\": ").append(version).append(",");
        body = body.append("\"dependencies\": [], \"fields\": []}");
        ctlSchema.setBody(body.toString());
        return ctlSchema;
    }

    protected String ctlRandomFqn() {
        return DEFAULT_FQN + RANDOM.nextInt(100000);
    }

    protected ServerProfileSchemaDto generateServerProfileSchema(String appId, String tenantId) {
        return generateServerProfileSchema(appId, tenantId, RANDOM.nextInt(100000));
    }

    protected ServerProfileSchemaDto generateServerProfileSchema(String appId, String tenantId, int version) {
        ServerProfileSchemaDto schemaDto = new ServerProfileSchemaDto();
        if (isBlank(tenantId)) {
            ApplicationDto applicationDto = generateApplicationDto();
            appId = applicationDto.getId();
            tenantId = applicationDto.getTenantId();
        }
        schemaDto.setApplicationId(appId);
        schemaDto.setCreatedTime(System.currentTimeMillis());

        CTLSchemaDto ctlSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(ctlRandomFqn(), tenantId, appId, version));
        schemaDto.setCtlSchemaId(ctlSchema.getId());
        return serverProfileService.saveServerProfileSchema(schemaDto);
    }

    protected List<String> getIds(List<? extends GenericModel> modelList) {
        List<String> ids = new ArrayList<>();
        for (GenericModel gm : modelList) {
            ids.add(gm.getStringId());
        }
        Collections.sort(ids);
        return ids;
    }

    protected List<String> getIdsDto(List<? extends HasId> hId) {
        List<String> ids = new ArrayList<>();
        for (HasId id : hId) {
            ids.add(id.getId());
        }
        Collections.sort(ids);
        return ids;
    }
}
