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

package org.kaaproject.kaa.server.common.dao.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.dao.impl.SecureRoleDao;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDataLoader;
import org.kaaproject.kaa.server.common.dao.impl.mongo.SecureRoleMongoDao;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogEventMongoDBServiceImplTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEventMongoDBServiceImplTest.class);

    private static final String TEST_USER = "test user";
    private static final String TEST_PASSWORD = "test password";
    private static final String TEST_ROLE = "test role";
    private static final String TEST_COLLECTION = "test collection";

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
    public void createUserTest() {
        
        Assert.assertEquals(0, MongoDBTestRunner.getDB().getCollection("system.users").count());
        
        logEventService.createUser(TEST_USER, TEST_PASSWORD, TEST_ROLE);
        
        Assert.assertEquals(1, MongoDBTestRunner.getDB().getCollection("system.users").count());
    }

    @Test
    public void createRoleTest() throws UnknownHostException {

        Assert.assertEquals(0, MongoDBTestRunner.getDB().getCollection("system.roles").count());
        
        LogEventMongoDBServiceImpl logEventMongoDBServiceImpl = mock(LogEventMongoDBServiceImpl.class);  
        SecureRoleDao<SecureRole> secureRoleMongoDao = mock(SecureRoleMongoDao.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        
        ReflectionTestUtils.setField(secureRoleMongoDao, "mongoTemplate", mongoTemplate);
        ReflectionTestUtils.setField(logEventMongoDBServiceImpl, "secureRoleDao", secureRoleMongoDao);
        
        logEventMongoDBServiceImpl.createRole(TEST_ROLE, TEST_COLLECTION);
        secureRoleMongoDao.saveRole(null);
        
        ((SecureRoleMongoDao) verify(ReflectionTestUtils.getField(logEventMongoDBServiceImpl, "secureRoleDao"))).saveRole(any(SecureRole.class));
    }

    @Test
    public void removeAllTest() {
        logEventService.createCollection(COLLECTION_NAME);
        
        Assert.assertEquals(0, MongoDBTestRunner.getDB().getCollection(COLLECTION_NAME).count());
        
        List<LogEventDto> events = new ArrayList<LogEventDto>();
        events.add(new LogEventDto());
        events.add(new LogEventDto());
        events.add(new LogEventDto());
        
        logEventService.save(events, COLLECTION_NAME);
        
        Assert.assertEquals(3, MongoDBTestRunner.getDB().getCollection(COLLECTION_NAME).count());
        
        logEventService.removeAll(COLLECTION_NAME);
        
        Assert.assertEquals(0, MongoDBTestRunner.getDB().getCollection(COLLECTION_NAME).count());
    }
}
