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

import org.kaaproject.kaa.common.dto.plugin.ContractDto;
import org.kaaproject.kaa.common.dto.plugin.ContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.ContractMessageDto;
import org.kaaproject.kaa.common.dto.plugin.ContractType;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;

import java.util.HashSet;
import java.util.Set;

public class PluginTestFactory {

    public static final String NAME = "Plugin name";
    public static final String CLASS_NAME = "ClassName";
    public static final Integer VERSION = 1;

    public static PluginDto create() {
        PluginDto pluginDto = new PluginDto();
        pluginDto.setClassName(CLASS_NAME);
        pluginDto.setName(NAME);
        pluginDto.setVersion(VERSION);
        pluginDto.setScope(PluginScope.ENDPOINT);
        pluginDto.setConfSchema("{Schema}");
        pluginDto.setPluginContracts(generatePluginContracts());
        return pluginDto;
    }

    private static Set<PluginContractDto> generatePluginContracts() {
        Set<PluginContractDto> contracts = new HashSet<>();
        PluginContractDto pluginContractDto = new PluginContractDto();
        pluginContractDto.setPluginContractItems(generatePluginContractItems());
        pluginContractDto.setContract(generateContract());
        pluginContractDto.setDirection(PluginContractDirection.IN);
        contracts.add(pluginContractDto);
        return contracts;
    }

    private static Set<PluginContractItemDto> generatePluginContractItems() {
        Set<PluginContractItemDto> pluginContracts = new HashSet<>();
        PluginContractItemDto pluginContractItemDto = new PluginContractItemDto();
        pluginContractItemDto.setConfigSchema("{ConfigurationSchema}");
        pluginContractItemDto.setContractItem(generateContractItem());
        pluginContracts.add(pluginContractItemDto);
        return pluginContracts;
    }

    private static ContractItemDto generateContractItem() {
        ContractItemDto contractItemDto = new ContractItemDto();
        contractItemDto.setName("contract item name");
        ContractMessageDto inMessage = new ContractMessageDto();
        inMessage.setVersion(1);
        inMessage.setFqn("a.b.c");
        ContractMessageDto outMessage = new ContractMessageDto();
        outMessage.setVersion(2);
        outMessage.setFqn("c.d.e");
        contractItemDto.setInMessage(inMessage);
        contractItemDto.setOutMessage(outMessage);
        return contractItemDto;
    }

    private static ContractDto generateContract() {
        ContractDto contractDto = new ContractDto();
        contractDto.setVersion(1);
        contractDto.setName("name");
        contractDto.setType(ContractType.ROUTE);
        return contractDto;
    }
}
