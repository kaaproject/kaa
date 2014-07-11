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
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.HttpServerInfo;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOperationHttpChannel implements KaaDataChannel {
    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultOperationsChannel.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.UP);
        SUPPORTED_TYPES.put(TransportType.LOGGING, ChannelDirection.UP);
    }

    private static final String CHANNEL_ID = "default_operations_http_channel";

    private AbstractHttpClient httpClient;
    private KaaDataDemultiplexer demultiplexer;
    private KaaDataMultiplexer multiplexer;

    private HttpServerInfo currentServer;
    private final AbstractKaaClient client;
    private final KaaClientState state;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean lastConnectionFailed = false;
    private volatile boolean isShutdown = false;

    private class OperationRunnable implements Runnable {

        private final Map<TransportType, ChannelDirection> typesToProcess;

        OperationRunnable(Map<TransportType, ChannelDirection> types) {
            this.typesToProcess = types;
        }

        @Override
        public void run() {
            synchronized (DefaultOperationHttpChannel.this) {
                try {
                    processTypes(typesToProcess);
                    lastConnectionFailed = false;
                } catch (Exception e) {
                    LOG.error("Failed to receive response from the operation {}", e);
                    lastConnectionFailed = true;
                    client.getChannelMananager().onServerFailed(currentServer);
                }
            }
        }
    }

    public DefaultOperationHttpChannel(AbstractKaaClient client, KaaClientState state) {
        this.client = client;
        this.state = state;
    }

    private void processTypes(Map<TransportType, ChannelDirection> types) throws Exception {
        byte[] requestBodyRaw = multiplexer.compileRequest(types);
        LinkedHashMap<String, byte[]> requestEntity = HttpRequestCreator.createOperationHttpRequest(requestBodyRaw, httpClient.getEncoderDecoder());
        byte [] responseDataRaw = httpClient.executeHttpRequest("", requestEntity);
        byte [] decodedResponse = httpClient.getEncoderDecoder().decodeData(responseDataRaw);
        demultiplexer.processResponse(decodedResponse);
    }

    @Override
    public synchronized void sync(TransportType type) {
        if (isShutdown) {
            LOG.info("Can't sync. Channel {} is down", getId());
            return;
        }
        LOG.info("Processing sync {} for channel {}", type, getId());
        if (multiplexer != null && demultiplexer != null) {
            if (currentServer != null) {
                ChannelDirection direction = SUPPORTED_TYPES.get(type);
                if (direction != null) {
                    Map<TransportType, ChannelDirection> typeMap = new HashMap<>(1);
                    typeMap.put(type, direction);
                    executor.submit(new OperationRunnable(typeMap));
                } else {
                    LOG.error("Unsupported type {} for channel {}", type, getId());
                }
            } else {
                lastConnectionFailed = true;
                LOG.warn("Can't sync. Server is null");
            }
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
            if (currentServer != null) {
                executor.submit(new OperationRunnable(SUPPORTED_TYPES));
            } else {
                lastConnectionFailed = true;
                LOG.warn("Can't sync. Server is null");
            }
        }
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public ChannelType getType() {
        return ChannelType.HTTP;
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
            this.currentServer = (HttpServerInfo) server;
            this.httpClient = client.createHttpClient(currentServer.getURL(), state.getPrivateKey(), state.getPublicKey(), currentServer.getPublicKey());
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
