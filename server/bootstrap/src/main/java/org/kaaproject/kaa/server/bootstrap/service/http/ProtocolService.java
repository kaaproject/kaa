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

package org.kaaproject.kaa.server.bootstrap.service.http;

import org.kaaproject.kaa.server.common.server.http.DefaultHttpServerInitializer;
import org.kaaproject.kaa.server.common.server.http.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProtocolService class.
 * Used to start and stop Bootstrap HTTP service.
 *
 * @author Andrey Panasenko
 */
public class ProtocolService {

    /** The Constant logger */
    private static final Logger LOG = LoggerFactory
            .getLogger(ProtocolService.class);

    /** Netty server instance */
    private NettyHttpServer netty;
    private final BootstrapConfig config;

    /**
     * ProtocolService constructor
     * @param config BootstrapConfig
     */
    public ProtocolService(BootstrapConfig config, DefaultHttpServerInitializer serverInitializer) {
        this.config = config;
        this.netty = new NettyHttpServer(this.config, serverInitializer);
    }

    /**
     * Start.
     */
    public void start() {
        LOG.trace("Bootstrap protocol service: starting Netty...");
        netty.init();
        netty.start();
    }

    /**
     * Stop.
     */
    public void stop() {
        LOG.trace("Bootstrap protocol service, stopping Netty...");
        netty.shutdown();
        netty.deInit();
    }

    /**
     * Netty getter.
     * @return the netty NettyHttpServer
     */
    public NettyHttpServer getNetty() {
        return netty;
    }

    /**
     * Netty setter.
     * @param netty the netty to set
     */
    public void setNetty(NettyHttpServer netty) {
        this.netty = netty;
    }
}
