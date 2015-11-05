package org.kaaproject.kaa.server.plugin.contracts.messaging;


import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractType;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;

public class MessagingPluginContract {

    public static BasePluginContractDef buildMessagingContract(PluginContractDirection direction) {
        return buildMessagingContract(direction, null, null);
    }
    
    public static BasePluginContractDef buildMessagingContract(PluginContractDirection direction, String inSchema, String outSchema) {
        return BasePluginContractDef.builder("Messaging Route contract", 1)
                .withType(ContractType.ROUTE)
                .withDirection(direction)
                .withItem(
                        BasePluginContractItemDef.builder("sendMessageToEndpoint")
                                .withSchema(inSchema)
                                .withInMessage(EndpointMessage.class)
                                .withOutMessage(EndpointMessage.class).build())
                .withItem(
                        BasePluginContractItemDef.builder("sendMessageToPlugin")
                                .withSchema(outSchema)
                                .withInMessage(EndpointMessage.class)
                                .withOutMessage(EndpointMessage.class).build())
        .build();
    }

}
