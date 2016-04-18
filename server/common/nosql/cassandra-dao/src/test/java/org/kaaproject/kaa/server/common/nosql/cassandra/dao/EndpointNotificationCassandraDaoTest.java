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

import com.datastax.driver.core.utils.Bytes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.ByteBuffer;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointNotificationCassandraDaoTest extends AbstractCassandraTest {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointNotificationCassandraDaoTest.class);

    @Test
    public void testFindNotificationsByKeyHash() throws Exception {
        List<CassandraEndpointNotification> notifications = generateEndpointNotification(null, 1);
        generateEndpointNotification(null, 2);
        CassandraEndpointNotification notification = notifications.get(0);
        List<CassandraEndpointNotification> found = unicastNotificationDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertEquals(notifications.size(), found.size());
    }

    @Test
    public void testRemoveNotificationsByKeyHash() throws Exception {
        ByteBuffer epKeyHash = ByteBuffer.wrap(generateBytes());
        generateEndpointNotification(epKeyHash, 3);
        unicastNotificationDao.removeNotificationsByKeyHash(epKeyHash.array());
        List<CassandraEndpointNotification> found = unicastNotificationDao.findNotificationsByKeyHash(epKeyHash.array());
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testRemoveNotificationsByAppId() throws Exception {
        CassandraEndpointNotification notification = generateEndpointNotification(null, 3).get(0);
        String appId = notification.getApplicationId();
        unicastNotificationDao.removeNotificationsByAppId(appId);
        List<CassandraEndpointNotification> found = unicastNotificationDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testSave() throws Exception {
        CassandraEndpointNotification notification = generateEndpointNotification(null, 1).get(0);
        List<CassandraEndpointNotification> found = unicastNotificationDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertEquals(1, found.size());
    }

    @Test
    public void testBytesToStringConversation() {
        byte[] array = new byte[]{-16, 7, 51, -98, -75, -19, -82, 119, -51, 122, -125, -14, 22, 44, -28, -56, 26, 111, 115, 2};
        String hash = Bytes.toHexString(array);
        LOG.info("---> hash is {}", hash);
        byte[] converted = Bytes.fromHexString(hash).array();
        Assert.assertArrayEquals(array, converted);
        Assert.assertEquals(hash, Bytes.toHexString(converted));
    }

    @Test
    public void testDtoConversation() throws Exception {
        CassandraEndpointNotification notification = generateEndpointNotification(null, 1).get(0);
        Assert.assertNull(notification.getId());
        EndpointNotificationDto notificationDto = notification.toDto();
        CassandraEndpointNotification cassandraEndpointNotification = new CassandraEndpointNotification(notificationDto);
        Assert.assertEquals(notificationDto,cassandraEndpointNotification.toDto() );
    }

    @Test
    public void testFindById() {
        CassandraEndpointNotification notification = generateEndpointNotification(null, 1).get(0);
        CassandraEndpointNotification saved = unicastNotificationDao.save(notification.toDto());
        Assert.assertNotNull(saved.getId());
        CassandraEndpointNotification found = unicastNotificationDao.findById(saved.getId());
        Assert.assertEquals(saved, found);
    }
}