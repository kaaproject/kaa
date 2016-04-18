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

import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.LocalEndpointActor;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.user.LocalUserActor;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.sync.Event;

import akka.actor.ActorRef;

/**
 * Represents message with events.
 * Originator: {@link LocalEndpointActor}
 * Destination: {@link LocalUserActor}
 * 
 * @author Andrew Shvayka
 *
 */
public class EndpointEventSendMessage extends EndpointAwareMessage implements UserAwareMessage{

    private final String userId;
    private final List<Event> events;

    public EndpointEventSendMessage(String userId, List<Event> events, EndpointObjectHash endpointKey, String applicationToken, ActorRef originator) {
        super(applicationToken, endpointKey, originator);
        this.userId = userId;
        this.events = Collections.unmodifiableList(events);
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public List<Event> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointEventSendMessage [userId=");
        builder.append(userId);
        builder.append(", events=");
        builder.append(events);
        builder.append(", getAppToken()=");
        builder.append(getAppToken());
        builder.append(", getKey()=");
        builder.append(getKey());
        builder.append(", getOriginator()=");
        builder.append(getOriginator());
        builder.append("]");
        return builder.toString();
    }
}
