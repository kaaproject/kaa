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
package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;

import java.util.List;
import java.util.Set;

/**
 * Enables plugin and plugin instance manipulation
 */
public interface PluginService {

    /**
     * Register plugin by its definition
     * @param pluginDef the plugin definition
     * @return the registered plugin
     */
    PluginDto registerPlugin(PluginDto pluginDef);

    /**
     * Find plugin by its name and version
     * @param name the plugin name
     * @param version the plugin version
     * @return the found plugin
     */
    PluginDto findPluginByNameAndVersion(String name, Integer version);

    /**
     * Find plugin by its class name
     * @param className the plugin class name
     * @return the found plugin
     */
    PluginDto findPluginByClassName(String className);

    /**
     * Finds all plugins
     * @return all of existent plugins
     */
    List<PluginDto> findAllPlugins();

    /**
     * Unregister plugin by plugin id
     * @param id the plugin id
     */
    void unregisterPluginById(String id);

    /**
     * Save plugin instance
     * @param pluginInstanceDto the plugin instance
     * @return the saved plugin instance
     */
    PluginInstanceDto saveInstance(PluginInstanceDto pluginInstanceDto);

    /**
     * Find plugin instance by its id
     * @param id the plugin instance id
     * @return the found plugin instance
     */
    PluginInstanceDto findInstanceById(String id);

    /**
     * Find plugin instances by their plugin id
     * @param pluginId the plugin id
     * @return the list of plugin instances
     */
    Set<PluginInstanceDto> findInstancesByPluginId(String pluginId);

    /**
     * Remove plugin instance by its id
     * @param id the plugin instance id
     */
    void removeInstanceById(String id);
}
