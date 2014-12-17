package org.kaaproject.kaa.server.common.dao.cassandra;

import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestExecutionListeners({CassandraUnitDependencyInjectionTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@CassandraDataSet(keyspace = "kaa", value = {"cassandra.cql"})
@EmbeddedCassandra(configuration = "/embedded-cassandra.yaml")
public class EndpointProfileCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testFindByKeyHash() throws Exception {

    }

    @Test
    public void testGetCountByKeyHash() throws Exception {

    }

    @Test
    public void testRemoveByKeyHash() throws Exception {

    }

    @Test
    public void testRemoveByAppId() throws Exception {

    }

    @Test
    public void testFindByAccessToken() throws Exception {

    }

    @Test
    public void testFindByEndpointUserId() throws Exception {

    }
}