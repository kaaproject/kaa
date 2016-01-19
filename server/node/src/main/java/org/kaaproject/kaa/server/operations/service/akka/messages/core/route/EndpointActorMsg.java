package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public interface EndpointActorMsg {
    
    EndpointAddress getAddress();
    
    ActorClassifier getClassifier();
    
}
