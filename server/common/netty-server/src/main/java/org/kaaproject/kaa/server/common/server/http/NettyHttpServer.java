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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.server.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyHttpServer Class.
 * Used to start Netty server.
 * Config is used to handle netty server configuration.
 * Usage:
 * netty = new NettyHttpServer(conf);
 * netty.init();
 * netty.start();
 *
 * To stop Netty:
 * netty.shutdown();
 * netty.DeInit();
 *
 * @author Andrey Panasenko
 */
public class NettyHttpServer extends AbstractNettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);
    private final Config conf;
    private final DefaultHttpServerInitializer initializer;

    /**
     * Config getter.
     * @return Config
     */
    public Config getConf() {
        return conf;
    }

    /**
     * NettyHttpServer constructor.
     * @param conf Config
     */
    public NettyHttpServer(Config conf, DefaultHttpServerInitializer initializer) {
        super(conf.getBindInterface(), conf.getPort(), conf.getExecutorThreadSize());
        this.conf = conf;
        this.initializer = initializer;
        LOG.trace("NettyHttpServer config:\n{}", conf.toString());
    }

    @Override
    protected ChannelInitializer<SocketChannel> configureInitializer() throws Exception {
        initializer.setConf(conf);
        initializer.setExecutor(getEventExecutorGroup());
        initializer.init();
        return initializer;
    }
}
