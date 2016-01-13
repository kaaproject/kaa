/*
 * Copyright 2015-2016 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceItemDto;
import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_CONF_DATA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_IN_PLUGIN_INSTANCE_CONTRACT_ITEM_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_IN_PLUGIN_INSTANCE_CONTRACT_ITEM_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_OUT_PLUGIN_INSTANCE_CONTRACT_ITEM_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_OUT_PLUGIN_INSTANCE_CONTRACT_ITEM_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_PARAM_MESSAGE_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_RESULT_MESSAGE_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PARAM_MESSAGE_SCHEMA_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PARENT_PLUGIN_CONTRACT_ITEM_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_ITEM_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_ITEM_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_ITEM_PARENT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_RESULT_MESSAGE_SCHEMA_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONTRACT_INSTANCE_ITEM_TABLE_NAME;

@Entity
@Table(name = PLUGIN_CONTRACT_INSTANCE_ITEM_TABLE_NAME)
public class PluginContractInstanceItem extends GenericModel<PluginContractInstanceItemDto> implements Serializable {

    private static final long serialVersionUID = -91824744868705314L;

    @Lob
    @Column(name = PLUGIN_CONTRACT_INSTANCE_ITEM_CONF_DATA)
    private String confData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_ID,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_INSTANCE_FK))
    private PluginContractInstance pluginContractInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_ITEM_ID,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_ITEM_FK))
    private PluginContractItem pluginContractItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PLUGIN_CONTRACT_ITEM_PARENT_ID,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PARENT_PLUGIN_CONTRACT_ITEM_FK))
    private PluginContractItem parentPluginContractItem;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_NAME,
            joinColumns = {@JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_IN_PLUGIN_INSTANCE_CONTRACT_ITEM_ID,
                    foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_IN_PLUGIN_INSTANCE_CONTRACT_ITEM_FK))},
            inverseJoinColumns = {@JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_OUT_PLUGIN_INSTANCE_CONTRACT_ITEM_ID,
                    foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_OUT_PLUGIN_INSTANCE_CONTRACT_ITEM_FK))})
    private Set<PluginContractInstanceItem> pluginContractItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_PARAM_MESSAGE_SCHEMA_ID,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_PARAM_MESSAGE_SCHEMA_FK))
    private CTLSchema inMessageSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PLUGIN_CONTRACT_INSTANCE_ITEM_JOIN_TABLE_RESULT_MESSAGE_SCHEMA_ID,
            foreignKey = @ForeignKey(name = PLUGIN_CONTRACT_INSTANCE_ITEM_RESULT_MESSAGE_SCHEMA_FK))
    private CTLSchema outMessageSchema;

    public PluginContractInstanceItem() {
    }

    public PluginContractInstanceItem(PluginContractInstanceItemDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.confData = dto.getConfData();
        this.pluginContractItem = new PluginContractItem(dto.getPluginContractItem());
        this.parentPluginContractItem = new PluginContractItem(dto.getParentPluginContractItem());
        this.inMessageSchema = new CTLSchema(dto.getInMessageSchema());
        this.outMessageSchema = new CTLSchema(dto.getOutMessageSchema());
    }

    public PluginContractInstanceItem(Long id) {
        this.id = id;
    }

    public String getConfData() {
        return confData;
    }

    public void setConfData(String confData) {
        this.confData = confData;
    }

    public PluginContractInstance getPluginContractInstance() {
        return pluginContractInstance;
    }

    public void setPluginContractInstance(PluginContractInstance pluginContractInstance) {
        this.pluginContractInstance = pluginContractInstance;
    }

    public PluginContractItem getPluginContractItem() {
        return pluginContractItem;
    }

    public void setPluginContractItem(PluginContractItem pluginContractItem) {
        this.pluginContractItem = pluginContractItem;
    }

    public PluginContractItem getParentPluginContractItem() {
        return parentPluginContractItem;
    }

    public void setParentPluginContractItem(PluginContractItem parentPluginContractItem) {
        this.parentPluginContractItem = parentPluginContractItem;
    }

    public Set<PluginContractInstanceItem> getPluginContractItems() {
        return pluginContractItems;
    }

    public void setPluginContractItems(Set<PluginContractInstanceItem> pluginContractItems) {
        this.pluginContractItems = pluginContractItems;
    }

    public CTLSchema getInMessageSchema() {
        return inMessageSchema;
    }

    public void setInMessageSchema(CTLSchema inMessageSchema) {
        this.inMessageSchema = inMessageSchema;
    }

    public CTLSchema getOutMessageSchema() {
        return outMessageSchema;
    }

    public void setOutMessageSchema(CTLSchema outMessageSchema) {
        this.outMessageSchema = outMessageSchema;
    }

    @Override
    protected PluginContractInstanceItemDto createDto() {
        return new PluginContractInstanceItemDto();
    }

    @Override
    protected PluginContractInstanceItem newInstance(Long id) {
        return new PluginContractInstanceItem(id);
    }

    @Override
    public PluginContractInstanceItemDto toDto() {
        PluginContractInstanceItemDto pluginContractInstanceItemDto = createDto();
        pluginContractInstanceItemDto.setId(getStringId());
        pluginContractInstanceItemDto.setConfData(confData);
        pluginContractInstanceItemDto.setPluginContractItem(ModelUtils.getDto(pluginContractItem));
        pluginContractInstanceItemDto.setParentPluginContractItem(ModelUtils.getDto(parentPluginContractItem));
        pluginContractInstanceItemDto.setInMessageSchema(ModelUtils.getDto(inMessageSchema));
        pluginContractInstanceItemDto.setOutMessageSchema(ModelUtils.getDto(outMessageSchema));
        return pluginContractInstanceItemDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginContractInstanceItem)) {
            return false;
        }

        PluginContractInstanceItem that = (PluginContractInstanceItem) o;

        if (inMessageSchema != null ? !inMessageSchema.equals(that.inMessageSchema) : that.inMessageSchema != null) {
            return false;
        }
        if (outMessageSchema != null ? !outMessageSchema.equals(that.outMessageSchema) : that.outMessageSchema != null) {
            return false;
        }
        if (parentPluginContractItem != null ? !parentPluginContractItem.equals(that.parentPluginContractItem) : that.parentPluginContractItem != null) {
            return false;
        }
        if (pluginContractInstance != null ? !pluginContractInstance.equals(that.pluginContractInstance) : that.pluginContractInstance != null) {
            return false;
        }
        if (pluginContractItem != null ? !pluginContractItem.equals(that.pluginContractItem) : that.pluginContractItem != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pluginContractInstance != null ? pluginContractInstance.hashCode() : 0;
        result = 31 * result + (pluginContractItem != null ? pluginContractItem.hashCode() : 0);
        result = 31 * result + (parentPluginContractItem != null ? parentPluginContractItem.hashCode() : 0);
        result = 31 * result + (inMessageSchema != null ? inMessageSchema.hashCode() : 0);
        result = 31 * result + (outMessageSchema != null ? outMessageSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginContractInstanceItem{");
        sb.append("id=").append(id);
        sb.append(", pluginContractInstance=").append(pluginContractInstance);
        sb.append(", pluginContractItem=").append(pluginContractItem);
        sb.append(", parentPluginContractItem=").append(parentPluginContractItem);
        sb.append(", pluginContractItems=").append(pluginContractItems);
        sb.append(", inMessageSchema=").append(inMessageSchema);
        sb.append(", outMessageSchema=").append(outMessageSchema);
        sb.append('}');
        return sb.toString();
    }
}
