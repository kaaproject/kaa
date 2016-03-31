package org.kaaproject.kaa.server.common.paf.shared.context;

public interface SessionContext {

    SessionId getSessionId();
    
    SessionType getSessionType();
    
    void onMessage(OutboundSessionMessage message);
    
    void onControlMessage(SessionControlMessage controlMessage);
    
}
