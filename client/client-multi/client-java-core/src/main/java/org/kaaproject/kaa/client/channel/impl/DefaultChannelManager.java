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

package org.kaaproject.kaa.client.channel.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.AbstractServerInfo;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.connectivity.PingServerStorage;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelManager implements KaaChannelManager, PingServerStorage {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultChannelManager.class);

    private final List<KaaDataChannel> channels = new LinkedList<>();
    private final Map<TransportType, KaaDataChannel> upChannels = new HashMap<TransportType, KaaDataChannel>();
    private final BootstrapManager bootstrapManager;
    private final Map<ChannelType, ServerInfo> lastServers = new HashMap<>();

    private final Map<ChannelType, List<ServerInfo>> bootststrapServers;
    private final Map<ChannelType, ServerInfo> lastBSServers = new HashMap<>();

    private ConnectivityChecker connectivityChecker;
    
    public DefaultChannelManager(BootstrapManager manager, Map<ChannelType, List<ServerInfo>> bootststrapServers) {
        if (manager == null || bootststrapServers == null || bootststrapServers.isEmpty()) {
            throw new ChannelRuntimeException("Failed to create channel manager");
        }
        this.bootstrapManager = manager;
        this.bootststrapServers = bootststrapServers;
    }

    private void useNewChannelForType(TransportType type) {
        for (KaaDataChannel channel : channels) {
            ChannelDirection direction = channel.getSupportedTransportTypes().get(type);
            if (direction != null &&
                    (direction.equals(ChannelDirection.BIDIRECTIONAL) || direction.equals(ChannelDirection.UP))) {
                upChannels.put(type,  channel);
                return;
            }
        }
        upChannels.put(type, null);
    }

    private void useNewChannel(KaaDataChannel channel) {
        for (Map.Entry<TransportType, ChannelDirection> type : channel.getSupportedTransportTypes().entrySet()) {
            if (type.getValue().equals(ChannelDirection.UP)
                    || type.getValue().equals(ChannelDirection.BIDIRECTIONAL)) {
                upChannels.put(type.getKey(), channel);
            }
        }
    }

    @Override
    public synchronized void addChannel(KaaDataChannel channel) {
        if (!channels.contains(channel)) {
            channel.setConnectivityChecker(connectivityChecker);
            channels.add(channel);
            ServerInfo server;
            if (channel.getServerType() == ServerType.BOOTSTRAP) {
                server = getCurrentBootstrapServer(channel.getType());
            } else {
                server = lastServers.get(channel.getType());
            }
            if (server != null) {
                LOG.debug("Applying server {} for channel [{}] type {}", server, channel.getId(), channel.getType());
                channel.setServer(server);
            } else {
                if (lastServers != null && lastServers.isEmpty()) {
                    LOG.warn("Failed to find server for channel [{}] type {}", channel.getId(), channel.getType());
                } else {
                    LOG.debug("list of servers is empty for channel [{}] type {}", channel.getId(), channel.getType());
                }
            }
            useNewChannel(channel);
        }
    }

    @Override
    public synchronized void removeChannel(KaaDataChannel channel) {
        channels.remove(channel);
        for (Map.Entry<TransportType, KaaDataChannel> entry : upChannels.entrySet()) {
            if (entry.getValue() == channel) {
                useNewChannelForType(entry.getKey());
            }
        }
    }

    @Override
    public synchronized List<KaaDataChannel> getChannels() {
        return new LinkedList<>(channels);
    }

    @Override
    public synchronized List<KaaDataChannel> getChannelsByType(ChannelType type) {
        List<KaaDataChannel> result = new LinkedList<>();
        for (KaaDataChannel channel : channels) {
            if (channel.getType().equals(type)) {
                result.add(channel);
            }
        }
        return result;
    }

    @Override
    public synchronized KaaDataChannel getChannelByTransportType(TransportType type) {
        return upChannels.get(type);
    }

    @Override
    public synchronized KaaDataChannel getChannel(String id) {
        for (KaaDataChannel channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public synchronized void onServerUpdated(ServerInfo newServer) {
        if (newServer.getServerType() == ServerType.OPERATIONS) {
            lastServers.put(newServer.getChannelType(), newServer);
        }

        for (KaaDataChannel channel : channels) {
            if (channel.getServerType() == newServer.getServerType()
                    && channel.getType() == newServer.getChannelType())
            {
                LOG.debug("Applying server {} for channel [{}] type {}"
                        , newServer, channel.getId(), channel.getType());
                channel.setServer(newServer);
            }
        }
    }

    @Override
    public synchronized void onServerFailed(ServerInfo server) {
        if (server.getServerType() == ServerType.BOOTSTRAP) {
            onServerUpdated(getNextBootstrapServer(server));
        } else {
            bootstrapManager.useNextOperationsServer(server.getChannelType());
        }
    }

    @Override
    public synchronized void clearChannelList() {
        channels.clear();
        upChannels.clear();
    }

    private ServerInfo getCurrentBootstrapServer(ChannelType type) {
        ServerInfo bsi = lastBSServers.get(type);
        if (bsi == null) {
            List<ServerInfo> serverList = bootststrapServers.get(type);
            if (serverList != null && !serverList.isEmpty()) {
                bsi = serverList.get(0);
                lastBSServers.put(type, bsi);
            }
        }

        return bsi;
    }

    private ServerInfo getNextBootstrapServer(ServerInfo currentServer) {
        ServerInfo bsi = null;

        List<ServerInfo> serverList =
                bootststrapServers.get(currentServer.getChannelType());
        int serverIndex = serverList.indexOf(currentServer);

        if (serverIndex >= 0) {
            if (++serverIndex == serverList.size()) {
                serverIndex = 0;
            }
            bsi = serverList.get(serverIndex);
            lastBSServers.put(currentServer.getChannelType(), bsi);
        }

        return bsi;
    }
    
    @Override
    public void setConnectivityChecker(ConnectivityChecker checker) {
        connectivityChecker = checker;
        for (KaaDataChannel channel : channels) {
            channel.setConnectivityChecker(connectivityChecker);
        }
    }

    @Override
    public AbstractServerInfo getCurrentPingServer() {
        //TODO Modify algorithm for more extended
        return (AbstractServerInfo)lastBSServers.values().iterator().next();
    }
}
