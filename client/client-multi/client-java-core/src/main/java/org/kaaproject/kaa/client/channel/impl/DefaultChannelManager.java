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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.BootstrapServerInfo;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.HttpLongPollServerInfo;
import org.kaaproject.kaa.client.channel.HttpServerInfo;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelManager implements KaaChannelManager {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultChannelManager.class);

    private final List<KaaDataChannel> channels = new LinkedList<>();
    private final Map<TransportType, KaaDataChannel> upChannels = new HashMap<TransportType, KaaDataChannel>();
    private final BootstrapManager bootstrapManager;
    private final Map<ChannelType, ServerInfo> lastServers = new HashMap<>();
    private final List<BootstrapServerInfo> bootststrapServers;
    private Iterator<BootstrapServerInfo> bootststrapServersIterator;
    private BootstrapServerInfo lastBootstrapServer;

    public DefaultChannelManager(BootstrapManager manager, List<BootstrapServerInfo> bootststrapServers) {
        if (manager == null || bootststrapServers == null || bootststrapServers.isEmpty()) {
            throw new ChannelRuntimeException("Failed to create channel manager");
        }
        this.bootstrapManager = manager;
        this.bootststrapServers = bootststrapServers;
        this.bootststrapServersIterator = bootststrapServers.iterator();
        this.lastBootstrapServer = bootststrapServersIterator.next();
        LOG.info("Initialized channel manager with bootstrap servers: {}", bootststrapServers);
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
            channels.add(channel);
            ServerInfo server;
            if (channel.getType().equals(ChannelType.BOOTSTRAP)) {
                server = lastBootstrapServer;
            } else {
                server = lastServers.get(channel.getType());
            }
            if (server != null) {
                LOG.debug("Applying server {} for channel \"{}\" type {}", server, channel.getId(), channel.getType());
                channel.setServer(server);
            } else {
                if (lastServers != null && lastServers.isEmpty()) {
                    LOG.warn("Failed to find server for channel \"{}\" type {}", channel.getId(), channel.getType());
                } else {
                    LOG.debug("list of servers is empty for channel \"{}\" type {}", channel.getId(), channel.getType());
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

    private static ChannelType getTypeByServerInfo(ServerInfo info) {
        if (info instanceof HttpLongPollServerInfo) {
            return ChannelType.HTTP_LP;
        }
        if (info instanceof HttpServerInfo) {
            return ChannelType.HTTP;
        }
        if (info instanceof BootstrapServerInfo) {
            return ChannelType.BOOTSTRAP;
        }
        return null;
    }

    @Override
    public synchronized void onServerUpdated(ServerInfo newServer) {
        ChannelType typeToVerify = getTypeByServerInfo(newServer);
        lastServers.put(typeToVerify, newServer);
        for (KaaDataChannel channel : channels) {
            if (channel.getType().equals(typeToVerify)) {
                LOG.debug("Applying server {} for channel \"{}\" type {}", newServer, channel.getId(), channel.getType());
                channel.setServer(newServer);
            }
        }
    }

    @Override
    public synchronized void onServerFailed(ServerInfo server) {
        if (server instanceof BootstrapServerInfo) {
            if (!bootststrapServersIterator.hasNext()) {
                bootststrapServersIterator = bootststrapServers.iterator();
            }
            lastBootstrapServer = bootststrapServersIterator.next();
            onServerUpdated(lastBootstrapServer);
        } else {
            ChannelType typeToVerify = getTypeByServerInfo(server);
            bootstrapManager.useNextOperationsServer(typeToVerify);
        }
    }

    @Override
    public synchronized void clearChannelList() {
        channels.clear();
        upChannels.clear();
    }

}
