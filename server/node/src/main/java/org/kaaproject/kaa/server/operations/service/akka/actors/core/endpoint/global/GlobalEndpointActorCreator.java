package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.EndpointActorCreator;

public class GlobalEndpointActorCreator extends EndpointActorCreator<GlobalEndpointActor> {

    private static final long serialVersionUID = 9080174513879065821L;

    public GlobalEndpointActorCreator(AkkaContext context, String endpointActorKey, String appToken, EndpointObjectHash key) {
        super(context, endpointActorKey, appToken, key);
    }

    @Override
    public GlobalEndpointActor create() throws Exception {
        return new GlobalEndpointActor(context, actorKey, appToken, endpointKey);
    }

}
