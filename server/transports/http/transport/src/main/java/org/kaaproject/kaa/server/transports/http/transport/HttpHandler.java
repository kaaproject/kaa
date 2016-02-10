/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.transports.http.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.NettyChannelContext;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.kaaproject.kaa.server.transports.http.transport.commands.AbstractHttpSyncCommand;
import org.kaaproject.kaa.server.transports.http.transport.messages.NettyHttpSyncMessage;
import org.kaaproject.kaa.server.transports.http.transport.netty.AbstractCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AkkaHandler.
 */
public class HttpHandler extends SimpleChannelInboundHandler<AbstractCommand>  implements MessageBuilder, ErrorBuilder{

    private static final Logger LOG = LoggerFactory.getLogger(HttpHandler.class);

    private final MessageHandler messageHandler;

    /** The uuid. */
    private final UUID uuid;

    private volatile AbstractHttpSyncCommand command;

    /**
     * Instantiates a new akka handler.
     *
     * @param uuid
     *            - session uuid
     * @param messageHandler
     *            the message handler
     */
    public HttpHandler(UUID uuid, MessageHandler messageHandler) {
        super();
        this.messageHandler = messageHandler;
        this.uuid = uuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.DefaultHandler#channelRead0
     * (io.netty.channel.ChannelHandlerContext,
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor)
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final AbstractCommand msg) throws Exception {
        this.command = (AbstractHttpSyncCommand) msg;
        NettyHttpSyncMessage message = new NettyHttpSyncMessage(uuid, msg.getNextProtocol(), new NettyChannelContext(ctx), command.getChannelType(), command , this, this);
        LOG.trace("Forwarding {} to handler", message);
        messageHandler.process(message);
    }

    @Override
    public Object[] build(Exception e) {
        return null;
    }

    @Override
    public Object[] build(byte[] responseData, byte[] responseSignatureData, boolean isEncrypted) {
        command.setResponseBody(responseData);
        if(responseSignatureData != null){
            command.setResponseSignature(responseSignatureData);
        }
        return new Object[]{command};
    }

    @Override
    public Object[] build(byte[] messageData, boolean isEncrypted) {
        return build(messageData, null, isEncrypted);
    }
}
