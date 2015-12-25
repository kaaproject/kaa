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

import org.kaaproject.kaa.common.dto.plugin.ContractMessageDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_MESSAGE_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_MESSAGE_FQN_VERSION_CONSTRAINT_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_MESSAGE_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONTRACT_MESSAGE_VERSION;

@Entity
@Table(name = CONTRACT_MESSAGE_TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {CONTRACT_MESSAGE_FQN, CONTRACT_MESSAGE_VERSION},
                name = CONTRACT_MESSAGE_FQN_VERSION_CONSTRAINT_NAME)})
public class ContractMessage extends GenericModel<ContractMessageDto> implements Serializable {

    private static final long serialVersionUID = 2122224444729382739L;

    @Column(name = CONTRACT_MESSAGE_FQN, nullable = false)
    private String fqn;

    @Column(name = CONTRACT_MESSAGE_VERSION, nullable = false)
    private Integer version;

    public ContractMessage() {
    }

    public ContractMessage(ContractMessageDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.fqn = dto.getFqn();
        this.version = dto.getVersion();
    }

    public ContractMessage(Long id) {
        this.id = id;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public ContractMessageDto toDto() {
        ContractMessageDto dto = createDto();
        dto.setId(getStringId());
        dto.setFqn(fqn);
        dto.setVersion(version);
        return dto;
    }

    @Override
    protected ContractMessageDto createDto() {
        return new ContractMessageDto();
    }

    @Override
    protected ContractMessage newInstance(Long id) {
        return new ContractMessage(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractMessage)) {
            return false;
        }

        ContractMessage that = (ContractMessage) o;

        if (fqn != null ? !fqn.equals(that.fqn) : that.fqn != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fqn != null ? fqn.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ContractMessage{");
        sb.append("fqn='").append(fqn).append('\'');
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }
}
