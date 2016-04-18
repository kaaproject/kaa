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

import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class TopicServiceImplTest extends AbstractTest {

    @Autowired
    private DataSource dataSource;

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveTopicTest(){
        ApplicationDto app = generateApplicationDto();
        TopicDto topic1 = generateTopicDto(app.getId(), TopicTypeDto.MANDATORY);
        topicService.saveTopic(topic1);
        TopicDto topic2 = generateTopicDto(app.getId(), TopicTypeDto.OPTIONAL);
        topicService.saveTopic(topic2);
    }

    @Test
    public void findTopicByIdTest() {
        TopicDto topic = generateTopicDto(null, null);
        TopicDto found = topicService.findTopicById(topic.getId());
        Assert.assertEquals(topic, found);
    }

    @Test
    public void findTopicsByAppIdTest() {
        TopicDto topic = generateTopicDto(null, null);
        List<TopicDto> found = topicService.findTopicsByAppId(topic.getApplicationId());
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void findTopicsByAppIdAndTypeTest() {
        TopicDto topic = generateTopicDto(null, TopicTypeDto.OPTIONAL);
        List<TopicDto> found = topicService.findTopicsByAppIdAndType(topic.getApplicationId(), TopicTypeDto.OPTIONAL);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void removeTopicByIdTest() {
        TopicDto topic = generateTopicDto(null, null);
        topicService.removeTopicById(topic.getId());
        TopicDto found = topicService.findTopicById(topic.getId());
        Assert.assertNull(found);
        List<EndpointGroupDto> groups = endpointService.findEndpointGroupsByAppId(topic.getApplicationId());
        Assert.assertFalse(groups.isEmpty());
    }

    @Test
    public void removeTopicByIdAddedToGroupTest() {
        TopicDto topic = generateTopicDto(null, null);
        EndpointGroupDto groupDto = generateEndpointGroupDto(topic.getApplicationId());
        endpointService.addTopicToEndpointGroup(groupDto.getId(), topic.getId());
        topicService.removeTopicById(topic.getId());
        TopicDto found = topicService.findTopicById(topic.getId());
        Assert.assertNull(found);
    }

    @Test
    public void removeTopicsByAppIdTest() {
        TopicDto topic = generateTopicDto(null, null);
        topicService.removeTopicsByAppId(topic.getApplicationId());
        List<TopicDto> found = topicService.findTopicsByAppId(topic.getApplicationId());
        Assert.assertNotNull(found);
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testFindVacantTopicsByEndpointGroupId() {
        ApplicationDto app = generateApplicationDto();
        EndpointGroupDto group = generateEndpointGroupDto(app.getId());
        TopicDto topic = generateTopicDto(app.getId(), null);
        List<TopicDto> found = topicService.findVacantTopicsByEndpointGroupId(group.getId());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void testFindTopicsByEndpointGroupId() {
        ApplicationDto app = generateApplicationDto();
        EndpointGroupDto group = generateEndpointGroupDto(app.getId());
        TopicDto topic = generateTopicDto(app.getId(), null);
        endpointService.addTopicToEndpointGroup(group.getId(), topic.getId());
        List<TopicDto> found = topicService.findTopicsByEndpointGroupId(group.getId());
        Assert.assertEquals(topic, found.get(0));
    }
}
