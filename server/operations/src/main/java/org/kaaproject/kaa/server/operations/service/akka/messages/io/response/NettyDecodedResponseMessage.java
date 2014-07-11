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

import io.netty.channel.ChannelHandlerContext;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.NettyCommandAwareMessage;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;


/**
 * The Class NettyDecodedResponseMessage.
 */
public class NettyDecodedResponseMessage extends NettyCommandAwareMessage {

    /** The response. */
    private final SpecificRecordBase response;

    /**
     * Instantiates a new netty decoded response message.
     *
     * @param handlerUuid the handler uuid
     * @param channelContext the channel context
     * @param command the command
     * @param response the response
     */
    public NettyDecodedResponseMessage(String handlerUuid, ChannelHandlerContext channelContext,
            AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> command, ChannelType channelType, SpecificRecordBase response) {
        super(handlerUuid, channelContext, command, channelType);
        this.response = response;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public SpecificRecordBase getResponse() {
        return response;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.akka.messages.io.NettyCommandAwareMessage#getCommand()
     */
    @Override
    public AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> getCommand() {
        return command;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NettyDecodedResponseMessage [handlerUuid=" + handlerUuid + ", channelContext=" + channelContext + ", command=" + command + "]";
    }

}
