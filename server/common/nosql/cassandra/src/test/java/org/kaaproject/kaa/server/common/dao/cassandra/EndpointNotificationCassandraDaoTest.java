package org.kaaproject.kaa.server.common.dao.cassandra;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
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
        List<CassandraEndpointNotification> found = endpointNotificationDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertEquals(notifications.size(), found.size());
    }

    @Test
    public void testRemoveNotificationsByKeyHash() throws Exception {
        ByteBuffer epKeyHash = ByteBuffer.wrap(generateBytes());
        generateEndpointNotification(epKeyHash, 3);
        endpointNotificationDao.removeNotificationsByKeyHash(epKeyHash.array());
        List<CassandraEndpointNotification> found = endpointNotificationDao.findNotificationsByKeyHash(epKeyHash.array());
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testRemoveNotificationsByAppId() throws Exception {
        CassandraEndpointNotification notification = generateEndpointNotification(null, 3).get(0);
        String appId = notification.getNotification().getApplicationId();
        endpointNotificationDao.removeNotificationsByAppId(appId);
        List<CassandraEndpointNotification> found = endpointNotificationDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testSave() throws Exception {
        CassandraEndpointNotification notification = generateEndpointNotification(null, 1).get(0);
        List<CassandraEndpointNotification> found = endpointNotificationDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertEquals(1, found.size());
    }
}