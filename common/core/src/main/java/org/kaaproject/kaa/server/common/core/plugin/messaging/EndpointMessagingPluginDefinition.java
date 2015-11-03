package org.kaaproject.kaa.server.common.core.plugin.messaging;

import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractType;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginScope;

public class EndpointMessagingPluginDefinition implements PluginDef {

    private static final long serialVersionUID = -5566067441896469264L;

    private final BasePluginDef pluginDef;

    public EndpointMessagingPluginDefinition() {
        this.pluginDef = new BasePluginDef.Builder("Endpoint Messaging Plugin", 1)
                .withSchema("My schema")
                .withScope(PluginScope.ENDPOINT)
                .withType("LOL")
                .withContract(
                        BasePluginContractDef.builder("Messaging SDK contract", 1)
                                .withType(ContractType.SDK)
                                .withItem(
                                        BasePluginContractItemDef.builder("sendMessage").withSchema("My out message Schema")
                                                .withInMessage(SdkMessage.class)
                                                .withOutMessage(SdkMessage.class).build())
                                .withItem(
                                        BasePluginContractItemDef.builder("receiveMessage").withSchema("My in message Schema")
                                                .withInMessage(SdkMessage.class)
                                                .withOutMessage(SdkMessage.class).build())
                        .build())
                .withContract(
                        BasePluginContractDef.builder("Messaging Route contract", 1)
                                .withType(ContractType.ROUTE)
                                .withDirection(PluginContractDirection.IN)
                                .withItem(
                                        BasePluginContractItemDef.builder("sendMessageToEndpoint")
                                                .withSchema("My in endpoint message Schema")
                                                .withInMessage(EndpointMessage.class)
                                                .withOutMessage(EndpointMessage.class).build())
                                .withItem(
                                        BasePluginContractItemDef.builder("sendMessageToPlugin").withSchema("My out endpoint message Schema")
                                                .withInMessage(EndpointMessage.class)
                                                .withOutMessage(EndpointMessage.class).build())
                        .build())
                .build();
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
