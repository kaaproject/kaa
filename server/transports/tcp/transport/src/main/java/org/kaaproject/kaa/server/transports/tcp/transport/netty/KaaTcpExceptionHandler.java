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

package org.kaaproject.kaa.server.transports.tcp.transport.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KaaTcpExceptionHandler Class. Used to generate Kaa Tcp response in case of
 * error during Kaa Tcp request processing.
 *
 */
public class KaaTcpExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory
            .getLogger(KaaTcpExceptionHandler.class);

    @Override
    public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
            throws Exception {
        LOG.error("Exception caught", cause);
        DisconnectReason reason = null;
        if (cause instanceof BadRequestException) {
            reason = DisconnectReason.BAD_REQUEST;
        } else {
            reason = DisconnectReason.INTERNAL_ERROR;
        }
        Disconnect message = new Disconnect(reason);
        ChannelFuture future = ctx.writeAndFlush(message.getFrame().array());
        future.addListener(ChannelFutureListener.CLOSE);
        ctx.close();
    }
}
