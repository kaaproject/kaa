package org.kaaproject.kaa.server.operations.service.akka.messages.core.stats;


public class StatusRequestState {

    private final StatusRequestMessage originator;
    private int pendingResponses;
    private int endpontCount;
    
    public StatusRequestState(StatusRequestMessage originator, int pendingResponses) {
        super();
        this.originator = originator;
        this.pendingResponses = pendingResponses;
    }
    
    public boolean processResponse(ActorStatusResponse response){
        endpontCount += response.getEndpointCount();
        pendingResponses--;
        return pendingResponses == 0;
    }

    public int getEndpontCount() {
        return endpontCount;
    }

    public StatusRequestMessage getOriginator() {
        return originator;
    }
}
