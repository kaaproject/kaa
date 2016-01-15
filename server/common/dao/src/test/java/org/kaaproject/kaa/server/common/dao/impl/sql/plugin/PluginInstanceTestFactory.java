/*
 * Copyright 2015-2016 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.common.dto.plugin.ContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory which is used to test cascade save/removal of plugin instances
 */
public class PluginInstanceTestFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PluginInstanceTestFactory.class);

    public static final String NAME = "Endpoint messaging plugin instance";
    public static final String CONF_DATA = "ConfData";
    public static final String SCHEMA_FQN = "some.f.q.n.Name";

    public static PluginInstanceDto create(PluginDto pluginDto) {
        return create(pluginDto, NAME);
    }

    public static PluginInstanceDto create(PluginDto pluginDto, String name) {
        PluginInstanceDto pluginInstanceDto = new PluginInstanceDto();
        pluginInstanceDto.setPluginDefinition(pluginDto);
        pluginInstanceDto.setName(name);
        pluginInstanceDto.setState(PluginInstanceState.ACTIVE);
        pluginInstanceDto.setConfigurationData(CONF_DATA);

        Set<PluginContractInstanceDto> pluginContractInstances = new HashSet<>();
        PluginContractInstanceDto pluginContractInstanceDto = new PluginContractInstanceDto();
        pluginContractInstances.add(pluginContractInstanceDto);
        pluginInstanceDto.setContracts(pluginContractInstances);

        PluginContractDto pluginContract = getPluginContract(pluginDto);
        addItemsToPluginContractInstance(pluginContractInstanceDto, pluginContract);
        pluginContractInstanceDto.setContract(pluginContract);

        return pluginInstanceDto;
    }

    private static PluginContractDto getPluginContract(PluginDto pluginDto) {
        return pluginDto.getPluginContracts().iterator().next();
    }

    private static void addItemsToPluginContractInstance(PluginContractInstanceDto pluginContractInstanceDto,
                                                         PluginContractDto pluginContract) {
        Set<PluginContractInstanceItemDto> items = new HashSet<>();

        ContractDto contract = pluginContract.getContract();
        Set<PluginContractInstanceItemDto> pluginContractInstanceItems = new HashSet<>();

        PluginContractInstanceItemDto item1 = getPluginContractInstanceItem("sendA", null, contract);
        PluginContractInstanceItemDto item2 = getPluginContractInstanceItem("getA", null, contract);
        PluginContractInstanceItemDto item3 = getPluginContractInstanceItem("getB", null, contract);
        pluginContractInstanceItems.addAll(Arrays.asList(item2, item3));
        item1.setPluginContractInstanceItems(pluginContractInstanceItems);

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(getPluginContractInstanceItem("getC", null, contract));
        items.add(getPluginContractInstanceItem("setMethodAListener", null, contract));
        items.add(getPluginContractInstanceItem("setMethodBListener", null, contract));
        items.add(getPluginContractInstanceItem("setMethodCListener", null, contract));

        pluginContractInstanceDto.setItems(items);
    }

    private static PluginContractInstanceItemDto getPluginContractInstanceItem(String methodName,
                                                                               PluginContractItemDto pluginContractItem, ContractDto contract) {
        PluginContractInstanceItemDto instanceItemDto = new PluginContractInstanceItemDto();
        instanceItemDto.setConfData(methodName);
        instanceItemDto.setInMessageSchema(getSchema(contract));
        instanceItemDto.setOutMessageSchema(getSchema(contract));
        instanceItemDto.setPluginContractItem(pluginContractItem);
        return instanceItemDto;
    }

    private static CTLSchemaDto getSchema(ContractDto contract) {
        CTLSchemaInfoDto ctlSchemaInfo = new CTLSchemaInfoDto();
        ctlSchemaInfo.setFqn(SCHEMA_FQN);
        // CTL schema version is taken from the corresponding contract
        ctlSchemaInfo.setVersion(contract.getVersion());
        ctlSchemaInfo.setScope(CTLSchemaScopeDto.SYSTEM);
        CTLSchemaDto schemaDto = new CTLSchemaDto(ctlSchemaInfo, null);
        schemaDto.setBody("Schema body");
        return schemaDto;
    }
}
