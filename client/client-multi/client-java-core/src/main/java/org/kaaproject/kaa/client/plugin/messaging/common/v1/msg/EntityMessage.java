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
package org.kaaproject.kaa.client.plugin.messaging.common.v1.msg;

import java.util.UUID;

public class EntityMessage extends AbstractPayloadMessage {

    private final byte[] payload;

    public EntityMessage(byte[] payload, short methodId) {
        this(UUID.randomUUID(), payload, methodId);
    }

    public EntityMessage(UUID uid, byte[] msg, short methodId) {
        super(uid, methodId);
        this.payload = msg;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public MessageType getType() {
        return MessageType.ENTITY;
    }

}
