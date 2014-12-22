/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.server.operations.service.akka.messages.io.request;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.SessionCreateListener;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

public class NettyTcpConnectMessage extends AbstractRequestMessage implements SessionInitRequest {

    private final Connect command;
    private final SessionCreateListener sessionAware;

    public NettyTcpConnectMessage(UUID uuid, String platformId, ChannelHandlerContext channelContext, Connect command,
            ChannelType channelType, SessionCreateListener sessionAware, ResponseBuilder responseConverter, ErrorBuilder errorConverter,
            SyncStatistics syncStatistics) {
        super(uuid, platformId, channelContext, channelType, responseConverter, errorConverter, syncStatistics);
        this.command = command;
        this.sessionAware = sessionAware;
    }

    @Override
    public void onSessionCreated(NettySessionInfo session) {
        this.sessionAware.onSessionCreated(session);
    }

    @Override
    public byte[] getEncodedRequestData() {
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
