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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
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
     * @param endpointMap Map<String, endpointServer>, key - DNS name of Operations Server
     */
    public void updateList(Map<String, org.kaaproject.kaa.server.common.thrift.gen.bootstrap.OperationsServer> endpointMap) {
        LinkedList<OperationsServer> newList = new LinkedList<OperationsServer>();
        for(String server : endpointMap.keySet()) {
            if (endpointMap.get(server) != null) {
                OperationsServer eps = new OperationsServer();
                eps.setDNSName(server);
                eps.setPriority(endpointMap.get(server).getPriority());
                eps.setPublicKey(ByteBuffer.wrap(endpointMap.get(server).getPublicKey()));
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
