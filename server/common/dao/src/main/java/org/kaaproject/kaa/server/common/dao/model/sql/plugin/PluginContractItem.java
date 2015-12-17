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
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_ITEM_CONF_SCHEMA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_ITEM_CONTRACT_ITEM_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_ITEM_PLUGIN_CONTRACT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_ITEM_TABLE_NAME;

@Entity
@Table(name = PLUGIN_CONTRACT_ITEM_TABLE_NAME)
public final class PluginContractItem extends GenericModel<PluginContractItemDto> implements Serializable {

    private static final long serialVersionUID = -7515388614790241386L;

    @Column(name = PLUGIN_CONTRACT_ITEM_CONF_SCHEMA)
    private String configSchema;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = PLUGIN_CONTRACT_ITEM_CONTRACT_ITEM_ID)
    private ContractItem contractItem;

    @ManyToOne
    @JoinColumn(name = PLUGIN_CONTRACT_ITEM_PLUGIN_CONTRACT_ID)
    private PluginContract pluginContract;

    public PluginContractItem() {
    }

    public PluginContractItem(PluginContractItemDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.configSchema = dto.getConfigSchema();
        ContractItemDto contractItemDto = dto.getContractItem();
        if (contractItemDto != null) {
            this.contractItem = new ContractItem(contractItemDto);
        }
    }

    public PluginContractItem(ContractItem contractItem, String configSchema) {
        this.contractItem = contractItem;
        this.configSchema = configSchema;
    }

    @Override
    protected PluginContractItemDto createDto() {
        return new PluginContractItemDto();
    }

    @Override
    public PluginContractItemDto toDto() {
        PluginContractItemDto dto = createDto();
        dto.setId(getStringId());
        dto.setConfigSchema(configSchema);
        dto.setContractItem(contractItem != null ? contractItem.toDto() : null);
        return dto;
    }
}
