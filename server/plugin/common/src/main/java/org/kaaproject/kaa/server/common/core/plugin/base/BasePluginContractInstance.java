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
package org.kaaproject.kaa.server.common.core.plugin.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;

public class BasePluginContractInstance implements PluginContractInstance {

    private final PluginContractDef contract;
    private final Map<PluginContractItemDef, List<PluginContractItemInfo>> info;

    public BasePluginContractInstance(PluginContractDef contract) {
        super();
        this.contract = contract;
        this.info = new HashMap<>();
    }

    public void addContractItemInfo(PluginContractItemDef itemDef, PluginContractItemInfo itemInfo) {
        List<PluginContractItemInfo> items = info.get(itemDef);
        if(items == null){
            items = new ArrayList<>();
            info.put(itemDef, items);
        }
        items.add(itemInfo);
    }

    @Override
    public PluginContractDef getDef() {
        return contract;
    }

    @Override
    public List<PluginContractItemInfo> getContractItemInfo(PluginContractItemDef itemDef) {
        return info.get(itemDef);
    }

}
