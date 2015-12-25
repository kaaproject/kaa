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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginContractDto)) {
            return false;
        }

        PluginContractDto that = (PluginContractDto) o;

        if (contract != null ? !contract.equals(that.contract) : that.contract != null) {
            return false;
        }
        if (direction != that.direction) {
            return false;
        }
        if (pluginContractInstances != null ? !pluginContractInstances.equals(that.pluginContractInstances) : that.pluginContractInstances != null) {
            return false;
        }
        if (pluginContractItems != null ? !pluginContractItems.equals(that.pluginContractItems) : that.pluginContractItems != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = direction != null ? direction.hashCode() : 0;
        result = 31 * result + (contract != null ? contract.hashCode() : 0);
        result = 31 * result + (pluginContractItems != null ? pluginContractItems.hashCode() : 0);
        result = 31 * result + (pluginContractInstances != null ? pluginContractInstances.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginContractDto{");
        sb.append("id='").append(id).append('\'');
        sb.append(", direction=").append(direction);
        sb.append(", contract=").append(contract);
        sb.append(", pluginContractItems=").append(pluginContractItems);
        sb.append(", pluginContractInstances=").append(pluginContractInstances);
        sb.append('}');
        return sb.toString();
    }
}
