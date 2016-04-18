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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.event.EndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.RouteTableAddress;
import org.kaaproject.kaa.server.sync.Event;

import akka.actor.ActorRef;

public class EndpointEventReceiveMessage extends EndpointAwareMessage implements UserAwareMessage {

    private final String userId;
    private final List<EndpointEvent> events;
    private final RouteTableAddress address;

    public EndpointEventReceiveMessage(String userId, List<EndpointEvent> events, RouteTableAddress address, ActorRef originator) {
        super(address.getApplicationToken(), address.getEndpointKey(), originator);
        this.userId = userId;
        this.address = address;
        this.events = Collections.unmodifiableList(events);
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public RouteTableAddress getAddress() {
        return address;
    }

    public List<EndpointEvent> getEndpointEvents() {
        return events;
    }

    public List<Event> getEvents() {
        List<Event> result = new ArrayList<>(events.size());
        for (EndpointEvent event : events) {
            result.add(event.getEvent());
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointEventReceiveMessage [userId=");
        builder.append(userId);
        builder.append(", events=");
        builder.append(events);
        builder.append("]");
        return builder.toString();
    }
}
