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

package org.kaaproject.kaa.server.transport;

import java.security.PublicKey;

import org.kaaproject.kaa.server.transport.message.MessageHandler;

/**
 * Provides a context for transport initialization parameters and {@link MessageHandler}.
 * 
 * @author Andrew Shvayka
 *
 */
public class TransportContext {

    private final TransportProperties commonProperties;
    private final PublicKey serverKey;
    private final MessageHandler handler;
    
    public TransportContext(TransportProperties commonProperties, PublicKey serverKey, MessageHandler handler) {
        super();
        this.commonProperties = commonProperties;
        this.serverKey = serverKey;
        this.handler = handler;
    }
    
    public TransportContext(TransportContext other){
        this(other.getCommonProperties(), other.getServerKey(), other.getHandler());
    }

    public TransportProperties getCommonProperties() {
        return commonProperties;
    }

    /**
     * Returns {@link PublicKey} that is used during the encoding/decoding 
     * of the messages that are dispatched by this {@link Transport}.
     * 
     * @return the public key
     */
    public PublicKey getServerKey() {
        return serverKey;
    }

    /**
     * Returns {@link MessageHandler} for this {@link Transport}
     * @return the message handler
     */
    public MessageHandler getHandler() {
        return handler;
    }
}
