package org.kaaproject.kaa.server.common.core.plugin.base;

import java.io.Serializable;

import org.kaaproject.kaa.server.common.core.plugin.def.ContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;

public class BasePluginContractItemDef implements PluginContractItemDef {

    private static final long serialVersionUID = -800752882716887179L;

    private final ContractItemDef item;
    private final String schema;

    private BasePluginContractItemDef(ContractItemDef item, String schema) {
        super();
        this.item = item;
        this.schema = schema;
    }

    @Override
    public ContractItemDef getContractItem() {
        return item;
    }

    @Override
    public String getConfigurationSchema() {
        return schema;
    }
    
    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;
        private String schema;
        private ContractMessageDef in;
        private ContractMessageDef out;

        private Builder(String name) {
            super();
            this.name = name;
        }

        public Builder withSchema(String schema) {
            this.schema = schema;
            return this;
        }
        
        public Builder withInMessage(Class<? extends Serializable> clazz) {
            return withInMessage(clazz.getName(), 1);
        }

        
        public Builder withInMessage(String fqn) {
            return withInMessage(fqn, 1);
        }

        public Builder withInMessage(String fqn, int version) {
            this.in = new BaseContractMessageDef(fqn, version);
            return this;
        }
        
        public Builder withOutMessage(Class<? extends Serializable> clazz) {
            return withOutMessage(clazz.getName(), 1);
        }

        public Builder withOutMessage(String fqn) {
            return withOutMessage(fqn, 1);
        }
        
        public Builder withOutMessage(String fqn, int version) {
            this.out = new BaseContractMessageDef(fqn, version);
            return this;
        }

        public BasePluginContractItemDef build() {
            validate();
            return new BasePluginContractItemDef(new BaseContractItemDef(name, in, out), schema);
        }

        private void validate() {
            // TODO implement
        }

    }

    private static class BaseContractItemDef implements ContractItemDef {

        private static final long serialVersionUID = 2774234565702245946L;

        private final String name;
        private final ContractMessageDef in;
        private final ContractMessageDef out;

        private BaseContractItemDef(String name, ContractMessageDef in, ContractMessageDef out) {
            super();
            this.name = name;
            this.in = in;
            this.out = out;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ContractMessageDef getInMessage() {
            return in;
        }

        @Override
        public ContractMessageDef getOutMessage() {
            return out;
        }
    }

}
