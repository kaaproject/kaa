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

package org.kaaproject.kaa.server.common.core.plugin.def;

import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;

/**
 * An object to initialize plugin instances with.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @see org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin
 *
 * @since v1.0.0
 */
public interface PluginInitContext {

    /**
     * Returns plugin configuration data.
     *
     * @return Plugin configuration data
     */
    String getPluginConfigurationData();

    /**
     * Returns plugin contract instances.
     *
     * @return Plugin contract instances
     */
    Set<PluginContractInstance> getPluginContracts();
}
