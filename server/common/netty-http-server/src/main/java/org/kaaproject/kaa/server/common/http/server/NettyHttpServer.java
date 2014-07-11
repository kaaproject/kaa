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

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;

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
public class NettyHttpServer extends Thread {

    public static final AttributeKey<UUID> UUID_KEY = new AttributeKey<>(ConfigConst.UUID_KEY);
    public static final AttributeKey<Track> TRACK_KEY = new AttributeKey<>(ConfigConst.TRACK_KEY);

    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bServer;
    private EventExecutorGroup executor;
    private Channel bindChannel;

    private final Config conf;

    private String bindAddress;

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
    public NettyHttpServer(Config conf) {
        this.conf = conf;
        LOG.trace("NettyHttpServer config:\n{}", conf.toString());
    }

    /**
     * Netty HTTP server initialization.
     */
    public void init() {
        try {
            LOG.info("NettyHttpServer Initializing...");
            bossGroup = new NioEventLoopGroup();

            LOG.debug("NettyHttpServer bossGroup created");
            workerGroup = new NioEventLoopGroup();
            LOG.debug("NettyHttpServer workGroup created");
            bServer = new ServerBootstrap();
            LOG.debug("NettyHttpServer ServerBootstrap created");
            executor = new DefaultEventExecutorGroup(conf.getExecutorThreadSize());
            LOG.debug("NettyHttpServer Task Executor created");
            DefaultServerInitializer sInit = NettyHttpServer
                    .initClassFromConfig(conf);
            LOG.debug("NettyHttpServer InitClass instance created");

            sInit.setConf(conf);
            sInit.setExecutor(executor);
            sInit.init();
            sInit.setServer(this);
            LOG.debug("NettyHttpServer InitClass instance init()");
            bServer.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class).childHandler(sInit)
                    .option(ChannelOption.SO_REUSEADDR, true);
            LOG.debug("NettyHttpServer ServerBootstrap group initialized");
            bindChannel = bServer.bind(conf.getBindInterface(), conf.getPort()).sync().channel();
            bindAddress = conf.getBindInterface();
        } catch (Exception e) {
            LOG.error("NettyHttpServer init() failed",e);
        }
    }

    @Override
    public void run() {
        LOG.info("NettyHttpServer starting...");
        try {
            bindChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error("NettyHttpServer error", e);
        } finally {
            shutdown();
            LOG.info("NettyHttpServer shut down");
        }
    }

    /**
     * Netty HTTP server shutdown.
     */
    public void shutdown() {
        LOG.info("NettyHttpServer stopping...");
        if (bossGroup != null) {
            try {
                Future<? extends Object> f = bossGroup.shutdownGracefully(250, 1000, TimeUnit.MILLISECONDS);
                f.await();
            } catch (InterruptedException e) {
                LOG.trace("NettyHttpServer stopping: bossGroup error", e);
            } finally {
                bossGroup = null;
                LOG.trace("NettyHttpServer stopping: bossGroup stoped");
            }
        }
        if (workerGroup != null) {
            try {
                Future<? extends Object> f = workerGroup.shutdownGracefully(250, 1000, TimeUnit.MILLISECONDS);
                f.await();
            } catch (InterruptedException e) {
                LOG.trace("NettyHttpServer stopping: workerGroup error", e);
            } finally {
                workerGroup = null;
                LOG.trace("NettyHttpServer stopping: workerGroup stopped");
            }
        }
        if (executor != null) {
            try {
                Future<? extends Object> f = executor.shutdownGracefully(250, 1000, TimeUnit.MILLISECONDS);
                f.await();
            } catch (InterruptedException e) {
                LOG.trace("NettyHttpServer stopping: task executor error", e);
            } finally {
                executor = null;
                LOG.trace("NettyHttpServer stopping: task executor stopped.");
            }
        }
    }

    /**
     * Netty HTTP server deinitialization.
     */
    public void deInit() {
        LOG.info("NettyHttpServer deInitializing...");
    }

    /**
     * Heller static method to get ServerInitializer instance from config.
     * @param conf Config
     * @return DefaultServerInitializer
     * @throws ClassNotFoundException - if specified in Config class not found.
     * @throws IllegalAccessException - other reflection exceptions.
     * @throws InstantiationException - other reflection exceptions.
     */
    private static DefaultServerInitializer initClassFromConfig(Config conf)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOG.trace("NettyHttpServer initClassFromConfig class: {}", conf.getServerInitializerClass());
        Class<?> c = null;
        c = Class.forName(conf.getServerInitializerClass());
        Object obj = c.newInstance();
        if (obj instanceof DefaultServerInitializer) {
            LOG.trace("NettyHttpServer initClassFromConfig class {} created", conf.getServerInitializerClass());
            return (DefaultServerInitializer) obj;
        }
        throw new InstantiationException("Class " + conf.getServerInitializerClass()
                + " is not an instanceof DefaultServerInitializer");
    }

    /**
     * Getter for real HTTP server bind IP address.
     * @return the bindAddress
     */
    public String getBindAddress() {
        return bindAddress;
    }
}
