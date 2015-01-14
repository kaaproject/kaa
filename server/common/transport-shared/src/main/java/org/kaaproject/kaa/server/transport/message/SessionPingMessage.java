package org.kaaproject.kaa.server.transport.message;

import org.kaaproject.kaa.server.transport.session.SessionInfo;

public abstract class SessionPingMessage extends SessionControlMessage {

    public SessionPingMessage(SessionInfo session) {
        super(session);
    }

}
