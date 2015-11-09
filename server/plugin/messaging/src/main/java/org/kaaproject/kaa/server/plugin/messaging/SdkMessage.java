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

import java.util.UUID;

import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;

public class SdkMessage implements KaaMessage {

    private static final long serialVersionUID = -8369085602140052037L;

    private final UUID uid;
    private final byte[] data;

    public SdkMessage(UUID uid, byte[] data) {
        super();
        this.uid = uid;
        this.data = data;
    }

    public UUID getUid() {
        return uid;
    }

    public byte[] getData() {
        return data;
    }

}
