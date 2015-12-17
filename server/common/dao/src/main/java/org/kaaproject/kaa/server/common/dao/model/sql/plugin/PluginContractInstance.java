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

import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_INSTANCE_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_TABLE_NAME;

@Entity
@Table(name = PLUGIN_CONTRACT_INSTANCE_TABLE_NAME)
public final class PluginContractInstance extends GenericModel implements Serializable {

    private static final long serialVersionUID = 2161008833999163889L;

    @ManyToOne
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_PLUGIN_INSTANCE_ID)
    private PluginInstance pluginInstance;

    @ManyToOne
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_ID)
    private PluginContract pluginContract;

    // TODO: change
    @Transient
    private Set<PluginContractInstanceItem> pluginContractInstanceItems;

    public PluginInstance getPluginInstance() {
        return pluginInstance;
    }

    public void setPluginInstance(PluginInstance pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    public PluginContract getPluginContract() {
        return pluginContract;
    }

    public void setPluginContract(PluginContract pluginContract) {
        this.pluginContract = pluginContract;
    }

    public Set<PluginContractInstanceItem> getPluginContractInstanceItems() {
        return pluginContractInstanceItems;
    }

    public void setPluginContractInstanceItems(Set<PluginContractInstanceItem> pluginContractInstanceItems) {
        this.pluginContractInstanceItems = pluginContractInstanceItems;
    }

    public PluginContractInstance() {
    }

    public PluginContractInstance(PluginContractInstanceDto dto) {
    }

    @Override
    protected PluginContractInstanceDto createDto() {
        return new PluginContractInstanceDto();
    }

    @Override
    public PluginContractInstanceDto toDto() {
        PluginContractInstanceDto dto = createDto();
        dto.setId(getStringId());
        dto.setInstance(pluginInstance != null ? pluginInstance.toDto() : null);
        dto.setContract(pluginContract != null ? pluginContract.toDto() : null);
        // TODO: populate items
        return new PluginContractInstanceDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginContractInstance)) {
            return false;
        }

        PluginContractInstance that = (PluginContractInstance) o;

        if (pluginContract != null ? !pluginContract.equals(that.pluginContract) : that.pluginContract != null) {
            return false;
        }
        if (pluginInstance != null ? !pluginInstance.equals(that.pluginInstance) : that.pluginInstance != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pluginInstance != null ? pluginInstance.hashCode() : 0;
        result = 31 * result + (pluginContract != null ? pluginContract.hashCode() : 0);
        return result;
    }
}
