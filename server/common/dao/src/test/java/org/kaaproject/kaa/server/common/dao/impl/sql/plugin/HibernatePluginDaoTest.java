/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.common.dao.impl.sql.HibernateAbstractTest;
import org.kaaproject.kaa.server.common.dao.model.sql.plugin.Plugin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class HibernatePluginDaoTest extends HibernateAbstractTest {

    @Test
    public void testSavePlugin() {
        PluginDto pluginDto = PluginTestFactory.create();
        Plugin p = new Plugin(pluginDto);
        p = pluginDao.save(p);
        Assert.assertEquals(pluginDto, p.toDto());
    }

    @Test
    public void testFindPluginByNameAndVersion() {
        PluginDto pluginDto = PluginTestFactory.create();
        Plugin p = new Plugin(pluginDto);
        pluginDao.save(p);
        Plugin found = pluginDao.findByNameAndVersion(PluginTestFactory.NAME, PluginTestFactory.VERSION);
        Assert.assertEquals(pluginDto, found.toDto());
    }

    @Test
    public void testFindPluginByClassName() {
        PluginDto pluginDto = PluginTestFactory.create();
        Plugin p = new Plugin(pluginDto);
        p = pluginDao.save(p);
        Plugin found = pluginDao.findByClassName(PluginTestFactory.CLASS_NAME);
        Assert.assertEquals(pluginDto, found.toDto());
    }

    @Test
    public void testDeletePlugin() {
        PluginDto pluginDto = PluginTestFactory.create();
        Plugin p = new Plugin(pluginDto);
        p = pluginDao.save(p);
        Assert.assertNotNull(p.getStringId());
        pluginDao.removeById(p.getStringId());
        Plugin found = pluginDao.findByClassName(PluginTestFactory.CLASS_NAME);
        Assert.assertNull(found);
    }
}
