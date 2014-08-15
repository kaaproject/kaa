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

package org.kaaproject.kaa.server.common.server.kaatcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.server.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyKaaTcpServer Class.
 *
 * @author Yaroslav Zeygerman
 */
public class NettyKaaTcpServer extends AbstractNettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(NettyKaaTcpServer.class);
    private final Config conf;
    private final AbstractKaaTcpServerInitializer initializer;

    /**
     * Config getter.
     * @return Config
     */
    public Config getConf() {
        return conf;
    }

    /**
     * NettyKaaTcpServer constructor.
     * @param conf Config
     */
    public NettyKaaTcpServer(Config conf, AbstractKaaTcpServerInitializer initializer) {
        super(conf.getBindInterface(), conf.getPort(), conf.getExecutorThreadSize());
        this.conf = conf;
        this.initializer = initializer;
        LOG.trace("NettyKaaTcpServer config:\n{}", conf.toString());
    }

    @Override
    protected ChannelInitializer<SocketChannel> configureInitializer() throws Exception {
        initializer.setConf(conf);
        initializer.init();
        return initializer;
    }

}
