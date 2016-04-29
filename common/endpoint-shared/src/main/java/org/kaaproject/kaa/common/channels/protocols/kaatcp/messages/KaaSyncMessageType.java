/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.common.channels.protocols.kaatcp.messages;

/**
 * KAASYNC subcomand id table
 * Mnemonic  Enumeration Description
 * UNUSED    0           reserved value
 * SYNC      1           Sync request/response
 * BOOTSTRAP 2           Bootstrap resolve/response
 * 
 * @author Andrey Panasenko
 *
 */
public enum KaaSyncMessageType {
    UNUSED((byte)0),
    SYNC((byte)1);
    
    private byte type;
    
    private KaaSyncMessageType(byte type) {
        this.type = type;
    }

    /**
     * Return byte representation of KAASYNC subcomand MessageType enum.
     * @return byte type
     */
    public byte getType() {
        return type;
    }
}
