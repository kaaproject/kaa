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

import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import akka.actor.ActorRef;


/**
 * The Class RegistrationRequestMessage.
 */
public class RegistrationRequestMessage extends EndpointAwareMessage {

    /** The request. */
    private final EndpointRegistrationRequest request;

    /**
     * Instantiates a new registration request message.
     * 
     * @param appToken
     *            the app token
     * @param key
     *            the key
     * @param request
     *            the request
     * @param originator
     *            the originator
     */
    public RegistrationRequestMessage(String appToken, EndpointObjectHash key, EndpointRegistrationRequest request, ActorRef originator) {
        super(appToken, key, originator);
        this.request = request;
    }

    /**
     * Gets the request.
     * 
     * @return the request
     */
    public EndpointRegistrationRequest getRequest() {
        return request;
    }
}
