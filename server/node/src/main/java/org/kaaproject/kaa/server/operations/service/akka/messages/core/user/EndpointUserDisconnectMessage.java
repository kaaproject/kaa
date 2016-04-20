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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;

import akka.actor.ActorRef;

/**
 * Represents intent of endpoint to disconnect from local user actor
 * 
 * @author Andrew Shvayka
 *
 */
public class EndpointUserDisconnectMessage extends EndpointAwareMessage implements UserAwareMessage{

    private final String userId;

    public EndpointUserDisconnectMessage(String userId, EndpointObjectHash endpointKey, String applicationToken, ActorRef originator) {
        super(applicationToken, endpointKey, originator);
        this.userId = userId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointUserDisconnectMessage [userId=");
        builder.append(userId);
        builder.append(", getAppToken()=");
        builder.append(getAppToken());
        builder.append(", getKey()=");
        builder.append(getKey());
        builder.append("]");
        return builder.toString();
    }

}
