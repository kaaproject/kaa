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

package org.kaaproject.kaa.server.common.dao.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointProfileSchema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class ProfileServiceImplTest extends AbstractTest {

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void findProfileSchemasByAppIdTest() {
        int profSchemaCount = 2;
        int profSchemaWithDefaultCount = profSchemaCount + 1;
        ApplicationDto application = generateApplicationDto(null);
        generateProfSchemaDto(application.getTenantId(), application.getId(), profSchemaCount);
        List<EndpointProfileSchemaDto> schemas = profileService.findProfileSchemasByAppId(application.getId());
        Assert.assertNotNull(schemas);
        Assert.assertEquals(profSchemaWithDefaultCount, schemas.size());
    }

    @Test
    public void findProfileSchemaByIdTest() {
        String schemaId = generateProfSchemaDto(null, null, 1).get(0).getId();
        EndpointProfileSchemaDto schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNotNull(schemaDto);
    }

    @Test
    public void saveProfileSchemaTest() {
        String schemaId = generateProfSchemaDto(null, null, 1).get(0).getId();
        EndpointProfileSchemaDto schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNotNull(schemaDto);
        int version = schemaDto.getVersion();
        schemaDto.setId(null);
        EndpointProfileSchemaDto saved = profileService.saveProfileSchema(schemaDto);
        Assert.assertNotNull(saved);
        Assert.assertNotEquals(version, saved.getVersion());
        Assert.assertNotEquals(schemaId, saved.getId());
    }

    @Test
    public void removeProfileSchemasByAppIdTest() {
        int profSchemaCount = 2;
        ApplicationDto application = generateApplicationDto(null);
        generateProfSchemaDto(application.getTenantId(), application.getId(), profSchemaCount);
        List<EndpointProfileSchemaDto> schemas = profileService.findProfileSchemasByAppId(application.getId());
        Assert.assertNotNull(schemas);
        Assert.assertFalse(schemas.isEmpty());

        profileService.removeProfileSchemasByAppId(application.getId());
        schemas = profileService.findProfileSchemasByAppId(application.getId());
        Assert.assertNotNull(schemas);
        Assert.assertTrue(schemas.isEmpty());
    }

    @Test
    public void removeProfileSchemaByIdTest() {
        String schemaId = generateProfSchemaDto(null, null, 1).get(0).getId();
        EndpointProfileSchemaDto schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNotNull(schemaDto);
        profileService.removeProfileSchemaById(schemaId);
        schemaDto = profileService.findProfileSchemaById(schemaId);
        Assert.assertNull(schemaDto);
    }

    @Test
    public void findProfileFilterByIdTest() {
        List<ProfileFilterDto> filters = generateFilterDto(null, null, null, 1, true);
        ProfileFilterDto filterDto = profileService.findProfileFilterById(filters.get(0).getId());
        Assert.assertNotNull(filterDto);
        Assert.assertEquals(filters.get(0), filterDto);
    }

    @Test
    public void activateProfileFilterTest() {
        List<ProfileFilterDto> filters = generateFilterDto(null, null, null, 1, false);
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

    @Test(expected = UpdateStatusConflictException.class)
    public void activateProfileFilterActiveStatusTest() {
        List<ProfileFilterDto> filters = generateFilterDto(null, null, null, 1, true);
        ProfileFilterDto filterDto = filters.get(0);
        ChangeProfileFilterNotification activated = profileService.activateProfileFilter(filterDto.getId(), null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void activateProfileFilterNoProfileFilterTest() {
        ChangeProfileFilterNotification activated = profileService.activateProfileFilter("13232", null);
    }

    @Test
    public void findProfileFilterByAppIdAndVersionTest() {
        List<ProfileFilterDto> generated = generateFilterDto(null, null, null, 1, true);
        ProfileFilterDto first = generated.get(0);
        List<ProfileFilterDto> filters = profileService.findProfileFiltersByAppIdAndVersionsCombination(first.getApplicationId(), first.getEndpointProfileSchemaVersion(),
                first.getServerProfileSchemaVersion());
        Assert.assertNotNull(filters);
        Assert.assertFalse(filters.isEmpty());
    }

    @Test
    public void findProfileSchemaByAppIdTest() {
        ApplicationDto application = generateApplicationDto(null);
        List<EndpointProfileSchemaDto> schemas = profileService.findProfileSchemasByAppId(application.getId());
        Assert.assertNotNull(schemas);
        Assert.assertFalse(schemas.isEmpty());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveFilterWithIncorrectIdTestFail() {
        ProfileFilterDto dto = new ProfileFilterDto();
        Assert.assertNotNull(dto);
        //dto.setId(INCORRECT_ID);
        profileService.saveProfileFilter(dto);
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void saveFilterWithIncorrectStatusTestFail() {
        List<ProfileFilterDto> filters = generateFilterDto(null, null, null, 1, true);
        ProfileFilterDto filterDto = filters.get(0);
        Assert.assertNotNull(filterDto);
        profileService.saveProfileFilter(filterDto);
    }

    @Test
    public void saveFilterObjectWithIdTest() {
        List<ProfileFilterDto> filters = generateFilterDto(null, null, null, 1, false);
        ProfileFilterDto filterDto = filters.get(0);
        filterDto.setStatus(null);
        ProfileFilterDto saved = profileService.saveProfileFilter(filterDto);
        saved = profileService.saveProfileFilter(saved);

        Assert.assertNotNull(saved);
        Assert.assertEquals(saved.getStatus(), UpdateStatus.INACTIVE);
    }

    @Test
    public void saveFilterObjectWithoutIdTest() {
        List<ProfileFilterDto> filters = generateFilterDto(null, null, null, 1, false);
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
        ProfileFilterDto filterDto = generateFilterDto(null, null, null, 1, true).get(0);
        filterDto.setId(null);
        ProfileFilterDto saved = profileService.saveProfileFilter(filterDto);
        Assert.assertNotNull(saved);
        Assert.assertEquals(saved.getStatus(), UpdateStatus.INACTIVE);
        Assert.assertEquals(saved.getSequenceNumber(), filterDto.getSequenceNumber());
        Assert.assertNotEquals(saved.getId(), filterDto.getId());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveFilterObjectWithIncorrectSchemaIdTest() {
        ProfileFilterDto filterDto = generateFilterDto(null, null, null, 1, false).get(0);
        filterDto.setId(null);
        filterDto.setEndpointProfileSchemaId("100500");
        profileService.saveProfileFilter(filterDto);
    }

    @Test
    public void saveMoreFiltersTest() {
        List<EndpointProfileSchemaDto> schemas = generateProfSchemaDto(null, null, 1);
        EndpointProfileSchemaDto schema = schemas.get(0);
        ApplicationDto app = applicationService.findAppById(schema.getApplicationId());
        ServerProfileSchemaDto serverSchema = generateServerProfileSchema(app.getId(), app.getTenantId(), 1);
        generateFilterDto(schema.getId(), serverSchema.getId(), null, 10, true);
        List<ProfileFilterDto> filters = profileService.findProfileFiltersByAppIdAndVersionsCombination(schema.getApplicationId(), schema.getVersion(), serverSchema.getVersion());
        Assert.assertFalse(filters.isEmpty());
        Assert.assertEquals(filters.get(0).getStatus(), UpdateStatus.ACTIVE);
        Assert.assertEquals(schema.getId(), filters.get(0).getEndpointProfileSchemaId());
        Assert.assertEquals(serverSchema.getId(), filters.get(0).getServerProfileSchemaId());
    }

    @Test
    public void saveFiltersWithDiferentGroupsTest() {
        ApplicationDto application = generateApplicationDto(null);
        EndpointGroupDto groupDto = generateEndpointGroupDto(application.getId());
        EndpointGroupDto groupDto2 = generateEndpointGroupDto(application.getId(), "OTHER_GROUP");

        EndpointProfileSchemaDto schema = generateProfSchemaDto(application.getTenantId(), application.getId(), 1).get(0);
        ServerProfileSchemaDto serverSchema = generateServerProfileSchema(application.getId(), application.getTenantId());

        ProfileFilterDto filterDto = generateFilterDto(schema.getId(), serverSchema.getId(), groupDto.getId(), 1, false).get(0);
        Assert.assertNotNull(filterDto);

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

        List<ProfileFilterDto> activeList = profileService.findProfileFiltersByAppIdAndVersionsCombination(application.getId(), schema.getVersion(), serverSchema.getVersion());
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
        ApplicationDto app = generateApplicationDto(null);
        EndpointProfileSchemaDto schema = generateProfSchemaDto(app.getTenantId(), app.getId(), 1).get(0);
        ServerProfileSchemaDto serverSchema = generateServerProfileSchema(app.getId(), app.getTenantId());
        EndpointGroupDto defaultGroup = endpointService.findDefaultGroup(app.getId());
        ProfileFilterDto filter = new ProfileFilterDto();
        filter.setApplicationId(app.getId());
        filter.setBody("true");
        filter.setEndpointProfileSchemaId(schema.getId());
        filter.setServerProfileSchemaId(serverSchema.getId());
        filter.setEndpointGroupId(defaultGroup.getId());
        filter = profileService.saveProfileFilter(filter);
        profileService.activateProfileFilter(filter.getId(), "test");
    }

    @Test
    public void findProfileSchemaVersionsByAppIdTest() {
        String applicationId = generateApplicationDto(null).getId();
        List<VersionDto> versions = profileService.findProfileSchemaVersionsByAppId(applicationId);
        Assert.assertNotNull(versions);
        Assert.assertFalse(versions.isEmpty());
        Assert.assertEquals(1, versions.size());
        Assert.assertSame(0, versions.get(0).getVersion());
    }

    @Test
    public void findProfileSchemaByAppIdAndVersionTest() {
        String appId = generateApplicationDto(null).getId();
        int schemaVersion = 0;
        EndpointProfileSchemaDto schema = profileService.findProfileSchemaByAppIdAndVersion(appId, schemaVersion);
        Assert.assertNotNull(schema);
        Assert.assertEquals(schemaVersion, schema.getVersion());
        Assert.assertEquals(appId, schema.getApplicationId());
    }

    @Test
    public void findAllProfileFilterRecordsByEndpointGroupIdTest() {
        ApplicationDto app = generateApplicationDto(null);
        String appId = app.getId();
        EndpointGroupDto groupDto = generateEndpointGroupDto(appId);
        EndpointProfileSchemaDto schema = generateProfSchemaDto(app.getTenantId(), appId, 1).get(0);
        ServerProfileSchemaDto serverProfileSchema = generateServerProfileSchema(app.getId(), app.getTenantId());
        ProfileFilterDto filter = generateFilterDto(schema.getId(), serverProfileSchema.getId(), groupDto.getId(), 3, true).get(2);
        Assert.assertEquals(UpdateStatus.ACTIVE, filter.getStatus());
        profileService.deactivateProfileFilter(filter.getId(), null);
        List<ProfileFilterRecordDto> records = (List<ProfileFilterRecordDto>) profileService.findAllProfileFilterRecordsByEndpointGroupId(groupDto.getId(), true);
        Assert.assertNotNull(records);
        Assert.assertEquals(1, records.size());
        Assert.assertEquals(UpdateStatus.DEPRECATED, records.get(0).getActiveStructureDto().getStatus());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveProfileFilterWithIncorrectSchemaIdTest() {
        ApplicationDto app = generateApplicationDto(null);
        String appId = app.getId();
        ProfileFilterDto filter = new ProfileFilterDto();
        filter.setId(null);
        filter.setApplicationId(appId);
        filter.setBody("true");
        filter.setEndpointProfileSchemaId(null);
        filter.setServerProfileSchemaId(null);
        profileService.saveProfileFilter(filter);
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveProfileFilterWithIncorrectGroupIdTest() {
        ApplicationDto app = generateApplicationDto(null);
        String appId = app.getId();
        EndpointProfileSchemaDto schema = generateProfSchemaDto(app.getTenantId(), appId, 1).get(0);
        ServerProfileSchemaDto serverProfileSchema = generateServerProfileSchema(app.getId(), app.getTenantId());
        ProfileFilterDto filter = new ProfileFilterDto();
        filter.setId(null);
        filter.setApplicationId(appId);
        filter.setEndpointGroupId(null);
        filter.setBody("true");
        filter.setEndpointProfileSchemaId(schema.getId());
        filter.setServerProfileSchemaId(serverProfileSchema.getId());
        profileService.saveProfileFilter(filter);
    }

    @Test
    public void deactivateProfileFilterTest() {
        List<ProfileFilterDto> filterList = generateFilterDto(null, null, null, 1, true);
        ProfileFilterDto pf = filterList.get(0);
        String appId = pf.getApplicationId();

        List<ProfileFilterDto> filters = profileService.findProfileFiltersByAppIdAndVersionsCombination(pf.getApplicationId(),
                pf.getEndpointProfileSchemaVersion(), pf.getServerProfileSchemaVersion());

        Assert.assertNotNull(filters);
        ProfileFilterDto filter = filters.get(0);
        filter.setId(null);
        EndpointGroupDto group = generateEndpointGroupDto(appId);
        filter.setEndpointGroupId(group.getId());
        ProfileFilterDto dto = profileService.saveProfileFilter(filter);
        ChangeProfileFilterNotification change = profileService.activateProfileFilter(dto.getId(), null);
        Assert.assertNotNull(change);
        Assert.assertEquals(UpdateStatus.ACTIVE, change.getProfileFilterDto().getStatus());
        change = profileService.deactivateProfileFilter(dto.getId(), null);
        Assert.assertNotNull(change);
        Assert.assertEquals(UpdateStatus.DEPRECATED, change.getProfileFilterDto().getStatus());
    }

    @Test(expected = IncorrectParameterException.class)
    public void invalidProfileFilterActivationTest() {
        String id = "-1";
        profileService.activateProfileFilter(id, null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void invalidProfileFilterDeactivationTest() {
        String id = "-1";
        profileService.deactivateProfileFilter(id, null);
    }

    @Test
    public void updateExistingProfileSchemaTest() {
        ApplicationDto app = generateApplicationDto(null);
        String appId = app.getId();
        List<EndpointProfileSchemaDto> schemas = generateProfSchemaDto(app.getTenantId(), appId, 1);
        EndpointProfileSchemaDto schema = profileService.findProfileSchemaById(schemas.get(0).getId());
        Assert.assertNotNull(schema);
        EndpointProfileSchema endpointProfileSchema = new EndpointProfileSchema(schema);
        endpointProfileSchema.setName("Updated name");
        endpointProfileSchema.setDescription("Updated description");
        EndpointProfileSchemaDto saved = profileService.saveProfileSchema(endpointProfileSchema.toDto());
        Assert.assertNotNull(saved);
        Assert.assertEquals(schema.getId(), saved.getId());
        Assert.assertEquals(schema.getApplicationId(), saved.getApplicationId());
        Assert.assertNotEquals(schema.getName(), saved.getName());
        Assert.assertNotEquals(schema.getDescription(), saved.getDescription());
    }

    @Test(expected = IncorrectParameterException.class)
    public void updateExistingProfileSchemaWithIncorrectIdTest() {
        String appId = generateApplicationDto(null).getId();
        EndpointProfileSchemaDto profileSchema = new EndpointProfileSchemaDto();
        profileSchema.setId("-1");
        profileSchema.setApplicationId(appId);
        profileSchema.setName("Updated name");
        profileSchema.setDescription("Updated description");
        profileService.saveProfileSchema(profileSchema);
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateFilterNullDtoTest() throws Throwable {
        ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl();
        Method validateMethod = profileServiceImpl.getClass().getDeclaredMethod("validateFilter", ProfileFilterDto.class);
        validateMethod.setAccessible(true);
        try {
            validateMethod.invoke(profileServiceImpl, new ProfileFilterDto[]{null});
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateFilterBlankSchemaIdTest() throws Throwable {
        ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl();
        Method validateMethod = profileServiceImpl.getClass().getDeclaredMethod("validateFilter", ProfileFilterDto.class);
        validateMethod.setAccessible(true);
        ProfileFilterDto filterDto = new ProfileFilterDto();
        try {
            validateMethod.invoke(profileServiceImpl, filterDto);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateFilterBlankEndpointGroupIdTest() throws Throwable {
        ProfileServiceImpl profileServiceImpl = new ProfileServiceImpl();
        Method validateMethod = profileServiceImpl.getClass().getDeclaredMethod("validateFilter", ProfileFilterDto.class);
        validateMethod.setAccessible(true);
        ProfileFilterDto filterDto = new ProfileFilterDto();
        filterDto.setEndpointGroupId("");
        try {
            validateMethod.invoke(profileServiceImpl, filterDto);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
