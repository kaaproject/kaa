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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kaaproject.kaa.common.dto.plugin.ContractDto;
import org.kaaproject.kaa.common.dto.plugin.ContractType;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemInfo;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;

public class BasePluginInitContext implements PluginInitContext {

    private final String configurationData;
    private final Set<PluginContractInstance> contracts;

    public BasePluginInitContext(PluginInstanceDto dto) {
        this.configurationData = dto.getConfigurationData();
        this.contracts = new LinkedHashSet<>();
        for (PluginContractInstanceDto contractInstanceDto : dto.getContracts()) {
            PluginContractDto pluginContractDto = contractInstanceDto.getContract();
            ContractDto contractDto = pluginContractDto.getContract();
            BasePluginContractDef.Builder builder = BasePluginContractDef.builder(contractDto.getName(), contractDto.getVersion())
                    .withType(ContractType.valueOf(contractDto.getType().name()));
            if (pluginContractDto.getDirection() != null) {
                builder = builder.withDirection(PluginContractDirection.valueOf(pluginContractDto.getDirection().name()));
            }
            Map<PluginContractItemDef, PluginContractItemInfo> infoMap = new HashMap<>();
            for (PluginContractInstanceItemDto itemDto : contractInstanceDto.getItems()) {
                BasePluginContractItemDef.Builder itemDefBuilder = BasePluginContractItemDef.builder(itemDto.getName()).withSchema(
                        itemDto.getConfSchema());
                if (itemDto.getInMessage() != null) {
                    itemDefBuilder = itemDefBuilder.withInMessage(itemDto.getInMessage().getFqn(), itemDto.getInMessage().getVersion());
                }
                if (itemDto.getOutMessage() != null) {
                    itemDefBuilder = itemDefBuilder.withInMessage(itemDto.getOutMessage().getFqn(), itemDto.getOutMessage().getVersion());
                }
                BasePluginContractItemDef itemDef = itemDefBuilder.build();
                BasePluginContractItemInfo.Builder itemInfoBuilder = BasePluginContractItemInfo.builder();
                itemInfoBuilder.withData(itemDto.getConfData());
                if (itemDto.getInMessageSchema() != null) {
                    // TODO: convert to plain body with all dependencies inline;
                    itemInfoBuilder.withInMsgSchema(itemDto.getInMessageSchema().getBody());
                }
                if (itemDto.getOutMessageSchema() != null) {
                    // TODO: convert to plain body with all dependencies inline;
                    itemInfoBuilder.withOutMsgSchema(itemDto.getOutMessageSchema().getBody());
                }
                infoMap.put(itemDef, itemInfoBuilder.build());
                builder.withItem(itemDef);
            }

            BasePluginContractInstance contractInstance = new BasePluginContractInstance(builder.build());
            for (Entry<PluginContractItemDef, PluginContractItemInfo> entry : infoMap.entrySet()) {
                contractInstance.addContractItemInfo(entry.getKey(), entry.getValue());
            }
            contracts.add(contractInstance);
        }
    }

    @Override
    public String getPluginConfigurationData() {
        return configurationData;
    }

    @Override
    public Set<PluginContractInstance> getPluginContracts() {
        return contracts;
    }

}
