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
package org.kaaproject.kaa.server.operations.service.netty;

import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.zk.ZkChannelException;
import org.kaaproject.kaa.server.common.zk.ZkChannelsUtils;
import org.kaaproject.kaa.server.common.zk.gen.BaseStatistics;
import org.kaaproject.kaa.server.common.zk.gen.IpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkSupportedChannel;
import org.kaaproject.kaa.server.operations.service.bootstrap.ServiceChannel;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.config.ServiceChannelConfig;

public abstract class NettyService implements ServiceChannel{

    private static final boolean REDIRECTION_SUPPORTED = true;

    /** The netty. */
    private final AbstractNettyServer netty;

    private final ServiceChannelConfig conf;

    private final OperationsServerConfig operationServerConfig;

    protected NettyService(AbstractNettyServer netty, ServiceChannelConfig conf, OperationsServerConfig operationServerConfig) {
        super();
        this.netty = netty;
        this.conf = conf;
        this.operationServerConfig = operationServerConfig;
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.ServiceChannel#start()
     */
    @Override
    public void start() {
        netty.init();
        netty.start();
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.ServiceChannel#stop()
     */
    @Override
    public void stop() {
        netty.shutdown();
        netty.deInit();
    }

    /**
     * @return the netty
     */
    public AbstractNettyServer getNetty() {
        return netty;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.ServiceChannel#getChannelType()
     */
    @Override
    public ChannelType getChannelType() {
        return conf.getChannelType();
    }

    /**
     * @return the redirection supported
     */
    public static boolean isRedirectionsupported() {
        return REDIRECTION_SUPPORTED;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.bootstrap.ServiceChannel#getZkSupportedChannel()
     */
    @Override
    public SupportedChannel getZkSupportedChannel() throws ZkChannelException {

        ZkSupportedChannel zkChannel = new ZkSupportedChannel(
                ZkChannelsUtils.getZkChannelTypeFromChanneltype(getChannelType()),
                isRedirectionsupported(),
                getZkCommunicationParameters(),
                getChannelStatistics());
        return new SupportedChannel(zkChannel);
    }

    /**
     * @return
     * @throws ZkChannelException
     */
    private Object getChannelStatistics() throws ZkChannelException {
        switch (getChannelType()) {
        case HTTP:
            return getZkHttpStatistics();
        case HTTP_LP:
            return getZkHttpLpStatistics();
        case KAATCP:
            return getZkKaaTcpStatistics();
        default:
            throw new ZkChannelException("Error returnig statistics parameteres, assigned unsupported channel type");
        }
    }

    /**
     * @return
     */
    private ZkKaaTcpStatistics getZkKaaTcpStatistics() {
        return new ZkKaaTcpStatistics(getBasicStatistics());
    }

    /**
     * @return
     */
    private ZkHttpLpStatistics getZkHttpLpStatistics() {
        return new ZkHttpLpStatistics(getBasicStatistics());
    }

    /**
     * @return
     */
    private BaseStatistics getBasicStatistics() {
        return new BaseStatistics(
                0, 0, 0, System.currentTimeMillis());
    }

    /**
     * @return
     */
    private ZkHttpStatistics getZkHttpStatistics() {
        return new ZkHttpStatistics(getBasicStatistics());
    }

    private Object getZkCommunicationParameters() throws ZkChannelException {
        switch (getChannelType()) {
        case HTTP:
            return getZkHttpComunicationParameters();
        case HTTP_LP:
            return getZkHttpLpComunicationParameters();
        case KAATCP:
            return getZkKaaTcpComunicationParameters();
        default:
            throw new ZkChannelException("Error returnig communication parameteres, assigned unsupported channel type");
        }
    }

    /**
     * @return
     */
    private ZkKaaTcpComunicationParameters getZkKaaTcpComunicationParameters() {
        return new ZkKaaTcpComunicationParameters(getIpCommunicationParameters());
    }


    /**
     * @return
     */
    private ZkHttpLpComunicationParameters getZkHttpLpComunicationParameters() {
        return new ZkHttpLpComunicationParameters(getIpCommunicationParameters());
    }

    /**
     * @return
     */
    private ZkHttpComunicationParameters getZkHttpComunicationParameters() {
        return new ZkHttpComunicationParameters(getIpCommunicationParameters());
    }

    abstract protected IpComunicationParameters getIpCommunicationParameters();

    /**
     * @return the operationServerConfig
     */
    public OperationsServerConfig getOperationServerConfig() {
        return operationServerConfig;
    }
}
