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

import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plugin", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "version"}, name = "plugin_name_version_constraint"),
        @UniqueConstraint(columnNames = {"class_name"}, name = "plugin_class_name_constraint")})
public class Plugin extends GenericModel<PluginDto> implements Serializable {

    private String name;
    private String className;
    private Integer version;
    private String configSchema;
    private PluginScope scope;
    private Set<PluginInstance> pluginInstances = new HashSet<>();
    private Set<PluginContract> pluginContracts = new HashSet<>();

    public Plugin(PluginDto dto) {
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

    @Override
    protected PluginDto createDto() {
        return new PluginDto();
    }

    @Override
    public PluginDto toDto() {
        PluginDto dto = createDto();
        return dto;
    }
}
