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
import org.kaaproject.kaa.client.channel.HttpLongPollServerInfo;
import org.kaaproject.kaa.client.channel.HttpServerInfo;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaTcpServerInfo;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.Channel;
import org.kaaproject.kaa.common.channels.ChannelFactory;
import org.kaaproject.kaa.common.channels.HttpChannel;
import org.kaaproject.kaa.common.channels.HttpLongPollChannel;
import org.kaaproject.kaa.common.channels.KaaTcpChannel;
import org.kaaproject.kaa.common.channels.ParsingException;
import org.kaaproject.kaa.common.channels.communication.HttpLongPollParameters;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;
import org.kaaproject.kaa.common.channels.communication.KaaTcpParameters;
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
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultBootstrapManager.class);

    private BootstrapTransport transport;
    private List<OperationsServer> operationsServerList;
    private KaaChannelManager channelManager;
    private String serverToApply;
    private final Map<ChannelType, List<OperationsServer>> mappedOperationServerList = new HashMap<ChannelType, List<OperationsServer>>();
    private final Map<ChannelType, Iterator<OperationsServer>> mappedIterators = new HashMap<>();

    public DefaultBootstrapManager(BootstrapTransport transport) {
        this.transport = transport;
    }

    private static ServerInfo getSeverInfoByChannel(OperationsServer server, SupportedChannel supportedChannel) throws ParsingException, NoSuchAlgorithmException, InvalidKeySpecException {
        Channel channel = ChannelFactory.getChannelFromSupportedChannel(supportedChannel);
        ServerInfo info;
        switch(channel.getChannelType()) {
            case HTTP:
                HttpParameters httpParams = HttpChannel.getHttpParametersFromSupportedChannel(supportedChannel);
                info = new HttpServerInfo(ServerType.OPERATIONS, httpParams.getHostName(), httpParams.getPort(), server.getPublicKey().array());
                break;
            case HTTP_LP:
                HttpLongPollParameters longPollParams = HttpLongPollChannel.getHttpLongPollParametersFromSupportedChannel(supportedChannel);
                info = new HttpLongPollServerInfo(ServerType.OPERATIONS, longPollParams.getHostName(), longPollParams.getPort(), server.getPublicKey().array());
                break;
            case KAATCP:
                KaaTcpParameters tcpParams = KaaTcpChannel.getKaaTcpParametersFromSupportedChannel(supportedChannel);
                info = new KaaTcpServerInfo(ServerType.OPERATIONS, tcpParams.getHostName(), tcpParams.getPort(), server.getPublicKey().array());
                break;
            default:
                throw new BootstrapRuntimeException("Invalid type");
        }
        return info;
    }

    private ServerInfo getServerInfoByType(OperationsServer server, ChannelType type) {
        List<SupportedChannel> supportedChannelList = server.getSupportedChannelsArray();
        for (SupportedChannel supportedChannel : supportedChannelList) {
            if (supportedChannel.getChannelType().equals(type)) {
                try {
                    return getSeverInfoByChannel(server, supportedChannel);
                } catch (ParsingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                    throw new BootstrapRuntimeException(e.getMessage());
                }
            }
        }
        return null;
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
    public void receiveOperationsServerList() throws TransportException {
        LOG.debug("Going to invoke sync method of assigned transport");
        transport.sync();
    }

    @Override
    public void useNextOperationsServer(ChannelType type) {
        if (mappedOperationServerList != null && !mappedOperationServerList.isEmpty()) {
            if (!mappedIterators.get(type).hasNext()) {
                transport.sync();
            } else {
                channelManager.onServerUpdated(getServerInfoByType(mappedIterators.get(type).next(), type));
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
    public synchronized List<OperationsServer> getOperationsServerList() {
        return operationsServerList != null ? new ArrayList<OperationsServer>(operationsServerList) : null;
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

    @Override
    public synchronized void setChannelManager(KaaChannelManager manager) {
        this.channelManager = manager;
    }

    @Override
    public synchronized void onServerListUpdated(OperationsServerList list) {
        operationsServerList = list.getOperationsServerArray();
        mappedOperationServerList.clear();
        mappedIterators.clear();
        if (operationsServerList != null && !operationsServerList.isEmpty()) {
            for (OperationsServer server : operationsServerList) {
                for (SupportedChannel supportedChannel : server.getSupportedChannelsArray()) {
                    try {
                        Channel channel = ChannelFactory.getChannelFromSupportedChannel(supportedChannel);
                        List<OperationsServer> servers = mappedOperationServerList.get(channel.getChannelType());
                        if (servers == null) {
                            servers = new LinkedList<>();
                            mappedOperationServerList.put(channel.getChannelType(), servers);
                        }
                        servers.add(server);
                    } catch (ParsingException e) {
                        throw new BootstrapRuntimeException(e.getMessage());
                    }
                }
            }
            for (Map.Entry<ChannelType, List<OperationsServer>> entry : mappedOperationServerList.entrySet()) {
                Collections.shuffle(entry.getValue());
                Collections.sort(entry.getValue(), new Comparator<OperationsServer>() {
                        @Override
                        public int compare(OperationsServer o1, OperationsServer o2) {
                            return o1.getPriority().compareTo(o2.getPriority());
                        }
                    }
                );
                mappedIterators.put(entry.getKey(), entry.getValue().iterator());
            }
            if (serverToApply != null) {
                OperationsServer server = getServerByName(serverToApply);
                if (server != null) {
                    notifyChannelManangerAboutServer(server);
                    serverToApply = null;
                }
            } else {
                for (Map.Entry<ChannelType, Iterator<OperationsServer>> entry : mappedIterators.entrySet()) {
                    ServerInfo info = getServerInfoByType(entry.getValue().next(), entry.getKey());
                    channelManager.onServerUpdated(info);
                }
            }
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
    }
}
