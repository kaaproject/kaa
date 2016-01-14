/*
 * Copyright 2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.control.service.modularization;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.PluginSdkApiGenerationContext;
import org.kaaproject.kaa.server.common.core.plugin.generator.SdkApiGenerationException;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.control.service.exception.KaaPluginLoadException;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.plugin.BasePluginInitContext;
import org.kaaproject.kaa.server.plugin.messaging.EndpointMessagingPluginDefinition;
import org.kaaproject.kaa.server.plugin.messaging.JavaEndpointMessagingPluginGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
public class JavaEndpointMessagingPluginGeneratorIT {

    @Autowired
    private KaaPluginLoadService kaaPluginLoadService;

    @Autowired
    private PluginService pluginService;

    @Test
    public void generatePluginSdkApiTest() throws IOException, SdkApiGenerationException, KaaPluginLoadException {
        JavaEndpointMessagingPluginGenerator generator = new JavaEndpointMessagingPluginGenerator();
        kaaPluginLoadService.load();

        List<PluginDto> plugins = pluginService.findAllPlugins();
        PluginDto messagingPlugin = getPluginForClass(EndpointMessagingPluginDefinition.class, plugins);
        Set<PluginInstanceDto> pluginInstances = messagingPlugin.getPluginInstances();
        PluginInstanceDto messagingPluginInstance = getInstanceForName(HardCodedPluginInstanceFactory.ENDPOINT_MESSAGING_PLUGIN_INSTANCE_NAME, pluginInstances);
        PluginInitContext initContext = new BasePluginInitContext(messagingPluginInstance);

        PluginSdkApiGenerationContext pluginSdkApiGenerationContext = new PluginSdkApiGenerationContext() {
            @Override
            public int getExtensionId() {
                return 1;
            }

            @Override
            public String getPluginConfigurationData() {
                return messagingPluginInstance.getConfigurationData();
            }

            @Override
            public Set<PluginContractInstance> getPluginContracts() {
                return initContext.getPluginContracts();
            }
        };

        List<SdkApiFile> files = generator.generatePluginSdkApi(pluginSdkApiGenerationContext).getFiles();
        Assert.assertEquals(5, files.size());
    }

    private PluginDto getPluginForClass(Class<?> clazz, List<PluginDto> plugins) {
        for (PluginDto pluginDto : plugins) {
            if (pluginDto.getClassName().equals(clazz.getName())) {
                return pluginDto;
            }
        }
        return null;
    }

    private PluginInstanceDto getInstanceForName(String name, Set<PluginInstanceDto> pluginInstances) {
        for (PluginInstanceDto pluginInstanceDto : pluginInstances) {
            if (pluginInstanceDto.getName().equals(name)) {
                return pluginInstanceDto;
            }
        }
        return null;
    }
}
