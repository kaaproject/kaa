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
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointServiceImplTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointServiceImplTest.class);

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
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findEndpointGroupsByAppIdTest() {
        List<EndpointGroupDto> groupDtoList = endpointService.findEndpointGroupsByAppId(apps.get(0));
        Assert.assertNotNull(groupDtoList);
        Assert.assertFalse(groupDtoList.isEmpty());
    }

    @Test(expected = IncorrectParameterException.class)
    public void findEndpointGroupByIdTest() {
        EndpointGroupDto group = endpointService.findEndpointGroupById(endGroups.get(0));
        Assert.assertNotNull(group);
        group = endpointService.findEndpointGroupById(INCORRECT_ID);
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveEndpointGroupWithSameWeightTest() {
        EndpointGroupDto found = endpointService.findEndpointGroupById(endGroups.get(0));
        found.setId(null);
        endpointService.saveEndpointGroup(found);
    }

    @Test
    public void removeEndpointGroupByAppIdTest() {
        String appId = apps.get(0);
        List<EndpointGroupDto> groupDtoList = endpointService.findEndpointGroupsByAppId(appId);
        Assert.assertNotNull(groupDtoList);
        Assert.assertFalse(groupDtoList.isEmpty());
        endpointService.removeEndpointGroupByAppId(appId);
        groupDtoList = endpointService.findEndpointGroupsByAppId(appId);
        Assert.assertNotNull(groupDtoList);
        Assert.assertTrue(groupDtoList.isEmpty());
    }

    @Test
    public void removeEndpointGroupByIdTest() {

    }

    @Test(expected = IncorrectParameterException.class)
    public void invalidUpdateEndpointGroupTest() {
        ApplicationDto app = generateApplication();
        List<EndpointGroupDto> groups = endpointService.findEndpointGroupsByAppId(app.getId());
        Assert.assertFalse(groups.isEmpty());
        EndpointGroupDto group = groups.get(0);
        group.setName("Updated Group Name");
        endpointService.saveEndpointGroup(group);
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveEndpointGroupWithExistingWeightTest() {
        ApplicationDto app = generateApplication();
        List<EndpointGroupDto> groups = endpointService.findEndpointGroupsByAppId(app.getId());
        Assert.assertFalse(groups.isEmpty());
        EndpointGroupDto group = groups.get(0);
        group.setId(null);
        group.setName("Updated Group Name");
        endpointService.saveEndpointGroup(group);
    }

//    @Test
//    public void findEndpointConfigurationByHashTest() {
//
//    }
//
//    @Test
//    public void removeEndpointConfigurationByHashTest() {
//
//    }
//
//    @Test
//    public void findEndpointConfigurationByIdTest() {
//
//    }
//
//    @Test
//    public void removeEndpointConfigurationByIdTest() {
//
//    }
//
//    @Test
//    public void saveEndpointConfigurationTest() {
//
//    }
//
//    @Test
//    public void findEndpointProfileByKeyHashTest() {
//
//    }
//
//    @Test
//    public void findEndpointProfileByIdTest() {
//
//    }
//
//    @Test
//    public void removeEndpointProfileByKeyHashTest() {
//
//    }
//
//    @Test
//    public void removeEndpointProfileByIdTest() {
//
//    }
//
//    @Test
//    public void saveEndpointProfileTest() {
//
//    }
}
