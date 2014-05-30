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
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.mongo.model.ConfigurationSchema;
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
public class ConfigurationSchemaMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSchemaMongoDaoTest.class);

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
    public void findSchemaByApplicationIdTest() {
        List<ConfigurationSchema> schemas = configurationSchemaDao.findByApplicationId(apps.get(0));
        Assert.assertNotNull(schemas);
        Assert.assertNotSame(schemas.size(), 0);
    }

    @Test
    public void getLatestSchemaByAppIdTest() {
        ConfigurationSchema schema = configurationSchemaDao.findByAppIdAndVersion(apps.get(0), 1);
        Assert.assertNotNull(schema);
        Assert.assertEquals(schema.getStatus(), UpdateStatus.ACTIVE);
        Assert.assertSame(schema.getMajorVersion(), 1);
    }

    @Test
    public void findLatestByAppId() {
        ConfigurationSchema schema = configurationSchemaDao.findLatestByApplicationId(apps.get(0));
        Assert.assertNotNull(schema);
        Assert.assertEquals(schema.getStatus(), UpdateStatus.ACTIVE);
        Assert.assertSame(schema.getMajorVersion(), 1);
    }

    @Test
    public void removeSchemaByAppId() {
        String id = apps.get(0);
        List<ConfigurationSchema> schemas = configurationSchemaDao.findByApplicationId(id);
        Assert.assertNotNull(schemas);
        Assert.assertNotSame(schemas.size(), 0);
        configurationSchemaDao.removeByAppId(id);

        schemas = configurationSchemaDao.findByApplicationId(id);
        Assert.assertNotNull(schemas);
        Assert.assertSame(schemas.size(), 0);
    }

    @Test
    public void convertToDtoTest() {
        String id = schemas.get(0);
        ConfigurationSchema schema = configurationSchemaDao.findById(id);
        Assert.assertNotNull(schema);
        ConfigurationSchemaDto dto = schema.toDto();
        ConfigurationSchema converted  = new ConfigurationSchema(dto);
        Assert.assertEquals(schema, converted);
    }
}
