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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CLASS_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CLASS_NAME_CONSTRAINT;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_CONF_SCHEMA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_NAME_AND_VERSION_CONSTRAINT_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_SCOPE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_VERSION;

@Entity
@Table(name = PLUGIN_TABLE_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = {PLUGIN_NAME, PLUGIN_VERSION}, name = PLUGIN_NAME_AND_VERSION_CONSTRAINT_NAME),
        @UniqueConstraint(columnNames = {PLUGIN_CLASS_NAME}, name = PLUGIN_CLASS_NAME_CONSTRAINT)})
public class Plugin extends GenericModel<PluginDto> implements Serializable {

    private static final long serialVersionUID = -2739834403973192215L;

    @Column(name = PLUGIN_NAME, nullable = false)
    private String name;

    @Column(name = PLUGIN_CLASS_NAME, nullable = false)
    private String className;

    @Column(name = PLUGIN_VERSION, nullable = false)
    private Integer version;

    @Lob
    @Column(name = PLUGIN_CONF_SCHEMA)
    private String configSchema;

    @Column(name = PLUGIN_SCOPE, nullable = false)
    @Enumerated(EnumType.STRING)
    private PluginScope scope;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = PLUGIN_PROPERTY, orphanRemoval = true)
    private Set<PluginContract> pluginContracts = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = PLUGIN_PROPERTY, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<PluginInstance> pluginInstances = new HashSet<>();

    public Plugin() {
    }

    public Plugin(Long id) {
        this.id = id;
    }

    public Plugin(PluginDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.name = dto.getName();
        this.className = dto.getClassName();
        this.version = dto.getVersion();
        this.configSchema = dto.getConfSchema();
        this.scope = dto.getScope();
        Set<PluginContractDto> contracts = dto.getPluginContracts();
        if (contracts != null && !contracts.isEmpty()) {
            for (PluginContractDto contract : contracts) {
                pluginContracts.add(new PluginContract(contract));
            }
        }
        Set<PluginInstanceDto> instances = dto.getPluginInstances();
        if (instances != null && !instances.isEmpty()) {
            for (PluginInstanceDto instance : instances) {
                pluginInstances.add(new PluginInstance(instance));
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getConfigSchema() {
        return configSchema;
    }

    public void setConfigSchema(String configSchema) {
        this.configSchema = configSchema;
    }

    public PluginScope getScope() {
        return scope;
    }

    public void setScope(PluginScope scope) {
        this.scope = scope;
    }

    public Set<PluginContract> getPluginContracts() {
        return pluginContracts;
    }

    public void setPluginContracts(Set<PluginContract> pluginContracts) {
        this.pluginContracts = pluginContracts;
    }

    public Set<PluginInstance> getPluginInstances() {
        return pluginInstances;
    }

    public void addPluginInstance(PluginInstance pluginInstance) {
        this.pluginInstances.add(pluginInstance);
    }

    public void setPluginInstances(Set<PluginInstance> pluginInstances) {
        this.pluginInstances = pluginInstances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Plugin)) {
            return false;
        }

        Plugin plugin = (Plugin) o;

        if (className != null ? !className.equals(plugin.className) : plugin.className != null) {
            return false;
        }
        if (name != null ? !name.equals(plugin.name) : plugin.name != null) {
            return false;
        }
        if (version != null ? !version.equals(plugin.version) : plugin.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    protected PluginDto createDto() {
        return new PluginDto();
    }

    @Override
    protected Plugin newInstance(Long id) {
        return new Plugin(id);
    }

    @Override
    public PluginDto toDto() {
        PluginDto dto = toDtoNoPluginInstances();

        if (!pluginInstances.isEmpty()) {
            Set<PluginInstanceDto> pluginInstanceDtos = new HashSet<>();
            for (PluginInstance pluginInstance : pluginInstances) {
                pluginInstanceDtos.add(pluginInstance.toDto());
            }
            dto.setPluginInstances(pluginInstanceDtos);
        }
        return dto;
    }

    PluginDto toDtoNoPluginInstances() {
        PluginDto dto = createDto();
        dto.setId(getStringId());
        dto.setName(name);
        dto.setClassName(className);
        dto.setVersion(version);
        dto.setConfSchema(configSchema);
        dto.setScope(scope);
        if (!pluginContracts.isEmpty()) {
            Set<PluginContractDto> pluginContractDtos = new HashSet<>();
            for (PluginContract contract : pluginContracts) {
                pluginContractDtos.add(contract.toDto());
            }
            dto.setPluginContracts(pluginContractDtos);
        }
        return dto;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Plugin{");
        sb.append("id=").append(id == null ? null : id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", version=").append(version);
        sb.append(", configSchema='").append(configSchema).append('\'');
        sb.append(", scope=").append(scope);
        sb.append('}');
        return sb.toString();
    }
}
