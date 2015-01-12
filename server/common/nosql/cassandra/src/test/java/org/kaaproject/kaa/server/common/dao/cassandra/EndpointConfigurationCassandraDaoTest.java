package org.kaaproject.kaa.server.common.dao.cassandra;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointConfigurationCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testFindByHash() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        CassandraEndpointConfiguration found = endpointConfigurationDao.findByHash(expected.getConfigurationHash().array());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testRemoveByHash() throws Exception {
        List<CassandraEndpointConfiguration> configs = generateConfiguration(3);
        CassandraEndpointConfiguration expected = configs.get(0);
        endpointConfigurationDao.removeByHash(expected.getConfigurationHash().array());
        CassandraEndpointConfiguration found = endpointConfigurationDao.findByHash(expected.getConfigurationHash().array());
        Assert.assertNull(found);
    }

}