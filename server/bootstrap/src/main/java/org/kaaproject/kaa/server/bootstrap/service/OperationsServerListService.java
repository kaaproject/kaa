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

package org.kaaproject.kaa.server.bootstrap.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.HttpChannel;
import org.kaaproject.kaa.common.channels.HttpLongPollChannel;
import org.kaaproject.kaa.common.channels.communication.HttpLongPollParameters;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftCommunicationParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftSupportedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OperationsServerListService Class.
 * Receive new Operations Servers list form Thrift service and create AVRP object EndPointServerList
 *
 * @author Andrey Panasenko
 */
public class OperationsServerListService {
    private static final Logger LOG = LoggerFactory.getLogger(OperationsServerListService.class);
    private final BootstrapConfig config;
    private List<OperationsServer> serverList;

    /** Avro object representing OperationsServer list */
    OperationsServerList list;

    /**
     * Default constructor.
     * @param config BootstrapConfig
     */
    public OperationsServerListService(BootstrapConfig config) {
        this.config = config;
        serverList = new LinkedList<OperationsServer>();
        updateEPSL();
    }

    /**
     * Initialization of OperationsServerList Service,
     * collects server list and register listener on ZK nodes changes.
     */
    public void init() {
        LOG.info("Initializing OperationsServer List service...");
        if (config.getBootstrapNode() != null) {
            LOG.trace("Operations Server List service bootstrap ZK node set.");
        }
    }

    /**
     * Deinitialization of OperationsServerList Service,
     * unregister ZK listener.
     */
    public void deinit() {
    }

    /**
     * EndPointServerList getter.
     * @return EndPointServerList
     */
    public OperationsServerList getOpsServerList() {
        return list;
    }

    /**
     * Update Operations Server list from Map received from Thrift service.
     * @param operationsServersList Map<String, endpointServer>, key - DNS name of Operations Server
     */
    public void updateList(List<ThriftOperationsServer> operationsServersList) {
        
        LinkedList<OperationsServer> newList = new LinkedList<OperationsServer>();
        
        for(ThriftOperationsServer server : operationsServersList) {
            
                OperationsServer eps = new OperationsServer();
                List<SupportedChannel> supportedChannels = new ArrayList<>();
                
                for(ThriftSupportedChannel thriftSupChannel : server.getSupportedChannels()) {
                    ThriftCommunicationParameters thriftParams = thriftSupChannel.getCommunicationParams();
                    switch (thriftSupChannel.getType()) {
                    case HTTP:
                        HttpParameters httpParams = new HttpParameters();
                        httpParams.setHostName(thriftParams.getHttpParams().getHostName());
                        httpParams.setPort(thriftParams.getHttpParams().getPort());
                        supportedChannels.add(HttpChannel.getSupportedChannelFromHttpParameters(httpParams));
                        break;
                    case HTTP_LP:
                        HttpLongPollParameters httpLpParams = new HttpLongPollParameters();
                        httpLpParams.setHostName(thriftParams.getHttpLpParams().getHostName());
                        httpLpParams.setPort(thriftParams.getHttpLpParams().getPort());
                        supportedChannels.add(HttpLongPollChannel.getSupportedChannelFromHttpLongPollParameters(httpLpParams));
                        break;
                    }
                    eps.setName(server.getName());
                    eps.setSupportedChannelsArray(supportedChannels);
                    eps.setPriority(server.getPriority());
                    eps.setPublicKey(ByteBuffer.wrap(server.getPublicKey()));
                    newList.add(eps);
                }
            
        }
        if (!newList.isEmpty()) {
            serverList = newList;
        }
        updateEPSL();
    }

    /**
     * Create EndPointServerList from List of server.
     */
    private void updateEPSL() {
        list = new OperationsServerList(serverList);
    }
}
