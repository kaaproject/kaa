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

package org.kaaproject.kaa.server.common.dao.impl;

/**
 * Provides methods to retrieve plugins
 * @param <T> the generic plugin type
 */
public interface PluginDao<T> extends SqlDao<T> {

    /**
     * Find plugin by its name and version
     * @param name the plugin name
     * @param version the plugin version
     * @return the found plugin
     */
    T findByNameAndVersion(String name, Integer version);

    /**
     * Find plugin by its class name
     * @param className the plugin class name
     * @return the found plugin
     */
    T findByClassName(String className);
}
