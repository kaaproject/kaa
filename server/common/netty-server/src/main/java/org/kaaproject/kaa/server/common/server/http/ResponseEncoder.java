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

package org.kaaproject.kaa.server.common.server.http;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.Attribute;


/**
 * ResponseEncoder Class.
 * Used to convert CommandProcressor after execution into HTTP response
 * and set Netty channel to close in case it indicated by CommandProcessor.
 *
 * @author Andrey Panasenko
 */
public class ResponseEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestDecoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg,
            ChannelPromise promise) throws Exception {
        Attribute<UUID> sessionUuidAttr = ctx.channel().attr(NettyHttpServer.UUID_KEY);

        if (!(msg instanceof CommandProcessor)) {
            LOG.warn("Session [{}] got invalid HTTP response: {}", sessionUuidAttr, msg);
            super.write(ctx, msg, promise);
            return;
        }else{
            LOG.trace("Session [{}] got valid HTTP response: {}", sessionUuidAttr, msg);
        }

        CommandProcessor cp = (CommandProcessor) msg;

        Attribute<Track> sessionTrackAttr = ctx.channel().attr(NettyHttpServer.TRACK_KEY);

        HttpResponse httpResponse = cp.getResponse();

        if (sessionTrackAttr.get() != null) {
            sessionTrackAttr.get().setProcessTime(cp.getCommandId(), cp.getSyncTime());
            sessionTrackAttr.get().closeRequest(cp.getCommandId());
        }

        ChannelFuture future = ctx.writeAndFlush(httpResponse, promise);
        if (!HttpHeaders.isKeepAlive(httpResponse)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
