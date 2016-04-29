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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateTopicDaoTest extends HibernateAbstractTest {

    @Test
    public void testFindTopicsByAppId() {
        Topic topic = generateTopic(null, null, null);
        Application app = topic.getApplication();
        List<Topic> found = topicDao.findTopicsByAppId(app.getId().toString());
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void testFindTopicsByAppIdAndType() {
        Topic topic = generateTopic(null, TopicTypeDto.OPTIONAL, null);
        Application app = topic.getApplication();
        List<Topic> found = topicDao.findTopicsByAppIdAndType(app.getId().toString(), TopicTypeDto.OPTIONAL);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(topic, found.get(0));
    }

    @Test
    public void testFindTopicByAppIdAndName() {
        Topic topic = generateTopic(null, TopicTypeDto.OPTIONAL, null);
        Application app = topic.getApplication();
        Topic found = topicDao.findTopicByAppIdAndName(app.getId().toString(), topic.getName());
        Assert.assertEquals(topic, found);
    }

    @Test
    public void testFindTopicsByIds() {
        Topic first = generateTopic(null, null, "first");
        Application app = first.getApplication();
        Topic second = generateTopic(app, null, "second");
        List<Topic> expected = new ArrayList<>();
        expected.add(first);
        expected.add(second);
        List<Topic> found = topicDao.findTopicsByIds(Arrays.asList(first.getId().toString(), second.getId().toString()));
        Assert.assertEquals(expected.size(), found.size());
    }

    @Test
    public void testRemoveTopicsByAppId() {
        Topic topic = generateTopic(null, null, null);
        Application app = topic.getApplication();
        topicDao.removeTopicsByAppId(app.getId().toString());
        Topic found = topicDao.findById(topic.getId().toString());
        Assert.assertNull(found);
    }

    @Test
    public void testUpdateSeqNumber() {
        Topic topic = generateTopic(null, null, null);
        int seqNum = topic.getSequenceNumber();
        Topic updated = topicDao.getNextSeqNumber(topic.getId().toString());
        Assert.assertNotNull(updated);
        Assert.assertNotEquals(seqNum, updated.getSequenceNumber());
        Assert.assertEquals(seqNum + 1, updated.getSequenceNumber());
    }


    @Test
    public void testSaveTopic() {
        Topic topic = generateTopic(null, null, null);
        EndpointGroup endpointGroup = generateEndpointGroup(topic.getApplication(), null);

        topic.setName("Updated...");
        Topic updated = topicDao.save(topic);
        Assert.assertNotNull(updated);
//        Assert.assertNotEquals(seqNum, updated.getSequenceNumber());
//        Assert.assertEquals(seqNum + 1, updated.getSequenceNumber());
    }

    @Test
    public void testFindVacantTopicsByGroupId() {
        Topic first = generateTopic(null, null, "first");
        Application app = first.getApplication();
        Topic second = generateTopic(app, null, "second");
        Set<Topic> firstTopics = new HashSet<>();
        firstTopics.add(first);
        firstTopics.add(second);

        Topic third = generateTopic(app, null, "third");
        Topic fourth = generateTopic(app, null, "fourth");
        Set<Topic> secondTopics = new HashSet<>();
        secondTopics.add(third);
        secondTopics.add(fourth);

        EndpointGroup firstGroup = generateEndpointGroup(app, firstTopics);
        EndpointGroup secondGroup = generateEndpointGroup(app, secondTopics);

        List<Topic> foundOne = topicDao.findVacantTopicsByGroupId(firstGroup.getApplicationId(), firstGroup.getId().toString());
        Set<Topic> firstGroupSet = new HashSet<>();
        firstGroupSet.addAll(foundOne);
        List<Topic> foundTwo = topicDao.findVacantTopicsByGroupId(secondGroup.getApplicationId(), secondGroup.getId().toString());
        Set<Topic> secondGroupSet = new HashSet<>();
        secondGroupSet.addAll(foundTwo);

        Assert.assertEquals(secondTopics, firstGroupSet);
        Assert.assertEquals(firstTopics, secondGroupSet);
    }

//    @Test
//    public void testFindVacantTopicsByAppId() {
//        Topic first = generateTopic(null, null);
//        Application app = first.getApplication();
//        Topic second = generateTopic(app, null);
//        Set<Topic> firstTopics = new HashSet<>();
//        firstTopics.add(first);
//        firstTopics.add(second);
//
//        Topic third = generateTopic(app, null);
//        Topic fourth = generateTopic(app, null);
//        Set<Topic> secondTopics = new HashSet<>();
//        secondTopics.add(third);
//        secondTopics.add(fourth);
//
//        generateEndpointGroup(app, firstTopics);
//        generateEndpointGroup(app, secondTopics);
//
//        List<Topic> foundOne = topicDao.findVacantTopicsByAppId(app.getId().toString(), getIds(firstTopics));
//
//        Set<Topic> firstGroupSet = new HashSet<>();
//        firstGroupSet.addAll(foundOne);
//
//        List<Topic> foundTwo = topicDao.findVacantTopicsByAppId(app.getId().toString(), getIds(secondTopics));
//
//        Set<Topic> secondGroupSet = new HashSet<>();
//        secondGroupSet.addAll(foundTwo);
//
//        Assert.assertEquals(secondTopics, firstGroupSet);
//        Assert.assertEquals(firstTopics, secondGroupSet);
//    }

    @SuppressWarnings("rawtypes")
    private List<String> getIds(Set<Topic> topics) {
        List<String> ids = Collections.emptyList();
        if (topics != null && !topics.isEmpty()) {
            ids = new ArrayList<>();
            for (GenericModel model : topics) {
                ids.add(model.getId().toString());
            }
        }
        return ids;
    }
}
