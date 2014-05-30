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
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.server.common.dao.mongo.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TenantMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMongoDaoTest.class);

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
        LOGGER.info("ConfigurationMongoDao init before tests.");
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findTenantByIdTest() {
        Tenant dto = tenantDao.findById(tenants.get(0));
        tenantDao.save(dto);
        Tenant findResult = tenantDao.findById(dto.getId());
        Assert.assertEquals(dto, findResult);
    }

    @Test
    public void findTenantByNameTest() {
        Tenant dto = tenantDao.findById(tenants.get(0));
        tenantDao.save(dto);
        Tenant findResult = tenantDao.findByName(dto.getName());
        Assert.assertEquals(dto, findResult);
    }

    @Test
    public void updateTenantTest() {
        Tenant dto = tenantDao.findById(tenants.get(0));
        tenantDao.save(dto);

        Tenant update = new Tenant();
        update.setId(dto.getId());
        update.setName(UPDATED_OBJECT);
        Tenant upd = tenantDao.save(update);
        Assert.assertEquals(update, upd);

        Tenant fnd = tenantDao.findById(dto.getId());
        Assert.assertEquals(update, fnd);
        LOGGER.debug("New tenant id " + dto.getId());
        Assert.assertNotNull(dto.getId());
    }

    @Test
    public void saveTenantTest() {
        Tenant tenant = tenantDao.findById(tenants.get(0));
        tenant.setId(null);
        tenantDao.save(tenant);
        LOGGER.debug("New tenant id " + tenant.getId());
        Assert.assertNotNull(tenant.getId());
    }


    @Test
    public void removeTenantByIdTest() {
        Tenant tenant = tenantDao.findById(tenants.get(0));
        tenantDao.save(tenant);
        LOGGER.debug("New tenant id " + tenant.getId());
        String id = tenant.getId();
        tenantDao.removeById(id);
        Assert.assertNull(tenantDao.findById(id));
    }

    @Test
    public void convertToDtoTest() {
        Tenant tenant = tenantDao.findById(tenants.get(0));
        Assert.assertNotNull(tenant);
        TenantDto dto = tenant.toDto();
        Tenant converted = new Tenant(dto);
        Assert.assertEquals(tenant, converted);
    }
}
