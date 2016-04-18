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

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.server.transport.message.AbstractMessage;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.SessionAwareMessage;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

public class NettyTcpSyncMessage extends AbstractMessage implements SessionAwareMessage {

    private final SyncRequest command;
    private final SessionInfo sessionInfo;

    public NettyTcpSyncMessage(SyncRequest command, SessionInfo sessionInfo,
            MessageBuilder responseConverter, ErrorBuilder errorConverter) {
        super(sessionInfo.getUuid(), sessionInfo.getPlatformId(), sessionInfo.getCtx(), sessionInfo.getChannelType(), responseConverter, errorConverter);
        this.command = command;
        this.sessionInfo = sessionInfo;
    }

    @Override
    public byte[] getEncodedMessageData() {
        return command.getAvroObject();
    }

    @Override
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    @Override
    public boolean isEncrypted() {
        return command.isEncrypted();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NettyTcpSyncMessage [command=");
        builder.append(command);
        builder.append(", sessionInfo=");
        builder.append(sessionInfo);
        builder.append(", getUuid()=");
        builder.append(getChannelUuid());
        builder.append(", getChannelContext()=");
        builder.append(getChannelContext());
        builder.append(", getChannelType()=");
        builder.append(getChannelType());
        builder.append("]");
        return builder.toString();
    }
}
