package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public class AbstractEndpointActorMsg implements EndpointActorMsg {

    private final EndpointAddress address;
    private final ActorClassifier classifier;

    public AbstractEndpointActorMsg(EndpointAddress address, ActorClassifier classifier) {
        super();
        this.address = address;
        this.classifier = classifier;
    }

    @Override
    public EndpointAddress getAddress() {
        return address;
    }

    @Override
    public ActorClassifier getClassifier() {
        return classifier;
    }

    @Override
    public String toString() {
        return "AbstractEndpointActorMessage [address=" + address + ", classifier=" + classifier + "]";
    }

}
