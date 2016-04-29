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
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointRouteUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * This actor is responsible for handling and efficient routing of different
 * updates to endpoints.
 * 
 * @author Andrew Shvayka
 *
 */
public class GlobalUserActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalUserActor.class);

    private final String userId;

    private final GlobalUserActorMessageProcessor messageProcessor;

    /**
     * Instantiates a new user actor.
     *
     * @param context   the context
     * @param userId    the user id
     * @param tenantId  the tenant id
     */
    private GlobalUserActor(AkkaContext context, String userId, String tenantId) {
        this.messageProcessor = new GlobalUserActorMessageProcessor(context, userId, tenantId);
        this.userId = userId;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("[{}] Received: {}", userId, message);
        if (message instanceof EndpointRouteUpdateMessage) {
            messageProcessor.process(context(), ((EndpointRouteUpdateMessage) message).getRoute());
        } else if (message instanceof UserConfigurationUpdateMessage) {
            messageProcessor.process(context(), ((UserConfigurationUpdateMessage) message).getUpdate());
        } else if (message instanceof ClusterUpdateMessage) {
            messageProcessor.processClusterUpdate(context());
        }
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<GlobalUserActor> {

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
        public GlobalUserActor create() throws Exception {
            return new GlobalUserActor(context, userId, tenantId);
        }
    }
}
