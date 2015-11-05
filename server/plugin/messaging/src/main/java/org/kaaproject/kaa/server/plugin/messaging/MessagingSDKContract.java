package org.kaaproject.kaa.server.plugin.messaging;

import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractType;

public class MessagingSDKContract {

    static BasePluginContractDef buildMessagingSDKContract() {
        return BasePluginContractDef.builder("Messaging SDK contract", 1)
                .withType(ContractType.SDK)
                .withItem(
                        BasePluginContractItemDef.builder("sendMessage").withSchema("My out message Schema")
                                .withInMessage(SdkMessage.class)
                                .withOutMessage(SdkMessage.class).build())
                .withItem(
                        BasePluginContractItemDef.builder("receiveMessage").withSchema("My in message Schema")
                                .withInMessage(SdkMessage.class)
                                .withOutMessage(SdkMessage.class).build())
        .build();
    }

}
