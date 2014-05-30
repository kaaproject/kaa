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

package org.kaaproject.kaa.server.operations.service.akka.actors.io;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.NettyCommandAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyDecodedRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettyDecodedResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.japi.Creator;


/**
 * The Class EndpointRequestActor.
 */
public class EndpointRequestActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EndpointRequestActor.class);

    /** The pending command message. */
    private NettyCommandAwareMessage pendingCommandMessage;

    /** The eps actor. */
    private ActorRef epsActor;

    /**
     * Instantiates a new endpoint request actor.
     * 
     * @param epsActor
     *            the eps actor
     */
    public EndpointRequestActor(ActorRef epsActor) {
        super();
        this.epsActor = epsActor;
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<EndpointRequestActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The eps actor. */
        private ActorRef epsActor;

        /**
         * Instantiates a new actor creator.
         * 
         * @param epsActor
         *            the eps actor
         */
        public ActorCreator(ActorRef epsActor) {
            super();
            this.epsActor = epsActor;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public EndpointRequestActor create() throws Exception {
            return new EndpointRequestActor(epsActor);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("Received: {}", message);
        if (message instanceof NettyDecodedRequestMessage) {
            procesCommandRequest((NettyDecodedRequestMessage) message);
        } else if (message instanceof SyncResponseMessage) {
            processEndpointSyncResponse((SyncResponseMessage) message);
        }
    }

    /**
     * Proces command request.
     * 
     * @param message
     *            the message
     */
    private void procesCommandRequest(NettyDecodedRequestMessage message) {
        this.pendingCommandMessage = message;
        this.epsActor.tell(message.toEndpointMessage(self()), self());
    }

    /**
     * Process endpoint sync response.
     * 
     * @param message
     *            the message
     */
    private void processEndpointSyncResponse(SyncResponseMessage message) {
        try {
            pendingCommandMessage.getCommand().setSyncTime(message.getSyncTime());
            pendingCommandMessage.getCommand().encode(message.getResponse());
        } catch (GeneralSecurityException | IOException e) {
            LOG.error("processEndpointRegistrationResponse", e);
        }
        NettyDecodedResponseMessage response = new NettyDecodedResponseMessage(pendingCommandMessage.getHandlerUuid(),
                pendingCommandMessage.getChannelContext(), pendingCommandMessage.getCommand(), message.getResponse());
        context().parent().tell(response, self());
        LOG.debug("Stoping actor for {}", pendingCommandMessage.getHandlerUuid());
        getContext().stop(getSelf());
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.debug("Starting {}", this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.debug("Stoped {}", this);
    }
}
