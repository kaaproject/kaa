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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.session;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;

import akka.actor.ActorRef;


/**
 * The Class SessionMessage.
 */
public class SessionMessage extends EndpointAwareMessage {

    /** The session attributes. */
    private final SessionAttributes sessionAttributes;

    /**
     * Instantiates a new session message.
     * 
     * @param appToken
     *            the app token
     * @param key
     *            the key
     * @param originator
     *            the originator
     * @param sessionAttributes
     *            the session attributes
     */
    public SessionMessage(String appToken, EndpointObjectHash key, ActorRef originator, SessionAttributes sessionAttributes) {
        super(appToken, key, originator);
        this.sessionAttributes = sessionAttributes;
    }

    /**
     * Gets the session attributes.
     * 
     * @return the session attributes
     */
    public SessionAttributes getSessionAttributes() {
        return sessionAttributes;
    }

    /**
     * The Class SessionAttributes.
     */
    public static class SessionAttributes {

        /** The start. */
        final long start;

        /** The timeout. */
        final long timeout;

        /**
         * Instantiates a new session attributes.
         * 
         * @param start
         *            the start
         * @param timeout
         *            the timeout
         */
        public SessionAttributes(long start, long timeout) {
            super();
            this.start = start;
            this.timeout = timeout;
        }

        /**
         * Gets the start.
         * 
         * @return the start
         */
        public long getStart() {
            return start;
        }

        /**
         * Gets the timeout.
         * 
         * @return the timeout
         */
        public long getTimeout() {
            return timeout;
        }
    }
}
