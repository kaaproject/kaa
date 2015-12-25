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

package org.kaaproject.kaa.server.common.dao.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.sql.plugin.PluginInstanceTestFactory;
import org.kaaproject.kaa.server.common.dao.impl.sql.plugin.PluginTestFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;

public abstract class BasePluginServiceTest extends AbstractTest {

    @Test
    public void registerPluginUseExistentContractTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        PluginDto registeredPlugin = pluginService.registerPlugin(pluginDto);

        // pretend to register another plugin
        registeredPlugin.setId(null);
        registeredPlugin.setName("anotherName");
        registeredPlugin.setClassName("anotherClassName");
        registeredPlugin.setVersion(2);

        // reset plugin contract id, so the same plugin contract is fetched from db
        PluginContractDto pluginContract = registeredPlugin.getPluginContracts().iterator().next();
        String contractId = pluginContract.getContract().getId();
        pluginContract.getContract().setId(null);

        PluginDto newlyRegisteredPlugin = pluginService.registerPlugin(registeredPlugin);
        PluginContractDto newlyRegisteredPluginContract = newlyRegisteredPlugin.getPluginContracts().iterator().next();
        Assert.assertEquals(contractId, newlyRegisteredPluginContract.getContract().getId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void registerSamePluginTwiceTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        pluginService.registerPlugin(pluginDto);
        pluginService.registerPlugin(pluginDto);
    }

    @Test
    public void findPluginByNameAndVersionTest() {
        PluginDto foundPluginDto = pluginService.findPluginByNameAndVersion("Incorrect name", 14);
        Assert.assertNull(foundPluginDto);
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        foundPluginDto = pluginService.findPluginByNameAndVersion(pluginDto.getName(), pluginDto.getVersion());
        Assert.assertEquals(pluginDto, foundPluginDto);
    }

    @Test
    public void findPluginByClassName() {
        PluginDto foundPluginDto = pluginService.findPluginByClassName("Incorrect class name");
        Assert.assertNull(foundPluginDto);
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        foundPluginDto = pluginService.findPluginByClassName(pluginDto.getClassName());
        Assert.assertEquals(pluginDto, foundPluginDto);
    }

    @Test
    public void findInstanceByIdTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        PluginInstanceDto pluginInstanceDto = PluginInstanceTestFactory.create(pluginDto);
        PluginInstanceDto savedInstance = pluginService.saveInstance(pluginInstanceDto);
        Assert.assertNotNull(savedInstance.getId());
        PluginInstanceDto foundInstance = pluginService.findInstanceById(savedInstance.getId());
        Assert.assertEquals(savedInstance, foundInstance);
    }

    @Test
    public void findInstancesByPluginIdTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        PluginInstanceDto pluginInstanceDto1 = PluginInstanceTestFactory.create(pluginDto, "instanceName1");
        pluginInstanceDto1 = pluginService.saveInstance(pluginInstanceDto1);
        PluginInstanceDto pluginInstanceDto2 = PluginInstanceTestFactory.create(pluginDto, "instanceName2");
        pluginInstanceDto2 = pluginService.saveInstance(pluginInstanceDto2);
        Set<PluginInstanceDto> pluginInstances = pluginService.findInstancesByPluginId(pluginDto.getId());
        Assert.assertEquals(2, pluginInstances.size());
        Assert.assertTrue(pluginInstances.contains(pluginInstanceDto1));
        Assert.assertTrue(pluginInstances.contains(pluginInstanceDto2));
    }

    @Test
    public void removeInstanceById() {
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        PluginInstanceDto pluginInstanceDto = PluginInstanceTestFactory.create(pluginDto);
        pluginService.removeInstanceById(pluginInstanceDto.getId());
        PluginInstanceDto foundInstance = pluginService.findInstanceById(pluginInstanceDto.getId());
        Assert.assertNull(foundInstance);
    }

    @Test
    public void unregisterPluginByIdTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        PluginInstanceDto pluginInstanceDto1 = PluginInstanceTestFactory.create(pluginDto, "instanceName1");
        pluginInstanceDto1 = pluginService.saveInstance(pluginInstanceDto1);
        PluginInstanceDto pluginInstanceDto2 = PluginInstanceTestFactory.create(pluginDto, "instanceName2");
        pluginInstanceDto2 = pluginService.saveInstance(pluginInstanceDto2);
        pluginService.unregisterPluginById(pluginDto.getId());
        Set<PluginInstanceDto> pluginInstances = pluginService.findInstancesByPluginId(pluginDto.getId());
        Assert.assertTrue(pluginInstances.isEmpty());
        Assert.assertNull(pluginService.findInstanceById(pluginInstanceDto1.getId()));
        Assert.assertNull(pluginService.findInstanceById(pluginInstanceDto2.getId()));
    }
}
