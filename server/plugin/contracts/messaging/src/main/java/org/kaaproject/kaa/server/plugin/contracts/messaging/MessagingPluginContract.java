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
package org.kaaproject.kaa.server.plugin.contracts.messaging;

import org.kaaproject.kaa.common.dto.plugin.ContractType;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;

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
