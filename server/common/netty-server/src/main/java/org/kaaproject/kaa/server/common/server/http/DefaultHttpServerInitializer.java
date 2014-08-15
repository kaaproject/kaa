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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.Attribute;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.UUID;

import org.kaaproject.kaa.server.common.server.CommandFactory;
import org.kaaproject.kaa.server.common.server.Config;
import org.kaaproject.kaa.server.common.server.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultServerInitializer Class.
 * Used to initialize Netty Server.
 *
 * @author Andrey Panasenko
 */
public class DefaultHttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpServerInitializer.class);

    private Config conf;
    private EventExecutorGroup executor;
    private CommandFactory commandFactory;

    /**
     * Config getter.
     * @return Config
     */
    public Config getConf() {
        return conf;
    }

    /**
     * Config setter.
     * @param conf Config
     */
    public void setConf(Config conf) {
        this.conf = conf;
    }

    /**
     * EventExecutorGroup setter.
     * @param executor EventExecutorGroup
     */
    public void setExecutor(EventExecutorGroup executor) {
        this.executor = executor;
    }

    /**
     * DefaultServerInitializer constructor.
     */
    public DefaultHttpServerInitializer() {

    }

    /**
     * init() method for necessary initializations.
     * @throws Exception - initialization exceptions
     */
    public void init() throws Exception { //NOSONAR
        LOG.info("Default Server Initializer Init() started: ");
        commandFactory = new CommandFactory(getConf().getCommandList());
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline p = ch.pipeline();

        final UUID uuid = UUID.randomUUID();

        LOG.info("DefaultServerInitializer Initializing Channel {} connection from {}:{}"
                , uuid, ch.remoteAddress().getAddress().toString(), ch.remoteAddress().getPort());

        Attribute<UUID> uuidAttr = ch.attr(NettyHttpServer.UUID_KEY);
        uuidAttr.set(uuid);

        if (conf.getSessionTrack() != null) {
            Track track = conf.getSessionTrack().newSession(uuid);
            Attribute<Track> trackAttr = ch.attr(NettyHttpServer.TRACK_KEY);
            trackAttr.set(track);
        }

        p.addLast("httpDecoder", new HttpRequestDecoder());
        p.addLast("httpAggregator", new HttpObjectAggregator(conf.getClientMaxBodySize()));
        p.addLast("httpDecoderAux", new RequestDecoder(commandFactory));
        p.addLast("httpEncoder", new HttpResponseEncoder());
        p.addLast("httpEncoderAux", new ResponseEncoder());
        p.addLast("handler", getMainHandler(uuid));
        p.addLast("httpExceptionHandler", new DefaultExceptionHandler());

        ChannelFuture closeFuture = ch.closeFuture();
        closeFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

            @Override
            public void operationComplete(Future<? super Void> future)
                    throws Exception {
                if (conf.getSessionTrack() != null) {
                    conf.getSessionTrack().closeSession(uuid);
                }
            }

        });
    }

    protected SimpleChannelInboundHandler<CommandProcessor> getMainHandler(UUID uuid){
        return new DefaultHandler(executor);
    }
}
