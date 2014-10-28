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

import java.io.IOException;

import org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpServerInitializer;
import org.kaaproject.kaa.server.common.server.kaatcp.NettyKaaTcpServer;
import org.kaaproject.kaa.server.common.server.statistics.StatisticsService;
import org.kaaproject.kaa.server.common.zk.ZkChannelException;
import org.kaaproject.kaa.server.common.zk.ZkChannelsUtils;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.operations.service.config.KaaTcpServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.netty.NettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class HttpService.
 */
public class KaaTcpService  extends NettyService implements StatisticsNodeUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(KaaTcpService.class);
    
    private final KaaTcpServiceChannelConfig conf;
    
    private StatisticsService statService;
    /**
     * Instantiates a new protocol service.
     *
     * @param conf
     *            the conf
     */
    public KaaTcpService(OperationsServerConfig operationServerConfig, KaaTcpServiceChannelConfig conf, AbstractKaaTcpServerInitializer initializer) {
        super(new NettyKaaTcpServer(conf, initializer), conf, operationServerConfig);
        this.conf = conf;
        statService = new StatisticsService(conf.getChannelType(), conf, this);
        conf.setSessionTrack(statService);
    }

    @Override
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
