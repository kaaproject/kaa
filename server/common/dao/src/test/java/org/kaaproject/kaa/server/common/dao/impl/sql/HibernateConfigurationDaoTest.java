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

package org.kaaproject.kaa.server.common.dao.impl.sql;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.dao.model.sql.Application;
import org.kaaproject.kaa.server.common.dao.model.sql.Configuration;
import org.kaaproject.kaa.server.common.dao.model.sql.ConfigurationSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.EndpointGroup;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernateConfigurationDaoTest extends HibernateAbstractTest {

    @Test
    public void findConfigurationByAppIdAndVersionTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        Application app = first.getApplication();
        Configuration found = configurationDao.findConfigurationByAppIdAndVersion(app.getId().toString(), first.getSchemaVersion());
        Assert.assertEquals(first, found);
    }

    @Test
    public void findConfigurationByEndpointGroupIdAndVersionTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        Configuration found = configurationDao.findConfigurationByEndpointGroupIdAndVersion(group.getId().toString(), first.getSchemaVersion());
        Assert.assertEquals(first, found);
    }

    @Test
    public void findLatestActiveBySchemaIdAndGroupIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        ConfigurationSchema schema = first.getConfigurationSchema();
        Configuration found = configurationDao.findLatestActiveBySchemaIdAndGroupId(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(first, found);
    }

    @Test
    public void findInactiveBySchemaIdAndGroupIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        ConfigurationSchema schema = first.getConfigurationSchema();
        List<Configuration> inactiveConfigs = generateConfiguration(schema, group, 1, UpdateStatus.INACTIVE);
        generateConfiguration(schema, group, 3, UpdateStatus.DEPRECATED);
        Configuration found = configurationDao.findInactiveBySchemaIdAndGroupId(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(inactiveConfigs.get(0), found);
    }

    @Test
    public void findActiveByEndpointGroupIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 3, UpdateStatus.ACTIVE);
        Assert.assertEquals(3, configs.size());
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        List<Configuration> found = configurationDao.findActiveByEndpointGroupId(group.getId().toString());
        Assert.assertEquals(3, found.size());
        Assert.assertEquals(configs, found);
    }

    @Test
    public void findActualByEndpointGroupIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 4, UpdateStatus.DEPRECATED);
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        List<Configuration> active = generateConfiguration(null, group, 1, UpdateStatus.ACTIVE);
        List<Configuration> inactive = generateConfiguration(null, group, 1, UpdateStatus.INACTIVE);
        List<Configuration> actual = new ArrayList<>();
        actual.addAll(active);
        actual.addAll(inactive);
        List<Configuration> found = configurationDao.findActualByEndpointGroupId(group.getId().toString());
        Assert.assertEquals(actual.size(), found.size());
        Assert.assertEquals(actual, found);
    }

    @Test
    public void findActualBySchemaIdAndGroupIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 4, UpdateStatus.DEPRECATED);
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        ConfigurationSchema schema = first.getConfigurationSchema();
        List<Configuration> active = generateConfiguration(schema, group, 1, UpdateStatus.ACTIVE);
        generateConfiguration(null, group, 1, UpdateStatus.ACTIVE);
        List<Configuration> inactive = generateConfiguration(schema, group, 1, UpdateStatus.INACTIVE);
        List<Configuration> actual = new ArrayList<>();
        actual.addAll(active);
        actual.addAll(inactive);
        List<Configuration> found = configurationDao.findActualBySchemaIdAndGroupId(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(actual.size(), found.size());
        Assert.assertEquals(actual, found);
    }

    @Test
    public void findLatestDeprecatedTest() {
        List<Configuration> configs = generateConfiguration(null, null, 10, UpdateStatus.DEPRECATED);
        Assert.assertEquals(10, configs.size());
        Configuration first = configs.get(0);
        EndpointGroup group = first.getEndpointGroup();
        ConfigurationSchema schema = first.getConfigurationSchema();
        Configuration deprecated = configurationDao.findLatestDeprecated(schema.getId().toString(), group.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deprecated.getStatus());
        Assert.assertEquals(9, deprecated.getSequenceNumber());
    }

    @Test
    public void removeByConfigurationSchemaIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 3, UpdateStatus.ACTIVE);
        Assert.assertEquals(3, configs.size());
        Long id = configs.get(0).getId();
        ConfigurationSchema schema = configs.get(0).getConfigurationSchema();
        configurationDao.removeByConfigurationSchemaId(schema.getId().toString());
        Configuration found = configurationDao.findById(id.toString());
        Assert.assertNull(found);
    }

    @Test
    public void removeByEndpointGroupIdTest() {
        List<Configuration> configs = generateConfiguration(null, null, 3, UpdateStatus.ACTIVE);
        Assert.assertEquals(3, configs.size());
        Long id = configs.get(0).getId();
        EndpointGroup group = configs.get(0).getEndpointGroup();
        configurationDao.removeByEndpointGroupId(group.getId().toString());
        Configuration found = configurationDao.findById(id.toString());
        Assert.assertNull(found);
    }

    @Test
    public void activateTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, null);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        Configuration activated = configurationDao.activate(first.getId().toString(), "Test user");
        Configuration found = configurationDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.ACTIVE, activated.getStatus());
        Assert.assertEquals(activated, found);
    }

    @Test
    public void deactivateTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        Assert.assertEquals(UpdateStatus.ACTIVE, first.getStatus());
        Configuration deactivated = configurationDao.deactivate(first.getId().toString(), "Test user");
        Configuration found = configurationDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivated.getStatus());
        Assert.assertEquals(deactivated, found);
    }

    @Test
    public void deactivateOldConfigurationTest() {
        List<Configuration> configs = generateConfiguration(null, null, 1, UpdateStatus.ACTIVE);
        Assert.assertEquals(1, configs.size());
        Configuration first = configs.get(0);
        ConfigurationSchema schema = first.getConfigurationSchema();
        EndpointGroup group = first.getEndpointGroup();
        Assert.assertEquals(UpdateStatus.ACTIVE, first.getStatus());
        Configuration deactivated = configurationDao.deactivateOldConfiguration(schema.getId().toString(), group.getId().toString(), "Test user");
        Configuration found = configurationDao.findById(first.getId().toString());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivated.getStatus());
        Assert.assertEquals(deactivated, found);
    }
}
