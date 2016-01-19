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
        } else if(msg instanceof EndpointActorMsg){
            processEndpointActorMsg((EndpointActorMsg) msg);
        }else if (msg instanceof ClusterUpdateMessage) {
            processClusterUpdateMessage();
        }
    }

    private void processRouteMessage(EndpointRouteMessage msg) {
        messageProcessor.processRouteMessage(msg);
    }
    
    private void processEndpointActorMsg(EndpointActorMsg msg) {
        messageProcessor.processEndpointActorMsg(msg);
    }

    private void processClusterUpdateMessage() {
        messageProcessor.processClusterUpdate(context());
    }
}
