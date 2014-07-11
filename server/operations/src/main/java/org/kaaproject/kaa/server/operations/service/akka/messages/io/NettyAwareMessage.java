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

package org.kaaproject.kaa.server.operations.service.akka.messages.io;

import io.netty.channel.ChannelHandlerContext;


/**
 * The Class NettyAwareMessage.
 */
public class NettyAwareMessage {

    /** The handler uuid. */
    protected final String handlerUuid;

    /** The channel context. */
    protected final ChannelHandlerContext channelContext;

    /**
     * Instantiates a new netty aware message.
     *
     * @param handlerUuid the handler uuid
     * @param channelContext the channel context
     */
    public NettyAwareMessage(String handlerUuid, ChannelHandlerContext channelContext) {
        super();
        this.handlerUuid = handlerUuid;
        this.channelContext = channelContext;
    }

    /**
     * Gets the handler uuid.
     *
     * @return the handler uuid
     */
    public String getHandlerUuid() {
        return handlerUuid;
    }

    /**
     * Gets the channel context.
     *
     * @return the channel context
     */
    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }
}
