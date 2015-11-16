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
package org.kaaproject.kaa.client.plugin.messaging.common.v1;

import java.util.UUID;

import org.apache.avro.specific.SpecificRecordBase;

public class Message<T extends SpecificRecordBase> {

    private final UUID uuid;
    private final T msg;

    public Message(UUID uid, T msg) {
        this.uuid = uid;
        this.msg = msg;
    }

    public UUID getUuid() {
        return uuid;
    }

    public T getMsg() {
        return msg;
    }

}
