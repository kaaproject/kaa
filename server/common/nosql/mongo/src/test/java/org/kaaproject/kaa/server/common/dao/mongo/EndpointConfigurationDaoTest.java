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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDataLoader;
import org.kaaproject.kaa.server.common.dao.mongo.model.MongoEndpointConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointConfigurationDaoTest extends AbstractMongoTest {

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
    public void saveEndpointConfigurationTest() {
        MongoEndpointConfiguration found = endpointConfigurationDao.findById(endConf.get(0));
        Assert.assertNotNull(found);
        found.setId(null);
        MongoEndpointConfiguration savedDto = endpointConfigurationDao.save(found);
        Assert.assertNotNull(savedDto);
        Assert.assertEquals(savedDto, found);
    }

    @Test
    public void findEndpointConfigurationByIdTest() {
        MongoEndpointConfiguration found = endpointConfigurationDao.findById(endConf.get(0));
        Assert.assertNotNull(found);
    }

    @Test
    public void removeEndpointConfigurationByIdTest() {
        String id = endConf.get(0);
        MongoEndpointConfiguration endpointConfiguration = endpointConfigurationDao.findById(id);
        Assert.assertNotNull(endpointConfiguration);
        endpointConfigurationDao.removeById(id);

        endpointConfiguration = endpointConfigurationDao.findById(id);
        Assert.assertNull(endpointConfiguration);
    }

    @Test
    public void removeEndpointConfigurationByHashTest() {
        String id = endConf.get(0);
        MongoEndpointConfiguration endpointConfiguration = endpointConfigurationDao.findById(id);
        Assert.assertNotNull(endpointConfiguration);
        byte[] bytes = endpointConfiguration.getConfigurationHash();
        endpointConfiguration = endpointConfigurationDao.findByHash(bytes);
        Assert.assertNotNull(endpointConfiguration);
        endpointConfigurationDao.removeByHash(bytes);

        endpointConfiguration = endpointConfigurationDao.findByHash(bytes);
        Assert.assertNull(endpointConfiguration);
    }

    @Test
    public void convertToDtoTest() {
        String id = endConf.get(0);
        MongoEndpointConfiguration endpointConfiguration = endpointConfigurationDao.findById(id);
        Assert.assertNotNull(endpointConfiguration);
        EndpointConfigurationDto dto = endpointConfiguration.toDto();
        MongoEndpointConfiguration converted = new MongoEndpointConfiguration(dto);
        Assert.assertEquals(endpointConfiguration, converted);
    }
}
