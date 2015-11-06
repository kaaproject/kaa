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

package org.kaaproject.kaa.server.operations.service.profile;

import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.avro.generic.GenericRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.schema.base.Profile;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.common.dao.impl.ApplicationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.ProfileSchemaDao;
import org.kaaproject.kaa.server.common.dao.impl.TenantDao;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.ProfileSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.SdkProfile;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
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
    private static final String APP_TOKEN = "APP_TOKEN";
    private String sdkToken;
    private String newSdkToken;

    private static final Profile ENDPOINT_PROFILE = new Profile();
    private static final BasicEndpointProfile NEW_ENDPOINT_PROFILE = new BasicEndpointProfile("newprofile");

    protected static final Logger LOG = LoggerFactory.getLogger(ProfileServiceIT.class);

    private final GenericAvroConverter<GenericRecord> baseAvroConverter = new GenericAvroConverter<GenericRecord>(Profile.SCHEMA$);
    private final GenericAvroConverter<GenericRecord> newAvroConverter = new GenericAvroConverter<GenericRecord>(BasicEndpointProfile.SCHEMA$);

    @Autowired
    protected ProfileService profileService;

    @Autowired
    protected SdkProfileService sdkProfileService;

    @Autowired
    protected EndpointProfileDao<EndpointProfile> endpointProfileDao;

    @Autowired
    protected TenantDao<Tenant> customerDao;

    @Autowired
    protected ApplicationDao<Application> applicationDao;

    @Autowired
    protected ProfileSchemaDao<ProfileSchema> profileSchemaDao;

    private Tenant tenant;
    private Application application;
    private ProfileSchema profileSchema;
    private ProfileSchema profileSchema2;

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
        tenant = new Tenant();
        tenant.setName(CUSTOMER_NAME);
        tenant = customerDao.save(tenant);

        application = new Application();
        application.setName(APP_NAME);
        application.setApplicationToken(APP_TOKEN);
        application.setTenant(tenant);
        application = applicationDao.save(application);

        profileSchema = new ProfileSchema();
        profileSchema.setMajorVersion(PROFILE_SCHEMA_VERSION);
        profileSchema.setMinorVersion(0);
        profileSchema.setSchema(Profile.SCHEMA$.toString());
        profileSchema.setApplication(application);
        profileSchema = profileSchemaDao.save(profileSchema);
        
        profileSchema2 = new ProfileSchema();
        profileSchema2.setMajorVersion(NEW_PROFILE_SCHEMA_VERSION);
        profileSchema2.setMinorVersion(0);
        profileSchema2.setSchema(BasicEndpointProfile.SCHEMA$.toString());
        profileSchema2.setApplication(application);
        profileSchema2 = profileSchemaDao.save(profileSchema2);

        SdkProfileDto sdkPropertiesDto = new SdkProfileDto();
        sdkPropertiesDto.setApplicationId(application.getStringId());
        sdkPropertiesDto.setProfileSchemaVersion(profileSchema.getMajorVersion());
        sdkPropertiesDto.setConfigurationSchemaVersion(1);
        sdkPropertiesDto.setNotificationSchemaVersion(1);
        sdkPropertiesDto.setLogSchemaVersion(1);
        sdkPropertiesDto = sdkProfileService.saveSdkProfile(sdkPropertiesDto);
        sdkToken = new SdkProfile(sdkPropertiesDto).getToken();

        SdkProfileDto newSdkProfileDto = new SdkProfileDto();
        newSdkProfileDto.setApplicationId(application.getStringId());
        newSdkProfileDto.setProfileSchemaVersion(profileSchema2.getMajorVersion());
        newSdkProfileDto.setConfigurationSchemaVersion(1);
        newSdkProfileDto.setNotificationSchemaVersion(1);
        newSdkProfileDto.setLogSchemaVersion(1);
        newSdkProfileDto = sdkProfileService.saveSdkProfile(newSdkProfileDto);
        newSdkToken = new SdkProfile(newSdkProfileDto).getToken();
    }

    @After
    public void afterTest() throws IOException {
        endpointProfileDao.removeAll();
    }

    @Test
    public void registerProfileServiceTest() throws IOException {
        byte[] profile = baseAvroConverter.encode(ENDPOINT_PROFILE);
        RegisterProfileRequest request = new RegisterProfileRequest(APP_TOKEN,
                ENDPOINT_KEY, sdkToken, profile);
        EndpointProfileDto dto = profileService.registerProfile(request);
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getId());
        Assert.assertTrue(Arrays.equals(ENDPOINT_KEY, dto.getEndpointKey()));
        Assert.assertTrue(Arrays.equals(EndpointObjectHash.fromSHA1(ENDPOINT_KEY).getData(),
                dto.getEndpointKeyHash()));
        Assert.assertEquals(baseAvroConverter.encodeToJson(ENDPOINT_PROFILE), dto.getProfile().replaceAll(" ", ""));
        Assert.assertTrue(Arrays.equals(EndpointObjectHash.fromSHA1(profile).getData(), dto.getProfileHash()));
    }

    @Test
    public void updateProfileServiceTest() throws IOException {
        byte[] profile = baseAvroConverter.encode(ENDPOINT_PROFILE);
        RegisterProfileRequest request = new RegisterProfileRequest(APP_TOKEN, ENDPOINT_KEY, sdkToken, profile);

        EndpointProfileDto oldDto = profileService.registerProfile(request);

        Assert.assertEquals(baseAvroConverter.encodeToJson(ENDPOINT_PROFILE), oldDto.getProfile().replaceAll(" ", ""));

        byte[] newProfile = newAvroConverter.encode(NEW_ENDPOINT_PROFILE);
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(APP_TOKEN,
                EndpointObjectHash.fromSHA1(ENDPOINT_KEY),
                null,
                newProfile,
                newSdkToken);
        EndpointProfileDto newDto = profileService.updateProfile(updateRequest);

        Assert.assertNotNull(newDto);
        Assert.assertNotNull(newDto.getId());
        Assert.assertEquals(oldDto.getId(), newDto.getId());
        Assert.assertEquals(newAvroConverter.encodeToJson(NEW_ENDPOINT_PROFILE), newDto.getProfile().replaceAll(" ", ""));
        Assert.assertTrue(Arrays.equals(EndpointObjectHash.fromSHA1(newProfile).getData(),
                newDto.getProfileHash()));
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
