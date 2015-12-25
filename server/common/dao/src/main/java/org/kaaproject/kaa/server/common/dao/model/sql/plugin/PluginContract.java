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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_CONTRACT_ID_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_DIRECTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_PLUGIN_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_ITEM_PLUGIN_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_PLUGIN_CONTRACT_ITEM_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_PLUGIN_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_PLUGIN_ID_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_TABLE_NAME;

@Entity
@Table(name = PLUGIN_CONTRACT_TABLE_NAME)
public class PluginContract extends GenericModel implements Serializable {

    private static final long serialVersionUID = 3561690611845570639L;

    @Enumerated(EnumType.STRING)
    @Column(name = PLUGIN_CONTRACT_DIRECTION, nullable = false)
    private PluginContractDirection direction;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = PLUGIN_CONTRACT_CONTRACT_ID, foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_CONTRACT_ID_FK), nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_PLUGIN_ID, foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_PLUGIN_ID_FK))
    private Plugin plugin;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = PLUGIN_CONTRACT_ITEM_PLUGIN_CONTRACT_ID, foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_PLUGIN_CONTRACT_ITEM_FK), nullable = false)
    private Set<PluginContractItem> pluginContractItems = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = PLUGIN_CONTRACT_PROPERTY)
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

    public PluginContract(Long id) {
        this.id = id;
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
    protected PluginContract newInstance(Long id) {
        return new PluginContract(id);
    }

    @Override
    public PluginContractDto toDto() {
        PluginContractDto dto = createDto();
        dto.setId(getStringId());
        dto.setDirection(direction);
        dto.setContract(contract != null ? contract.toDto() : null);
        Set<PluginContractItemDto> pluginContractItemDtos = new HashSet<>();

        if (!pluginContractItems.isEmpty()) {
            for (PluginContractItem pluginContractItem : pluginContractItems) {
                pluginContractItemDtos.add(pluginContractItem.toDto());
            }
            dto.setPluginContractItems(pluginContractItemDtos);
        }

        if (!pluginContractInstances.isEmpty()) {
            Set<PluginContractInstanceDto> pluginContractInstanceDtos = new HashSet<>();
            for (PluginContractInstance pluginContractInstance : pluginContractInstances) {
                pluginContractInstanceDtos.add(pluginContractInstance.toDto());
            }
            dto.setPluginContractInstances(pluginContractInstanceDtos);
        }
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginContract)) {
            return false;
        }

        PluginContract that = (PluginContract) o;

        if (contract != null ? !contract.equals(that.contract) : that.contract != null) {
            return false;
        }
        if (direction != that.direction) {
            return false;
        }
        if (plugin != null ? !plugin.equals(that.plugin) : that.plugin != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = direction != null ? direction.hashCode() : 0;
        result = 31 * result + (contract != null ? contract.hashCode() : 0);
        result = 31 * result + (plugin != null ? plugin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginContract{");
        sb.append("direction=").append(direction);
        sb.append(", contract=").append(contract);
        sb.append(", plugin=").append(plugin);
        sb.append('}');
        return sb.toString();
    }
}
