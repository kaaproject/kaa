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

package org.kaaproject.kaa.server.transports.tcp.transport.messages;

import java.util.UUID;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.AbstractMessage;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionCreateListener;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

public class NettyTcpConnectMessage extends AbstractMessage implements SessionInitMessage {

    private final Connect command;
    private final SessionCreateListener sessionAware;

    public NettyTcpConnectMessage(UUID uuid, ChannelContext channelContext, Connect command,
            ChannelType channelType, SessionCreateListener sessionAware, MessageBuilder responseConverter, ErrorBuilder errorConverter) {
        super(uuid, command.getNextProtocolId(), channelContext, channelType, responseConverter, errorConverter);
        this.command = command;
        this.sessionAware = sessionAware;
    }

    @Override
    public void onSessionCreated(SessionInfo session) {
        this.sessionAware.onSessionCreated(session);
    }

    @Override
    public byte[] getEncodedMessageData() {
        return command.getSyncRequest();
    }

    @Override
    public byte[] getEncodedSessionKey() {
        return command.getAesSessionKey();
    }

    @Override
    public byte[] getSessionKeySignature() {
        return command.getSignature();
    }

    @Override
    public boolean isEncrypted() {
        return command.isEncrypted();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NettyTcpConnectMessage [command=");
        builder.append(command);
        builder.append(", sessionAware=");
        builder.append(sessionAware);
        builder.append(", getUuid()=");
        builder.append(getChannelUuid());
        builder.append(", getChannelContext()=");
        builder.append(getChannelContext());
        builder.append(", getChannelType()=");
        builder.append(getChannelType());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int getKeepAlive() {
        return command.getKeepAlive();
    }

}
