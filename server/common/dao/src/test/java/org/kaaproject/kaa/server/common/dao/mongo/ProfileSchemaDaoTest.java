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
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.server.common.dao.mongo.model.ProfileSchema;
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
public class ProfileSchemaDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileSchemaDaoTest.class);

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
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findByApplicationIdTest() {
        ProfileSchemaDto profileSchemaDto = generateProfSchema(null, 1).get(0);
        Assert.assertNotNull(profileSchemaDto);
        ProfileSchema profileSchema = new ProfileSchema(profileSchemaDto);
        List<ProfileSchema> schemaList = profileSchemaDao.findByApplicationId(profileSchema.getApplicationId().toString());
        Assert.assertNotNull(schemaList);
        ProfileSchema found = null;
        for(ProfileSchema schema:schemaList) {
            if(schema.getId().equals(profileSchema.getId())) {
                found = schema;
            }
        }
        Assert.assertEquals(profileSchema, found);
    }

    @Test
    public void findByAppIdAndVersionTest() {
        ProfileSchemaDto profileSchema = generateProfSchema(null, 1).get(0);
        Assert.assertNotNull(profileSchema);
        ProfileSchema found = profileSchemaDao.findByAppIdAndVersion(profileSchema.getApplicationId(), profileSchema.getMajorVersion());
        Assert.assertEquals(new ProfileSchema(profileSchema), found);
    }

    @Test
    public void removeByApplicationIdTest() {
        ProfileSchemaDto profileSchema = generateProfSchema(null, 1).get(0);
        Assert.assertNotNull(profileSchema);
        profileSchemaDao.removeByApplicationId(profileSchema.getApplicationId().toString());
        ProfileSchema schema = profileSchemaDao.findById(profileSchema.getId());
        Assert.assertNull(schema);
    }

    @Test
    public void removeByIdTest() {
        ProfileSchemaDto profileSchema = generateProfSchema(null, 1).get(0);
        Assert.assertNotNull(profileSchema);
        profileSchemaDao.removeById(profileSchema.getId());
        ProfileSchema schema = profileSchemaDao.findById(profileSchema.getId());
        Assert.assertNull(schema);
    }

    @Test
    public void convertToDtoTest() {
        ProfileSchemaDto profileSchema = generateProfSchema(null, 1).get(0);
        Assert.assertNotNull(profileSchema);
        ProfileSchema converted = new ProfileSchema(profileSchema);
        Assert.assertEquals(profileSchema, converted.toDto());
    }
}
