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

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.mongo.model.MongoNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class NotificationMongoDaoTest extends AbstractTest {

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

    @Autowired
    private NotificationDao<MongoNotification> notificationDao;

    @Test
    public void testRemoveById() {
        NotificationDto notification = generateNotifications(null, null, 1, null).get(0);
        Assert.assertNotNull(notification.getId());
        notificationDao.removeById(notification.getId());
        MongoNotification found = notificationDao.findById(notification.getId());
        Assert.assertNull(found);
    }

    @Test
    public void testFindNotificationsBySchemaId() {
        NotificationDto notification = generateNotifications(null, null, 1, null).get(0);
        Assert.assertNotNull(notification.getId());
        List<MongoNotification> found = notificationDao.findNotificationsBySchemaId(notification.getSchemaId());
        Assert.assertEquals(notification, found.get(0).toDto());
    }

    @Test
    public void removeNotificationsBySchemaId() {
        NotificationDto notification = generateNotifications(null, null, 1, null).get(0);
        Assert.assertNotNull(notification.getId());
        String schemaId = notification.getSchemaId();
        List<MongoNotification> found = notificationDao.findNotificationsBySchemaId(schemaId);
        Assert.assertEquals(notification, found.get(0).toDto());
        notificationDao.removeNotificationsBySchemaId(schemaId);;
        found = notificationDao.findNotificationsBySchemaId(schemaId);
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void findNotificationsByAppId() {
        NotificationDto notification = generateNotifications(null, null, 1, null).get(0);
        Assert.assertNotNull(notification.getId());
        List<MongoNotification> found = notificationDao.findNotificationsByAppId(notification.getApplicationId());
        Assert.assertEquals(notification, found.get(0).toDto());
    }

    @Test
    public void findNotificationsBySchemaIdAndType() {
        NotificationDto notification = generateNotifications(null, null, 1, NotificationTypeDto.USER).get(0);
        Assert.assertNotNull(notification.getId());
        List<MongoNotification> found = notificationDao.findNotificationsBySchemaIdAndType(notification.getSchemaId(), NotificationTypeDto.USER);
        Assert.assertEquals(notification, found.get(0).toDto());
    }
}
