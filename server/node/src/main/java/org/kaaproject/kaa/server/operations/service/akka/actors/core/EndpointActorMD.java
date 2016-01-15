package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import akka.actor.ActorRef;

public class EndpointActorMD {
    final ActorRef actorRef;
    final String actorId;

    public EndpointActorMD(ActorRef actorRef, String actorId) {
        super();
        this.actorRef = actorRef;
        this.actorId = actorId;
    }

}
