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

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "plugin_contract_item")
public class PluginContractItem extends GenericModel implements Serializable {

    private String configSchema;
    private ContractItem contractItem;

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

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
