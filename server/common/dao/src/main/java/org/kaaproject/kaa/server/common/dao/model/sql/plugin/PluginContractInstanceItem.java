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

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class PluginContractInstanceItem extends GenericModel implements Serializable {

    private String confData;
    private PluginContractInstance pluginContractInstance;
    private PluginContractItem pluginContractItem;
    private PluginContractItem parentPluginContractItem;
    private String inMessageSchema;
    private String outMessageSchema;
    private Set<ContractItemRoute> contractItemRoutes;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
