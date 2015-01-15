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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.GenericTransportInfo;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportId;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.channels.ParsingException;
import org.kaaproject.kaa.common.channels.communication.HttpLongPollParameters;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;
import org.kaaproject.kaa.common.channels.communication.KaaTcpParameters;
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
    private KaaChannelManager channelManager;
    private String serverToApply;
    private final Map<TransportId, List<ProtocolMetaData>> mappedOperationServerList = new HashMap<TransportId, List<ProtocolMetaData>>();
    private final Map<TransportId, Iterator<ProtocolMetaData>> mappedIterators = new HashMap<>();

    public DefaultBootstrapManager(BootstrapTransport transport) {
        this.transport = transport;
    }

    @Override
    public void receiveOperationsServerList() throws TransportException {
        LOG.debug("Going to invoke sync method of assigned transport");
        transport.sync();
    }

    @Override
    public void useNextOperationsServer(TransportId transportId) {
        if (mappedOperationServerList != null && !mappedOperationServerList.isEmpty()) {
            if (!mappedIterators.get(transportId).hasNext()) {
                transport.sync();
            } else {
                channelManager.onServerUpdated(new GenericTransportInfo(ServerType.OPERATIONS, mappedIterators.get(transportId).next()));
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
    public synchronized void useNextOperationsServerByDnsName(String name) {
        if (name != null) {
            OperationsServer server = getServerByName(name);
            if (server != null) {
                notifyChannelManangerAboutServer(server);
            } else {
                serverToApply = name;
                transport.sync();
            }
        }
    }

    private void notifyChannelManangerAboutServer(OperationsServer server) {
        List<SupportedChannel> supportedChannelList = server.getSupportedChannelsArray();
        for (SupportedChannel supportedChannel : supportedChannelList) {
            try {
                LOG.debug("Applying new server {}", server);
                channelManager.onServerUpdated(getSeverInfoByChannel(server, supportedChannel));
            } catch (ParsingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new BootstrapRuntimeException(e.getMessage());
            }
        }
    }

    private OperationsServer getServerByName(String name) {
        if (operationsServerList != null && !operationsServerList.isEmpty()) {
            for (OperationsServer server : operationsServerList) {
                if (server.getName().equals(name)) {
                    return server;
                }
            }
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
        return null;
    }

    @Override
    public synchronized void setChannelManager(KaaChannelManager manager) {
        this.channelManager = manager;
    }

    @Override
    public synchronized void onProtocolListUpdated(List<ProtocolMetaData> list) {
        operationsServerList = list;
        mappedOperationServerList.clear();
        mappedIterators.clear();
        if (operationsServerList != null && !operationsServerList.isEmpty()) {
            for (ProtocolMetaData server : operationsServerList) {
                TransportId transportId = new TransportId(server.getProtocolId(), server.getProtocolVersion());
                List<ProtocolMetaData> servers = mappedOperationServerList.get(transportId);
                if (servers == null) {
                    servers = new LinkedList<>();
                    mappedOperationServerList.put(transportId, servers);
                }
                servers.add(server);
            }
            for (Map.Entry<TransportId, List<ProtocolMetaData>> entry : mappedOperationServerList.entrySet()) {
                Collections.shuffle(entry.getValue());
                mappedIterators.put(entry.getKey(), entry.getValue().iterator());
            }
            if (serverToApply != null) {
                OperationsServer server = getServerByName(serverToApply);
                if (server != null) {
                    notifyChannelManangerAboutServer(server);
                    serverToApply = null;
                }
            } else {
                for (Map.Entry<TransportId, Iterator<ProtocolMetaData>> entry : mappedIterators.entrySet()) {
                    ServerInfo info = new GenericTransportInfo(ServerType.OPERATIONS, entry.getValue().next());
                    channelManager.onServerUpdated(info);
                }
            }
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
    }
}
