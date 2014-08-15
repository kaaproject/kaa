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

package org.kaaproject.kaa.server.operations.service.tcp;

import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpServerInitializer;
import org.kaaproject.kaa.server.common.server.kaatcp.NettyKaaTcpServer;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.operations.service.config.KaaTcpServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.netty.NettyService;


/**
 * The Class HttpService.
 */
public class KaaTcpService  extends NettyService{

    private final KaaTcpServiceChannelConfig conf;
    /**
     * Instantiates a new protocol service.
     *
     * @param conf
     *            the conf
     */
    public KaaTcpService(OperationsServerConfig operationServerConfig, KaaTcpServiceChannelConfig conf, AbstractKaaTcpServerInitializer initializer) {
        super(new NettyKaaTcpServer(conf, initializer), conf, operationServerConfig);
        this.conf = conf;
    }

    @Override
    protected IpComunicationParameters getIpCommunicationParameters() {
        return new IpComunicationParameters(conf.getBindInterface(), conf.getPort());
    }
}
