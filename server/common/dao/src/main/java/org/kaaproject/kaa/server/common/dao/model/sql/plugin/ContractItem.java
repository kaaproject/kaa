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
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_CONSTRAINT_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_IN_MESSAGE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_IN_MESSAGE_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_OUT_MESSAGE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_OUT_MESSAGE_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_ITEM_TABLE_NAME;

@Entity
@Table(name = CONTRACT_ITEM_TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {CONTRACT_ITEM_NAME, CONTRACT_ITEM_CONTRACT_ID,
                CONTRACT_ITEM_IN_MESSAGE, CONTRACT_ITEM_OUT_MESSAGE}, name = CONTRACT_ITEM_CONSTRAINT_NAME)})
public class ContractItem extends GenericModel<ContractItemDto> implements Serializable {

    private static final long serialVersionUID = 2062055405562778911L;

    @Column(name = CONTRACT_ITEM_NAME, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CONTRACT_ITEM_IN_MESSAGE, foreignKey = @ForeignKey(name = CONTRACT_ITEM_IN_MESSAGE_FK))
    private ContractMessage inMessage;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CONTRACT_ITEM_OUT_MESSAGE, foreignKey = @ForeignKey(name = CONTRACT_ITEM_OUT_MESSAGE_FK))
    private ContractMessage outMessage;

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

    public ContractItem(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    protected ContractItemDto createDto() {
        return new ContractItemDto();
    }

    @Override
    protected ContractItem newInstance(Long id) {
        return new ContractItem(id);
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
        result = 31 * result + (inMessage != null ? inMessage.hashCode() : 0);
        result = 31 * result + (outMessage != null ? outMessage.hashCode() : 0);
        return result;
    }
}
