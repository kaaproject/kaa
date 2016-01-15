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

import java.util.Arrays;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;

public class EndpointMessage implements KaaMessage {

    private static final long serialVersionUID = -7358355594071995237L;

    private final EndpointObjectHash key;
    private byte[] messageData;

    public EndpointMessage(EndpointObjectHash key) {
        super();
        this.key = key;
    }

    public EndpointMessage(EndpointObjectHash key, byte[] messageData) {
        super();
        this.key = key;
        this.messageData = messageData;
    }

    public EndpointObjectHash getKey() {
        return key;
    }

    public byte[] getMessageData() {
        return this.messageData;
    }

    public void setMessageData(byte[] messageData) {
        this.messageData = Arrays.copyOf(messageData, messageData.length);
    }

}
