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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.lb.ClusterUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.EndpointEventTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RemoteEndpointEventMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.RouteInfoMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserRouteInfoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;

public class LocalUserActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalUserActor.class);

    private final String userId;

    private final LocalUserActorMessageProcessor messageProcessor;

    /**
     * Instantiates a new user actor.
     *
     * @param context   the context
     * @param userId    the user id
     * @param tenantId  the tenant id
     */
    private LocalUserActor(AkkaContext context, String userId, String tenantId) {
        this.messageProcessor = new LocalUserActorMessageProcessor(context, userId, tenantId);
        this.userId = userId;
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<LocalUserActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Akka service context */
        private final AkkaContext context;

        private final String userId;

        private final String tenantId;

        /**
         * Instantiates a new actor creator.
         *
         * @param context   the context
         * @param userId    the user id
         * @param tenantId  the tenant id
         */
        public ActorCreator(AkkaContext context, String userId, String tenantId) {
            super();
            this.context = context;
            this.userId = userId;
            this.tenantId = tenantId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public LocalUserActor create() throws Exception {
            return new LocalUserActor(context, userId, tenantId);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("[{}] Received: {}", userId, message);
        if (message instanceof EndpointUserConnectMessage) {
            processEndpointConnectMessage((EndpointUserConnectMessage) message);
        } else if (message instanceof EndpointUserDisconnectMessage) {
            processEndpointDisconnectMessage((EndpointUserDisconnectMessage) message);
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
        } else if (message instanceof ClusterUpdateMessage) {
            messageProcessor.processClusterUpdate(context());
        }
    }

    private void processEndpointConnectMessage(EndpointUserConnectMessage message) {
        messageProcessor.processEndpointConnectMessage(context(), message);
        context().watch(message.getOriginator());
    }

    private void processEndpointDisconnectMessage(EndpointUserDisconnectMessage message) {
        messageProcessor.processEndpointDisconnectMessage(context(), message);
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
