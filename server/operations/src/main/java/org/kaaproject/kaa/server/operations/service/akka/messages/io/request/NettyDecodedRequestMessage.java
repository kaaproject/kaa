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

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.NettyCommandAwareMessage;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;

import akka.actor.ActorRef;


/**
 * The Class NettyDecodedRequestMessage.
 */
public abstract class NettyDecodedRequestMessage extends NettyCommandAwareMessage {

    /**
     * Instantiates a new netty decoded request message.
     *
     * @param handlerUuid
     *            the handler uuid
     * @param channelContext
     *            the channel context
     * @param command
     *            the command
     */
    public NettyDecodedRequestMessage(String handlerUuid, ChannelHandlerContext channelContext,
            AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> command, ChannelType channelType) {
        super(handlerUuid, channelContext, command, channelType);
    }

    /**
     * To endpoint message.
     *
     * @param originator
     *            the originator
     * @return the endpoint aware message
     */
    public abstract EndpointAwareMessage toEndpointMessage(ActorRef originator);

}
