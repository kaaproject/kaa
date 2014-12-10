package org.kaaproject.kaa.server.common.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.Bytes;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.cassandra.client.CassandraClient;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointNotificationCassandraDaoTest extends AbstractCassandraTest {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointNotificationCassandraDaoTest.class);

    @Autowired
    private CassandraClient cassandraClient;
    private Session session;

    @Before
    public void init() {
        session = cassandraClient.getSession();
    }

    @After
    public void after() {
        cassandraClient.close();
    }

    @Test
    public void testFindNotificationsByKeyHash() throws Exception {
        List<CassandraEndpointNotification> notifications = generateEndpointNotification(null, 1);
//        generateEndpointNotification(null, 2);
        CassandraEndpointNotification notification = notifications.get(0);
        List<CassandraEndpointNotification> found = endpointNotificationCassandraDao.findNotificationsByKeyHash(notification.getEndpointKeyHash().array());
        Assert.assertEquals(notifications.size(), found.size());
    }

    @Test
    public void testRemoveNotificationsByKeyHash() throws Exception {

    }

    @Test
    public void testRemoveNotificationsByAppId() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }
}