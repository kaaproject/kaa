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

package org.kaaproject.kaa.server.admin.shared.plugin;

import java.io.Serializable;

import org.kaaproject.avro.ui.shared.RecordField;

public class PluginInfoDto implements Serializable {
    
    private static final long serialVersionUID = 7749261853080648846L;
    
    private String pluginTypeName;
    private RecordField fieldConfiguration;
    private String pluginClassName;
    
    public PluginInfoDto() {
        super();
    }

    public PluginInfoDto(String pluginTypeName,
            RecordField fieldConfiguration, String pluginClassName) {
        super();
        this.pluginTypeName = pluginTypeName;
        this.fieldConfiguration = fieldConfiguration;
        this.pluginClassName = pluginClassName;
    }

    public String getPluginTypeName() {
        return pluginTypeName;
    }

    public void setPluginTypeName(String pluginTypeName) {
        this.pluginTypeName = pluginTypeName;
    }

    public RecordField getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setFieldConfiguration(RecordField fieldConfiguration) {
        this.fieldConfiguration = fieldConfiguration;
    }

    public String getPluginClassName() {
        return pluginClassName;
    }

    public void setPluginClassName(String pluginClassName) {
        this.pluginClassName = pluginClassName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((fieldConfiguration == null) ? 0 : fieldConfiguration
                        .hashCode());
        result = prime * result
                + ((pluginClassName == null) ? 0 : pluginClassName.hashCode());
        result = prime * result
                + ((pluginTypeName == null) ? 0 : pluginTypeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PluginInfoDto other = (PluginInfoDto) obj;
        if (fieldConfiguration == null) {
            if (other.fieldConfiguration != null) {
                return false;
            }
        } else if (!fieldConfiguration.equals(other.fieldConfiguration)) {
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
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginInfoDto [pluginTypeName=");
        builder.append(pluginTypeName);
        builder.append(", fieldConfiguration=");
        builder.append(fieldConfiguration);
        builder.append(", pluginClassName=");
        builder.append(pluginClassName);
        builder.append("]");
        return builder.toString();
    }

}
