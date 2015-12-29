package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.AbstractEndpointActorMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalEndpointActorMessageProcessor extends AbstractEndpointActorMessageProcessor<GlobalEndpointActorState> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalEndpointActorMessageProcessor.class);

    public GlobalEndpointActorMessageProcessor(AkkaContext context, String appToken, EndpointObjectHash key, String actorKey) {
        super(new GlobalEndpointActorState(Base64Util.encode(key.getData()), actorKey), context.getOperationsService(), appToken, key,
                actorKey, Base64Util.encode(key.getData()), context.getGlobalEndpointTimeout());
    }
}
