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
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.mongo.model.Configuration;
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
public class ConfigurationMongoDaoTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMongoDaoTest.class);

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
    public void afterTest() throws IOException {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findConfigurationByIdTest() {
        String confId = configurations.get(0);
        Configuration configuration = configurationDao.findById(confId);
        Assert.assertNotNull(configuration.getSchemaId());
        ConfigurationSchema configurationSchemaDto = configurationSchemaDao.findById(configuration.getSchemaId().toString());
        Assert.assertNotNull(configurationSchemaDto);
    }

    @Test
    public void findLatestConfigurationByAppIdTest() {
        String id = apps.get(2);
        Configuration configuration = configurationDao.findConfigurationByAppIdAndVersion(id, 3);
        Assert.assertNotNull(configuration);
        Assert.assertEquals(configuration.getStatus(), UpdateStatus.ACTIVE);
    }

    @Test
    public void findConfigurationByEndpointGroupIdAndVersionTest() {
        String id = endGroups.get(0);
        int majorVersion = 1;
        Configuration configuration = configurationDao.findConfigurationByEndpointGroupIdAndVersion(id, majorVersion);
        Assert.assertNotNull(configuration);
        Assert.assertEquals(majorVersion, configuration.getMajorVersion());
        Assert.assertEquals(UpdateStatus.ACTIVE, configuration.getStatus());
    }

    @Test
    public void findConfigurationByAppIdTest() {
        List<Configuration> configs = configurationDao.findActiveByApplicationId(apps.get(0));
        Assert.assertNotNull(configs);
        Assert.assertNotEquals(0, configs.size());

        configs = configurationDao.findActiveByApplicationId(apps.get(1));
        Assert.assertNotNull(configs);
        Assert.assertNotEquals(0, configs.size());

        configs = configurationDao.findActiveByApplicationId(apps.get(2));
        Assert.assertNotNull(configs);
        Assert.assertNotEquals(0, configs.size());
    }

    @Test
    public void findLatestConfigurationByConfigurationSchemaIdTest() {
        Configuration configuration = configurationDao.findLatestActiveBySchemaIdAndGroupId(schemas.get(2), endGroups.get(0));
        Assert.assertNotNull(configuration);
        Assert.assertEquals(configuration.getStatus(), UpdateStatus.ACTIVE);
        Assert.assertEquals(configuration.getId(), configurations.get(6));
        Assert.assertSame(configuration.getMajorVersion(), 3);
        Assert.assertSame(configuration.getSequenceNumber(), 2);
    }

    @Test
    public void findInactiveByConfigurationSchemaIdTest() {
        Configuration configuration = configurationDao.findInactiveByConfigurationSchemaId(schemas.get(2));
        Assert.assertNotNull(configuration);
        Assert.assertEquals(configuration.getStatus(), UpdateStatus.INACTIVE);
        Assert.assertSame(configuration.getMajorVersion(), 3);
        Assert.assertSame(configuration.getSequenceNumber(), 2);
    }

    @Test
    public void findByConfigurationSchemaIdTest() {
        List<Configuration> configs = configurationDao.findActiveByConfigurationSchemaId(schemas.get(2));
        Assert.assertNotNull(configs);
        Assert.assertSame(configs.size(), 3);
    }

    @Test
    public void findConfigurationByEndpointGroupIdTest() {
        List<Configuration> config = configurationDao.findActiveByEndpointGroupId(endGroups.get(0));
        Assert.assertNotNull(config);
        Assert.assertNotSame(config.size(), 0);
    }

    @Test
    public void removeByConfigurationSchemaIdTest() {
        String schemaId = configurationDao.findById(configurations.get(6)).getSchemaId().toString();
        configurationDao.removeByConfigurationSchemaId(schemaId);
        List<Configuration> configs = configurationDao.findActiveByConfigurationSchemaId(schemaId);
        Assert.assertNotNull(configs);
        Assert.assertSame(configs.size(), 0);
    }

    @Test
    public void removeConfigurationByApplicationIdTest() {
        String appId = apps.get(1);
        List<Configuration> configs = configurationDao.findActiveByApplicationId(appId);
        Assert.assertNotNull(configs);
        Assert.assertNotSame(configs.size(), 0);

        configurationDao.removeByApplicationId(appId);
        List<Configuration> emptyConfig = configurationDao.findActiveByApplicationId(appId);
        Assert.assertNotNull(emptyConfig);
        Assert.assertSame(emptyConfig.size(), 0);

    }

    @Test
    public void activateTest() {
        String id = configurations.get(7);
        Configuration inactiveConfig = configurationDao.findById(id);
        Configuration activateConfig = configurationDao.activate(id, null);
        Assert.assertNotNull(activateConfig);
        Assert.assertNotEquals(inactiveConfig, activateConfig);
        Assert.assertEquals(activateConfig.getStatus(), UpdateStatus.ACTIVE);
        Assert.assertSame(inactiveConfig.getSequenceNumber() + 1, activateConfig.getSequenceNumber());
    }

    @Test
    public void convertToDtoTest() {
        String id = configurations.get(7);
        Configuration conf = configurationDao.findById(id);
        Assert.assertNotNull(conf);
        ConfigurationDto dto = conf.toDto();
        Configuration converted  = new Configuration(dto);
        Assert.assertEquals(conf, converted);
    }
}
