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

package org.kaaproject.kaa.server.transports.http.transport.messages;

import java.util.UUID;

import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.AbstractMessage;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.kaaproject.kaa.server.transports.http.transport.commands.AbstractHttpSyncCommand;

/**
 * The Class NettyCommandAwareMessage.
 */
public class NettyHttpSyncMessage extends AbstractMessage implements SessionInitMessage {

    private final AbstractHttpSyncCommand command;

    public NettyHttpSyncMessage(UUID uuid, Integer platformId, ChannelContext channelContext, ChannelType channelType, AbstractHttpSyncCommand command,
            MessageBuilder responseConverter, ErrorBuilder errorConverter) {
        super(uuid, platformId, channelContext, channelType, responseConverter, errorConverter);
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
    public byte[] getEncodedMessageData() {
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
    public void onSessionCreated(SessionInfo session) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getKeepAlive() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.akka.messages.io.request.Request#isEncrypted()
     */
    @Override
    public boolean isEncrypted() {
        return true;
    }
}
