/*
 * Copyright 2014 CyberVision, Inc.
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
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointProfileCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testSave() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(endpointProfile, found.toDto());
    }

    @Test
    public void testFindByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testGetCountByKeyHash() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null, null);
        long count = endpointProfileDao.getCountByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(1L, count);
    }

    @Test
    public void testRemoveByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        endpointProfileDao.removeByKeyHash(expected.getEndpointKeyHash());
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNull(found);
    }

    @Test
    public void testRemoveByAppId() throws Exception {

    }

    @Test
    public void testFindByAccessToken() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null, null);
        EndpointProfile found = endpointProfileDao.findByAccessToken(expected.getAccessToken());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByEndpointUserId() throws Exception {
        EndpointProfileDto endpointProfileDto = generateEndpointProfile(null, null, null, null);
        EndpointUserDto endpointUserDto = generateEndpointUser(Arrays.asList(endpointProfileDto.getId()));
        List<CassandraEndpointProfile> found = endpointProfileDao.findByEndpointUserId(endpointUserDto.getId());
        Assert.assertFalse(found.isEmpty());
        Assert.assertEquals(endpointProfileDto, found.get(0).toDto());
    }

    @Test
    public void testCheckSdkToken() throws Exception {
        this.generateEndpointProfile(null, "alpha", null, null);
        Assert.assertTrue(endpointProfileDao.checkSdkToken("alpha"));
        Assert.assertFalse(endpointProfileDao.checkSdkToken("beta"));
    }
}