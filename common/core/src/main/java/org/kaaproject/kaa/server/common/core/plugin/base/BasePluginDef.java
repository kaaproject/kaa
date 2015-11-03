package org.kaaproject.kaa.server.common.core.plugin.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginScope;

public class BasePluginDef implements PluginDef {

    private static final long serialVersionUID = 5265478380530998610L;

    private final String name;
    private final int version;
    private final String type;
    private final PluginScope scope;
    private final String confSchema;
    private final Set<PluginContractDef> contracts;

    private BasePluginDef(String name, int version, String type, PluginScope scope, String confSchema) {
        super();
        this.name = name;
        this.version = version;
        this.type = type;
        this.scope = scope;
        this.confSchema = confSchema;
        this.contracts = new HashSet<PluginContractDef>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public PluginScope getScope() {
        return scope;
    }

    @Override
    public String getConfigurationSchema() {
        return confSchema;
    }

    @Override
    public Set<PluginContractDef> getPluginContracts() {
        return Collections.unmodifiableSet(contracts);
    }

    public static class Builder {
        private String name;
        private int version;
        private String type;
        private PluginScope scope;
        private String confSchema;
        private Set<PluginContractDef> contracts;

        public Builder(String name, int version) {
            super();
            this.name = name;
            this.version = version;
            this.contracts = new LinkedHashSet<>();
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withScope(PluginScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder withSchema(String confSchema) {
            this.confSchema = confSchema;
            return this;
        }

        public Builder withContract(PluginContractDef contract) {
            this.contracts.add(contract);
            return this;
        }

        public BasePluginDef build() {
            validate();
            BasePluginDef result = new BasePluginDef(name, version, type, scope, confSchema);
            result.contracts.addAll(this.contracts);
            return result;
        }

        private void validate() {
            // TODO implement
        }

    }
}
