/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

public class PluginContractInstanceItemDto implements HasId, Serializable {

    private static final long serialVersionUID = 5369616154739139086L;

    private String id;
    private String confData;
    private PluginContractItemDto pluginContractItem;
    private PluginContractItemDto parentPluginContractItem;
    private CTLSchemaDto inMessageSchema;
    private CTLSchemaDto outMessageSchema;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getConfData() {
        return confData;
    }

    public void setConfData(String confData) {
        this.confData = confData;
    }

    public PluginContractItemDto getPluginContractItem() {
        return pluginContractItem;
    }

    public void setPluginContractItem(PluginContractItemDto pluginContractItem) {
        this.pluginContractItem = pluginContractItem;
    }

    public PluginContractItemDto getParentPluginContractItem() {
        return parentPluginContractItem;
    }

    public void setParentPluginContractItem(PluginContractItemDto parentPluginContractItem) {
        this.parentPluginContractItem = parentPluginContractItem;
    }

    public CTLSchemaDto getInMessageSchema() {
        return inMessageSchema;
    }

    public void setInMessageSchema(CTLSchemaDto inMessageSchema) {
        this.inMessageSchema = inMessageSchema;
    }

    public CTLSchemaDto getOutMessageSchema() {
        return outMessageSchema;
    }

    public void setOutMessageSchema(CTLSchemaDto outMessageSchema) {
        this.outMessageSchema = outMessageSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginContractInstanceItemDto)) {
            return false;
        }

        PluginContractInstanceItemDto that = (PluginContractInstanceItemDto) o;

        if (confData != null ? !confData.equals(that.confData) : that.confData != null) {
            return false;
        }
        if (inMessageSchema != null ? !inMessageSchema.equals(that.inMessageSchema) : that.inMessageSchema != null) {
            return false;
        }
        if (outMessageSchema != null ? !outMessageSchema.equals(that.outMessageSchema) : that.outMessageSchema != null) {
            return false;
        }
        if (parentPluginContractItem != null ? !parentPluginContractItem.equals(that.parentPluginContractItem) : that.parentPluginContractItem != null) {
            return false;
        }
        if (pluginContractItem != null ? !pluginContractItem.equals(that.pluginContractItem) : that.pluginContractItem != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = confData != null ? confData.hashCode() : 0;
        result = 31 * result + (pluginContractItem != null ? pluginContractItem.hashCode() : 0);
        result = 31 * result + (parentPluginContractItem != null ? parentPluginContractItem.hashCode() : 0);
        result = 31 * result + (inMessageSchema != null ? inMessageSchema.hashCode() : 0);
        result = 31 * result + (outMessageSchema != null ? outMessageSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginContractInstanceItemDto{");
        sb.append("id='").append(id).append('\'');
        sb.append(", confData='").append(confData).append('\'');
        sb.append(", pluginContractItem=").append(pluginContractItem);
        sb.append(", parentPluginContractItem=").append(parentPluginContractItem);
        sb.append(", inMessageSchema=").append(inMessageSchema);
        sb.append(", outMessageSchema=").append(outMessageSchema);
        sb.append('}');
        return sb.toString();
    }
}
