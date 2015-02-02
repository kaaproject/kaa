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
import org.kaaproject.kaa.client.channel.IPTransportInfo;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaInvalidChannelException;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.connectivity.PingServerStorage;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelManager implements KaaChannelManager, PingServerStorage {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultChannelManager.class);

    private final List<KaaDataChannel> channels = new LinkedList<>();
    private final Map<TransportType, KaaDataChannel> upChannels = new HashMap<TransportType, KaaDataChannel>();
    private final BootstrapManager bootstrapManager;
    private final Map<TransportProtocolId, TransportConnectionInfo> lastServers = new HashMap<>();

    private final Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers;
    private final Map<TransportProtocolId, TransportConnectionInfo> lastBSServers = new HashMap<>();

    private ConnectivityChecker connectivityChecker;
    private boolean isShutdown = false;
    private boolean isPaused = false;

    public DefaultChannelManager(BootstrapManager manager, Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers) {
        if (manager == null || bootststrapServers == null || bootststrapServers.isEmpty()) {
            throw new ChannelRuntimeException("Failed to create channel manager");
        }
        this.bootstrapManager = manager;
        this.bootststrapServers = bootststrapServers;
    }

    private boolean useChannelForType(KaaDataChannel channel, TransportType type) {
        ChannelDirection direction = channel.getSupportedTransportTypes().get(type);
        if (direction != null &&
                (direction.equals(ChannelDirection.BIDIRECTIONAL) || direction.equals(ChannelDirection.UP))) {
            upChannels.put(type,  channel);
            return true;
        }
        return false;
    }

    private void useNewChannelForType(TransportType type) {
        for (KaaDataChannel channel : channels) {
            if (useChannelForType(channel, type)) {
                return;
            }
        }
        upChannels.put(type, null);
    }

    private void applyNewChannel(KaaDataChannel channel) {
        for (TransportType type : channel.getSupportedTransportTypes().keySet()) {
            useChannelForType(channel, type);
        }
    }

    private void replaceAndRemoveChannel(KaaDataChannel channel) {
        channels.remove(channel);
        for (Map.Entry<TransportType, KaaDataChannel> entry : upChannels.entrySet()) {
            if (entry.getValue() == channel) {
                useNewChannelForType(entry.getKey());
            }
        }
        channel.shutdown();
    }

    private void addChannelToList(KaaDataChannel channel) {
        if (!channels.contains(channel)) {
            channel.setConnectivityChecker(connectivityChecker);
            channels.add(channel);
            TransportConnectionInfo server;
            if (channel.getServerType() == ServerType.BOOTSTRAP) {
                server = getCurrentBootstrapServer(channel.getTransportProtocolId());
            } else {
                server = lastServers.get(channel.getTransportProtocolId());
            }
            if (server != null) {
                LOG.debug("Applying server {} for channel [{}] type {}", server, channel.getId(), channel.getTransportProtocolId());
                channel.setServer(server);
            } else {
                if (lastServers != null && lastServers.isEmpty()) {
                    LOG.warn("Failed to find server for channel [{}] type {}", channel.getId(), channel.getTransportProtocolId());
                } else {
                    LOG.debug("list of servers is empty for channel [{}] type {}", channel.getId(), channel.getTransportProtocolId());
                }
            }
        }
    }

    @Override
    public synchronized void setChannel(TransportType transport,
            KaaDataChannel channel) throws KaaInvalidChannelException {
        if (isShutdown) {
            LOG.warn("Can't set a channel. Channel manager is down");
            return;
        }
        if (channel != null) {
            if (!useChannelForType(channel, transport)) {
                throw new KaaInvalidChannelException(
                        "Unsupported transport type " + transport.toString()
                                + " for channel \"" + channel.getId() + "\"");
            }
            if (isPaused) {
                channel.pause();
            }
            addChannelToList(channel);
        }
    }

    @Override
    public synchronized void addChannel(KaaDataChannel channel) {
        if (isShutdown) {
            LOG.warn("Can't add a channel. Channel manager is down");
            return;
        }
        if (channel != null) {
            if (isPaused) {
                channel.pause();
            }
            addChannelToList(channel);
            applyNewChannel(channel);
        }
    }

    @Override
    public synchronized void removeChannel(KaaDataChannel channel) {
        replaceAndRemoveChannel(channel);
    }

    @Override
    public synchronized void removeChannel(String id) {
        for (KaaDataChannel channel : channels) {
            if (channel.getId().equals(id)) {
                replaceAndRemoveChannel(channel);
                return;
            }
        }
    }

    @Override
    public synchronized List<KaaDataChannel> getChannels() {
        return new LinkedList<>(channels);
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
    public synchronized void onTransportConnectionInfoUpdated(TransportConnectionInfo newServer) {
        if (isShutdown) {
            LOG.warn("Can't process server update. Channel manager is down");
            return;
        }
        if (newServer.getServerType() == ServerType.OPERATIONS) {
            lastServers.put(newServer.getTransportId(), newServer);
        }

        for (KaaDataChannel channel : channels) {
            if (channel.getServerType() == newServer.getServerType()
                    && channel.getTransportProtocolId().equals(newServer.getTransportId()))
            {
                LOG.debug("Applying server {} for channel [{}] type {}"
                        , newServer, channel.getId(), channel.getTransportProtocolId());
                channel.setServer(newServer);
            }
        }
    }

    @Override
    public synchronized void onServerFailed(TransportConnectionInfo server) {
        if (isShutdown) {
            LOG.warn("Can't process server failure. Channel manager is down");
            return;
        }
        if (server.getServerType() == ServerType.BOOTSTRAP) {
            onTransportConnectionInfoUpdated(getNextBootstrapServer(server));
        } else {
            bootstrapManager.useNextOperationsServer(server.getTransportId());
        }
    }

    @Override
    public synchronized void clearChannelList() {
        channels.clear();
        upChannels.clear();
    }

    private TransportConnectionInfo getCurrentBootstrapServer(TransportProtocolId type) {
        TransportConnectionInfo bsi = lastBSServers.get(type);
        if (bsi == null) {
            List<TransportConnectionInfo> serverList = bootststrapServers.get(type);
            if (serverList != null && !serverList.isEmpty()) {
                bsi = serverList.get(0);
                lastBSServers.put(type, bsi);
            }
        }

        return bsi;
    }

    private TransportConnectionInfo getNextBootstrapServer(TransportConnectionInfo currentServer) {
        TransportConnectionInfo bsi = null;

        List<TransportConnectionInfo> serverList =
                bootststrapServers.get(currentServer.getTransportId());
        int serverIndex = serverList.indexOf(currentServer);

        if (serverIndex >= 0) {
            if (++serverIndex == serverList.size()) {
                serverIndex = 0;
            }
            bsi = serverList.get(serverIndex);
            lastBSServers.put(currentServer.getTransportId(), bsi);
        }

        return bsi;
    }

    @Override
    public void setConnectivityChecker(ConnectivityChecker checker) {
        if (isShutdown) {
            LOG.warn("Can't set connectivity checker. Channel manager is down");
            return;
        }
        connectivityChecker = checker;
        for (KaaDataChannel channel : channels) {
            channel.setConnectivityChecker(connectivityChecker);
        }
    }

    @Override
    public IPTransportInfo getCurrentPingServer() {
        //TODO Modify algorithm for more extended
        return (IPTransportInfo)lastBSServers.values().iterator().next();
    }

    @Override
    public synchronized void shutdown() {
        if (!isShutdown) {
            isShutdown = true;
            for (KaaDataChannel channel : channels) {
                channel.shutdown();
            }
        }
    }

    @Override
    public synchronized void pause() {
        if (isShutdown) {
            LOG.warn("Can't pause. Channel manager is down");
            return;
        }
        if (!isPaused) {
            isPaused = true;
            for (KaaDataChannel channel : upChannels.values()) {
                channel.pause();
            }
        }
    }

    @Override
    public synchronized void resume() {
        if (isShutdown) {
            LOG.warn("Can't resume. Channel manager is down");
            return;
        }
        if (isPaused) {
            isPaused = false;
            for (KaaDataChannel channel : upChannels.values()) {
                channel.resume();
            }
        }
    }

}
