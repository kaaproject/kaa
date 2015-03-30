/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class UserConfigurationServiceImplTest extends AbstractTest {

    private static final String OVERRIDE_USER_DATA_JSON = "dao/user/overrideData.json";

    @Before
    public void beforeTest() throws Exception {
        clearDBData();
    }

    @Test
    public void findUserConfigurationByUserIdAndAppTokenAndSchemaVersionTest() throws IOException {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        ConfigurationSchemaDto schema = generateConfSchema(appDto.getId(), 1).get(0);
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, schema, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfiguration(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfiguration(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        EndpointUserConfigurationDto found = userConfigurationService.findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), schema.getMajorVersion());
        Assert.assertEquals(firstUserConfigurationDto, found);
    }

    @Test
    public void findUserConfigurationByUserIdTest() throws IOException {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        EndpointUserConfigurationDto secondUserConfigurationDto = generateEndpointUserConfiguration(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        List<EndpointUserConfigurationDto> expectedList = new ArrayList<>();
        expectedList.add(firstUserConfigurationDto);
        expectedList.add(secondUserConfigurationDto);
        generateEndpointUserConfiguration(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        List<EndpointUserConfigurationDto> foundList = userConfigurationService.findUserConfigurationByUserId(userDto.getId());
        Assert.assertEquals(expectedList.size(), foundList.size());
    }

    @Test
    public void removeByUserIdAndAppTokenAndSchemaVersionTest() throws IOException {
        EndpointUserDto userDto = generateEndpointUser(null);
        ApplicationDto appDto = generateApplication();
        ConfigurationSchemaDto schema = generateConfSchema(appDto.getId(), 1).get(0);
        generateEndpointUserConfiguration(userDto, appDto, schema, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfiguration(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfiguration(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        userConfigurationService.removeByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), schema.getMajorVersion());
        EndpointUserConfigurationDto removed = userConfigurationService.findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), schema.getMajorVersion());
        Assert.assertNull(removed);
        List<EndpointUserConfigurationDto> foundList = userConfigurationService.findUserConfigurationByUserId(userDto.getId());
        Assert.assertEquals(2, foundList.size());
    }

    @Test
    public void saveUserConfigurationTest() throws IOException {
        EndpointUserConfigurationDto configurationDto = generateEndpointUserConfiguration(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        Assert.assertNotNull(configurationDto);
    }

    @Test
    public void userConfigNullTest() {
        EndpointUserConfigurationDto result = userConfigurationService.saveUserConfiguration(null);
        Assert.assertNull(result);
    }

    @Test
    public void userConfigNotNullTest() throws IOException {
        EndpointUserConfigurationDto configurationDto = generateEndpointUserConfiguration(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        EndpointUserConfigurationDto result = userConfigurationService.saveUserConfiguration(configurationDto);
        Assert.assertEquals(configurationDto, result);
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveUserConfigBlankTest() {
        EndpointUserConfigurationDto configurationDto = generateEndpointUserConfiguration(null, null, null, null);
        EndpointUserConfigurationDto result = userConfigurationService.saveUserConfiguration(null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void schemaDtoNullTest() throws IOException {
        EndpointUserConfigurationDto configurationDto = generateEndpointUserConfiguration(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        configurationDto.setAppToken(null);
        EndpointUserConfigurationDto result = userConfigurationService.saveUserConfiguration(configurationDto);
        Assert.assertEquals(configurationDto, result);
    }
}
