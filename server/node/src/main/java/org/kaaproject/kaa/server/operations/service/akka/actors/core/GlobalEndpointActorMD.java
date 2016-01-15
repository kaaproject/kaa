package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import akka.actor.ActorRef;

public class GlobalEndpointActorMD extends EndpointActorMD {

    public GlobalEndpointActorMD(ActorRef actorRef, String actorId) {
        super(actorRef, actorId);
    }

}
