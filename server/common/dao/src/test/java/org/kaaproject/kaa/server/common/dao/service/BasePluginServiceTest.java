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
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.sql.plugin.PluginTestFactory;
import org.springframework.dao.DataIntegrityViolationException;


public class BasePluginServiceTest extends AbstractTest {

    @Test
    public void registerPluginUseExistentContractTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        PluginDto registeredPlugin = pluginService.registerPlugin(pluginDto);

        // pretend to register yet another plugin
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
    public void getInstanceByIdTest() {
        PluginDto pluginDto = PluginTestFactory.create();
        pluginDto = pluginService.registerPlugin(pluginDto);
        PluginInstanceDto pluginInstanceDto = new PluginInstanceDto();
        pluginInstanceDto.setPluginDefinition(pluginDto);
        pluginInstanceDto.setName("Instance 1");
        pluginInstanceDto.setConfigurationData("ConfData");
        pluginInstanceDto.setState(PluginInstanceState.ACTIVE);
        PluginInstanceDto savedInstance = pluginService.saveInstance(pluginInstanceDto);
        Assert.assertNotNull(savedInstance.getId());
        PluginInstanceDto foundInstance = pluginService.getInstanceById(savedInstance.getId());
        Assert.assertEquals(savedInstance, foundInstance);
    }
}
