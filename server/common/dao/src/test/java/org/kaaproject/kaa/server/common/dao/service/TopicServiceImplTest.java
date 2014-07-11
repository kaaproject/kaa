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

import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TopicServiceImplTest extends AbstractTest {

    @Autowired
    private DataSource dataSource;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void findTopicByIdTest() {
        TopicDto topic = generateTopic(null, null);
        TopicDto found = topicService.findTopicById(topic.getId());
        Assert.assertEquals(topic, found);
    }

    @Test
    public void findTopicsByAppIdTest() {
        TopicDto topic = generateTopic(null, null);
        List<TopicDto> found = topicService.findTopicsByAppId(topic.getApplicationId());
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void findTopicsByAppIdAndTypeTest() {
        TopicDto topic = generateTopic(null, TopicTypeDto.VOLUNTARY);
        List<TopicDto> found = topicService.findTopicsByAppIdAndType(topic.getApplicationId(), TopicTypeDto.VOLUNTARY);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void removeTopicByIdTest() {
        TopicDto topic = generateTopic(null, null);
        topicService.removeTopicById(topic.getId());
        TopicDto found = topicService.findTopicById(topic.getId());
        Assert.assertNull(found);
    }

    @Test
    public void removeTopicByIdAddedToGroupTest() {
        TopicDto topic = generateTopic(null, null);
        EndpointGroupDto groupDto = generateEndpointGroup(topic.getApplicationId());
        endpointService.addTopicToEndpointGroup(groupDto.getId(), topic.getId());
        topicService.removeTopicById(topic.getId());
        TopicDto found = topicService.findTopicById(topic.getId());
        Assert.assertNull(found);
    }

    @Test
    public void removeTopicsByAppIdTest() {
        TopicDto topic = generateTopic(null, null);
        topicService.removeTopicsByAppId(topic.getApplicationId());
        List<TopicDto> found = topicService.findTopicsByAppId(topic.getApplicationId());
        Assert.assertNotNull(found);
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testFindVacantTopicsByEndpointGroupId() {
        ApplicationDto app = generateApplication();
        EndpointGroupDto group = generateEndpointGroup(app.getId());
        TopicDto topic = generateTopic(app.getId(), null);
        List<TopicDto> found = topicService.findVacantTopicsByEndpointGroupId(group.getId());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void testFindTopicsByEndpointGroupId() {
        ApplicationDto app = generateApplication();
        EndpointGroupDto group = generateEndpointGroup(app.getId());
        TopicDto topic = generateTopic(app.getId(), null);
        endpointService.addTopicToEndpointGroup(group.getId(), topic.getId());
        List<TopicDto> found = topicService.findTopicsByEndpointGroupId(group.getId());
        Assert.assertEquals(topic, found.get(0));
    }
}
