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

import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_CONF_DATA;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_PLUGIN_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_STATE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PLUGIN_INSTANCE_TABLE_NAME;

// TODO: review, corresponding dto contains more fields
@Entity
@Table(name = PLUGIN_INSTANCE_TABLE_NAME)
public class PluginInstance extends GenericModel implements Serializable {

    private static final long serialVersionUID = 6524013582789661070L;

    @Column(name = PLUGIN_INSTANCE_CONF_DATA)
    private String configData;

    @Column(name = PLUGIN_INSTANCE_STATE)
    private String state;

    @ManyToOne
    @JoinColumn(name = PLUGIN_INSTANCE_PLUGIN_ID)
    private Plugin plugin;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pluginInstance")
    private Set<PluginContractInstance> pluginContractInstances;

    public PluginInstance() {
    }

    public PluginInstance(PluginInstanceDto dto) {

    }

    public PluginInstance(String configData, String state, Plugin plugin, Set<PluginContractInstance> pluginContractInstances) {
        this.configData = configData;
        this.state = state;
        this.plugin = plugin;
        this.pluginContractInstances = pluginContractInstances;
    }

    @Override
    protected PluginInstanceDto createDto() {
        return new PluginInstanceDto();
    }

    @Override
    public PluginInstanceDto toDto() {
        return createDto();
    }
}
