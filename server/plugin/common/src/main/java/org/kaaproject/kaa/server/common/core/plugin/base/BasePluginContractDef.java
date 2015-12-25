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

import org.kaaproject.kaa.common.dto.plugin.ContractType;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class BasePluginContractDef implements PluginContractDef {

    private static final long serialVersionUID = 6888190340997489468L;

    private final ContractDef contract;
    private final PluginContractDirection direction;
    private final Set<PluginContractItemDef> contractItems;

    private BasePluginContractDef(ContractDef contract, PluginContractDirection direction) {
        super();
        this.contract = contract;
        this.direction = direction;
        this.contractItems = new LinkedHashSet<>();
    }

    @Override
    public ContractDef getContract() {
        return contract;
    }

    @Override
    public PluginContractDirection getDirection() {
        return direction;
    }

    @Override
    public Set<PluginContractItemDef> getPluginContractItems() {
        return Collections.unmodifiableSet(contractItems);
    }

    public static Builder builder(String name, int version) {
        return new Builder(name, version);
    }

    public static class Builder {
        private final String name;
        private final int version;
        private ContractType type;
        private PluginContractDirection direction;
        private final Set<PluginContractItemDef> contractItems;

        private Builder(String name, int version) {
            super();
            this.name = name;
            this.version = version;
            this.contractItems = new LinkedHashSet<PluginContractItemDef>();
        }

        public Builder withType(ContractType type) {
            this.type = type;
            return this;
        }

        public Builder withDirection(PluginContractDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder withItem(PluginContractItemDef contract) {
            this.contractItems.add(contract);
            return this;
        }

        public BasePluginContractDef build() {
            validate();
            BasePluginContractDef result = new BasePluginContractDef(new BaseContractDef(name, version, type), direction);
            result.contractItems.addAll(this.contractItems);
            return result;
        }

        private void validate() {
            // TODO implement
        }

    }

}
