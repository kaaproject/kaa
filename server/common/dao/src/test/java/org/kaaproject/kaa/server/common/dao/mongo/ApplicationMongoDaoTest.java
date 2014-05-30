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

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ProcessingStatus;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.mongo.model.Application;
import org.kaaproject.kaa.server.common.dao.mongo.model.Tenant;
import org.kaaproject.kaa.server.common.dao.mongo.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ApplicationMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMongoDaoTest.class);

    private static final Random random = new Random(0);

    @Autowired
    private ApplicationService applicationService;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        LOGGER.info("ConfigurationMongoDao init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }


    @Test
    public void saveApplicationTest() {
        Application application = applicationDao.findById(apps.get(0));
        Assert.assertNotNull(application);
        application.setId(null);
        Application saved = applicationDao.save(application);
        Assert.assertNotNull(saved.getId());
    }

    @Test
    public void findApplicationByIdTest() {
        Application dto = applicationDao.findById(apps.get(0));
        Application app = applicationDao.findById(dto.getId());
        Assert.assertEquals(dto, app);
    }

    @Test
    public void findApplicationByTenantIdTest() {
        String tenantId = tenants.get(0);
        List<Application> apps = applicationDao.findByTenantId(tenantId);
        Assert.assertSame(apps.size(), 3);
    }

    @Test
    public void removeApplicationByIdTest() {
        String id = apps.get(0);
        Application dto = applicationDao.findById(id);
        Assert.assertNotNull(dto);
        applicationDao.removeById(id);
        Assert.assertNull(applicationDao.findById(id));
    }

    @Test
    public void removeApplicationByApplicationTokenTest() {
        String id = apps.get(0);
        Assert.assertNotNull(id);
        applicationDao.removeByApplicationToken(id);
        Assert.assertNull(applicationDao.findByApplicationToken(id));
    }

    @Test
    public void removeApplicationByTenantIdTest() {
        Tenant tenant = tenantDao.findById(tenants.get(0));
        Assert.assertNotNull(tenant.getId());

        List<Application> apps = applicationDao.findByTenantId(tenant.getId());
        Assert.assertSame(apps.size(), 3);

        applicationDao.removeByTenantId(tenant.getId());
        List<Application> emptyList = applicationDao.findByTenantId(tenant.getId());
        Assert.assertEquals(emptyList.size(), Collections.emptyList().size());
    }

    @Test
    public void updateApplicationTest() {
        Application dto = applicationDao.findById(apps.get(0));
        String id = dto.getId();
        Assert.assertNotNull(id);
        Application newApp = new Application();
        newApp.setId(id);
        newApp.setSequenceNumber(random.nextInt());
        newApp.setName(UPDATED_OBJECT);
        newApp.setTenantId(dto.getTenantId());
        Application updated = applicationDao.save(newApp);
        Application selected = applicationDao.findById(id);
        Assert.assertEquals(newApp, updated);
        Assert.assertEquals(newApp, selected);
    }

    @Test
    public void findApplicationByApplicationTokenTest() {
        Application application = applicationDao.findById(apps.get(0));
        Assert.assertNotNull(application);
        String token = application.getApplicationToken();
        if (StringUtils.isNotBlank(token)) {
            Application found = applicationDao.findByApplicationToken(token);
            Assert.assertEquals(application, found);
        } else {
            throw new RuntimeException("Application Token is empty.");
        }
    }

    @Test
    public void updateSeqNumberTest() {
        Application application = applicationDao.findById(apps.get(0));
        Assert.assertNotNull(application);
        Update upd = application.getUpdate();
        upd.setStatus(ProcessingStatus.PENDING);
        upd.setSequenceNumber(upd.getSequenceNumber()+1);
        Application saved = applicationDao.save(application);
        Assert.assertNotNull(saved);
        Application updated = applicationDao.updateSeqNumber(application.getId());
        Assert.assertNotNull(updated);
        Assert.assertEquals(updated.getSequenceNumber(), updated.getUpdate().getSequenceNumber());
        Assert.assertEquals(updated.getUpdate().getStatus(), ProcessingStatus.IDLE);
    }

    @Test
    public void getNextSeqNumberTest() {
        Application application = applicationDao.findById(apps.get(0));
        Assert.assertNotNull(application);
        Application updated = applicationDao.getNextSeqNumber(application.getId());
        Assert.assertNotNull(updated);
        Assert.assertNotEquals(updated.getSequenceNumber(), updated.getUpdate().getSequenceNumber());
        Assert.assertEquals(ProcessingStatus.PENDING, updated.getUpdate().getStatus());
    }

    @Test
    public void forceNextSeqNumberTest() {
        Application application = applicationDao.findById(apps.get(0));
        Assert.assertNotNull(application);
        Update upd = application.getUpdate();
        upd.setStatus(ProcessingStatus.PENDING);
        upd.setSequenceNumber(upd.getSequenceNumber()+1);
        Application saved = applicationDao.save(application);
        Assert.assertNotNull(saved);
        Application updated = applicationDao.forceNextSeqNumber(application.getId());
        Assert.assertNotNull(updated);
        Assert.assertNotEquals(updated.getSequenceNumber(), updated.getUpdate().getSequenceNumber());
        Assert.assertEquals(ProcessingStatus.PENDING, updated.getUpdate().getStatus());
    }

    @Test
    public void convertToDtoTest() {
        Application application = applicationDao.findById(apps.get(0));
        Assert.assertNotNull(application);
        application.setUpdate(null);
        ApplicationDto dto = application.toDto();
        Application converted = new Application(dto);
        Assert.assertEquals(application, converted);
    }

    @Test
    public void applicationCreateTest() {
        ApplicationDto application = new ApplicationDto();
        application.setName("My Application");
        application.setTenantId(new ObjectId().toString());
        application = applicationService.saveApp(application);
        Assert.assertTrue(ObjectId.isValid(application.getId()));
    }


}
