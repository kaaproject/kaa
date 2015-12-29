package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.EndpointActorCreator;

public class LocalEndpointActorCreator extends EndpointActorCreator<LocalEndpointActor> {

    private static final long serialVersionUID = 9080174513879065821L;

    public LocalEndpointActorCreator(AkkaContext context, String endpointActorKey, String appToken, EndpointObjectHash key) {
        super(context, endpointActorKey, appToken, key);
    }

    @Override
    public LocalEndpointActor create() throws Exception {
        return new LocalEndpointActor(context, actorKey, appToken, endpointKey);
    }

}
