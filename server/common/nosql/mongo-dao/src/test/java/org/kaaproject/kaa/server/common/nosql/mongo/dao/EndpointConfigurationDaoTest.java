/*
 * Copyright 2014-2016 CyberVision, Inc.
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.ByteBuffer;

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

    @After
    public void afterTest() {
        MongoDataLoader.clearDBData();
    }

    @Test
    public void saveEndpointConfigurationTest() {
        EndpointConfigurationDto configurationDto = generateEndpointConfiguration();
        MongoEndpointConfiguration found = endpointConfigurationDao.findById(ByteBuffer.wrap(configurationDto.getConfigurationHash()));
        Assert.assertNotNull(found);
        MongoEndpointConfiguration savedDto = endpointConfigurationDao.save(found);
        Assert.assertNotNull(savedDto);
        Assert.assertEquals(savedDto, found);
    }

    @Test
    public void findEndpointConfigurationByIdTest() {
        EndpointConfigurationDto configurationDto = generateEndpointConfiguration();
        MongoEndpointConfiguration found = endpointConfigurationDao.findById(ByteBuffer.wrap(configurationDto.getConfigurationHash()));
        Assert.assertNotNull(found);
    }

    @Test
    public void removeEndpointConfigurationByIdTest() {
        EndpointConfigurationDto endpointConfiguration = generateEndpointConfiguration();
        Assert.assertNotNull(endpointConfiguration);
        endpointConfigurationDao.removeById(ByteBuffer.wrap(endpointConfiguration.getConfigurationHash()));
        MongoEndpointConfiguration found = endpointConfigurationDao.findById(ByteBuffer.wrap(endpointConfiguration.getConfigurationHash()));
        Assert.assertNull(found);
    }

    @Test
    public void removeEndpointConfigurationByHashTest() {
        EndpointConfigurationDto endpointConfiguration = generateEndpointConfiguration();
        Assert.assertNotNull(endpointConfiguration);
        byte[] bytes = endpointConfiguration.getConfigurationHash();
        endpointConfigurationDao.removeByHash(bytes);
        MongoEndpointConfiguration configurationDto = endpointConfigurationDao.findByHash(bytes);
        Assert.assertNull(configurationDto);
    }

    @Test
    public void convertToDtoTest() {
        EndpointConfigurationDto endpointConfiguration = generateEndpointConfiguration();
        Assert.assertNotNull(endpointConfiguration);
        MongoEndpointConfiguration converted = new MongoEndpointConfiguration(endpointConfiguration);
        Assert.assertEquals(endpointConfiguration, converted.toDto());
    }
}
