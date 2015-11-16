/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.plugin.messaging;

import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.AbstractSdkApiGenerator;
import org.kaaproject.kaa.server.common.core.plugin.generator.SpecificPluginSdkApiGenerationContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.kaaproject.kaa.server.plugin.messaging.gen.Configuration;

public class EndpointMessagePluginGenerator extends AbstractSdkApiGenerator<Configuration> {

    @Override
    protected List<SdkApiFile> generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<Configuration> context) {
        Configuration configuration = context.getConfiguration();
        Set<PluginContractInstance> contractInstances = context.getPluginContracts();
        
        configuration.getMessageFamilyFqn();
        return null;
    }

    @Override
    public Class<Configuration> getConfigurationClass() {
        return Configuration.class;
    }

}
