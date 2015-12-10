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
package org.kaaproject.kaa.common.dto.plugin;

import org.kaaproject.kaa.common.dto.HasId;

import java.io.Serializable;
import java.util.Set;

public class PluginContractDto implements HasId, Serializable {

    private static final long serialVersionUID = -8204932871903228546L;

    private String id;
    private PluginContractDirection direction;
    private ContractDto contract;
    private Set<PluginContractItemDto> pluginContractItems;
    private Set<PluginContractInstanceDto> pluginContractInstances;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public PluginContractDirection getDirection() {
        return direction;
    }

    public void setDirection(PluginContractDirection direction) {
        this.direction = direction;
    }

    public ContractDto getContract() {
        return contract;
    }

    public void setContract(ContractDto contract) {
        this.contract = contract;
    }

    public Set<PluginContractItemDto> getPluginContractItems() {
        return pluginContractItems;
    }

    public void setPluginContractItems(Set<PluginContractItemDto> pluginContractItems) {
        this.pluginContractItems = pluginContractItems;
    }

    public Set<PluginContractInstanceDto> getPluginContractInstances() {
        return pluginContractInstances;
    }

    public void setPluginContractInstances(Set<PluginContractInstanceDto> pluginContractInstances) {
        this.pluginContractInstances = pluginContractInstances;
    }
}
