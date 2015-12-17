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
import org.kaaproject.kaa.common.dto.plugin.ContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.ContractType;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_TYPE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_VERSION;

@Entity
@Table(name = CONTRACT_TABLE_NAME)
public final class Contract extends GenericModel<ContractDto> implements Serializable {

    private static final long serialVersionUID = 4244237138705713321L;

    @Column(name = CONTRACT_NAME)
    private String name;

    @Column(name = CONTRACT_VERSION)
    private Integer version;

    @Column(name = CONTRACT_TYPE)
    @Enumerated(value = EnumType.STRING)
    private ContractType type;

    @Column
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contract")
    private Set<ContractItem> contractItems = new HashSet<>();

    @Column
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contract")
    private Set<PluginContract> pluginContracts = new HashSet<>();

    public Contract() {
    }

    public Contract(ContractDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.name = dto.getName();
        this.version = dto.getVersion();
        this.type = dto.getType();
        if (dto.getContractItems() != null) {
            for (ContractItemDto contractItem : dto.getContractItems()) {
                contractItems.add(new ContractItem(contractItem));
            }
        }
        if (dto.getPluginContracts() != null) {
            for (PluginContractDto pluginContract : dto.getPluginContracts()) {
                pluginContracts.add(new PluginContract(pluginContract));
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ContractType getType() {
        return type;
    }

    public void setType(ContractType type) {
        this.type = type;
    }

    public Set<ContractItem> getContractItems() {
        return contractItems;
    }

    public void setContractItems(Set<ContractItem> contractItems) {
        this.contractItems = contractItems;
    }

    public Set<PluginContract> getPluginContracts() {
        return pluginContracts;
    }

    public void setPluginContracts(Set<PluginContract> pluginContracts) {
        this.pluginContracts = pluginContracts;
    }

    @Override
    protected ContractDto createDto() {
        return new ContractDto();
    }

    @Override
    public ContractDto toDto() {
        ContractDto dto = createDto();
        dto.setId(getStringId());
        dto.setName(name);
        dto.setVersion(version);
        dto.setType(type);
        Set<ContractItemDto> contractItemDtos = new HashSet<>();
        for (ContractItem contractItem : contractItems) {
            contractItemDtos.add(contractItem.toDto());
        }
        dto.setContractItems(contractItemDtos);

        Set<PluginContractDto> pluginContractDtos = new HashSet<>();
        for (PluginContract pluginContract : pluginContracts) {
            pluginContractDtos.add(pluginContract.toDto());
        }
        dto.setPluginContracts(pluginContractDtos);
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Contract)) {
            return false;
        }

        Contract contract = (Contract) o;

        if (name != null ? !name.equals(contract.name) : contract.name != null) {
            return false;
        }
        if (type != contract.type) {
            return false;
        }
        if (version != null ? !version.equals(contract.version) : contract.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Contract{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
