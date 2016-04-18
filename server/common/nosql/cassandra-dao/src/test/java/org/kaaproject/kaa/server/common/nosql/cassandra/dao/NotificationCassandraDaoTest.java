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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraNotification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testSave() throws Exception {
        NotificationDto expected = generateNotifications(null, null, null, 1, NotificationTypeDto.USER).get(0);
        NotificationDto found = notificationDao.findById(expected.getId()).toDto();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testRemoveById() throws Exception {
        NotificationDto expected = generateNotifications(null, null, null, 1, NotificationTypeDto.USER).get(0);
        notificationDao.removeById(expected.getId());
        Assert.assertNull(notificationDao.findById(expected.getId()));
    }

    @Test
    public void testFindNotificationsByTopicId() throws Exception {
        String topicId = UUID.randomUUID().toString();
        List<NotificationDto> expectedList = new ArrayList<>();
        expectedList.addAll(generateNotifications(topicId, null, null, 2, NotificationTypeDto.USER));
        expectedList.addAll(generateNotifications(topicId, null, null, 1, NotificationTypeDto.SYSTEM));
        List<CassandraNotification> found = notificationDao.findNotificationsByTopicId(topicId);
        Assert.assertEquals(expectedList.size(), found.size());
    }

    @Test
    public void testRemoveNotificationsByTopicId() throws Exception {
        String topicId = UUID.randomUUID().toString();
        List<NotificationDto> expectedList = new ArrayList<>();
        expectedList.addAll(generateNotifications(topicId, null, null, 2, NotificationTypeDto.USER));
        expectedList.addAll(generateNotifications(topicId, null, null, 1, NotificationTypeDto.SYSTEM));
        List<CassandraNotification> found = notificationDao.findNotificationsByTopicId(topicId);
        Assert.assertEquals(expectedList.size(), found.size());
    }

    @Test
    public void testFindNotificationsByTopicIdAndVersionAndStartSecNum() throws Exception {
        String topicId = UUID.randomUUID().toString();
        List<NotificationDto> expectedList = new ArrayList<>();
        expectedList.addAll(generateNotifications(topicId, null, null, 7, NotificationTypeDto.USER));
        List<CassandraNotification> foundList = notificationDao.findNotificationsByTopicIdAndVersionAndStartSecNum(topicId, 3, 0, 1);
        Assert.assertEquals(3, foundList.size());
        for (int i = 0; i < foundList.size(); i++) {
            CassandraNotification notification = foundList.get(i);
            Assert.assertEquals(NotificationTypeDto.USER, notification.getType());
            Assert.assertEquals(topicId, notification.getTopicId());
            Assert.assertTrue(3 < notification.getSeqNum());
        }
    }

    @Test
    public void testToDto() {
        NotificationDto expected = generateNotifications(null, null, null, 1, NotificationTypeDto.USER).get(0);
        Assert.assertEquals(expected, new CassandraNotification(expected).toDto());
    }
}