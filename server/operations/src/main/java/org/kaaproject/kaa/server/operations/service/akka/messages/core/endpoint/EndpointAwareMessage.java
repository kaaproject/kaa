/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import akka.actor.ActorRef;


/**
 * The Class EndpointAwareMessage.
 */
public class EndpointAwareMessage {

    /** The app token. */
    private final String appToken;

    /** The key. */
    private final EndpointObjectHash key;

    /** The originator. */
    private final ActorRef originator;

    /**
     * Instantiates a new endpoint aware message.
     * 
     * @param appToken
     *            the app token
     * @param key
     *            the key
     * @param originator
     *            the originator
     */
    public EndpointAwareMessage(String appToken, EndpointObjectHash key, ActorRef originator) {
        super();
        this.appToken = appToken;
        this.key = key;
        this.originator = originator;
    }

    /**
     * Gets the app token.
     * 
     * @return the app token
     */
    public String getAppToken() {
        return appToken;
    }

    /**
     * Gets the key.
     * 
     * @return the key
     */
    public EndpointObjectHash getKey() {
        return key;
    }

    /**
     * Gets the originator.
     * 
     * @return the originator
     */
    public ActorRef getOriginator() {
        return originator;
    }
}
