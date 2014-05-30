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

package org.kaaproject.kaa.server.common.dao.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
import org.kaaproject.kaa.server.common.dao.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProfileServiceImplTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImplTest.class);

    @Autowired
    private ProfileService profileService;

    private static final String INCORRECT_ID = "7";

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
        LOGGER.info("ConfigurationMongoDao init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findProfileSchemasByAppIdTest() {
        String appId = apps.get(0);
        List<ProfileSchemaDto> schemas = profileService.findProfileSchemasByAppId(appId);
        Assert.assertNotNull(schemas);
        Assert.assertFalse(schemas.isEmpty());
    }

    @Test
    public void findProfileSchemaByIdTest() {
        String schemaId = generateProfSchema(null, 1).get(0).getId();
        ProfileSchemaDto schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNotNull(schemaDto);
    }

    @Test
    public void saveProfileSchemaTest() {
        String schemaId = generateProfSchema(null, 1).get(0).getId();
        ProfileSchemaDto schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNotNull(schemaDto);
        int majorVersion = schemaDto.getMajorVersion();
        schemaDto.setId(null);
        ProfileSchemaDto saved = profileService.saveProfileSchema(schemaDto);
        Assert.assertNotNull(saved);
        Assert.assertNotEquals(majorVersion, saved.getMajorVersion());
        Assert.assertNotEquals(schemaId, saved.getId());
    }

    @Test
    public void removeProfileSchemasByAppIdTest() {
        String appId = apps.get(0);
        List<ProfileSchemaDto> schemas = profileService.findProfileSchemasByAppId(appId);
        Assert.assertNotNull(schemas);
        Assert.assertFalse(schemas.isEmpty());

        profileService.removeProfileSchemasByAppId(appId);
        schemas = profileService.findProfileSchemasByAppId(appId);
        Assert.assertNotNull(schemas);
        Assert.assertTrue(schemas.isEmpty());
    }

    @Test
    public void removeProfileSchemaByIdTest() {
        String schemaId = generateProfSchema(null, 1).get(0).getId();
        ProfileSchemaDto schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNotNull(schemaDto);
        profileService.removeProfileSchemaById(schemaId);
        schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNull(schemaDto);
    }

    @Test
    public void findProfileFilterByIdTest() {
        List<ProfileFilterDto> filters = generateFilter(null, null, 1, true);
        ProfileFilterDto filterDto = profileService.findProfileFilterById(filters.get(0).getId());
        Assert.assertNotNull(filterDto);
        Assert.assertEquals(filters.get(0), filterDto);
    }

    @Test
    public void activateProfileFilterTest() {
        List<ProfileFilterDto> filters = generateFilter(null, null, 1, false);
        ProfileFilterDto filterDto = filters.get(0);
        Assert.assertNotNull(filterDto);
        Assert.assertEquals(UpdateStatus.INACTIVE, filterDto.getStatus());
        ChangeProfileFilterNotification activated = profileService.activateProfileFilter(filterDto.getId(), null);
        Assert.assertNotNull(activated);
        ProfileFilterDto profileFilterDto = activated.getProfileFilterDto();
        Assert.assertNotNull(profileFilterDto);
        Assert.assertEquals(UpdateStatus.ACTIVE, profileFilterDto.getStatus());
        Assert.assertNotEquals(filterDto.getSequenceNumber(), profileFilterDto.getSequenceNumber());
    }

    @Test
    public void removeProfileFiltersByProfileSchemaIdTest() {
        List<ProfileSchemaDto> schemas = generateProfSchema(null, 1);
        ProfileSchemaDto schema = schemas.get(0);
        List<ProfileFilterDto> filters = profileService.findProfileFilterByAppIdAndVersion(schema.getApplicationId(), schema.getMajorVersion());
        Assert.assertNotNull(filters);
        Assert.assertFalse(filters.isEmpty());
        profileService.removeProfileFiltersByProfileSchemaId(schema.getId());
        filters = profileService.findProfileFilterByAppIdAndVersion(schema.getApplicationId(), schema.getMajorVersion());
        Assert.assertNotNull(filters);
        Assert.assertTrue(filters.isEmpty());
    }

    @Test
    public void findProfileFilterByAppIdAndVersionTest() {
        List<ProfileFilterDto> filters = profileService.findProfileFilterByAppIdAndVersion(apps.get(0), 1);
        Assert.assertNotNull(filters);
        Assert.assertFalse(filters.isEmpty());
    }

    @Test
    public void findProfileSchemaByAppIdTest() {
        List<ProfileSchemaDto> schemas = profileService.findProfileSchemasByAppId(apps.get(0));
        Assert.assertNotNull(schemas);
        Assert.assertFalse(schemas.isEmpty());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveFilterWithIncorrectIdTestFail() {
        ProfileFilterDto dto = new ProfileFilterDto();
        Assert.assertNotNull(dto);
        dto.setId(INCORRECT_ID);
        profileService.saveProfileFilter(dto);
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void saveFilterWithIncorrectStatusTestFail() {
        List<ProfileFilterDto> filters = generateFilter(null, null, 1, true);
        ProfileFilterDto filterDto = filters.get(0);
        Assert.assertNotNull(filterDto);
        profileService.saveProfileFilter(filterDto);
    }

    @Test
    public void saveFilterObjectWithIdTest() {
        List<ProfileFilterDto> filters = generateFilter(null, null, 1, false);
        ProfileFilterDto filterDto = filters.get(0);
        filterDto.setStatus(null);
        ProfileFilterDto saved = profileService.saveProfileFilter(filterDto);

        Assert.assertNotNull(saved);
        Assert.assertEquals(saved.getStatus(), UpdateStatus.INACTIVE);
    }

    @Test
    public void saveFilterObjectWithoutIdTest() {
        List<ProfileFilterDto> filters = generateFilter(null, null, 1, false);
        ProfileFilterDto filterDto = filters.get(0);
        String inactiveId = filterDto.getId();
        filterDto.setId(null);
        ProfileFilterDto saved = profileService.saveProfileFilter(filterDto);
        Assert.assertNotNull(saved);
        Assert.assertEquals(saved.getStatus(), UpdateStatus.INACTIVE);
        Assert.assertEquals(inactiveId, saved.getId());
    }

    @Test
    public void saveFilterObjectWithoutInactiveFilterTest() {
        ProfileFilterDto filterDto = generateFilter(null, null, 1, true).get(0);
        // remove inactive profile filter

        filterDto.setId(null);
        ProfileFilterDto saved = profileService.saveProfileFilter(filterDto);
        Assert.assertNotNull(saved);
        Assert.assertEquals(saved.getStatus(), UpdateStatus.INACTIVE);
        Assert.assertEquals(saved.getSequenceNumber(), filterDto.getSequenceNumber());
        Assert.assertNotEquals(saved.getId(), filterDto.getId());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveFilterObjectWithIncorrectSchemaIdTest() {
        ProfileFilterDto filterDto = generateFilter(null, null, 1, false).get(0);
        filterDto.setId(null);
        filterDto.setSchemaId(new ObjectId().toString());
        Assert.assertNotNull(filterDto);
        filterDto.setId(null);
        filterDto.setSchemaId(filterDto.getApplicationId());
        profileService.saveProfileFilter(filterDto);
    }

    @Test
    public void saveMoreFiltersTest() {
        List<ProfileSchemaDto> schemas = generateProfSchema(null, 1);
        ProfileSchemaDto schema = schemas.get(0);
        generateFilter(schema.getId(), null, 10, true);
        List<ProfileFilterDto> filters = profileService.findProfileFilterByAppIdAndVersion(schema.getApplicationId(), schema.getMajorVersion());
        Assert.assertFalse(filters.isEmpty());
        Assert.assertEquals(filters.get(0).getStatus(), UpdateStatus.ACTIVE);
        Assert.assertEquals(schema.getId(), filters.get(0).getSchemaId());
    }

    @Test
    public void saveFiltersWithDiferentGroupsTest() {
        ApplicationDto application = generateApplication();
        EndpointGroupDto groupDto = generateEndpointGroup(application.getId());
        EndpointGroupDto groupDto2 = generateEndpointGroup(application.getId());

        ProfileSchemaDto schema = generateProfSchema(application.getId(), 1).get(0);

        ProfileFilterDto filterDto = generateFilter(schema.getId(), groupDto.getId(), 1, false).get(0);
        Assert.assertNotNull(filterDto);

        profileService.removeProfileFiltersByProfileSchemaId(schema.getId());

        filterDto.setId(null);
        filterDto.setEndpointGroupId(groupDto.getId());
        ProfileFilterDto savedOne = profileService.saveProfileFilter(filterDto);
        Assert.assertNotNull(savedOne);
        ChangeProfileFilterNotification activeOne = profileService.activateProfileFilter(savedOne.getId(), null);
        Assert.assertNotNull(activeOne);

        filterDto.setId(null);
        filterDto.setEndpointGroupId(groupDto2.getId());
        ProfileFilterDto savedTwo = profileService.saveProfileFilter(filterDto);
        Assert.assertNotNull(savedTwo);
        ChangeProfileFilterNotification activeTwo = profileService.activateProfileFilter(savedTwo.getId(), null);
        Assert.assertNotNull(activeTwo);

        List<ProfileFilterDto> activeList = profileService.findProfileFilterByAppIdAndVersion(application.getId(), schema.getMajorVersion());
        Assert.assertFalse(activeList.isEmpty());
        Set<String> groupIds = new HashSet<>();
        for (ProfileFilterDto dto : activeList) {
            Assert.assertEquals(dto.getStatus(), UpdateStatus.ACTIVE);
            groupIds.add(dto.getEndpointGroupId());
        }
        Assert.assertEquals(2, groupIds.size());
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void updateDefaultProfileFilter() {
        String appId = generateApplication().getId();
        List<ProfileFilter> filters = profileFilterDao.findByAppIdAndSchemaVersion(appId, 1);
        Assert.assertNotNull(filters);
        ProfileFilter filter = filters.get(0);
        filter.setId(null);
        filter.setBody("false");
        profileService.saveProfileFilter(filter.toDto());
    }

    @Test(expected = IncorrectParameterException.class)
    public void deactivateDefaultProfileFilter() {
        String appId = generateApplication().getId();
        List<ProfileFilter> filters = profileFilterDao.findByAppIdAndSchemaVersion(appId, 1);
        Assert.assertNotNull(filters);
        ProfileFilter filter = filters.get(0);
        profileService.deactivateProfileFilter(filter.getId(), null);
    }

    @Test
    public void findProfileSchemaVersionsByAppIdTest() {
        String applicationId = generateApplication().getId();
        List<SchemaDto> versions = profileService.findProfileSchemaVersionsByAppId(applicationId);
        Assert.assertNotNull(versions);
        Assert.assertFalse(versions.isEmpty());
        Assert.assertEquals(1, versions.size());
        Assert.assertSame(1, versions.get(0).getMajorVersion());
    }

    @Test
    public void findProfileSchemaByAppIdAndVersionTest() {
        String appId = generateApplication().getId();
        int schemaVersion = 1;
        ProfileSchemaDto schema = profileService.findProfileSchemaByAppIdAndVersion(appId, schemaVersion);
        Assert.assertNotNull(schema);
        Assert.assertEquals(schemaVersion, schema.getMajorVersion());
        Assert.assertEquals(appId, schema.getApplicationId());
    }

    @Test
    public void findAllProfileFilterRecordsByEndpointGroupIdTest() {
        String appId = generateApplication().getId();
        EndpointGroupDto groupDto = generateEndpointGroup(appId);
        ProfileSchemaDto schema = generateProfSchema(appId, 1).get(0);
        ProfileFilterDto filter = generateFilter(schema.getId(), groupDto.getId(), 3, true).get(2);
        Assert.assertEquals(UpdateStatus.ACTIVE, filter.getStatus());
        profileService.deactivateProfileFilter(filter.getId(), null);
        List<StructureRecordDto<ProfileFilterDto>> records = (List<StructureRecordDto<ProfileFilterDto>>)
                profileService.findAllProfileFilterRecordsByEndpointGroupId(groupDto.getId(), true);
        Assert.assertNotNull(records);
        Assert.assertEquals(1, records.size());
        Assert.assertEquals(UpdateStatus.DEPRECATED, records.get(0).getActiveStructureDto().getStatus());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveProfileFilterWithIncorrectSchemaIdTest() {
        String appId = generateApplication().getId();
        int schemaVersion = 1;
        List<ProfileFilterDto> filters = profileService.findProfileFilterByAppIdAndVersion(appId, schemaVersion);
        Assert.assertFalse(filters.isEmpty());
        ProfileFilterDto filter = filters.get(0);
        filter.setId(null);
        filter.setSchemaId(null);
        profileService.saveProfileFilter(filter);
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveProfileFilterWithIncorrectGroupIdTest() {
        String appId = generateApplication().getId();
        int schemaVersion = 1;
        List<ProfileFilterDto> filters = profileService.findProfileFilterByAppIdAndVersion(appId, schemaVersion);
        Assert.assertFalse(filters.isEmpty());
        ProfileFilterDto filter = filters.get(0);
        filter.setId(null);
        filter.setEndpointGroupId(null);
        profileService.saveProfileFilter(filter);
    }

    @Test
    public void deactivateProfileFilterTest() {
        String appId = generateApplication().getId();
        List<ProfileFilter> filters = profileFilterDao.findByAppIdAndSchemaVersion(appId, 1);
        Assert.assertNotNull(filters);
        ProfileFilter filter = filters.get(0);
        filter.setId(null);
        String groupId = generateEndpointGroup(appId).getId();
        filter.setEndpointGroupId(new ObjectId(groupId));
        ProfileFilterDto dto = profileService.saveProfileFilter(filter.toDto());
        ChangeProfileFilterNotification change = profileService.activateProfileFilter(dto.getId(), null);
        Assert.assertNotNull(change);
        Assert.assertEquals(UpdateStatus.ACTIVE, change.getProfileFilterDto().getStatus());
        change = profileService.deactivateProfileFilter(dto.getId(), null);
        Assert.assertNotNull(change);
        Assert.assertEquals(UpdateStatus.DEPRECATED, change.getProfileFilterDto().getStatus());
    }

    @Test(expected = IncorrectParameterException.class)
    public void invalidProfileFilterActivationTest() {
        String id = new ObjectId().toString();
        profileService.activateProfileFilter(id, null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void invalidProfileFilterDeactivationTest() {
        String id = new ObjectId().toString();
        profileService.deactivateProfileFilter(id, null);
    }

    @Test
    public void updateExistingProfileSchemaTest() {
        String appId = generateApplication().getId();
        List<ProfileSchemaDto> schemas = generateProfSchema(appId, 1);
        ProfileSchemaDto schema = profileService.findProfileSchemaById(schemas.get(0).getId());
        Assert.assertNotNull(schema);
        ProfileSchema profileSchema = new ProfileSchema(schema);
        profileSchema.setName("Updated name");
        profileSchema.setDescription("Updated description");
        ProfileSchemaDto saved = profileService.saveProfileSchema(profileSchema.toDto());
        Assert.assertNotNull(saved);
        Assert.assertEquals(schema.getId(), saved.getId());
        Assert.assertEquals(schema.getApplicationId(), saved.getApplicationId());
        Assert.assertNotEquals(schema.getName(), saved.getName());
        Assert.assertNotEquals(schema.getDescription(), saved.getDescription());
    }

    @Test(expected = IncorrectParameterException.class)
    public void updateExistingProfileSchemaWithIncorrectIdTest() {
        ProfileSchemaDto profileSchema = new ProfileSchemaDto();
        profileSchema.setId(new ObjectId().toString());
        profileSchema.setName("Updated name");
        profileSchema.setDescription("Updated description");
        profileService.saveProfileSchema(profileSchema);
    }

}
