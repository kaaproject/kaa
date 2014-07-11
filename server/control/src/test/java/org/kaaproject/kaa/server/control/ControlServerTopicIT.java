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

package org.kaaproject.kaa.server.control;


import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;

import java.io.IOException;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControlServerTopicIT.
 */
public class ControlServerTopicIT extends AbstractTestControlServer {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlServerTopicIT.class);


    /**
     * Test create topic.
     *
     * @throws TException the t exception
     */
    @Test
    public void testCreateTopic() throws TException {
        TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        Assert.assertNotNull(topic.getId());
    }

    /**
     * Test get topic by id.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetTopic() throws TException {
        TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        LOGGER.debug("Created topic with id {}", topic.getId());
        TopicDto storedTopic = toDto(client.getTopic(topic.getId()));
        Assert.assertNotNull(storedTopic);
        Assert.assertEquals(topic, storedTopic);
    }

    /**
     * Test get topic by application id.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetTopicByAppId() throws TException {
        TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        LOGGER.debug("Created topic with id {}", topic.getId());
        List<TopicDto> storedTopic = toDtoList(client.getTopicByAppId(topic.getApplicationId()));
        Assert.assertNotNull(storedTopic);
        Assert.assertFalse(storedTopic.isEmpty());
        Assert.assertEquals(topic, storedTopic.get(0));
    }

    /**
     * Test get topic by endpoint group id.
     *
     * @throws TException the t exception
     * @throws IOException the IO exception
     */
    @Test
    public void testGetTopicByEndpointGroupId() throws TException, IOException {
        EndpointGroupDto group = createEndpointGroup();
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        LOGGER.debug("Created topic with id {}", topic.getId());
        client.addTopicsToEndpointGroup(group.getId(), topic.getId());
        List<TopicDto> storedTopic = toDtoList(client.getTopicByEndpointGroupId(group.getId()));
        Assert.assertNotNull(storedTopic);
        Assert.assertFalse(storedTopic.isEmpty());
        Assert.assertEquals(topic, storedTopic.get(0));
    }

    /**
     * Test get vacant topic by endpoint group id.
     *
     * @throws TException the t exception
     * @throws IOException the IO exception
     */
    @Test
    public void testGetVacantTopicByEndpointGroupId() throws TException, IOException {
        EndpointGroupDto group = createEndpointGroup();
        TopicDto addedTopic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        group = toDto(client.addTopicsToEndpointGroup(group.getId(), addedTopic.getId()));
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        LOGGER.debug("Created topic with id {}", topic.getId());
        List<TopicDto> storedTopic = toDtoList(client.getVacantTopicByEndpointGroupId(group.getId()));
        Assert.assertNotNull(storedTopic);
        Assert.assertFalse(storedTopic.isEmpty());
        Assert.assertEquals(topic, storedTopic.get(0));
    }

    /**
     * Test delete topic by id.
     *
     * @throws TException the t exception
     */
    @Test
    public void testDeleteTopicById() throws TException {
        TopicDto topic = createTopic(null, TopicTypeDto.MANDATORY);
        LOGGER.debug("Created topic with id {}", topic.getId());
        TopicDto foundTopic = toDto(client.getTopic(topic.getId()));
        Assert.assertNotNull(foundTopic);
        Assert.assertEquals(topic, foundTopic);

        client.deleteTopicById(topic.getId());
        foundTopic = toDto(client.getTopic(topic.getId()));
        Assert.assertNull(foundTopic);
    }

    /**
     * Test delete topic by application id.
     *
     * @throws TException the t exception
     * @throws IOException the IO exception
     */
    @Test
    public void testDeleteTopicFromEndpointGroup() throws TException, IOException {
        EndpointGroupDto group = createEndpointGroup();
        String groupId = group.getId();
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        String topicId = topic.getId();
        LOGGER.debug("Created topic with id {}", topicId);
        client.addTopicsToEndpointGroup(group.getId(), topicId);
        EndpointGroupDto found = toDto(client.getEndpointGroup(groupId));
        Assert.assertNotNull(found);
        Assert.assertEquals(topicId, found.getTopics().get(0));
        client.removeTopicsFromEndpointGroup(groupId, topicId);
        found = toDto(client.getEndpointGroup(groupId));
        Assert.assertNotNull(found);
        List<String> topics = found.getTopics();
        Assert.assertNotNull(topics);
        Assert.assertTrue(topics.isEmpty());
    }

    /**
     * Test delete topic by application id.
     *
     * @throws TException the t exception
     * @throws IOException the IO exception
     */
    @Test
    public void testAddTopicToEndpointGroup() throws TException, IOException {
        EndpointGroupDto group = createEndpointGroup();
        TopicDto topic = createTopic(group.getApplicationId(), TopicTypeDto.MANDATORY);
        LOGGER.debug("Created topic with id {}", topic.getId());
        String groupId = group.getId();
        client.addTopicsToEndpointGroup(group.getId(), topic.getId());
        EndpointGroupDto found = toDto(client.getEndpointGroup(groupId));
        Assert.assertNotNull(found);
        Assert.assertEquals(topic.getId(), found.getTopics().get(0));
    }
}
