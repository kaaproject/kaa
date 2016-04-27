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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUser;
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
        CassandraEndpointUser found = endpointUserDao.findById(expected.getId());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByExternalIdAndTenantId() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        CassandraEndpointUser found = endpointUserDao.findByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testRemoveByExternalIdAndTenantId() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        endpointUserDao.removeByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        CassandraEndpointUser found = endpointUserDao.findByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        Assert.assertNull(found);
    }

    @Test
    public void testGenerateAccessToken() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        String accessToken = endpointUserDao.generateAccessToken(expected.getExternalId(), expected.getTenantId());
        CassandraEndpointUser found = endpointUserDao.findByExternalIdAndTenantId(expected.getExternalId(), expected.getTenantId());
        Assert.assertEquals(accessToken, found.getAccessToken());
    }

    @Test
    public void testCheckAccessToken() throws Exception {
        EndpointUserDto expected = generateEndpointUser();
        String accessToken = endpointUserDao.generateAccessToken(expected.getExternalId(), expected.getTenantId());
        Boolean result = endpointUserDao.checkAccessToken(expected.getExternalId(), expected.getTenantId(), accessToken);
        Assert.assertTrue(result);
        result = endpointUserDao.checkAccessToken(expected.getExternalId(), expected.getTenantId(), "");
        Assert.assertFalse(result);
    }
}