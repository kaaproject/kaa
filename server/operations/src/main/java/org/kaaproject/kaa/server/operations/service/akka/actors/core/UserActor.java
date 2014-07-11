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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.EndpointEventTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RemoteEndpointEventMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;

public class UserActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(UserActor.class);

    private final String userId;

    private final UserActorMessageProcessor messageProcessor;

    /**
     * Instantiates a new user actor.
     *
     * @param operationsService
     *            the operations service
     */
    private UserActor(CacheService cacheService, EventService eventService, String userId, String tenantId) {
        this.messageProcessor = new UserActorMessageProcessor(cacheService, eventService, userId, tenantId);
        this.userId = userId;
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<UserActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The cache service. */
        private final CacheService cacheService;

        /** The event service. */
        private final EventService eventService;

        private final String userId;

        private final String tenantId;

        /**
         * Instantiates a new actor creator.
         *
         * @param operationsService
         *            the operations service
         * @param eventService
         * @param notificationDeltaService
         *            the notification delta service
         */
        public ActorCreator(CacheService cacheService, EventService eventService, String userId, String tenantId) {
            super();
            this.cacheService = cacheService;
            this.eventService = eventService;
            this.userId = userId;
            this.tenantId = tenantId;
        }

        /*
         * (non-Javadoc)
         *
         * @see akka.japi.Creator#create()
         */
        @Override
        public UserActor create() throws Exception {
            return new UserActor(cacheService, eventService, userId, tenantId);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("[{}] Received: {}", userId, message);
        if (message instanceof EndpointUserConnectMessage) {
            processEndpointConnectMessage((EndpointUserConnectMessage) message);
        } else if (message instanceof EndpointEventSendMessage) {
            processEndpointEventSendMessage((EndpointEventSendMessage) message);
        } else if (message instanceof RemoteEndpointEventMessage) {
            processRemoteEndpointEventMessage((RemoteEndpointEventMessage) message);
        } else if (message instanceof EndpointEventTimeoutMessage) {
            processEndpointEventTimeoutMessage((EndpointEventTimeoutMessage) message);
        } else if (message instanceof EndpointEventDeliveryMessage) {
            processEndpointEventDeliveryMessage((EndpointEventDeliveryMessage) message);
        } else if (message instanceof RouteInfoMessage) {
            processRouteInfoMessage((RouteInfoMessage) message);
        } else if (message instanceof UserRouteInfoMessage) {
            processUserRouteInfoMessage((UserRouteInfoMessage) message);
        } else if (message instanceof Terminated) {
            processTerminationMessage((Terminated) message);
        }
    }

    private void processEndpointConnectMessage(EndpointUserConnectMessage message) {
        messageProcessor.processEndpointConnectMessage(context(), message);
        context().watch(message.getOriginator());
    }

    private void processEndpointEventSendMessage(EndpointEventSendMessage message) {
        messageProcessor.processEndpointEventSendMessage(context(), message);
    }

    private void processRemoteEndpointEventMessage(RemoteEndpointEventMessage message) {
        messageProcessor.processRemoteEndpointEventMessage(context(), message);
    }

    private void processEndpointEventTimeoutMessage(EndpointEventTimeoutMessage message) {
        messageProcessor.processEndpointEventTimeoutMessage(context(), message);
    }

    private void processEndpointEventDeliveryMessage(EndpointEventDeliveryMessage message) {
        messageProcessor.processEndpointEventDeliveryMessage(context(), message);
    }

    private void processRouteInfoMessage(RouteInfoMessage message) {
        messageProcessor.processRouteInfoMessage(context(), message);
    }

    private void processUserRouteInfoMessage(UserRouteInfoMessage message) {
        messageProcessor.processUserRouteInfoMessage(context(), message);
    }

    private void processTerminationMessage(Terminated message) {
        messageProcessor.processTerminationMessage(context(), message);
    }

}
