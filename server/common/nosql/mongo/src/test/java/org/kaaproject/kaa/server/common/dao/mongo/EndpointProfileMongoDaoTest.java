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

package org.kaaproject.kaa.server.common.dao.mongo;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDataLoader;
import org.kaaproject.kaa.server.common.dao.mongo.model.MongoEndpointProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointProfileMongoDaoTest extends AbstractMongoTest {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointProfileMongoDaoTest.class);

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        LOG.info("EndpointProfileMongoDaoTest init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() throws IOException {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void testFindByKeyHash() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        Assert.assertNotNull(endpointProfile);
        MongoEndpointProfile found = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertNotNull(found);
        Assert.assertEquals(endpointProfile, found.toDto());
    }

    @Test
    public void testFindById() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        MongoEndpointProfile profile = endpointProfileDao.findById(ByteBuffer.wrap(endpointProfile.getEndpointKeyHash()));
        Assert.assertNotNull(profile);
    }

    @Test
    public void testRemoveByKeyHash() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        Assert.assertNotNull(endpointProfile);
        endpointProfileDao.removeByKeyHash(endpointProfile.getEndpointKeyHash());
        MongoEndpointProfile profile = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertNull(profile);
    }

    @Test
    public void removeByIdTest() {
        EndpointProfileDto epDto = generateEndpointProfile(null, null);
        Assert.assertNotNull(epDto);
        endpointProfileDao.removeById(ByteBuffer.wrap(epDto.getEndpointKeyHash()));
        MongoEndpointProfile endpointProfile = endpointProfileDao.findById(ByteBuffer.wrap(epDto.getEndpointKeyHash()));
        Assert.assertNull(endpointProfile);
    }

    @Test
    public void saveEndpointProfileTest() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        Assert.assertNotNull(endpointProfile);
        endpointProfile.setId(null);
        MongoEndpointProfile saved = endpointProfileDao.save(new MongoEndpointProfile(endpointProfile));
        Assert.assertNotNull(saved);
        Assert.assertEquals(endpointProfile, saved.toDto());
    }

    @Test
    public void convertToDtoTest() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        Assert.assertNotNull(endpointProfile);
        MongoEndpointProfile converted = new MongoEndpointProfile(endpointProfile);
        Assert.assertEquals(endpointProfile, converted.toDto());
    }

    @Test
    public void getCountByKeyHash() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        Assert.assertNotNull(endpointProfile);
        long count = endpointProfileDao.getCountByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(1, count);
    }

    @Test
    public void removeByAppId() {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null);
        Assert.assertNotNull(endpointProfile);
        byte[] keyHash = endpointProfile.getEndpointKeyHash();
        endpointProfileDao.removeByAppId(endpointProfile.getApplicationId());
        MongoEndpointProfile found = endpointProfileDao.findByKeyHash(keyHash);
        Assert.assertNull(found);
    }
}
