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
package org.kaaproject.kaa.common.dto.plugin;

import org.kaaproject.kaa.common.dto.HasId;

import java.io.Serializable;
import java.util.Set;

public class PluginDto implements HasId, Serializable {

    private static final long serialVersionUID = -5572266074098498423L;

    private String id;
    private String className;
    private PluginState state;
    private String name;
    private Integer version;
    private PluginScope scope;
    private String confSchema;
    private Set<PluginContractDto> pluginContracts;
    private Set<PluginInstanceDto> pluginInstances;

    public PluginDto() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public PluginState getState() {
        return state;
    }

    public void setState(PluginState state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public PluginScope getScope() {
        return scope;
    }

    public void setScope(PluginScope scope) {
        this.scope = scope;
    }

    public String getConfSchema() {
        return confSchema;
    }

    public void setConfSchema(String confSchema) {
        this.confSchema = confSchema;
    }

    public Set<PluginContractDto> getPluginContracts() {
        return pluginContracts;
    }

    public void setPluginContracts(Set<PluginContractDto> pluginContracts) {
        this.pluginContracts = pluginContracts;
    }

    public Set<PluginInstanceDto> getPluginInstances() {
        return pluginInstances;
    }

    public void setPluginInstances(Set<PluginInstanceDto> pluginInstances) {
        this.pluginInstances = pluginInstances;
    }
}
