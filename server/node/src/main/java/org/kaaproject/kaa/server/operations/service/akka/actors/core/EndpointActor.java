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

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.ServerProfileUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ChannelTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationResponseMessage;
import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.kaaproject.kaa.server.transport.message.SessionDisconnectMessage;
import org.kaaproject.kaa.server.transport.message.SessionPingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class EndpointActor.
 */
public class EndpointActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EndpointActor.class);

    private final String actorKey;

    private final EndpointActorMessageProcessor messageProcessor;

    /**
     * Instantiates a new endpoint actor.
     *
     * @param context           the context
     * @param endpointActorKey  the endpoint actor key
     * @param appToken          the app token
     * @param key               the key
     */
    public EndpointActor(AkkaContext context, String endpointActorKey, String appToken, EndpointObjectHash key) {
        this.messageProcessor = new EndpointActorMessageProcessor(context, appToken, key, endpointActorKey);
        this.actorKey = endpointActorKey;
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<EndpointActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Akka service context */
        private final AkkaContext context;

        private final String actorKey;

        /** The app token. */
        private final String appToken;

        /** The key. */
        private final EndpointObjectHash key;

        /**
         * Instantiates a new actor creator.
         *
         * @param context           the context
         * @param endpointActorKey  the endpoint actor key
         * @param appToken          the app token
         * @param key               the key
         */
        public ActorCreator(AkkaContext context, String endpointActorKey, String appToken, EndpointObjectHash key) {
            super();
            this.context = context;
            this.actorKey = endpointActorKey;
            this.appToken = appToken;
            this.key = key;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public EndpointActor create() throws Exception {
            return new EndpointActor(context, actorKey, appToken, key);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Received: {}", actorKey, message);
        } else {
            LOG.debug("[{}] Received: {}", actorKey, message.getClass().getName());
        }
        if (message instanceof SyncRequestMessage) {
            processEndpointSync((SyncRequestMessage) message);
        } else if (message instanceof EndpointEventReceiveMessage) {
            processEndpointEventReceiveMessage((EndpointEventReceiveMessage) message);
        } else if (message instanceof LogDeliveryMessage) {
            processLogDeliveryMessage((LogDeliveryMessage) message);
        } else if (message instanceof EndpointUserConfigurationUpdateMessage) {
            processUserConfigurationUpdateMessage((EndpointUserConfigurationUpdateMessage) message);
        } else if (message instanceof UserVerificationResponseMessage) {
            processUserVerificationMessage((UserVerificationResponseMessage) message);
        } else if (message instanceof SessionDisconnectMessage) {
            processDisconnectMessage((ChannelAware) message);
        } else if (message instanceof SessionPingMessage) {
            processPingMessage((ChannelAware) message);
        } else if (message instanceof ServerProfileUpdateMessage) {
            processServerProfileUpdate((ServerProfileUpdateMessage) message);
        } else if (message instanceof ThriftNotificationMessage) {
            processThriftNotification((ThriftNotificationMessage) message);
        } else if (message instanceof NotificationMessage) {
            processNotification((NotificationMessage) message);
        } else if (message instanceof RequestTimeoutMessage) {
            processRequestTimeoutMessage((RequestTimeoutMessage) message);
        } else if (message instanceof ActorTimeoutMessage) {
            processActorTimeoutMessage((ActorTimeoutMessage) message);
        } else if (message instanceof ChannelTimeoutMessage) {
            processChannelTimeoutMessage((ChannelTimeoutMessage) message);
        } else if (message instanceof EndpointUserActionMessage) {
            processEndpointUserActionMessage((EndpointUserActionMessage) message);
        } else if (message instanceof EndpointStopMessage) {
            LOG.debug("[{}] Received stop request from application actor", actorKey);
            context().stop(self());
        } else {
            LOG.warn("[{}] Received unknown message {}", actorKey, message);
        }
    }

    private void processUserConfigurationUpdateMessage(EndpointUserConfigurationUpdateMessage message) {
        messageProcessor.processUserConfigurationUpdate(context(), message);
    }

    private void processUserVerificationMessage(UserVerificationResponseMessage message) {
        messageProcessor.processUserVerificationMessage(context(), message);
    }

    private void processLogDeliveryMessage(LogDeliveryMessage message) {
        messageProcessor.processLogDeliveryMessage(context(), message);
    }

    private void processEndpointSync(SyncRequestMessage message) {
        messageProcessor.processEndpointSync(context(), message);
    }

    private void processEndpointEventReceiveMessage(EndpointEventReceiveMessage message) {
        messageProcessor.processEndpointEventReceiveMessage(context(), message);
    }

    private void processDisconnectMessage(ChannelAware message) {
        messageProcessor.processDisconnectMessage(context(), message);
    }

    private void processPingMessage(ChannelAware message) {
        messageProcessor.processPingMessage(context(), message);
    }

    private void processServerProfileUpdate(ServerProfileUpdateMessage message) {
        messageProcessor.processServerProfileUpdate(context());
    }
    
    private void processThriftNotification(ThriftNotificationMessage message) {
        messageProcessor.processThriftNotification(context());
    }

    private void processNotification(NotificationMessage message) {
        messageProcessor.processNotification(context(), message);
    }

    private void processRequestTimeoutMessage(RequestTimeoutMessage message) {
        messageProcessor.processRequestTimeoutMessage(context(), message);
    }

    private void processActorTimeoutMessage(ActorTimeoutMessage message) {
        messageProcessor.processActorTimeoutMessage(context(), message);
    }

    private void processChannelTimeoutMessage(ChannelTimeoutMessage message) {
        messageProcessor.processChannelTimeoutMessage(context(), message);
    }

    private void processEndpointUserActionMessage(EndpointUserActionMessage message) {
        messageProcessor.processEndpointUserActionMessage(context(), message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.debug("[{}] Starting", actorKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.debug("[{}] Stoped", actorKey);
    }
}
