package org.kaaproject.kaa.server.transport.message;

import org.kaaproject.kaa.server.transport.session.SessionInfo;

public abstract class SessionDisconnectMessage extends SessionControlMessage {

    public SessionDisconnectMessage(SessionInfo session) {
        super(session);
    }

}
