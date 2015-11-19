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

public enum MessageType {

    ENTITY((short) 0), VOID((short)1), ACK((short) 2), ERROR((short) 3);

    private final short code;

    private MessageType(short type) {
        this.code = type;
    }

    public static MessageType get(short code) {
        for (MessageType type : MessageType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("No MessageType with code: " + code);
    }

    public short getCode() {
        return code;
    }

}
