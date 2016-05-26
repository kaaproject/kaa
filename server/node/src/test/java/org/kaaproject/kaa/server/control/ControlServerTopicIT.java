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

package org.kaaproject.kaa.server.control;


import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The Class ControlServerTopicIT.
 */
public class ControlServerTopicIT extends AbstractTestControlServer {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlServerTopicIT.class);


    /**
     * Test create topic.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateTopic() throws Exception {
        TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        Assert.assertNotNull(topic.getId());
    }

    /**
     * Test get topic.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTopic() throws Exception {
        TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        LOG.debug("Created topic with id {}", topic.getId());
        TopicDto storedTopic = client.getTopic(topic.getId());
        Assert.assertNotNull(storedTopic);
        Assert.assertEquals(topic, storedTopic);
    }

    /**
     * Test get topic by app token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTopicByAppToken() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        TopicDto topic = createTopic(application.getId(), TopicTypeDto.MANDATORY);
        LOG.debug("Created topic with id {}", topic.getId());
        List<TopicDto> storedTopic = client.getTopicsByApplicationToken(application.getApplicationToken());
        Assert.assertNotNull(storedTopic);
        Assert.assertFalse(storedTopic.isEmpty());
        Assert.assertEquals(topic, storedTopic.get(0));
    }

    /**
     * Test get topic by endpoint group id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetTopicByEndpointGroupId() throws Exception {
        EndpointGroupDto group = createEndpointGroup();
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        LOG.debug("Created topic with id {}", topic.getId());
        client.addTopicToEndpointGroup(group.getId(), topic.getId());
        List<TopicDto> storedTopic = client.getTopicsByEndpointGroupId(group.getId());
        Assert.assertNotNull(storedTopic);
        Assert.assertFalse(storedTopic.isEmpty());
        Assert.assertEquals(topic, storedTopic.get(0));
    }

    /**
     * Test get vacant topic by endpoint group id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetVacantTopicByEndpointGroupId() throws Exception {
        EndpointGroupDto group = createEndpointGroup();
        TopicDto addedTopic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        client.addTopicToEndpointGroup(group.getId(), addedTopic.getId());
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        LOG.debug("Created topic with id {}", topic.getId());
        List<TopicDto> storedTopic = client.getVacantTopicsByEndpointGroupId(group.getId());
        Assert.assertNotNull(storedTopic);
        Assert.assertFalse(storedTopic.isEmpty());
        Assert.assertEquals(topic, storedTopic.get(0));
    }

    /**
     * Test delete topic by id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteTopicById() throws Exception {
        final TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        LOG.debug("Created topic with id {}", topic.getId());
        TopicDto foundTopic = client.getTopic(topic.getId());
        Assert.assertNotNull(foundTopic);
        Assert.assertEquals(topic, foundTopic);
        client.deleteTopic(topic.getId());
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getTopic(topic.getId());
                
            }
        });
    }

    /**
     * Test delete topic from endpoint group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteTopicFromEndpointGroup() throws Exception {
        EndpointGroupDto group = createEndpointGroup();
        String groupId = group.getId();
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        String topicId = topic.getId();
        LOG.debug("Created topic with id {}", topicId);
        client.addTopicToEndpointGroup(group.getId(), topicId);
        EndpointGroupDto found = client.getEndpointGroup(groupId);
        Assert.assertNotNull(found);
        Assert.assertEquals(topicId, found.getTopics().get(0));
        client.removeTopicFromEndpointGroup(groupId, topicId);
        found = client.getEndpointGroup(groupId);
        Assert.assertNotNull(found);
        List<String> topics = found.getTopics();
        Assert.assertNotNull(topics);
        Assert.assertTrue(topics.isEmpty());
    }

    /**
     * Test add topic to endpoint group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAddTopicToEndpointGroup() throws Exception {
        EndpointGroupDto group = createEndpointGroup();
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        LOG.debug("Created topic with id {}", topic.getId());
        String groupId = group.getId();
        client.addTopicToEndpointGroup(group.getId(), topic.getId());
        EndpointGroupDto found = client.getEndpointGroup(groupId);
        Assert.assertNotNull(found);
        Assert.assertEquals(topic.getId(), found.getTopics().get(0));
    }
}
