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

package org.kaaproject.kaa.server.plugin.rest.test;

import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class PluginTestInitContext implements PluginInitContext {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(PluginTestInitContext.class);

    private final String pluginConfig;
    private final Set<PluginContractInstance> contractInstances;

    public PluginTestInitContext(String pluginConfig, Set<PluginContractInstance> contractInstances) {
        this.pluginConfig = pluginConfig;
        this.contractInstances = contractInstances;
    }

    @Override
    public String getPluginConfigurationData() {
        return this.pluginConfig;
    }

    @Override
    public Set<PluginContractInstance> getPluginContracts() {
        return this.contractInstances;
    }
}
