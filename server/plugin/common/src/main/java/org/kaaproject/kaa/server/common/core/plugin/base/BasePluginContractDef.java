package org.kaaproject.kaa.server.common.core.plugin.base;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.ContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractType;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;

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

    public static Builder builder(String name, int version){
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
