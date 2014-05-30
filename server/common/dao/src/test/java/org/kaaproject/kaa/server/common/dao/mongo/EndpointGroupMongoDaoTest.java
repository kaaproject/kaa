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

package org.kaaproject.kaa.server.common.dao.mongo;


import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.common.dao.mongo.model.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointGroupMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointGroupMongoDaoTest.class);

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        MongoDataLoader.loadData();
        application = applicationDao.findById(apps.get(0));
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }


    @Test
    public void saveEndpointGroupTest() {
        EndpointGroup found = endpointGroupDao.findById(endGroups.get(0));
        found.setId(null);
        found.setWeight(found.getWeight() + 1);
        EndpointGroup dto = endpointGroupDao.save(found);
        Assert.assertEquals(dto, found);
    }

    @Test
    public void findEndpointGroupByApplicationIdTest() {
        List<EndpointGroup> dto = endpointGroupDao.findByApplicationId(application.getId().toString());
        Assert.assertSame(dto.size(), 2);
    }

    @Test
    public void removeByIdTest() {
        String id = endGroups.get(0);
        EndpointGroup found = endpointGroupDao.findById(id);
        Assert.assertNotNull(found);
        endpointGroupDao.removeById(id);
        EndpointGroup dto = endpointGroupDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void removeByApplicationIdTest() {
        String id = apps.get(0);
        List<EndpointGroup> found = endpointGroupDao.findByApplicationId(id);
        Assert.assertNotNull(found);
        Assert.assertSame(found.size(), 2);
        endpointGroupDao.removeByApplicationId(id);
        List<EndpointGroup> dto = endpointGroupDao.findByApplicationId(id);
        Assert.assertEquals(dto.size(), 0);
    }

    @Test
    public void convertToDtoTest() {
        String id = endGroups.get(0);
        EndpointGroup found = endpointGroupDao.findById(id);
        Assert.assertNotNull(found);
        EndpointGroupDto dto = found.toDto();
        EndpointGroup converted = new EndpointGroup(dto);
        Assert.assertEquals(found, converted);
    }

    @Test
    public void removeTopicsFromEndpointGroupTest() {
        String id = endGroups.get(0);
        EndpointGroup found = endpointGroupDao.findById(id);
        Assert.assertNotNull(found);
        String topicId = new ObjectId().toString();
        found.setTopics(Arrays.asList(topicId));
        endpointGroupDao.addTopicToEndpointGroup(id, topicId);
        endpointGroupDao.removeTopicFromEndpointGroup(id, found.getTopics().get(0));
        EndpointGroup updated = endpointGroupDao.findById(id);
        Assert.assertNotEquals(found.getTopics(), updated.getTopics());
    }

}
