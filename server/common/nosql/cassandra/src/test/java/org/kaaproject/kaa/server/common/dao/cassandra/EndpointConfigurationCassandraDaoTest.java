package org.kaaproject.kaa.server.common.dao.cassandra;

import com.datastax.driver.core.Session;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.server.common.dao.cassandra.client.CassandraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@TestExecutionListeners({ CassandraUnitTestExecutionListener.class })
//@CassandraDataSet(value = { "/cassandra.cql" })
//@EmbeddedCassandra(configuration = "embedded-cassandra.yaml")
public class EndpointConfigurationCassandraDaoTest {

    @Autowired
    private EndpointConfigurationCassandraDao endpointConfigurationDao;
    @Autowired
    private CassandraClient cassandraClient;

    @Test
    public void testFindByHash() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("embedded-cassandra.yaml");

    }

    @Test
    public void testRemoveByHash() throws Exception {

    }

}