package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.TenantAwareMessage;

public class RouteMessage<T extends EntityClusterAddress> implements TenantAwareMessage {

    private final T address;
    private final RouteOperation operation;

    public RouteMessage(T address, RouteOperation operation) {
        super();
        this.address = address;
        this.operation = operation;
    }

    public T getAddress() {
        return address;
    }

    public RouteOperation getOperation() {
        return operation;
    }

    @Override
    public String getTenantId() {
        return address.getTenantId();
    }

    public String getAppToken() {
        return address.getAppToken();
    }

    @Override
    public String toString() {
        return "RouteMessage [address=" + address + ", operation=" + operation + "]";
    }
}
