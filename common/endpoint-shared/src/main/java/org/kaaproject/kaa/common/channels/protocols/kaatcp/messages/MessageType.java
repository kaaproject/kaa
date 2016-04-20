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
 * Kaatcp messgage type enum:
 * 1. CONNECT - use to authenticate new created tcp/ip session
 * 2. CONNACK - use to acknowledge CONNECT message
 * 3. PINGREQ - use to ping request operations server
 * 4. PINGRESP - use to ping response from operations server to endpoint
 * 5. DISCONNECT - use to notify other side that tcp/ip session terminates.
 * 6. KAASYNC - use to transmit Avro synchronizations objects. 
 * @author Andrey Panasenko
 *
 */
public enum MessageType {
    CONNECT((byte)1),
    CONNACK((byte)2),
    PINGREQ((byte)12),
    PINGRESP((byte)13),
    DISCONNECT((byte)14),
    KAASYNC((byte)15);
    
    private byte type;
    
    private MessageType(byte type) {
        this.type = type;
    }

    /**
     * Return byte representation of MessageType enum.
     * @return byte type
     */
    public byte getType() {
        return type;
    }
}
