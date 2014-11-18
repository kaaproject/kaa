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

package org.kaaproject.kaa.server.control;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDataStruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;
import org.kaaproject.kaa.common.dto.logs.avro.HostInfoDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicSystemNotification;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.sql.H2DBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.sql.PostgreDBTestRunner;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.DB;

/**
 * The Class AbstractTestControlServer.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(AbstractTestControlServer.class);

    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 10090;

    /** The Constant TENANT. */
    protected static final String TENANT = "Tenant";

    /** The Constant USER. */
    protected static final String USER = "User";

    /** The Constant APPLICATION. */
    protected static final String APPLICATION = "Application";

    /** The Constant ENDPOINT_GROUP. */
    protected static final String ENDPOINT_GROUP = "EndpointGroup";

    /** The Constant EVENT_CLASS_FAMILY. */
    protected static final String EVENT_CLASS_FAMILY = "EventClassFamily";

    /** The Constant EVENT_CLASS_FAMILY_NAMESPACE. */
    protected static final String EVENT_CLASS_FAMILY_NAMESPACE = "org.kaaproject.event";

    /** The Constant EVENT_CLASS_FAMILY_CLASS_NAME. */
    protected static final String EVENT_CLASS_FAMILY_CLASS_NAME = "DefaultEventClassFamily";

    /** The Constant TEST_CONFIG_SCHEMA. */
    protected static final String TEST_CONFIG_SCHEMA = "data/testConfigSchema.json";

    /** The Constant TEST_INVALID_CONFIG_SCHEMA. */
    protected static final String TEST_INVALID_CONFIG_SCHEMA = "data/testInvalidConfigSchema.json";

    /** The Constant TEST_CONFIG_SCHEMA_UPDATED. */
    protected static final String TEST_CONFIG_SCHEMA_UPDATED = "data/testConfigSchemaUpdated.json";

    /** The Constant TEST_PROFILE_SCHEMA. */
    protected static final String TEST_PROFILE_SCHEMA = "data/testProfileSchema.json";

    /** The Constant TEST_PROFILE_SCHEMA_UPDATED. */
    protected static final String TEST_PROFILE_SCHEMA_UPDATED = "data/testProfileSchemaUpdated.json";

    /** The Constant TEST_USER_NOTIFICATION_SCHEMA. */
    protected static final String TEST_USER_NOTIFICATION_SCHEMA = "data/testUserNotificationSchema.json";

    /** The Constant TEST_PROFILE_FILTER. */
    protected static final String TEST_PROFILE_FILTER = "data/testProfileFilter.json";

    /** The Constant TEST_PROFILE_FILTER_UPDATED. */
    protected static final String TEST_PROFILE_FILTER_UPDATED = "data/testProfileFilterUpdated.json";

    /** The Constant TEST_CONFIGURATION. */
    protected static final String TEST_CONFIGURATION = "data/testConfig.json";

    /** The Constant TEST_CONFIGURATION_UPDATED. */
    protected static final String TEST_CONFIGURATION_UPDATED = "data/testConfigUpdated.json";

    /** The Constant TOPIC. */
    protected static final String TOPIC = "Topic";

    /** The Constant TEST_EVENT_CLASS_FAMILY_SCHEMA. */
    protected static final String TEST_EVENT_CLASS_FAMILY_SCHEMA = "data/testEventClassFamilySchema.json";

    /** External id of the test Endpoint User */
    protected static final String ENDPOINT_USER_EXTERNAL_ID = "Generated Test Endpoint User External Id";

    /** Name of the test Endpoint User */
    protected static final String ENDPOINT_USER_NAME = "Generated Test Endpoint User Name";

    /** The Constant TEST_LOG_SCHEMA. */
    protected static final String TEST_LOG_SCHEMA = "data/testLogSchema.json";

    /** The control service. */
    @Autowired
    private ControlService controlService;

    /** The client. */
    protected ControlThriftService.Client client;

    /** The transport. */
    private TTransport transport;

    private final Random random = new Random();

    @Autowired
    private DataSource dataSource;

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    /**
     * After.
     *
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void after() throws Exception {
        TestCluster.stop();
        MongoDBTestRunner.getDB().dropDatabase();
        MongoDBTestRunner.tearDown();
    }

    /**
     * Before test.
     *
     * @throws Exception the exception
     */
    @Before
    public void beforeTest() throws Exception {
        TestCluster.checkStarted(controlService);
        boolean connected = connect(3, 1000);
        Assert.assertTrue(connected);
    }

    /**
     * After test.
     *
     * @throws Exception the exception
     */
    @After
    public void afterTest() throws Exception {
        transport.close();
        clearDBData();
    }

    protected void clearDBData() {
        logger.info("Deleting data from MongoDB database");
        DB db = MongoDBTestRunner.getDB();
        if (db != null) {
            db.dropDatabase();
        }
        try {
            String url;
            try (Connection connection = dataSource.getConnection()) {
                url = connection.getMetaData().getURL();
            }
            if (url.contains("h2")) {
                logger.info("Deleting data from H2 database");
                new H2DBTestRunner().truncateTables(dataSource);
            } else {
                logger.info("Deleting data from PostgreSQL database");
                new PostgreDBTestRunner().truncateTables(dataSource);
            }
        } catch (SQLException ex) {
            logger.error("Can't delete data from databases.", ex);
        }
    }

    private boolean connect(int retryAmount, long retryDelay) {
        boolean connected = false;
        logger.info("Connecting to ControlService on {}:{} ...", HOST, PORT);
        transport = new TSocket(HOST, PORT);
        TProtocol protocol = new TBinaryProtocol(transport);
        client = new ControlThriftService.Client(protocol);
        for (int i=0;i<retryAmount;i++) {
            try {
                transport.open();
                connected = true;
                logger.info("Connected to ControlService on {}:{} !", HOST, PORT);
                break;
            } catch (TTransportException e) {
                logger.info("Failed to connect to ControlService on {}:{} !", PORT, PORT);
                if (i<retryAmount-1) {
                    logger.info("Sleeping for {} msec before next connect attempt...", retryDelay);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {}
                }
                else {
                    logger.info("Reached maximum connect retry amount {}. Aborting...", retryAmount);
                }
            }
        }
        return connected;
    }


    /**
     * Generate string.
     *
     * @param string
     *            the string
     * @return the string
     */
    protected static String generateString(String string) {
        return string + "_" + UUID.randomUUID().toString();
    }

    /**
     * Str is empty.
     *
     * @param str
     *            the str
     * @return true, if successful
     */
    protected static boolean strIsEmpty(String str) {
        return str == null || str.trim().equals("");
    }

    /**
     * Gets the resource as string.
     *
     * @param path the path
     * @return the resource as string
     * @throws IOException Signals that an I/O exception has occurred.
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

    /**
     * The Class IdComparator.
     */
    protected class IdComparator implements Comparator<HasId> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(HasId o1, HasId o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }

    /**
     * Creates the tenant.
     *
     * @return the tenant dto
     * @throws TException the t exception
     */
    protected TenantDto createTenant() throws TException {
        TenantDto tenant = new TenantDto();
        tenant.setName(generateString(TENANT));
        TenantDto savedTenant = toDto(client
                .editTenant(toDataStruct(tenant)));
        return savedTenant;
    }

    /**
     * Creates the tenant admin.
     *
     * @return the tenantAdmin dto
     * @throws TException the t exception
     */
    protected TenantAdminDto createTenantAdmin() throws TException {
        TenantAdminDto tenantAdmin = new TenantAdminDto();
        tenantAdmin.setExternalUid(UUID.randomUUID().toString());
        tenantAdmin.setUsername(generateString("user"));
        TenantDto tenant = new TenantDto();
        tenant.setName(generateString(TENANT));
        tenantAdmin.setTenant(tenant);
        TenantAdminDto savedTenantAdmin = toDto(client
                .editTenantAdmin(toDataStruct(tenantAdmin)));
        return savedTenantAdmin;
    }

    /**
     * Creates the user.
     *
     * @param authority the authority
     * @return the application dto
     * @throws TException the t exception
     */
    protected UserDto createUser(KaaAuthorityDto authority) throws TException {
        return createUser(null, authority);
    }

    /**
     * Creates the user.
     *
     * @param tenantId the tenant id
     * @param authority the authority
     * @return the user dto
     * @throws TException the t exception
     */
    protected UserDto createUser(String tenantId, KaaAuthorityDto authority) throws TException {
        UserDto user = new UserDto();
        user.setExternalUid(UUID.randomUUID().toString());
        user.setUsername(generateString("user"));
        user.setAuthority(authority);
        if (strIsEmpty(tenantId)) {
            TenantDto tenant = createTenant();
            user.setTenantId(tenant.getId());
        }
        else {
            user.setTenantId(tenantId);
        }
        UserDto savedUser = toDto(client
                .editUser(toDataStruct(user)));
        return savedUser;
    }

    /**
     * Creates the application.
     *
     * @return the application dto
     * @throws TException the t exception
     */
    protected ApplicationDto createApplication() throws TException {
        return createApplication(null);
    }

    /**
     * Creates the application for tenant.
     *
     * @param tenantId the tenant id
     * @return the application dto
     * @throws TException the t exception
     */
    protected ApplicationDto createApplication(String tenantId) throws TException {
        ApplicationDto application = new ApplicationDto();
        application.setName(generateString(APPLICATION));
        if (strIsEmpty(tenantId)) {
            TenantDto tenant = createTenant();
            application.setTenantId(tenant.getId());
        }
        else {
            application.setTenantId(tenantId);
        }
        ApplicationDto savedApplication = toDto(client
                .editApplication(toDataStruct(application)));
        return savedApplication;
    }

    /**
     * Creates the configuration schema.
     *
     * @return the configuration schema dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ConfigurationSchemaDto createConfigurationSchema() throws TException, IOException {
        return createConfigurationSchema(null);
    }

    /**
     * Creates the configuration schema for application.
     *
     * @param applicationId the application id
     * @return the configuration schema dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ConfigurationSchemaDto createConfigurationSchema(String applicationId) throws TException, IOException {
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setStatus(UpdateStatus.ACTIVE);
        DataSchema schema = new KaaSchemaFactoryImpl().createDataSchema(getResourceAsString(TEST_CONFIG_SCHEMA));
        configurationSchema.setSchema(schema.getRawSchema());
        configurationSchema.setName(generateString("Test Schema"));
        configurationSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            configurationSchema.setApplicationId(application.getId());
        }
        else {
            configurationSchema.setApplicationId(applicationId);
        }
        ConfigurationSchemaDto savedConfigurationSchema = toDto(client
                .editConfigurationSchema(toDataStruct(configurationSchema)));
        return savedConfigurationSchema;
    }


    /**
     * Creates the profile schema.
     *
     * @return the profile schema dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ProfileSchemaDto createProfileSchema() throws TException, IOException {
        return createProfileSchema(null);
    }

    /**
     * Creates the profile schema for application.
     *
     * @param applicationId the application id
     * @return the profile schema dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ProfileSchemaDto createProfileSchema(String applicationId) throws TException, IOException {
        ProfileSchemaDto profileSchema = new ProfileSchemaDto();
        DataSchema schema = new KaaSchemaFactoryImpl().createDataSchema(getResourceAsString(TEST_PROFILE_SCHEMA));
        profileSchema.setSchema(schema.getRawSchema());
        profileSchema.setName(generateString("Test Schema"));
        profileSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            profileSchema.setApplicationId(application.getId());
        }
        else {
            profileSchema.setApplicationId(applicationId);
        }
        ProfileSchemaDto savedProfileSchema = toDto(client
                .editProfileSchema(toDataStruct(profileSchema)));
        return savedProfileSchema;
    }

    /**
     * Creates the endpoint group.
     *
     * @return the endpoint group dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected EndpointGroupDto createEndpointGroup() throws TException, IOException {
        return createEndpointGroup(null);
    }

    /**
     * Creates the endpoint group for application.
     *
     * @param applicationId the application id
     * @return the endpoint group dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected EndpointGroupDto createEndpointGroup(String applicationId) throws TException, IOException {
        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setName(generateString(ENDPOINT_GROUP));
        endpointGroup.setWeight(random.nextInt());
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            endpointGroup.setApplicationId(application.getId());
        }
        else {
            endpointGroup.setApplicationId(applicationId);
        }
        EndpointGroupDto savedEndpointGroup = toDto(client
                .editEndpointGroup(toDataStruct(endpointGroup)));
        return savedEndpointGroup;
    }

    /**
     * Creates the profile filter.
     *
     * @return the profile filter dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ProfileFilterDto createProfileFilter() throws TException, IOException {
        return createProfileFilter(null, null);
    }

    /**
     * Creates the profile filter for profile schema.
     *
     * @param profileSchemaId the profile schema id
     * @param endpointGroupId       the endpoint group id
     * @return the profile filter dto
     * @throws TException  the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ProfileFilterDto createProfileFilter(String profileSchemaId, String endpointGroupId) throws TException, IOException {
        ApplicationDto application = createApplication();
        return createProfileFilter(profileSchemaId, endpointGroupId, application.getId());
    }

    /**
     * Creates the profile filter for profile schema.
     *
     * @param profileSchemaId the profile schema id
     * @param endpointGroupId the endpoint group id
     * @param applicationId       the application id
     * @return the profile filter dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ProfileFilterDto createProfileFilter(String profileSchemaId, String endpointGroupId, String applicationId) throws TException, IOException {
        ProfileFilterDto profileFilter = new ProfileFilterDto();
        String filter = getResourceAsString(TEST_PROFILE_FILTER);
        profileFilter.setBody(filter);
        profileFilter.setStatus(UpdateStatus.INACTIVE);
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            profileFilter.setApplicationId(application.getId());
            applicationId = application.getId();
        } else {
            profileFilter.setApplicationId(applicationId);
        }

        if (strIsEmpty(profileSchemaId)) {
            ProfileSchemaDto profileSchema = createProfileSchema(applicationId);
            profileFilter.setSchemaId(profileSchema.getId());
        }
        else {
            profileFilter.setSchemaId(profileSchemaId);
        }
        if (strIsEmpty(endpointGroupId)) {
            EndpointGroupDto endpointGroup = createEndpointGroup(applicationId);
            profileFilter.setEndpointGroupId(endpointGroup.getId());
        }
        else {
            profileFilter.setEndpointGroupId(endpointGroupId);
        }
        ProfileFilterDto savedProfileFilter = toDto(client
                .editProfileFilter(toDataStruct(profileFilter)));
        return savedProfileFilter;
    }

    /**
     * Creates the configuration.
     *
     * @return the configuration dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ConfigurationDto createConfiguration() throws TException, IOException {
        return createConfiguration(null, null);
    }

    /**
     * Creates the configuration for configuration schema.
     *
     * @param configurationSchemaId the configuration schema id
     * @param endpointGroupId       the endpoint group id
     * @return the configuration dto
     * @throws TException  the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ConfigurationDto createConfiguration(String configurationSchemaId, String endpointGroupId) throws TException, IOException {
        ApplicationDto application = createApplication();
        return createConfiguration(configurationSchemaId, endpointGroupId, application.getId());
    }

    /**
     * Creates the configuration for configuration schema.
     *
     * @param configurationSchemaId the configuration schema id
     * @param endpointGroupId       the endpoint group id
     * @param applicationId       the application id
     * @return the configuration dto
     * @throws TException  the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ConfigurationDto createConfiguration(String configurationSchemaId, String endpointGroupId, String applicationId) throws TException, IOException {
        ConfigurationDto configuration = new ConfigurationDto();
        String config = getResourceAsString(TEST_CONFIGURATION);

//        configuration.setBody(config.getBytes());
        configuration.setStatus(UpdateStatus.INACTIVE);
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            configuration.setApplicationId(application.getId());
            applicationId = application.getId();
        } else {
            configuration.setApplicationId(applicationId);
        }

        ConfigurationSchemaDto configSchema = null;
        if (strIsEmpty(configurationSchemaId)) {
            configSchema = createConfigurationSchema(applicationId);
            configuration.setSchemaId(configSchema.getId());
        } else {
            configuration.setSchemaId(configurationSchemaId);
            configSchema = toDto(client.getConfigurationSchema(configurationSchemaId));
        }
        if (strIsEmpty(endpointGroupId)) {
            EndpointGroupDto endpointGroup = createEndpointGroup(applicationId);
            configuration.setEndpointGroupId(endpointGroup.getId());
        } else {
            configuration.setEndpointGroupId(endpointGroupId);
        }
        GenericAvroConverter converter = new GenericAvroConverter(configSchema.getOverrideSchema());
        configuration.setBody(converter.endcodeToJson(converter.decodeJson(config)));
        ConfigurationDto savedConfiguration = toDto(client
                .editConfiguration(toDataStruct(configuration)));
        return savedConfiguration;
    }

    /**
     * Creates the topic.
     *
     * @param appId the app id
     * @param type the type
     * @return the user dto
     * @throws TException the t exception
     */
    protected TopicDto createTopic(String appId, TopicTypeDto type) throws TException {
        TopicDto topicDto = new TopicDto();
        topicDto.setName(generateString(TOPIC));
        topicDto.setType(type);
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication();
            topicDto.setApplicationId(applicationDto.getId());
        } else {
            topicDto.setApplicationId(appId);
        }
        TopicDto savedTopic = toDto(client
                .editTopic(toDataStruct(topicDto)));
        return savedTopic;
    }

    /**
     * Creates the notification schema.
     *
     * @param appId the app id
     * @param type the type
     * @return the user dto
     * @throws TException the t exception
     */
    protected NotificationSchemaDto createNotificationSchema(String appId, NotificationTypeDto type) throws TException {
        NotificationSchemaDto notificationSchema = new NotificationSchemaDto();
        notificationSchema.setSchema(new KaaSchemaFactoryImpl().createDataSchema(BasicSystemNotification.SCHEMA$.toString()).getRawSchema());
        notificationSchema.setType(type);
        notificationSchema.setName(generateString("Test Schema"));
        notificationSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication();
            notificationSchema.setApplicationId(applicationDto.getId());
        } else {
            notificationSchema.setApplicationId(appId);
        }
        NotificationSchemaDto savedSchema = toDto(client
                .editNotificationSchema(toDataStruct(notificationSchema)));
        return savedSchema;
    }

    /**
     * Creates the user notification schema.
     *
     * @param appId the app id
     * @return the notification schema dto
     * @throws TException the t exception
     * @throws IOException the IO exception
     */
    protected NotificationSchemaDto createUserNotificationSchema(String appId) throws TException, IOException {
        NotificationSchemaDto notificationSchema = new NotificationSchemaDto();
        DataSchema schema = new KaaSchemaFactoryImpl().createDataSchema(getResourceAsString(TEST_USER_NOTIFICATION_SCHEMA));
        notificationSchema.setSchema(schema.getRawSchema());
        notificationSchema.setType(NotificationTypeDto.USER);
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication();
            notificationSchema.setApplicationId(applicationDto.getId());
        } else {
            notificationSchema.setApplicationId(appId);
        }
        NotificationSchemaDto savedSchema = toDto(client
                .editNotificationSchema(toDataStruct(notificationSchema)));
        return savedSchema;
    }

    /**
     * Creates the log schema.
     *
     * @return the log schema dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected LogSchemaDto createLogSchema() throws TException, IOException {
        return createLogSchema(null);
    }

    /**
     * Creates the log schema for application.
     *
     * @param applicationId the application id
     * @return the log schema dto
     * @throws TException the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected LogSchemaDto createLogSchema(String applicationId) throws TException, IOException {
        LogSchemaDto logSchema = new LogSchemaDto();
        DataSchema schema = new KaaSchemaFactoryImpl().createDataSchema(getResourceAsString(TEST_LOG_SCHEMA));
        logSchema.setSchema(schema.getRawSchema());
        logSchema.setName(generateString("Test Schema"));
        logSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            logSchema.setApplicationId(application.getId());
        }
        else {
            logSchema.setApplicationId(applicationId);
        }
        LogSchemaDto savedLogSchema = toDto(client
                .editLogSchema(toDataStruct(logSchema)));
        return savedLogSchema;
    }

    /**
     * Creates the log appender for application.
     *
     * @param applicationId
     *            the application id
     * @return the log schema dto
     * @throws TException
     *             the t exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected LogAppenderDto createLogAppender(ApplicationDto application, LogSchemaDto schema, LogAppenderTypeDto type, FlumeBalancingTypeDto balancingTypeDto)
            throws TException, IOException {
        LogAppenderDto appender = new LogAppenderDto();
        appender.setName(generateString("Test Schema"));
        appender.setDescription(generateString("Test Desc"));

        if (application == null) {
            application = createApplication();
        }
        appender.setApplicationId(application.getId());
        appender.setApplicationToken(application.getApplicationToken());
        appender.setTenantId(application.getTenantId());

        if (schema == null) {
            schema = createLogSchema(application.getId());
        }
        appender.setSchema(schema);
        appender.setStatus(LogAppenderStatusDto.REGISTERED);
        LogAppenderParametersDto parametersDto = new LogAppenderParametersDto();
        if (type == null) {
            type = LogAppenderTypeDto.FILE;
        } else if (type.equals(LogAppenderTypeDto.FLUME)) {
            FlumeAppenderParametersDto flumeAppenderParametersDto = new FlumeAppenderParametersDto();
            flumeAppenderParametersDto.setBalancingType(balancingTypeDto != null ? balancingTypeDto : FlumeBalancingTypeDto.ROUND_ROBIN);
            flumeAppenderParametersDto.setHosts(Arrays.asList(new HostInfoDto("localhost", 12121, 0), new HostInfoDto("localhost", 12122, 0)));
            parametersDto.setParameters(flumeAppenderParametersDto);
        }

        appender.setProperties(parametersDto);

        LogAppenderDto savedLogAppender = toDto(client.editLogAppender(toDataStruct(appender)));
        return savedLogAppender;
    }


    /**
     * Creates the notification.
     *
     * @param appId the app id
     * @param schemaId the schema id
     * @param type the type
     * @return the user dto
     * @throws TException the t exception
     */
    protected NotificationDto createNotification(String appId, String schemaId, NotificationTypeDto type) throws TException {
        NotificationDto notification = new NotificationDto();
        notification.setBody("{\"notificationBody\":\"dummy\", \"systemNotificationParam1\":42, \"systemNotificationParam2\":43}".getBytes());
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication();
            notification.setApplicationId(applicationDto.getId());
        } else {
            notification.setApplicationId(appId);
        }
        if (strIsEmpty(schemaId)) {
            NotificationSchemaDto schema = createNotificationSchema(appId, type);
            notification.setSchemaId(schema.getId());
        } else {
            notification.setSchemaId(schemaId);
        }
        TopicDto topic = createTopic(appId, TopicTypeDto.MANDATORY);
        notification.setTopicId(topic.getId());

        NotificationDto savedNotification = toDto(client
                .editNotification(toDataStruct(notification)));
        return savedNotification;
    }

    /**
     * Creates the unicast notification.
     *
     * @param keyHash the key hash
     * @param appId the app id
     * @param schemaId the schema id
     * @param type the type
     * @return the user dto
     * @throws TException the t exception
     */
    protected EndpointNotificationDto createUnicastNotification(byte[] keyHash, String topicId, String appId, String schemaId, NotificationTypeDto type) throws TException {
        EndpointNotificationDto personaNotification = new EndpointNotificationDto();
        personaNotification.setEndpointKeyHash(keyHash);
        NotificationDto notification = new NotificationDto();
        notification.setTopicId(topicId);
        notification.setBody("{\"notificationBody\":\"dummy\", \"systemNotificationParam1\":42, \"systemNotificationParam2\":43}".getBytes());
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication();
            notification.setApplicationId(applicationDto.getId());
        } else {
            notification.setApplicationId(appId);
        }
        if (strIsEmpty(schemaId)) {
            NotificationSchemaDto schema = createNotificationSchema(appId, type);
            notification.setSchemaId(schema.getId());
        } else {
            notification.setSchemaId(schemaId);
        }
        personaNotification.setNotificationDto(notification);
        EndpointNotificationDto savedUnicast = toDto(client
                .editUnicastNotification(toDataStruct(personaNotification)));
        return savedUnicast;
    }

    /**
     * Creates the event class family.
     *
     * @return the event class family dto
     * @throws TException the t exception
     */
    protected EventClassFamilyDto createEventClassFamily() throws TException {
        return createEventClassFamily(null, null);
    }

    /**
     * Creates the event class family.
     *
     * @param tenantId the tenant id
     * @return the event class family dto
     * @throws TException the t exception
     */
    protected EventClassFamilyDto createEventClassFamily(String tenantId) throws TException {
        return createEventClassFamily(tenantId, null);
    }

    /**
     * Creates the event class family for tenant.
     *
     * @param tenantId the tenant id
     * @param classNameSuffix the class name suffix
     * @return the event class family dto
     * @throws TException the t exception
     */
    protected EventClassFamilyDto createEventClassFamily(String tenantId, String classNameSuffix) throws TException {
        EventClassFamilyDto eventClassFamily = new EventClassFamilyDto();
        eventClassFamily.setName(generateString(EVENT_CLASS_FAMILY));
        eventClassFamily.setNamespace(EVENT_CLASS_FAMILY_NAMESPACE);
        String className = EVENT_CLASS_FAMILY_CLASS_NAME;
        if (StringUtils.isNotBlank(classNameSuffix)) {
            className+=classNameSuffix;
        }
        eventClassFamily.setClassName(className);
        if (strIsEmpty(tenantId)) {
            TenantDto tenant = createTenant();
            eventClassFamily.setTenantId(tenant.getId());
        }
        else {
            eventClassFamily.setTenantId(tenantId);
        }
        EventClassFamilyDto savedEventClassFamily = toDto(client
                .editEventClassFamily(toDataStruct(eventClassFamily)));
        return savedEventClassFamily;
    }

    /**
     * Creates the application event family map.
     *
     * @return the application event family map dto
     * @throws TException the t exception
     * @throws IOException
     */
    protected ApplicationEventFamilyMapDto createApplicationEventFamilyMap() throws TException, IOException {
        return createApplicationEventFamilyMap(null, null, 1);
    }

    /**
     * Creates the application event family map.
     *
     * @param applicationId the application id
     * @param ecfId the event class family id
     * @return the application event family map dto
     * @throws TException the t exception
     * @throws IOException
     */
    protected ApplicationEventFamilyMapDto createApplicationEventFamilyMap(String applicationId, String ecfId, int version) throws TException, IOException {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = new ApplicationEventFamilyMapDto();
        String tenantId = null;
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication();
            tenantId = application.getTenantId();
            applicationEventFamilyMap.setApplicationId(application.getId());
        }
        else {
            applicationEventFamilyMap.setApplicationId(applicationId);
            ApplicationDto application = toDto(client.getApplication(applicationId));
            tenantId = application.getTenantId();
        }
        EventClassFamilyDto eventClassFamily = null;
        if (strIsEmpty(ecfId)) {
            eventClassFamily = createEventClassFamily(tenantId);
        }
        else {
            eventClassFamily = toDto(client.getEventClassFamily(ecfId));
        }
        applicationEventFamilyMap.setEcfId(eventClassFamily.getId());
        applicationEventFamilyMap.setEcfName(eventClassFamily.getName());
        if (eventClassFamily.getSchemas() == null || eventClassFamily.getSchemas().size()<version) {
            int start = eventClassFamily.getSchemas() == null ? 0 : eventClassFamily.getSchemas().size();
            for (int i=start;i<version;i++) {
                String schema = getResourceAsString(TEST_EVENT_CLASS_FAMILY_SCHEMA);
                client.addEventClassFamilySchema(eventClassFamily.getId(), schema, null);
            }
        }
        applicationEventFamilyMap.setVersion(version);
        List<EventClassDto> eventClasses = toDtoList(client.getEventClassesByFamilyIdVersionAndType(eventClassFamily.getId(), version, toGenericDataStruct(EventClassType.EVENT)));
        List<ApplicationEventMapDto> eventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
            eventMap.setAction(ApplicationEventAction.BOTH);
            eventMaps.add(eventMap);
        }
        applicationEventFamilyMap.setEventMaps(eventMaps);

        ApplicationEventFamilyMapDto savedApplicationEventFamilyMap = toDto(client
                .editApplicationEventFamilyMap(toDataStruct(applicationEventFamilyMap)));
        return savedApplicationEventFamilyMap;
    }

    /**
     * Assert schemas equals.
     *
     * @param schema the schema
     * @param storedSchema the stored schema
     */
    protected void assertSchemasEquals(SchemaDto schema, SchemaDto storedSchema) {
        Assert.assertEquals(schema.getId(), storedSchema.getId());
        Assert.assertEquals(schema.getMajorVersion(), storedSchema.getMajorVersion());
        Assert.assertEquals(schema.getMinorVersion(), storedSchema.getMinorVersion());
    }

    /**
     * Creates the endpoint user.
     *
     * @param tenantId tenant id
     * @return the endpoint user dto
     * @throws TException the t exception
     */
    protected EndpointUserDto createEndpointUser(String externalUID, String tenantId) throws TException {
        EndpointUserDto endpointUser = new EndpointUserDto();
        endpointUser.setExternalId(ENDPOINT_USER_EXTERNAL_ID + externalUID);
        endpointUser.setUsername(ENDPOINT_USER_NAME);
        endpointUser.setTenantId(tenantId);
        EndpointUserDto savedEndpointUser = toDto(client
                .editEndpointUser(toDataStruct(endpointUser)));
        return savedEndpointUser;
    }
}
