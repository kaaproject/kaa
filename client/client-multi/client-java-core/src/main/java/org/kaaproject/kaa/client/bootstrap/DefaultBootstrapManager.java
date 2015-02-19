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

package org.kaaproject.kaa.client.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.GenericTransportInfo;
import org.kaaproject.kaa.client.channel.KaaInternalChannelManager;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link BootstrapManager} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultBootstrapManager implements BootstrapManager {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapManager.class);

    private BootstrapTransport transport;
    private List<ProtocolMetaData> operationsServerList;
    private KaaInternalChannelManager channelManager;
    private Integer serverToApply;
    private final Map<TransportProtocolId, List<ProtocolMetaData>> mappedOperationServerList = new HashMap<TransportProtocolId, List<ProtocolMetaData>>();
    private final Map<TransportProtocolId, Iterator<ProtocolMetaData>> mappedIterators = new HashMap<>();

    public DefaultBootstrapManager(BootstrapTransport transport) {
        this.transport = transport;
    }

    @Override
    public void receiveOperationsServerList() throws TransportException {
        LOG.debug("Going to invoke sync method of assigned transport");
        transport.sync();
    }

    @Override
    public void useNextOperationsServer(TransportProtocolId transportId) {
        if (mappedOperationServerList != null && !mappedOperationServerList.isEmpty()) {
            if (!mappedIterators.get(transportId).hasNext()) {
                transport.sync();
            } else {
                channelManager.onTransportConnectionInfoUpdated(new GenericTransportInfo(ServerType.OPERATIONS, mappedIterators.get(transportId).next()));
            }
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
    }

    @Override
    public synchronized void setTransport(BootstrapTransport transport) {
        this.transport = transport;
    }

    @Override
    public synchronized void useNextOperationsServerByAccessPointId(int accessPointId) {
        List<ProtocolMetaData> servers = getTransportsByAccessPointId(accessPointId);
        if (servers != null && servers.size() > 0) {
            notifyChannelManangerAboutServer(servers);
        } else {
            serverToApply = accessPointId;
            transport.sync();
        }
    }

    private void notifyChannelManangerAboutServer(List<ProtocolMetaData> transports) {
        for (ProtocolMetaData transport : transports) {
            LOG.debug("Applying new transport {}", transports);
            channelManager.onTransportConnectionInfoUpdated(new GenericTransportInfo(ServerType.OPERATIONS, transport));
        }
    }

    private List<ProtocolMetaData> getTransportsByAccessPointId(int accessPointId) {
        if (operationsServerList == null || operationsServerList.isEmpty()) {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
        List<ProtocolMetaData> result = new ArrayList<ProtocolMetaData>();
        for (ProtocolMetaData transport : operationsServerList) {
            if (transport.getAccessPointId().intValue() == accessPointId) {
                result.add(transport);
            }
        }
        return result;
    }

    @Override
    public synchronized void setChannelManager(KaaInternalChannelManager manager) {
        this.channelManager = manager;
    }

    @Override
    public synchronized void onProtocolListUpdated(List<ProtocolMetaData> list) {
        operationsServerList = list;
        mappedOperationServerList.clear();
        mappedIterators.clear();
        if (operationsServerList != null && !operationsServerList.isEmpty()) {
            for (ProtocolMetaData server : operationsServerList) {
                TransportProtocolId transportId = new TransportProtocolId(server.getProtocolVersionInfo().getId(), server.getProtocolVersionInfo().getVersion());
                List<ProtocolMetaData> servers = mappedOperationServerList.get(transportId);
                if (servers == null) {
                    servers = new LinkedList<>();
                    mappedOperationServerList.put(transportId, servers);
                }
                servers.add(server);
            }
            for (Map.Entry<TransportProtocolId, List<ProtocolMetaData>> entry : mappedOperationServerList.entrySet()) {
                Collections.shuffle(entry.getValue());
                mappedIterators.put(entry.getKey(), entry.getValue().iterator());
            }
            if (serverToApply != null) {
                List<ProtocolMetaData> servers = getTransportsByAccessPointId(serverToApply);
                if (servers != null && servers.size() > 0) {
                    notifyChannelManangerAboutServer(servers);
                    serverToApply = null;
                }
            } else {
                for (Map.Entry<TransportProtocolId, Iterator<ProtocolMetaData>> entry : mappedIterators.entrySet()) {
                    TransportConnectionInfo info = new GenericTransportInfo(ServerType.OPERATIONS, entry.getValue().next());
                    channelManager.onTransportConnectionInfoUpdated(info);
                }
            }
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
    }
}
