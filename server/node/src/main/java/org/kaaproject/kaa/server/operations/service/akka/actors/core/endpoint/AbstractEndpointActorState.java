package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint;

public abstract class AbstractEndpointActorState {

    protected final String endpointKey;
    protected final String actorKey;
    private long lastActivityTime;

    public AbstractEndpointActorState(String endpointKey, String actorKey) {
        super();
        this.endpointKey = endpointKey;
        this.actorKey = actorKey;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long time) {
        this.lastActivityTime = time;
    }

}
