package org.kaaproject.kaa.server.common.dao.cassandra;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointUserCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testSave() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        CassandraEndpointUser found = userEndpointUserDao.findById(expected.getId());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByExternalIdAndTenantId() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        CassandraEndpointUser found = userEndpointUserDao.findByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testRemoveByExternalIdAndTenantId() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        userEndpointUserDao.removeByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        CassandraEndpointUser found = userEndpointUserDao.findByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        Assert.assertNull(found);
    }

    @Test
    public void testGenerateAccessToken() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        String accessToken = userEndpointUserDao.generateAccessToken(expected.getExternalId(), expected.getTenantId());
        CassandraEndpointUser found = userEndpointUserDao.findByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        Assert.assertEquals(accessToken, found.getAccessToken());
    }

    @Test
    public void testCheckAccessToken() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        String accessToken = userEndpointUserDao.generateAccessToken(expected.getExternalId(), expected.getTenantId());
        Boolean result = userEndpointUserDao.checkAccessToken(expected.getExternalId(), expected.getTenantId(), accessToken);
        Assert.assertTrue(result);
        result = userEndpointUserDao.checkAccessToken(expected.getExternalId(), expected.getTenantId(), "");
        Assert.assertFalse(result);
    }
}