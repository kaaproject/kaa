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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointUserConfigurationMongoDaoTest extends AbstractMongoTest {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserConfigurationMongoDaoTest.class);
    private static final String OVERRIDE_USER_DATA_JSON = "dao/user/overrideData.json";

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() throws IOException {
        MongoDataLoader.clearDBData();
    }

    @Test
    public void saveEndpointUserConfigurationDtoTest() throws IOException {
        EndpointUserConfigurationDto userConfigurationDto = generateEndpointUserConfigurationDto(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        Assert.assertNotNull(userConfigurationDto);
        Assert.assertEquals(userConfigurationDto, new MongoEndpointUserConfiguration(userConfigurationDto).toDto());
    }

    @Test
    public void findByUserIdAndAppTokenAndSchemaVersionTest() throws IOException {
        EndpointUserDto userDto = generateEndpointUserDto(null);
        ApplicationDto appDto = generateApplicationDto();
        ConfigurationSchemaDto schema = generateConfSchemaDto(null, appDto.getId(),1).get(0);
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfigurationDto(userDto, appDto, schema, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfigurationDto(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfigurationDto(null, null, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        MongoEndpointUserConfiguration found = endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), schema.getVersion());
        Assert.assertEquals(firstUserConfigurationDto, found.toDto());
    }

    @Test
    public void removeByUserIdAndAppTokenAndSchemaVersionTest() throws IOException {
        EndpointUserDto userDto = generateEndpointUserDto(null);
        ApplicationDto appDto = generateApplicationDto();
        ConfigurationSchemaDto configurationSchemaDto = generateConfSchemaDto(null, appDto.getId(),1).get(0);
        generateEndpointUserConfigurationDto(userDto, appDto, configurationSchemaDto, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfigurationDto(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        generateEndpointUserConfigurationDto(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        endpointUserConfigurationDao.removeByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), configurationSchemaDto.getVersion());
        MongoEndpointUserConfiguration removed = endpointUserConfigurationDao.findByUserIdAndAppTokenAndSchemaVersion(userDto.getId(), appDto.getApplicationToken(), configurationSchemaDto.getVersion());
        Assert.assertNull(removed);
        List<MongoEndpointUserConfiguration> foundList = endpointUserConfigurationDao.findByUserId(userDto.getId());
        Assert.assertEquals(2, foundList.size());
    }

    @Test
    @Ignore("invalid")
    public void findByUserIdTest() throws IOException {
        EndpointUserDto userDto = generateEndpointUserDto(null);
        ApplicationDto appDto = generateApplicationDto();
        EndpointUserConfigurationDto firstUserConfigurationDto = generateEndpointUserConfigurationDto(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        EndpointUserConfigurationDto secondUserConfigurationDto = generateEndpointUserConfigurationDto(userDto, appDto, null, readSchemaFileAsString(OVERRIDE_USER_DATA_JSON));
        List<MongoEndpointUserConfiguration> expectedList = new ArrayList<>();
        expectedList.add(new MongoEndpointUserConfiguration(firstUserConfigurationDto));
        expectedList.add(new MongoEndpointUserConfiguration(secondUserConfigurationDto));
        generateEndpointUserConfigurationDto(null, null, null);
        List<MongoEndpointUserConfiguration> foundList = endpointUserConfigurationDao.findByUserId(userDto.getId());
        Assert.assertEquals(expectedList.size(), foundList.size());
        Assert.assertEquals(expectedList, foundList);
    }
}
