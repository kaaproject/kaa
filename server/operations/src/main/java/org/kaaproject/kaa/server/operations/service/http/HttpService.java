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

import org.kaaproject.kaa.server.common.server.http.DefaultHttpServerInitializer;
import org.kaaproject.kaa.server.common.server.http.NettyHttpServer;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.operations.service.config.HttpServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.netty.NettyService;


/**
 * The Class HttpService.
 */
public class HttpService extends NettyService{

    private final HttpServiceChannelConfig conf;
    /**
     * Instantiates a new protocol service.
     *
     * @param conf
     *            the conf
     */
    public HttpService(OperationsServerConfig operationServerConfig, HttpServiceChannelConfig conf, DefaultHttpServerInitializer initializer) {
        super(new NettyHttpServer(conf, initializer), conf, operationServerConfig);
        this.conf = conf;
    }

    /**
     * @return
     */
    protected IpComunicationParameters getIpCommunicationParameters() {
        return new IpComunicationParameters(conf.getBindInterface(), conf.getPort());
    }
}
