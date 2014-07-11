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

import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * DefaultServerInitializer Class.
 * Used to initialize Netty Server.
 *
 * @author Andrey Panasenko
 */
public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultServerInitializer.class);

    private Config conf;
    private EventExecutorGroup executor;
    private NettyHttpServer server;


    /**
     * NettyHttpServer getter.
     * @return NettyHttpServer
     */

    public NettyHttpServer getServer() {
        return server;
    }

    /**
     * NettyHttpServer setter
     * @param server NettyHttpServer
     */
    public void setServer(NettyHttpServer server) {
        this.server = server;
    }

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
     * EventExecutorGroup getter.
     * @return EventExecutorGroup
     */
    public EventExecutorGroup getExecutor() {
        return executor;
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
    public DefaultServerInitializer() {

    }

    /**
     * DefaultServerInitializer constructor.
     * @param conf Config
     * @param executor EventExecutorGroup
     */
    public DefaultServerInitializer(Config conf, EventExecutorGroup executor) {
        this.conf = conf;
        this.executor = executor;
    }

    /**
     * init() method for necessary initializations.
     * @throws Exception - initialization exceptions
     */
    public void init() throws Exception { //NOSONAR
        LOG.info("Default Server Initializer Init() started: ");
        for(String commandClass : getConf().getCommandList()){
            CommandFactory.addCommandClass(commandClass);
        }
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
        p.addLast("httpAggregator",
                new HttpObjectAggregator(conf.getClientMaxBodySize()));

        avroCommandsInit(p);
        p.addLast("httpDecoderAux", new RequestDecoder(server));
        p.addLast("httpEncoder", new HttpResponseEncoder());
        p.addLast("httpEncoderAux", new ResponseEncoder());
        p.addLast("handler", getMainHandler(uuid));
        avroHandlerInit(p);
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

    /**
     * Special Avro initializations
     * @param p ChannelPipeline
     */
    protected void avroCommandsInit(ChannelPipeline p) {

    }

    /**
     * Special Avro handler initializations.
     * @param p ChannelPipeline
     */
    protected void avroHandlerInit(ChannelPipeline p) {

    }

    protected SimpleChannelInboundHandler<CommandProcessor> getMainHandler(UUID uuid){
        return new DefaultHandler(executor);
    }
}
