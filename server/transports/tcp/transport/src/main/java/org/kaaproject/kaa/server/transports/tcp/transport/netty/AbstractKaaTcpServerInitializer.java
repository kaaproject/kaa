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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.Attribute;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KaaTcpInitializer Class.
 * Used to initialize Netty Server.
 *
 * @author Yaroslav Zeygerman
 */
public abstract class AbstractKaaTcpServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaTcpServerInitializer.class);

    /**
     * KaaTcpServerInitializer constructor.
     */
    public AbstractKaaTcpServerInitializer() {
        super();
    }

    /**
     * init() method for necessary initializations.
     * @throws Exception - initialization exceptions
     */
    public void init() throws Exception { //NOSONAR
        LOG.debug("Default Server Initializer Init() started: ");
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline p = ch.pipeline();

        final UUID uuid = UUID.randomUUID();

        LOG.debug("KaaTcpServerInitializer Initializing Channel {} connection from {}:{}"
                , uuid, ch.remoteAddress().getAddress().toString(), ch.remoteAddress().getPort());

        Attribute<UUID> uuidAttr = ch.attr(AbstractNettyServer.UUID_KEY);
        uuidAttr.set(uuid);

        p.addLast("binaryDecoder", new ByteArrayDecoder());
        p.addLast("kaaTcpDecoder", getDecoder());
        p.addLast("binaryEncoder", new ByteArrayEncoder());
        p.addLast("kaaTcpEncoder", new KaaTcpEncoder());
        p.addLast("mainHandler", getMainHandler(uuid));
        p.addLast("kaaTcpExceptionHandler", new KaaTcpExceptionHandler());
    }

    protected abstract KaaTcpDecoder getDecoder();
    
    protected abstract SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> getMainHandler(UUID uuid);
}
