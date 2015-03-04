package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

import java.util.ArrayList;
import java.util.List;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class UserConfigurationServiceImplTest extends AbstractTest {

    @Before
    public void beforeTest() throws Exception {
        clearDBData();
    }

    @Test
    public void findUserConfigurationByUserIdAndAppTokenAndSchemaVersionTest() {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        int foundVersion = random.nextInt();
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, foundVersion);
        generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        generateEndpointUserConfiguration(null, null, null);
        EndpointUserConfigurationDto found = userConfigurationService.findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), foundVersion);
        Assert.assertEquals(firstUserConfigurationDto, found);
    }

    @Test
    public void findUserConfigurationByUserIdTest() {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        EndpointUserConfigurationDto secondUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        List<EndpointUserConfigurationDto> expectedList = new ArrayList<>();
        expectedList.add(firstUserConfigurationDto);
        expectedList.add(secondUserConfigurationDto);
        generateEndpointUserConfiguration(null, null, null);
        List<EndpointUserConfigurationDto> foundList = userConfigurationService.findUserConfigurationByUserId(userDto.getId());
        Assert.assertEquals(expectedList.size(), foundList.size());
        Assert.assertEquals(expectedList, foundList);
    }

    @Test
    public void removeByUserIdAndAppTokenAndSchemaVersionTest() {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        int removedVersion = random.nextInt();
        generateEndpointUserConfiguration(userDto, appDto, removedVersion);
        generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        generateEndpointUserConfiguration(userDto, appDto, random.nextInt());
        userConfigurationService.removeByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), removedVersion);
        EndpointUserConfigurationDto removed = userConfigurationService.findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), removedVersion);
        Assert.assertNull(removed);
        List<EndpointUserConfigurationDto> foundList = userConfigurationService.findUserConfigurationByUserId(userDto.getId());
        Assert.assertEquals(2, foundList.size());
    }
}
