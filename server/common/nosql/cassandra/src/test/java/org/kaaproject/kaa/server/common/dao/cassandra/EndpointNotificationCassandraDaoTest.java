package org.kaaproject.kaa.server.common.dao.cassandra;

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.nio.ByteBuffer;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestExecutionListeners({CassandraUnitDependencyInjectionTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@CassandraDataSet(keyspace = "kaa", value = {"cassandra.cql"})
@EmbeddedCassandra(configuration = "/embedded-cassandra.yaml")
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
        ByteBuffer epKeyHash = ByteBuffer.wrap(generateBytes());
        String appId = generateEndpointNotification(epKeyHash, 3).get(0).getNotification().getApplicationId();
        endpointNotificationDao.removeNotificationsByAppId(appId);
        List<CassandraEndpointNotification> found = endpointNotificationDao.findNotificationsByKeyHash(epKeyHash.array());
        Assert.assertTrue(found.isEmpty());
    }

    @Ignore
    @Test
    public void testSave() throws Exception {

    }
}