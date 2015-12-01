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
import org.kaaproject.kaa.common.dto.plugin.ContractType;
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
import java.util.Set;

@Entity
@Table(name = "contract")
public class Contract extends GenericModel<ContractDto> implements Serializable {

    @Column
    private String name;
    @Column
    private Integer version;
    @Column
    @Enumerated(value = EnumType.STRING)
    private ContractType type;
    @Column
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contract")
    private Set<ContractItem> contractItems;
    @Column
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contract")
    private Set<PluginContract> pluginContracts;

    public Contract(ContractDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.name = dto.getName();
        this.version = dto.getVersion();
        this.type = dto.getType();
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
        return dto;
    }
}
