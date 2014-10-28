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
package org.kaaproject.kaa.server.bootstrap.service.tcp;

import java.io.IOException;

import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.server.bootstrap.service.config.KaaTcpServiceChannelConfig;
import org.kaaproject.kaa.server.bootstrap.service.config.BootstrapServerConfig;
import org.kaaproject.kaa.server.bootstrap.service.initialization.ServiceChannel;
import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater;
import org.kaaproject.kaa.server.common.server.kaatcp.NettyKaaTcpServer;
import org.kaaproject.kaa.server.common.server.statistics.StatisticsService;
import org.kaaproject.kaa.server.common.zk.ZkChannelException;
import org.kaaproject.kaa.server.common.zk.ZkChannelsUtils;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KaaTcpService Class. Control of KaaTcp Service operations.
 * @author Andrey Panasenko
 *
 */
public class KaaTcpService implements ServiceChannel, StatisticsNodeUpdater {
    
    private static final Logger LOG = LoggerFactory.getLogger(KaaTcpService.class);
    
    private BootstrapServerConfig opServerConfig;
    
    private KaaTcpServiceChannelConfig kaaTcpConfig;
    
    private KaaTcpServerInitializer initializer;

    /** The netty. */
    private final AbstractNettyServer netty;

    private int processedRequestCount = 0;

    private int registeredUsersCount = 0;

    private int deltaCalculationCount = 0;

    private long timeStarted = 0;
    
    private StatisticsService statService;
    /**
     * Default Constructor.
     * @param opServerConfig - BootstrapServerConfig common server configuration
     * @param kaaTcpConfig - KaaTcpServiceChannelConfig KaaTcp Channel specific configuration including communication parameters.
     * @param initializer - KaaTcpServerInitializer KaaTcp Service initializer class.
     */
    public KaaTcpService(BootstrapServerConfig opServerConfig, KaaTcpServiceChannelConfig kaaTcpConfig, KaaTcpServerInitializer initializer) {
        this.setOpServerConfig(opServerConfig);
        this.kaaTcpConfig = kaaTcpConfig;
        this.initializer = initializer;
        this.netty = new NettyKaaTcpServer(kaaTcpConfig, initializer);
        this.statService = new StatisticsService(getChannelType(), kaaTcpConfig, this);
        this.kaaTcpConfig.setSessionTrack(statService);
        LOG.info("KaaTcp netty starting: {}", netty.toString());
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.ServiceChannel#getChannelType()
     */
    @Override
    public ChannelType getChannelType() {
        return ChannelType.KAATCP;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.ServiceChannel#start()
     */
    @Override
    public void start() {
        netty.init();
        initializer.setExecutor(netty.getEventExecutorGroup());
        netty.start();
        statService.start();
        LOG.info("KaaTcp netty started");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.ServiceChannel#stop()
     */
    @Override
    public void stop() {
        statService.shutdown();
        netty.shutdown();
        netty.deInit();
        LOG.info("KaaTcp netty stoped");
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.bootstrap.service.initialization.ServiceChannel#getZkSupportedChannel()
     */
    @Override
    public SupportedChannel getZkSupportedChannel() throws ZkChannelException {
        ZkSupportedChannel zkChannel = new ZkSupportedChannel(
                ZkChannelsUtils.getZkChannelTypeFromChanneltype(getChannelType()),
                false,
                getZkCommunicationParameters(),
                getChannelStatistics());
        return new SupportedChannel(zkChannel);
    }

    /**
     * Return Channel load statistics
     * @return ZkKaaTcpStatistics - statistics of Channel load
     */
    private ZkKaaTcpStatistics getChannelStatistics() {
        return new ZkKaaTcpStatistics(
                new BaseStatistics(
                        processedRequestCount, 
                        registeredUsersCount, 
                        deltaCalculationCount, 
                        timeStarted));
    }

    /**
     * Return Channel communication parameters
     * @return ZkKaaTcpComunicationParameters Channel communication parameters
     */
    private ZkKaaTcpComunicationParameters getZkCommunicationParameters() {
        return new ZkKaaTcpComunicationParameters(
                new IpComunicationParameters(kaaTcpConfig.getBindInterface(), kaaTcpConfig.getPort()));
    }

    /**
     * BootstrapServerConfig getter.
     * @return BootstrapServerConfig the opServerConfig
     */
    public BootstrapServerConfig getOpServerConfig() {
        return opServerConfig;
    }

    /**
     * BootstrapServerConfig setter.
     * @param opServerConfig the opServerConfig to set
     */
    public void setOpServerConfig(BootstrapServerConfig opServerConfig) {
        this.opServerConfig = opServerConfig;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater#setStatistics(int, int, int)
     */
    @Override
    public void setStatistics(int averageProcessedRequests, int averageOnlineSessions, int averageDeltaSync) {
        processedRequestCount = Integer.valueOf(averageProcessedRequests);
        registeredUsersCount = Integer.valueOf(averageOnlineSessions);
        deltaCalculationCount = Integer.valueOf(averageDeltaSync);
        timeStarted = System.currentTimeMillis();
        if (getOpServerConfig().getBootstrapNode() != null) {
            try {
                getOpServerConfig().getBootstrapNode().updateNodeStatsValues(
                        ZkChannelsUtils.getZkChannelTypeFromChanneltype(getChannelType()), 
                        averageDeltaSync, 
                        averageProcessedRequests, 
                        averageOnlineSessions);
            } catch (IOException e) {
                LOG.error("Error update statistics for channel "+getChannelType(), e);
            } catch (ZkChannelException e) {
                LOG.error("Error update statistics for channel "+getChannelType(), e);
            }            
        } else {
            LOG.error("Error update statistics for channel "+getChannelType()+ " BootstrapNode not set.");
        }
        
    }

}
