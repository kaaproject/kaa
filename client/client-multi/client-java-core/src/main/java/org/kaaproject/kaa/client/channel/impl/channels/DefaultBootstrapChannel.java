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

package org.kaaproject.kaa.client.channel.impl.channels;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.channel.BootstrapServerInfo;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBootstrapChannel implements KaaDataChannel {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultBootstrapChannel.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.BOOTSTRAP, ChannelDirection.BIDIRECTIONAL);
    }

    private static final String CHANNEL_ID = "default_bootstrap_channel";

    private AbstractHttpClient httpClient;
    private KaaDataDemultiplexer demultiplexer;
    private KaaDataMultiplexer multiplexer;

    private BootstrapServerInfo currentServer;
    private final AbstractKaaClient client;
    private boolean lastConnectionFailed = false;
    private volatile boolean isShutdown = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private class BootstrapRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (DefaultBootstrapChannel.this) {
                try {
                    processTypes(SUPPORTED_TYPES);
                    lastConnectionFailed = false;
                } catch (Exception e) {
                    LOG.error("Failed to receive operation servers list {}", e);
                    lastConnectionFailed = true;
                    client.getChannelMananager().onServerFailed(currentServer);
                }
            }
        }

    }

    public DefaultBootstrapChannel(AbstractKaaClient client) {
        this.client = client;
    }

    private void processTypes(Map<TransportType, ChannelDirection> types) throws Exception {
        byte [] requestBodyRaw = multiplexer.compileRequest(types);

        LinkedHashMap<String, byte[]> requestEntity = new LinkedHashMap<String, byte[]>();
        requestEntity.put(CommonBSConstants.APPLICATION_TOKEN_ATTR_NAME, requestBodyRaw);

        LOG.debug("Going to execute {}", requestEntity);
        byte[] responseDataRaw = httpClient.executeHttpRequest("", requestEntity);
        demultiplexer.processResponse(responseDataRaw);
    }

    @Override
    public synchronized void sync(TransportType type) {
        if (isShutdown) {
            LOG.info("Can't sync. Channel {} is down", getId());
            return;
        }
        LOG.info("Processing sync {} for channel {}", type, getId());
        if (multiplexer != null && demultiplexer != null) {
            if (type.equals(TransportType.BOOTSTRAP)) {
                executor.submit(new BootstrapRunnable());
            } else {
                LOG.error("Unsupported type {} for channel {}", type, getId());
            }
        } else{
            LOG.warn("Multiplexer {} or Demultiplexer {} is not set", multiplexer, demultiplexer);
        }
    }

    @Override
    public synchronized void syncAll() {
        if (isShutdown) {
            LOG.info("Can't sync. Channel {} is down", getId());
            return;
        }
        LOG.info("Processing sync all for channel {}", getId());
        if (multiplexer != null && demultiplexer != null) {
            executor.submit(new BootstrapRunnable());
        }
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public ChannelType getType() {
        return ChannelType.BOOTSTRAP;
    }

    @Override
    public synchronized void setDemultiplexer(KaaDataDemultiplexer demultiplexer) {
        if (demultiplexer != null) {
            this.demultiplexer = demultiplexer;
        }
    }

    @Override
    public synchronized void setMultiplexer(KaaDataMultiplexer multiplexer) {
        if (multiplexer != null) {
            this.multiplexer = multiplexer;
        }
    }

    @Override
    public synchronized void setServer(ServerInfo server) {
        if (isShutdown) {
            LOG.info("Can't set server. Channel {} is down", getId());
            return;
        }
        if (server != null) {
            this.currentServer = (BootstrapServerInfo) server;
            this.httpClient = client.createHttpClient(currentServer.getURL(), null, null, currentServer.getPublicKey());
            if (lastConnectionFailed) {
                lastConnectionFailed = false;
                syncAll();
            }
        }
    }

    public void shutdown() {
        isShutdown = true;
        executor.shutdownNow();
    }

    @Override
    public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
        return SUPPORTED_TYPES;
    }


}
