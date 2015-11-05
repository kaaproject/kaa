package org.kaaproject.kaa.server.plugin.messaging;

import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginScope;
import org.kaaproject.kaa.server.plugin.contracts.messaging.MessagingPluginContract;

public class EndpointMessagingPluginDefinition implements PluginDef {

    private static final long serialVersionUID = -5566067441896469264L;

    private final BasePluginDef pluginDef;

    public EndpointMessagingPluginDefinition() {
        this.pluginDef = new BasePluginDef.Builder("Endpoint Messaging Plugin", 1).withSchema(null).withScope(PluginScope.ENDPOINT)
                .withType("LOL").withContract(MessagingSDKContract.buildMessagingSDKContract())
                .withContract(MessagingPluginContract.buildMessagingContract(PluginContractDirection.IN)).build();
    }

    @Override
    public String getName() {
        return pluginDef.getName();
    }

    @Override
    public int getVersion() {
        return pluginDef.getVersion();
    }

    @Override
    public String getType() {
        return pluginDef.getType();
    }

    @Override
    public PluginScope getScope() {
        return pluginDef.getScope();
    }

    @Override
    public String getConfigurationSchema() {
        return pluginDef.getConfigurationSchema();
    }

    public Set<PluginContractDef> getPluginContracts() {
        return pluginDef.getPluginContracts();
    }
}
