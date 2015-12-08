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

import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.CommunicationPluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginScope;
import org.kaaproject.kaa.server.common.core.plugin.generator.PluginSdkApiGenerator;
import org.kaaproject.kaa.server.plugin.contracts.messaging.MessagingPluginContract;
import org.kaaproject.kaa.server.plugin.messaging.gen.Configuration;

public class EndpointMessagingPluginDefinition implements CommunicationPluginDef {

    private static final long serialVersionUID = -5566067441896469264L;

    private final BasePluginDef pluginDef;

    public EndpointMessagingPluginDefinition() {
        this.pluginDef = new BasePluginDef.Builder("Endpoint Messaging Plugin", 1)
                .withSchema(Configuration.SCHEMA$.toString())
                .withScope(PluginScope.ENDPOINT).withType("LOL")
                .withContract(MessagingSDKContract.buildMessagingSDKContract())
                .withContract(MessagingPluginContract.buildMessagingContract(PluginContractDirection.IN)).build();
    }

    @Override
    public String getName() {
        return pluginDef.getName();
    }

    @Override
    public int getVersion() {
        return pluginDef.getVersion();
    }

    @Override
    public String getType() {
        return pluginDef.getType();
    }

    @Override
    public PluginScope getScope() {
        return pluginDef.getScope();
    }

    @Override
    public String getConfigurationSchema() {
        return pluginDef.getConfigurationSchema();
    }

    public Set<PluginContractDef> getPluginContracts() {
        return pluginDef.getPluginContracts();
    }

    @Override
    public Class<? extends PluginSdkApiGenerator> getSdkGeneratorClass() {
        return EndpointMessagePluginGenerator.class;
    }
}
