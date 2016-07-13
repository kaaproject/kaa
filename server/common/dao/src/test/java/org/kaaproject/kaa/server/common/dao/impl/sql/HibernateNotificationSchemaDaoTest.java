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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.impl.NotificationSchemaDao;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.NotificationSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateNotificationSchemaDaoTest extends HibernateAbstractTest {

    @Autowired
    private NotificationSchemaDao<NotificationSchema> notificationSchemaDao;

    @Test
    public void testFindNotificationSchemasByAppId() throws Exception {
        Application application = generateApplication(null);
        List<NotificationSchema> schemas = generateNotificationSchema(application, 1, 1, null);
        List<NotificationSchema> found = notificationSchemaDao.findNotificationSchemasByAppId(application.getStringId());
        Assert.assertEquals(schemas, found);
    }

    @Test
    public void testRemoveNotificationSchemasByAppId() throws Exception {
        Application application = generateApplication(null);
        generateNotificationSchema(application, 1, 1, null);
        notificationSchemaDao.removeNotificationSchemasByAppId(application.getStringId());
        List<NotificationSchema> found = notificationSchemaDao.findNotificationSchemasByAppId(application.getStringId());
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testFindNotificationSchemasByAppIdAndType() throws Exception {
        Application application = generateApplication(null);
        List<NotificationSchema> userSchemas = generateNotificationSchema(application, 1, 2, NotificationTypeDto.USER);
        generateNotificationSchema(application, 2, 3, NotificationTypeDto.SYSTEM);
        List<NotificationSchema> found = notificationSchemaDao.findNotificationSchemasByAppIdAndType(application.getStringId(), NotificationTypeDto.USER);
        Assert.assertEquals(userSchemas, found);
    }

    @Test
    public void testFindNotificationSchemasByAppIdAndTypeAndVersion() throws Exception {
        Application application = generateApplication(null);
        generateNotificationSchema(application, 1, 1, NotificationTypeDto.SYSTEM);
        List<NotificationSchema> userSchemas = generateNotificationSchema(application, 2, 3, NotificationTypeDto.USER);
        NotificationSchema expected = userSchemas.get(2);
        NotificationSchema found = notificationSchemaDao.findNotificationSchemasByAppIdAndTypeAndVersion(application.getStringId(), NotificationTypeDto.USER, expected.getVersion());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testFindLatestNotificationSchemaByAppId() throws Exception {
        Application application = generateApplication(null);
        List<NotificationSchema> userSchemas = generateNotificationSchema(application, 1, 3, NotificationTypeDto.USER);
        List<NotificationSchema> systemSchemas = generateNotificationSchema(application, 2, 3, NotificationTypeDto.SYSTEM);
        NotificationSchema found = notificationSchemaDao.findLatestNotificationSchemaByAppId(application.getStringId(), NotificationTypeDto.USER);
        Assert.assertEquals(userSchemas.get(2), found);
    }
}