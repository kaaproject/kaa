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
package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.common.dto.plugin.ContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plugin_contract")
public class PluginContract extends GenericModel implements Serializable {

    private PluginContractDirection direction;
    private Contract contract;
    private Plugin plugin;
    private Set<PluginContractItem> pluginContractItems = new HashSet<>();
    private Set<PluginContractInstance> pluginContractInstances = new HashSet<>();

    public PluginContract() {
    }

    public PluginContract(PluginContractDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.direction = dto.getDirection();
        ContractDto contractDto = dto.getContract();
        if (contractDto != null) {
            this.contract = new Contract(contractDto);
        }

        Set<PluginContractInstanceDto> instances = dto.getPluginContractInstances();
        if (instances != null && !instances.isEmpty()) {
            for (PluginContractInstanceDto instance : instances) {
                pluginContractInstances.add(new PluginContractInstance(instance));
            }
        }

        Set<PluginContractItemDto> items = dto.getPluginContractItems();
        if (items != null && !items.isEmpty()) {
            for (PluginContractItemDto item : items) {
                pluginContractItems.add(new PluginContractItem(item));
            }
        }
    }

    public PluginContractDirection getDirection() {
        return direction;
    }

    public void setDirection(PluginContractDirection direction) {
        this.direction = direction;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Set<PluginContractItem> getPluginContractItems() {
        return pluginContractItems;
    }

    public void setPluginContractItems(Set<PluginContractItem> pluginContractItems) {
        this.pluginContractItems = pluginContractItems;
    }

    public Set<PluginContractInstance> getPluginContractInstances() {
        return pluginContractInstances;
    }

    public void setPluginContractInstances(Set<PluginContractInstance> pluginContractInstances) {
        this.pluginContractInstances = pluginContractInstances;
    }

    @Override
    protected PluginContractDto createDto() {
        return new PluginContractDto();
    }

    @Override
    public PluginContractDto toDto() {
        return createDto();
    }
}
