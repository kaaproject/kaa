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

package org.kaaproject.kaa.server.common.http.server;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RequestDecoder Class.
 * ChannelInboundHandler for HttpObject in Netty pipeline.
 * On channelRead0() check HTTP METHOD POST, and find CommandProcessor for
 * HTTP request URI. Then pass CommandProcessor to next handler in pipeline.
 *
 * @author Andrey Panasenko
 */
public class RequestDecoder extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger LOG = LoggerFactory.getLogger(RequestDecoder.class);

    private final NettyHttpServer server;

    /**
     * RequestDecoder constructor.
     * @param server NettyHttpServer
     */
    public RequestDecoder(NettyHttpServer server) {
        super();
        this.server = server;
    }

    /**
     * channelReadCompete
     * @param ctx ChannelHandlerContext
     * @throws Exception exception on error
     */
    public void channelReadCompete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject)
            throws Exception {

        DecoderResult result = httpObject.getDecoderResult();
        if (!result.isSuccess()) {
            throw new BadRequestException(result.cause());
        }

        Attribute<UUID> sessionUuidAttr = ctx.channel().attr(NettyHttpServer.UUID_KEY);
        Attribute<Track> sessionTrackAttr = ctx.channel().attr(NettyHttpServer.TRACK_KEY);

        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            LOG.trace("Session: {} got valid HTTP request:\n{}"
                    , sessionUuidAttr.get().toString(), httpRequest.headers().toString());
            if (httpRequest.getMethod().equals(HttpMethod.POST)) {
                String uri = httpRequest.getUri();
                CommandProcessor cp = CommandFactory.getCommandProcessor(uri);
                if (sessionTrackAttr.get() != null) {
                    int id = sessionTrackAttr.get().newRequest(cp.getName());
                    cp.setCommandId(id);
                }
                cp.setSessionUuid(sessionUuidAttr.get());
                cp.setHttpRequest(httpRequest);
                cp.setServer(server);
                cp.parse();
                ctx.fireChannelRead(cp);
            } else {
                LOG.error("Got invalid HTTP method: expecting only POST");
                throw new BadRequestException("Incorrect method "
                        + httpRequest.getMethod().toString() + ", expected POST");
            }
        }
    }
}
