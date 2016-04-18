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

package org.kaaproject.kaa.server.operations.service.akka;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointRouteUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdate;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RemoteEndpointEventMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.event.EventServiceListener;
import org.kaaproject.kaa.server.operations.service.event.GlobalRouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.RouteInfo;
import org.kaaproject.kaa.server.operations.service.event.UserRouteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

final class AkkaEventServiceListener implements EventServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(AkkaEventServiceListener.class);

    private final ActorRef opsActor;

    public AkkaEventServiceListener(ActorRef opsActor) {
        super();
        this.opsActor = opsActor;
    }

    @Override
    public void onRouteInfo(RouteInfo routeInfo) {
        RouteInfoMessage message = new RouteInfoMessage(routeInfo);
        LOG.debug("Sending message {} to OPS actor", message);
        opsActor.tell(message, ActorRef.noSender());
    }

    @Override
    public void onUserRouteInfo(UserRouteInfo routeInfo) {
        UserRouteInfoMessage message = new UserRouteInfoMessage(routeInfo);
        LOG.debug("Sending message {} to OPS actor", message);
        opsActor.tell(message, ActorRef.noSender());
    }

    @Override
    public void onEvent(RemoteEndpointEvent event) {
        RemoteEndpointEventMessage message = new RemoteEndpointEventMessage(event);
        LOG.debug("Sending message {} to OPS actor", message);
        opsActor.tell(message, ActorRef.noSender());
    }

    @Override
    public void onEndpointStateUpdate(EndpointUserConfigurationUpdate notification) {
        opsActor.tell(new EndpointUserConfigurationUpdateMessage(notification), ActorRef.noSender());
    }

    @Override
    public void onEndpointRouteUpdate(GlobalRouteInfo message) {
        opsActor.tell(new EndpointRouteUpdateMessage(message), ActorRef.noSender());
    }

    @Override
    public void onServerError(String serverId) {
        // TODO: handle
    }
}