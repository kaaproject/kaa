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

package org.kaaproject.kaa.server.operations.service.profile;

import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.avro.generic.GenericRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.schema.system.EmptyData;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest;
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
public class ProfileServiceIT extends AbstractTest {

    private static final int PROFILE_SCHEMA_VERSION = 1;
    private static final int NEW_PROFILE_SCHEMA_VERSION = 2;

    private static final byte[] ENDPOINT_KEY = getRandEndpointKey();
    private static final String CUSTOMER_NAME = "CUSTOMER_NAME";
    private static final String APP_NAME = "APP_NAME";
    private String sdkToken;
    private String newSdkToken;

    private static final EmptyData ENDPOINT_PROFILE = new EmptyData();
    private static final BasicEndpointProfile NEW_ENDPOINT_PROFILE = new BasicEndpointProfile("newprofile");

    protected static final Logger LOG = LoggerFactory.getLogger(ProfileServiceIT.class);

    private final GenericAvroConverter<GenericRecord> baseAvroConverter = new GenericAvroConverter<GenericRecord>(EmptyData.SCHEMA$);
    private final GenericAvroConverter<GenericRecord> newAvroConverter = new GenericAvroConverter<GenericRecord>(
            BasicEndpointProfile.SCHEMA$);

    @Autowired
    protected ProfileService profileService;

    @Autowired
    protected SdkProfileService sdkProfileService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected org.kaaproject.kaa.server.common.dao.ProfileService daoProfileService;

    @Autowired
    protected CTLService ctlService;

    private TenantDto tenant;
    private ApplicationDto application;
    private EndpointProfileSchemaDto schema1Dto;
    private EndpointProfileSchemaDto schema2Dto;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException, SQLException {
        clearDBData();
        tenant = new TenantDto();
        tenant.setName(CUSTOMER_NAME);
        tenant = userService.saveTenant(tenant);

        application = new ApplicationDto();
        application.setName(APP_NAME);
        application.setTenantId(tenant.getId());
        application = applicationService.saveApp(application);

        CTLSchemaMetaInfoDto ctl1MetaDataDto = new CTLSchemaMetaInfoDto(EmptyData.SCHEMA$.getFullName(),
                null, null);
        CTLSchemaDto ctl1SchemaDto = new CTLSchemaDto();
        ctl1SchemaDto.setMetaInfo(ctl1MetaDataDto);
        ctl1SchemaDto.setVersion(2);
        ctl1SchemaDto.setBody(EmptyData.SCHEMA$.toString());
        ctl1SchemaDto = ctlService.saveCTLSchema(ctl1SchemaDto);

        schema1Dto = new EndpointProfileSchemaDto();
        schema1Dto.setVersion(PROFILE_SCHEMA_VERSION);
        schema1Dto.setCtlSchemaId(ctl1SchemaDto.getId());
        schema1Dto.setApplicationId(application.getId());
        schema1Dto = daoProfileService.saveProfileSchema(schema1Dto);
        
        CTLSchemaMetaInfoDto ctl2MetaDataDto = new CTLSchemaMetaInfoDto(BasicEndpointProfile.SCHEMA$.getFullName(),
                application.getTenantId(), application.getId());
        CTLSchemaDto ctl2SchemaDto = new CTLSchemaDto();
        ctl2SchemaDto.setMetaInfo(ctl2MetaDataDto);
        ctl2SchemaDto.setVersion(2);
        ctl2SchemaDto.setBody(BasicEndpointProfile.SCHEMA$.toString());
        ctl2SchemaDto = ctlService.saveCTLSchema(ctl2SchemaDto);
        
        schema2Dto = new EndpointProfileSchemaDto();
        schema2Dto.setVersion(NEW_PROFILE_SCHEMA_VERSION);
        schema2Dto.setCtlSchemaId(ctl2SchemaDto.getId());
        schema2Dto.setApplicationId(application.getId());
        schema2Dto = daoProfileService.saveProfileSchema(schema2Dto);

        SdkProfileDto sdkPropertiesDto = new SdkProfileDto();
        sdkPropertiesDto.setApplicationId(application.getId());
        sdkPropertiesDto.setProfileSchemaVersion(PROFILE_SCHEMA_VERSION);
        sdkPropertiesDto.setConfigurationSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);
        sdkPropertiesDto.setApplicationToken(application.getApplicationToken());
        sdkPropertiesDto = sdkProfileService.saveSdkProfile(sdkPropertiesDto);
        sdkToken = new SdkProfile(sdkPropertiesDto).getToken();

        SdkProfileDto newSdkProfileDto = new SdkProfileDto();
        newSdkProfileDto.setApplicationId(application.getId());
        newSdkProfileDto.setProfileSchemaVersion(NEW_PROFILE_SCHEMA_VERSION);
        newSdkProfileDto.setConfigurationSchemaVersion(1);
        newSdkProfileDto.setNotificationSchemaVersion(1);
        newSdkProfileDto.setLogSchemaVersion(1);
        newSdkProfileDto.setApplicationToken(application.getApplicationToken());
        newSdkProfileDto = sdkProfileService.saveSdkProfile(newSdkProfileDto);
        newSdkToken = new SdkProfile(newSdkProfileDto).getToken();
    }

    @Test
    public void registerProfileServiceTest() throws IOException {
        byte[] profile = baseAvroConverter.encode(ENDPOINT_PROFILE);
        RegisterProfileRequest request = new RegisterProfileRequest(application.getApplicationToken(), ENDPOINT_KEY, sdkToken, profile);
        EndpointProfileDto dto = profileService.registerProfile(request);
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(Arrays.equals(ENDPOINT_KEY, dto.getEndpointKey()));
        Assert.assertTrue(Arrays.equals(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData(), dto.getEndpointKeyHash()));
        Assert.assertEquals(baseAvroConverter.encodeToJson(ENDPOINT_PROFILE), dto.getClientProfileBody().replaceAll(" ", ""));
        Assert.assertTrue(Arrays.equals(EndpointObjectHash.fromSHA1(profile).getData(), dto.getProfileHash()));
    }

    @Test
    public void updateProfileServiceTest() throws IOException {
        byte[] profile = baseAvroConverter.encode(ENDPOINT_PROFILE);
        RegisterProfileRequest request = new RegisterProfileRequest(application.getApplicationToken(), ENDPOINT_KEY, sdkToken, profile);

        EndpointProfileDto oldDto = profileService.registerProfile(request);

        Assert.assertEquals(baseAvroConverter.encodeToJson(ENDPOINT_PROFILE), oldDto.getClientProfileBody().replaceAll(" ", ""));

        byte[] newProfile = newAvroConverter.encode(NEW_ENDPOINT_PROFILE);
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(application.getApplicationToken(), EndpointObjectHash.fromSHA1(ENDPOINT_KEY), null,
                newProfile, newSdkToken);
        EndpointProfileDto newDto = profileService.updateProfile(updateRequest);

        Assert.assertNotNull(newDto);
        Assert.assertNotNull(newDto.getId());
        Assert.assertEquals(oldDto.getId(), newDto.getId());
        Assert.assertEquals(newAvroConverter.encodeToJson(NEW_ENDPOINT_PROFILE), newDto.getClientProfileBody().replaceAll(" ", ""));
        Assert.assertTrue(Arrays.equals(EndpointObjectHash.fromSHA1(newProfile).getData(), newDto.getProfileHash()));
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
