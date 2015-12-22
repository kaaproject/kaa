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

import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_CONF_DATA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_PLUGIN_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_STATE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_TABLE_NAME;

// TODO: review, corresponding dto has name field, but the model doesn't have any
@Entity
@Table(name = PLUGIN_INSTANCE_TABLE_NAME)
public class PluginInstance extends GenericModel<PluginInstanceDto> implements Serializable {

    private static final long serialVersionUID = 1508341006838633974L;

    @Lob
    @Column(name = PLUGIN_INSTANCE_CONF_DATA)
    private String configData;              // TODO: mb change to byte[] as in the corresponding dto?

    @Column(name = PLUGIN_INSTANCE_STATE)
    @Enumerated(EnumType.STRING)
    private PluginInstanceState state;

    @ManyToOne
    @JoinColumn(name = PLUGIN_INSTANCE_PLUGIN_ID, foreignKey = @ForeignKey(name = "fk_plugin_instance_plugin"))
    private Plugin plugin;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = PLUGIN_INSTANCE_PROPERTY)
    private Set<PluginContractInstance> pluginContractInstances = new HashSet<>();

    public PluginInstance() {
    }

    public PluginInstance(PluginInstanceDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.configData = new String(dto.getConfigurationData());
        this.state = dto.getState();
        PluginDto pluginDto = dto.getPluginDefinition();
        if (pluginDto != null) {
            this.plugin = new Plugin();
            plugin.setId(ModelUtils.getLongId(pluginDto));
            plugin.setClassName(pluginDto.getClassName());
            plugin.setVersion(pluginDto.getVersion());
            plugin.setConfigSchema(pluginDto.getConfSchema());
            plugin.setScope(pluginDto.getScope());
            Set<PluginContractDto> contracts = pluginDto.getPluginContracts();
            if (contracts != null && !contracts.isEmpty()) {
                for (PluginContractDto contract : contracts) {
                    plugin.getPluginContracts().add(new PluginContract(contract));
                }
            }
        }
        Set<PluginContractInstanceDto> contractDtos = dto.getContracts();
        if (contractDtos != null && !contractDtos.isEmpty()) {
            for (PluginContractInstanceDto contractDto : contractDtos) {
                pluginContractInstances.add(new PluginContractInstance(contractDto));
            }
        }
    }

    public PluginInstance(String configData, PluginInstanceState state, Plugin plugin, Set<PluginContractInstance> pluginContractInstances) {
        this.configData = configData;
        this.state = state;
        this.plugin = plugin;
        this.pluginContractInstances = pluginContractInstances;
    }

    @Override
    protected PluginInstanceDto createDto() {
        return new PluginInstanceDto();
    }

    @Override
    public PluginInstanceDto toDto() {
        PluginInstanceDto dto = createDto();
        dto.setId(getStringId());
        dto.setName(plugin != null ? plugin.getName() : null);
        dto.setState(state);
        dto.setConfigurationData(configData.getBytes());
        dto.setPluginDefinition(plugin != null ? plugin.toDtoNoPluginInstances() : null);
        if (!pluginContractInstances.isEmpty()) {
            Set<PluginContractInstanceDto> pluginContractDtos = new HashSet<>();
            for (PluginContractInstance instance : pluginContractInstances) {
                pluginContractDtos.add(instance.toDto());
            }
            dto.setContracts(pluginContractDtos);
        }
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginInstance)) {
            return false;
        }

        PluginInstance that = (PluginInstance) o;

        if (configData != null ? !configData.equals(that.configData) : that.configData != null) {
            return false;
        }
        if (plugin != null ? !plugin.equals(that.plugin) : that.plugin != null) {
            return false;
        }
        if (state != that.state) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = configData != null ? configData.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (plugin != null ? plugin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginInstance{");
        sb.append("configData='").append(configData).append('\'');
        sb.append(", state=").append(state);
        sb.append(", plugin=").append(plugin != null ? plugin.getClassName() : null);
        sb.append(", pluginContractInstances=").append(pluginContractInstances);
        sb.append('}');
        return sb.toString();
    }
}
