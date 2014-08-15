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

import org.kaaproject.kaa.server.operations.service.http.commands.AbstractHttpSyncCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

/**
 * The Class NettyCommandAwareMessage.
 */
public class NettyHttpSyncMessage extends AbstractRequestMessage implements SessionInitRequest {

    private final AbstractHttpSyncCommand command;

    public NettyHttpSyncMessage(UUID uuid, ChannelHandlerContext channelContext, ChannelType channelType, AbstractHttpSyncCommand command,
            ResponseBuilder responseConverter, ErrorBuilder errorConverter, SyncStatistics syncStatistics) {
        super(uuid, channelContext, channelType, responseConverter, errorConverter, syncStatistics);
        this.command = command;
    }

    /**
     * Gets the command.
     *
     * @return the command
     */
    public AbstractHttpSyncCommand getCommand() {
        return command;
    }

    @Override
    public byte[] getEncodedRequestData() {
        return getCommand().getRequestData();
    }

    @Override
    public byte[] getEncodedSessionKey() {
        return getCommand().getRequestkey();
    }

    @Override
    public byte[] getSessionKeySignature() {
        return getCommand().getRequestSignature();
    }

    @Override
    public void onSessionCreated(NettySessionInfo session) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getKeepAlive() {
        return 0;
    }
}
