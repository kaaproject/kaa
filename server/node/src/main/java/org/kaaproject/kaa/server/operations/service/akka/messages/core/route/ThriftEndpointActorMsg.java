package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public class ThriftEndpointActorMsg<T> extends AbstractEndpointActorMsg {

    private final T msg;

    public ThriftEndpointActorMsg(EndpointAddress address, ActorClassifier classifier, T msg) {
        super(address, classifier);
        this.msg = msg;
    }

    public T getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ThriftEndpointActorMsg [msg=" + msg + ", address=" + getAddress() + ", classifier=" + getClassifier() + "]";
    }

}
