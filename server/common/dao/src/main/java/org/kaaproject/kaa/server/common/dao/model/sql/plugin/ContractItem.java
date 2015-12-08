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
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "contract_item")
public class ContractItem extends GenericModel<ContractItemDto> implements Serializable {

    private String name;
    private Contract contract;
    @ManyToOne
    private ContractMessage inMessage;
    @ManyToOne
    private ContractMessage outMessage;
    @OneToMany
    private Set<PluginContractItem> pluginContractItems;

    @Override
    protected ContractItemDto createDto() {
        return new ContractItemDto();
    }

    @Override
    public ContractItemDto toDto() {
        ContractItemDto dto = createDto();
        return dto;
    }
}
