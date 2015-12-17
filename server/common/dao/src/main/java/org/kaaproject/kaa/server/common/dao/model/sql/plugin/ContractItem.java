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

import org.kaaproject.kaa.common.dto.plugin.ContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.ContractMessageDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_IN_MESSAGE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_OUT_MESSAGE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_TABLE_NAME;

@Entity
@Table(name = CONTRACT_ITEM_TABLE_NAME)
public final class ContractItem extends GenericModel<ContractItemDto> implements Serializable {

    private static final long serialVersionUID = 5710163046228962807L;

    @Column(name = CONTRACT_ITEM_NAME)
    private String name;

    @ManyToOne
    @JoinColumn(name = CONTRACT_ITEM_CONTRACT_ID)
    private Contract contract;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = CONTRACT_ITEM_IN_MESSAGE)
    private ContractMessage inMessage;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = CONTRACT_ITEM_OUT_MESSAGE)
    private ContractMessage outMessage;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contractItem")
    private Set<PluginContractItem> pluginContractItems = new HashSet<>();

    public ContractItem() {
    }

    public ContractItem(ContractItemDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.name = dto.getName();
        ContractMessageDto in = dto.getInMessage();
        if (in != null) {
            this.inMessage = new ContractMessage(in);
        }
        ContractMessageDto out = dto.getOutMessage();
        if (out != null) {
            this.outMessage = new ContractMessage(out);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ContractMessage getInMessage() {
        return inMessage;
    }

    public void setInMessage(ContractMessage inMessage) {
        this.inMessage = inMessage;
    }

    public ContractMessage getOutMessage() {
        return outMessage;
    }

    public void setOutMessage(ContractMessage outMessage) {
        this.outMessage = outMessage;
    }

    public Set<PluginContractItem> getPluginContractItems() {
        return pluginContractItems;
    }

    public void setPluginContractItems(Set<PluginContractItem> pluginContractItems) {
        this.pluginContractItems = pluginContractItems;
    }

    @Override
    protected ContractItemDto createDto() {
        return new ContractItemDto();
    }

    @Override
    public ContractItemDto toDto() {
        ContractItemDto dto = createDto();
        dto.setId(getStringId());
        dto.setName(name);
        dto.setInMessage(inMessage != null ? inMessage.toDto() : null);
        dto.setOutMessage(outMessage != null ? outMessage.toDto() : null);
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractItem)) {
            return false;
        }

        ContractItem that = (ContractItem) o;

        if (contract != null ? !contract.equals(that.contract) : that.contract != null) {
            return false;
        }
        if (inMessage != null ? !inMessage.equals(that.inMessage) : that.inMessage != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (outMessage != null ? !outMessage.equals(that.outMessage) : that.outMessage != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (contract != null ? contract.hashCode() : 0);
        result = 31 * result + (inMessage != null ? inMessage.hashCode() : 0);
        result = 31 * result + (outMessage != null ? outMessage.hashCode() : 0);
        return result;
    }
}
