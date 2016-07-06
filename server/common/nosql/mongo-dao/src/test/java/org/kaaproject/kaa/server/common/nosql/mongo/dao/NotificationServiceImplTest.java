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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.*;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationServiceImplTest extends AbstractMongoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImplTest.class);

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
        MongoDataLoader.clearDBData();
        clearDBData();
    }

    @Ignore
    @Test
    public void findNotificationsByIdWithExpiredTimeTest() {
        ApplicationDto app = generateApplicationDto();

        NotificationSchemaDto notificationSchemaDto = new NotificationSchemaDto();
        notificationSchemaDto.setApplicationId(app.getId());
        CTLSchemaDto ctlSchema = ctlService.saveCTLSchema(generateCTLSchemaDto(app.getTenantId()));
        notificationSchemaDto.setCtlSchemaId(ctlSchema.getId());
        if (notificationSchemaDto == null) {
            throw new RuntimeException("Can't save default profile schema "); //NOSONAR
        }

        notificationSchemaDto.setCtlSchemaId(ctlSchema.getId());
        notificationSchemaDto.setType(NotificationTypeDto.USER);
        NotificationSchemaDto savedSchema = notificationService.saveNotificationSchema(notificationSchemaDto);

        TopicDto topicDto = new TopicDto();
        topicDto.setApplicationId(app.getId());
        topicDto.setName("New Topic");
        topicDto.setType(TopicTypeDto.MANDATORY);

        topicDto = topicService.saveTopic(topicDto);

        NotificationDto dto = new NotificationDto();
        dto.setSchemaId(savedSchema.getId());
        dto.setTopicId(topicDto.getId());
        dto.setBody("{\"notificationBody\":\"dummy\", \"systemNotificationParam1\":42, \"systemNotificationParam2\":43}".getBytes());
        dto.setExpiredAt(new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10L)));

        UpdateNotificationDto<NotificationDto> up = notificationService.saveNotification(dto);
        String nid = up.getPayload().getId();
        Assert.assertNotNull(nid);
        try {
            TimeUnit.SECONDS.sleep(70L);
        } catch (InterruptedException e) {
            LOGGER.error("Catch interrupted exception during sleep.", e);
        }

        NotificationDto found = notificationService.findNotificationById(nid);

        LOGGER.debug("Try to get expired notification {}", found);
        Assert.assertNull(found);
    }

    @Test
    public void testSaveNotificationSchema() {
        NotificationSchemaDto schema = generateNotificationSchemaDto(null, null);
        Assert.assertNotNull(schema);
        Assert.assertTrue(isNotBlank(schema.getId()));
        Assert.assertEquals(2, schema.getVersion());
        Assert.assertEquals(NotificationTypeDto.USER, schema.getType());
    }

    @Test
    public void testSaveNotification() {
        NotificationDto notification = generateNotificationsDto(null, null, 1, null).get(0);
        Assert.assertNotNull(notification);
        Assert.assertTrue(isNotBlank(notification.getId()));
        Assert.assertEquals(NotificationTypeDto.USER, notification.getType());
    }

    @Test
    public void testFindNotificationById() {
        NotificationDto notification = generateNotificationsDto(null, null, 1, null).get(0);
        Assert.assertNotNull(notification);
        Assert.assertTrue(isNotBlank(notification.getId()));
        NotificationDto found = notificationService.findNotificationById(notification.getId());
        Assert.assertEquals(notification, found);
    }

    @Test
    public void testFindNotificationsByTopicId() {
        NotificationDto notification = generateNotificationsDto(null, null, 1, null).get(0);
        Assert.assertNotNull(notification);
        NotificationDto found = notificationService.findNotificationsByTopicId(notification.getTopicId()).get(0);
        Assert.assertEquals(notification, found);
    }

    @Test
    public void testFindNotificationSchemaById() {
        NotificationSchemaDto schema = generateNotificationSchemaDto(null, null);
        Assert.assertNotNull(schema);
        NotificationSchemaDto found = notificationService.findNotificationSchemaById(schema.getId());
        Assert.assertEquals(schema, found);
    }

    @Test
    public void testFindNotificationSchemasByAppId() {
        NotificationSchemaDto schema = generateNotificationSchemaDto(null, null);
        Assert.assertNotNull(schema);
        NotificationSchemaDto found = notificationService.findNotificationSchemasByAppId(schema.getApplicationId()).get(1);
        Assert.assertEquals(schema, found);
    }

    @Test
    public void testFindUserNotificationSchemasByAppId() {
        NotificationDto dto = generateNotificationsDto(null, null, 1, null).get(0);
        List<VersionDto> schemas = notificationService.findUserNotificationSchemasByAppId(dto.getApplicationId());
        generateNotificationSchemaDto(dto.getApplicationId(), NotificationTypeDto.SYSTEM);
        Assert.assertEquals(2, schemas.size());
    }

    @Test
    public void testFindNotificationSchemaVersionsByAppId() {
        NotificationDto dto = generateNotificationsDto(null, null, 1, null).get(0);
        generateNotificationSchemaDto(dto.getApplicationId(), NotificationTypeDto.SYSTEM);
        List<VersionDto> schemas = notificationService.findNotificationSchemaVersionsByAppId(dto.getApplicationId());
        Assert.assertEquals(3, schemas.size());
    }

    @Test
    public void testRemoveNotificationSchemasByAppId() {
        NotificationDto dto = generateNotificationsDto(null, null, 3, null).get(0);
        String appId = dto.getApplicationId();
        notificationService.removeNotificationSchemasByAppId(appId);
        List<NotificationSchemaDto> schemas = notificationService.findNotificationSchemasByAppId(appId);
        Assert.assertTrue(schemas.isEmpty());
    }

    @Test
    public void testFindNotificationsByTopicIdAndVersionAndStartSecNum() {
        NotificationDto dto = generateNotificationsDto(null, null, 3, NotificationTypeDto.USER).get(0);
        String topicId = dto.getTopicId();
        List<NotificationDto> notifications = notificationService.findNotificationsByTopicIdAndVersionAndStartSecNum(topicId, 0, 1, dto.getNfVersion());
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertEquals(3, notifications.size());
    }

    @Test
    public void testFindNotificationSchemasByAppIdAndType() {
        NotificationDto dto = generateNotificationsDto(null, null, 1, null).get(0);
        generateNotificationSchemaDto(dto.getApplicationId(), NotificationTypeDto.SYSTEM);
        List<NotificationSchemaDto> schemas = notificationService.findNotificationSchemasByAppIdAndType(dto.getApplicationId(), NotificationTypeDto.SYSTEM);
        Assert.assertEquals(1, schemas.size());
        schemas = notificationService.findNotificationSchemasByAppIdAndType(dto.getApplicationId(), NotificationTypeDto.USER);
        Assert.assertEquals(2, schemas.size());
    }

    @Test
    public void testFindNotificationSchemaByAppIdAndTypeAndVersion() {
        NotificationDto dto = generateNotificationsDto(null, null, 1, null).get(0);
        generateNotificationSchemaDto(dto.getApplicationId(), NotificationTypeDto.SYSTEM);
        NotificationSchemaDto schema = notificationService.findNotificationSchemaByAppIdAndTypeAndVersion(dto.getApplicationId(), NotificationTypeDto.SYSTEM, 1);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testFindUnicastNotificationById() {
        TopicDto topicDto = generateTopicDto(null, null);
        EndpointProfileDto profile = generateEndpointProfileDto(topicDto.getApplicationId(), Arrays.asList(topicDto.getId()));
        byte[] keyHash = profile.getEndpointKeyHash();
        EndpointNotificationDto notification = generateUnicastNotificationDto(null, topicDto.getId(), keyHash);
        Assert.assertTrue(isNotBlank(notification.getId()));
        EndpointNotificationDto found = notificationService.findUnicastNotificationById(notification.getId());
        Assert.assertEquals(notification, found);
    }

    @Test
    public void testRemoveUnicastNotificationsByKeyHash() {
        TopicDto topicDto = generateTopicDto(null, null);
        EndpointProfileDto profile = generateEndpointProfileDto(topicDto.getApplicationId(), Arrays.asList(topicDto.getId()));
        byte[] keyHash = profile.getEndpointKeyHash();
        EndpointNotificationDto notification = generateUnicastNotificationDto(null, topicDto.getId(), keyHash);
        Assert.assertTrue(isNotBlank(notification.getId()));
        notificationService.removeUnicastNotificationsByKeyHash(keyHash);
        List<EndpointNotificationDto> notifications = notificationService.findUnicastNotificationsByKeyHash(keyHash);
        Assert.assertTrue(notifications.isEmpty());
    }

    @Test(expected = IncorrectParameterException.class)
    public void testSaveInvalidNotificationSchema() {
        notificationService.saveNotificationSchema(new NotificationSchemaDto());
    }

    @Test(expected = IncorrectParameterException.class)
    public void testSaveNotificationSchemaWithEmptyType() {
        NotificationSchemaDto schema = new NotificationSchemaDto();
        schema.setApplicationId(new ObjectId().toString());
        notificationService.saveNotificationSchema(schema);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testSaveNotificationSchemaWithInvalidAppId() {
        NotificationSchemaDto schema = new NotificationSchemaDto();
        schema.setApplicationId("Invalid Application id.");
        notificationService.saveNotificationSchema(schema);
    }

    @Test
    public void testRemoveUnicastNotificationById() {
        TopicDto topicDto = generateTopicDto(null, null);
        EndpointProfileDto profile = generateEndpointProfileDto(topicDto.getApplicationId(), Arrays.asList(topicDto.getId()));
        byte[] keyHash = profile.getEndpointKeyHash();
        EndpointNotificationDto notification = generateUnicastNotificationDto(null, topicDto.getId(), keyHash);
        Assert.assertTrue(isNotBlank(notification.getId()));
        notificationService.removeUnicastNotificationById(notification.getId());
        EndpointNotificationDto notif = notificationService.findUnicastNotificationById(notification.getId());
        Assert.assertNull(notif);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testSaveInvalidNotification() {
        NotificationDto notification = new NotificationDto();
        notificationService.saveNotification(notification);
    }

    @Test(expected = NumberFormatException.class)
    public void testSaveNotificationWithIncorrectIds() {
        NotificationDto notification = new NotificationDto();
        notification.setSchemaId(new ObjectId().toString());
        notification.setTopicId(new ObjectId().toString());
        notificationService.saveNotification(notification);
    }
}
