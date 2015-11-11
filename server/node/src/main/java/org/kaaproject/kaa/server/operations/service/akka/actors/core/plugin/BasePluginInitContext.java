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
package org.kaaproject.kaa.server.operations.service.akka.actors.core.plugin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;

public class BasePluginInitContext implements PluginInitContext {

    private final byte[] configurationData;
    private final Set<PluginContractInstance> contracts;

    public BasePluginInitContext(PluginInstanceDto dto) {
        this.configurationData = dto.getConfigurationData();
        this.contracts = new LinkedHashSet<PluginContractInstance>();
        //TODO: convert info from DTO to contracts 
    }

    @Override
    public byte[] getPluginConfigurationData() {
        return configurationData;
    }

    @Override
    public Set<PluginContractInstance> getPluginContracts() {
        return contracts;
    }

}
