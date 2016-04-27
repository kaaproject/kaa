/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.transport.message;

import java.util.UUID;

import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

/**
 * An abstract class that holds common logic/data for session control messages.
 * 
 * @author Andrew Shvayka
 *
 */
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
