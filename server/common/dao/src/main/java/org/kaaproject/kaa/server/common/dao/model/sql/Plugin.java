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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CLASS_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_RAW_CONFIGURATION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_TYPE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = PLUGIN_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Plugin<T extends PluginDto> extends GenericModel<T> implements Serializable {

    private static final long serialVersionUID = 7054619253518648721L; 

    @Column(name = PLUGIN_NAME)
    private String name;

    @Column(name = PLUGIN_DESCRIPTION, length = 1000)
    private String description;

    @Column(name = PLUGIN_CREATED_USERNAME)
    private String createdUsername;

    @Column(name = PLUGIN_CREATED_TIME)
    private long createdTime;

    @ManyToOne
    @JoinColumn(name = PLUGIN_APPLICATION_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    protected Application application;

    @Column(name = PLUGIN_TYPE_NAME)
    private String pluginTypeName;

    @Column(name = PLUGIN_CLASS_NAME)
    private String pluginClassName;

    @Lob
    @Column(name = PLUGIN_RAW_CONFIGURATION)
    private byte[] rawConfiguration;
    
    public Plugin() {
    }

    public Plugin(Long id) {
        this.id = id;
    }

    public Plugin(PluginDto dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.name = dto.getName();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            this.pluginTypeName = dto.getPluginTypeName();
            this.pluginClassName = dto.getPluginClassName();
            this.rawConfiguration = dto.getRawConfiguration();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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

    @Override
    public T toDto() {
        T dto = createDto();
        dto.setId(getStringId());
        dto.setName(name);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        dto.setPluginTypeName(pluginTypeName);
        dto.setPluginClassName(pluginClassName);
        dto.setRawConfiguration(rawConfiguration);
        return dto;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((application == null) ? 0 : application.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((pluginClassName == null) ? 0 : pluginClassName.hashCode());
        result = prime * result
                + ((pluginTypeName == null) ? 0 : pluginTypeName.hashCode());
        result = prime * result + Arrays.hashCode(rawConfiguration);
        return result;
    }

    @SuppressWarnings("unchecked")
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
        Plugin<T> other = (Plugin<T>) obj;
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
        if (createdTime != other.createdTime) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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
        builder.append("Plugin [name=");
        builder.append(name);
        builder.append(", description=");
        builder.append(description);
        builder.append(", createdUsername=");
        builder.append(createdUsername);
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", application=");
        builder.append(application);
        builder.append(", pluginTypeName=");
        builder.append(pluginTypeName);
        builder.append(", pluginClassName=");
        builder.append(pluginClassName);
        builder.append(", rawConfiguration=");
        builder.append(Arrays.toString(rawConfiguration));
        builder.append("]");
        return builder.toString();
    }

}
