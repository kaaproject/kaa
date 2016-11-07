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

package org.kaaproject.kaa.server.operations.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.TenantDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;
import org.kaaproject.kaa.server.common.dao.model.sql.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.operations.pojo.SyncContext;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.EventListenersRequest;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SubscriptionCommand;
import org.kaaproject.kaa.server.sync.SubscriptionCommandType;
import org.kaaproject.kaa.server.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.UserAttachRequest;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/operations/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Transactional
public class OperationsServiceIT extends AbstractTest {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final Logger LOG = LoggerFactory.getLogger(OperationsServiceIT.class);

    private static final String ENDPOINT_ACCESS_TOKEN = "endpointAccessToken";
    private static final String INVALID_ENDPOINT_ACCESS_TOKEN = "InvalidEndpointAccessToken";
    private static final int REQUEST_ID1 = 42;
    private static final String USER_VERIFIER_ID = "user@test.com";
    private static final String USER_EXTERNAL_ID = "user@test.com";
    private static final String USER_ACCESS_TOKEN = "userAccessToken";
    private static final String INVALID_USER_ACCESS_TOKEN = "invalidUserAccessToken";

    private static final int CONF_SCHEMA_VERSION = 2;
    private static final int PROFILE_SCHEMA_VERSION = 1;
    private static final int APPLICATION_SEQ_NUMBER = 7;

    private static final String CUSTOMER_ID = "CustomerId";
    private static final String APPLICATION_NAME = "ApplicationName";
    public static final String DATA_SCHEMA_LOCATION = "operations/service/default_schema.json";
    public static final String BASE_SCHEMA_LOCATION = "operations/service/default_schema_converted_to_base.json";
    public static final String BASE_DATA_LOCATION = "operations/service/base_data.json";
    public static final String BASE_DATA_UPDATED_LOCATION = "operations/service/base_data_updated.json";
    private static final BasicEndpointProfile ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 1");
    private static final BasicEndpointProfile NEW_ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 2");
    private static final BasicEndpointProfile FAKE_ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 3");
    private static final byte[] ENDPOINT_KEY = getRandEndpointKey();
    private static final byte[] ENDPOINT_KEY2 = getRandEndpointKey();
    private static final SdkProfileDto SDK_PROFILE = new SdkProfileDto(null, CONF_SCHEMA_VERSION,
            PROFILE_SCHEMA_VERSION, 1, 1, null, null, null, null, null, null);
    private String SDK_TOKEN;

    public static final String NEW_COMPLEX_CONFIG = "operations/service/delta/complexFieldsDeltaNew.json";

    private String deltaSchema;
    private String currentConfiguration;
    private byte[] currentConfigurationHash;
    private ConfigurationSchema confSchema;
    private Application application;
    private Tenant customer;
    private EndpointProfileSchema endpointProfileSchema;
    private ProfileFilterDto profileFilter;
    private TopicDto mandatoryTopicDto;
    private TopicDto optionalTopicDto;

    private final GenericAvroConverter<GenericRecord> avroConverter = new GenericAvroConverter<GenericRecord>(BasicEndpointProfile.SCHEMA$);

    @Autowired
    protected OperationsService operationsService;

    @Autowired
    protected TenantDao<Tenant> customerDao;

    @Autowired
    protected EndpointConfigurationDao<EndpointConfiguration> endpointConfigurationDao;

    @Autowired
    protected SdkProfileService sdkProfileService;

    private EndpointUserDto userDto;

    private KeyPair keyPair;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() {
        endpointConfigurationDao.removeAll();
    }

    @Before
    public void beforeTest() throws IOException, NoSuchAlgorithmException, SQLException {
        clearDBData();
        keyPair = KeyUtil.generateKeyPair();
        operationsService.setPublicKey(keyPair.getPublic());

        String dataSchema = getResourceAsString(DATA_SCHEMA_LOCATION);
        deltaSchema = getResourceAsString(BASE_SCHEMA_LOCATION);
        currentConfiguration = getResourceAsString(NEW_COMPLEX_CONFIG);

        customer = new Tenant();
        customer.setName(CUSTOMER_ID);
        customer = customerDao.save(customer);
        Assert.assertNotNull(customer);
        Assert.assertNotNull(customer.getId());

        Application appObj = new Application();
        appObj.setTenant(customer);
        appObj.setName(APPLICATION_NAME);

        ApplicationDto applicationDto = applicationService.saveApp(appObj.toDto());
        Assert.assertNotNull(applicationDto);
        Assert.assertNotNull(applicationDto.getId());

        application = applicationDao.findById(applicationDto.getId());

        SDK_PROFILE.setApplicationId(applicationDto.getId());
        SDK_TOKEN = sdkProfileService.saveSdkProfile(SDK_PROFILE).getToken();

        EndpointGroup groupAll = endpointGroupDao.findByAppIdAndWeight(application.getStringId(), 0);

        
        CTLSchemaMetaInfo metaInfo = new CTLSchemaMetaInfo(BasicEndpointProfile.SCHEMA$.getFullName(),
                customer, application);
        
        metaInfo = ctlSchemaMetaInfoDao.save(metaInfo);

        CTLSchema ctlSchema = new CTLSchema();
        ctlSchema.setMetaInfo(metaInfo);
        ctlSchema.setVersion(1);
        ctlSchema.setBody(BasicEndpointProfile.SCHEMA$.toString());
        ctlSchema.setDependencySet(new HashSet<CTLSchema>());
        
        ctlSchema = ctlSchemaDao.save(ctlSchema);
        
        EndpointProfileSchema endpointProfileSchemaObj = new EndpointProfileSchema();
        endpointProfileSchemaObj.setVersion(PROFILE_SCHEMA_VERSION);
        endpointProfileSchemaObj.setCtlSchema(ctlSchema);
        endpointProfileSchemaObj.setApplication(application);
        EndpointProfileSchemaDto profileSchemaDto = profileService.saveProfileSchema(endpointProfileSchemaObj.toDto());

        endpointProfileSchema = profileSchemaDao.findById(profileSchemaDto.getId());

        EndpointGroup endpointGroup = new EndpointGroup();
        endpointGroup.setApplication(application);
        endpointGroup.setName("Test group");
        endpointGroup.setWeight(177);
        endpointGroup.setDescription("Test Description");
        endpointGroup = endpointGroupDao.save(endpointGroup);

        ProfileFilter profileFilterObj = new ProfileFilter();
        profileFilterObj.setApplication(application);
        profileFilterObj.setEndpointGroup(endpointGroup);
        profileFilterObj.setBody("profileBody.contains(\"dummy\")");
        profileFilterObj.setEndpointProfileSchema(endpointProfileSchema);
        profileFilter = profileService.saveProfileFilter(profileFilterObj.toDto());
        profileService.activateProfileFilter(profileFilter.getId(), null);

        confSchema = new ConfigurationSchema();
        confSchema.setApplication(application);
        confSchema.setVersion(CONF_SCHEMA_VERSION);
        confSchema.setCtlSchema(ctlSchema);
        try {
            confSchema = new ConfigurationSchema(configurationService.saveConfSchema(confSchema.toDto()));
        } catch (IncorrectParameterException e) {
            Assert.fail("Can't generate schemas");
        }
        Assert.assertNotNull(confSchema);
        Assert.assertNotNull(confSchema.getId());

        userDto = new EndpointUserDto();
        userDto.setTenantId(customer.getId().toString());
        userDto.setExternalId(USER_EXTERNAL_ID);
        userDto.setAccessToken(USER_ACCESS_TOKEN);
        userDto = endpointService.saveEndpointUser(userDto);
        Assert.assertNotNull(userDto);
        Assert.assertNotNull(userDto.getId());

        mandatoryTopicDto = new TopicDto();
        mandatoryTopicDto.setApplicationId(applicationDto.getId());
        mandatoryTopicDto.setName("Mandatory");
        mandatoryTopicDto.setType(TopicTypeDto.MANDATORY);
        mandatoryTopicDto = topicService.saveTopic(mandatoryTopicDto);

        optionalTopicDto = new TopicDto();
        optionalTopicDto.setApplicationId(applicationDto.getId());
        optionalTopicDto.setName("Optional");
        optionalTopicDto.setType(TopicTypeDto.OPTIONAL);
        optionalTopicDto = topicService.saveTopic(optionalTopicDto);


        List<EndpointGroupDto> groups =  endpointService.findEndpointGroupsByAppId(applicationDto.getId());
        for(EndpointGroupDto group : groups){
            endpointService.addTopicToEndpointGroup(group.getId(), mandatoryTopicDto.getId());
            endpointService.addTopicToEndpointGroup(group.getId(), optionalTopicDto.getId());
        }

        NotificationSchemaDto userSchemaDto = notificationService.findNotificationSchemaByAppIdAndTypeAndVersion(applicationDto.getId(), NotificationTypeDto.USER, 1);

        NotificationDto mNotificationDto = new NotificationDto();
        mNotificationDto.setTopicId(mandatoryTopicDto.getId());
        mNotificationDto.setSchemaId(userSchemaDto.getId());
        mNotificationDto.setType(NotificationTypeDto.USER);
        mNotificationDto.setBody("{\"message\": \"mandatory\"}".getBytes(Charset.forName("UTF-8")));
        notificationService.saveNotification(mNotificationDto);

        NotificationDto vNotificationDto = new NotificationDto();
        vNotificationDto.setTopicId(optionalTopicDto.getId());
        vNotificationDto.setSchemaId(userSchemaDto.getId());
        vNotificationDto.setType(NotificationTypeDto.USER);
        vNotificationDto.setBody("{\"message\": \"optional\"}".getBytes(Charset.forName("UTF-8")));
        notificationService.saveNotification(vNotificationDto);

    }

    @Test
    public void basicRegistrationTest() throws GetDeltaException, IOException {
        registerEndpoint();
    }

    private EndpointProfileDto registerEndpoint() throws IOException, GetDeltaException {
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);
        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setSdkToken(SDK_TOKEN);
        request.setClientSyncMetaData(md);

        ProfileClientSync profileSync = new ProfileClientSync(ByteBuffer.wrap(ENDPOINT_KEY),
                ByteBuffer.wrap(profile), SDK_TOKEN, null);
        request.setProfileSync(profileSync);

        request.setConfigurationSync(new ConfigurationClientSync());

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncConfiguration(context, request.getConfigurationSync());

        currentConfigurationHash = context.getEndpointProfile().getConfigurationHash();
        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getConfigurationSync());
        Assert.assertEquals(SyncResponseStatus.RESYNC, response.getConfigurationSync().getResponseStatus());
        Assert.assertNotNull(response.getConfigurationSync().getConfDeltaBody());
        // Kaa #7786
        Assert.assertNull(response.getConfigurationSync().getConfSchemaBody());
        return context.getEndpointProfile();
    }

    @Test
    public void basicDoubleRegistrationTest() throws GetDeltaException, IOException {
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);
        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setSdkToken(SDK_TOKEN);
        request.setClientSyncMetaData(md);

        ProfileClientSync profileSync = new ProfileClientSync(ByteBuffer.wrap(ENDPOINT_KEY),
                ByteBuffer.wrap(profile), SDK_TOKEN, null);
        request.setProfileSync(profileSync);

        request.setConfigurationSync(new ConfigurationClientSync());

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncConfiguration(context, request.getConfigurationSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getConfigurationSync());
        Assert.assertEquals(SyncResponseStatus.RESYNC, response.getConfigurationSync().getResponseStatus());
        Assert.assertNotNull(response.getConfigurationSync().getConfDeltaBody());
        // Kaa #7786
        Assert.assertNull(response.getConfigurationSync().getConfSchemaBody());

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncConfiguration(context, request.getConfigurationSync());

        response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getConfigurationSync());
        Assert.assertEquals(SyncResponseStatus.RESYNC, response.getConfigurationSync().getResponseStatus());
        Assert.assertNotNull(response.getConfigurationSync().getConfDeltaBody());
        // Kaa #7786
        Assert.assertNull(response.getConfigurationSync().getConfSchemaBody());
    }

    @Test
    public void basicUpdateTest() throws GetDeltaException, IOException {
        basicRegistrationTest();
        byte[] oldProfile = avroConverter.encode(ENDPOINT_PROFILE);
        byte[] profile = avroConverter.encode(NEW_ENDPOINT_PROFILE);
        ClientSync request = new ClientSync();
        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(oldProfile).getData()));
        md.setSdkToken(SDK_TOKEN);
        request.setClientSyncMetaData(md);

        ProfileClientSync profileSync = new ProfileClientSync(null,
                ByteBuffer.wrap(profile), SDK_TOKEN, null);
        request.setProfileSync(profileSync);

        ConfigurationClientSync confSyncRequest = new ConfigurationClientSync();
        confSyncRequest.setConfigurationHash(ByteBuffer.wrap(currentConfigurationHash));

        request.setConfigurationSync(confSyncRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncConfiguration(context, request.getConfigurationSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getConfigurationSync());
        Assert.assertEquals(SyncResponseStatus.NO_DELTA, response.getConfigurationSync().getResponseStatus());
        Assert.assertNull(response.getConfigurationSync().getConfDeltaBody());
        // Kaa #7786
        Assert.assertNull(response.getConfigurationSync().getConfSchemaBody());
    }

    @Test
    public void basicProfileResyncTest() throws GetDeltaException, IOException {
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(FAKE_ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        ConfigurationClientSync confSyncRequest = new ConfigurationClientSync();
        confSyncRequest.setConfigurationHash(ByteBuffer.wrap(currentConfigurationHash));

        request.setConfigurationSync(confSyncRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());

        ServerSync response = context.getResponse();

        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.PROFILE_RESYNC, response.getStatus());
    }

    @Test
    public void basicDeltaTest() throws GetDeltaException, IOException {
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        ConfigurationClientSync confSyncRequest = new ConfigurationClientSync();
        confSyncRequest.setConfigurationHash(ByteBuffer.wrap(currentConfigurationHash));

        request.setConfigurationSync(confSyncRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncConfiguration(context, request.getConfigurationSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getConfigurationSync());
        Assert.assertEquals(SyncResponseStatus.NO_DELTA, response.getConfigurationSync().getResponseStatus());
        Assert.assertNull(response.getConfigurationSync().getConfDeltaBody());
        // Kaa #7786
        Assert.assertNull(response.getConfigurationSync().getConfSchemaBody());
        Assert.assertNull(response.getNotificationSync());
    }

    @Test
    public void basicMandatoryNotificationsTest() throws GetDeltaException, IOException {
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        NotificationClientSync nfSyncRequest = new NotificationClientSync();

        request.setNotificationSync(nfSyncRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncNotification(context, request.getNotificationSync());

        ServerSync response = context.getResponse();

        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getNotificationSync());
        Assert.assertEquals(SyncResponseStatus.DELTA, response.getNotificationSync().getResponseStatus());
        Assert.assertNotNull(response.getNotificationSync().getNotifications());
        //Only mandatory notification
        Assert.assertEquals(1, response.getNotificationSync().getNotifications().size());
    }

    @Test
    public void basicOptionalNotificationsTest() throws GetDeltaException, IOException {
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        NotificationClientSync nfSyncRequest = new NotificationClientSync();
        SubscriptionCommand command = new SubscriptionCommand(optionalTopicDto.getId(), SubscriptionCommandType.ADD);
        nfSyncRequest.setSubscriptionCommands(Collections.singletonList(command));
        request.setNotificationSync(nfSyncRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncNotification(context, request.getNotificationSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getNotificationSync());
        Assert.assertEquals(SyncResponseStatus.DELTA, response.getNotificationSync().getResponseStatus());
        Assert.assertNotNull(response.getNotificationSync().getNotifications());
        //Mandatory + Optional notification
        Assert.assertEquals(2, response.getNotificationSync().getNotifications().size());
    }

    @Test
    public void basicUserAttachTest() throws GetDeltaException, IOException {
        EndpointProfileDto profile = registerEndpoint();

        profile.setEndpointUserId(null);
        profile = operationsService.attachEndpointToUser(profile, application.getApplicationToken(), USER_EXTERNAL_ID);
        Assert.assertNotNull(profile.getEndpointUserId());
    }

    private EndpointProfileDto createSecondEndpoint() throws GetDeltaException, IOException {
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY2).getData()));
        md.setSdkToken(SDK_TOKEN);
        request.setClientSyncMetaData(md);

        ProfileClientSync profileSync = new ProfileClientSync(ByteBuffer.wrap(ENDPOINT_KEY2),
                ByteBuffer.wrap(profile), SDK_TOKEN, ENDPOINT_ACCESS_TOKEN);
        request.setProfileSync(profileSync);

        request.setConfigurationSync(new ConfigurationClientSync());

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.syncNotification(context, request.getNotificationSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        return context.getEndpointProfile();
    }

    @Test
    public void basicEndpointAttachTest() throws GetDeltaException, IOException {
        // register main endpoint
        EndpointProfileDto profileDto = registerEndpoint();
        // register second endpoint
        createSecondEndpoint();

        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        UserClientSync userRequest = new UserClientSync();
        userRequest.setEndpointAttachRequests(Collections.singletonList(new EndpointAttachRequest(REQUEST_ID1, ENDPOINT_ACCESS_TOKEN)));
        request.setUserSync(userRequest);

        profileDto.setEndpointUserId(userDto.getId());

        SyncContext context = createContext(request);
        context.setEndpointProfile(profileDto);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.processEndpointAttachDetachRequests(context, request.getUserSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNull(response.getConfigurationSync());
        Assert.assertNull(response.getNotificationSync());
        Assert.assertNotNull(response.getUserSync().getEndpointAttachResponses());
        Assert.assertEquals(1, response.getUserSync().getEndpointAttachResponses().size());
        Assert.assertEquals(SyncStatus.SUCCESS, response.getUserSync().getEndpointAttachResponses().get(0).getResult());
    }

    @Test
    public void basicEndpointAttachFailTest() throws GetDeltaException, IOException {
        // register main endpoint
        EndpointProfileDto profileDto = registerEndpoint();
        // register second endpoint
        createSecondEndpoint();

        operationsService.attachEndpointToUser(profileDto, application.getApplicationToken(), USER_EXTERNAL_ID);

        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        UserClientSync userRequest = new UserClientSync();
        userRequest.setEndpointAttachRequests(Collections.singletonList(new EndpointAttachRequest(REQUEST_ID1, INVALID_ENDPOINT_ACCESS_TOKEN)));
        request.setUserSync(userRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.processEndpointAttachDetachRequests(context, request.getUserSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNull(response.getConfigurationSync());
        Assert.assertNull(response.getNotificationSync());
        Assert.assertNotNull(response.getUserSync());
        Assert.assertNotNull(response.getUserSync().getEndpointAttachResponses());
        Assert.assertEquals(1, response.getUserSync().getEndpointAttachResponses().size());
        Assert.assertEquals(SyncStatus.FAILURE, response.getUserSync().getEndpointAttachResponses().get(0).getResult());
    }

    @Test
    public void basicEndpointDetachTest() throws GetDeltaException, IOException {
        // register main endpoint
        EndpointProfileDto profileDto = registerEndpoint();
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);
        // register second endpoint
        EndpointProfileDto secondDto = createSecondEndpoint();

        operationsService.attachEndpointToUser(profileDto, application.getApplicationToken(), USER_EXTERNAL_ID);
        operationsService.attachEndpointToUser(secondDto, application.getApplicationToken(), USER_EXTERNAL_ID);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        UserClientSync userRequest = new UserClientSync();
        userRequest.setEndpointDetachRequests(Collections.singletonList(new EndpointDetachRequest(REQUEST_ID1, Base64Util.encode(EndpointObjectHash.fromSHA1(ENDPOINT_KEY2).getData()))));
        request.setUserSync(userRequest);

        SyncContext context = createContext(request);
        context.setEndpointProfile(profileDto);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.processEndpointAttachDetachRequests(context, request.getUserSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNull(response.getConfigurationSync());
        Assert.assertNull(response.getNotificationSync());
        Assert.assertNotNull(response.getUserSync());
        Assert.assertNotNull(response.getUserSync().getEndpointDetachResponses());
        Assert.assertEquals(1, response.getUserSync().getEndpointDetachResponses().size());
        Assert.assertEquals(SyncStatus.SUCCESS, response.getUserSync().getEndpointDetachResponses().get(0).getResult());
    }

    @Test
    public void basicEndpointDetachFailTest() throws GetDeltaException, IOException {
        // register main endpoint
        EndpointProfileDto profileDto = registerEndpoint();
        // register second endpoint
        createSecondEndpoint();

        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        UserClientSync userRequest = new UserClientSync();
        userRequest.setUserAttachRequest(new UserAttachRequest(USER_VERIFIER_ID, USER_EXTERNAL_ID, USER_ACCESS_TOKEN));
        userRequest.setEndpointDetachRequests(Collections.singletonList(new EndpointDetachRequest(REQUEST_ID1, Base64Util.encode(EndpointObjectHash.fromSHA1(ENDPOINT_KEY2).getData()))));
        request.setUserSync(userRequest);

        profileDto.setEndpointUserId(userDto.getId());
        SyncContext context = createContext(request);
        context.setEndpointProfile(profileDto);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.processEndpointAttachDetachRequests(context, request.getUserSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNull(response.getConfigurationSync());
        Assert.assertNull(response.getNotificationSync());
        Assert.assertNotNull(response.getUserSync());
        Assert.assertNotNull(response.getUserSync().getEndpointDetachResponses());
        Assert.assertEquals(1, response.getUserSync().getEndpointDetachResponses().size());
        Assert.assertEquals(SyncStatus.FAILURE, response.getUserSync().getEndpointDetachResponses().get(0).getResult());
    }

    @Test
    public void basicEventListenerFailTest() throws GetDeltaException, IOException {
        // register main endpoint
        basicRegistrationTest();

        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        ClientSync request = new ClientSync();

        ClientSyncMetaData md = new ClientSyncMetaData();
        md.setApplicationToken(application.getApplicationToken());
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        md.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        request.setClientSyncMetaData(md);

        EventClientSync eventRequest = new EventClientSync();
        eventRequest.setEventListenersRequests(Collections.singletonList(new EventListenersRequest(REQUEST_ID1, Arrays.asList("fqn"))));

        request.setEventSync(eventRequest);

        SyncContext context = createContext(request);

        operationsService.syncClientProfile(context, request.getProfileSync());
        operationsService.processEventListenerRequests(context, request.getEventSync());

        ServerSync response = context.getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncStatus.SUCCESS, response.getStatus());
        Assert.assertNull(response.getConfigurationSync());
        Assert.assertNull(response.getNotificationSync());
        Assert.assertNotNull(response.getEventSync());
        Assert.assertNotNull(response.getEventSync().getEventListenersResponses());
        Assert.assertEquals(1, response.getEventSync().getEventListenersResponses().size());
        Assert.assertEquals(SyncStatus.FAILURE, response.getEventSync().getEventListenersResponses().get(0).getResult());
    }

    public static String getResourceAsString(String path) throws IOException {
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

    private static SyncContext createContext(ClientSync request) {
        SyncContext context = new SyncContext(new ServerSync());
        context.setRequestId(request.getRequestId());
        context.setStatus(SyncStatus.SUCCESS);
        context.setMetaData(request.getClientSyncMetaData());
        return context;
    }

    private static byte[] getRandEndpointKey() {
        try {
            return KeyPairGenerator.getInstance("RSA", "SunRsaSign").generateKeyPair().getPublic().getEncoded();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }
}
