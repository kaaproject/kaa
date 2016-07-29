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

package org.kaaproject.kaa.server.control;

import com.mongodb.DB;
import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.kaaproject.avro.ui.shared.FqnVersion;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicSystemNotification;
import org.kaaproject.kaa.server.appenders.file.config.FileSystemAppenderConfig;
import org.kaaproject.kaa.server.common.admin.AdminClient;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.sql.H2DBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.sql.PostgreDBTestRunner;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.node.service.initialization.KaaNodeInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpStatusCodeException;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The Class AbstractTestControlServer.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractTestControlServer extends AbstractTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractTestControlServer.class);

    /** The Constant HOST. */
    private static final String HOST = "localhost";

    /** The Constant PORT. */
    private static final int PORT = 9080;

    /** The Constant DEFAULT_KAA_ADMIN_USER. */
    private static final String DEFAULT_KAA_ADMIN_USER = "kaa";

    /** The Constant DEFAULT_KAA_ADMIN_PASSWORD. */
    private static final String DEFAULT_KAA_ADMIN_PASSWORD = "kaa123";

    /** The Constant DEFAULT_TENANT_ADMIN_USER. */
    private static final String DEFAULT_TENANT_ADMIN_USER = "admin";

    /** The Constant DEFAULT_TENANT_ADMIN_PASSWORD. */
    private static final String DEFAULT_TENANT_ADMIN_PASSWORD = "admin123";

    /** The Constant DEFAULT_TENANT_DEVELOPER_USER. */
    private static final String DEFAULT_TENANT_DEVELOPER_USER = "devuser";

    /** The Constant DEFAULT_TENANT_DEVELOPER_PASSWORD. */
    private static final String DEFAULT_TENANT_DEVELOPER_PASSWORD = "devuser123";

    /** The Constant TENANT. */
    protected static final String TENANT = "Tenant";

    /** The Constant TENANT_ADMIN_USERNAME. */
    protected static final String TENANT_ADMIN_USERNAME = "TenantUsername";

    /** The Constant USERNAME. */
    protected static final String USERNAME = "Username";

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
    protected static final String TEST_CONFIG_SCHEMA = "control/data/testConfigSchema.json";

    /** The Constant TEST_INVALID_CONFIG_SCHEMA. */
    protected static final String TEST_INVALID_CONFIG_SCHEMA = "control/data/testInvalidConfigSchema.json";

    /** The Constant TEST_CONFIG_SCHEMA_UPDATED. */
    protected static final String TEST_CONFIG_SCHEMA_UPDATED = "control/data/testConfigSchemaUpdated.json";

    /** The Constant TEST_PROFILE_SCHEMA. */
    protected static final String TEST_PROFILE_SCHEMA = "control/data/testProfileSchema.json";

    /** The Constant TEST_PROFILE_SCHEMA_UPDATED. */
    protected static final String TEST_PROFILE_SCHEMA_UPDATED = "control/data/testProfileSchemaUpdated.json";

    /** The Constant TEST_USER_NOTIFICATION_SCHEMA. */
    protected static final String TEST_USER_NOTIFICATION_SCHEMA = "control/data/testUserNotificationSchema.json";

    /** The Constant TEST_PROFILE_FILTER. */
    protected static final String TEST_PROFILE_FILTER = "control/data/testProfileFilter.json";

    /** The Constant TEST_PROFILE_FILTER_UPDATED. */
    protected static final String TEST_PROFILE_FILTER_UPDATED = "control/data/testProfileFilterUpdated.json";

    /** The Constant TEST_CONFIGURATION. */
    protected static final String TEST_CONFIGURATION = "control/data/testConfig.json";

    /** The Constant TEST_CONFIGURATION_UPDATED. */
    protected static final String TEST_CONFIGURATION_UPDATED = "control/data/testConfigUpdated.json";

    /** The Constant TOPIC. */
    protected static final String TOPIC = "Topic";

    /** The Constant TEST_EVENT_CLASS_FAMILY_SCHEMA. */
    protected static final String TEST_EVENT_CLASS_FAMILY_SCHEMA = "control/data/testEventClassFamilySchema.json";

    /** The Constant ENDPOINT_USER_EXTERNAL_ID. */
    protected static final String ENDPOINT_USER_EXTERNAL_ID = "Generated Test Endpoint User External Id";

    /** The Constant ENDPOINT_USER_NAME. */
    protected static final String ENDPOINT_USER_NAME = "Generated Test Endpoint User Name";

    /** The Constant TEST_LOG_SCHEMA. */
    protected static final String TEST_LOG_SCHEMA = "control/data/testLogSchema.json";
    
    protected static final String CTL_DEFAULT_NAME = "name";
    
    protected static final String CTL_DEFAULT_NAMESPACE = "org.kaaproject.kaa";
    
    protected static final String CTL_DEFAULT_TYPE = "Type";
    
    /** The kaa admin user. */
    protected static String kaaAdminUser = DEFAULT_KAA_ADMIN_USER;

    /** The kaa admin password. */
    protected static String kaaAdminPassword = DEFAULT_KAA_ADMIN_PASSWORD;

    /** The tenant admin user. */
    protected static String tenantAdminUser = DEFAULT_TENANT_ADMIN_USER;

    /** The tenant admin password. */
    protected static String tenantAdminPassword = DEFAULT_TENANT_ADMIN_PASSWORD;

    /** The tenant developer user. */
    protected static String tenantDeveloperUser = DEFAULT_TENANT_DEVELOPER_USER;

    /** The tenant developer password. */
    protected static String tenantDeveloperPassword = DEFAULT_TENANT_DEVELOPER_PASSWORD;

    /** The tenant admin dto. */
    protected org.kaaproject.kaa.common.dto.admin.UserDto tenantAdminDto;

    /** The tenant developer dto. */
    protected UserDto tenantDeveloperDto;

    /** The kaa node initialization service. */
    @Autowired
    private KaaNodeInitializationService kaaNodeInitializationService;

    /** The client. */
    protected AdminClient client;

    /** The random. */
    private final Random random = new Random();

    /** The data source. */
    @Autowired
    private DataSource dataSource;

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    /**
     * After.
     *
     * @throws Exception the exception
     */
    @AfterClass
    public static void after() throws Exception {
        TestCluster.stop();
        MongoDBTestRunner.tearDown();
    }

    /**
     * Before test.
     *
     * @throws Exception the exception
     */
    @Before
    public void beforeTest() throws Exception {
        TestCluster.checkStarted(kaaNodeInitializationService);
        client = new AdminClient(HOST, PORT);
        createUsers();
    }

    /**
     * After test.
     *
     * @throws Exception the exception
     */
    @After
    public void afterTest() throws Exception {
        clearDBData();
    }

    /**
     * Clear db data.
     */
    protected void clearDBData() {
        LOG.info("Deleting data from MongoDB database");
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
                LOG.info("Deleting data from H2 database");
                new H2DBTestRunner().truncateTables(dataSource);
//                new H2DBTestRunner().truncateSequences(dataSource);
            } else {
                LOG.info("Deleting data from PostgreSQL database");
                new PostgreDBTestRunner().truncateTables(dataSource);
            }
        } catch (SQLException ex) {
            LOG.error("Can't delete data from databases.", ex);
        }
    }

    /**
     * Login kaa admin.
     *
     * @throws Exception the exception
     */
    protected void loginKaaAdmin() throws Exception {
        client.login(kaaAdminUser, kaaAdminPassword);
    }

    /**
     * Login tenant admin.
     *
     * @param username the username
     * @throws Exception the exception
     */
    protected void loginTenantAdmin(String username) throws Exception {
        client.login(username, tenantAdminPassword);
    }

    /**
     * Login tenant developer.
     *
     * @param username the username
     * @throws Exception the exception
     */
    protected void loginTenantDeveloper(String username) throws Exception {
        client.login(username, tenantDeveloperPassword);
    }

    /**
     * Creates the tenant admin needed.
     *
     * @return true, if successful
     */
    protected boolean createTenantAdminNeeded() {
        return true;
    }

    /**
     * Creates the tenant developer needed.
     *
     * @return true, if successful
     */
    protected boolean createTenantDeveloperNeeded() {
        return true;
    }

    /**
     * Creates the users.
     *
     * @throws Exception the exception
     */
    private void createUsers() throws Exception {
        LOG.info("Creating users...");
        client.createKaaAdmin(kaaAdminUser, kaaAdminPassword);
        if (createTenantAdminNeeded()) {
            tenantAdminDto = createTenantAdmin(tenantAdminUser);
            loginTenantAdmin(tenantAdminUser);
            if (createTenantDeveloperNeeded()) {
                tenantDeveloperDto = createTenantDeveloper(tenantDeveloperUser);
                loginTenantDeveloper(tenantDeveloperUser);
            }
        }
    }
//
//    /**
//     * Generate string.
//     *
//     * @param string the string
//     * @return the string
//     */
//    protected static String generateString(String string) {
//        return string + "_" + UUID.randomUUID().toString();
//    }

    /**
     * Str is empty.
     *
     * @param str the str
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
     * The Interface TestRestCall.
     */
    protected interface TestRestCall {

        /**
         * Execute rest call.
         *
         * @throws Exception the exception
         */
        void executeRestCall() throws Exception;

    }

    /**
     * Check not found.
     *
     * @param restCall the rest call
     * @throws Exception the exception
     */
    protected void checkNotFound(TestRestCall restCall) throws Exception {
        checkRestErrorStatusCode(restCall, HttpStatus.NOT_FOUND);
    }

    /**
     * Check bad request.
     *
     * @param restCall the rest call
     * @throws Exception the exception
     */
    protected void checkBadRequest(TestRestCall restCall) throws Exception {
        checkRestErrorStatusCode(restCall, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Check forbidden.
     *
     * @param restCall the rest call
     * @throws Exception the exception
     */
    protected void checkForbidden(TestRestCall restCall) throws Exception {
        checkRestErrorStatusCode(restCall, HttpStatus.FORBIDDEN);
    }
    
    protected void checkRestErrorStatusCode(TestRestCall restCall, HttpStatus expectedStatus) throws Exception {
        HttpStatus errorStatus = null;
        try {
            restCall.executeRestCall();
        } catch (HttpStatusCodeException e) {
            errorStatus = e.getStatusCode();
        }
        Assert.assertNotNull(errorStatus);
        Assert.assertEquals(expectedStatus, errorStatus);
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

    protected TenantDto createTenant() throws Exception{
        loginKaaAdmin();
        TenantDto tenantDto = new TenantDto();
        tenantDto.setName(generateString(TENANT_NAME));
       tenantDto= client.editTenant(tenantDto);
        return tenantDto;
    }



    /**
     * Creates the tenant.
     *
     * @return the tenant user dto
     * @throws Exception the exception
     */
    protected org.kaaproject.kaa.common.dto.admin.UserDto createTenantAdmin() throws Exception {
        return createTenantAdmin(null);
    }

    /**
     * Creates the tenant.
     *
     * @param username the username
     * @return the tenant user dto
     * @throws Exception the exception
     */
    protected org.kaaproject.kaa.common.dto.admin.UserDto createTenantAdmin(String username) throws Exception {
        loginKaaAdmin();
        if (username == null) {
            username = generateString(TENANT_ADMIN_USERNAME);
        }
            TenantDto tenantDto = new TenantDto();
            if(client.getTenants().isEmpty()) {
                tenantDto.setName(TENANT_NAME);
                tenantDto = client.editTenant(tenantDto);
            }else {
              for(TenantDto t :client.getTenants()){
                  if(t.getName().equals(TENANT_NAME))
                      tenantDto=t;
              }
            }

        org.kaaproject.kaa.common.dto.admin.UserDto tenAdmin = new org.kaaproject.kaa.common.dto.admin.UserDto();
        tenAdmin.setAuthority(KaaAuthorityDto.TENANT_ADMIN);

        tenAdmin.setUsername(username);
        tenAdmin.setMail(username + "@demoproject.org");
        tenAdmin.setFirstName("Tenant");
        tenAdmin.setLastName("Admin");
        tenAdmin.setTenantId(tenantDto.getId());

            tenAdmin = client.editUser(tenAdmin);

        if (StringUtils.isNotBlank(tenAdmin.getTempPassword())) {
            client.clearCredentials();
            client.changePassword(tenAdmin.getUsername(), tenAdmin.getTempPassword(), tenantAdminPassword);
        }


        return tenAdmin;
    }

    /**
     * Creates the tenant developer.
     *
     * @param username the username
     * @return the org.kaaproject.kaa.common.dto.admin. user dto
     * @throws Exception the exception
     */
    private org.kaaproject.kaa.common.dto.admin.UserDto createTenantDeveloper(String username) throws Exception {
        org.kaaproject.kaa.common.dto.admin.UserDto tenantDeveloper = new org.kaaproject.kaa.common.dto.admin.UserDto();
        tenantDeveloper.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);
        tenantDeveloper.setUsername(username);
        tenantDeveloper.setMail(username + "@demoproject.org");
        tenantDeveloper.setFirstName("Tenant");
        tenantDeveloper.setLastName("Developer");
        tenantDeveloper.setTenantId(tenantAdminDto.getTenantId());
        tenantDeveloper = client.editUser(tenantDeveloper);

        if (StringUtils.isNotBlank(tenantDeveloper.getTempPassword())) {
            client.clearCredentials();
            client.changePassword(tenantDeveloper.getUsername(), tenantDeveloper.getTempPassword(), tenantDeveloperPassword);
        }
        return tenantDeveloper;
    }

    /**
     * Creates the user.
     *
     * @param authority the authority
     * @return the user dto
     * @throws Exception the exception
     */
    protected UserDto createUser(KaaAuthorityDto authority) throws Exception {
        return createUser(null, authority);
    }

    /**
     * Creates the user.
     *
     * @param tenant the tenant
     * @param authority the authority
     * @return the user dto
     * @throws Exception the exception
     */
    protected UserDto createUser(org.kaaproject.kaa.common.dto.admin.UserDto tenant, KaaAuthorityDto authority) throws Exception {
        UserDto user = new UserDto();
        String username = generateString(USERNAME);
        user.setUsername(username);
        user.setMail(username + "@demoproject.org");
        user.setFirstName(generateString("Test"));
        user.setLastName(generateString("User"));
        user.setAuthority(authority);
        if (tenant == null) {
            tenant = createTenantAdmin();
        }
        user.setTenantId(tenantAdminDto.getTenantId());
        loginTenantAdmin(tenant.getUsername());
        UserDto savedUser = client.editUser(user);
        return savedUser;
    }

    /**
     * Creates the application.
     *
     * @return the application dto
     * @throws Exception the exception
     */
    protected ApplicationDto createApplication() throws Exception {
        return createApplication(null);
    }

    /**
     * Creates the application.
     *
     * @param tenant the tenant
     * @return the application dto
     * @throws Exception the exception
     */
    protected ApplicationDto createApplication(org.kaaproject.kaa.common.dto.admin.UserDto tenant) throws Exception {
        ApplicationDto application = new ApplicationDto();
        application.setName(generateString(APPLICATION));
        if (tenant == null) {
            tenant = createTenantAdmin();
        }
        loginTenantAdmin(tenant.getUsername());
        ApplicationDto savedApplication = client
                .editApplication(application);
        return savedApplication;
    }

    /**
     * Creates the configuration schema.
     *
     * @return the configuration schema dto
     * @throws Exception the exception
     */
    protected ConfigurationSchemaDto createConfigurationSchema() throws Exception {
        return createConfigurationSchema(null, null);
    }

    /**
     * Creates the configuration schema.
     *
     * @param applicationId the application id
     * @param ctlSchemaId
     * @return the configuration schema dto
     * @throws Exception the exception
     */
    protected ConfigurationSchemaDto createConfigurationSchema(String applicationId, String ctlSchemaId) throws Exception {
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setStatus(UpdateStatus.ACTIVE);
        configurationSchema.setName(generateString("Test Schema"));
        configurationSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            configurationSchema.setApplicationId(application.getId());
        }
        else {
            configurationSchema.setApplicationId(applicationId);
        }

        if (strIsEmpty(ctlSchemaId)) {
            CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null, null, null);
            configurationSchema.setCtlSchemaId(ctlSchema.getId());
        } else {
            configurationSchema.setCtlSchemaId(ctlSchemaId);
        }

        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        ConfigurationSchemaDto savedConfigurationSchema = client.saveConfigurationSchema(configurationSchema);
        return savedConfigurationSchema;
    }

    /**
     * Creates the profile schema.
     *
     * @return the profile schema dto
     * @throws Exception the exception
     */
    protected EndpointProfileSchemaDto createProfileSchema() throws Exception {
        return createEndpointProfileSchema(null, null);
    }

    /**
     * Creates the profile schema.
     *
     * @param applicationId the application id
     * @return the profile schema dto
     * @throws Exception the exception
     */
    protected EndpointProfileSchemaDto createEndpointProfileSchema(String applicationId, String ctlSchemaId) throws Exception {
        EndpointProfileSchemaDto profileSchema = new EndpointProfileSchemaDto();
        profileSchema.setName(generateString("Test Schema"));
        profileSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            profileSchema.setApplicationId(application.getId());
        }
        else {
            profileSchema.setApplicationId(applicationId);
        }
        if (strIsEmpty(ctlSchemaId)) {
            CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null, null, null);
            profileSchema.setCtlSchemaId(ctlSchema.getId());
        } else {
            profileSchema.setCtlSchemaId(ctlSchemaId);
        }
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        EndpointProfileSchemaDto savedProfileSchema = client
                .saveProfileSchema(profileSchema);
        return savedProfileSchema;
    }
    
    /**
     * Creates the profile schema.
     *
     * @param applicationId the application id
     * @return the profile schema dto
     * @throws Exception the exception
     */
    protected ServerProfileSchemaDto createServerProfileSchema(String applicationId, String ctlSchemaId) throws Exception {
        ServerProfileSchemaDto profileSchema = new ServerProfileSchemaDto();
        profileSchema.setName(generateString("Test Schema"));
        profileSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            profileSchema.setApplicationId(application.getId());
        }
        else {
            profileSchema.setApplicationId(applicationId);
        }
        if (strIsEmpty(ctlSchemaId)) {
            CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null, null, null);
            profileSchema.setCtlSchemaId(ctlSchema.getId());
        }
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        ServerProfileSchemaDto savedProfileSchema = client
                .saveServerProfileSchema(profileSchema);
        return savedProfileSchema;
    }


    /**
     * Creates the endpoint group.
     *
     * @return the endpoint group dto
     * @throws Exception the exception
     */
    protected EndpointGroupDto createEndpointGroup() throws Exception {
        return createEndpointGroup(null);
    }

    /**
     * Creates the endpoint group.
     *
     * @param applicationId the application id
     * @return the endpoint group dto
     * @throws Exception the exception
     */
    protected EndpointGroupDto createEndpointGroup(String applicationId) throws Exception {
        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setName(generateString(ENDPOINT_GROUP));
        endpointGroup.setWeight(Math.abs(random.nextInt()));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            endpointGroup.setApplicationId(application.getId());
        }
        else {
            endpointGroup.setApplicationId(applicationId);
        }
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        EndpointGroupDto savedEndpointGroup = client
                .editEndpointGroup(endpointGroup);
        return savedEndpointGroup;
    }

    /**
     * Creates the profile filter.
     *
     * @return the profile filter dto
     * @throws Exception the exception
     */
    protected ProfileFilterDto createProfileFilter() throws Exception {
        return createProfileFilter(null, null, null);
    }

    /**
     * Creates the profile filter.
     *
     * @param endpointPfSchemaId the profile schema id
     * @param endpointGroupId the endpoint group id
     * @return the profile filter dto
     * @throws Exception the exception
     */
    protected ProfileFilterDto createProfileFilter(String endpointPfSchemaId, String serverPfSchemaId, String endpointGroupId) throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        return createProfileFilter(endpointPfSchemaId, serverPfSchemaId, endpointGroupId, application.getId());
    }

    /**
     * Creates the profile filter.
     *
     * @param endpointPfSchemaId the profile schema id
     * @param serverPfSchemaId the profile schema id
     * @param endpointGroupId the endpoint group id
     * @param applicationId the application id
     * @return the profile filter dto
     * @throws Exception the exception
     */
    protected ProfileFilterDto createProfileFilter(String endpointPfSchemaId, String serverPfSchemaId, String endpointGroupId, String applicationId) throws Exception {
        ProfileFilterDto profileFilter = new ProfileFilterDto();
        String filter = getResourceAsString(TEST_PROFILE_FILTER);
        profileFilter.setBody(filter);
        profileFilter.setStatus(UpdateStatus.INACTIVE);
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            profileFilter.setApplicationId(application.getId());
            applicationId = application.getId();
        } else {
            profileFilter.setApplicationId(applicationId);
        }

        if (strIsEmpty(endpointPfSchemaId)) {
            EndpointProfileSchemaDto profileSchema = createEndpointProfileSchema(applicationId, null);
            profileFilter.setEndpointProfileSchemaId(profileSchema.getId());
        }
        else {
            profileFilter.setEndpointProfileSchemaId(endpointPfSchemaId);
        }
        if (strIsEmpty(serverPfSchemaId)) {
            ServerProfileSchemaDto profileSchema = createServerProfileSchema(applicationId, null);
            profileFilter.setServerProfileSchemaId(profileSchema.getId());
        }
        else {
            profileFilter.setServerProfileSchemaId(serverPfSchemaId);
        }
        if (strIsEmpty(endpointGroupId)) {
            EndpointGroupDto endpointGroup = createEndpointGroup(applicationId);
            profileFilter.setEndpointGroupId(endpointGroup.getId());
        }
        else {
            profileFilter.setEndpointGroupId(endpointGroupId);
        }
        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        ProfileFilterDto savedProfileFilter = client
                .editProfileFilter(profileFilter);
        return savedProfileFilter;
    }

    /**
     * Creates the configuration.
     *
     * @return the configuration dto
     * @throws Exception the exception
     */
    protected ConfigurationDto createConfiguration() throws Exception {
        return createConfiguration(null, null);
    }

    /**
     * Creates the configuration.
     *
     * @param configurationSchemaId the configuration schema id
     * @param endpointGroupId the endpoint group id
     * @return the configuration dto
     * @throws Exception the exception
     */
    protected ConfigurationDto createConfiguration(String configurationSchemaId, String endpointGroupId) throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        return createConfiguration(configurationSchemaId, endpointGroupId, application.getId());
    }

    /**
     * Creates the configuration.
     *
     * @param configurationSchemaId the configuration schema id
     * @param endpointGroupId the endpoint group id
     * @param applicationId the application id
     * @return the configuration dto
     * @throws Exception the exception
     */
    protected ConfigurationDto createConfiguration(String configurationSchemaId, String endpointGroupId, String applicationId) throws Exception {
        ConfigurationDto configuration = new ConfigurationDto();
        String config = getResourceAsString(TEST_CONFIGURATION);

//        configuration.setBody(config.getBytes());
        configuration.setStatus(UpdateStatus.INACTIVE);
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            configuration.setApplicationId(application.getId());
            applicationId = application.getId();
        } else {
            configuration.setApplicationId(applicationId);
        }

        ConfigurationSchemaDto configSchema = null;
        if (strIsEmpty(configurationSchemaId)) {
            configSchema = createConfigurationSchema(applicationId, null);
            configuration.setSchemaId(configSchema.getId());
        } else {
            loginTenantDeveloper(tenantDeveloperDto.getUsername());
            configuration.setSchemaId(configurationSchemaId);
            configSchema = client.getConfigurationSchema(configurationSchemaId);
        }
        if (strIsEmpty(endpointGroupId)) {
            EndpointGroupDto endpointGroup = createEndpointGroup(applicationId);
            configuration.setEndpointGroupId(endpointGroup.getId());
        } else {
            configuration.setEndpointGroupId(endpointGroupId);
        }
        GenericAvroConverter converter = new GenericAvroConverter(configSchema.getOverrideSchema());
        configuration.setBody(converter.encodeToJson(converter.decodeJson(config)));
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        ConfigurationDto savedConfiguration = client
                .editConfiguration(configuration);
        return savedConfiguration;
    }

    /**
     * Creates the topic.
     *
     * @param appId the app id
     * @param type the type
     * @return the topic dto
     * @throws Exception the exception
     */
    protected TopicDto createTopic(String appId, TopicTypeDto type) throws Exception {
        TopicDto topicDto = new TopicDto();
        topicDto.setName(generateString(TOPIC));
        topicDto.setType(type);
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication(tenantAdminDto);
            topicDto.setApplicationId(applicationDto.getId());
        } else {
            topicDto.setApplicationId(appId);
        }
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        TopicDto savedTopic = client.createTopic(topicDto);
        return savedTopic;
    }

    /**
     * Creates the notification schema.
     *
     * @param appId the app id
     * @param type the type
     * @return the notification schema dto
     * @throws Exception the exception
     */
    protected NotificationSchemaDto createNotificationSchema(String appId, NotificationTypeDto type) throws Exception {
        NotificationSchemaDto notificationSchema = new NotificationSchemaDto();
        notificationSchema.setName(generateString("Test Schema"));
        notificationSchema.setDescription(generateString("Test Desc"));
        notificationSchema.setType(type);
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication(tenantAdminDto);
            notificationSchema.setApplicationId(applicationDto.getId());
        } else {
            notificationSchema.setApplicationId(appId);
        }
        CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null, null, null);
        notificationSchema.setCtlSchemaId(ctlSchema.getId());

        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        NotificationSchemaDto savedSchema = client
                .createNotificationSchema(notificationSchema);
        return savedSchema;
    }

    /**
     * Creates the user notification schema.
     *
     * @param appId the app id
     * @return the notification schema dto
     * @throws Exception the exception
     */
    protected NotificationSchemaDto createUserNotificationSchema(String appId) throws Exception {
        NotificationSchemaDto notificationSchema = new NotificationSchemaDto();
        notificationSchema.setType(NotificationTypeDto.USER);
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication(tenantAdminDto);
            notificationSchema.setApplicationId(applicationDto.getId());
        } else {
            notificationSchema.setApplicationId(appId);
        }
        CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null, null, null);
        notificationSchema.setCtlSchemaId(ctlSchema.getId());
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        NotificationSchemaDto savedSchema = client
                .createNotificationSchema(notificationSchema);
        return savedSchema;
    }

    /**
     * Creates the log schema.
     *
     * @return the log schema dto
     * @throws Exception the exception
     */
    protected LogSchemaDto createLogSchema() throws Exception {
        return createLogSchema(null);
    }

    /**
     * Creates the log schema.
     *
     * @param applicationId the application id
     * @return the log schema dto
     * @throws Exception the exception
     */
    protected LogSchemaDto createLogSchema(String applicationId) throws Exception {
        LogSchemaDto logSchema = new LogSchemaDto();
        logSchema.setName(generateString("Test Schema"));
        logSchema.setDescription(generateString("Test Desc"));
        if (strIsEmpty(applicationId)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            logSchema.setApplicationId(application.getId());
        }
        else {
            logSchema.setApplicationId(applicationId);
        }

        CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantAdminDto.getTenantId(), null, null, null);
        logSchema.setCtlSchemaId(ctlSchema.getId());

        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        LogSchemaDto savedLogSchema = client.createLogSchema(logSchema);
        return savedLogSchema;
    }

    /**
     * Creates the log appender.
     *
     * @param application the application
     * @param schema the schema
     * @return the log appender dto
     * @throws Exception the exception
     */
    protected LogAppenderDto createLogAppender(ApplicationDto application, LogSchemaDto schema)
            throws Exception {
        LogAppenderDto appender = new LogAppenderDto();
        appender.setName(generateString("Test Schema"));
        appender.setDescription(generateString("Test Desc"));

        if (application == null) {
            application = createApplication(tenantAdminDto);
        }
        appender.setApplicationId(application.getId());
        appender.setApplicationToken(application.getApplicationToken());
        appender.setTenantId(application.getTenantId());
        FileSystemAppenderConfig config = new FileSystemAppenderConfig();
        appender.setPluginClassName(config.getPluginClassName());
        Schema pluginSchema = config.getPluginConfigSchema();
        RawSchema rawSchema = new RawSchema(pluginSchema.toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm =
                new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        appender.setJsonConfiguration(rawData.getRawData());

        if (schema == null) {
            schema = createLogSchema(application.getId());
        }
        appender.setMinLogSchemaVersion(schema.getVersion());
        appender.setMaxLogSchemaVersion(schema.getVersion());

        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        LogAppenderDto savedLogAppender = client.editLogAppenderDto(appender);
        return savedLogAppender;
    }


    /**
     * Send notification.
     *
     * @param appId the app id
     * @param schemaId the schema id
     * @param type the type
     * @return the notification dto
     * @throws Exception the exception
     */
    protected NotificationDto sendNotification(String appId, String schemaId, NotificationTypeDto type) throws Exception {
        NotificationDto notification = new NotificationDto();
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication(tenantAdminDto);
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
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        NotificationDto savedNotification = client
                .sendNotification(notification, "body",
                        "{\"notificationBody\":\"dummy\", \"systemNotificationParam1\":42, \"systemNotificationParam2\":43}");
        return savedNotification;
    }

    /**
     * Send unicast notification.
     *
     * @param keyHash the key hash
     * @param appId the app id
     * @param schemaId the schema id
     * @param type the type
     * @return the endpoint notification dto
     * @throws Exception the exception
     */
    protected EndpointNotificationDto sendUnicastNotification(byte[] keyHash, String appId,
            String schemaId, NotificationTypeDto type) throws Exception {
        NotificationDto notification = new NotificationDto();
        if (strIsEmpty(appId)) {
            ApplicationDto applicationDto = createApplication(tenantAdminDto);
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
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        EndpointNotificationDto savedUnicast = client
                .sendUnicastNotification(notification, Base64.encode(keyHash), "body",
                        "{\"notificationBody\":\"dummy\", \"systemNotificationParam1\":42, \"systemNotificationParam2\":43}");
        return savedUnicast;
    }

    /**
     * Creates the event class family.
     *
     * @return the event class family dto
     * @throws Exception the exception
     */
    protected EventClassFamilyDto createEventClassFamily() throws Exception {
        return createEventClassFamily(null, null);
    }

    /**
     * Creates the event class family.
     *
     * @param tenantId the tenant id
     * @return the event class family dto
     * @throws Exception the exception
     */
    protected EventClassFamilyDto createEventClassFamily(String tenantId) throws Exception {
        return createEventClassFamily(tenantId, null);
    }

    /**
     * Creates the event class family.
     *
     * @param tenantId the tenant id
     * @param classNameSuffix the class name suffix
     * @return the event class family dto
     * @throws Exception the exception
     */
    protected EventClassFamilyDto createEventClassFamily(String tenantId, String classNameSuffix) throws Exception {
        EventClassFamilyDto eventClassFamily = new EventClassFamilyDto();
        eventClassFamily.setName(generateString(EVENT_CLASS_FAMILY));
        eventClassFamily.setNamespace(EVENT_CLASS_FAMILY_NAMESPACE);
        String className = EVENT_CLASS_FAMILY_CLASS_NAME;
        if (StringUtils.isNotBlank(classNameSuffix)) {
            className+=classNameSuffix;
        }
        eventClassFamily.setClassName(className);
        if (strIsEmpty(tenantId)) {
            org.kaaproject.kaa.common.dto.admin.UserDto tenant = createTenantAdmin(tenantAdminUser);
            eventClassFamily.setTenantId(tenant.getId());
        }
        else {
            eventClassFamily.setTenantId(tenantId);
        }
        loginTenantAdmin(tenantAdminUser);
        EventClassFamilyDto savedEventClassFamily = client
                .editEventClassFamily(eventClassFamily);
        return savedEventClassFamily;
    }

    /**
     * Creates the application event family map.
     *
     * @return the application event family map dto
     * @throws Exception the exception
     */
    protected ApplicationEventFamilyMapDto createApplicationEventFamilyMap() throws Exception {
        return createApplicationEventFamilyMap(null, null, 1);
    }

    /**
     * Creates the application event family map.
     *
     * @param applicationToken the application token
     * @param ecfId the ecf id
     * @param version the version
     * @return the application event family map dto
     * @throws Exception the exception
     */
    protected ApplicationEventFamilyMapDto createApplicationEventFamilyMap(String applicationToken, String ecfId, int version) throws Exception {
        ApplicationEventFamilyMapDto applicationEventFamilyMap = new ApplicationEventFamilyMapDto();
        String tenantId = null;
        if (strIsEmpty(applicationToken)) {
            ApplicationDto application = createApplication(tenantAdminDto);
            tenantId = application.getTenantId();
            applicationEventFamilyMap.setApplicationId(application.getId());
        }
        else {
            ApplicationDto application = client.getApplicationByApplicationToken(applicationToken);
            applicationEventFamilyMap.setApplicationId(application.getId());
            tenantId = application.getTenantId();
        }
        EventClassFamilyDto eventClassFamily = null;
        if (strIsEmpty(ecfId)) {
            eventClassFamily = createEventClassFamily(tenantId);
        }
        else {
            loginTenantAdmin(tenantAdminUser);
            eventClassFamily = client.getEventClassFamilyById(ecfId);
        }
        applicationEventFamilyMap.setEcfId(eventClassFamily.getId());
        applicationEventFamilyMap.setEcfName(eventClassFamily.getName());
        if (eventClassFamily.getSchemas() == null || eventClassFamily.getSchemas().size()<version) {
            int start = eventClassFamily.getSchemas() == null ? 0 : eventClassFamily.getSchemas().size();
            for (int i=start;i<version;i++) {
                loginTenantAdmin(tenantAdminUser);
                client.addEventClassFamilySchema(eventClassFamily.getId(), TEST_EVENT_CLASS_FAMILY_SCHEMA);
            }
        }
        loginTenantDeveloper(tenantDeveloperUser);
        applicationEventFamilyMap.setVersion(version);
        List<EventClassDto> eventClasses = client.getEventClassesByFamilyIdVersionAndType(eventClassFamily.getId(), version, EventClassType.EVENT);
        List<ApplicationEventMapDto> eventMaps = new ArrayList<>(eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            ApplicationEventMapDto eventMap = new ApplicationEventMapDto();
            eventMap.setEventClassId(eventClass.getId());
            eventMap.setFqn(eventClass.getFqn());
            eventMap.setAction(ApplicationEventAction.BOTH);
            eventMaps.add(eventMap);
        }
        applicationEventFamilyMap.setEventMaps(eventMaps);

        ApplicationEventFamilyMapDto savedApplicationEventFamilyMap = client
                .editApplicationEventFamilyMap(applicationEventFamilyMap);
        return savedApplicationEventFamilyMap;
    }

    protected static final String TEST_CTL_SCHEMA_ALPHA = "control/data/ctl/alpha.json";
    protected static final String TEST_CTL_SCHEMA_BETA = "control/data/ctl/beta.json";
    protected static final String TEST_CTL_SCHEMA_GAMMA = "control/data/ctl/gamma.json";
    
    protected String ctlRandomFieldName() {
        return CTL_DEFAULT_NAME + random.nextInt(100000);
    }

    protected String ctlRandomFieldType() {
        return CTL_DEFAULT_TYPE + random.nextInt(100000);
    }

    protected CTLSchemaDto createCTLSchema(String name, String namespace, int version, 
            String tenantId, String applicationToken, Set<FqnVersion> dependencies,
            Map<String, String> fields) throws Exception {

        LOG.debug("Generating CTL schema...");

        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode body = factory.objectNode();
        body.put("type", "record");
        body.put("name", name);
        body.put("namespace", namespace);
        body.put("version", version);

        if (dependencies != null && !dependencies.isEmpty()) {
            ArrayNode array = factory.arrayNode();
            for (FqnVersion dependency : dependencies) {
                ObjectNode object = factory.objectNode();
                object.put("fqn", dependency.getFqnString());
                object.put("version", dependency.getVersion());
                array.add(object);
            }
            body.put("dependencies", array);
        }

        ArrayNode array = factory.arrayNode();
        if (fields != null) {
            for (Map.Entry<String, String> field : fields.entrySet()) {
                ObjectNode object = factory.objectNode();
                object.put("name", field.getKey());
                object.put("type", field.getValue());
                array.add(object);
            }
        }
        body.put("fields", array);

        LOG.debug("CTL schema generated: " + body);

        return client.saveCTLSchemaWithAppToken(body.toString(), tenantId, applicationToken);
    }

    /**
     * Assert schemas equals.
     *
     * @param schema the schema
     * @param storedSchema the stored schema
     */
    protected void assertSchemasEquals(VersionDto schema, VersionDto storedSchema) {
        Assert.assertEquals(schema.getId(), storedSchema.getId());
        Assert.assertEquals(schema.getVersion(), storedSchema.getVersion());
    }

}
