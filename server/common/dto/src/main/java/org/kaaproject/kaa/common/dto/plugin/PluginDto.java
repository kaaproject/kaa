/*
 * Copyright 2014-2016 CyberVision, Inc.
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

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.AbstractDetailDto;
import org.kaaproject.kaa.common.dto.HasId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"rawConfiguration","fieldConfiguration"})
public abstract class PluginDto extends AbstractDetailDto implements HasId, Serializable {

    private static final long serialVersionUID = -5156203569187681620L;
    
    private String id;
    private String applicationId;
    private String pluginTypeName;
    private String pluginClassName;
    
    private byte[] rawConfiguration;
    private String jsonConfiguration;
    private RecordField fieldConfiguration;
    
    public PluginDto() {
        super();
    }

    public PluginDto(PluginDto pluginDto) {
        super(pluginDto);
        this.id = pluginDto.getId();
        this.applicationId = pluginDto.getApplicationId();
        this.pluginTypeName = pluginDto.getPluginTypeName();
        this.pluginClassName = pluginDto.getPluginClassName();
        
        this.rawConfiguration = pluginDto.getRawConfiguration();
        this.jsonConfiguration = pluginDto.getJsonConfiguration();
        this.fieldConfiguration = pluginDto.getFieldConfiguration();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getPluginTypeName() {
        return pluginTypeName;
    }

    public void setPluginTypeName(String pluginTypeName) {
        this.pluginTypeName = pluginTypeName;
    }

    public String getPluginClassName() {
        return pluginClassName;
    }

    public void setPluginClassName(String pluginClassName) {
        this.pluginClassName = pluginClassName;
    }

    public byte[] getRawConfiguration() {
        return rawConfiguration;
    }

    public void setRawConfiguration(byte[] rawConfiguration) {
        this.rawConfiguration = rawConfiguration;
    }
    
    public String getJsonConfiguration() {
        return jsonConfiguration;
    }

    public void setJsonConfiguration(String jsonConfiguration) {
        this.jsonConfiguration = jsonConfiguration;
    }
    
    public RecordField getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setFieldConfiguration(RecordField fieldConfiguration) {
        this.fieldConfiguration = fieldConfiguration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime
                * result
                + ((fieldConfiguration == null) ? 0 : fieldConfiguration
                        .hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime
                * result
                + ((jsonConfiguration == null) ? 0 : jsonConfiguration
                        .hashCode());
        result = prime * result
                + ((pluginClassName == null) ? 0 : pluginClassName.hashCode());
        result = prime * result
                + ((pluginTypeName == null) ? 0 : pluginTypeName.hashCode());
        result = prime * result + Arrays.hashCode(rawConfiguration);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PluginDto other = (PluginDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (fieldConfiguration == null) {
            if (other.fieldConfiguration != null) {
                return false;
            }
        } else if (!fieldConfiguration.equals(other.fieldConfiguration)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (jsonConfiguration == null) {
            if (other.jsonConfiguration != null) {
                return false;
            }
        } else if (!jsonConfiguration.equals(other.jsonConfiguration)) {
            return false;
        }
        if (pluginClassName == null) {
            if (other.pluginClassName != null) {
                return false;
            }
        } else if (!pluginClassName.equals(other.pluginClassName)) {
            return false;
        }
        if (pluginTypeName == null) {
            if (other.pluginTypeName != null) {
                return false;
            }
        } else if (!pluginTypeName.equals(other.pluginTypeName)) {
            return false;
        }
        if (!Arrays.equals(rawConfiguration, other.rawConfiguration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginDto [id=");
        builder.append(id);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", pluginTypeName=");
        builder.append(pluginTypeName);
        builder.append(", pluginClassName=");
        builder.append(pluginClassName);
        builder.append(", rawConfiguration=");
        builder.append(Arrays.toString(rawConfiguration));
        builder.append(", jsonConfiguration=");
        builder.append(jsonConfiguration);
        builder.append(", fieldConfiguration=");
        builder.append(fieldConfiguration);
        builder.append(", parent=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
}
