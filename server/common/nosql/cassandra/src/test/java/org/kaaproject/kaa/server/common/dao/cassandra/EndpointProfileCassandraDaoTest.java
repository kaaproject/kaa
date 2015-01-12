package org.kaaproject.kaa.server.common.dao.cassandra;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointProfileCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testSave() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(endpointProfile, found.toDto());
    }

    @Test
    public void testFindByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testGetCountByKeyHash() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null);
        long count = endpointProfileDao.getCountByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(1L, count);
    }

    @Test
    public void testRemoveByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null);
        endpointProfileDao.removeByKeyHash(expected.getEndpointKeyHash());
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNull(found);
    }

    @Test
    public void testRemoveByAppId() throws Exception {

    }

    @Test
    public void testFindByAccessToken() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null);
        EndpointProfile found = endpointProfileDao.findByAccessToken(expected.getAccessToken());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByEndpointUserId() throws Exception {

    }
}