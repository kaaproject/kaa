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
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.UserDao;
import org.kaaproject.kaa.server.common.dao.mongo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserMongoDaoTest extends AbstractTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EndpointGroupMongoDaoTest.class);

    protected static final String USER = "User";
    protected static final String PASSWORD = "Password";

    @Autowired
    private UserDao<User> userDao;

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
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void removeByTenantId() {
        String id = users.get(0);
        User dto = userDao.findById(id);
        Assert.assertNotNull(dto);
        String tenantId = dto.getTenantId().toString();
        Assert.assertNotNull(tenantId);

        userDao.removeByTenantId(tenantId);
        dto = userDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void removeById() {
        String id = users.get(0);
        User dto = userDao.findById(id);
        Assert.assertNotNull(dto);

        userDao.removeById(id);
        dto = userDao.findById(id);
        Assert.assertNull(dto);
    }

    @Test
    public void findByExternalUidTest() {
        User dto = userDao.findById(users.get(0));
        Assert.assertNotNull(dto);
        User found = userDao.findByExternalUid(dto.getExternalUid());
        Assert.assertEquals(dto, found);
    }

    @Test
    public void findByTenantIdTest() {
        User dto = userDao.findById(users.get(0));
        Assert.assertNotNull(dto);
        List<User> userList = userDao.findByTenantId(dto.getTenantId().toString());
        User user = null;
        for(User found: userList) {
            if(dto.getId().equals(found.getId())) {
                user = found;
            }
        }
        Assert.assertNotNull(user);
        Assert.assertEquals(dto, user);
    }

    @Test
    public void removeByExternalUidTest() {
        User dto = userDao.findById(users.get(0));
        Assert.assertNotNull(dto);
        userDao.removeByExternalUid(dto.getExternalUid());
        dto = userDao.findById(users.get(0));
        Assert.assertNull(dto);
    }

    @Test
    public void convertToDtoTest() {
        User user = userDao.findById(users.get(0));
        Assert.assertNotNull(user);
        UserDto dto = user.toDto();
        User converted = new User(dto);
        Assert.assertEquals(user, converted);
    }
}
