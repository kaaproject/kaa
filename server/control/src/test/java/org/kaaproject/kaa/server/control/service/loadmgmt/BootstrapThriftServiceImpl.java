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
package org.kaaproject.kaa.server.control.service.loadmgmt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPLPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftSupportedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Panasenko
 *
 */
public class BootstrapThriftServiceImpl extends BaseCliThriftService implements BootstrapThriftService.Iface {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(BootstrapThriftServiceImpl.class);
    
    private Map<String, OperationsServer> opServerMap;
    
    private Object sync;
    
    public BootstrapThriftServiceImpl() {
        opServerMap = null;
        sync = new Object();
    }
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService#getServerShortName()
     */
    @Override
    protected String getServerShortName() {
        return "TestBootstrap";
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService#initServiceCommands()
     */
    @Override
    protected void initServiceCommands() {
        
    }

    public Map<String, OperationsServer> getOperatonsServerMap() {
        synchronized (sync) {
            if (opServerMap == null) {
                try {
                    sync.wait(60000);
                } catch (InterruptedException e) {
                    
                }
            }
        }
        
        return opServerMap;
    }
    
    public void reset() {
        synchronized (sync) {
            opServerMap = null;
            sync.notify();
        }
    }
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService.Iface#onOperationsServerListUpdate(java.util.List)
     */
    @Override
    public void onOperationsServerListUpdate(List<ThriftOperationsServer> operationsServersList) throws TException {
        synchronized (sync) {
            if (operationsServersList.size() > 0 
                    && opServerMap == null) {
                opServerMap = new HashMap<String, OperationsServer>();
            
                for(ThriftOperationsServer thriftServer : operationsServersList) {
                    OperationsServer opServer = new OperationsServer(
                            thriftServer.getName(), 
                            thriftServer.getPriority(), 
                            ByteBuffer.wrap(thriftServer.getPublicKey()), 
                            transfromThriftSupportedChannels(thriftServer.getSupportedChannels()));
                    LOG.info("onOperationsServerListUpdate: ThriftOperationsServer {} ",thriftServer.toString());
                    LOG.info("onOperationsServerListUpdate: OperationsServer {} ",opServer.toString());
                    opServerMap.put(thriftServer.getName(), opServer );
                }
            
                sync.notify();
            }
        }
    }
    /**
     * @param supportedChannels
     * @return
     */
    private List<SupportedChannel> transfromThriftSupportedChannels(List<ThriftSupportedChannel> supportedChannels) {
        List<SupportedChannel> suppChannels = new ArrayList<>();
        for(ThriftSupportedChannel thriftSuppChannel : supportedChannels) {
            ChannelType channelType = ChannelType.HTTP;
            Object communicationParameters = null;
            switch (thriftSuppChannel.getType()) {
            case HTTP:
                channelType = ChannelType.HTTP;
                communicationParameters = new HTTPComunicationParameters(
                        thriftSuppChannel.getCommunicationParams().getHttpParams().getHostName(),
                        thriftSuppChannel.getCommunicationParams().getHttpParams().getPort());
                break;
            case HTTP_LP:
                channelType = ChannelType.HTTP_LP;
                communicationParameters = new HTTPLPComunicationParameters(
                        thriftSuppChannel.getCommunicationParams().getHttpLpParams().getHostName(),
                        thriftSuppChannel.getCommunicationParams().getHttpLpParams().getPort());
                break;
            default:
                break;
            }
            if (communicationParameters != null) {
                SupportedChannel channel = new SupportedChannel(channelType, communicationParameters);
                suppChannels.add(channel);
            }
        }
        return suppChannels;
    }
}
