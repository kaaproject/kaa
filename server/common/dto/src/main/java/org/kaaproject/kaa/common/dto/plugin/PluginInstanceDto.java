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
import java.util.Arrays;
import java.util.Set;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginInstanceDto implements HasId, Serializable {

    private static final long serialVersionUID = -4079418894836720242L;

    private String id;
    private String name;
    private PluginInstanceState state;
    private String configurationData;
    private PluginDto pluginDefinition;
    private Set<PluginContractInstanceDto> contracts;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PluginInstanceState getState() {
        return state;
    }

    public void setState(PluginInstanceState state) {
        this.state = state;
    }

    public String getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(String configurationData) {
        this.configurationData = configurationData;
    }

    public PluginDto getPluginDefinition() {
        return pluginDefinition;
    }

    public void setPluginDefinition(PluginDto pluginDefinition) {
        this.pluginDefinition = pluginDefinition;
    }

    public Set<PluginContractInstanceDto> getContracts() {
        return contracts;
    }

    public void setContracts(Set<PluginContractInstanceDto> contracts) {
        this.contracts = contracts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginInstanceDto)) {
            return false;
        }

        PluginInstanceDto that = (PluginInstanceDto) o;

        if (configurationData != null ? !configurationData.equals(that.configurationData) : that.configurationData != null) {
            return false;
        }
        if (contracts != null ? !contracts.equals(that.contracts) : that.contracts != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (pluginDefinition != null ? !pluginDefinition.equals(that.pluginDefinition) : that.pluginDefinition != null) {
            return false;
        }
        if (state != that.state) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (configurationData != null ? configurationData.hashCode() : 0);
        result = 31 * result + (pluginDefinition != null ? pluginDefinition.hashCode() : 0);
        result = 31 * result + (contracts != null ? contracts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginInstanceDto{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", state=").append(state);
        sb.append(", configurationData=").append(configurationData);
        sb.append(", pluginDefinition=").append((pluginDefinition != null && pluginDefinition.getClassName() != null)
                ? pluginDefinition.getClassName() : null);
        sb.append(", contracts=").append(contracts);
        sb.append('}');
        return sb.toString();
    }
}
