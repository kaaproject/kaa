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

package org.kaaproject.kaa.server.operations.service.delta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericRecord;
import org.junit.*;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.admin.services.schema.CTLSchemaParser;
import org.kaaproject.kaa.server.common.core.algorithms.delta.DeltaCalculatorException;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.service.OperationsServiceIT;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationCacheEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/operations/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class DeltaServiceIT extends AbstractTest {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    protected static final Logger LOG = LoggerFactory.getLogger(DeltaServiceIT.class);

    private static final int PROFILE_VERSION = 1;
    private static final int PROFILE_SCHEMA_VERSION = 1;
    private static final int OLD_ENDPOINT_SEQ_NUMBER = 0;
    private static final int NEW_APPLICATION_SEQ_NUMBER = 6;
    private static final int MAJOR_VERSION = 1;

    private static final int CONF_SCHEMA_VERSION = 2;

    private static final String CUSTOMER_ID = "CustomerId";
    private static final String APPLICATION_ID = "ApplicationId";
    private static final String APPLICATION_NAME = "ApplicationName";
    private static final GenericAvroConverter<GenericRecord> avroConverter = new GenericAvroConverter<>(BasicEndpointProfile.SCHEMA$);
    private static final BasicEndpointProfile ENDPOINT_PROFILE = new BasicEndpointProfile("dummy profile 1");
    private static byte[] PROFILE_BYTES;
    private static String PROFILE_JSON;
    private static String APP_TOKEN;

    private static final byte[] ENDPOINT_KEY = "EndpointKey".getBytes(UTF_8);

    @Autowired
    protected DeltaService deltaService;

    @Autowired
    protected CacheService cacheService;



    private TenantDto tenant;
    private ApplicationDto application;
    private EndpointProfileSchemaDto profileSchema;
    private ProfileFilterDto profileFilter;
    private EndpointProfileDto endpointProfile;
    private EndpointConfigurationDto endpointConfiguration;
    private byte[] endpointConfigurationBytes;
    private ConfigurationSchemaDto confSchema;

    private String egAllId;
    private String pfAllId;
    private String cfAllId;

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
    public void beforeTest() throws IOException, DeltaCalculatorException, ControlServiceException {
        String dataSchema = OperationsServiceIT.getResourceAsString(OperationsServiceIT.DATA_SCHEMA_LOCATION);
        PROFILE_BYTES = avroConverter.encode(ENDPOINT_PROFILE);
        PROFILE_JSON = avroConverter.encodeToJson(ENDPOINT_PROFILE);

        tenant = new TenantDto();
        tenant.setName(CUSTOMER_ID);
        tenant = userService.saveTenant(tenant);
        assertNotNull(tenant);
        assertNotNull(tenant.getId());

        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setTenantId(tenant.getId());
        applicationDto.setApplicationToken(APPLICATION_ID);
        applicationDto.setName(APPLICATION_NAME);
        applicationDto.setSequenceNumber(NEW_APPLICATION_SEQ_NUMBER);
        applicationDto = applicationService.saveApp(applicationDto);
        APP_TOKEN = applicationDto.getApplicationToken();
        assertNotNull(applicationDto);
        assertNotNull(applicationDto.getId());

        application = applicationService.findAppById(applicationDto.getId());

        EndpointGroupDto groupAll = endpointService.findEndpointGroupsByAppId(application.getId()).get(0);

        CTLSchemaDto profileCtlSchema = new CTLSchemaDto();
        CTLSchemaMetaInfoDto metaInfo = new CTLSchemaMetaInfoDto(BasicEndpointProfile.SCHEMA$.getFullName(),
                application.getTenantId(),
                application.getId());
        profileCtlSchema.setMetaInfo(metaInfo);
        profileCtlSchema.setBody(BasicEndpointProfile.SCHEMA$.toString());
        profileCtlSchema.setVersion(1);
        profileCtlSchema.setDependencySet(new HashSet<CTLSchemaDto>());
        profileCtlSchema = ctlService.saveCTLSchema(profileCtlSchema);


        Schema schema = new Schema.Parser().parse(dataSchema);
        CTLSchemaDto confCtlSchema = new CTLSchemaDto();
        CTLSchemaMetaInfoDto confMetaInfo = new CTLSchemaMetaInfoDto(schema.getFullName(),
                application.getTenantId(),
                application.getId());
        confCtlSchema.setMetaInfo(confMetaInfo);
        confCtlSchema.setBody(schema.toString());
        confCtlSchema.setVersion(CONF_SCHEMA_VERSION);
        confCtlSchema.setDependencySet(new HashSet<CTLSchemaDto>());
        confCtlSchema = ctlService.saveCTLSchema(confCtlSchema);

        EndpointProfileSchemaDto profileSchemaObj = new EndpointProfileSchemaDto();
        profileSchemaObj.setVersion(PROFILE_SCHEMA_VERSION);
        profileSchemaObj.setCtlSchemaId(profileCtlSchema.getId());
        profileSchemaObj.setApplicationId(application.getId());
        EndpointProfileSchemaDto profileSchemaDto = profileService.saveProfileSchema(profileSchemaObj);

        profileSchema = profileService.findProfileSchemaById(profileSchemaDto.getId());

        EndpointGroupDto endpointGroup = new EndpointGroupDto();
        endpointGroup.setApplicationId(application.getId());
        endpointGroup.setName("Test group");
        endpointGroup.setWeight(277);
        endpointGroup.setDescription("Test Description");
        endpointGroup = endpointService.saveEndpointGroup(endpointGroup);

        ProfileFilterDto profileFilterObj = new ProfileFilterDto();
        profileFilterObj.setApplicationId(application.getId());
        profileFilterObj.setEndpointGroupId(endpointGroup.getId());
        profileFilterObj.setBody("profileBody.contains(\"dummy\")");
        profileFilterObj.setEndpointProfileSchemaId(profileSchema.getId());
        profileFilter = profileService.saveProfileFilter(profileFilterObj);
        profileService.activateProfileFilter(profileFilter.getId(), null);

        confSchema = new ConfigurationSchemaDto();
        confSchema.setApplicationId(application.getId());
        confSchema.setVersion(CONF_SCHEMA_VERSION);
        confSchema.setCtlSchemaId(confCtlSchema.getId());

        try {
            confSchema = configurationService.saveConfSchema(confSchema);
        } catch (IncorrectParameterException e) {
            Assert.fail("Can't generate schemas");
        }
        Assert.assertNotNull(confSchema);
        Assert.assertNotNull(confSchema.getId());

        egAllId = groupAll.getId();
        pfAllId = profileFilter.getId();
        ConfigurationDto confDto = configurationService.findConfigurationByEndpointGroupIdAndVersion(egAllId, CONF_SCHEMA_VERSION);
        cfAllId = confDto.getId();

        endpointConfiguration = new EndpointConfigurationDto();
        endpointConfiguration.setConfiguration(confDto.getBody().getBytes(UTF_8));
        endpointConfiguration.setConfigurationHash(EndpointObjectHash.fromSHA1(confDto.getBody()).getData());
        endpointConfiguration = endpointService.saveEndpointConfiguration(endpointConfiguration);
        assertNotNull(endpointConfiguration);

        EndpointGroupStateDto egs = new EndpointGroupStateDto();
        egs.setConfigurationId(cfAllId);
        egs.setEndpointGroupId(egAllId);
        egs.setProfileFilterId(pfAllId);

        endpointProfile = new EndpointProfileDto();
        endpointProfile.setApplicationId(application.getId());
        endpointProfile.setEndpointKeyHash(UUID.randomUUID().toString().getBytes());
        endpointProfile.setClientProfileBody(PROFILE_JSON);
        endpointProfile.setProfileHash(EndpointObjectHash.fromSHA1(PROFILE_BYTES).getData());
        endpointProfile.setConfigurationHash(endpointConfiguration.getConfigurationHash());
        endpointProfile.setConfigurationVersion(CONF_SCHEMA_VERSION);
        endpointProfile.setClientProfileVersion(PROFILE_VERSION);
        endpointProfile.setGroupState(Collections.singletonList(egs));
        endpointProfile = endpointService.saveEndpointProfile(endpointProfile);
        assertNotNull(endpointProfile);
        assertNotNull(endpointProfile.getId());
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void testDeltaServiceNoHistoryDelta() throws Exception {
        GetDeltaRequest request = new GetDeltaRequest(application.getApplicationToken(), EndpointObjectHash.fromSHA1(endpointConfiguration
                .getConfiguration()), true);
        request.setEndpointProfile(endpointProfile);
        GetDeltaResponse response = deltaService.getDelta(request);
        assertNotNull(response);
        assertEquals(GetDeltaResponse.GetDeltaResponseType.NO_DELTA, response.getResponseType());
        assertNull(response.getDelta());
        assertNull(response.getConfSchema());
    }

    @Test
    public void testDeltaServiceFirstRequest() throws Exception {
        GetDeltaRequest request = new GetDeltaRequest(application.getApplicationToken());
        request.setEndpointProfile(endpointProfile);
        GetDeltaResponse response = deltaService.getDelta(request);

        assertNotNull(response);
        assertEquals(GetDeltaResponse.GetDeltaResponseType.CONF_RESYNC, response.getResponseType());
        assertNotNull(response.getDelta());
        endpointConfigurationBytes = response.getDelta().getData();
        assertNotNull(endpointConfigurationBytes);
    }

    @Test
    public void testDeltaServiceHashMismatch() throws Exception {
        GetDeltaRequest request = new GetDeltaRequest(application.getApplicationToken(),
                EndpointObjectHash.fromBytes(new byte[] { 1, 2, 3 }));
        request.setEndpointProfile(endpointProfile);
        GetDeltaResponse response = deltaService.getDelta(request);

        assertNotNull(response);
        assertEquals(GetDeltaResponse.GetDeltaResponseType.CONF_RESYNC, response.getResponseType());
        assertNotNull(response.getDelta());
        endpointConfigurationBytes = response.getDelta().getData();
        assertNotNull(endpointConfigurationBytes);
    }

    @Test
    public void testDeltaServiceSecondRequest() throws Exception {
        ConfigurationCacheEntry cacheEntry = deltaService.getConfiguration(APP_TOKEN, "EndpointId", endpointProfile);
        assertNotNull(cacheEntry);
        assertNotNull(cacheEntry.getConfiguration());
        assertNotNull(cacheEntry.getDelta());
        assertNotNull(cacheEntry.getHash());
        assertNull(cacheEntry.getUserConfigurationHash());

        GenericAvroConverter<GenericContainer> newConfConverter = new GenericAvroConverter<>(new Schema.Parser().parse(confSchema
                .getBaseSchema()));
        GenericContainer container = newConfConverter.decodeJson(OperationsServiceIT
                .getResourceAsString(OperationsServiceIT.BASE_DATA_UPDATED_LOCATION));
        byte[] newConfData = newConfConverter.encodeToJsonBytes(container);

        ConfigurationDto newConfDto = new ConfigurationDto();
        newConfDto.setEndpointGroupId(egAllId);
        newConfDto.setSchemaId(confSchema.getId());
        newConfDto.setBody(new String(newConfData, UTF_8));

        newConfDto = configurationService.saveConfiguration(newConfDto);
        configurationService.activateConfiguration(newConfDto.getId(), "test");

        List<EndpointGroupStateDto> changes = new ArrayList<>();
        changes.add(new EndpointGroupStateDto(egAllId, pfAllId, newConfDto.getId()));
        endpointProfile.setGroupState(changes);

        ConfigurationCacheEntry newCacheEntry = deltaService.getConfiguration(APP_TOKEN, "EndpointId", endpointProfile);
        assertNotNull(newCacheEntry);
        assertNotNull(newCacheEntry.getConfiguration());
        assertNotNull(newCacheEntry.getDelta());
        assertNotNull(newCacheEntry.getHash());
        assertNull(newCacheEntry.getUserConfigurationHash());
        assertNotEquals(cacheEntry.getHash(), newCacheEntry.getHash());
    }

}
