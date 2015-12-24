/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.impl.sql.plugin;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;

public class PluginInstanceTestFactory {

    public static final String NAME = "Instance 1";
    public static final String CONF_DATA = "ConfData";

    public static PluginInstanceDto create(PluginDto pluginDto, String name) {
        PluginInstanceDto pluginInstanceDto = new PluginInstanceDto();
        pluginInstanceDto.setPluginDefinition(pluginDto);
        pluginInstanceDto.setName(name);
        pluginInstanceDto.setConfigurationData(CONF_DATA);
        pluginInstanceDto.setState(PluginInstanceState.ACTIVE);
        return pluginInstanceDto;
    }

    public static PluginInstanceDto create(PluginDto pluginDto) {
        return create(pluginDto, NAME);
    }
}
