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

package org.kaaproject.kaa.server.operations.service.akka.messages.io.response;

import java.util.UUID;

import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

/**
 * The Class NettyDecodedResponseMessage.
 */
public class NettySessionResponseMessage implements SessionResponse{

    private final SessionInfo sessionInfo;
    private final ServerSync syncResponse;
    private final MessageBuilder responseConverter;
    private final ErrorBuilder errorConverter;

    public NettySessionResponseMessage(SessionInfo sessionInfo, ServerSync syncResponse, MessageBuilder responseConverter, ErrorBuilder errorConverter){
        this.sessionInfo = sessionInfo;
        this.syncResponse = syncResponse;
        this.responseConverter = responseConverter;
        this.errorConverter = errorConverter;
    }

    public ServerSync getSyncResponse() {
        return syncResponse;
    }

    @Override
    public ErrorBuilder getErrorConverter() {
        return errorConverter;
    }

    @Override
    public UUID getChannelUuid() {
        return sessionInfo.getUuid();
    }

    @Override
    public ChannelType getChannelType() {
        return sessionInfo.getChannelType();
    }

    @Override
    public ChannelContext getChannelContext() {
        return sessionInfo.getCtx();
    }

    @Override
    public ServerSync getResponse() {
        return syncResponse;
    }

    @Override
    public MessageBuilder getMessageBuilder() {
        return responseConverter;
    }

    @Override
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    @Override
    public int getPlatformId() {
        return sessionInfo.getPlatformId();
    }
}
