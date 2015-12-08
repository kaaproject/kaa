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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointProfile;
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

    private static final String TEST_ENDPOINT_GROUP_ID = "124";
    private static final String TEST_LIMIT = "3";
    private static final String TEST_OFFSET = "0";
    private static final int GENERATED_PROFILES_COUNT = 5;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() throws IOException {
        MongoDataLoader.clearDBData();
    }

    @Test
    public void testFindByKeyHash() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        MongoEndpointProfile found = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertNotNull(found);
        Assert.assertEquals(endpointProfile, found.toDto());
    }

    @Test
    public void findBodyByKeyHashTest() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        EndpointProfileBodyDto found = endpointProfileDao.findBodyByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertNotNull(found);
        Assert.assertEquals(endpointProfile.getClientProfileBody(), found.getProfile());
    }

    @Test
    public void findBodyByEndpointGroupIdTest() {
        for (int i = 0; i < GENERATED_PROFILES_COUNT; i++) {
            generateEndpointProfileWithGroupIdDto(TEST_ENDPOINT_GROUP_ID, false);
        }
        int lim = Integer.valueOf(TEST_LIMIT);
        PageLinkDto pageLink = new PageLinkDto(TEST_ENDPOINT_GROUP_ID, TEST_LIMIT, TEST_OFFSET);
        EndpointProfilesBodyDto found = endpointProfileDao.findBodyByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfilesBody().isEmpty());
        Assert.assertEquals(lim, found.getEndpointProfilesBody().size());
    }

    @Test
    public void findBodyByEndpointGroupIdWithNfGroupStateTest() {
        for (int i = 0; i < GENERATED_PROFILES_COUNT; i++) {
            generateEndpointProfileWithGroupIdDto(TEST_ENDPOINT_GROUP_ID, true);
        }
        int lim = Integer.valueOf(TEST_LIMIT);
        PageLinkDto pageLink = new PageLinkDto(TEST_ENDPOINT_GROUP_ID, TEST_LIMIT, TEST_OFFSET);
        EndpointProfilesBodyDto found = endpointProfileDao.findBodyByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfilesBody().isEmpty());
        Assert.assertEquals(lim, found.getEndpointProfilesBody().size());
    }

    @Test
    public void findByEndpointGroupIdTest() {
        for (int i = 0; i < GENERATED_PROFILES_COUNT; i++) {
            generateEndpointProfileWithGroupIdDto(TEST_ENDPOINT_GROUP_ID, false);
        }
        int lim = Integer.valueOf(TEST_LIMIT);
        PageLinkDto pageLink = new PageLinkDto(TEST_ENDPOINT_GROUP_ID, TEST_LIMIT, TEST_OFFSET);
        EndpointProfilesPageDto found = endpointProfileDao.findByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        Assert.assertEquals(lim, found.getEndpointProfiles().size());
    }

    @Test
    public void findByEndpointGroupIdWithNfGroupStateTest() {
        for (int i = 0; i < GENERATED_PROFILES_COUNT; i++) {
            generateEndpointProfileWithGroupIdDto(TEST_ENDPOINT_GROUP_ID, true);
        }
        int lim = Integer.valueOf(TEST_LIMIT);
        PageLinkDto pageLink = new PageLinkDto(TEST_ENDPOINT_GROUP_ID, TEST_LIMIT, TEST_OFFSET);
        EndpointProfilesPageDto found = endpointProfileDao.findByEndpointGroupId(pageLink);
        Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        Assert.assertEquals(lim, found.getEndpointProfiles().size());
    }

    @Test
    public void testFindById() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        MongoEndpointProfile profile = endpointProfileDao.findById(ByteBuffer.wrap(endpointProfile.getEndpointKeyHash()));
        Assert.assertNotNull(profile);
    }

    @Test
    public void testRemoveByKeyHash() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        endpointProfileDao.removeByKeyHash(endpointProfile.getEndpointKeyHash());
        MongoEndpointProfile profile = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertNull(profile);
    }

    @Test
    public void removeByIdTest() {
        EndpointProfileDto epDto = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(epDto);
        endpointProfileDao.removeById(ByteBuffer.wrap(epDto.getEndpointKeyHash()));
        MongoEndpointProfile endpointProfile = endpointProfileDao.findById(ByteBuffer.wrap(epDto.getEndpointKeyHash()));
        Assert.assertNull(endpointProfile);
    }

    @Test
    public void saveEndpointProfileTest() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        endpointProfile.setId(null);
        MongoEndpointProfile saved = endpointProfileDao.save(new MongoEndpointProfile(endpointProfile));
        Assert.assertNotNull(saved);
        Assert.assertEquals(endpointProfile, saved.toDto());
    }

    @Test
    public void convertToDtoTest() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        MongoEndpointProfile converted = new MongoEndpointProfile(endpointProfile);
        Assert.assertEquals(endpointProfile, converted.toDto());
    }

    @Test
    public void getCountByKeyHash() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        long count = endpointProfileDao.getCountByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(1, count);
    }

    @Test
    public void removeByAppId() {
        EndpointProfileDto endpointProfile = generateEndpointProfileDto(null, null);
        Assert.assertNotNull(endpointProfile);
        byte[] keyHash = endpointProfile.getEndpointKeyHash();
        endpointProfileDao.removeByAppId(endpointProfile.getApplicationId());
        MongoEndpointProfile found = endpointProfileDao.findByKeyHash(keyHash);
        Assert.assertNull(found);
    }

}
