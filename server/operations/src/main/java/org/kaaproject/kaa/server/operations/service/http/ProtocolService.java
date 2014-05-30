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

package org.kaaproject.kaa.server.operations.service.http;

import org.kaaproject.kaa.server.common.http.server.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class ProtocolService.
 */
public class ProtocolService {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(ProtocolService.class);

    /** The netty. */
    private NettyHttpServer netty;

    /** The conf. */
    private OperationsServerConfig conf;

    /**
     * Instantiates a new protocol service.
     * 
     * @param conf
     *            the conf
     */
    public ProtocolService(OperationsServerConfig conf) {
        this.conf = conf;
        netty = new NettyHttpServer(conf);
    }

    /**
     * Start.
     */
    public void start() {
        netty.init();
        netty.start();
    }

    /**
     * Stop.
     */
    public void stop() {
        netty.shutdown();
        netty.deInit();
    }

    /**
     * @return the netty
     */
    public NettyHttpServer getNetty() {
        return netty;
    }
}
