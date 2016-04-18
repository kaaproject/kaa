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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.lb.ClusterUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class GlobalEndpointActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalEndpointActor.class);

    private final String actorKey;

    private final GlobalEndpointActorMessageProcessor messageProcessor;

    GlobalEndpointActor(AkkaContext context, String actorKey, String appToken, EndpointObjectHash endpointKey) {
        this.messageProcessor = new GlobalEndpointActorMessageProcessor(context, appToken, endpointKey, actorKey);
        this.actorKey = actorKey;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Received: {}", actorKey, msg);
        } else {
            LOG.debug("[{}] Received: {}", actorKey, msg.getClass().getName());
        }
        if (msg instanceof EndpointRouteMessage) {
            processRouteMessage((EndpointRouteMessage) msg);
        } else if (msg instanceof EndpointActorMsg) {
            processEndpointActorMsg((EndpointActorMsg) msg);
        } else if (msg instanceof ClusterUpdateMessage) {
            processClusterUpdateMessage();
        }
    }

    private void processRouteMessage(EndpointRouteMessage msg) {
        messageProcessor.processRouteMessage(msg);
    }

    private void processEndpointActorMsg(EndpointActorMsg msg) {
        messageProcessor.processEndpointActorMsg(context(), msg);
    }

    private void processClusterUpdateMessage() {
        messageProcessor.processClusterUpdate(context());
    }
}
