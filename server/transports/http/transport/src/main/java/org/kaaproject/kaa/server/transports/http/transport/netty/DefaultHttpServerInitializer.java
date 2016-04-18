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

package org.kaaproject.kaa.server.transports.http.transport.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.Attribute;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultServerInitializer Class. Used to initialize Netty Server.
 *
 * @author Andrey Panasenko
 */
public abstract class DefaultHttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpServerInitializer.class);

    /**
     * DefaultServerInitializer constructor.
     */
    public DefaultHttpServerInitializer() {
        super();
    }

    /**
     * init() method for necessary initializations.
     * 
     * @throws Exception
     *             - initialization exceptions
     */
    public void init() throws Exception { // NOSONAR
        LOG.info("Default Server Initializer Init() started");
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline p = ch.pipeline();

        final UUID uuid = UUID.randomUUID();

        LOG.info("DefaultServerInitializer Initializing Channel {} connection from {}:{}", uuid,
                ch.remoteAddress().getAddress().toString(), ch.remoteAddress().getPort());

        Attribute<UUID> uuidAttr = ch.attr(AbstractNettyServer.UUID_KEY);
        uuidAttr.set(uuid);

        p.addLast("httpDecoder", new HttpRequestDecoder());
        p.addLast("httpAggregator", new HttpObjectAggregator(getClientMaxBodySize()));
        p.addLast("httpDecoderAux", getRequestDecoder());
        p.addLast("httpEncoder", new HttpResponseEncoder());
        p.addLast("httpEncoderAux", new ResponseEncoder());
        p.addLast("handler", getMainHandler(uuid));
        p.addLast("httpExceptionHandler", new DefaultExceptionHandler());
    }

    protected abstract int getClientMaxBodySize();

    protected abstract ChannelHandler getRequestDecoder();
    
    protected abstract ChannelHandler getMainHandler(UUID uuid);
}
