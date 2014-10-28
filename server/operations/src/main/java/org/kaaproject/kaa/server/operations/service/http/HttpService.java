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

import java.io.IOException;

import org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater;
import org.kaaproject.kaa.server.common.server.http.DefaultHttpServerInitializer;
import org.kaaproject.kaa.server.common.server.http.NettyHttpServer;
import org.kaaproject.kaa.server.common.server.statistics.StatisticsService;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.ZkChannelException;
import org.kaaproject.kaa.server.common.zk.ZkChannelsUtils;
import org.kaaproject.kaa.server.operations.service.config.HttpServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.netty.NettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class HttpService.
 */
public class HttpService extends NettyService implements StatisticsNodeUpdater{

    private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
    
    private final HttpServiceChannelConfig conf;
    
    private StatisticsService statService;
    /**
     * Instantiates a new protocol service.
     *
     * @param conf
     *            the conf
     */
    public HttpService(OperationsServerConfig operationServerConfig, HttpServiceChannelConfig conf, DefaultHttpServerInitializer initializer) {
        super(new NettyHttpServer(conf, initializer), conf, operationServerConfig);
        this.conf = conf;
        statService = new StatisticsService(conf.getChannelType(), conf, this);
        conf.setSessionTrack(statService);
    }

    /**
     * @return
     */
    protected IpComunicationParameters getIpCommunicationParameters() {
        return new IpComunicationParameters(conf.getBindInterface(), conf.getPort());
    }
    

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.netty.NettyService#stop()
     */
    @Override
    public void stop() {
        super.stop();
        statService.shutdown();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater#setStatistics(int, int, int)
     */
    @Override
    public void setStatistics(int averageProcessedRequests, int averageOnlineSessions, int averageDeltaSync) {
        if (getOperationServerConfig().getOperationsNode() != null) {
            try {
                getOperationServerConfig().getOperationsNode().updateNodeStatsValues(
                        ZkChannelsUtils.getZkChannelTypeFromChanneltype(conf.getChannelType()), 
                        averageDeltaSync, 
                        averageProcessedRequests, 
                        averageOnlineSessions);
            } catch (IOException e) {
                LOG.error("Error update statistics for channel "+conf.getChannelType(), e);
            } catch (ZkChannelException e) {
                LOG.error("Error update statistics for channel "+conf.getChannelType(), e);
            }            
        } else {
            LOG.error("Error update statistics for channel "+conf.getChannelType()+ " OperationsNode not set.");
        }
        
    }
}
