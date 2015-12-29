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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((in == null) ? 0 : in.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((out == null) ? 0 : out.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            BaseContractItemDef other = (BaseContractItemDef) obj;
            if (in == null) {
                if (other.in != null) {
                    return false;
                }
            } else if (!in.equals(other.in)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (out == null) {
                if (other.out != null) {
                    return false;
                }
            } else if (!out.equals(other.out)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BasePluginContractItemDef other = (BasePluginContractItemDef) obj;
        if (item == null) {
            if (other.item != null) {
                return false;
            }
        } else if (!item.equals(other.item)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        return true;
    }
}
