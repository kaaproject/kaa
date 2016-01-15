package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
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
    public void onReceive(Object message) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("[{}] Received: {}", actorKey, message);
        } else {
            LOG.debug("[{}] Received: {}", actorKey, message.getClass().getName());
        }
    }    
}
