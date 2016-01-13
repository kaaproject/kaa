/*
 * Copyright 2014-2016 CyberVision, Inc.
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
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_INSTANCE_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_INSTANCE_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_TABLE_NAME;

@Entity
@Table(name = PLUGIN_CONTRACT_INSTANCE_TABLE_NAME)
public class PluginContractInstance extends GenericModel<PluginContractInstanceDto> implements Serializable {

    private static final long serialVersionUID = -6714384962255683537L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_ID, nullable = false,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_FK))
    private PluginContract pluginContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_PLUGIN_INSTANCE_ID, nullable = false,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_PLUGIN_INSTANCE_FK))
    private PluginInstance pluginInstance;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_ID,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_FK))
    private Set<PluginContractInstanceItem> pluginContractInstanceItems = new HashSet<>();

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

    public PluginContractInstance(String id) {
        this.id = ModelUtils.getLongId(id);
    }

    public PluginContractInstance(PluginContractInstanceDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        if (dto.getContract() != null) {
            this.pluginContract = new PluginContract(dto.getContract());
        }
        if (dto.getInstance() != null) {
            dto.getInstance().setContracts(null);
            this.pluginInstance = new PluginInstance(dto.getInstance());
        }
        Set<PluginContractInstanceItemDto> instanceItemDtos = dto.getItems();
        if (instanceItemDtos != null && !instanceItemDtos.isEmpty()) {
            for (PluginContractInstanceItemDto instanceItemDto : instanceItemDtos) {
                pluginContractInstanceItems.add(new PluginContractInstanceItem(instanceItemDto));
            }
        }
    }

    public PluginInstance getPluginInstance() {
        return pluginInstance;
    }

    public void setPluginInstance(PluginInstance pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @Override
    protected PluginContractInstanceDto createDto() {
        return new PluginContractInstanceDto();
    }

    @Override
    protected PluginContractInstance newInstance(Long id) {
        PluginContractInstanceDto pluginContractInstanceDto = new PluginContractInstanceDto();
        pluginContractInstanceDto.setId(ModelUtils.getStringId(id));
        return new PluginContractInstance(pluginContractInstanceDto);
    }

    @Override
    public PluginContractInstanceDto toDto() {
        PluginContractInstanceDto dto = toDtoNoContract();
        dto.setContract(ModelUtils.getDto(pluginContract));

        PluginInstanceDto pluginInstanceDto = new PluginInstanceDto();
        pluginInstanceDto.setId(getStringId());
        pluginInstanceDto.setName(pluginInstance.getName());
        pluginInstanceDto.setState(pluginInstance.getState());
        pluginInstanceDto.setConfigurationData(pluginInstance.getConfigData());
        pluginInstanceDto.setPluginDefinition(pluginInstance.getPlugin() != null ? pluginInstance.getPlugin().toDtoNoPluginInstances() : null);

        dto.setInstance(pluginInstanceDto);
        return dto;
    }

    PluginContractInstanceDto toDtoNoContract() {
        PluginContractInstanceDto dto = createDto();
        dto.setId(getStringId());
        Set<PluginContractInstanceItemDto> instanceItemDtos = new HashSet<>();
        for (PluginContractInstanceItem pluginContractInstanceItem : pluginContractInstanceItems) {
            instanceItemDtos.add(pluginContractInstanceItem.toDto());
        }
        dto.setItems(instanceItemDtos);
        return dto;
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
        int result = pluginContract != null ? pluginContract.hashCode() : 0;
        result = 31 * result + (pluginInstance != null ? pluginInstance.hashCode() : 0);
        return result;
    }
}
