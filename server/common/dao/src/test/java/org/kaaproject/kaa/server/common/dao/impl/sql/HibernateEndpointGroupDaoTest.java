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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateEndpointGroupDaoTest extends HibernateAbstractTest {

    @Test
    public void findByApplicationId() {
        EndpointGroup endpointGroup = generateEndpointGroup(null, null);
        List<EndpointGroup> groups = endpointGroupDao.findByApplicationId(endpointGroup.getApplicationId());
        Assert.assertEquals(1, groups.size());
    }

    @Test
    public void findByAppIdAndWeight() {
        EndpointGroup endpointGroup = generateEndpointGroup(null, null);
        EndpointGroup group = endpointGroupDao.findByAppIdAndWeight(endpointGroup.getApplicationId(), endpointGroup.getWeight());
        Assert.assertEquals(endpointGroup, group);
    }
    
    @Test
    public void findByAppIdAndName() {
        EndpointGroup endpointGroup = generateEndpointGroup(null, null);
        EndpointGroup group = endpointGroupDao.findByAppIdAndName(endpointGroup.getApplicationId(), endpointGroup.getName());
        Assert.assertEquals(endpointGroup, group);
    }

    @Test
    public void removeTopicFromEndpointGroup() {
        Topic first = generateTopic(null, null, "first");
        Topic second = generateTopic(first.getApplication(), null, "second");
        Topic third = generateTopic(first.getApplication(), null, "third");
        Set<Topic> topics = new HashSet<>();
        topics.add(first);
        topics.add(second);
        topics.add(third);
        EndpointGroup endpointGroup = generateEndpointGroup(first.getApplication(), topics);
        Assert.assertEquals(3, endpointGroup.getTopics().size());
        endpointGroupDao.removeTopicFromEndpointGroup(endpointGroup.getId().toString(), first.getId().toString());
        EndpointGroup group = endpointGroupDao.removeTopicFromEndpointGroup(endpointGroup.getId().toString(), second.getId().toString());
        Set<Topic> expected = new HashSet<>();
        expected.add(third);
        Assert.assertEquals(expected, group.getTopics());
    }

    @Test
    public void findEndpointGroupsByTopicIdAndAppId() throws InterruptedException {
        Topic topic = generateTopic(null, null, null);
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        EndpointGroup endpointGroup = generateEndpointGroup(topic.getApplication(), topics);
        List<EndpointGroup> found = endpointGroupDao.findEndpointGroupsByTopicIdAndAppId(topic.getApplication().getId().toString(), topic.getId().toString());
        Assert.assertNotNull(found);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(endpointGroup, found.get(0));
    }

    @Test
    public void addTopicToEndpointGroup() {
        Topic first = generateTopic(null, null, "first");
        Topic second = generateTopic(first.getApplication(), null, "second");
        Topic third = generateTopic(first.getApplication(), null, "third");
        Set<Topic> topics = new HashSet<>();
        topics.add(first);
        topics.add(second);
        topics.add(third);
        EndpointGroup endpointGroup = generateEndpointGroup(first.getApplication(), topics);
        endpointGroupDao.addTopicToEndpointGroup(endpointGroup.getId().toString(), first.getId().toString());
        endpointGroupDao.addTopicToEndpointGroup(endpointGroup.getId().toString(), second.getId().toString());
        EndpointGroup saved = endpointGroupDao.addTopicToEndpointGroup(endpointGroup.getId().toString(), third.getId().toString());
        Assert.assertNotNull(saved);
        Assert.assertEquals(topics, saved.getTopics());
    }

    @Test
    public void removeEndpointGroupTest() {
        Topic first = generateTopic(null, null, "first");
        Topic second = generateTopic(first.getApplication(), null, "second");
        Set<Topic> topicsOne = new HashSet<>();
        topicsOne.add(first);
        Set<Topic> topicsTwo = new HashSet<>();
        topicsTwo.add(second);
        EndpointGroup endpointGroupOne = generateEndpointGroup(first.getApplication(), topicsOne);
        generateEndpointGroup(first.getApplication(), topicsTwo);
        endpointGroupDao.removeById(endpointGroupOne.getStringId());
        Assert.assertNull(endpointGroupDao.findById(endpointGroupOne.getStringId()));
        Topic f = topicDao.findById(first.getStringId());
        Assert.assertNotNull(f);
        Assert.assertTrue(f.getEndpointGroups().isEmpty());
    }

}
