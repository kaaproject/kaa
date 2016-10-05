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

package org.kaaproject.kaa.server.common.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>NettyHttpServer Class. Used to start Netty server. Config is used to handle
 * netty server configuration. Usage: netty = new NettyHttpServer(conf);
 * netty.init(); netty.start();</p>
 *
 * <p>To stop Netty: netty.shutdown();</p>
 *
 * @author Yaroslav Zeygerman
 */
public abstract class AbstractNettyServer extends Thread {

  public static final AttributeKey<UUID> UUID_KEY = AttributeKey.valueOf("UUID");

  private static final Logger LOG = LoggerFactory.getLogger(AbstractNettyServer.class);
  private final String bindAddress;
  private final int bindPort;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private ServerBootstrap btsServer;
  private Channel bindChannel;

  /**
   * NettyHttpServer constructor.
   *
   * @param bindAddress bind address
   * @param port        bind port
   */
  public AbstractNettyServer(String bindAddress, int port) {
    this.bindAddress = bindAddress;
    this.bindPort = port;
  }

  protected abstract ChannelInitializer<SocketChannel> configureInitializer() throws Exception;

  /**
   * Netty HTTP server initialization.
   */
  public void init() {
    try {
      LOG.info("NettyServer Initializing...");
      bossGroup = new NioEventLoopGroup();

      LOG.debug("NettyServer bossGroup created");
      workerGroup = new NioEventLoopGroup();
      LOG.debug("NettyServer workGroup created");
      btsServer = new ServerBootstrap();
      LOG.debug("NettyServer ServerBootstrap created");
      ChannelInitializer<SocketChannel> serverInit = configureInitializer();
      LOG.debug("NettyServer InitClass instance created");

      LOG.debug("NettyServer InitClass instance init()");
      btsServer.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(serverInit)
          .option(ChannelOption.SO_REUSEADDR, true);
      LOG.debug("NettyServer ServerBootstrap group initialized");
      bindChannel = btsServer.bind(bindAddress, bindPort).sync().channel();
    } catch (Exception exception) {
      LOG.error("NettyHttpServer init() failed", exception);
    }
  }

  @Override
  public void run() {
    LOG.info("NettyHttpServer starting...");
    try {
      bindChannel.closeFuture().sync();
    } catch (InterruptedException exption) {
      LOG.error("NettyHttpServer error", exption);
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
        Future<? extends Object> future = bossGroup.shutdownGracefully(
            250, 1000, TimeUnit.MILLISECONDS);
        future.await();
      } catch (InterruptedException exception) {
        LOG.trace("NettyHttpServer stopping: bossGroup error", exception);
      } finally {
        bossGroup = null;
        LOG.trace("NettyHttpServer stopping: bossGroup stoped");
      }
    }
    if (workerGroup != null) {
      try {
        Future<? extends Object> future = workerGroup.shutdownGracefully(
            250, 1000, TimeUnit.MILLISECONDS);
        future.await();
      } catch (InterruptedException exception) {
        LOG.trace("NettyHttpServer stopping: workerGroup error", exception);
      } finally {
        workerGroup = null;
        LOG.trace("NettyHttpServer stopping: workerGroup stopped");
      }
    }
  }
}
