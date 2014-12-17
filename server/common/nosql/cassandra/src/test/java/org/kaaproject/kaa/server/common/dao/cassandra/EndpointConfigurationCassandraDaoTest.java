package org.kaaproject.kaa.server.common.dao.cassandra;

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener;
import org.cassandraunit.spring.CassandraUnitTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestExecutionListeners({CassandraUnitDependencyInjectionTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@CassandraDataSet(keyspace = "kaa", value = {"cassandra.cql"})
@EmbeddedCassandra(configuration = "/embedded-cassandra.yaml")
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