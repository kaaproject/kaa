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

package org.kaaproject.kaa.server.operations.service;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.idToObjectId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.avro.generic.GenericRecord;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationDao;
import org.kaaproject.kaa.server.common.dao.ConfigurationSchemaDao;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.EndpointGroupDao;
import org.kaaproject.kaa.server.common.dao.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.ProfileFilterDao;
import org.kaaproject.kaa.server.common.dao.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.TenantDao;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.Configuration;
import org.kaaproject.kaa.server.common.dao.mongo.model.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
import org.kaaproject.kaa.server.common.dao.mongo.model.Tenant;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.delta.DefaultDeltaCalculatorTest;
import org.kaaproject.kaa.server.operations.service.delta.DeltaServiceIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class OperationsServiceIT {
    protected static final Logger LOG = LoggerFactory.getLogger(DeltaServiceIT.class);

    private static final int CONF_SCHEMA_VERSION = 2;
    private static final int PROFILE_SCHEMA_VERSION = 2;
    private static final int APPLICATION_SEQ_NUMBER = 5;

    private static final String CUSTOMER_ID = "CustomerId";
    private static final String APPLICATION_NAME = "ApplicationName";
    public static final String DATA_SCHEMA_LOCATION = "service/default_schema.json";
    public static final String BASE_SCHEMA_LOCATION = "service/default_schema_converted_to_base.json";
    public static final String BASE_DATA_LOCATION = "service/base_data.json";
    public static final String BASE_DATA_UPDATED_LOCATION = "service/base_data_updated.json";
    private static final BasicEndpointProfile ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 1");
    private static final BasicEndpointProfile NEW_ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 2");
    private static final BasicEndpointProfile FAKE_ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 3");
    private static final byte[] ENDPOINT_KEY = "Endpoint Super Secret Public Key".getBytes();

    private String deltaSchema;
    private String currentConfiguration;
    private ConfigurationSchema confSchema;
    private ApplicationDto app;
    private Tenant customer;
    private ProfileSchemaDto profileSchema;
    private ProfileFilterDto profileFilter;

    private GenericAvroConverter<GenericRecord> avroConverter = new GenericAvroConverter<GenericRecord>(BasicEndpointProfile.SCHEMA$);

    @Autowired
    protected OperationsService operationsService;

    @Autowired
    protected ConfigurationService configurationService;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected ProfileService profileService;

    @Autowired
    protected TenantDao<Tenant> customerDao;

    @Autowired
    protected ApplicationDao<Application> applicationDao;

    @Autowired
    protected ConfigurationSchemaDao<ConfigurationSchema> configurationSchemaDao;

    @Autowired
    protected ConfigurationDao<Configuration> configurationDao;

    @Autowired
    protected EndpointConfigurationDao<EndpointConfiguration> endpointConfigurationDao;

    @Autowired
    protected EndpointProfileDao<EndpointProfile> endpointProfileDao;

    @Autowired
    protected ProfileSchemaDao<ProfileSchema> profileSchemaDao;

    @Autowired
    protected EndpointGroupDao<EndpointGroup> endpointGroupDao;

    @Autowired
    protected ProfileFilterDao<ProfileFilter> profileFilterDao;


    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        String dataSchema = getResourceAsString(DATA_SCHEMA_LOCATION);
        deltaSchema = getResourceAsString(BASE_SCHEMA_LOCATION);
        currentConfiguration = getResourceAsString(DefaultDeltaCalculatorTest.NEW_COMPLEX_CONFIG);

        customer = new Tenant();
        customer.setName(CUSTOMER_ID);
        customer = customerDao.save(customer);
        Assert.assertNotNull(customer);
        Assert.assertNotNull(customer.getId());

        Application appObj = new Application();
        appObj.setTenantId(idToObjectId(customer.getId()));
        appObj.setName(APPLICATION_NAME);
        this.app = applicationService.saveApp(appObj.toDto());
        Assert.assertNotNull(app);
        Assert.assertNotNull(app.getId());

        EndpointGroup groupAll = endpointGroupDao.findByAppIdAndWeight(app.getId(), 0);

        ProfileSchema profileSchemaObj = new ProfileSchema();
        profileSchemaObj.setMajorVersion(PROFILE_SCHEMA_VERSION);
        profileSchemaObj.setMinorVersion(0);
        profileSchemaObj.setSchema(BasicEndpointProfile.SCHEMA$.toString());
        profileSchemaObj.setApplicationId(idToObjectId(app.getId()));
        profileSchema = profileService.saveProfileSchema(profileSchemaObj.toDto());

        EndpointGroup endpointGroup = new EndpointGroup();
        endpointGroup.setApplicationId(new ObjectId(app.getId()));
        endpointGroup.setName("Test group");
        endpointGroup.setWeight(177);
        endpointGroup.setDescription("Test Description");
        endpointGroup = endpointGroupDao.save(endpointGroup);


        ProfileFilter profileFilterObj = new ProfileFilter();
        profileFilterObj.setApplicationId(idToObjectId(app.getId()));
        profileFilterObj.setEndpointGroupId(idToObjectId(endpointGroup.getId()));
        profileFilterObj.setBody("profileBody.contains(\"dummy\")");
        profileFilterObj.setSchemaId(idToObjectId(profileSchema.getId()));
        profileFilter = profileService.saveProfileFilter(profileFilterObj.toDto());
        profileService.activateProfileFilter(profileFilter.getId(), null);

        confSchema = new ConfigurationSchema();
        confSchema.setApplicationId(idToObjectId(app.getId()));
        confSchema.setMajorVersion(CONF_SCHEMA_VERSION);
        confSchema.setMinorVersion(CONF_SCHEMA_VERSION);
        confSchema.setSchema(dataSchema);
        try {
            confSchema = new ConfigurationSchema(configurationService.saveConfSchema(confSchema.toDto()));
        } catch (IncorrectParameterException e) {
            Assert.fail("Can't generate schemas");
        }
        Assert.assertNotNull(confSchema);
        Assert.assertNotNull(confSchema.getId());
    }

    @Test
    public void basicRegistrationTest() throws GetDeltaException, IOException{
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);
        EndpointRegistrationRequest request = new EndpointRegistrationRequest();
        request.setApplicationToken(app.getApplicationToken());
        request.setVersionInfo(new EndpointVersionInfo(CONF_SCHEMA_VERSION, PROFILE_SCHEMA_VERSION, 1, 1));
        request.setEndpointPublicKey(ByteBuffer.wrap(ENDPOINT_KEY));
        request.setProfileBody(ByteBuffer.wrap(profile));
        SyncResponse response = operationsService.registerEndpoint(request).getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncResponseStatus.CONF_RESYNC, response.getResponseType());
        Assert.assertEquals(APPLICATION_SEQ_NUMBER, (int)response.getAppStateSeqNumber());
        Assert.assertNotNull(response.getConfSyncResponse().getConfDeltaBody());
        //Kaa #7786
        Assert.assertNull(response.getConfSyncResponse().getConfSchemaBody());
//        System.out.println(new String(response.getConfSyncResponse().getConfSchemaBody().array()).replaceAll("\\s+",""));
//        Assert.assertEquals(deltaSchema.replaceAll("\\s+",""), new String(response.getConfSyncResponse().getConfSchemaBody().array()).replaceAll("\\s+",""));
    }

    @Test
    public void basicDoubleRegistrationTest() throws GetDeltaException, IOException{
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);
        EndpointRegistrationRequest request = new EndpointRegistrationRequest();
        request.setApplicationToken(app.getApplicationToken());
        request.setVersionInfo(new EndpointVersionInfo(CONF_SCHEMA_VERSION, PROFILE_SCHEMA_VERSION, 1, 1));
        request.setEndpointPublicKey(ByteBuffer.wrap(ENDPOINT_KEY));
        request.setProfileBody(ByteBuffer.wrap(profile));
        SyncResponse response = operationsService.registerEndpoint(request).getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncResponseStatus.CONF_RESYNC, response.getResponseType());
        Assert.assertEquals(APPLICATION_SEQ_NUMBER, (int)response.getAppStateSeqNumber());
        Assert.assertNotNull(response.getConfSyncResponse().getConfDeltaBody());
        Assert.assertNull(response.getConfSyncResponse().getConfSchemaBody());
//        Assert.assertEquals(deltaSchema.replaceAll("\\s+",""), new String(response.getConfSyncResponse().getConfSchemaBody().array()).replaceAll("\\s+",""));
        response = operationsService.registerEndpoint(request).getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncResponseStatus.CONF_RESYNC, response.getResponseType());
        Assert.assertEquals(APPLICATION_SEQ_NUMBER, (int)response.getAppStateSeqNumber());
        Assert.assertNotNull(response.getConfSyncResponse().getConfDeltaBody());
        Assert.assertNull(response.getConfSyncResponse().getConfSchemaBody());
//        Assert.assertEquals(deltaSchema.replaceAll("\\s+",""), new String(response.getConfSyncResponse().getConfSchemaBody().array()).replaceAll("\\s+",""));

    }

    @Test
    public void basicUpdateTest() throws GetDeltaException, IOException{
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(NEW_ENDPOINT_PROFILE);
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setApplicationToken(app.getApplicationToken());
        request.setVersionInfo(new EndpointVersionInfo(CONF_SCHEMA_VERSION, PROFILE_SCHEMA_VERSION, 1, 1));
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        request.setProfileBody(ByteBuffer.wrap(profile));
        SyncResponse response = operationsService.updateProfile(request).getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(APPLICATION_SEQ_NUMBER, (int)response.getAppStateSeqNumber());
        Assert.assertEquals(SyncResponseStatus.NO_DELTA, response.getResponseType());
        Assert.assertNull(response.getConfSyncResponse().getConfDeltaBody());
//        Kaa #7786
        Assert.assertNull(response.getConfSyncResponse().getConfSchemaBody());
    }

    @Test
    public void basicProfileResyncTest() throws GetDeltaException, IOException{
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(FAKE_ENDPOINT_PROFILE);

        SyncRequest request = new SyncRequest();
        request.setApplicationToken(app.getApplicationToken());
        request.setAppStateSeqNumber(APPLICATION_SEQ_NUMBER-1);
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        request.setConfigurationHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(currentConfiguration).getData()));
        request.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        SyncResponse response = operationsService.sync(request).getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncResponseStatus.PROFILE_RESYNC, response.getResponseType());
        Assert.assertEquals(APPLICATION_SEQ_NUMBER-1, (int)response.getAppStateSeqNumber());
        Assert.assertNull(response.getConfSyncResponse());
        Assert.assertNull(response.getNotificationSyncResponse());
    }

    @Test
    public void basicDeltaTest() throws GetDeltaException, IOException{
        basicRegistrationTest();
        byte[] profile = avroConverter.encode(ENDPOINT_PROFILE);

        SyncRequest request = new SyncRequest();
        request.setApplicationToken(app.getApplicationToken());
        request.setAppStateSeqNumber(APPLICATION_SEQ_NUMBER);
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData()));
        request.setConfigurationHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(currentConfiguration).getData()));
        request.setProfileHash(ByteBuffer.wrap(EndpointObjectHash.fromSHA1(profile).getData()));
        SyncResponse response = operationsService.sync(request).getResponse();
        Assert.assertNotNull(response);
        Assert.assertEquals(SyncResponseStatus.NO_DELTA, response.getResponseType());
        Assert.assertEquals(APPLICATION_SEQ_NUMBER, (int)response.getAppStateSeqNumber());
        Assert.assertNull(response.getConfSyncResponse().getConfDeltaBody());
        Assert.assertNull(response.getConfSyncResponse().getConfSchemaBody());
    }

    @After
    public void afterTest() {
        endpointProfileDao.removeAll();
        profileSchemaDao.removeAll();
        endpointConfigurationDao.removeAll();
        configurationDao.removeAll();
        configurationSchemaDao.removeAll();
        applicationDao.removeAll();
        customerDao.removeAll();
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
}
