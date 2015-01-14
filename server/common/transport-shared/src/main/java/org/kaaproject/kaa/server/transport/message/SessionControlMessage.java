package org.kaaproject.kaa.server.transport.message;

import java.util.UUID;

import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

public abstract class SessionControlMessage implements SessionAware, ChannelAware {

    private final SessionInfo session;

    public SessionControlMessage(SessionInfo session) {
        this.session = session;
    }
    
    @Override
    public UUID getChannelUuid() {
        return session.getUuid();
    }

    @Override
    public SessionInfo getSessionInfo() {
        return session;
    }

    @Override
    public ChannelType getChannelType() {
        return session.getChannelType();
    }

    @Override
    public ChannelContext getChannelContext() {
        return session.getCtx();
    }
}
