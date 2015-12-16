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
package org.kaaproject.kaa.server.operations.service.akka.messages.core.plugin;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointExtMsg implements PluginMsg {
    private final SdkExtensionKey extKey;
    private final EndpointObjectHash endpointKey;
    private final byte[] data;

    public EndpointExtMsg(String sdkToken, int extensionId, EndpointObjectHash endpointKey, byte[] data) {
        this(new SdkExtensionKey(sdkToken, extensionId), endpointKey, data);
    }

    protected EndpointExtMsg(EndpointExtMsg msg) {
        this(msg.extKey, msg.endpointKey, msg.data);
    }

    private EndpointExtMsg(SdkExtensionKey extKey, EndpointObjectHash endpointKey, byte[] data) {
        super();
        this.extKey = extKey;
        this.endpointKey = endpointKey;
        this.data = data;
    }

    public SdkExtensionKey getExtKey() {
        return extKey;
    }

    public EndpointObjectHash getEpKey() {
        return endpointKey;
    }

    public byte[] getData() {
        return data;
    }
}
