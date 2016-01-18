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

package org.kaaproject.kaa.server.plugin.rest;

import java.util.Set;

import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.dto.plugin.PluginScope;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.plugin.contracts.messaging.MessagingPluginContract;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;

/**
 * @author Bohdan Khablenko
 */
public class KaaRestPluginDef implements PluginDef {

    private static final long serialVersionUID = -6539544242450109900L;

    private final BasePluginDef pluginDef;

    public KaaRestPluginDef() {
        this.pluginDef = new BasePluginDef.Builder("Kaa REST Plugin", 1)
                .withType("rest")
                .withScope(PluginScope.LOCAL_APPLICATION)
                .withSchema(KaaRestPluginConfig.SCHEMA$.toString())
                .withContract(MessagingPluginContract.buildMessagingContract(PluginContractDirection.OUT))
                .build();
    }

    @Override
    public String getName() {
        return this.pluginDef.getName();
    }

    @Override
    public int getVersion() {
        return this.pluginDef.getVersion();
    }

    @Override
    public String getType() {
        return this.pluginDef.getType();
    }

    @Override
    public PluginScope getScope() {
        return this.pluginDef.getScope();
    }

    @Override
    public String getConfigurationSchema() {
        return this.pluginDef.getConfigurationSchema();
    }

    @Override
    public Set<PluginContractDef> getPluginContracts() {
        return this.pluginDef.getPluginContracts();
    }
}
