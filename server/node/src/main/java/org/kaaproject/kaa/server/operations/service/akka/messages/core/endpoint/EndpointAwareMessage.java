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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint;

import java.util.Random;
import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import akka.actor.ActorRef;


/**
 * The Class EndpointAwareMessage.
 */
public class EndpointAwareMessage {

    private final UUID uuid;

    /** The app token. */
    private final String appToken;

    /** The key. */
    private final EndpointObjectHash key;

    /** The originator. */
    private final ActorRef originator;
    
    private final static ThreadLocal<Random> state = new ThreadLocal<Random>() { //NOSONAR
        protected Random initialValue() {
            return new Random();
        }
    };

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
        this(new UUID(state.get().nextLong(), state.get().nextLong()), appToken, key, originator);
    }

    /**
     * Instantiates a new endpoint aware message.
     *
     * @param uuid
     *            the uuid
     * @param appToken
     *            the app token
     * @param key
     *            the key
     * @param originator
     *            the originator
     */
    protected EndpointAwareMessage(UUID uuid, String appToken, EndpointObjectHash key, ActorRef originator) {
        super();
        this.uuid = uuid;
        this.appToken = appToken;
        this.key = key;
        this.originator = originator;
    }



    public UUID getUuid() {
        return uuid;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EndpointAwareMessage other = (EndpointAwareMessage) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

}
