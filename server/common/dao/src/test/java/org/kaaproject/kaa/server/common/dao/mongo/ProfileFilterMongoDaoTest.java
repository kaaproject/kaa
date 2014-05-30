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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProfileFilterMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFilterMongoDaoTest.class);

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
        LOGGER.info("ProfileFilterMongoDaoTest init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() throws IOException {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void testFindByProfileSchemaId() throws Exception {
        ProfileSchemaDto schemaDto = generateProfSchema(null, 1).get(0);
        ProfileFilterDto filter = generateFilter(schemaDto.getId(), null, 1, true).get(0);
        List<ProfileFilter> filters = profileFilterDao.findActiveByProfileSchemaId(schemaDto.getId());
        Assert.assertNotNull(filters);
        checkList(filters, new ProfileFilter(filter));
    }

    @Test
    public void testFindByEndpointGroupId() throws Exception {
        ProfileSchemaDto schema = generateProfSchema(null, 1).get(0);
        ProfileFilterDto filter = generateFilter(schema.getId(), null, 1, true).get(0);
        Assert.assertNotNull(filter);
        ProfileFilter found = profileFilterDao.findActiveByEndpointGroupId(filter.getEndpointGroupId().toString());
        Assert.assertEquals(new ProfileFilter(filter), found);
    }

    @Test
    public void testRemoveByProfileSchemaId() throws Exception {
        ProfileSchemaDto schema = generateProfSchema(null, 1).get(0);
        List<ProfileFilterDto> filters = generateFilter(schema.getId(), null, 1, true);
        Assert.assertNotNull(filters);
        Assert.assertFalse(filters.isEmpty());
        profileFilterDao.removeByProfileSchemaId(schema.getId());
        List<ProfileFilter> filter = profileFilterDao.findActiveByProfileSchemaId(schema.getId());
        Assert.assertNotNull(filter);
        Assert.assertTrue(filter.isEmpty());
    }

    @Test
    public void testFindByAppAndSchemaVersion() throws Exception {
        ProfileSchemaDto schema = generateProfSchema(null, 1).get(0);
        ProfileFilterDto filter = generateFilter(schema.getId(), null, 1, true).get(0);
        Assert.assertNotNull(filter);
        List<ProfileFilter> found = profileFilterDao.findByAppIdAndSchemaVersion(filter.getApplicationId(), schema.getMajorVersion());
        Assert.assertNotNull(found);
        checkList(found, new ProfileFilter(filter));
    }

    @Test
    public void testFindInactiveFilterByProfSchemaId() throws Exception {
        ProfileSchemaDto schemaDto = generateProfSchema(null, 1).get(0);
        ProfileFilterDto filterDto = generateFilter(schemaDto.getId(), null, 1, false).get(0);
        ProfileFilter filter = profileFilterDao.findInactiveFilter(schemaDto.getId(), filterDto.getEndpointGroupId());
        Assert.assertNotNull(filter);
        Assert.assertEquals(filter.getStatus(), UpdateStatus.INACTIVE);
    }

    @Test
    public void testFindLatestFilterByProfSchemaId() throws Exception {
        ProfileFilterDto dto = generateFilter(null, null, 1, true).get(0);
        ProfileFilter filter = profileFilterDao.findLatestFilter(dto.getSchemaId(), dto.getEndpointGroupId());
        Assert.assertNotNull(filter);
        Assert.assertNotEquals(filter.getStatus(), UpdateStatus.INACTIVE);
    }

    @Test
    public void testActivate() throws Exception {
        ProfileFilterDto dto = generateFilter(null, null, 1, false).get(0);
        ProfileFilter inactiveFilter = new ProfileFilter(dto);
        Assert.assertNotNull(inactiveFilter);
        ProfileFilter activeFilter = profileFilterDao.activate(inactiveFilter.getId(), null);
        Assert.assertNotNull(activeFilter);
        Assert.assertNotEquals(inactiveFilter, activeFilter);
        Assert.assertNotEquals(activeFilter.getStatus(), UpdateStatus.INACTIVE);
        inactiveFilter = profileFilterDao.findInactiveFilter(dto.getSchemaId(), dto.getEndpointGroupId());
        Assert.assertNull(inactiveFilter);
    }

    @Test
    public void testSave() throws Exception {
        ProfileFilterDto dto = generateFilter(null, null, 1, true).get(0);
        ProfileFilter filter = new ProfileFilter(dto);
        Assert.assertNotNull(filter);
        filter.setId(null);
        ProfileFilter saved = profileFilterDao.save(filter);
        Assert.assertNotNull(saved.getId());
        Assert.assertEquals(filter, saved);
    }


    @Test
    public void testConvertToDto() throws Exception {
        ProfileFilterDto dto = generateFilter(null, null, 1, false).get(0);
        ProfileFilter converted = new ProfileFilter(dto);
        Assert.assertEquals(dto, converted.toDto());
    }

    private void checkList(List<ProfileFilter> filterList, ProfileFilter filter) {
        ProfileFilter found = null;
        for (ProfileFilter foundFilter : filterList) {
            if (foundFilter.getId().equals(filter.getId())) {
                found = foundFilter;
            }
        }
        Assert.assertEquals(filter, found);
    }
}
