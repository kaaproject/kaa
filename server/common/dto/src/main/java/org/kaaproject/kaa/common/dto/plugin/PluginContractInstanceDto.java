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

import java.io.Serializable;
import java.util.Set;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginContractInstanceDto implements HasId, Serializable {

    private static final long serialVersionUID = -7860833310132734432L;

    private String id;
    private PluginContractDto contract;
    private PluginInstanceDto instance;
    private Set<PluginContractInstanceItemDto> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PluginContractDto getContract() {
        return contract;
    }

    public void setContract(PluginContractDto contract) {
        this.contract = contract;
    }

    public PluginInstanceDto getInstance() {
        return instance;
    }

    public void setInstance(PluginInstanceDto instance) {
        this.instance = instance;
    }

    public Set<PluginContractInstanceItemDto> getItems() {
        return items;
    }

    public void setItems(Set<PluginContractInstanceItemDto> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginContractInstanceDto)) {
            return false;
        }

        PluginContractInstanceDto that = (PluginContractInstanceDto) o;

        if (contract != null ? !contract.equals(that.contract) : that.contract != null) {
            return false;
        }
        if (instance != null ? !instance.equals(that.instance) : that.instance != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = contract != null ? contract.hashCode() : 0;
        result = 31 * result + (instance != null ? instance.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginContractInstanceDto{");
        sb.append("id='").append(id).append('\'');
        sb.append(", contract=").append(contract);
        sb.append(", instance=").append(instance);
        sb.append(", items=").append(items);
        sb.append('}');
        return sb.toString();
    }
}
