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
